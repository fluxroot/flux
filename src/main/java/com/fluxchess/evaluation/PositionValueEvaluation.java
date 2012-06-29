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

/**
 * PositionValueEvaluation
 *
 * @author Phokham Nonava
 */
public final class PositionValueEvaluation {

	private static final int[][] positionValueOpening = {
		{ // Empty
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Pawn
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,   15,   15,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,   25,   25,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,   15,   15,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    0,    5,    5,    0,   -5,  -15,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Knight
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -50,  -40,  -30,  -25,  -25,  -30,  -40,  -50,     0,0,0,0,0,0,0,0,
			 -35,  -25,  -15,  -10,  -10,  -15,  -25,  -35,     0,0,0,0,0,0,0,0,
			 -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
			 -10,    0,   10,   15,   15,   10,    0,  -10,     0,0,0,0,0,0,0,0,
			  -5,    5,   15,   20,   20,   15,    5,   -5,     0,0,0,0,0,0,0,0,
			  -5,    5,   15,   20,   20,   15,    5,   -5,     0,0,0,0,0,0,0,0,
			 -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
			-135,  -25,  -15,  -10,  -10,  -15,  -25, -135,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // King
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			  40,   50,   30,   10,   10,   30,   50,   40,     0,0,0,0,0,0,0,0,
			  30,   40,   20,    0,    0,   20,   40,   30,     0,0,0,0,0,0,0,0,
			  10,   20,    0,  -20,  -20,    0,   20,   10,     0,0,0,0,0,0,0,0,
			   0,   10,  -10,  -30,  -30,  -10,   10,    0,     0,0,0,0,0,0,0,0,
			 -10,    0,  -20,  -40,  -40,  -20,    0,  -10,     0,0,0,0,0,0,0,0,
			 -20,  -10,  -30,  -50,  -50,  -30,  -10,  -20,     0,0,0,0,0,0,0,0,
			 -30,  -20,  -40,  -60,  -60,  -40,  -20,  -30,     0,0,0,0,0,0,0,0,
			 -40,  -30,  -50,  -70,  -70,  -50,  -30,  -40,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Empty
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Bishop
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -18,  -18,   16,  -14,  -14,  -16,  -18,  -18,     0,0,0,0,0,0,0,0,
			  -8,    0,   -2,    0,    0,   -2,    0,   -8,     0,0,0,0,0,0,0,0,
			  -6,   -2,    4,    2,    2,    4,   -2,   -6,     0,0,0,0,0,0,0,0,
			  -4,    0,    2,    8,    8,    2,    0,   -4,     0,0,0,0,0,0,0,0,
			  -4,    0,    2,    8,    8,    2,    0,   -4,     0,0,0,0,0,0,0,0,
			  -6,   -2,    4,    2,    2,    4,   -2,   -6,     0,0,0,0,0,0,0,0,
			  -8,    0,   -2,    0,    0,   -2,    0,   -8,     0,0,0,0,0,0,0,0,
			  -8,   -8,   -6,   -4,   -4,   -6,   -8,   -8,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Rook
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0,
			  -6,   -3,    0,    3,    3,    0,   -3,   -6,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Queen
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			  -5,   -5,   -5,   -5,   -5,   -5,   -5,   -5,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		}
	};

	private static final int[][] positionValueEndgame = {
		{ // Empty
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Pawn
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Knight
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -40,  -30,  -20,  -15,  -15,  -20,  -30,  -40,     0,0,0,0,0,0,0,0,
			 -30,  -20,  -10,   -5,   -5,  -10,  -20,  -30,     0,0,0,0,0,0,0,0,
			 -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
			 -15,   -5,    5,   10,   10,    5,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -15,   -5,    5,   10,   10,    5,   -5,  -15,     0,0,0,0,0,0,0,0,
			 -20,  -10,    0,    5,    5,    0,  -10,  -20,     0,0,0,0,0,0,0,0,
			 -30,  -20,  -10,   -5,   -5,  -10,  -20,  -30,     0,0,0,0,0,0,0,0,
			 -40,  -30,  -20,  -15,  -15,  -20,  -30,  -40,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // King
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -72,  -48,  -36,  -24,  -24,  -36,  -48,  -72,     0,0,0,0,0,0,0,0,
			 -48,  -24,  -12,    0,    0,  -12,  -24,  -48,     0,0,0,0,0,0,0,0,
			 -36,  -12,    0,   12,   12,    0,  -12,  -36,     0,0,0,0,0,0,0,0,
			 -24,    0,   12,   24,   24,   12,    0,  -24,     0,0,0,0,0,0,0,0,
			 -24,    0,   12,   24,   24,   12,    0,  -24,     0,0,0,0,0,0,0,0,
			 -36,  -12,    0,   12,   12,    0,  -12,  -36,     0,0,0,0,0,0,0,0,
			 -48,  -24,  -12,    0,    0,  -12,  -24,  -48,     0,0,0,0,0,0,0,0,
			 -72,  -48,  -36,  -24,  -24,  -36,  -48,  -72,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Empty
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Bishop
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -18,  -12,   -9,   -6,   -6,   -9,  -12,  -18,     0,0,0,0,0,0,0,0,
			 -12,   -6,   -3,    0,    0,   -3,   -6,  -12,     0,0,0,0,0,0,0,0,
			  -9,   -3,    0,    3,    3,    0,   -3,   -9,     0,0,0,0,0,0,0,0,
			   6,    0,    3,    6,    6,    3,    0,    6,     0,0,0,0,0,0,0,0,
			   6,    0,    3,    6,    6,    3,    0,    6,     0,0,0,0,0,0,0,0,
			  -9,   -3,    0,    3,    3,    0,   -3,   -9,     0,0,0,0,0,0,0,0,
			 -12,   -6,   -3,    0,    0,   -3,   -6,  -12,     0,0,0,0,0,0,0,0,
			 -18,  -12,   -9,   -6,   -6,   -9,  -12,  -18,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Rook
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0,
			   0,    0,    0,    0,    0,    0,    0,    0,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		},
		{ // Queen
		//    a1,   b1,   c1,   d1,   e1,   f1,   g1,   h1
			 -24,  -16,  -12,   -8,   -8,  -12,  -16,  -24,     0,0,0,0,0,0,0,0,
			 -16,   -8,   -4,    0,    0,   -4,   -8,  -16,     0,0,0,0,0,0,0,0,
			 -12,   -4,    0,    4,    4,    0,   -4,  -12,     0,0,0,0,0,0,0,0,
			  -8,    0,    4,    8,    8,    4,    0,   -8,     0,0,0,0,0,0,0,0,
			  -8,    0,    4,    8,    8,    4,    0,   -8,     0,0,0,0,0,0,0,0,
			 -12,   -4,    0,    4,    4,    0,   -4,  -12,     0,0,0,0,0,0,0,0,
			 -16,   -8,   -4,    0,    0,   -4,   -8,  -16,     0,0,0,0,0,0,0,0,
			 -24,  -16,  -12,   -8,   -8,  -12,  -16,  -24,     0,0,0,0,0,0,0,0
		//    a8,   b8,   c8,   d8,   e8,   f8,   g8,   h8
		}
	};

