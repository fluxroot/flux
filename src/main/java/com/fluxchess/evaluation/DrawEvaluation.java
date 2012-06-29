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

/**
 * DrawEvaluation
 *
 * @author Phokham Nonava
 */
public final class DrawEvaluation {

	// Draw values
	public static final int DRAW_FACTOR = 16;

	private DrawEvaluation() {
	}

	public static int evaluateDraw(Hex88Board board) {
		assert board != null;

		for (int myColor : IntColor.values) {
			int enemyColor = IntColor.switchColor(myColor);
			
			assert Hex88Board.kingList[myColor].size != 0;
			assert Hex88Board.kingList[enemyColor].size != 0;
			
			if (Hex88Board.queenList[myColor].size == 0) {
				if (Hex88Board.rookList[myColor].size == 0) {
					if (Hex88Board.bishopList[myColor].size == 0) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KK*
								assert Hex88Board.materialCountAll[myColor] == 0;

								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KK
													assert Hex88Board.materialCountAll[enemyColor] == 0;

													return 0;
												} else if (Hex88Board.pawnList[enemyColor].size == 1) {
													// KKP

													return evaluateDrawKPK(enemyColor, myColor, board);
												}
											} else if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KKN
													
													return 0;
												}
											}
										} else if (Hex88Board.bishopList[enemyColor].size == 1) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KKB
													
													return 0;
												}
											}
										}
									}
								}
							} else if (Hex88Board.pawnList[myColor].size == 1) {
								// KPK*
								
								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KPKN

													return evaluateDrawKPKN(myColor, enemyColor, board);
												}
											}
										} else if (Hex88Board.bishopList[enemyColor].size == 1) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KPKB

													return evaluateDrawKPKB(myColor, enemyColor, board);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} // for

		return DRAW_FACTOR;
	}

	private static int evaluateDrawKPK(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		byte[] myAttackTable = AttackTableEvaluation.attackTable[myColor];

		assert Hex88Board.pawnList[myColor].size == 1;
		int pawnPosition = Hex88Board.pawnList[myColor].position[0];
		int pawnFile = IntPosition.getFile(pawnPosition);
		int pawnRank = IntPosition.getRank(pawnPosition);
		assert Hex88Board.kingList[enemyColor].size == 1;
		int enemyKingPosition = Hex88Board.kingList[enemyColor].position[0];
		int enemyKingFile = IntPosition.getFile(enemyKingPosition);
		int enemyKingRank = IntPosition.getRank(enemyKingPosition);
		assert Hex88Board.kingList[myColor].size == 1;
		int myKingPosition = Hex88Board.kingList[myColor].position[0];
		int myKingFile = IntPosition.getFile(myKingPosition);
		int myKingRank = IntPosition.getRank(myKingPosition);

		int myKingPromotionDistance;
		int enemyKingPromotionDistance;
		if (myColor == IntColor.WHITE) {
			myKingPromotionDistance = Math.max(Math.abs(pawnFile - myKingFile), IntPosition.rank8 - myKingRank);
			enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), IntPosition.rank8 - enemyKingRank);
		}
		else {
			assert myColor == IntColor.BLACK;

			myKingPromotionDistance = Math.max(Math.abs(pawnFile - myKingFile), myKingRank);
			enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), enemyKingRank);
		}
		// Unstoppable passer
		boolean unstoppablePasser = false;
		if (myKingFile != pawnFile) {
			int delta;
			int promotionDistance;
			int difference;

			if (myColor == IntColor.WHITE) {
				delta = 16;

				promotionDistance = IntPosition.rank8 - pawnRank;
				if (pawnRank == IntPosition.rank2) {
					// We can do a pawn double move
					promotionDistance--;
				}
			} else {
				assert myColor == IntColor.BLACK;

				delta = -16;

				promotionDistance = pawnRank;
				if (pawnRank == IntPosition.rank7) {
					// We can do a pawn double move
					promotionDistance--;
				}
			}

			difference = enemyKingPromotionDistance - promotionDistance;

			if (board.activeColor == enemyColor) {
				difference--;
			}

			if (difference >= 1) {
				unstoppablePasser = true;
			}

			// King protected passer
			else if (IntPosition.getRelativeRank(myKingPosition, myColor) == IntPosition.rank7
					&& ((promotionDistance <= 2 && (myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_KING) != 0)
							|| (promotionDistance <= 3 && (myAttackTable[pawnPosition + delta] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
							&& (myKingFile != pawnFile || (pawnFile != IntPosition.fileA && pawnFile != IntPosition.fileH))) {
				unstoppablePasser = true;
			}
		}

		if (!unstoppablePasser) {
			if (pawnFile == IntPosition.fileA || pawnFile == IntPosition.fileH) {
				int difference = enemyKingPromotionDistance - myKingPromotionDistance;
				if (board.activeColor == enemyColor) {
					difference--;
				}
				
				if (difference < 1) {
					// The enemy king can reach the corner.
					return 0;
				}
			}
			else {
				boolean enemyKingInFrontPawn = false;
				if (myColor == IntColor.WHITE) {
					enemyKingInFrontPawn = pawnRank < enemyKingRank;
				}
				else {
					assert myColor == IntColor.BLACK;

					enemyKingInFrontPawn = pawnRank > enemyKingRank;
				}
				if (enemyKingInFrontPawn
						&& Math.abs(myKingRank - enemyKingRank) >= 2
						&& !(Math.abs(myKingRank - enemyKingRank) == 2 && myKingFile == enemyKingFile && board.activeColor == enemyColor)) {
					return 0;
				}
			}
		}

		return DRAW_FACTOR;
	}

	private static int evaluateDrawKPKN(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		byte[] enemyAttackTable = AttackTableEvaluation.attackTable[enemyColor];

		// Check the promotion path
		int delta = 16;
		if (myColor == IntColor.BLACK) {
			delta = -16;
		} else {
			assert myColor == IntColor.WHITE;
		}
		int end = Hex88Board.pawnList[myColor].position[0] + delta;
		while ((end & 0x88) == 0) {
			int chessman = Hex88Board.board[end];
			if ((chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & AttackTableEvaluation.BIT_MINOR) != 0) {
				return 1;
			} else {
				end += delta;
			}
		}

		return DRAW_FACTOR;
	}

	private static int evaluateDrawKPKB(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert enemyColor != IntColor.NOCOLOR;
		assert board != null;

		byte[] enemyAttackTable = AttackTableEvaluation.attackTable[enemyColor];

		// Check the promotion path
		int delta = 16;
		if (myColor == IntColor.BLACK) {
			delta = -16;
		} else {
			assert myColor == IntColor.WHITE;
		}
		int end = Hex88Board.pawnList[myColor].position[0] + delta;
		while ((end & 0x88) == 0) {
			int chessman = Hex88Board.board[end];
			if ((chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & AttackTableEvaluation.BIT_MINOR) != 0) {
				return 1;
			} else {
				end += delta;
			}
		}

		return DRAW_FACTOR;
	}

}
