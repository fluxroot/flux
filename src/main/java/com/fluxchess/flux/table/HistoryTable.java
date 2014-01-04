/*
 * Copyright 2007-2014 the original author or authors.
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
package com.fluxchess.flux.table;

import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.flux.move.Move;

public final class HistoryTable {

  public static final int MAX_HISTORYVALUE = 65536;

  private final int[][] historyTable = new int[IntChessman.PIECE_VALUE_SIZE][Board.BOARDSIZE];

  /**
   * Returns the number of hits for the move.
   *
   * @param move the Move.
   * @return the number of hits.
   */
  public int get(int move) {
    assert move != Move.NOMOVE;

    int piece = Move.getChessmanPiece(move);
    int end = Move.getEnd(move);
    assert Move.getChessman(move) != IntChessman.NOPIECE;
    assert Move.getChessmanColor(move) != IntColor.NOCOLOR;
    assert (end & 0x88) == 0;

    return historyTable[piece][end];
  }

  /**
   * Increment the number of hits for this move.
   *
   * @param move the Move.
   */
  public void add(int move, int depth) {
    assert move != Move.NOMOVE;

    int piece = Move.getChessmanPiece(move);
    int end = Move.getEnd(move);
    assert Move.getChessman(move) != IntChessman.NOPIECE;
    assert Move.getChessmanColor(move) != IntColor.NOCOLOR;
    assert (end & 0x88) == 0;

    historyTable[piece][end] += depth;

    if (historyTable[piece][end] >= MAX_HISTORYVALUE) {
      for (int pieceValue : IntChessman.pieceValues) {
        for (int positionValue : IntPosition.values) {
          historyTable[pieceValue][positionValue] /= 2;
        }
      }
    }
  }

}
