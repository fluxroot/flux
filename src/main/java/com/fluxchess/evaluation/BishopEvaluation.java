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
import com.fluxchess.board.IntChessman;
import com.fluxchess.board.IntColor;
import com.fluxchess.board.PositionList;
import com.fluxchess.move.MoveGenerator;

/**
 * BishopEvaluation
 *
 * @author Phokham Nonava
 */
public final class BishopEvaluation {

	public static int EVAL_BISHOP_MOBILITY_BASE = -6;
	public static int EVAL_BISHOP_MOBILITYFACTOR = 5;
	public static int EVAL_BISHOP_SAFETY = 10;
	public static int EVAL_BISHOP_PAIR = 50;

	private BishopEvaluation() {
	}

	public static int evaluateBishop(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int total = 0;
		byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];
		PositionList myBishopList = board.bishopList[myColor];
		
		// Evaluate each bishop
		for (int i = 0; i < myBishopList.size; i++) {
			int bishopPosition = myBishopList.position[i];

			int allMobility = EVAL_BISHOP_MOBILITY_BASE;

			// Evaluate mobility
			for (int delta : MoveGenerator.moveDeltaBishop) {
				int targetPosition = bishopPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					int target = board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						allMobility++;
						targetPosition += delta;
					} else {
						if (IntChessman.getColor(target) == enemyColor) {
							allMobility++;
						}
						break;
					}
				}
			}

			// Evaluate mobility
			total += EVAL_BISHOP_MOBILITYFACTOR * allMobility;

			// Evaluate safety
			if ((enemyAttackTable[bishopPosition] & AttackTableEvaluation.BIT_PAWN) == 0) {
				total += EVAL_BISHOP_SAFETY;
			}
		}

		// Evaluate bishop pair
		if (myBishopList.size >= 2) {
			total += EVAL_BISHOP_PAIR;
		}
		
		return total;
	}
	
}
