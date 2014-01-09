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
import com.fluxchess.flux.board.Move;
import com.fluxchess.flux.board.MoveSee;
import com.fluxchess.flux.board.Square;
import com.fluxchess.jcpi.models.*;

public final class PawnPasserEvaluation {

  private static final int EVAL_PAWN_PASSER_MAXBONUS = 6 * 6 * 6;
  private static final int EVAL_PAWN_PASSER_OPENING_MIN = 10;
  private static final int EVAL_PAWN_PASSER_OPENING_MAX = 70;
  public static int EVAL_PAWN_PASSER_ENDGAME_MIN = 20;
  public static int EVAL_PAWN_PASSER_ENDGAME_MAX = 140;
  private static final int EVAL_PAWN_PASSER_FREE = 60;
  private static final int EVAL_PAWN_PASSER_UNSTOPPABLE = 800;
  private static final int EVAL_PAWN_MYKING_DISTANCE = 5;
  private static final int EVAL_PAWN_ENEMYKING_DISTANCE = 20;

  private PawnPasserEvaluation() {
  }

  public static int evaluatePawnPasser(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int opening = 0;
    int endgame = 0;
    byte[] myAttackTable = AttackTableEvaluation.getInstance().attackTable[myColor];
    byte[] enemyPawnTable = PawnTableEvaluation.getInstance().pawnTable[enemyColor];

    assert Long.bitCount(board.kingList[enemyColor]) == 1;
    int enemyKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[enemyColor]));
    int enemyKingFile = Square.getFile(enemyKingSquare);
    int enemyKingRank = Square.getRank(enemyKingSquare);
    assert Long.bitCount(board.kingList[myColor]) == 1;
    int myKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[myColor]));
    int myKingFile = Square.getFile(myKingSquare);
    int myKingRank = Square.getRank(myKingSquare);

    // Evaluate each pawn
    for (long squares = board.pawnList[myColor]; squares != 0; squares &= squares - 1) {
      int pawnSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      int pawnFile = Square.getFile(pawnSquare);
      int pawnRank = Square.getRank(pawnSquare);
      int pawn = board.board[pawnSquare];
      int tableFile = pawnFile + 1;

      // Passed pawn
      boolean isPasser = false;
      int sign = 1;
      int rankBonus = pawnRank;
      if (myColor == IntColor.WHITE) {
        if ((enemyPawnTable[tableFile] == 0 || enemyPawnTable[tableFile] < pawnRank)
          && (enemyPawnTable[tableFile + 1] == 0 || enemyPawnTable[tableFile + 1] <= pawnRank)
          && (enemyPawnTable[tableFile - 1] == 0 || enemyPawnTable[tableFile - 1] <= pawnRank)) {
          isPasser = true;

          if ((myAttackTable[pawnSquare] & AttackTableEvaluation.BIT_ROOK) != 0) {
            // We are protected by a rook
            // Check whether the rook is in front of us
            int endSquare = pawnSquare + 16;
            for (int j = pawnRank + 1; j <= 7; j++) {
              int chessman = board.board[endSquare];
              if (chessman != IntPiece.NOPIECE) {
                if (IntPiece.getChessman(chessman) == IntChessman.ROOK && IntPiece.getColor(chessman) == myColor) {
                  // We have no bad rook
                  isPasser = false;
                }
                break;
              }
              endSquare += 16;
            }
          }
        }
      } else {
        assert myColor == IntColor.BLACK;

        if ((enemyPawnTable[tableFile] == 0 || enemyPawnTable[tableFile] > pawnRank)
          && (enemyPawnTable[tableFile + 1] == 0 || enemyPawnTable[tableFile + 1] >= pawnRank)
          && (enemyPawnTable[tableFile - 1] == 0 || enemyPawnTable[tableFile - 1] >= pawnRank)) {
          isPasser = true;
          sign = -1;
          rankBonus = 7 - pawnRank;

          if ((myAttackTable[pawnSquare] & AttackTableEvaluation.BIT_ROOK) != 0) {
            // We are protected by a rook
            // Check whether the rook is in front of us
            int targetSquare = pawnSquare - 16;
            for (int j = pawnRank - 1; j >= 0; j--) {
              int chessman = board.board[targetSquare];
              if (chessman != IntPiece.NOPIECE) {
                if (IntPiece.getChessman(chessman) == IntChessman.ROOK && IntPiece.getColor(chessman) == myColor) {
                  // We have no bad rook
                  isPasser = false;
                }
                break;
              }
              targetSquare -= 16;
            }
          }
        }
      }
      if (isPasser) {
        int bonus = 0;
        if (rankBonus >= 3) {
          bonus = rankBonus * rankBonus * rankBonus;
        }

        // Evaluate opening value
        opening += EVAL_PAWN_PASSER_OPENING_MIN + ((EVAL_PAWN_PASSER_OPENING_MAX - EVAL_PAWN_PASSER_OPENING_MIN) * bonus) / EVAL_PAWN_PASSER_MAXBONUS;

        // Evaluate endgame value
        int endgameMax = EVAL_PAWN_PASSER_ENDGAME_MAX;

        // King distance
        int myKingDistance = Math.max(Math.abs(pawnFile - myKingFile), Math.abs(pawnRank - myKingRank));
        int enemyKingDistance = Math.max(Math.abs(pawnFile - enemyKingFile), Math.abs(pawnRank - enemyKingRank));
        endgameMax -= myKingDistance * EVAL_PAWN_MYKING_DISTANCE;
        endgameMax += enemyKingDistance * EVAL_PAWN_ENEMYKING_DISTANCE;

        if (Evaluation.materialCount(enemyColor, board) == 0) {
          // Unstoppable passer
          if (myColor == IntColor.WHITE) {
            // Is a friendly chessman blocking our promotion path?
            boolean pathClear = true;
            int targetSquare = pawnSquare + 16;
            for (int j = pawnRank + 1; j <= 7; j++) {
              int chessman = board.board[targetSquare];
              if (chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == myColor) {
                pathClear = false;
              }
              targetSquare += 16;
            }

            if (pathClear) {
              int promotionDistance = 7 - pawnRank;
              if (pawnRank == 1) {
                // We can do a pawn double move
                promotionDistance--;
              }

              int enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), 7 - enemyKingRank);

              int difference = enemyKingPromotionDistance - promotionDistance;
              if (board.activeColor == enemyColor) {
                difference--;
              }
              if (difference >= 1) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE + 7 - promotionDistance;
              }

              // King protected passer
              else if (Square.getRelativeRank(myKingSquare, myColor) == IntRank.R7
                && ((promotionDistance <= 2 && (myAttackTable[pawnSquare] & AttackTableEvaluation.BIT_KING) != 0)
                || (promotionDistance <= 3 && (myAttackTable[pawnSquare + 16] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
                && (myKingFile != pawnFile || (pawnFile != IntFile.Fa && pawnFile != IntFile.Fh))) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
              }
            }
          } else {
            // Is a friendly chessman blocking our promotion path?
            boolean pathClear = true;
            int targetSquare = pawnSquare - 16;
            for (int j = pawnRank - 1; j >= 0; j--) {
              int chessman = board.board[targetSquare];
              if (chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == myColor) {
                pathClear = false;
              }
              targetSquare -= 16;
            }

            if (pathClear) {
              int promotionDistance = pawnRank;
              if (pawnRank == 6) {
                // We can do a pawn double move
                promotionDistance--;
              }

              int enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), enemyKingRank);

              int difference = enemyKingPromotionDistance - promotionDistance;
              if (board.activeColor == enemyColor) {
                difference--;
              }
              if (difference >= 1) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE + 7 - promotionDistance;
              }

              // King protected passer
              else if (Square.getRelativeRank(myKingSquare, myColor) == IntRank.R7
                && ((promotionDistance <= 2 && (myAttackTable[pawnSquare] & AttackTableEvaluation.BIT_KING) != 0)
                || (promotionDistance <= 3 && (myAttackTable[pawnSquare - 16] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
                && (myKingFile != pawnFile || (pawnFile != IntFile.Fa && pawnFile != IntFile.Fh))) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
              }
            }
          }
        } else {
          // Free passer
          assert ((pawnSquare + sign * 16) & 0x88) == 0;
          if (board.board[pawnSquare + sign * 16] == IntPiece.NOPIECE) {
            // TODO: Do we have to consider promotion moves?
            int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, pawnSquare + sign * 16, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
            if (MoveSee.seeMove(move, myColor) >= 0) {
              endgameMax += EVAL_PAWN_PASSER_FREE;
            }
          }
        }

        // Evaluate endgame value
        endgame += EVAL_PAWN_PASSER_ENDGAME_MIN;
        if (endgameMax - EVAL_PAWN_PASSER_ENDGAME_MIN > 0) {
          endgame += ((endgameMax - EVAL_PAWN_PASSER_ENDGAME_MIN) * bonus) / EVAL_PAWN_PASSER_MAXBONUS;
        }
      }
    }

    return Evaluation.getGamePhaseEvaluation(myColor, opening, endgame, board);
  }

}