	/**
	 * Creates a new PositionValueEvaluation.
	 */
	public PositionValueEvaluation() {
	}

	public static int evaluatePositionValue(int myColor) {
		assert myColor != IntColor.NOCOLOR;
		
		// Initialize
		int opening = 0;
		int endgame = 0;

		// Pawns
		PositionList chessmanList = Hex88Board.pawnList[myColor];
		int[] chessmanValueOpening = positionValueOpening[IntChessman.PAWN];
		int[] chessmanValueEndgame = positionValueEndgame[IntChessman.PAWN];
		for (int i = 0; i < chessmanList.size; i++) {
			int position = chessmanList.position[i];
			if (myColor == IntColor.BLACK) {
				position = 127 - 8 - position;
			} else {
				assert myColor == IntColor.WHITE;
			}
			opening += chessmanValueOpening[position];
			endgame += chessmanValueEndgame[position];
		}

		// Knights
		chessmanList = Hex88Board.knightList[myColor];
		chessmanValueOpening = positionValueOpening[IntChessman.KNIGHT];
		chessmanValueEndgame = positionValueEndgame[IntChessman.KNIGHT];
		for (int i = 0; i < chessmanList.size; i++) {
			int position = chessmanList.position[i];
			if (myColor == IntColor.BLACK) {
				position = 127 - 8 - position;
			} else {
				assert myColor == IntColor.WHITE;
			}
			opening += chessmanValueOpening[position];
			endgame += chessmanValueEndgame[position];
		}
		
		// Bishops
		chessmanList = Hex88Board.bishopList[myColor];
		chessmanValueOpening = positionValueOpening[IntChessman.BISHOP];
		chessmanValueEndgame = positionValueEndgame[IntChessman.BISHOP];
		for (int i = 0; i < chessmanList.size; i++) {
			int position = chessmanList.position[i];
			if (myColor == IntColor.BLACK) {
				position = 127 - 8 - position;
			} else {
				assert myColor == IntColor.WHITE;
			}
			opening += chessmanValueOpening[position];
			endgame += chessmanValueEndgame[position];
		}

		// Rooks
		chessmanList = Hex88Board.rookList[myColor];
		chessmanValueOpening = positionValueOpening[IntChessman.ROOK];
		chessmanValueEndgame = positionValueEndgame[IntChessman.ROOK];
		for (int i = 0; i < chessmanList.size; i++) {
			int position = chessmanList.position[i];
			if (myColor == IntColor.BLACK) {
				position = 127 - 8 - position;
			} else {
				assert myColor == IntColor.WHITE;
			}
			opening += chessmanValueOpening[position];
			endgame += chessmanValueEndgame[position];
		}

		// Queens
		chessmanList = Hex88Board.queenList[myColor];
		chessmanValueOpening = positionValueOpening[IntChessman.QUEEN];
		chessmanValueEndgame = positionValueEndgame[IntChessman.QUEEN];
		for (int i = 0; i < chessmanList.size; i++) {
			int position = chessmanList.position[i];
			if (myColor == IntColor.BLACK) {
				position = 127 - 8 - position;
			} else {
				assert myColor == IntColor.WHITE;
			}
			opening += chessmanValueOpening[position];
			endgame += chessmanValueEndgame[position];
		}

		// King
		assert Hex88Board.kingList[myColor].size == 1;
		int position = Hex88Board.kingList[myColor].position[0];
		if (myColor == IntColor.BLACK) {
			position = 127 - 8 - position;
		} else {
			assert myColor == IntColor.WHITE;
		}
		opening += positionValueOpening[IntChessman.KING][position];
		endgame += positionValueEndgame[IntChessman.KING][position];

		// Return linear mix
		return Evaluation.createLinearMix(myColor, opening, endgame);
	}

}
