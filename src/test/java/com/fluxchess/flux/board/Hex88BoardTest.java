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
package com.fluxchess.flux.board;

import com.fluxchess.jcpi.models.*;
import com.fluxchess.flux.move.IntMove;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Hex88BoardTest {

	@Test
	public void testClassCreation() {
		// Setup a new board
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		// Test chessman setup
		for (GenericFile file : GenericFile.values()) {
			for (GenericRank rank : GenericRank.values()) {
				GenericPiece piece = board.getPiece(GenericPosition.valueOf(file, rank));
				int testChessman = Hex88Board.board[IntPosition.valueOfPosition(GenericPosition.valueOf(file, rank))];
				if (piece == null) {
					assertEquals(IntChessman.NOPIECE, testChessman);
				} else {
					assertEquals(IntChessman.valueOfChessman(piece.chessman), IntChessman.getChessman(testChessman));
					assertEquals(IntColor.valueOfColor(piece.color), IntChessman.getColor(testChessman));
				}
			}
		}

		// Test active color
		assertEquals(IntColor.valueOfColor(board.getActiveColor()), testBoard.activeColor);

		// Test en passant
		if (board.getEnPassant() == null) {
			assertEquals(IntPosition.NOPOSITION, testBoard.enPassantSquare);
		} else {
			assertEquals(IntPosition.valueOfPosition(board.getEnPassant()), testBoard.enPassantSquare);
		}

		// Test half move clock
		assertEquals(board.getHalfMoveClock(), testBoard.halfMoveClock);

		// Test full move number
		assertEquals(board.getFullMoveNumber(), testBoard.getFullMoveNumber());

		// Test game phase
		assertEquals(IntGamePhase.OPENING, testBoard.getGamePhase());

		// Test material value
		assertEquals(IntChessman.VALUE_KING + IntChessman.VALUE_QUEEN + 2 * IntChessman.VALUE_ROOK + 2 * IntChessman.VALUE_BISHOP + 2 * IntChessman.VALUE_KNIGHT + 8 * IntChessman.VALUE_PAWN, Hex88Board.materialValue[IntColor.WHITE]);
	}

	@Test
	public void testMakeMoveNormalMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		int move = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertEquals(board, testBoard.getBoard());
	}

	@Test
	public void testMakeMovePawnPromotionMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("8/P5k1/8/8/2K5/8/8/8 w - - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Hex88Board testBoard = new Hex88Board(board);

		int move = IntMove.createMove(IntMove.PAWNPROMOTION, 96, 112, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.QUEEN);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertEquals(board, testBoard.getBoard());
	}

	@Test
	public void testMakeMovePawnDoubleMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		int move = IntMove.createMove(IntMove.PAWNDOUBLE, 16, 48, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertEquals(board, testBoard.getBoard());
	}
	
	@Test
	public void testMakeMoveCastlingMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Hex88Board testBoard = new Hex88Board(board);

		int move = IntMove.createMove(IntMove.CASTLING, 4, 2, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);
		assertEquals(board, testBoard.getBoard());

		move = IntMove.createMove(IntMove.CASTLING, 4, 6, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);
		assertEquals(board, testBoard.getBoard());
	}

	@Test
	public void testMakeMoveEnPassantMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("5k2/8/8/8/3Pp3/8/8/3K4 b - d3 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Hex88Board testBoard = new Hex88Board(board);

		// Make en passant move
		int move = IntMove.createMove(IntMove.ENPASSANT, IntPosition.e4, IntPosition.d3, IntChessman.createPiece(IntChessman.PAWN, IntColor.BLACK), IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertEquals(board, testBoard.getBoard());
	}

	@Test
	public void testMakeMoveNullMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		int move = IntMove.createMove(IntMove.NULL, IntPosition.NOPOSITION, IntPosition.NOPOSITION, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertEquals(board, testBoard.getBoard());
	}

	@Test
	public void testZobrist() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("r3k2r/2P5/8/5p2/3p4/8/2PB4/R3K2R w KQkq - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}

		Hex88Board testBoard = new Hex88Board(board);
		// Move white bishop
		int move = IntMove.createMove(IntMove.NORMAL, IntPosition.d2, IntPosition.e3, IntChessman.createPiece(IntChessman.BISHOP, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// EnumCastling black KINGSIDE
		move = IntMove.createMove(IntMove.CASTLING, IntPosition.e8, IntPosition.g8, IntChessman.createPiece(IntChessman.KING, IntColor.BLACK), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = IntMove.createMove(IntMove.PAWNDOUBLE, IntPosition.c2, IntPosition.c4, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move black pawn
		move = IntMove.createMove(IntMove.ENPASSANT, IntPosition.d4, IntPosition.c3, IntChessman.createPiece(IntChessman.PAWN, IntColor.BLACK), IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = IntMove.createMove(IntMove.PAWNPROMOTION, IntPosition.c7, IntPosition.c8, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.QUEEN);
		testBoard.makeMove(move);
		long zobrist1 = testBoard.zobristCode;
		long pawnZobrist1 = testBoard.pawnZobristCode;

		testBoard = new Hex88Board(board);
		// Move white pawn
		move = IntMove.createMove(IntMove.PAWNDOUBLE, IntPosition.c2, IntPosition.c4, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move black pawn
		move = IntMove.createMove(IntMove.ENPASSANT, IntPosition.d4, IntPosition.c3, IntChessman.createPiece(IntChessman.PAWN, IntColor.BLACK), IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move white bishop
		move = IntMove.createMove(IntMove.NORMAL, IntPosition.d2, IntPosition.e3, IntChessman.createPiece(IntChessman.BISHOP, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// EnumCastling black KINGSIDE
		move = IntMove.createMove(IntMove.CASTLING, IntPosition.e8, IntPosition.g8, IntChessman.createPiece(IntChessman.KING, IntColor.BLACK), IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = IntMove.createMove(IntMove.PAWNPROMOTION, IntPosition.c7, IntPosition.c8, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.QUEEN);
		testBoard.makeMove(move);
		long zobrist2 = testBoard.zobristCode;
		long pawnZobrist2 = testBoard.pawnZobristCode;
		
		assertEquals(zobrist1, zobrist2);
		assertEquals(pawnZobrist1, pawnZobrist2);
	}

	@Test
	public void testActiveColor() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		// Move white pawn
		int move = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(IntColor.BLACK, testBoard.activeColor);

		// Move black pawn
		move = IntMove.createMove(IntMove.NORMAL, 96, 80, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(IntColor.WHITE, testBoard.activeColor);
	}

	@Test
	public void testHalfMoveClock() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		// Move white pawn
		int move = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(0, testBoard.halfMoveClock);

		// Move black pawn
		move = IntMove.createMove(IntMove.NORMAL, 96, 80, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		// Move white knight
		move = IntMove.createMove(IntMove.NORMAL, 1, 34, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(1, testBoard.halfMoveClock);
	}

	@Test
	public void testFullMoveNumber() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Hex88Board testBoard = new Hex88Board(board);

		// Move white pawn
		int move = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(1, testBoard.getFullMoveNumber());

		// Move black pawn
		move = IntMove.createMove(IntMove.NORMAL, 96, 80, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		testBoard.makeMove(move);
		assertEquals(2, testBoard.getFullMoveNumber());
	}

}
