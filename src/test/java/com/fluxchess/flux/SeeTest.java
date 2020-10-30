/*
 * Copyright 2007-2020 Phokham Nonava
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
package com.fluxchess.flux;

import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class SeeTest {

	@Test
	void testSeeMove() {
		try {
			// Pawn capture
			Position board = new Position(new GenericBoard("8/8/8/4p1k1/2KP4/8/8/8 w - -"));
			int move = Move.createMove(MoveType.NORMAL, Square.d4, Square.e5, Position.board[Square.d4], Position.board[Square.e5], Piece.NOPIECE);
			new See(board);
			int value = See.seeMove(move, Color.WHITE);
			assertThat(Piece.VALUE_PAWN).isEqualTo(value);

			// En passant capture
			board = new Position(new GenericBoard("8/8/K7/6k1/2Pp4/8/1P6/8 b - c3"));
			move = Move.createMove(MoveType.ENPASSANT, Square.d4, Square.c3, Position.board[Square.d4], Position.board[Square.c4], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.BLACK);
			assertThat(0).isEqualTo(value);

			// En passant capture with hidden attacker
			board = new Position(new GenericBoard("8/6k1/4r3/8/4Pp2/8/1K1P4/8 b - e3"));
			move = Move.createMove(MoveType.ENPASSANT, Square.f4, Square.e3, Position.board[Square.f4], Position.board[Square.e4], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.BLACK);
			assertThat(Piece.VALUE_PAWN).isEqualTo(value);

			// Pawn promotion capture
			board = new Position(new GenericBoard("8/8/K7/6k1/8/5B2/4p3/3R4 b - -"));
			move = Move.createMove(MoveType.PAWNPROMOTION, Square.e2, Square.d1, Position.board[Square.e2], Position.board[Square.d1], PieceType.ROOK);
			new See(board);
			value = See.seeMove(move, Color.BLACK);
			assertThat(
					Piece.VALUE_ROOK
							+ (Piece.VALUE_ROOK - Piece.VALUE_PAWN)
							- Piece.VALUE_ROOK).isEqualTo(value);

			// King capture abort
			board = new Position(new GenericBoard("8/6k1/8/4q3/8/5p2/1R1KP3/8 b - -"));
			move = Move.createMove(MoveType.NORMAL, Square.f3, Square.e2, Position.board[Square.f3], Position.board[Square.e2], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.BLACK);
			assertThat(
					Piece.VALUE_PAWN
							- Piece.VALUE_PAWN
							+ Piece.VALUE_KING).isEqualTo(value);

			// Complex capture
			board = new Position(new GenericBoard("R1B3q1/N1KP4/3n4/8/6b1/2R5/6k1/8 b - -"));
			move = Move.createMove(MoveType.NORMAL, Square.d6, Square.c8, Position.board[Square.d6], Position.board[Square.c8], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.BLACK);
			assertThat(
					Piece.VALUE_BISHOP
							- Piece.VALUE_KNIGHT
							- (Piece.VALUE_QUEEN - Piece.VALUE_PAWN)
							+ Piece.VALUE_QUEEN
							- Piece.VALUE_BISHOP
							+ Piece.VALUE_KNIGHT
							- Piece.VALUE_QUEEN).isEqualTo(value);

			// Same piece capture test
			board = new Position(new GenericBoard("r4rk1/5ppp/2Np4/p2P2b1/Pp3Rq1/1R1pP2P/1PP3P1/7K w - -"));
			move = Move.createMove(MoveType.NORMAL, Square.c6, Square.b4, Position.board[Square.c6], Position.board[Square.b4], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.WHITE);
			assertThat(
					Piece.VALUE_PAWN
							- Piece.VALUE_KNIGHT
							+ Piece.VALUE_PAWN
							- Piece.VALUE_ROOK
							+ Piece.VALUE_QUEEN).isEqualTo(value);

			// Non-capture move
			board = new Position(new GenericBoard("8/6k1/4r3/8/5p2/8/1K1PP3/8 w - -"));
			move = Move.createMove(MoveType.NORMAL, Square.e2, Square.e3, Position.board[Square.e2], Position.board[Square.e3], Piece.NOPIECE);
			new See(board);
			value = See.seeMove(move, Color.WHITE);
			assertThat(-Piece.VALUE_PAWN).isEqualTo(value);
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
	}
}
