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
import com.fluxchess.board.IntColor;
import com.fluxchess.board.IntPosition;
import com.fluxchess.board.PositionList;

/**
 * PawnTableEvaluation
 *
 * @author Phokham Nonava
 */
public final class PawnTableEvaluation {

	// Our pawn structure table. 8 + 2 -> 2 Sentinels for each side.
	public final byte[][] pawnTable = new byte[IntColor.ARRAY_DIMENSION][10];
	
	private static final PawnTableEvaluation instance = new PawnTableEvaluation();
	
	private PawnTableEvaluation() {
	}

	public static PawnTableEvaluation getInstance() {
		return instance;
	}
	
	public void createPawnTable(int myColor) {
		assert myColor != IntColor.NOCOLOR;

		// Zero our table
		Arrays.fill(this.pawnTable[myColor], (byte) 0);
		
		// Initialize
		byte[] myPawnTable = this.pawnTable[myColor];
		PositionList myPawnList = Hex88Board.pawnList[myColor];

		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			int pawnFile = IntPosition.getFile(pawnPosition);
			int pawnRank = IntPosition.getRank(pawnPosition);
			
			// Fill pawn table
			int tableFile = pawnFile + 1;
			if (myPawnTable[tableFile] == 0
					|| (myPawnTable[tableFile] > pawnRank && myColor == IntColor.WHITE)
					|| (myPawnTable[tableFile] < pawnRank && myColor == IntColor.BLACK)) {
				// Set the rank to the lowest pawn rank
				myPawnTable[tableFile] = (byte) pawnRank;
			}
		}
	}

}
