/*
 * Copyright 2007-2013 the original author or authors.
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

import com.fluxchess.flux.board.*;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.MoveSee;

/**
 * PawnPasserEvaluation
 */
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

  public static int evaluatePawnPasser(int myColor, int enemyColor, Hex88Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int opening = 0;
    int endgame = 0;
    byte[] myAttackTable = AttackTableEvaluation.getInstance().attackTable[myColor];
    byte[] enemyPawnTable = PawnTableEvaluation.getInstance().pawnTable[enemyColor];
    PositionList myPawnList = board.pawnList[myColor];

    assert board.kingList[enemyColor].size == 1;
    int enemyKingPosition = board.kingList[enemyColor].position[0];
    int enemyKingFile = IntPosition.getFile(enemyKingPosition);
    int enemyKingRank = IntPosition.getRank(enemyKingPosition);
    assert board.kingList[myColor].size == 1;
    int myKingPosition = board.kingList[myColor].position[0];
    int myKingFile = IntPosition.getFile(myKingPosition);
    int myKingRank = IntPosition.getRank(myKingPosition);

    // Evaluate each pawn
    for (int i = 0; i < myPawnList.size; i++) {
      int pawnPosition = myPawnList.position[i];
      int pawnFile = IntPosition.getFile(pawnPosition);
      int pawnRank = IntPosition.getRank(pawnPosition);
      int pawn = board.board[pawnPosition];
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

          if ((myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_ROOK) != 0) {
            // We are protected by a rook
            // Check whether the rook is in front of us
            int endPosition = pawnPosition + 16;
            for (int j = pawnRank + 1; j <= 7; j++) {
              int chessman = board.board[endPosition];
              if (chessman != IntChessman.NOPIECE) {
                if (IntChessman.getChessman(chessman) == IntChessman.ROOK && IntChessman.getColor(chessman) == myColor) {
                  // We have no bad rook
                  isPasser = false;
                }
                break;
              }
              endPosition += 16;
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

          if ((myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_ROOK) != 0) {
            // We are protected by a rook
            // Check whether the rook is in front of us
            int endPosition = pawnPosition - 16;
            for (int j = pawnRank - 1; j >= 0; j--) {
              int chessman = board.board[endPosition];
              if (chessman != IntChessman.NOPIECE) {
                if (IntChessman.getChessman(chessman) == IntChessman.ROOK && IntChessman.getColor(chessman) == myColor) {
                  // We have no bad rook
                  isPasser = false;
                }
                break;
              }
              endPosition -= 16;
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

        if (board.materialCount[enemyColor] == 0) {
          // Unstoppable passer
          if (myColor == IntColor.WHITE) {
            // Is a friendly chessman blocking our promotion path?
            boolean pathClear = true;
            int endPosition = pawnPosition + 16;
            for (int j = pawnRank + 1; j <= 7; j++) {
              int chessman = board.board[endPosition];
              if (chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == myColor) {
                pathClear = false;
              }
              endPosition += 16;
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
              else if (IntPosition.getRelativeRank(myKingPosition, myColor) == IntPosition.rank7
                && ((promotionDistance <= 2 && (myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_KING) != 0)
                || (promotionDistance <= 3 && (myAttackTable[pawnPosition + 16] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
                && (myKingFile != pawnFile || (pawnFile != IntPosition.fileA && pawnFile != IntPosition.fileH))) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
              }
            }
          } else {
            assert myColor == IntColor.BLACK;

            // Is a friendly chessman blocking our promotion path?
            boolean pathClear = true;
            int endPosition = pawnPosition - 16;
            for (int j = pawnRank - 1; j >= 0; j--) {
              int chessman = board.board[endPosition];
              if (chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == myColor) {
                pathClear = false;
              }
              endPosition -= 16;
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
              else if (IntPosition.getRelativeRank(myKingPosition, myColor) == IntPosition.rank7
                && ((promotionDistance <= 2 && (myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_KING) != 0)
                || (promotionDistance <= 3 && (myAttackTable[pawnPosition - 16] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
                && (myKingFile != pawnFile || (pawnFile != IntPosition.fileA && pawnFile != IntPosition.fileH))) {
                endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
              }
            }
          }
        } else {
          // Free passer
          assert ((pawnPosition + sign * 16) & 0x88) == 0;
          if (board.board[pawnPosition + sign * 16] == IntChessman.NOPIECE) {
            // TODO: Do we have to consider promotion moves?
            int move = IntMove.createMove(IntMove.NORMAL, pawnPosition, pawnPosition + sign * 16, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
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

    return board.getGamePhaseEvaluation(myColor, opening, endgame);
  }

}
