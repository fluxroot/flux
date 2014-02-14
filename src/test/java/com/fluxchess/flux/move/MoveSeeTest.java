/*
 * Copyright 2007-2014 the original author or authors.
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
package com.fluxchess.flux.move;

import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoveSeeTest {

	@Test
	public void testSeeMove() {
		try {
			// Pawn capture
			Hex88Board board = new Hex88Board(new GenericBoard("8/8/8/4p1k1/2KP4/8/8/8 w - -"));
			int move = IntMove.createMove(IntMove.NORMAL, IntPosition.d4, IntPosition.e5, Hex88Board.board[IntPosition.d4], Hex88Board.board[IntPosition.e5], IntChessman.NOPIECE);
			new MoveSee(board);
			int value = MoveSee.seeMove(move, IntColor.WHITE);
			assertEquals(IntChessman.VALUE_PAWN, value);

			// En passant capture
			board = new Hex88Board(new GenericBoard("8/8/K7/6k1/2Pp4/8/1P6/8 b - c3"));
			move = IntMove.createMove(IntMove.ENPASSANT, IntPosition.d4, IntPosition.c3, Hex88Board.board[IntPosition.d4], Hex88Board.board[IntPosition.c4], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.BLACK);
			assertEquals(0, value);

			// En passant capture with hidden attacker
			board = new Hex88Board(new GenericBoard("8/6k1/4r3/8/4Pp2/8/1K1P4/8 b - e3"));
			move = IntMove.createMove(IntMove.ENPASSANT, IntPosition.f4, IntPosition.e3, Hex88Board.board[IntPosition.f4], Hex88Board.board[IntPosition.e4], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.BLACK);
			assertEquals(IntChessman.VALUE_PAWN, value);

			// Pawn promotion capture
			board = new Hex88Board(new GenericBoard("8/8/K7/6k1/8/5B2/4p3/3R4 b - -"));
			move = IntMove.createMove(IntMove.PAWNPROMOTION, IntPosition.e2, IntPosition.d1, Hex88Board.board[IntPosition.e2], Hex88Board.board[IntPosition.d1], IntChessman.ROOK);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.BLACK);
			assertEquals(
				IntChessman.VALUE_ROOK
				+ (IntChessman.VALUE_ROOK - IntChessman.VALUE_PAWN)
				- IntChessman.VALUE_ROOK, value);

			// King capture abort
			board = new Hex88Board(new GenericBoard("8/6k1/8/4q3/8/5p2/1R1KP3/8 b - -"));
			move = IntMove.createMove(IntMove.NORMAL, IntPosition.f3, IntPosition.e2, Hex88Board.board[IntPosition.f3], Hex88Board.board[IntPosition.e2], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.BLACK);
			assertEquals(
				IntChessman.VALUE_PAWN
				- IntChessman.VALUE_PAWN
				+ IntChessman.VALUE_KING, value);

			// Complex capture
			board = new Hex88Board(new GenericBoard("R1B3q1/N1KP4/3n4/8/6b1/2R5/6k1/8 b - -"));
			move = IntMove.createMove(IntMove.NORMAL, IntPosition.d6, IntPosition.c8, Hex88Board.board[IntPosition.d6], Hex88Board.board[IntPosition.c8], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.BLACK);
			assertEquals(
				IntChessman.VALUE_BISHOP
				- IntChessman.VALUE_KNIGHT
				- (IntChessman.VALUE_QUEEN - IntChessman.VALUE_PAWN)
				+ IntChessman.VALUE_QUEEN
				- IntChessman.VALUE_BISHOP
				+ IntChessman.VALUE_KNIGHT
				- IntChessman.VALUE_QUEEN, value);

			// Same piece capture test
			board = new Hex88Board(new GenericBoard("r4rk1/5ppp/2Np4/p2P2b1/Pp3Rq1/1R1pP2P/1PP3P1/7K w - -"));
			move = IntMove.createMove(IntMove.NORMAL, IntPosition.c6, IntPosition.b4, Hex88Board.board[IntPosition.c6], Hex88Board.board[IntPosition.b4], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.WHITE);
			assertEquals(
				IntChessman.VALUE_PAWN
				- IntChessman.VALUE_KNIGHT
				+ IntChessman.VALUE_PAWN
				- IntChessman.VALUE_ROOK
				+ IntChessman.VALUE_QUEEN, value);

			// Non-capture move
			board = new Hex88Board(new GenericBoard("8/6k1/4r3/8/5p2/8/1K1PP3/8 w - -"));
			move = IntMove.createMove(IntMove.NORMAL, IntPosition.e2, IntPosition.e3, Hex88Board.board[IntPosition.e2], Hex88Board.board[IntPosition.e3], IntChessman.NOPIECE);
			new MoveSee(board);
			value = MoveSee.seeMove(move, IntColor.WHITE);
			assertEquals(-IntChessman.VALUE_PAWN, value);
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
	}

}
