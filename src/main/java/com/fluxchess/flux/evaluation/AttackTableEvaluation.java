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
import com.fluxchess.flux.board.Position;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

import java.util.Arrays;

public final class AttackTableEvaluation {

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
    for (long positions = board.pawnList[myColor]; positions != 0; positions &= positions - 1) {
      int pawnPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));

      // Fill attack table
      for (int j = 1; j < MoveGenerator.moveDeltaPawn.length; j++) {
        int delta = MoveGenerator.moveDeltaPawn[j];

        int targetPosition = pawnPosition;
        if (myColor == IntColor.WHITE) {
          targetPosition += delta;
        } else {
          assert myColor == IntColor.BLACK;

          targetPosition -= delta;
        }
        if ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_PAWN;
        }
      }
    }
  }

  private void knightInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each knight
    for (long positions = board.knightList[myColor]; positions != 0; positions &= positions - 1) {
      int knightPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaKnight) {
        int targetPosition = knightPosition + delta;
        if ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_MINOR;
        }
      }
    }
  }

  private void bishopInformationToAttackTable(int myColor, Board board, byte[] myAttackTable) {
    assert myColor != IntColor.NOCOLOR;
    assert board != null;
    assert myAttackTable != null;

    // Evaluate each bishop
    for (long positions = board.bishopList[myColor]; positions != 0; positions &= positions - 1) {
      int bishopPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaBishop) {
        int targetPosition = bishopPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_MINOR;

          int target = board.board[targetPosition];
          if (target == IntPiece.NOPIECE) {
            targetPosition += delta;
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
    for (long positions = board.rookList[myColor]; positions != 0; positions &= positions - 1) {
      int rookPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaRook) {
        int targetPosition = rookPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_ROOK;

          int target = board.board[targetPosition];
          if (target == IntPiece.NOPIECE) {
            targetPosition += delta;
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
    for (long positions = board.queenList[myColor]; positions != 0; positions &= positions - 1) {
      int queenPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaQueen) {
        int targetPosition = queenPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_QUEEN;

          int target = board.board[targetPosition];
          if (target == IntPiece.NOPIECE) {
            targetPosition += delta;
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
    int kingPosition = Position.toX88Position(Long.numberOfTrailingZeros(board.kingList[myColor]));

    // Fill attack table
    for (int delta : MoveGenerator.moveDeltaKing) {
      int targetPosition = kingPosition + delta;
      if ((targetPosition & 0x88) == 0) {
        myAttackTable[targetPosition]++;
        myAttackTable[targetPosition] |= BIT_KING;
      }
    }
  }

}
