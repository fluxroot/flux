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
import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.move.*;
import com.fluxchess.flux.table.EvaluationTable;
import com.fluxchess.flux.table.PawnTable;
import com.fluxchess.flux.table.TranspositionTable;
import com.fluxchess.flux.table.TranspositionTableEntry;
import com.fluxchess.jcpi.models.GenericMove;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class AlphaBetaRootTask extends AbstractSearchTask {

  private int depth;
  private int alpha;
  private int beta;
  private int height;
  private MoveList rootMoveList;
  private boolean isCheck;
  private Result moveResult;
  private AtomicBoolean canStop;
  private final Hex88Board board;
  private final TranspositionTable transpositionTable;
  private final EvaluationTable evaluationTable;
  private final PawnTable pawnTable;

  private final Evaluation evaluation;
  private final MoveGenerator moveGenerator;

  public AlphaBetaRootTask(
    int depth,
    int alpha,
    int beta,
    int height,
    MoveList rootMoveList,
    boolean isCheck,
    Result moveResult,
    Hex88Board board,
    Parameter parameter
  ) {
    super(parameter);

    this.depth = depth;
    this.alpha = alpha;
    this.beta = beta;
    this.height = height;
    this.rootMoveList = rootMoveList;
    this.isCheck = isCheck;
    this.moveResult = moveResult;
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
    updateSearch(height);

    // Abort conditions
    if ((stopped.get() && canStop.get()) || height == Search.MAX_HEIGHT) {
      return evaluation.evaluate(board);
    }

    // Initialize
    int hashType = IntScore.ALPHA;
    int bestValue = -Search.INFINITY;
    int bestMove = IntMove.NOMOVE;
    int oldAlpha = alpha;
    PrincipalVariation lastMultiPv = null;
    PrincipalVariation bestPv = null;
    PrincipalVariation firstPv = null;

    // Initialize the move number
    int currentMoveNumber = 0;

    // Initialize Single-Response Extension
    boolean isSingleReply = isCheck && rootMoveList.getLength() == 1;

    for (int j = rootMoveList.head; j < rootMoveList.tail; j++) {
      int move = rootMoveList.move[j];

      // Update the information if we evaluate a new move.
      currentMoveNumber++;
      info.sendInformationMove(IntMove.toGenericMove(move), currentMoveNumber);

      // Extension
      int newDepth = getNewDepth(board, depth, move, isSingleReply, false);

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (bestValue == -Search.INFINITY) {
        // First move
        value = -new AlphaBetaTask(newDepth, -beta, -alpha, height + 1, true, true, new Hex88Board(board), parameter).invoke();
      } else {
        value = -new AlphaBetaTask(newDepth, -alpha - 1, -alpha, height + 1, false, true, new Hex88Board(board), parameter).invoke();
        if (value > alpha && value < beta) {
          // Research again
          value = -new AlphaBetaTask(newDepth, -beta, -alpha, height + 1, true, true, new Hex88Board(board), parameter).invoke();
        }
      }
      //## ENDOF Principal Variation Search

      // Undo move
      board.undoMove(move);

      if (stopped.get() && canStop.get()) {
        break;
      }

      // Store value
      int sortValue;
      int moveType;
      if (value <= alpha) {
        value = alpha;
        moveType = IntScore.ALPHA;
        rootMoveList.value[j] = oldAlpha;
        sortValue = -Search.INFINITY;
      } else if (value >= beta) {
        value = beta;
        moveType = IntScore.BETA;
        rootMoveList.value[j] = beta;
        sortValue = Search.INFINITY;
      } else {
        moveType = IntScore.EXACT;
        rootMoveList.value[j] = value;
        sortValue = value;
      }

      // Add pv to list
      List<GenericMove> genericMoveList = transpositionTable.getMoveList(board, depth, new ArrayList<GenericMove>());
      PrincipalVariation pv = new PrincipalVariation(
        currentMoveNumber,
        value,
        moveType,
        sortValue,
        genericMoveList,
        info.getCurrentDepth(),
        info.getCurrentMaxDepth(),
        transpositionTable.getPermillUsed(),
        info.getCurrentNps(),
        System.currentTimeMillis() - info.getTotalTimeStart(),
        info.getTotalNodes());
      parameter.multiPvMap.put(move, pv);

      // Save first pv
      if (currentMoveNumber == 1) {
        firstPv = pv;
      }

      // Show refutations
      if (Configuration.showRefutations) {
        info.sendInformationRefutations(genericMoveList);
      }

      // Show multi pv
      if (parameter.showPvNumber > 1) {
        assert currentMoveNumber <= parameter.showPvNumber || lastMultiPv != null;
        if (currentMoveNumber <= parameter.showPvNumber || pv.compareTo(lastMultiPv) < 0) {
          PriorityQueue<PrincipalVariation> tempPvList = new PriorityQueue<>(parameter.multiPvMap.values());
          for (int i = 1; i <= parameter.showPvNumber && !tempPvList.isEmpty(); i++) {
            lastMultiPv = tempPvList.remove();
            info.sendInformation(lastMultiPv, i);
          }
        }
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        bestMove = move;

        // Do we have a better value?
        if (value > alpha) {
          bestPv = pv;
          hashType = IntScore.EXACT;
          alpha = value;

          if (depth > 1 && parameter.showPvNumber <= 1) {
            // Send pv information for depth > 1
            // Print the best move as soon as we get a new one
            // This is really an optimistic assumption
            info.sendInformation(bestPv, 1);
          }

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = IntScore.BETA;
            break;
          }
        }
      }

      if (parameter.showPvNumber > 1) {
        // Reset alpha to get the real value of the next move
        assert oldAlpha == -Search.CHECKMATE;
        alpha = oldAlpha;
      }
    }

    if (!(stopped.get() && canStop.get())) {
      transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, false, height);
    }

    if (depth == 1 && parameter.showPvNumber <= 1 && bestPv != null) {
      // Send pv information for depth 1
      // On depth 1 we have no move ordering available
      // To reduce the output we only print the best move here
      info.sendInformation(bestPv, 1);
    }

    if (parameter.showPvNumber <= 1 && bestPv == null && firstPv != null) {
      // We have a fail low
      assert oldAlpha == alpha;

      PrincipalVariation resultPv = new PrincipalVariation(
        firstPv.moveNumber,
        firstPv.value,
        firstPv.type,
        firstPv.sortValue,
        firstPv.pv,
        firstPv.depth,
        info.getCurrentMaxDepth(),
        transpositionTable.getPermillUsed(),
        info.getCurrentNps(),
        System.currentTimeMillis() - info.getTotalTimeStart(),
        info.getTotalNodes());
      info.sendInformation(resultPv, 1);
    }

    moveResult.bestMove = bestMove;
    moveResult.resultValue = bestValue;
    moveResult.value = hashType;
    moveResult.moveNumber = currentMoveNumber;

    if (Configuration.useTranspositionTable) {
      TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        for (int i = rootMoveList.head; i < rootMoveList.tail; i++) {
          if (rootMoveList.move[i] == entry.move) {
            rootMoveList.value[i] = Search.INFINITY;
            break;
          }
        }
      }
    }

    MoveSorter.sort(rootMoveList);

    return bestValue;
  }

}
