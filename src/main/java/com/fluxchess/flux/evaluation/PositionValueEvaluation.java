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

import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.Position;
import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntColor;

public final class PositionValueEvaluation {

  private static final int[][] positionValueOpening = {
    { // Empty
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Pawn
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,   15,   15,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,   25,   25,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,   15,   15,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Knight
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -50,  -40,  -30,  -25,  -25,  -30,  -40,  -50,     0,0,0,0,0,0,0,0,
         -35,  -25,  -15,  -10,  -10,  -15,  -25,  -35,     0,0,0,0,0,0,0,0,
         -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
         -10,    0,   10,   15,   15,   10,    0,  -10,     0,0,0,0,0,0,0,0,
          -5,    5,   15,   20,   20,   15,    5,   -5,     0,0,0,0,0,0,0,0,
          -5,    5,   15,   20,   20,   15,    5,   -5,     0,0,0,0,0,0,0,0,
         -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
        -135,  -25,  -15,  -10,  -10,  -15,  -25, -135,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // King
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
          40,   50,   30,   10,   10,   30,   50,   40,     0,0,0,0,0,0,0,0,
          30,   40,   20,    0,    0,   20,   40,   30,     0,0,0,0,0,0,0,0,
          10,   20,    0,  -20,  -20,    0,   20,   10,     0,0,0,0,0,0,0,0,
           0,   10,  -10,  -30,  -30,  -10,   10,    0,     0,0,0,0,0,0,0,0,
         -10,    0,  -20,  -40,  -40,  -20,    0,  -10,     0,0,0,0,0,0,0,0,
         -20,  -10,  -30,  -50,  -50,  -30,  -10,  -20,     0,0,0,0,0,0,0,0,
         -30,  -20,  -40,  -60,  -60,  -40,  -20,  -30,     0,0,0,0,0,0,0,0,
         -40,  -30,  -50,  -70,  -70,  -50,  -30,  -40,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Empty
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Bishop
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -18,  -18,   16,  -14,  -14,  -16,  -18,  -18,     0,0,0,0,0,0,0,0,
          -8,    0,   -2,    0,    0,   -2,    0,   -8,     0,0,0,0,0,0,0,0,
          -6,   -2,    4,    2,    2,    4,   -2,   -6,     0,0,0,0,0,0,0,0,
          -4,    0,    2,    8,    8,    2,    0,   -4,     0,0,0,0,0,0,0,0,
          -4,    0,    2,    8,    8,    2,    0,   -4,     0,0,0,0,0,0,0,0,
          -6,   -2,    4,    2,    2,    4,   -2,   -6,     0,0,0,0,0,0,0,0,
          -8,    0,   -2,    0,    0,   -2,    0,   -8,     0,0,0,0,0,0,0,0,
          -8,   -8,   -6,   -4,   -4,   -6,   -8,   -8,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Rook
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
          -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Queen
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
          -5,   -5,   -5,   -5,   -5,   -5,   -5,   -5,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    }
  };

  private static final int[][] positionValueEndgame = {
    { // Empty
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Pawn
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Knight
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -40,  -30,  -20,  -15,  -15,  -20,  -30,  -40,     0,0,0,0,0,0,0,0,
         -30,  -20,  -10,   -5,   -5,  -10,  -20,  -30,     0,0,0,0,0,0,0,0,
         -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
         -15,   -5,    5,   10,   10,    5,   -5,  -15,     0,0,0,0,0,0,0,0,
         -15,   -5,    5,   10,   10,    5,   -5,  -15,     0,0,0,0,0,0,0,0,
         -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
         -30,  -20,  -10,   -5,   -5,  -10,  -20,  -30,     0,0,0,0,0,0,0,0,
         -40,  -30,  -20,  -15,  -15,  -20,  -30,  -40,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // King
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -72,  -48,  -36,  -24,  -24,  -36,  -48,  -72,     0,0,0,0,0,0,0,0,
         -48,  -24,  -12,    0,    0,  -12,  -24,  -48,     0,0,0,0,0,0,0,0,
         -36,  -12,    0,   12,   12,    0,  -12,  -36,     0,0,0,0,0,0,0,0,
         -24,    0,   12,   24,   24,   12,    0,  -24,     0,0,0,0,0,0,0,0,
         -24,    0,   12,   24,   24,   12,    0,  -24,     0,0,0,0,0,0,0,0,
         -36,  -12,    0,   12,   12,    0,  -12,  -36,     0,0,0,0,0,0,0,0,
         -48,  -24,  -12,    0,    0,  -12,  -24,  -48,     0,0,0,0,0,0,0,0,
         -72,  -48,  -36,  -24,  -24,  -36,  -48,  -72,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Empty
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Bishop
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -18,  -12,   -9,   -6,   -6,   -9,  -12,  -18,     0,0,0,0,0,0,0,0,
         -12,   -6,   -3,    0,    0,   -3,   -6,  -12,     0,0,0,0,0,0,0,0,
          -9,   -3,    0,    3,    3,    0,   -3,   -9,     0,0,0,0,0,0,0,0,
           6,    0,    3,    6,    6,    3,    0,    6,     0,0,0,0,0,0,0,0,
           6,    0,    3,    6,    6,    3,    0,    6,     0,0,0,0,0,0,0,0,
          -9,   -3,    0,    3,    3,    0,   -3,   -9,     0,0,0,0,0,0,0,0,
         -12,   -6,   -3,    0,    0,   -3,   -6,  -12,     0,0,0,0,0,0,0,0,
         -18,  -12,   -9,   -6,   -6,   -9,  -12,  -18,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Rook
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
           0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    },
    { // Queen
      //  a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
         -24,  -16,  -12,   -8,   -8,  -12,  -16,  -24,     0,0,0,0,0,0,0,0,
         -16,   -8,   -4,    0,    0,   -4,   -8,  -16,     0,0,0,0,0,0,0,0,
         -12,   -4,    0,    4,    4,    0,   -4,  -12,     0,0,0,0,0,0,0,0,
          -8,    0,    4,    8,    8,    4,    0,   -8,     0,0,0,0,0,0,0,0,
          -8,    0,    4,    8,    8,    4,    0,   -8,     0,0,0,0,0,0,0,0,
         -12,   -4,    0,    4,    4,    0,   -4,  -12,     0,0,0,0,0,0,0,0,
         -16,   -8,   -4,    0,    0,   -4,   -8,  -16,     0,0,0,0,0,0,0,0,
         -24,  -16,  -12,   -8,   -8,  -12,  -16,  -24,     0,0,0,0,0,0,0,0
      //  a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
    }
  };

