/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.evaluation;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntColor;

/**
 * MaterialEvaluation
 *
 * @author Phokham Nonava
 */
public final class MaterialEvaluation {

//	private static final int EVAL_MATERIAL_QUEEN_BONUS = 2 * IntChessman.VALUE_ROOK - IntChessman.VALUE_QUEEN;
	
	private MaterialEvaluation() {
	}
	
	public static int evaluateMaterial(int myColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;

		int total = board.materialValueAll[myColor];

//		if (Hex88Board.materialCount[myColor] == 1) {
//			if (Hex88Board.knightList[myColor].size == 1) {
//				assert Hex88Board.queenList[myColor].size == 0
//				&& Hex88Board.rookList[myColor].size == 0
//				&& Hex88Board.bishopList[myColor].size == 0;
//
//				myMaterialValue -= IntChessman.VALUE_KNIGHT - 250;
//			}
//			else if (Hex88Board.knightList[myColor].size == 1 || Hex88Board.bishopList[myColor].size == 1) {
//				assert Hex88Board.queenList[myColor].size == 0
//				&& Hex88Board.rookList[myColor].size == 0
//				&& Hex88Board.knightList[myColor].size == 0;
//
//				myMaterialValue -= IntChessman.VALUE_BISHOP - 250;
//			}
//		}
		
		// Correct material value based on Larry Kaufman's paper
		// TODO: Check this one
//		myMaterialValue += (Hex88Board.knightList[myColor].size * (Hex88Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 16;
//		myMaterialValue -= (Hex88Board.rookList[myColor].size * (Hex88Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 8;

		// Queen + pawn vs. two rooks
		// TODO: Check this one
//		if ((Hex88Board.knightList[myColor].size + Hex88Board.bishopList[myColor].size >= 2)
//				&& (Hex88Board.knightList[enemyColor].size + Hex88Board.bishopList[enemyColor].size >= 2)) {
//			myMaterialValue += EVAL_MATERIAL_QUEEN_BONUS;
//		}
		
		return total;
	}
	
}
