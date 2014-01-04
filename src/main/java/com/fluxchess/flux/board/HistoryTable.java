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
package com.fluxchess.flux.board;

import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

public final class HistoryTable {

  public static final int MAX_HISTORYVALUE = 65536;

  private final int[][] historyTable = new int[IntPiece.values.length][Board.BOARDSIZE];

  /**
   * Returns the number of hits for the move.
   *
   * @param move the Move.
   * @return the number of hits.
   */
  public int get(int move) {
    assert move != Move.NOMOVE;

    int piece = Move.getOriginPiece(move);
    int end = Move.getEnd(move);
    assert Move.getOriginChessman(move) != IntChessman.NOCHESSMAN;
    assert Move.getOriginColor(move) != IntColor.NOCOLOR;
    assert (end & 0x88) == 0;

    return historyTable[IntPiece.ordinal(piece)][end];
  }

  /**
   * Increment the number of hits for this move.
   *
   * @param move the Move.
   */
  public void add(int move, int depth) {
    assert move != Move.NOMOVE;

    int piece = Move.getOriginPiece(move);
    int end = Move.getEnd(move);
    assert Move.getOriginChessman(move) != IntChessman.NOCHESSMAN;
    assert Move.getOriginColor(move) != IntColor.NOCOLOR;
    assert (end & 0x88) == 0;

    historyTable[IntPiece.ordinal(piece)][end] += depth;

    if (historyTable[IntPiece.ordinal(piece)][end] >= MAX_HISTORYVALUE) {
      for (int pieceValue : IntPiece.values) {
        for (int positionValue : Position.values) {
          historyTable[IntPiece.ordinal(pieceValue)][positionValue] /= 2;
        }
      }
    }
  }

}
