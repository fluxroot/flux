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

import com.fluxchess.flux.board.BitPieceList;
import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.move.MoveGenerator;

import java.util.Arrays;

public final class AttackTableEvaluation {

  public static final byte BIT_PAWN = 1 << 3;
  public static final byte BIT_MINOR = 1 << 4;
  public static final byte BIT_ROOK = 1 << 5;
  public static final byte BIT_QUEEN = 1 << 6;
  public static final byte BIT_KING = -128;

  private static final AttackTableEvaluation instance = new AttackTableEvaluation();

  // Our attack table
  public final byte[][] attackTable = new byte[IntColor.ARRAY_DIMENSION][Board.BOARDSIZE];

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
    for (long positions = board.pawnList[myColor].list; positions != 0; positions &= positions - 1) {
      int pawnPosition = BitPieceList.next(positions);

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
    for (long positions = board.knightList[myColor].list; positions != 0; positions &= positions - 1) {
      int knightPosition = BitPieceList.next(positions);

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
    for (long positions = board.bishopList[myColor].list; positions != 0; positions &= positions - 1) {
      int bishopPosition = BitPieceList.next(positions);

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaBishop) {
        int targetPosition = bishopPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_MINOR;

          int target = board.board[targetPosition];
          if (target == IntChessman.NOPIECE) {
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
    for (long positions = board.rookList[myColor].list; positions != 0; positions &= positions - 1) {
      int rookPosition = BitPieceList.next(positions);

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaRook) {
        int targetPosition = rookPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_ROOK;

          int target = board.board[targetPosition];
          if (target == IntChessman.NOPIECE) {
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
    for (long positions = board.queenList[myColor].list; positions != 0; positions &= positions - 1) {
      int queenPosition = BitPieceList.next(positions);

      // Fill attack table
      for (int delta : MoveGenerator.moveDeltaQueen) {
        int targetPosition = queenPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          myAttackTable[targetPosition]++;
          myAttackTable[targetPosition] |= BIT_QUEEN;

          int target = board.board[targetPosition];
          if (target == IntChessman.NOPIECE) {
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
    assert board.kingList[myColor].size() == 1;
    int kingPosition = BitPieceList.next(board.kingList[myColor].list);

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
