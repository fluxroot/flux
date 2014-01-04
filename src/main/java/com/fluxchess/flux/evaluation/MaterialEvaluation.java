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
import com.fluxchess.jcpi.models.IntColor;

public final class MaterialEvaluation {

//  private static final int EVAL_MATERIAL_QUEEN_BONUS = 2 * IntChessman.VALUE_ROOK - IntChessman.VALUE_QUEEN;

  private MaterialEvaluation() {
  }

  public static int evaluateMaterial(int myColor, Board board) {
    assert myColor != IntColor.NOCOLOR;

    int total = board.materialValueAll[myColor];

//      if (Board.materialCount[myColor] == 1) {
//          if (Board.knightList[myColor].size == 1) {
//              assert Board.queenList[myColor].size == 0
//              && Board.rookList[myColor].size == 0
//              && Board.bishopList[myColor].size == 0;
//
//              myMaterialValue -= IntChessman.VALUE_KNIGHT - 250;
//          }
//          else if (Board.knightList[myColor].size == 1 || Board.bishopList[myColor].size == 1) {
//              assert Board.queenList[myColor].size == 0
//              && Board.rookList[myColor].size == 0
//              && Board.knightList[myColor].size == 0;
//
//              myMaterialValue -= IntChessman.VALUE_BISHOP - 250;
//          }
//      }

    // Correct material value based on Larry Kaufman's paper
    // TODO: Check this one
//      myMaterialValue += (Board.knightList[myColor].size * (Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 16;
//      myMaterialValue -= (Board.rookList[myColor].size * (Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 8;

    // Queen + pawn vs. two rooks
    // TODO: Check this one
//      if ((Board.knightList[myColor].size + Board.bishopList[myColor].size >= 2)
//              && (Board.knightList[enemyColor].size + Board.bishopList[enemyColor].size >= 2)) {
//          myMaterialValue += EVAL_MATERIAL_QUEEN_BONUS;
//      }

    return total;
  }

}
