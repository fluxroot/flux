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
import com.fluxchess.flux.board.MoveGenerator;
import com.fluxchess.flux.board.Square;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

final class KnightEvaluation {

  public static int EVAL_KNIGHT_MOBILITY_BASE = -4;
  public static int EVAL_KNIGHT_MOBILITYFACTOR = 4;
  public static int EVAL_KNIGHT_SAFETY = 10;

  private KnightEvaluation() {
  }

  public static int evaluateKnight(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int total = 0;
    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];

    // Evaluate each knight
    for (long squares = board.knightList[myColor]; squares != 0; squares &= squares - 1) {
      int knightSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      int allMobility = EVAL_KNIGHT_MOBILITY_BASE;

      // Evaluate mobility
      for (int delta : MoveGenerator.moveDeltaKnight) {
        int targetSquare = knightSquare + delta;
        if ((targetSquare & 0x88) == 0) {
          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            allMobility++;
          } else {
            if (IntPiece.getColor(target) == enemyColor) {
              allMobility++;
            }
          }
        }
      }

      // Evaluate mobility
      total += EVAL_KNIGHT_MOBILITYFACTOR * allMobility;

      // Evaluate safety
      if ((enemyAttackTable[knightSquare] & AttackTableEvaluation.BIT_PAWN) == 0) {
        total += EVAL_KNIGHT_SAFETY;
      }
    }

    return total;
  }

}
