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

import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.jcpi.models.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BoardTest {

  @Test
  public void testClassCreation() {
    // Setup a new board
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    // Test chessman setup
    for (GenericFile file : GenericFile.values()) {
      for (GenericRank rank : GenericRank.values()) {
        GenericPiece piece = board.getPiece(GenericPosition.valueOf(file, rank));
        int testChessman = testBoard.board[Position.valueOfPosition(GenericPosition.valueOf(file, rank))];
        if (piece == null) {
          assertEquals(IntPiece.NOPIECE, testChessman);
        } else {
          assertEquals(IntChessman.valueOf(piece.chessman), IntPiece.getChessman(testChessman));
          assertEquals(IntColor.valueOf(piece.color), IntPiece.getColor(testChessman));
        }
      }
    }

    // Test active color
    assertEquals(IntColor.valueOf(board.getActiveColor()), testBoard.activeColor);

    // Test en passant
    if (board.getEnPassant() == null) {
      assertEquals(Position.NOPOSITION, testBoard.enPassantSquare);
    } else {
      assertEquals(Position.valueOfPosition(board.getEnPassant()), testBoard.enPassantSquare);
    }

    // Test half move clock
    assertEquals(board.getHalfMoveClock(), testBoard.halfMoveClock);

    // Test full move number
    assertEquals(board.getFullMoveNumber(), testBoard.getFullMoveNumber());

    // Test game phase
    assertEquals(IntGamePhase.OPENING, testBoard.getGamePhase());

    // Test material value
    assertEquals(Evaluation.VALUE_KING + Evaluation.VALUE_QUEEN + 2 * Evaluation.VALUE_ROOK + 2 * Evaluation.VALUE_BISHOP + 2 * Evaluation.VALUE_KNIGHT + 8 * Evaluation.VALUE_PAWN, testBoard.materialValueAll[IntColor.WHITE]);
    assertEquals(1 /* QUEEN */ + 2 /* ROOKS */ + 2 /* BISHOPS */ + 2 /* KNIGHTS */, testBoard.materialCount[IntColor.WHITE]);
    assertEquals(1 /* QUEEN */ + 2 /* ROOKS */ + 2 /* BISHOPS */ + 2 /* KNIGHTS */ + 8 /* PAWNS */, testBoard.materialCountAll[IntColor.WHITE]);
  }

  @Test
  public void testMakeMoveNormalMove() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    int move = Move.createMove(Move.Type.NORMAL, 16, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
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
    Board testBoard = new Board(board);

    int move = Move.createMove(Move.Type.PAWNPROMOTION, 96, 112, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.QUEEN);
    testBoard.makeMove(move);
    testBoard.undoMove(move);

    assertEquals(board, testBoard.getBoard());
  }

  @Test
  public void testMakeMovePawnDoubleMove() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    int move = Move.createMove(Move.Type.PAWNDOUBLE, 16, 48, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
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
    Board testBoard = new Board(board);

    int move = Move.createMove(Move.Type.CASTLING, 4, 2, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    testBoard.undoMove(move);
    assertEquals(board, testBoard.getBoard());

    move = Move.createMove(Move.Type.CASTLING, 4, 6, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
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
    Board testBoard = new Board(board);

    // Make en passant move
    int move = Move.createMove(Move.Type.ENPASSANT, Position.e4, Position.d3, IntPiece.BLACKPAWN, IntPiece.WHITEPAWN, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    testBoard.undoMove(move);

    assertEquals(board, testBoard.getBoard());
  }

  @Test
  public void testMakeMoveNullMove() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    int move = Move.createMove(Move.Type.NULL, Position.NOPOSITION, Position.NOPOSITION, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
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

    Board testBoard = new Board(board);
    // Move white bishop
    int move = Move.createMove(Move.Type.NORMAL, Position.d2, Position.e3, IntPiece.WHITEBISHOP, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Castling black KINGSIDE
    move = Move.createMove(Move.Type.CASTLING, Position.e8, Position.g8, IntPiece.BLACKKING, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move white pawn
    move = Move.createMove(Move.Type.PAWNDOUBLE, Position.c2, Position.c4, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move black pawn
    move = Move.createMove(Move.Type.ENPASSANT, Position.d4, Position.c3, IntPiece.BLACKPAWN, IntPiece.WHITEPAWN, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move white pawn
    move = Move.createMove(Move.Type.PAWNPROMOTION, Position.c7, Position.c8, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.QUEEN);
    testBoard.makeMove(move);
    long zobrist1 = testBoard.zobristCode;
    long pawnZobrist1 = testBoard.pawnZobristCode;

    testBoard = new Board(board);
    // Move white pawn
    move = Move.createMove(Move.Type.PAWNDOUBLE, Position.c2, Position.c4, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move black pawn
    move = Move.createMove(Move.Type.ENPASSANT, Position.d4, Position.c3, IntPiece.BLACKPAWN, IntPiece.WHITEPAWN, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move white bishop
    move = Move.createMove(Move.Type.NORMAL, Position.d2, Position.e3, IntPiece.WHITEBISHOP, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Castling black KINGSIDE
    move = Move.createMove(Move.Type.CASTLING, Position.e8, Position.g8, IntPiece.BLACKKING, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move white pawn
    move = Move.createMove(Move.Type.PAWNPROMOTION, Position.c7, Position.c8, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.QUEEN);
    testBoard.makeMove(move);
    long zobrist2 = testBoard.zobristCode;
    long pawnZobrist2 = testBoard.pawnZobristCode;

    assertEquals(zobrist1, zobrist2);
    assertEquals(pawnZobrist1, pawnZobrist2);
  }

  @Test
  public void testActiveColor() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    // Move white pawn
    int move = Move.createMove(Move.Type.NORMAL, 16, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(IntColor.BLACK, testBoard.activeColor);

    // Move black pawn
    move = Move.createMove(Move.Type.NORMAL, 96, 80, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(IntColor.WHITE, testBoard.activeColor);
  }

  @Test
  public void testHalfMoveClock() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    // Move white pawn
    int move = Move.createMove(Move.Type.NORMAL, 16, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(0, testBoard.halfMoveClock);

    // Move black pawn
    move = Move.createMove(Move.Type.NORMAL, 96, 80, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    // Move white knight
    move = Move.createMove(Move.Type.NORMAL, 1, 34, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(1, testBoard.halfMoveClock);
  }

  @Test
  public void testFullMoveNumber() {
    GenericBoard board = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board testBoard = new Board(board);

    // Move white pawn
    int move = Move.createMove(Move.Type.NORMAL, 16, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(1, testBoard.getFullMoveNumber());

    // Move black pawn
    move = Move.createMove(Move.Type.NORMAL, 96, 80, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    testBoard.makeMove(move);
    assertEquals(2, testBoard.getFullMoveNumber());
  }

}
