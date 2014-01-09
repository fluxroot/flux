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
import com.fluxchess.flux.board.Square;
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
      if (board.board[Square.a7] == IntPiece.WHITEBISHOP
        && board.board[Square.b6] == IntPiece.BLACKPAWN) {
        total -= 100;
        if (board.board[Square.c7] == IntPiece.BLACKPAWN) {
          total -= 50;
        }
      }
      if (board.board[Square.b8] == IntPiece.WHITEBISHOP
        && board.board[Square.c7] == IntPiece.BLACKPAWN) {
        total -= 100;
      }
      if (board.board[Square.h7] == IntPiece.WHITEBISHOP
        && board.board[Square.g6] == IntPiece.BLACKPAWN) {
        total -= 100;
        if (board.board[Square.f7] == IntPiece.BLACKPAWN) {
          total -= 50;
        }
      }
      if (board.board[Square.g8] == IntPiece.WHITEBISHOP
        && board.board[Square.f7] == IntPiece.BLACKPAWN) {
        total -= 100;
      }
      if (board.board[Square.a6] == IntPiece.WHITEBISHOP
        && board.board[Square.b5] == IntPiece.BLACKPAWN) {
        total -= 50;
      }
      if (board.board[Square.h6] == IntPiece.WHITEBISHOP
        && board.board[Square.g5] == IntPiece.BLACKPAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Square.d2] == IntPiece.WHITEPAWN
        && board.board[Square.d3] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Square.c1] == IntPiece.WHITEBISHOP) {
          total -= 30;
        }
      }
      if (board.board[Square.e2] == IntPiece.WHITEPAWN
        && board.board[Square.e3] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Square.f1] == IntPiece.WHITEBISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Square.c1] == IntPiece.WHITEKING
        || board.board[Square.b1] == IntPiece.WHITEKING)
        && (board.board[Square.a1] == IntPiece.WHITEROOK
        || board.board[Square.a2] == IntPiece.WHITEROOK
        || board.board[Square.b1] == IntPiece.WHITEROOK)) {
        total -= 50;
      }
      if ((board.board[Square.f1] == IntPiece.WHITEKING
        || board.board[Square.g1] == IntPiece.WHITEKING)
        && (board.board[Square.h1] == IntPiece.WHITEROOK
        || board.board[Square.h2] == IntPiece.WHITEROOK
        || board.board[Square.g1] == IntPiece.WHITEROOK)) {
        total -= 50;
      }
    } else {
      assert myColor == IntColor.BLACK;

      // Trapped black bishop
      if (board.board[Square.a2] == IntPiece.BLACKBISHOP
        && board.board[Square.b3] == IntPiece.WHITEPAWN) {
        total -= 100;
        if (board.board[Square.c2] == IntPiece.WHITEPAWN) {
          total -= 50;
        }
      }
      if (board.board[Square.b1] == IntPiece.BLACKBISHOP
        && board.board[Square.c2] == IntPiece.WHITEPAWN) {
        total -= 100;
      }
      if (board.board[Square.h2] == IntPiece.BLACKBISHOP
        && board.board[Square.g3] == IntPiece.WHITEPAWN) {
        total -= 100;
        if (board.board[Square.f2] == IntPiece.WHITEPAWN) {
          total -= 50;
        }
      }
      if (board.board[Square.g1] == IntPiece.BLACKBISHOP
        && board.board[Square.f2] == IntPiece.WHITEPAWN) {
        total -= 100;
      }
      if (board.board[Square.a3] == IntPiece.BLACKBISHOP
        && board.board[Square.b4] == IntPiece.WHITEPAWN) {
        total -= 50;
      }
      if (board.board[Square.h3] == IntPiece.BLACKBISHOP
        && board.board[Square.g4] == IntPiece.WHITEPAWN) {
        total -= 50;
      }

      // Blocked center pawn
      if (board.board[Square.d7] == IntPiece.BLACKPAWN
        && board.board[Square.d6] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Square.c8] == IntPiece.BLACKBISHOP) {
          total -= 30;
        }
      }
      if (board.board[Square.e7] == IntPiece.BLACKPAWN
        && board.board[Square.e6] != IntPiece.NOPIECE) {
        total -= 20;
        if (board.board[Square.f8] == IntPiece.BLACKBISHOP) {
          total -= 30;
        }
      }

      // Blocked rook
      if ((board.board[Square.c8] == IntPiece.BLACKKING
        || board.board[Square.b8] == IntPiece.BLACKKING)
        && (board.board[Square.a8] == IntPiece.BLACKROOK
        || board.board[Square.a7] == IntPiece.BLACKROOK
        || board.board[Square.b8] == IntPiece.BLACKROOK)) {
        total -= 50;
      }
      if ((board.board[Square.f8] == IntPiece.BLACKKING
        || board.board[Square.g8] == IntPiece.BLACKKING)
        && (board.board[Square.h8] == IntPiece.BLACKROOK
        || board.board[Square.h7] == IntPiece.BLACKROOK
        || board.board[Square.g8] == IntPiece.BLACKROOK)) {
        total -= 50;
      }
    }

    return total;
  }

}
