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
package com.fluxchess.flux.board;

import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.jcpi.models.IntChessman;

public final class MoveRater {

  private final HistoryTable historyTable;

  public MoveRater(HistoryTable historyTable) {
    this.historyTable = historyTable;
  }

  public void rateEvasion(MoveList moveList, int transpositionMove, int primaryKillerMove, int secondaryKillerMove) {
    for (int i = moveList.head; i < moveList.tail; i++) {
      int move = moveList.move[i];

      if (move == transpositionMove) {
        moveList.value[i] = Integer.MAX_VALUE;
      } else if (Move.getTargetChessman(move) != IntChessman.NOCHESSMAN) {
        moveList.value[i] = getMVVLVARating(move);
      } else if (move == primaryKillerMove) {
        moveList.value[i] = 0;
      } else if (move == secondaryKillerMove) {
        moveList.value[i] = -1;
      } else {
        // -2 because of the secondary killer move
        moveList.value[i] = historyTable.get(moveList.move[i]) - HistoryTable.MAX_HISTORYVALUE - 2;
      }
    }
  }

  /**
   * Rates the move list according to the history table.
   *
   * @param moveList the move list.
   */
  public void rateFromHistory(MoveList moveList) {
    for (int i = moveList.head; i < moveList.tail; i++) {
      moveList.value[i] = historyTable.get(moveList.move[i]);
    }
  }

  /**
   * Rates the move according to SEE.
   *
   * @param moveList the move list.
   */
  public void rateFromSEE(MoveList moveList) {
    for (int i = moveList.head; i < moveList.tail; i++) {
      moveList.value[i] = MoveSee.seeMove(moveList.move[i], Move.getOriginColor(moveList.move[i]));
    }
  }

  /**
   * Rates the move according to the MVV/LVA.
   *
   * @param moveList the move list.
   */
  public void rateFromMVVLVA(MoveList moveList) {
    for (int i = moveList.head; i < moveList.tail; i++) {
      moveList.value[i] = getMVVLVARating(moveList.move[i]);
    }
  }

  /**
   * Rates the move according to the MVPD.
   *
   * @param moveList the move list.
   */
  public void rateFromMVPD(MoveList moveList) {
    for (int i = moveList.head; i < moveList.tail; i++) {
      moveList.value[i] = getMVPDRating(moveList.move[i]);
    }
  }

  /**
   * Rates the move according to the MVV/LVA.
   *
   * @param move the move.
   * @return the MVV/LVA value.
   */
  private int getMVVLVARating(int move) {
    int value = 0;

    int chessman = Move.getOriginChessman(move);
    int target = Move.getTargetChessman(move);

    value += Evaluation.VALUE_KING / Evaluation.getValueFromChessman(chessman);
    if (target != IntChessman.NOCHESSMAN) {
      value += 10 * Evaluation.getValueFromChessman(target);
    }

    assert value >= (Evaluation.VALUE_KING / Evaluation.VALUE_KING) && value <= (Evaluation.VALUE_KING / Evaluation.VALUE_PAWN) + 10 * Evaluation.VALUE_QUEEN;

    return value;
  }

  /**
   * Rates the move according to the MVD (Most valuable piece difference).
   *
   * @param move the move.
   * @return the MVPD value.
   */
  private int getMVPDRating(int move) {
    int value = 0;

    int chessman = Move.getOriginChessman(move);
    int target = Move.getTargetChessman(move);

    if (target != IntChessman.NOCHESSMAN) {
      value += Evaluation.VALUE_KING * (Evaluation.getValueFromChessman(target) - Evaluation.getValueFromChessman(chessman));
      value += Evaluation.getValueFromChessman(target);
    } else {
      value -= Evaluation.getValueFromChessman(chessman);
    }

    return value;
  }

}
