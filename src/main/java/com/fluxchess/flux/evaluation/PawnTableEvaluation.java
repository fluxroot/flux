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
package com.fluxchess.flux.evaluation;

import com.fluxchess.flux.board.BitPieceList;
import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.Position;

import java.util.Arrays;

public final class PawnTableEvaluation {

  // Our pawn structure table. 8 + 2 -> 2 Sentinels for each side.
  public final byte[][] pawnTable = new byte[IntColor.ARRAY_DIMENSION][10];

  private static final PawnTableEvaluation instance = new PawnTableEvaluation();

  private PawnTableEvaluation() {
  }

  public static PawnTableEvaluation getInstance() {
    return instance;
  }

  public void createPawnTable(int myColor, Board board) {
    assert myColor != IntColor.NOCOLOR;

    // Zero our table
    Arrays.fill(pawnTable[myColor], (byte) 0);

    // Initialize
    byte[] myPawnTable = pawnTable[myColor];

    // Evaluate each pawn
    for (long positions = board.pawnList[myColor].list; positions != 0; positions &= positions - 1) {
      int pawnPosition = BitPieceList.next(positions);
      int pawnFile = Position.getFile(pawnPosition);
      int pawnRank = Position.getRank(pawnPosition);

      // Fill pawn table
      int tableFile = pawnFile + 1;
      if (myPawnTable[tableFile] == 0
        || (myPawnTable[tableFile] > pawnRank && myColor == IntColor.WHITE)
        || (myPawnTable[tableFile] < pawnRank && myColor == IntColor.BLACK)) {
        // Set the rank to the lowest pawn rank
        myPawnTable[tableFile] = (byte) pawnRank;
      }
    }
  }

}
