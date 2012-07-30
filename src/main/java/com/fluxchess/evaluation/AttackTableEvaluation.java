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

import java.util.Arrays;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntChessman;
import com.fluxchess.board.IntColor;
import com.fluxchess.board.PositionList;
import com.fluxchess.move.MoveGenerator;

/**
 * AttackTableEvaluation
 *
 * @author Phokham Nonava
 */
public final class AttackTableEvaluation {

	public static final byte BIT_PAWN = 1 << 3;
	public static final byte BIT_MINOR = 1 << 4;
	public static final byte BIT_ROOK = 1 << 5;
	public static final byte BIT_QUEEN = 1 << 6;
	public static final byte BIT_KING = -128;

	private static final AttackTableEvaluation instance = new AttackTableEvaluation();
	
	// Our attack table
	public final byte[][] attackTable = new byte[IntColor.ARRAY_DIMENSION][Hex88Board.BOARDSIZE];

	private AttackTableEvaluation() {
	}
	
	public static AttackTableEvaluation getInstance() {
		return instance;
	}
	
	public void createAttackTable(int myColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Zero our table
		Arrays.fill(this.attackTable[myColor], (byte) 0);

		// Fill attack table
		pawnInformationToAttackTable(myColor, board, this.attackTable[myColor]);
		knightInformationToAttackTable(myColor, board, this.attackTable[myColor]);
		bishopInformationToAttackTable(myColor, board, this.attackTable[myColor]);
		rookInformationToAttackTable(myColor, board, this.attackTable[myColor]);
		queenInformationToAttackTable(myColor, board, this.attackTable[myColor]);
		kingInformationToAttackTable(myColor, board, this.attackTable[myColor]);
	}

	private void pawnInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myPawnList = Hex88Board.pawnList[myColor];

		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			
			// Fill attack table
			for (int j = 1; j < MoveGenerator.moveDeltaPawn.length; j++) {
				int delta = MoveGenerator.moveDeltaPawn[j];

				int targetPosition = pawnPosition;
				if (myColor == IntColor.WHITE) {
					targetPosition += delta;
				} else {
					assert myColor == IntColor.BLACK;
					
					targetPosition -= delta;
				}
				if ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_PAWN;
				}
			}
		}
	}

	private void knightInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myKnightList = Hex88Board.knightList[myColor];

		// Evaluate each knight
		for (int i = 0; i < myKnightList.size; i++) {
			int knightPosition = myKnightList.position[i];

			// Fill attack table
			for (int delta : MoveGenerator.moveDeltaKnight) {
				int targetPosition = knightPosition + delta;
				if ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_MINOR;
				}
			}
		}
	}

	private void bishopInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myBishopList = Hex88Board.bishopList[myColor];
		
		// Evaluate each bishop
		for (int i = 0; i < myBishopList.size; i++) {
			int bishopPosition = myBishopList.position[i];

			// Fill attack table
			for (int delta : MoveGenerator.moveDeltaBishop) {
				int targetPosition = bishopPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_MINOR;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						targetPosition += delta;
					} else {
						break;
					}
				}
			}
		}
	}

	private void rookInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myRookList = Hex88Board.rookList[myColor];
		
		// Evaluate each rook
		for (int i = 0; i < myRookList.size; i++) {
			int rookPosition = myRookList.position[i];

			// Fill attack table
			for (int delta : MoveGenerator.moveDeltaRook) {
				int targetPosition = rookPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_ROOK;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						targetPosition += delta;
					} else {
						break;
					}
				}
			}
		}
	}

	private void queenInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myQueenList = Hex88Board.queenList[myColor];

		// Evaluate the queen
		for (int i = 0; i < myQueenList.size; i++) {
			int queenPosition = myQueenList.position[i];

			// Fill attack table
			for (int delta : MoveGenerator.moveDeltaQueen) {
				int targetPosition = queenPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_QUEEN;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						targetPosition += delta;
					} else {
						break;
					}
				}
			}
		}
	}

	private void kingInformationToAttackTable(int myColor, Hex88Board board, byte[] myAttackTable) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;
		assert myAttackTable != null;

		// Initialize
		PositionList myKingList = Hex88Board.kingList[myColor];
		
		// Evaluate the king
		assert myKingList.size == 1;
		int kingPosition = myKingList.position[0];

		// Fill attack table
		for (int delta : MoveGenerator.moveDeltaKing) {
			int targetPosition = kingPosition + delta;
			if ((targetPosition & 0x88) == 0) {
				myAttackTable[targetPosition]++;
				myAttackTable[targetPosition] |= BIT_KING;
			}
		}
	}

}
