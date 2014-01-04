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
import com.fluxchess.flux.board.ChessmanList;
import com.fluxchess.flux.board.Position;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

public final class DrawEvaluation {

  // Draw values
  public static final int DRAW_FACTOR = 16;

  private static final int PAWN_VALUE = 1;
  private static final int KNIGHT_VALUE = PAWN_VALUE * 11;
  private static final int BISHOP_VALUE = KNIGHT_VALUE * 11;
  private static final int ROOK_VALUE = BISHOP_VALUE * 11;
  private static final int QUEEN_VALUE = ROOK_VALUE * 11;

  private DrawEvaluation() {
  }

  public static int evaluateDraw(Board board) {
    assert board != null;

    for (int myColor : IntColor.values) {
      int enemyColor = IntColor.opposite(myColor);

      assert board.kingList[myColor].size() != 0;
      assert board.kingList[enemyColor].size() != 0;

      int myMaterial = PAWN_VALUE * board.pawnList[myColor].size()
        + KNIGHT_VALUE * board.knightList[myColor].size()
        + BISHOP_VALUE * board.bishopList[myColor].size()
        + ROOK_VALUE * board.rookList[myColor].size()
        + QUEEN_VALUE * board.queenList[myColor].size();

      int enemyMaterial = PAWN_VALUE * board.pawnList[enemyColor].size()
        + KNIGHT_VALUE * board.knightList[enemyColor].size()
        + BISHOP_VALUE * board.bishopList[enemyColor].size()
        + ROOK_VALUE * board.rookList[enemyColor].size()
        + QUEEN_VALUE * board.queenList[enemyColor].size();

      if (myMaterial == 0) {
        if (enemyMaterial == 0) {
          // KK: insufficient material
          assert board.materialCountAll[myColor] == 0;
          assert board.materialCountAll[enemyColor] == 0;
          return 0;
        } else if (enemyMaterial == PAWN_VALUE) {
          // KKP
          return evaluateDrawKPK(enemyColor, myColor, board);
        } else if (enemyMaterial == KNIGHT_VALUE) {
          // KKN: insufficient material
          return 0;
        } else if (enemyMaterial == KNIGHT_VALUE * 2) {
          // KKNN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE) {
          // KKB: insufficient material
          return 0;
        }
      } else if (myMaterial == PAWN_VALUE) {
        if (enemyMaterial == KNIGHT_VALUE) {
          // KPKN
          return evaluateDrawKPKN(myColor, enemyColor, board);
        } else if (enemyMaterial == BISHOP_VALUE) {
          // KPKB
          return evaluateDrawKPKB(myColor, enemyColor, board);
        }
      } else if (myMaterial == KNIGHT_VALUE) {
        if (enemyMaterial == KNIGHT_VALUE) {
          // KNKN
          return 0;
        } else if (enemyMaterial == KNIGHT_VALUE * 2) {
          // KNKNN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE) {
          // KNKB
          return 0;
        } else if (enemyMaterial == ROOK_VALUE) {
          // KNKR
          return 0;
        }
      } else if (myMaterial == KNIGHT_VALUE * 2 && enemyMaterial == ROOK_VALUE + KNIGHT_VALUE) {
        // KNNKRN
        return 0;
      } else if (myMaterial == BISHOP_VALUE) {
        if (enemyMaterial == KNIGHT_VALUE * 2) {
          // KBKNN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE) {
          // KBKB: insufficient material
          if (Position.getFieldColor(ChessmanList.next(board.bishopList[myColor].list))
            == Position.getFieldColor(ChessmanList.next(board.bishopList[enemyColor].list))) {
            return 0;
          }
        } else if (enemyMaterial == BISHOP_VALUE + KNIGHT_VALUE) {
          // KBKBN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE * 2) {
          // KBKBB
          return 0;
        } else if (enemyMaterial == ROOK_VALUE) {
          // KBKR
          return 0;
        }
      } else if (myMaterial == BISHOP_VALUE + KNIGHT_VALUE && enemyMaterial == ROOK_VALUE + BISHOP_VALUE) {
        // KBNKRB
        if (Position.getFieldColor(ChessmanList.next(board.bishopList[myColor].list))
          == Position.getFieldColor(ChessmanList.next(board.bishopList[enemyColor].list))) {
          return 0;
        }
      } else if (myMaterial == ROOK_VALUE) {
        if (enemyMaterial == KNIGHT_VALUE * 2) {
          // KRKNN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE + KNIGHT_VALUE) {
          // KRKBN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE * 2) {
          // KRKBB
          return 0;
        } else if (enemyMaterial == ROOK_VALUE) {
          // KRKR
          return 0;
        } else if (enemyMaterial == ROOK_VALUE + KNIGHT_VALUE) {
          // KRKRN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE + KNIGHT_VALUE * 2) {
          // KRKBNN
          return 0;
        }
      } else if (myMaterial == ROOK_VALUE * 2) {
        if (enemyMaterial == BISHOP_VALUE + KNIGHT_VALUE * 2) {
          // KRRKBNN
          return 0;
        } else if (enemyMaterial == BISHOP_VALUE * 2 + KNIGHT_VALUE) {
          // KRRKBBN
          return 0;
        }
      } else if (myMaterial == QUEEN_VALUE) {
        if (enemyMaterial == ROOK_VALUE + KNIGHT_VALUE) {
          // KQKRN
          return 0;
        } else if (enemyMaterial == ROOK_VALUE + BISHOP_VALUE) {
          // KQKRB
          return 0;
        } else if (enemyMaterial == ROOK_VALUE + BISHOP_VALUE + KNIGHT_VALUE) {
          // KQKRBN
          return 0;
        } else if (enemyMaterial == ROOK_VALUE * 2) {
          // KQKRR
          return 0;
        } else if (enemyMaterial == QUEEN_VALUE) {
          // KQKQ
          return 0;
        }
      } else if (myMaterial == QUEEN_VALUE + KNIGHT_VALUE && enemyMaterial == ROOK_VALUE * 2) {
        // KQNKRR
        return 0;
      }
    }

    return DRAW_FACTOR;
  }