  private PositionValueEvaluation() {
  }

  public static int evaluatePositionValue(int myColor, Board board) {
    assert myColor != IntColor.NOCOLOR;

    // Initialize
    int opening = 0;
    int endgame = 0;

    // Pawns
    int[] chessmanValueOpening = positionValueOpening[IntChessman.PAWN];
    int[] chessmanValueEndgame = positionValueEndgame[IntChessman.PAWN];
    for (long positions = board.pawnList[myColor]; positions != 0; positions &= positions - 1) {
      int position = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      if (myColor == IntColor.BLACK) {
        position = 127 - 8 - position;
      } else {
        assert myColor == IntColor.WHITE;
      }
      opening += chessmanValueOpening[position];
      endgame += chessmanValueEndgame[position];
    }

    // Knights
    chessmanValueOpening = positionValueOpening[IntChessman.KNIGHT];
    chessmanValueEndgame = positionValueEndgame[IntChessman.KNIGHT];
    for (long positions = board.knightList[myColor]; positions != 0; positions &= positions - 1) {
      int position = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      if (myColor == IntColor.BLACK) {
        position = 127 - 8 - position;
      } else {
        assert myColor == IntColor.WHITE;
      }
      opening += chessmanValueOpening[position];
      endgame += chessmanValueEndgame[position];
    }

    // Bishops
    chessmanValueOpening = positionValueOpening[IntChessman.BISHOP];
    chessmanValueEndgame = positionValueEndgame[IntChessman.BISHOP];
    for (long positions = board.bishopList[myColor]; positions != 0; positions &= positions - 1) {
      int position = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      if (myColor == IntColor.BLACK) {
        position = 127 - 8 - position;
      } else {
        assert myColor == IntColor.WHITE;
      }
      opening += chessmanValueOpening[position];
      endgame += chessmanValueEndgame[position];
    }

    // Rooks
    chessmanValueOpening = positionValueOpening[IntChessman.ROOK];
    chessmanValueEndgame = positionValueEndgame[IntChessman.ROOK];
    for (long positions = board.rookList[myColor]; positions != 0; positions &= positions - 1) {
      int position = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      if (myColor == IntColor.BLACK) {
        position = 127 - 8 - position;
      } else {
        assert myColor == IntColor.WHITE;
      }
      opening += chessmanValueOpening[position];
      endgame += chessmanValueEndgame[position];
    }

    // Queens
    chessmanValueOpening = positionValueOpening[IntChessman.QUEEN];
    chessmanValueEndgame = positionValueEndgame[IntChessman.QUEEN];
    for (long positions = board.queenList[myColor]; positions != 0; positions &= positions - 1) {
      int position = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      if (myColor == IntColor.BLACK) {
        position = 127 - 8 - position;
      } else {
        assert myColor == IntColor.WHITE;
      }
      opening += chessmanValueOpening[position];
      endgame += chessmanValueEndgame[position];
    }

    // King
    assert Long.bitCount(board.kingList[myColor]) == 1;
    int position = Position.toX88Position(Long.numberOfTrailingZeros(board.kingList[myColor]));
    if (myColor == IntColor.BLACK) {
      position = 127 - 8 - position;
    } else {
      assert myColor == IntColor.WHITE;
    }
    opening += positionValueOpening[IntChessman.KING][position];
    endgame += positionValueEndgame[IntChessman.KING][position];

    // Return linear mix
    return Evaluation.getGamePhaseEvaluation(myColor, opening, endgame, board);
  }

}
