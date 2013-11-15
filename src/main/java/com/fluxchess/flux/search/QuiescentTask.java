/*
 * Copyright 2007-2013 the original author or authors.
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.flux.search;

import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.board.Attack;
import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.IntScore;
import com.fluxchess.flux.move.MoveGenerator;
import com.fluxchess.flux.table.*;

class QuiescentTask extends AbstractSearchTask {

  private static final int FUTILITY_QUIESCENTMARGIN = IntChessman.VALUE_PAWN;

  private int checkingDepth;
  private int alpha;
  private int beta;
  private int height;
  private boolean pvNode;
  private boolean useTranspositionTable;
  private final Hex88Board board;
  private final TranspositionTable transpositionTable;
  private final EvaluationTable evaluationTable;
  private final PawnTable pawnTable;

  private final Evaluation evaluation;
  private final MoveGenerator moveGenerator;

  public QuiescentTask(
    int checkingDepth,
    int alpha,
    int beta,
    int height,
    boolean pvNode,
    boolean useTranspositionTable,
    Hex88Board board,
    TranspositionTable transpositionTable,
    EvaluationTable evaluationTable,
    PawnTable pawnTable,
    KillerTable killerTable,
    HistoryTable historyTable
  ) {
    super(killerTable, historyTable);

    this.checkingDepth = checkingDepth;
    this.alpha = alpha;
    this.beta = beta;
    this.height = height;
    this.pvNode = pvNode;
    this.useTranspositionTable = useTranspositionTable;
    this.board = board;
    this.transpositionTable = transpositionTable;
    this.evaluationTable = evaluationTable;
    this.pawnTable = pawnTable;

    evaluation = new Evaluation(evaluationTable, pawnTable);
    moveGenerator = new MoveGenerator(board, killerTable, historyTable);
  }

  @Override
  protected Integer compute() {
    updateSearch(height);

    // Abort conditions
    if ((stopped && canStop) || height == Search.MAX_HEIGHT) {
      return evaluation.evaluate(board);
    }

    // Check the repetition table and fifty move rule
    if (board.isRepetition() || board.halfMoveClock >= 100) {
      return Search.DRAW;
    }

    //## BEGIN Mate Distance Pruning
    if (Configuration.useMateDistancePruning) {
      int value = -Search.CHECKMATE + height;
      if (value > alpha) {
        alpha = value;
        if (value >= beta) {
          return value;
        }
      }
      value = -(-Search.CHECKMATE + height + 1);
      if (value < beta) {
        beta = value;
        if (value <= alpha) {
          return value;
        }
      }
    }
    //## ENDOF Mate Distance Pruning

    // Check the transposition table first
    if (Configuration.useTranspositionTable && useTranspositionTable) {
      TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        assert entry.depth >= checkingDepth;
        int value = entry.getValue(height);
        int type = entry.type;

        switch (type) {
          case IntScore.BETA:
            if (value >= beta) {
              return value;
            }
            break;
          case IntScore.ALPHA:
            if (value <= alpha) {
              return value;
            }
            break;
          case IntScore.EXACT:
            return value;
          default:
            assert false;
            break;
        }
      }
    }

    // Get the attack
    // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
    // idea.
    Attack attack = board.getAttack(board.activeColor);
    boolean isCheck = attack.isCheck();

    // Initialize
    int hashType = IntScore.ALPHA;
    int bestValue = -Search.INFINITY;
    int evalValue = Search.INFINITY;

    if (!isCheck) {
      // Stand pat
      int value = evaluation.evaluate(board);

      // Store evaluation
      evalValue = value;

      // Pruning
      bestValue = value;

      // Do we have a better value?
      if (value > alpha) {
        hashType = IntScore.EXACT;
        alpha = value;

        // Is the value higher than beta?
        if (value >= beta) {
          // Cut-off

          hashType = IntScore.BETA;

          if (useTranspositionTable) {
            assert checkingDepth == 0;
            transpositionTable.put(board.zobristCode, 0, bestValue, hashType, IntMove.NOMOVE, false, height);
          }

          return bestValue;
        }
      }
    } else {
      // Check Extension
      checkingDepth++;
    }

    // Initialize the move generator
    moveGenerator.initializeQuiescent(attack, checkingDepth >= 0);

    int move = IntMove.NOMOVE;
    while ((move = moveGenerator.getNextMove()) != IntMove.NOMOVE) {
      //## BEGIN Futility Pruning
      if (Configuration.useDeltaPruning) {
        if (!pvNode
          && !isCheck
          && !board.isCheckingMove(move)
          && !isDangerousMove(board, move)) {
          assert IntMove.getTarget(move) != IntChessman.NOPIECE;
          assert IntMove.getType(move) != IntMove.PAWNPROMOTION : board.getBoard() + ", " + IntMove.toString(move);

          int value = evalValue + FUTILITY_QUIESCENTMARGIN;

          // Add the target value to the eval
          value += IntChessman.getValueFromChessman(IntMove.getTarget(move));

          // If we cannot reach alpha do not look at the move
          if (value <= alpha) {
            if (value > bestValue) {
              bestValue = value;
            }
            continue;
          }
        }
      }
      //## ENDOF Futility Pruning

      // Do move
      board.makeMove(move);

      // Recurse into Quiescent
      int value = -new QuiescentTask(checkingDepth - 1, -beta, -alpha, height + 1, pvNode, false, new Hex88Board(board), transpositionTable, evaluationTable, pawnTable, killerTable, historyTable).invoke();

      // Undo move
      board.undoMove(move);

      if (stopped && canStop) {
        break;
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          hashType = IntScore.EXACT;
          alpha = value;

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = IntScore.BETA;
            break;
          }
        }
      }
    }

    moveGenerator.destroy();

    if (bestValue == -Search.INFINITY) {
      assert isCheck;

      // We have a check mate. This is bad for us, so return a -CHECKMATE.
      bestValue = -Search.CHECKMATE + height;
    }

    if (useTranspositionTable) {
      if (!(stopped && canStop)) {
        transpositionTable.put(board.zobristCode, 0, bestValue, hashType, IntMove.NOMOVE, false, height);
      }
    }

    return bestValue;
  }

}
