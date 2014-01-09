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

public final class RookEvaluation {

  public static int EVAL_ROOK_MOBILITY_BASE = -7;
  public static int EVAL_ROOK_MOBILITYFACTOR_OPENING = 2;
  public static int EVAL_ROOK_MOBILITYFACTOR_ENDGAME = 4;
  public static int EVAL_ROOK_SAFETY = 20;
  public static int EVAL_ROOK_OPENFILE = 20;
  public static int EVAL_ROOK_NEARKINGFILE = 10;
  public static int EVAL_ROOK_SEVENTHRANK_OPENING = 20;
  public static int EVAL_ROOK_SEVENTHRANK_ENDGAME = 40;
  public static int EVAL_ROOK_SEVENTHRANK_BONUS = 10;

  private RookEvaluation() {
  }

  public static int evaluateRook(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int opening = 0;
    int endgame = 0;
    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];
    byte[] myPawnTable = PawnTableEvaluation.getInstance().pawnTable[myColor];
    byte[] enemyPawnTable = PawnTableEvaluation.getInstance().pawnTable[enemyColor];
    int totalRook7th = 0;

    // Evaluate each rook
    for (long positions = board.rookList[myColor]; positions != 0; positions &= positions - 1) {
      int rookPosition = Position.toX88Position(Long.numberOfTrailingZeros(positions));
      int rookFile = Position.getFile(rookPosition);
      int rookRank = Position.getRank(rookPosition);
      int tableFile = rookFile + 1;

      int allMobility = EVAL_ROOK_MOBILITY_BASE;

      // Evaluate mobility
      for (int delta : MoveGenerator.moveDeltaRook) {
        int targetPosition = rookPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          int target = board.board[targetPosition];
          if (target == IntPiece.NOPIECE) {
            allMobility++;
            targetPosition += delta;
          } else {
            if (IntPiece.getColor(target) == enemyColor) {
              allMobility++;
            }
            break;
          }
        }
      }

      // Evaluate mobility
      opening += EVAL_ROOK_MOBILITYFACTOR_OPENING * allMobility;
      endgame += EVAL_ROOK_MOBILITYFACTOR_ENDGAME * allMobility;

      // Evaluate safety
      if ((enemyAttackTable[rookPosition] & AttackTableEvaluation.BIT_PAWN) == 0
        && (enemyAttackTable[rookPosition] & AttackTableEvaluation.BIT_MINOR) == 0) {
        opening += EVAL_ROOK_SAFETY;
        endgame += EVAL_ROOK_SAFETY;
      }

      // Evaluate open file
      int totalOpenFile = 0;
      totalOpenFile -= EVAL_ROOK_OPENFILE / 2;
      if (myPawnTable[tableFile] == 0) {
        totalOpenFile += EVAL_ROOK_OPENFILE / 2;
        if (enemyPawnTable[tableFile] == 0) {
          totalOpenFile += EVAL_ROOK_OPENFILE / 2;
        }
        int kingPosition = Position.toX88Position(Long.numberOfTrailingZeros(board.kingList[enemyColor]));
        int kingFile = Position.getFile(kingPosition);
        int delta = Math.abs(kingFile - rookFile);
        if (delta <= 1) {
          opening += EVAL_ROOK_NEARKINGFILE;
          if (delta == 0) {
            opening += EVAL_ROOK_NEARKINGFILE;
          }
        }
      }
      opening += totalOpenFile;

      // Evaluate 7th rank
      int seventhRank = 6;
      int eighthRank = 7;
      if (myColor == IntColor.BLACK) {
        seventhRank = 1;
        eighthRank = 0;
      } else {
        assert myColor == IntColor.WHITE;
      }
      if (rookRank == seventhRank) {
        int kingPosition = Position.toX88Position(Long.numberOfTrailingZeros(board.kingList[enemyColor]));
        int kingRank = Position.getRank(kingPosition);
        boolean enemyPawnExists = false;
        for (int j = 1; j < enemyPawnTable.length - 1; j++) {
          if (enemyPawnTable[j] == seventhRank) {
            enemyPawnExists = true;
            break;
          }
        }
        if (enemyPawnExists || kingRank == eighthRank) {
          totalRook7th++;
          opening += EVAL_ROOK_SEVENTHRANK_OPENING;
          endgame += EVAL_ROOK_SEVENTHRANK_ENDGAME;
        }
      }
    }

    // Check whether we have both rooks on the 7th rank
    if (totalRook7th == 2) {
      opening += EVAL_ROOK_SEVENTHRANK_BONUS;
      endgame += EVAL_ROOK_SEVENTHRANK_BONUS;
    }

    return Evaluation.getGamePhaseEvaluation(myColor, opening, endgame, board);
  }

}
