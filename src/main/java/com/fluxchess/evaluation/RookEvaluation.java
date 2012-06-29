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
import com.fluxchess.board.IntPosition;
import com.fluxchess.board.PositionList;
import com.fluxchess.move.MoveGenerator;

/**
 * RookEvaluation
 *
 * @author Phokham Nonava
 */
public final class RookEvaluation {

	private static final int EVAL_ROOK_MOBILITY_BASE = -7;
	private static final int EVAL_ROOK_MOBILITYFACTOR_OPENING = 2;
	private static final int EVAL_ROOK_MOBILITYFACTOR_ENDGAME = 4;
	private static final int EVAL_ROOK_SAFETY = 20;
	private static final int EVAL_ROOK_OPENFILE = 20;
	private static final int EVAL_ROOK_NEARKINGFILE = 10;
	private static final int EVAL_ROOK_SEVENTHRANK_OPENING = 20;
	private static final int EVAL_ROOK_SEVENTHRANK_ENDGAME = 40;
	private static final int EVAL_ROOK_SEVENTHRANK_BONUS = 10;

	private RookEvaluation() {
	}
	
	public static int evaluateRook(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int opening = 0;
		int endgame = 0;
		byte[] enemyAttackTable = AttackTableEvaluation.attackTable[enemyColor];
		byte[] myPawnTable = PawnTableEvaluation.pawnTable[myColor];
		byte[] enemyPawnTable = PawnTableEvaluation.pawnTable[enemyColor];
		PositionList myRookList = Hex88Board.rookList[myColor];
		int totalRook7th = 0;
		
		// Evaluate each rook
		for (int i = 0; i < myRookList.size; i++) {
			int rookPosition = myRookList.position[i];
			int rookFile = IntPosition.getFile(rookPosition);
			int rookRank = IntPosition.getRank(rookPosition);
			int tableFile = rookFile + 1;

			int allMobility = EVAL_ROOK_MOBILITY_BASE;

			// Evaluate mobility
			for (int delta : MoveGenerator.moveDeltaRook) {
				int targetPosition = rookPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					int target = Hex88Board.board[targetPosition];
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
			opening += EVAL_ROOK_MOBILITYFACTOR_OPENING * allMobility;
			endgame += EVAL_ROOK_MOBILITYFACTOR_ENDGAME * allMobility;
			
			// Evaluate safety
			if ((enemyAttackTable[rookPosition] & AttackTableEvaluation.BIT_PAWN) == 0
					&& (enemyAttackTable[rookPosition] & AttackTableEvaluation.BIT_MINOR) == 0) {
				opening += EVAL_ROOK_SAFETY;
				endgame += EVAL_ROOK_SAFETY;
			}

			// Evaluate open file
			int totalOpenFile = 0;
			totalOpenFile -= EVAL_ROOK_OPENFILE / 2;
			if (myPawnTable[tableFile] == 0) {
				totalOpenFile += EVAL_ROOK_OPENFILE / 2;
				if (enemyPawnTable[tableFile] == 0) {
					totalOpenFile += EVAL_ROOK_OPENFILE / 2;
				}
				int kingPosition = Hex88Board.kingList[enemyColor].position[0];
				int kingFile = IntPosition.getFile(kingPosition);
				int delta = Math.abs(kingFile - rookFile);
				if (delta <= 1) {
					opening += EVAL_ROOK_NEARKINGFILE;
					if (delta == 0) {
						opening += EVAL_ROOK_NEARKINGFILE;
					}
				}
			}
			opening += totalOpenFile;
			
			// Evaluate 7th rank
			int seventhRank = 6;
			int eighthRank = 7;
			if (myColor == IntColor.BLACK) {
				seventhRank = 1;
				eighthRank = 0;
			} else {
				assert myColor == IntColor.WHITE;
			}
			if (rookRank == seventhRank) {
				int kingPosition = Hex88Board.kingList[enemyColor].position[0];
				int kingRank = IntPosition.getRank(kingPosition);
				boolean enemyPawnExists = false;
				for (int j = 1; j < enemyPawnTable.length - 1; j++) {
					if (enemyPawnTable[j] == seventhRank) {
						enemyPawnExists = true;
						break;
					}
				}
				if (enemyPawnExists || kingRank == eighthRank) {
					totalRook7th++;
					opening += EVAL_ROOK_SEVENTHRANK_OPENING;
					endgame += EVAL_ROOK_SEVENTHRANK_ENDGAME;
				}
			}
		}
		
		// Check whether we have both rooks on the 7th rank
		if (totalRook7th == 2) {
			opening += EVAL_ROOK_SEVENTHRANK_BONUS;
			endgame += EVAL_ROOK_SEVENTHRANK_BONUS;
		}

		return Evaluation.createLinearMix(myColor, opening, endgame);
	}
	
}
