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

import com.fluxchess.flux.board.*;
import com.fluxchess.flux.move.IntCastling;

public final class KingEvaluation {

  private static final byte MASK_ATTACKERS = 31;
  private static final int EVAL_KING_ATTACK = 40;

  // Our attack table implementing Idea of Ed Schr�der
  public static final byte[] KING_ATTACK_PATTERN = {
    // . P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P
    //     M M     M M     M M     M M     M M     M M     M M     M M
    //         R R R R         R R R R         R R R R         R R R R
    //                 Q Q Q Q Q Q Q Q                 Q Q Q Q Q Q Q Q
    //                                 K K K K K K K K K K K K K K K K
       0,0,1,1,2,2,3,3,3,3,4,4,5,5,6,6,0,0,1,1,2,2,3,3,3,3,4,4,5,5,6,8
  };
  private static final int[] KING_ATTACK_EVAL = {
    0, 0, 128, 192, 224, 240, 248, 252, 254, 255, 256, 256, 256, 256, 256, 256, 256, 256
  };

  private KingEvaluation() {
  }

  public static int evaluateKing(int myColor, int enemyColor, Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int opening = 0;
    int endgame = 0;
    byte[] myAttackTable = AttackTableEvaluation.getInstance().attackTable[myColor];
    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];

    // Evaluate the king
    assert board.kingList[myColor].size() == 1;
    int kingPosition = BitPieceList.next(board.kingList[myColor].list);

    // Evaluate king safety
    int attackedSquare = IntPosition.NOPOSITION;
    int attackCount = 0;
    byte flag = 0;

    int sign = 1;
    int castlingKingside = IntCastling.WHITE_KINGSIDE;
    int castlingQueenside = IntCastling.WHITE_QUEENSIDE;
    if (myColor == IntColor.BLACK) {
      sign = -1;
      castlingKingside = IntCastling.BLACK_KINGSIDE;
      castlingQueenside = IntCastling.BLACK_QUEENSIDE;
    } else {
      assert myColor == IntColor.WHITE;
    }
    attackedSquare = kingPosition + 1;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition - 1;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition - sign * 15;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition - sign * 16;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition - sign * 17;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition + sign * 17;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition + sign * 16;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }
    attackedSquare = kingPosition + sign * 15;
    if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
      attackCount += 4;
      flag |= enemyAttackTable[attackedSquare];
      int chessman = board.board[attackedSquare];
      if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
        attackCount += 3;
      }
      if (myAttackTable[attackedSquare] == -127) {
        attackCount += 1;
      }
    }

    attackCount /= 4;
    assert attackCount >= 0 && attackCount <= 16;
    assert KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] >= 0 && KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] <= 8;

    int kingSafety = (KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] * EVAL_KING_ATTACK * KING_ATTACK_EVAL[attackCount]) / 256;
    assert kingSafety >= 0 && kingSafety <= 8 * EVAL_KING_ATTACK;

    opening -= kingSafety;

    int castlingPositionKingside = IntPosition.WHITE_CASTLING_KINGSIDE;
    int castlingPositionQueenside = IntPosition.WHITE_CASTLING_QUEENSIDE;
    if (myColor == IntColor.BLACK) {
      castlingPositionKingside = IntPosition.BLACK_CASTLING_KINGSIDE;
      castlingPositionQueenside = IntPosition.BLACK_CASTLING_QUEENSIDE;
    } else {
      assert myColor == IntColor.WHITE;
    }

    // Evaluate pawn shield
    int positionPenalty = getPawnShieldPenalty(myColor, kingPosition);
    int castlingPenalty = positionPenalty;

    if ((board.castling & castlingKingside) != 0) {
      int tempPenalty = getPawnShieldPenalty(myColor, castlingPositionKingside);
      if (tempPenalty < castlingPenalty) {
        castlingPenalty = tempPenalty;
      }
    }
    if ((board.castling & castlingQueenside) != 0) {
      int tempPenalty = getPawnShieldPenalty(myColor, castlingPositionQueenside);
      if (tempPenalty < castlingPenalty) {
        castlingPenalty = tempPenalty;
      }
    }

    int pawnShieldPenalty = (positionPenalty + castlingPenalty) / 2;

    opening -= pawnShieldPenalty;

    return board.getGamePhaseEvaluation(myColor, opening, endgame);
  }

  private static int getPawnShieldPenalty(int myColor, int kingPosition) {
    assert myColor != IntColor.NOCOLOR;
    assert (kingPosition & 0x88) == 0;

    // Initialize
    byte[] myPawnTable = PawnTableEvaluation.getInstance().pawnTable[myColor];

    int kingFile = IntPosition.getFile(kingPosition);
    int kingRank = IntPosition.getRank(kingPosition);
    int tableFile = kingFile + 1;

    // Evaluate pawn shield
    int penalty = 0;
    if (myColor == IntColor.WHITE) {
      // Evaluate the file of the king
      if (myPawnTable[tableFile] == 0 || myPawnTable[tableFile] < kingRank) {
        penalty += 2 * 36;
      } else {
        penalty += 2 * (myPawnTable[tableFile] - 1) * (myPawnTable[tableFile] - 1);
      }
      // Evaluate the file at the left of the king
      if (kingFile != 0) {
        // We are not at the left border
        if (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] < kingRank) {
          penalty += 36;
        } else {
          penalty += (myPawnTable[tableFile - 1] - 1) * (myPawnTable[tableFile - 1] - 1);
        }
      }
      // Evaluate the file at the right of the king
      if (kingFile != 7) {
        // We are not at the right border
        if (myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] < kingRank) {
          penalty += 36;
        } else {
          penalty += (myPawnTable[tableFile + 1] - 1) * (myPawnTable[tableFile + 1] - 1);
        }
      }
    } else {
      assert myColor == IntColor.BLACK;

      // Evaluate the file of the king
      if (myPawnTable[tableFile] == 0 || myPawnTable[tableFile] > kingRank) {
        penalty += 2 * 36;
      } else {
        penalty += 2 * (7 - myPawnTable[tableFile] - 1) * (7 - myPawnTable[tableFile] - 1);
      }
      // Evaluate the file at the left of the king
      if (kingFile != 0) {
        // We are not at the left border
        if (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] > kingRank) {
          penalty += 36;
        } else {
          penalty += (7 - myPawnTable[tableFile - 1] - 1) * (7 - myPawnTable[tableFile - 1] - 1);
        }
      }
      // Evaluate the file at the right of the king
      if (kingFile != 7) {
        // We are not at the right border
        if (myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] > kingRank) {
          penalty += 36;
        } else {
          penalty += (7 - myPawnTable[tableFile + 1] - 1) * (7 - myPawnTable[tableFile + 1] - 1);
        }
      }
    }

    return penalty;
  }

}