  private static int evaluateDrawKPK(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    byte[] myAttackTable = AttackTableEvaluation.getInstance().attackTable[myColor];

    assert board.pawnList[myColor].size() == 1;
    int pawnPosition = ChessmanList.next(board.pawnList[myColor].list);
    int pawnFile = Position.getFile(pawnPosition);
    int pawnRank = Position.getRank(pawnPosition);
    assert board.kingList[enemyColor].size() == 1;
    int enemyKingPosition = ChessmanList.next(board.kingList[enemyColor].list);
    int enemyKingFile = Position.getFile(enemyKingPosition);
    int enemyKingRank = Position.getRank(enemyKingPosition);
    assert board.kingList[myColor].size() == 1;
    int myKingPosition = ChessmanList.next(board.kingList[myColor].list);
    int myKingFile = Position.getFile(myKingPosition);
    int myKingRank = Position.getRank(myKingPosition);

    int myKingPromotionDistance;
    int enemyKingPromotionDistance;
    if (myColor == IntColor.WHITE) {
      myKingPromotionDistance = Math.max(Math.abs(pawnFile - myKingFile), Position.rank8 - myKingRank);
      enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), Position.rank8 - enemyKingRank);
    } else {
      assert myColor == IntColor.BLACK;

      myKingPromotionDistance = Math.max(Math.abs(pawnFile - myKingFile), myKingRank);
      enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), enemyKingRank);
    }
    // Unstoppable passer
    boolean unstoppablePasser = false;
    if (myKingFile != pawnFile) {
      int delta;
      int promotionDistance;
      int difference;

      if (myColor == IntColor.WHITE) {
        delta = 16;

        promotionDistance = Position.rank8 - pawnRank;
        if (pawnRank == Position.rank2) {
          // We can do a pawn double move
          promotionDistance--;
        }
      } else {
        assert myColor == IntColor.BLACK;

        delta = -16;

        promotionDistance = pawnRank;
        if (pawnRank == Position.rank7) {
          // We can do a pawn double move
          promotionDistance--;
        }
      }

      difference = enemyKingPromotionDistance - promotionDistance;

      if (board.activeColor == enemyColor) {
        difference--;
      }

      if (difference >= 1) {
        unstoppablePasser = true;
      }

      // King protected passer
      else if (Position.getRelativeRank(myKingPosition, myColor) == Position.rank7
        && ((promotionDistance <= 2 && (myAttackTable[pawnPosition] & AttackTableEvaluation.BIT_KING) != 0)
        || (promotionDistance <= 3 && (myAttackTable[pawnPosition + delta] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
        && (myKingFile != pawnFile || (pawnFile != Position.fileA && pawnFile != Position.fileH))) {
        unstoppablePasser = true;
      }
    }

    if (!unstoppablePasser) {
      if (pawnFile == Position.fileA || pawnFile == Position.fileH) {
        int difference = enemyKingPromotionDistance - myKingPromotionDistance;
        if (board.activeColor == enemyColor) {
          difference--;
        }

        if (difference < 1) {
          // The enemy king can reach the corner.
          return 0;
        }
      } else {
        boolean enemyKingInFrontPawn = false;
        if (myColor == IntColor.WHITE) {
          enemyKingInFrontPawn = pawnRank < enemyKingRank;
        } else {
          assert myColor == IntColor.BLACK;

          enemyKingInFrontPawn = pawnRank > enemyKingRank;
        }
        if (enemyKingInFrontPawn
          && Math.abs(myKingRank - enemyKingRank) >= 2
          && !(Math.abs(myKingRank - enemyKingRank) == 2 && myKingFile == enemyKingFile && board.activeColor == enemyColor)) {
          return 0;
        }
      }
    }

    return DRAW_FACTOR;
  }

  private static int evaluateDrawKPKN(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];

    // Check the promotion path
    int delta = 16;
    if (myColor == IntColor.BLACK) {
      delta = -16;
    } else {
      assert myColor == IntColor.WHITE;
    }
    int end = ChessmanList.next(board.pawnList[myColor].list) + delta;
    while ((end & 0x88) == 0) {
      int chessman = board.board[end];
      if ((chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & AttackTableEvaluation.BIT_MINOR) != 0) {
        return 1;
      } else {
        end += delta;
      }
    }

    return DRAW_FACTOR;
  }

  private static int evaluateDrawKPKB(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];

    // Check the promotion path
    int delta = 16;
    if (myColor == IntColor.BLACK) {
      delta = -16;
    } else {
      assert myColor == IntColor.WHITE;
    }
    int end = ChessmanList.next(board.pawnList[myColor].list) + delta;
    while ((end & 0x88) == 0) {
      int chessman = board.board[end];
      if ((chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & AttackTableEvaluation.BIT_MINOR) != 0) {
        return 1;
      } else {
        end += delta;
      }
    }

    return DRAW_FACTOR;
  }

}
