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

final class BishopEvaluation {

  public static int EVAL_BISHOP_MOBILITY_BASE = -6;
  public static int EVAL_BISHOP_MOBILITYFACTOR = 5;
  public static int EVAL_BISHOP_SAFETY = 10;
  public static int EVAL_BISHOP_PAIR = 50;

  private BishopEvaluation() {
  }

  public static int evaluateBishop(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int total = 0;
    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];

    // Evaluate each bishop
    for (long squares = board.bishopList[myColor]; squares != 0; squares &= squares - 1) {
      int bishopSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      int allMobility = EVAL_BISHOP_MOBILITY_BASE;

      // Evaluate mobility
      for (int delta : MoveGenerator.moveDeltaBishop) {
        int targetSquare = bishopSquare + delta;
        while ((targetSquare & 0x88) == 0) {
          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            allMobility++;
            targetSquare += delta;
          } else {
            if (IntPiece.getColor(target) == enemyColor) {
              allMobility++;
            }
            break;
          }
        }
      }

      // Evaluate mobility
      total += EVAL_BISHOP_MOBILITYFACTOR * allMobility;

      // Evaluate safety
      if ((enemyAttackTable[bishopSquare] & AttackTableEvaluation.BIT_PAWN) == 0) {
        total += EVAL_BISHOP_SAFETY;
      }
    }

    // Evaluate bishop pair
    if (Long.bitCount(board.bishopList[myColor]) >= 2) {
      total += EVAL_BISHOP_PAIR;
    }

    return total;
  }

}
