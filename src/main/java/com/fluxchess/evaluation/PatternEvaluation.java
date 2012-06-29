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
 * PatternEvaluation
 *
 * @author Phokham Nonava
 */
public final class PatternEvaluation {

	private PatternEvaluation() {
	}
	
	public static int evaluatePatterns(int myColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int total = 0;

		if (myColor == IntColor.WHITE) {
			// Trapped white bishop
			if (Hex88Board.board[IntPosition.a7] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.b6] == IntChessman.BLACK_PAWN) {
				total -= 100;
				if (Hex88Board.board[IntPosition.c7] == IntChessman.BLACK_PAWN) {
					total -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.b8] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.c7] == IntChessman.BLACK_PAWN) {
				total -= 100;
			}
			if (Hex88Board.board[IntPosition.h7] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.g6] == IntChessman.BLACK_PAWN) {
				total -= 100;
				if (Hex88Board.board[IntPosition.f7] == IntChessman.BLACK_PAWN) {
					total -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.g8] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.f7] == IntChessman.BLACK_PAWN) {
				total -= 100;
			}
			if (Hex88Board.board[IntPosition.a6] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.b5] == IntChessman.BLACK_PAWN) {
				total -= 50;
			}
			if (Hex88Board.board[IntPosition.h6] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.g5] == IntChessman.BLACK_PAWN) {
				total -= 50;
			}

			// Blocked center pawn
			if (Hex88Board.board[IntPosition.d2] == IntChessman.WHITE_PAWN
					&& Hex88Board.board[IntPosition.d3] != IntChessman.NOPIECE) {
				total -= 20;
				if (Hex88Board.board[IntPosition.c1] == IntChessman.WHITE_BISHOP) {
					total -= 30;
				}
			}
			if (Hex88Board.board[IntPosition.e2] == IntChessman.WHITE_PAWN
					&& Hex88Board.board[IntPosition.e3] != IntChessman.NOPIECE) {
				total -= 20;
				if (Hex88Board.board[IntPosition.f1] == IntChessman.WHITE_BISHOP) {
					total -= 30;
				}
			}

			// Blocked rook
			if ((Hex88Board.board[IntPosition.c1] == IntChessman.WHITE_KING
					|| Hex88Board.board[IntPosition.b1] == IntChessman.WHITE_KING)
					&& (Hex88Board.board[IntPosition.a1] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.a2] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.b1] == IntChessman.WHITE_ROOK)) {
				total -= 50;
			}
			if ((Hex88Board.board[IntPosition.f1] == IntChessman.WHITE_KING
					|| Hex88Board.board[IntPosition.g1] == IntChessman.WHITE_KING)
					&& (Hex88Board.board[IntPosition.h1] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.h2] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.g1] == IntChessman.WHITE_ROOK)) {
				total -= 50;
			}
		} else {
			assert myColor == IntColor.BLACK;

			// Trapped black bishop
			if (Hex88Board.board[IntPosition.a2] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.b3] == IntChessman.WHITE_PAWN) {
				total -= 100;
				if (Hex88Board.board[IntPosition.c2] == IntChessman.WHITE_PAWN) {
					total -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.b1] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.c2] == IntChessman.WHITE_PAWN) {
				total -= 100;
			}
			if (Hex88Board.board[IntPosition.h2] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.g3] == IntChessman.WHITE_PAWN) {
				total -= 100;
				if (Hex88Board.board[IntPosition.f2] == IntChessman.WHITE_PAWN) {
					total -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.g1] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.f2] == IntChessman.WHITE_PAWN) {
				total -= 100;
			}
			if (Hex88Board.board[IntPosition.a3] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.b4] == IntChessman.WHITE_PAWN) {
				total -= 50;
			}
			if (Hex88Board.board[IntPosition.h3] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.g4] == IntChessman.WHITE_PAWN) {
				total -= 50;
			}

			// Blocked center pawn
			if (Hex88Board.board[IntPosition.d7] == IntChessman.BLACK_PAWN
					&& Hex88Board.board[IntPosition.d6] != IntChessman.NOPIECE) {
				total -= 20;
				if (Hex88Board.board[IntPosition.c8] == IntChessman.BLACK_BISHOP) {
					total -= 30;
				}
			}
			if (Hex88Board.board[IntPosition.e7] == IntChessman.BLACK_PAWN
					&& Hex88Board.board[IntPosition.e6] != IntChessman.NOPIECE) {
				total -= 20;
				if (Hex88Board.board[IntPosition.f8] == IntChessman.BLACK_BISHOP) {
					total -= 30;
				}
			}

			// Blocked rook
			if ((Hex88Board.board[IntPosition.c8] == IntChessman.BLACK_KING
					|| Hex88Board.board[IntPosition.b8] == IntChessman.BLACK_KING)
					&& (Hex88Board.board[IntPosition.a8] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.a7] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.b8] == IntChessman.BLACK_ROOK)) {
				total -= 50;
			}
			if ((Hex88Board.board[IntPosition.f8] == IntChessman.BLACK_KING
					|| Hex88Board.board[IntPosition.g8] == IntChessman.BLACK_KING)
					&& (Hex88Board.board[IntPosition.h8] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.h7] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.g8] == IntChessman.BLACK_ROOK)) {
				total -= 50;
			}
		}

		return total;
	}

}
