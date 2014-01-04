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

import com.fluxchess.flux.board.ChessmanList;
import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.MoveGenerator;

public final class KnightEvaluation {

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
    for (long positions = board.knightList[myColor].list; positions != 0; positions &= positions - 1) {
      int knightPosition = ChessmanList.next(positions);

      int allMobility = EVAL_KNIGHT_MOBILITY_BASE;

      // Evaluate mobility
      for (int delta : MoveGenerator.moveDeltaKnight) {
        int targetPosition = knightPosition + delta;
        if ((targetPosition & 0x88) == 0) {
          int target = board.board[targetPosition];
          if (target == IntChessman.NOPIECE) {
            allMobility++;
          } else {
            if (IntChessman.getColor(target) == enemyColor) {
              allMobility++;
            }
          }
        }
      }

      // Evaluate mobility
      total += EVAL_KNIGHT_MOBILITYFACTOR * allMobility;

      // Evaluate safety
      if ((enemyAttackTable[knightPosition] & AttackTableEvaluation.BIT_PAWN) == 0) {
        total += EVAL_KNIGHT_SAFETY;
      }
    }

    return total;
  }

}
