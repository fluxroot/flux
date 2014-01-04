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
package com.fluxchess.flux.evaluation;

import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.Position;

public final class PatternEvaluation {

  private PatternEvaluation() {
  }

  public static int evaluatePatterns(int myColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int total = 0;

    if (myColor == IntColor.WHITE) {
      // Trapped white bishop
      if (board.board[Position.a7] == IntChessman.WHITE_BISHOP
        && board.board[Position.b6] == IntChessman.BLACK_PAWN) {
        total -= 100;
        if (board.board[Position.c7] == IntChessman.BLACK_PAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.b8] == IntChessman.WHITE_BISHOP
        && board.board[Position.c7] == IntChessman.BLACK_PAWN) {
        total -= 100;
      }
      if (board.board[Position.h7] == IntChessman.WHITE_BISHOP
        && board.board[Position.g6] == IntChessman.BLACK_PAWN) {
        total -= 100;
        if (board.board[Position.f7] == IntChessman.BLACK_PAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.g8] == IntChessman.WHITE_BISHOP
        && board.board[Position.f7] == IntChessman.BLACK_PAWN) {
        total -= 100;
      }
      if (board.board[Position.a6] == IntChessman.WHITE_BISHOP
        && board.board[Position.b5] == IntChessman.BLACK_PAWN) {
        total -= 50;
      }
      if (board.board[Position.h6] == IntChessman.WHITE_BISHOP
        && board.board[Position.g5] == IntChessman.BLACK_PAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Position.d2] == IntChessman.WHITE_PAWN
        && board.board[Position.d3] != IntChessman.NOPIECE) {
        total -= 20;
        if (board.board[Position.c1] == IntChessman.WHITE_BISHOP) {
          total -= 30;
        }
      }
      if (board.board[Position.e2] == IntChessman.WHITE_PAWN
        && board.board[Position.e3] != IntChessman.NOPIECE) {
        total -= 20;
        if (board.board[Position.f1] == IntChessman.WHITE_BISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Position.c1] == IntChessman.WHITE_KING
        || board.board[Position.b1] == IntChessman.WHITE_KING)
        && (board.board[Position.a1] == IntChessman.WHITE_ROOK
        || board.board[Position.a2] == IntChessman.WHITE_ROOK
        || board.board[Position.b1] == IntChessman.WHITE_ROOK)) {
        total -= 50;
      }
      if ((board.board[Position.f1] == IntChessman.WHITE_KING
        || board.board[Position.g1] == IntChessman.WHITE_KING)
        && (board.board[Position.h1] == IntChessman.WHITE_ROOK
        || board.board[Position.h2] == IntChessman.WHITE_ROOK
        || board.board[Position.g1] == IntChessman.WHITE_ROOK)) {
        total -= 50;
      }
    } else {
      assert myColor == IntColor.BLACK;

      // Trapped black bishop
      if (board.board[Position.a2] == IntChessman.BLACK_BISHOP
        && board.board[Position.b3] == IntChessman.WHITE_PAWN) {
        total -= 100;
        if (board.board[Position.c2] == IntChessman.WHITE_PAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.b1] == IntChessman.BLACK_BISHOP
        && board.board[Position.c2] == IntChessman.WHITE_PAWN) {
        total -= 100;
      }
      if (board.board[Position.h2] == IntChessman.BLACK_BISHOP
        && board.board[Position.g3] == IntChessman.WHITE_PAWN) {
        total -= 100;
        if (board.board[Position.f2] == IntChessman.WHITE_PAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.g1] == IntChessman.BLACK_BISHOP
        && board.board[Position.f2] == IntChessman.WHITE_PAWN) {
        total -= 100;
      }
      if (board.board[Position.a3] == IntChessman.BLACK_BISHOP
        && board.board[Position.b4] == IntChessman.WHITE_PAWN) {
        total -= 50;
      }
      if (board.board[Position.h3] == IntChessman.BLACK_BISHOP
        && board.board[Position.g4] == IntChessman.WHITE_PAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Position.d7] == IntChessman.BLACK_PAWN
        && board.board[Position.d6] != IntChessman.NOPIECE) {
        total -= 20;
        if (board.board[Position.c8] == IntChessman.BLACK_BISHOP) {
          total -= 30;
        }
      }
      if (board.board[Position.e7] == IntChessman.BLACK_PAWN
        && board.board[Position.e6] != IntChessman.NOPIECE) {
        total -= 20;
        if (board.board[Position.f8] == IntChessman.BLACK_BISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Position.c8] == IntChessman.BLACK_KING
        || board.board[Position.b8] == IntChessman.BLACK_KING)
        && (board.board[Position.a8] == IntChessman.BLACK_ROOK
        || board.board[Position.a7] == IntChessman.BLACK_ROOK
        || board.board[Position.b8] == IntChessman.BLACK_ROOK)) {
        total -= 50;
      }
      if ((board.board[Position.f8] == IntChessman.BLACK_KING
        || board.board[Position.g8] == IntChessman.BLACK_KING)
        && (board.board[Position.h8] == IntChessman.BLACK_ROOK
        || board.board[Position.h7] == IntChessman.BLACK_ROOK
        || board.board[Position.g8] == IntChessman.BLACK_ROOK)) {
        total -= 50;
      }
    }

    return total;
  }

}
