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
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoveSeeTest {

  @Test
  public void testSeeMove() {
    try {
      // Pawn capture
      Board board = new Board(new GenericBoard("8/8/8/4p1k1/2KP4/8/8/8 w - -"));
      int move = Move.valueOf(Move.Type.NORMAL, Square.d4, Square.e5, board.board[Square.d4], board.board[Square.e5], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      int value = MoveSee.seeMove(move, IntColor.WHITE);
      assertEquals(Evaluation.VALUE_PAWN, value);

      // En passant capture
      board = new Board(new GenericBoard("8/8/K7/6k1/2Pp4/8/1P6/8 b - c3"));
      move = Move.valueOf(Move.Type.ENPASSANT, Square.d4, Square.c3, board.board[Square.d4], board.board[Square.c4], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(0, value);

      // En passant capture with hidden attacker
      board = new Board(new GenericBoard("8/6k1/4r3/8/4Pp2/8/1K1P4/8 b - e3"));
      move = Move.valueOf(Move.Type.ENPASSANT, Square.f4, Square.e3, board.board[Square.f4], board.board[Square.e4], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(Evaluation.VALUE_PAWN, value);

      // Pawn promotion capture
      board = new Board(new GenericBoard("8/8/K7/6k1/8/5B2/4p3/3R4 b - -"));
      move = Move.valueOf(Move.Type.PAWNPROMOTION, Square.e2, Square.d1, board.board[Square.e2], board.board[Square.d1], IntChessman.ROOK);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(
        Evaluation.VALUE_ROOK
          + (Evaluation.VALUE_ROOK - Evaluation.VALUE_PAWN)
          - Evaluation.VALUE_ROOK, value);

      // King capture abort
      board = new Board(new GenericBoard("8/6k1/8/4q3/8/5p2/1R1KP3/8 b - -"));
      move = Move.valueOf(Move.Type.NORMAL, Square.f3, Square.e2, board.board[Square.f3], board.board[Square.e2], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(
        Evaluation.VALUE_PAWN
          - Evaluation.VALUE_PAWN
          + Evaluation.VALUE_KING, value);

      // Complex capture
      board = new Board(new GenericBoard("R1B3q1/N1KP4/3n4/8/6b1/2R5/6k1/8 b - -"));
      move = Move.valueOf(Move.Type.NORMAL, Square.d6, Square.c8, board.board[Square.d6], board.board[Square.c8], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(
        Evaluation.VALUE_BISHOP
          - Evaluation.VALUE_KNIGHT
          - (Evaluation.VALUE_QUEEN - Evaluation.VALUE_PAWN)
          + Evaluation.VALUE_QUEEN
          - Evaluation.VALUE_BISHOP
          + Evaluation.VALUE_KNIGHT
          - Evaluation.VALUE_QUEEN, value);

      // Same piece capture test
      board = new Board(new GenericBoard("r4rk1/5ppp/2Np4/p2P2b1/Pp3Rq1/1R1pP2P/1PP3P1/7K w - -"));
      move = Move.valueOf(Move.Type.NORMAL, Square.c6, Square.b4, board.board[Square.c6], board.board[Square.b4], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.WHITE);
      assertEquals(
        Evaluation.VALUE_PAWN
          - Evaluation.VALUE_KNIGHT
          + Evaluation.VALUE_PAWN
          - Evaluation.VALUE_ROOK
          + Evaluation.VALUE_QUEEN, value);

      // Non-capture move
      board = new Board(new GenericBoard("8/6k1/4r3/8/5p2/8/1K1PP3/8 w - -"));
      move = Move.valueOf(Move.Type.NORMAL, Square.e2, Square.e3, board.board[Square.e2], board.board[Square.e3], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.WHITE);
      assertEquals(-Evaluation.VALUE_PAWN, value);
    } catch (IllegalNotationException e) {
      e.printStackTrace();
      fail();
    }
  }

}
