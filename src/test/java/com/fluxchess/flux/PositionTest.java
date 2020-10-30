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

import com.fluxchess.jcpi.models.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class PositionTest {

	@Test
	void testClassCreation() {
		// Setup a new board
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		// Test chessman setup
		for (GenericFile file : GenericFile.values()) {
			for (GenericRank rank : GenericRank.values()) {
				GenericPiece piece = board.getPiece(GenericPosition.valueOf(file, rank));
				int testChessman = Position.board[Square.valueOfPosition(GenericPosition.valueOf(file, rank))];
				if (piece == null) {
					assertThat(Piece.NOPIECE).isEqualTo(testChessman);
				} else {
					assertThat(Piece.valueOfChessman(piece.chessman)).isEqualTo(Piece.getChessman(testChessman));
					assertThat(Color.valueOfColor(piece.color)).isEqualTo(Piece.getColor(testChessman));
				}
			}
		}

		// Test active color
		assertThat(Color.valueOfColor(board.getActiveColor())).isEqualTo(testBoard.activeColor);

		// Test en passant
		if (board.getEnPassant() == null) {
			assertThat(Square.NOPOSITION).isEqualTo(testBoard.enPassantSquare);
		} else {
			assertThat(Square.valueOfPosition(board.getEnPassant())).isEqualTo(testBoard.enPassantSquare);
		}

		// Test half move clock
		assertThat(board.getHalfMoveClock()).isEqualTo(testBoard.halfMoveClock);

		// Test full move number
		assertThat(board.getFullMoveNumber()).isEqualTo(testBoard.getFullMoveNumber());

		// Test game phase
		assertThat(GamePhase.OPENING).isEqualTo(testBoard.getGamePhase());

		// Test material value
		assertThat(Piece.VALUE_KING + Piece.VALUE_QUEEN + 2 * Piece.VALUE_ROOK + 2 * Piece.VALUE_BISHOP + 2 * Piece.VALUE_KNIGHT + 8 * Piece.VALUE_PAWN).isEqualTo(Position.materialValue[Color.WHITE]);
	}

	@Test
	void testIsRepetition() {
		GenericBoard genericBoard = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position board = new Position(genericBoard);

		// Move white knight
		int move = Move.createMove(MoveType.NORMAL, Square.b1, Square.c3, Piece.WHITE_KNIGHT, Piece.NOPIECE, Piece.NOPIECE);
		board.makeMove(move);

		// Move black knight
		move = Move.createMove(MoveType.NORMAL, Square.b8, Square.c6, Piece.BLACK_KNIGHT, Piece.NOPIECE, Piece.NOPIECE);
		board.makeMove(move);

		// Move white knight
		move = Move.createMove(MoveType.NORMAL, Square.g1, Square.f3, Piece.WHITE_KNIGHT, Piece.NOPIECE, Piece.NOPIECE);
		board.makeMove(move);

		// Move black knight
		move = Move.createMove(MoveType.NORMAL, Square.c6, Square.b8, Piece.BLACK_KNIGHT, Piece.NOPIECE, Piece.NOPIECE);
		board.makeMove(move);

		// Move white knight
		move = Move.createMove(MoveType.NORMAL, Square.f3, Square.g1, Piece.WHITE_KNIGHT, Piece.NOPIECE, Piece.NOPIECE);
		board.makeMove(move);

		assertThat(board.isRepetition()).isTrue();
	}

	@Test
	void testMakeMoveNormalMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		int move = Move.createMove(MoveType.NORMAL, 16, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testMakeMovePawnPromotionMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("8/P5k1/8/8/2K5/8/8/8 w - - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Position testBoard = new Position(board);

		int move = Move.createMove(MoveType.PAWNPROMOTION, 96, 112, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, PieceType.QUEEN);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testMakeMovePawnDoubleMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		int move = Move.createMove(MoveType.PAWNDOUBLE, 16, 48, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testMakeMoveCastlingMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Position testBoard = new Position(board);

		int move = Move.createMove(MoveType.CASTLING, 4, 2, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);
		assertThat(board).isEqualTo(testBoard.getBoard());

		move = Move.createMove(MoveType.CASTLING, 4, 6, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);
		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testMakeMoveEnPassantMove() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("5k2/8/8/8/3Pp3/8/8/3K4 b - d3 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}
		Position testBoard = new Position(board);

		// Make en passant move
		int move = Move.createMove(MoveType.ENPASSANT, Square.e4, Square.d3, Piece.createPiece(PieceType.PAWN, Color.BLACK), Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE);
		testBoard.makeMove(move);
		testBoard.undoMove(move);

		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testMakeMoveNullMove() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		testBoard.makeMoveNull();
		testBoard.undoMoveNull();

		assertThat(board).isEqualTo(testBoard.getBoard());
	}

	@Test
	void testZobrist() {
		GenericBoard board = null;
		try {
			board = new GenericBoard("r3k2r/2P5/8/5p2/3p4/8/2PB4/R3K2R w KQkq - 0 1");
		} catch (IllegalNotationException e) {
			e.printStackTrace();
			fail();
		}

		Position testBoard = new Position(board);
		// Move white bishop
		int move = Move.createMove(MoveType.NORMAL, Square.d2, Square.e3, Piece.createPiece(PieceType.BISHOP, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// EnumCastling black KINGSIDE
		move = Move.createMove(MoveType.CASTLING, Square.e8, Square.g8, Piece.createPiece(PieceType.KING, Color.BLACK), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = Move.createMove(MoveType.PAWNDOUBLE, Square.c2, Square.c4, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move black pawn
		move = Move.createMove(MoveType.ENPASSANT, Square.d4, Square.c3, Piece.createPiece(PieceType.PAWN, Color.BLACK), Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = Move.createMove(MoveType.PAWNPROMOTION, Square.c7, Square.c8, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, PieceType.QUEEN);
		testBoard.makeMove(move);
		long zobrist1 = testBoard.zobristCode;
		long pawnZobrist1 = testBoard.pawnZobristCode;

		testBoard = new Position(board);
		// Move white pawn
		move = Move.createMove(MoveType.PAWNDOUBLE, Square.c2, Square.c4, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move black pawn
		move = Move.createMove(MoveType.ENPASSANT, Square.d4, Square.c3, Piece.createPiece(PieceType.PAWN, Color.BLACK), Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move white bishop
		move = Move.createMove(MoveType.NORMAL, Square.d2, Square.e3, Piece.createPiece(PieceType.BISHOP, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// EnumCastling black KINGSIDE
		move = Move.createMove(MoveType.CASTLING, Square.e8, Square.g8, Piece.createPiece(PieceType.KING, Color.BLACK), Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move white pawn
		move = Move.createMove(MoveType.PAWNPROMOTION, Square.c7, Square.c8, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, PieceType.QUEEN);
		testBoard.makeMove(move);
		long zobrist2 = testBoard.zobristCode;
		long pawnZobrist2 = testBoard.pawnZobristCode;

		assertThat(zobrist1).isEqualTo(zobrist2);
		assertThat(pawnZobrist1).isEqualTo(pawnZobrist2);
	}

	@Test
	void testActiveColor() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		// Move white pawn
		int move = Move.createMove(MoveType.NORMAL, 16, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(Color.BLACK).isEqualTo(testBoard.activeColor);

		// Move black pawn
		move = Move.createMove(MoveType.NORMAL, 96, 80, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(Color.WHITE).isEqualTo(testBoard.activeColor);
	}

	@Test
	void testHalfMoveClock() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		// Move white pawn
		int move = Move.createMove(MoveType.NORMAL, 16, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(0).isEqualTo(testBoard.halfMoveClock);

		// Move black pawn
		move = Move.createMove(MoveType.NORMAL, 96, 80, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		// Move white knight
		move = Move.createMove(MoveType.NORMAL, 1, 34, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(1).isEqualTo(testBoard.halfMoveClock);
	}

	@Test
	void testFullMoveNumber() {
		GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
		Position testBoard = new Position(board);

		// Move white pawn
		int move = Move.createMove(MoveType.NORMAL, 16, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(1).isEqualTo(testBoard.getFullMoveNumber());

		// Move black pawn
		move = Move.createMove(MoveType.NORMAL, 96, 80, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		testBoard.makeMove(move);
		assertThat(2).isEqualTo(testBoard.getFullMoveNumber());
	}
}
