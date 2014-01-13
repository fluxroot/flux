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
import com.fluxchess.jcpi.models.IntFile;
import com.fluxchess.jcpi.models.IntPiece;
import com.fluxchess.jcpi.models.IntRank;

final class DrawEvaluation {

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

      assert Long.bitCount(board.kingList[myColor]) != 0;
      assert Long.bitCount(board.kingList[enemyColor]) != 0;

      int myMaterial = PAWN_VALUE * Long.bitCount(board.pawnList[myColor])
        + KNIGHT_VALUE * Long.bitCount(board.knightList[myColor])
        + BISHOP_VALUE * Long.bitCount(board.bishopList[myColor])
        + ROOK_VALUE * Long.bitCount(board.rookList[myColor])
        + QUEEN_VALUE * Long.bitCount(board.queenList[myColor]);

      int enemyMaterial = PAWN_VALUE * Long.bitCount(board.pawnList[enemyColor])
        + KNIGHT_VALUE * Long.bitCount(board.knightList[enemyColor])
        + BISHOP_VALUE * Long.bitCount(board.bishopList[enemyColor])
        + ROOK_VALUE * Long.bitCount(board.rookList[enemyColor])
        + QUEEN_VALUE * Long.bitCount(board.queenList[enemyColor]);

      if (myMaterial == 0) {
        if (enemyMaterial == 0) {
          // KK: insufficient material
          assert Evaluation.materialCountAll(myColor, board) == 0;
          assert Evaluation.materialCountAll(enemyColor, board) == 0;
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
          if (Square.getFieldColor(Square.toX88Square(Long.numberOfTrailingZeros(board.bishopList[myColor])))
            == Square.getFieldColor(Square.toX88Square(Long.numberOfTrailingZeros(board.bishopList[enemyColor])))) {
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
        if (Square.getFieldColor(Square.toX88Square(Long.numberOfTrailingZeros(board.bishopList[myColor])))
          == Square.getFieldColor(Square.toX88Square(Long.numberOfTrailingZeros(board.bishopList[enemyColor])))) {
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

    assert Long.bitCount(board.pawnList[myColor]) == 1;
    int pawnSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.pawnList[myColor]));
    int pawnFile = Square.getFile(pawnSquare);
    int pawnRank = Square.getRank(pawnSquare);
    assert Long.bitCount(board.kingList[enemyColor]) == 1;
    int enemyKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[enemyColor]));
    int enemyKingFile = Square.getFile(enemyKingSquare);
    int enemyKingRank = Square.getRank(enemyKingSquare);
    assert Long.bitCount(board.kingList[myColor]) == 1;
    int myKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[myColor]));
    int myKingFile = Square.getFile(myKingSquare);
    int myKingRank = Square.getRank(myKingSquare);

    int myKingPromotionDistance;
    int enemyKingPromotionDistance;
    if (myColor == IntColor.WHITE) {
      myKingPromotionDistance = Math.max(Math.abs(pawnFile - myKingFile), IntRank.R8 - myKingRank);
      enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), IntRank.R8 - enemyKingRank);
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

        promotionDistance = IntRank.R8 - pawnRank;
        if (pawnRank == IntRank.R2) {
          // We can do a pawn double move
          promotionDistance--;
        }
      } else {
        delta = -16;

        promotionDistance = pawnRank;
        if (pawnRank == IntRank.R7) {
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
      else if (Square.getRelativeRank(myKingSquare, myColor) == IntRank.R7
        && ((promotionDistance <= 2 && (myAttackTable[pawnSquare] & AttackTableEvaluation.BIT_KING) != 0)
        || (promotionDistance <= 3 && (myAttackTable[pawnSquare + delta] & AttackTableEvaluation.BIT_KING) != 0 && board.activeColor == myColor))
        && (myKingFile != pawnFile || (pawnFile != IntFile.Fa && pawnFile != IntFile.Fh))) {
        unstoppablePasser = true;
      }
    }

    if (!unstoppablePasser) {
      if (pawnFile == IntFile.Fa || pawnFile == IntFile.Fh) {
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
    int targetSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.pawnList[myColor])) + delta;
    while ((targetSquare & 0x88) == 0) {
      int chessman = board.board[targetSquare];
      if ((chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == enemyColor) || (enemyAttackTable[targetSquare] & AttackTableEvaluation.BIT_MINOR) != 0) {
        return 1;
      } else {
        targetSquare += delta;
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
    int targetSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.pawnList[myColor])) + delta;
    while ((targetSquare & 0x88) == 0) {
      int chessman = board.board[targetSquare];
      if ((chessman != IntPiece.NOPIECE && IntPiece.getColor(chessman) == enemyColor) || (enemyAttackTable[targetSquare] & AttackTableEvaluation.BIT_MINOR) != 0) {
        return 1;
      } else {
        targetSquare += delta;
      }
    }

    return DRAW_FACTOR;
  }

}
