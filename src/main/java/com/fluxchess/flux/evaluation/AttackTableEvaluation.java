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
import com.fluxchess.flux.board.MoveGenerator;
import com.fluxchess.flux.board.Square;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

import java.util.Arrays;

final class AttackTableEvaluation {

  public static final byte BIT_PAWN = 1 << 3;
  public static final byte BIT_MINOR = 1 << 4;
  public static final byte BIT_ROOK = 1 << 5;
  public static final byte BIT_QUEEN = 1 << 6;
  public static final byte BIT_KING = -128;

  private static final AttackTableEvaluation instance = new AttackTableEvaluation();

  // Our attack table
  public final byte[][] attackTable = new byte[IntColor.values.length][Board.BOARDSIZE];

  private AttackTableEvaluation() {
  }

  public static AttackTableEvaluation getInstance() {
    return instance;
  }

  public void createAttackTable(int myColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;

    // Zero our table
    Arrays.fill(attackTable[myColor], (byte) 0);

    // Fill attack table
    pawnInformationToAttackTable(myColor, board, attackTable[myColor]);
    knightInformationToAttackTable(myColor, board, attackTable[myColor]);
    bishopInformationToAttackTable(myColor, board, attackTable[myColor]);
    rookInformationToAttackTable(myColor, board, attackTable[myColor]);
    queenInformationToAttackTable(myColor, board, attackTable[myColor]);
    kingInformationToAttackTable(myColor, board, attackTable[myColor]);
  }

  private void pawnInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each pawn
    for (long squares = board.pawnList[myColor]; squares != 0; squares &= squares - 1) {
      int pawnSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      // Fill attack table
      for (int j = 1; j < MoveGenerator.moveDeltaPawn.length; j++) {
        int delta = MoveGenerator.moveDeltaPawn[j];

        int targetSquare = pawnSquare;
        if (myColor == IntColor.WHITE) {
          targetSquare += delta;
        } else {
          assert myColor == IntColor.BLACK;

          targetSquare -= delta;
        }
        if ((targetSquare & 0x88) == 0) {
          myAttackTable[targetSquare]++;
          myAttackTable[targetSquare] |= BIT_PAWN;
        }
      }
    }
  }

  private void knightInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each knight
    for (long squares = board.knightList[myColor]; squares != 0; squares &= squares - 1) {
      int knightSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaKnight) {
        int targetSquare = knightSquare + delta;
        if ((targetSquare & 0x88) == 0) {
          myAttackTable[targetSquare]++;
          myAttackTable[targetSquare] |= BIT_MINOR;
        }
      }
    }
  }

  private void bishopInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each bishop
    for (long squares = board.bishopList[myColor]; squares != 0; squares &= squares - 1) {
      int bishopSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaBishop) {
        int targetSquare = bishopSquare + delta;
        while ((targetSquare & 0x88) == 0) {
          myAttackTable[targetSquare]++;
          myAttackTable[targetSquare] |= BIT_MINOR;

          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            targetSquare += delta;
          } else {
            break;
          }
        }
      }
    }
  }

  private void rookInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each rook
    for (long squares = board.rookList[myColor]; squares != 0; squares &= squares - 1) {
      int rookSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaRook) {
        int targetSquare = rookSquare + delta;
        while ((targetSquare & 0x88) == 0) {
          myAttackTable[targetSquare]++;
          myAttackTable[targetSquare] |= BIT_ROOK;

          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            targetSquare += delta;
          } else {
            break;
          }
        }
      }
    }
  }

  private void queenInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate the queen
    for (long squares = board.queenList[myColor]; squares != 0; squares &= squares - 1) {
      int queenSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaQueen) {
        int targetSquare = queenSquare + delta;
        while ((targetSquare & 0x88) == 0) {
          myAttackTable[targetSquare]++;
          myAttackTable[targetSquare] |= BIT_QUEEN;

          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            targetSquare += delta;
          } else {
            break;
          }
        }
      }
    }
  }

  private void kingInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate the king
    assert Long.bitCount(board.kingList[myColor]) == 1;
    int kingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[myColor]));

    // Fill attack table
    for (int delta : MoveGenerator.moveDeltaKing) {
      int targetSquare = kingSquare + delta;
      if ((targetSquare & 0x88) == 0) {
        myAttackTable[targetSquare]++;
        myAttackTable[targetSquare] |= BIT_KING;
      }
    }
  }

}
