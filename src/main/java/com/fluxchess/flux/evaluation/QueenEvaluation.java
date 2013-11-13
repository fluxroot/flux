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
import com.fluxchess.flux.move.MoveGenerator;

/**
 * QueenEvaluation
 */
public final class QueenEvaluation {

  public static int EVAL_QUEEN_MOBILITY_BASE = -13;
  public static int EVAL_QUEEN_MOBILITYFACTOR_OPENING = 1;
  public static int EVAL_QUEEN_MOBILITYFACTOR_ENDGAME = 2;
  public static int EVAL_QUEEN_SAFETY = 40;
  public static int EVAL_QUEEN_SEVENTHRANK_OPENING = 10;
  public static int EVAL_QUEEN_SEVENTHRANK_ENDGAME = 20;

  private QueenEvaluation() {
  }

  public static int evaluateQueen(int myColor, int enemyColor, Hex88Board board) {
    assert myColor != IntColor.NOCOLOR;
    assert enemyColor != IntColor.NOCOLOR;
    assert board != null;

    // Initialize
    int opening = 0;
    int endgame = 0;
    byte[] enemyAttackTable = AttackTableEvaluation.getInstance().attackTable[enemyColor];
    byte[] enemyPawnTable = PawnTableEvaluation.getInstance().pawnTable[enemyColor];
    PositionList myQueenList = board.queenList[myColor];

    // Evaluate the queen
    for (int i = 0; i < myQueenList.size; i++) {
      int queenPosition = myQueenList.position[i];
      int queenRank = IntPosition.getRank(queenPosition);

      int allMobility = EVAL_QUEEN_MOBILITY_BASE;

      // Evaluate mobility
      for (int delta : MoveGenerator.moveDeltaQueen) {
        int targetPosition = queenPosition + delta;
        while ((targetPosition & 0x88) == 0) {
          int target = board.board[targetPosition];
          if (target == IntChessman.NOPIECE) {
            allMobility++;
            targetPosition += delta;
          } else {
            if (IntChessman.getColor(target) == enemyColor) {
              allMobility++;
            }
            break;
          }
        }
      }

      // Evaluate mobility
      opening += EVAL_QUEEN_MOBILITYFACTOR_OPENING * allMobility;
      endgame += EVAL_QUEEN_MOBILITYFACTOR_ENDGAME * allMobility;

      // Evaluate safety
      if ((enemyAttackTable[queenPosition] & AttackTableEvaluation.BIT_PAWN) == 0
        && (enemyAttackTable[queenPosition] & AttackTableEvaluation.BIT_MINOR) == 0
        && (enemyAttackTable[queenPosition] & AttackTableEvaluation.BIT_ROOK) == 0) {
        opening += EVAL_QUEEN_SAFETY;
        endgame += EVAL_QUEEN_SAFETY;
      }

      // Evaluate 7th rank
      int seventhRank = 6;
      int eighthRank = 7;
      if (myColor == IntColor.BLACK) {
        seventhRank = 1;
        eighthRank = 0;
      } else {
        assert myColor == IntColor.WHITE;
      }
      if (queenRank == seventhRank) {
        int kingPosition = board.kingList[enemyColor].position[0];
        int kingRank = IntPosition.getRank(kingPosition);
        boolean enemyPawnExists = false;
        for (int j = 1; j < enemyPawnTable.length - 1; j++) {
          if (enemyPawnTable[j] == seventhRank) {
            enemyPawnExists = true;
            break;
          }
        }
        if (enemyPawnExists || kingRank == eighthRank) {
          opening += EVAL_QUEEN_SEVENTHRANK_OPENING;
          endgame += EVAL_QUEEN_SEVENTHRANK_ENDGAME;
        }
      }
    }

    return board.getGamePhaseEvaluation(myColor, opening, endgame);
  }

}
