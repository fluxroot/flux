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
import com.fluxchess.flux.board.Position;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

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
      if (board.board[Position.a7] == IntPiece.WHITEBISHOP
        && board.board[Position.b6] == IntPiece.BLACKPAWN) {
        total -= 100;
        if (board.board[Position.c7] == IntPiece.BLACKPAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.b8] == IntPiece.WHITEBISHOP
        && board.board[Position.c7] == IntPiece.BLACKPAWN) {
        total -= 100;
      }
      if (board.board[Position.h7] == IntPiece.WHITEBISHOP
        && board.board[Position.g6] == IntPiece.BLACKPAWN) {
        total -= 100;
        if (board.board[Position.f7] == IntPiece.BLACKPAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.g8] == IntPiece.WHITEBISHOP
        && board.board[Position.f7] == IntPiece.BLACKPAWN) {
        total -= 100;
      }
      if (board.board[Position.a6] == IntPiece.WHITEBISHOP
        && board.board[Position.b5] == IntPiece.BLACKPAWN) {
        total -= 50;
      }
      if (board.board[Position.h6] == IntPiece.WHITEBISHOP
        && board.board[Position.g5] == IntPiece.BLACKPAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Position.d2] == IntPiece.WHITEPAWN
        && board.board[Position.d3] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Position.c1] == IntPiece.WHITEBISHOP) {
          total -= 30;
        }
      }
      if (board.board[Position.e2] == IntPiece.WHITEPAWN
        && board.board[Position.e3] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Position.f1] == IntPiece.WHITEBISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Position.c1] == IntPiece.WHITEKING
        || board.board[Position.b1] == IntPiece.WHITEKING)
        && (board.board[Position.a1] == IntPiece.WHITEROOK
        || board.board[Position.a2] == IntPiece.WHITEROOK
        || board.board[Position.b1] == IntPiece.WHITEROOK)) {
        total -= 50;
      }
      if ((board.board[Position.f1] == IntPiece.WHITEKING
        || board.board[Position.g1] == IntPiece.WHITEKING)
        && (board.board[Position.h1] == IntPiece.WHITEROOK
        || board.board[Position.h2] == IntPiece.WHITEROOK
        || board.board[Position.g1] == IntPiece.WHITEROOK)) {
        total -= 50;
      }
    } else {
      assert myColor == IntColor.BLACK;

      // Trapped black bishop
      if (board.board[Position.a2] == IntPiece.BLACKBISHOP
        && board.board[Position.b3] == IntPiece.WHITEPAWN) {
        total -= 100;
        if (board.board[Position.c2] == IntPiece.WHITEPAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.b1] == IntPiece.BLACKBISHOP
        && board.board[Position.c2] == IntPiece.WHITEPAWN) {
        total -= 100;
      }
      if (board.board[Position.h2] == IntPiece.BLACKBISHOP
        && board.board[Position.g3] == IntPiece.WHITEPAWN) {
        total -= 100;
        if (board.board[Position.f2] == IntPiece.WHITEPAWN) {
          total -= 50;
        }
      }
      if (board.board[Position.g1] == IntPiece.BLACKBISHOP
        && board.board[Position.f2] == IntPiece.WHITEPAWN) {
        total -= 100;
      }
      if (board.board[Position.a3] == IntPiece.BLACKBISHOP
        && board.board[Position.b4] == IntPiece.WHITEPAWN) {
        total -= 50;
      }
      if (board.board[Position.h3] == IntPiece.BLACKBISHOP
        && board.board[Position.g4] == IntPiece.WHITEPAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Position.d7] == IntPiece.BLACKPAWN
        && board.board[Position.d6] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Position.c8] == IntPiece.BLACKBISHOP) {
          total -= 30;
        }
      }
      if (board.board[Position.e7] == IntPiece.BLACKPAWN
        && board.board[Position.e6] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Position.f8] == IntPiece.BLACKBISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Position.c8] == IntPiece.BLACKKING
        || board.board[Position.b8] == IntPiece.BLACKKING)
        && (board.board[Position.a8] == IntPiece.BLACKROOK
        || board.board[Position.a7] == IntPiece.BLACKROOK
        || board.board[Position.b8] == IntPiece.BLACKROOK)) {
        total -= 50;
      }
      if ((board.board[Position.f8] == IntPiece.BLACKKING
        || board.board[Position.g8] == IntPiece.BLACKKING)
        && (board.board[Position.h8] == IntPiece.BLACKROOK
        || board.board[Position.h7] == IntPiece.BLACKROOK
        || board.board[Position.g8] == IntPiece.BLACKROOK)) {
        total -= 50;
      }
    }

    return total;
  }

}
