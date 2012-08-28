/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU Lesser General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.evaluation;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntChessman;
import com.fluxchess.board.IntColor;
import com.fluxchess.board.IntPosition;
import com.fluxchess.board.PositionList;

/**
 * PawnStructureEvaluation
 *
 * @author Phokham Nonava
 */
public final class PawnStructureEvaluation {

	private static final int EVAL_PAWN_DOUBLED_OPENING = 10;
	private static final int EVAL_PAWN_DOUBLED_ENDGAME = 20;
	private static final int EVAL_PAWN_ISOLATED_OPENING = 10;
	private static final int EVAL_PAWN_ISOLATED_ENDGAME = 20;
	private static final int EVAL_PAWN_BACKWARD_OPENING = 15;
	private static final int EVAL_PAWN_BACKWARD_ENDGAME = 10;

	private PawnStructureEvaluation() {
	}
	
	public static int evaluatePawnStructure(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int opening = 0;
		int endgame = 0;
		byte[] myAttackTable = AttackTableEvaluation.getInstance().attackTable[myColor];
		byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];
		byte[] myPawnTable = PawnTableEvaluation.getInstance().pawnTable[myColor];
		PositionList myPawnList = board.pawnList[myColor];

		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			int pawnFile = IntPosition.getFile(pawnPosition);
			int pawnRank = IntPosition.getRank(pawnPosition);
			int tableFile = pawnFile + 1;

			// Doubled pawns
			if (myPawnTable[tableFile] != pawnRank) {
				opening -= EVAL_PAWN_DOUBLED_OPENING;
				endgame -= EVAL_PAWN_DOUBLED_ENDGAME;
			}
			
			// Isolated pawn
			if (myPawnTable[tableFile - 1] == 0 && myPawnTable[tableFile + 1] == 0) {
				opening -= EVAL_PAWN_ISOLATED_OPENING;
				endgame -= EVAL_PAWN_ISOLATED_ENDGAME;
			}
			
			// Backward pawn
			else if ((myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_PAWN) == 0) {
				// We are not protected, check whether we have a backward pawn here

				boolean backward = false;
				int sign = 1;
				if (myColor == IntColor.WHITE) {
					if ((myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] > pawnRank)
							&& (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] > pawnRank)) {
						// We are behind the left and right pawn
						backward = true;
					}
				} else {
					assert myColor == IntColor.BLACK;
					
					if ((myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] < pawnRank)
							&& (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] < pawnRank)) {
						// We are behind the left and right pawn
						backward = true;
						sign = -1;
					}
				}
				
				if (backward) {
					// Really backward?
					
					if (myPawnTable[tableFile + 1] == pawnRank + sign * 1
							|| myPawnTable[tableFile - 1] == pawnRank + sign * 1) {
						// We are protecting a buddy on the left or right side
						// Check whether we can advance
						assert ((pawnPosition + sign * 16) & 0x88) == 0;
						int chessman = board.board[pawnPosition + sign * 16];
						if ((chessman == IntChessman.NOPIECE || IntChessman.getChessman(chessman) != IntChessman.PAWN)
								&& (enemyAttackTable[pawnPosition] & AttackTableEvaluation.BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 16] & AttackTableEvaluation.BIT_PAWN) == 0) {
							backward = false;
						}
					} else if (pawnRank == 1
							&& (myPawnTable[tableFile + 1] == pawnRank + sign * 2
									|| myPawnTable[tableFile - 1] == pawnRank + sign * 2)) {
						// We can do a pawn double advance
						assert ((pawnPosition + sign * 32) & 0x88) == 0;
						int chessman1 = board.board[pawnPosition + sign * 16];
						int chessman2 = board.board[pawnPosition + sign * 32];
						if ((chessman1 == IntChessman.NOPIECE || IntChessman.getChessman(chessman1) != IntChessman.PAWN)
								&& (chessman2 == IntChessman.NOPIECE || IntChessman.getChessman(chessman2) != IntChessman.PAWN)
								&& (enemyAttackTable[pawnPosition] & AttackTableEvaluation.BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 16] & AttackTableEvaluation.BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 32] & AttackTableEvaluation.BIT_PAWN) == 0) {
							backward = false;
						}
					}

					if (backward) {
						opening -= EVAL_PAWN_BACKWARD_OPENING;
						endgame -= EVAL_PAWN_BACKWARD_ENDGAME;
					}
				}
			}
		}

		return board.getGamePhaseEvaluation(myColor, opening, endgame);
	}

}
