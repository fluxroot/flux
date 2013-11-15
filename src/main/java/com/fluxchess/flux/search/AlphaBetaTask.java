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
import com.fluxchess.flux.board.IntGamePhase;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.IntScore;
import com.fluxchess.flux.move.MoveGenerator;
import com.fluxchess.flux.table.EvaluationTable;
import com.fluxchess.flux.table.PawnTable;
import com.fluxchess.flux.table.TranspositionTable;
import com.fluxchess.flux.table.TranspositionTableEntry;

import java.util.concurrent.atomic.AtomicBoolean;

class AlphaBetaTask extends AbstractSearchTask {

  private static final int NULLMOVE_DEPTH = 2;
  private static final int NULLMOVE_REDUCTION;
  private static final int NULLMOVE_VERIFICATIONREDUCTION = 3;

  private static final int IID_DEPTH = 2;

  private static final int LMR_DEPTH = 3;
  private static final int LMR_MOVENUMBER_MINIMUM = 3;

  private static final int FUTILITY_FRONTIERMARGIN = 2 * IntChessman.VALUE_PAWN;
  private static final int FUTILITY_PREFRONTIERMARGIN = IntChessman.VALUE_ROOK;

  private int depth;
  private int alpha;
  private int beta;
  private int height;
  private boolean pvNode;
  private boolean doNull;
  private final AtomicBoolean canStop;
  private final Hex88Board board;
  private final TranspositionTable transpositionTable;
  private final EvaluationTable evaluationTable;
  private final PawnTable pawnTable;

  private final Evaluation evaluation;
  private final MoveGenerator moveGenerator;

  // Static initialization
  static {
    if (Configuration.useVerifiedNullMovePruning) {
      NULLMOVE_REDUCTION = 3;
    } else {
      NULLMOVE_REDUCTION = 2;
    }
  }

  public AlphaBetaTask(
    int depth,
    int alpha,
    int beta,
    int height,
    boolean pvNode,
    boolean doNull,
    Hex88Board board,
    Parameter parameter
  ) {
    super(parameter);

    this.depth = depth;
    this.alpha = alpha;
    this.beta = beta;
    this.height = height;
    this.pvNode = pvNode;
    this.doNull = doNull;
    this.board = board;
    this.canStop = parameter.canStop;
    this.transpositionTable = parameter.transpositionTable;
    this.evaluationTable = parameter.evaluationTable;
    this.pawnTable = parameter.pawnTable;

    evaluation = new Evaluation(evaluationTable, pawnTable);
    moveGenerator = new MoveGenerator(board, killerTable, historyTable);
  }

