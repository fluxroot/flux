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
      int move = Move.valueOf(Move.Type.NORMAL, Position.d4, Position.e5, board.board[Position.d4], board.board[Position.e5], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      int value = MoveSee.seeMove(move, IntColor.WHITE);
      assertEquals(Evaluation.VALUE_PAWN, value);

      // En passant capture
      board = new Board(new GenericBoard("8/8/K7/6k1/2Pp4/8/1P6/8 b - c3"));
      move = Move.valueOf(Move.Type.ENPASSANT, Position.d4, Position.c3, board.board[Position.d4], board.board[Position.c4], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(0, value);

      // En passant capture with hidden attacker
      board = new Board(new GenericBoard("8/6k1/4r3/8/4Pp2/8/1K1P4/8 b - e3"));
      move = Move.valueOf(Move.Type.ENPASSANT, Position.f4, Position.e3, board.board[Position.f4], board.board[Position.e4], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(Evaluation.VALUE_PAWN, value);

      // Pawn promotion capture
      board = new Board(new GenericBoard("8/8/K7/6k1/8/5B2/4p3/3R4 b - -"));
      move = Move.valueOf(Move.Type.PAWNPROMOTION, Position.e2, Position.d1, board.board[Position.e2], board.board[Position.d1], IntChessman.ROOK);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(
        Evaluation.VALUE_ROOK
          + (Evaluation.VALUE_ROOK - Evaluation.VALUE_PAWN)
          - Evaluation.VALUE_ROOK, value);

      // King capture abort
      board = new Board(new GenericBoard("8/6k1/8/4q3/8/5p2/1R1KP3/8 b - -"));
      move = Move.valueOf(Move.Type.NORMAL, Position.f3, Position.e2, board.board[Position.f3], board.board[Position.e2], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.BLACK);
      assertEquals(
        Evaluation.VALUE_PAWN
          - Evaluation.VALUE_PAWN
          + Evaluation.VALUE_KING, value);

      // Complex capture
      board = new Board(new GenericBoard("R1B3q1/N1KP4/3n4/8/6b1/2R5/6k1/8 b - -"));
      move = Move.valueOf(Move.Type.NORMAL, Position.d6, Position.c8, board.board[Position.d6], board.board[Position.c8], IntChessman.NOCHESSMAN);
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
      move = Move.valueOf(Move.Type.NORMAL, Position.c6, Position.b4, board.board[Position.c6], board.board[Position.b4], IntChessman.NOCHESSMAN);
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
      move = Move.valueOf(Move.Type.NORMAL, Position.e2, Position.e3, board.board[Position.e2], board.board[Position.e3], IntChessman.NOCHESSMAN);
      new MoveSee(board);
      value = MoveSee.seeMove(move, IntColor.WHITE);
      assertEquals(-Evaluation.VALUE_PAWN, value);
    } catch (IllegalNotationException e) {
      e.printStackTrace();
      fail();
    }
  }

}
