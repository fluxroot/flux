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
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.MoveList;
import com.fluxchess.flux.move.MoveSee;
import com.fluxchess.flux.table.HistoryTable;
import com.fluxchess.flux.table.KillerTable;

import java.util.concurrent.RecursiveTask;

abstract class AbstractSearchTask extends RecursiveTask<Integer> {

  protected final KillerTable killerTable;
  protected final HistoryTable historyTable;

  protected AbstractSearchTask(KillerTable killerTable, HistoryTable historyTable) {
    this.killerTable = killerTable;
    this.historyTable = historyTable;
  }

  private void updateSearch(int height) {
    assert transpositionTable.getPermillUsed() >= 0 && transpositionTable.getPermillUsed() <= 1000;

    info.totalNodes++;
    info.setCurrentMaxDepth(height);
    info.sendInformationStatus();

    if (searchNodes > 0 && searchNodes <= info.totalNodes) {
      // Hard stop on number of nodes
      stopped = true;
    }

    // Reset
    pvList[height].resetList();
  }

  protected boolean isDangerousMove(Hex88Board board, int move) {
    int chessman = IntMove.getChessman(move);
    int relativeRank = IntPosition.getRelativeRank(IntMove.getEnd(move), board.activeColor);
    if (chessman == IntChessman.PAWN && relativeRank >= IntPosition.rank7) {
      return true;
    }

    int target = IntMove.getTarget(move);
    if (target == IntChessman.QUEEN) {
      return true;
    }

    return false;
  }

  /**
   * Returns the new possibly extended search depth.
   *
   * @param board         the current board.
   * @param depth         the current depth.
   * @param move          the current move.
   * @param isSingleReply whether we are in check and have only one way out.
   * @param mateThreat    whether we have a mate threat.
   * @return the new possibly extended search depth.
   */
  protected int getNewDepth(Hex88Board board, int depth, int move, boolean isSingleReply, boolean mateThreat) {
    int newDepth = depth - 1;

    assert (IntMove.getEnd(move) != board.captureSquare) || (IntMove.getTarget(move) != IntChessman.NOPIECE);

    //## Recapture Extension
    if (Configuration.useRecaptureExtension
      && IntMove.getEnd(move) == board.captureSquare
      && MoveSee.seeMove(move, IntMove.getChessmanColor(move)) > 0) {
      newDepth++;
    }

    //## Check Extension
    else if (Configuration.useCheckExtension
      && board.isCheckingMove(move)) {
      newDepth++;
    }

    //## Pawn Extension
    else if (Configuration.usePawnExtension
      && IntMove.getChessman(move) == IntChessman.PAWN
      && IntPosition.getRelativeRank(IntMove.getEnd(move), board.activeColor) == IntPosition.rank7) {
      newDepth++;
    }

    //## Single-Reply Extension
    else if (Configuration.useSingleReplyExtension
      && isSingleReply) {
      newDepth++;
    }

    //## Mate Threat Extension
    else if (Configuration.useMateThreatExtension
      && mateThreat) {
      newDepth++;
    }

    // Extend another ply if we enter a pawn endgame
    if (board.materialCount[board.activeColor] == 0
      && board.materialCount[IntColor.switchColor(board.activeColor)] == 1
      && IntMove.getTarget(move) != IntChessman.NOPIECE
      && IntMove.getTarget(move) != IntChessman.PAWN) {
      newDepth++;
    }

    return newDepth;
  }

  private void addPv(MoveList destination, MoveList source, int move) {
    assert destination != null;
    assert source != null;
    assert move != IntMove.NOMOVE;

    destination.resetList();

    destination.move[destination.tail++] = move;

    for (int i = source.head; i < source.tail; i++) {
      destination.move[destination.tail++] = source.move[i];
    }
  }

  protected void addGoodMove(int move, int depth, int height) {
    assert move != IntMove.NOMOVE;

    if (IntMove.getTarget(move) != IntChessman.NOPIECE) {
      return;
    }

    int type = IntMove.getType(move);
    if (type == IntMove.PAWNPROMOTION || type == IntMove.NULL) {
      return;
    }

    assert type != IntMove.ENPASSANT;

    killerTable.add(move, height);
    historyTable.add(move, depth);
  }

}