  @Override
  protected Integer compute() {
    // We are at a leaf/horizon. So calculate that value.
    if (depth <= 0) {
      // Descend into quiescent
      return new QuiescentTask(0, alpha, beta, height, pvNode, true, new Hex88Board(board), parameter).invoke();
    }

    updateSearch(height);

    // Abort conditions
    if ((stopped.get() && canStop.get()) || height == Search.MAX_HEIGHT) {
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
    int transpositionMove = IntMove.NOMOVE;
    boolean mateThreat = false;
    if (Configuration.useTranspositionTable) {
      TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        transpositionMove = entry.move;
        mateThreat = entry.mateThreat;

        if (!pvNode && entry.depth >= depth) {
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
    }

    // Get the attack
    // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
    // idea.
    Attack attack = board.getAttack(board.activeColor);
    boolean isCheck = attack.isCheck();

    //## BEGIN Null-Move Pruning
    // Notes: Ideas from http://www.cs.biu.ac.il/~davoudo/pubs/vrfd_null.html
    int evalValue = Search.INFINITY;
    if (Configuration.useNullMovePruning) {
      if (!pvNode
        && depth >= NULLMOVE_DEPTH
        && doNull
        && !isCheck
        && !mateThreat
        && board.getGamePhase() != IntGamePhase.ENDGAME
        && (evalValue = evaluation.evaluate(board)) >= beta) {
        // Depth reduction
        int newDepth = depth - 1 - NULLMOVE_REDUCTION;

        // Make the null move
        board.makeMove(IntMove.NULLMOVE);
        int value = -new AlphaBetaTask(newDepth, -beta, -beta + 1, height + 1, false, false, new Hex88Board(board), parameter).invoke();
        board.undoMove(IntMove.NULLMOVE);

        // Verify on beta exceeding
        if (Configuration.useVerifiedNullMovePruning) {
          if (depth > NULLMOVE_VERIFICATIONREDUCTION) {
            if (value >= beta) {
              newDepth = depth - NULLMOVE_VERIFICATIONREDUCTION;

              // Verify
              value = new AlphaBetaTask(newDepth, alpha, beta, height, true, false, new Hex88Board(board), parameter).invoke();

              if (value >= beta) {
                // Cut-off

                return value;
              }
            }
          }
        }

        // Check for mate threat
        if (value < -Search.CHECKMATE_THRESHOLD) {
          mateThreat = true;
        }

        if (value >= beta) {
          // Do not return unproven mate values
          if (value > Search.CHECKMATE_THRESHOLD) {
            value = Search.CHECKMATE_THRESHOLD;
          }

          if (!(stopped.get() && canStop.get())) {
            // Store the value into the transposition table
            transpositionTable.put(board.zobristCode, depth, value, IntScore.BETA, IntMove.NOMOVE, mateThreat, height);
          }

          return value;
        }
      }
    }
    //## ENDOF Null-Move Forward Pruning

    // Initialize
    int hashType = IntScore.ALPHA;
    int bestValue = -Search.INFINITY;
    int bestMove = IntMove.NOMOVE;
    int searchedMoves = 0;

    //## BEGIN Internal Iterative Deepening
    if (Configuration.useInternalIterativeDeepening) {
      if (pvNode
        && depth >= IID_DEPTH
        && transpositionMove == IntMove.NOMOVE
        // alpha is not equal the initial -CHECKMATE anymore, because of depth >= IID_DEPTH
        // so alpha has a real value. Don't do IID if it's a checkmate value
        && Math.abs(alpha) < Search.CHECKMATE_THRESHOLD) {
        int oldAlpha = alpha;
        int oldBeta = beta;
        alpha = -Search.CHECKMATE;
        beta = Search.CHECKMATE;

        for (int newDepth = 1; newDepth < depth; newDepth++) {
          new AlphaBetaTask(newDepth, alpha, beta, height, true, false, new Hex88Board(board), parameter).invoke();

          if (stopped.get() && canStop.get()) {
            return oldAlpha;
          }
        }

        alpha = oldAlpha;
        beta = oldBeta;

        if (pvList[height].getLength() > 0) {
          // Hopefully we have a transposition move now
          transpositionMove = pvList[height].move[pvList[height].head];
        }
      }
    }
    //## ENDOF Internal Iterative Deepening

    // Initialize the move generator
    moveGenerator.initializeMain(attack, height, transpositionMove);

    // Initialize Single-Response Extension
    boolean isSingleReply = isCheck && attack.numberOfMoves == 1;

    int move = IntMove.NOMOVE;
    while ((move = moveGenerator.getNextMove()) != IntMove.NOMOVE) {
      //## BEGIN Minor Promotion Pruning
      if (Configuration.useMinorPromotionPruning
        && !parameter.analyzeMode
        && IntMove.getType(move) == IntMove.PAWNPROMOTION
        && IntMove.getPromotion(move) != IntChessman.QUEEN) {
        assert IntMove.getPromotion(move) == IntChessman.ROOK || IntMove.getPromotion(move) == IntChessman.BISHOP || IntMove.getPromotion(move) == IntChessman.KNIGHT;
        continue;
      }
      //## ENDOF Minor Promotion Pruning

      // Extension
      int newDepth = getNewDepth(board, depth, move, isSingleReply, mateThreat);

      //## BEGIN Extended Futility Pruning
      // Notes: Ideas from http://supertech.lcs.mit.edu/~heinz/dt/node18.html
      if (Configuration.useExtendedFutilityPruning) {
        if (!pvNode
          && depth == 2
          && newDepth == 1
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && !isDangerousMove(board, move)) {
          assert !board.isCheckingMove(move);
          assert IntMove.getType(move) != IntMove.PAWNPROMOTION : board.getBoard() + ", " + IntMove.toString(move);

          if (evalValue == Search.INFINITY) {
            // Store evaluation
            evalValue = evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_PREFRONTIERMARGIN;

          // Add the target value to the eval
          int target = IntMove.getTarget(move);
          if (target != IntChessman.NOPIECE) {
            value += IntChessman.getValueFromChessman(target);
          }

          // If we cannot reach alpha do not look at the move
          if (value <= alpha) {
            if (value > bestValue) {
              bestValue = value;
            }
            continue;
          }
        }
      }
      //## ENDOF Extended Futility Pruning

      //## BEGIN Futility Pruning
      // Notes: Ideas from http://supertech.lcs.mit.edu/~heinz/dt/node18.html
      if (Configuration.useFutilityPruning) {
        if (!pvNode
          && depth == 1
          && newDepth == 0
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && !isDangerousMove(board, move)) {
          assert !board.isCheckingMove(move);
          assert IntMove.getType(move) != IntMove.PAWNPROMOTION : board.getBoard() + ", " + IntMove.toString(move);

          if (evalValue == Search.INFINITY) {
            // Store evaluation
            evalValue = evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_FRONTIERMARGIN;

          // Add the target value to the eval
          int target = IntMove.getTarget(move);
          if (target != IntChessman.NOPIECE) {
            value += IntChessman.getValueFromChessman(target);
          }

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

      //## BEGIN Late Move Reduction
      // Notes: Ideas from: http://www.glaurungchess.com/lmr.html
      boolean reduced = false;
      if (Configuration.useLateMoveReduction) {
        if (!pvNode
          && searchedMoves >= LMR_MOVENUMBER_MINIMUM
          && depth >= LMR_DEPTH
          && newDepth < depth
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && IntMove.getTarget(move) == IntChessman.NOPIECE
          && !isDangerousMove(board, move)) {
          assert !board.isCheckingMove(move);
          assert IntMove.getType(move) != IntMove.PAWNPROMOTION : board.getBoard() + ", " + IntMove.toString(move);

          newDepth--;
          reduced = true;
        }
      }
      //## ENDOF Late Move Reduction

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (!pvNode || bestValue == -Search.INFINITY) {
        // First move
        value = -new AlphaBetaTask(newDepth, -beta, -alpha, height + 1, pvNode, true, new Hex88Board(board), parameter).invoke();
      } else {
        if (newDepth >= depth) {
          value = -new AlphaBetaTask(depth - 1, -alpha - 1, -alpha, height + 1, false, true, new Hex88Board(board), parameter).invoke();
        } else {
          value = -new AlphaBetaTask(newDepth, -alpha - 1, -alpha, height + 1, false, true, new Hex88Board(board), parameter).invoke();
        }
        if (value > alpha && value < beta) {
          // Research again
          value = -new AlphaBetaTask(newDepth, -beta, -alpha, height + 1, true, true, new Hex88Board(board), parameter).invoke();
        }
      }
      //## ENDOF Principal Variation Search

      //## BEGIN Late Move Reduction Research
      if (Configuration.useLateMoveReductionResearch) {
        if (reduced && value >= beta) {
          // Research with original depth
          newDepth++;
          value = -new AlphaBetaTask(newDepth, -beta, -alpha, height + 1, pvNode, true, new Hex88Board(board), parameter).invoke();
        }
      }
      //## ENDOF Late Move Reduction Research

      // Undo move
      board.undoMove(move);

      if (stopped.get() && canStop.get()) {
        break;
      }

      // Update
      searchedMoves++;

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        bestMove = move;
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

    // If we cannot move, check for checkmate and stalemate.
    if (bestValue == -Search.INFINITY) {
      if (isCheck) {
        // We have a check mate. This is bad for us, so return a -CHECKMATE.
        hashType = IntScore.EXACT;
        bestValue = -Search.CHECKMATE + height;
      } else {
        // We have a stale mate. Return the draw value.
        hashType = IntScore.EXACT;
        bestValue = Search.DRAW;
      }
    }

    if (!(stopped.get() && canStop.get())) {
      if (bestMove != IntMove.NOMOVE) {
        addGoodMove(bestMove, depth, height);
      }
      transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, mateThreat, height);
    }

    return bestValue;
  }

}
