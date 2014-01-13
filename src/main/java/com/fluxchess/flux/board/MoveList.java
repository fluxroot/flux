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
import com.fluxchess.flux.search.Search;
import com.fluxchess.jcpi.models.IntPiece;

public final class MoveList {

  private static final int MAXSIZE = 4096;
  private static final int HISTORYSIZE = Search.MAX_HEIGHT + 1;

  public final int[] move = new int[MAXSIZE];
  public final int[] value = new int[MAXSIZE];
  public int head = 0;
  public int index = 0;
  public int tail = 0;

  private final int[] historyHead = new int[HISTORYSIZE];
  private final int[] historyIndex = new int[HISTORYSIZE];
  private final int[] historyTail = new int[HISTORYSIZE];
  private int historyCount = 0;

  public void newList() {
    assert historyCount < HISTORYSIZE;

    historyHead[historyCount] = head;
    historyIndex[historyCount] = index;
    historyTail[historyCount] = tail;
    historyCount++;

    head = tail;
    index = tail;
  }

  public void deleteList() {
    assert historyCount > 0;

    historyCount--;
    head = historyHead[historyCount];
    index = historyIndex[historyCount];
    tail = historyTail[historyCount];
  }

  public void clear() {
    tail = head;
    index = head;
  }

  public int size() {
    return tail - head;
  }

  /**
   * This is an implementation of the insertion sort.
   * <p/>
   * Note: Here insertionsort sorts the list in descending order!
   */
  public void sort() {
    int i;
    int j;
    int m;
    int v;

    int left = head;
    int right = tail - 1;

    for (i = left + 1; i <= right; i++) {
      m = move[i];
      v = value[i];
      j = i;
      while ((j > left) && (value[j - 1] < v)) {
        move[j] = move[j - 1];
        value[j] = value[j - 1];
        j--;
      }
      move[j] = m;
      value[j] = v;
    }
  }

  public void rateEvasion(int transpositionMove, int primaryKillerMove, int secondaryKillerMove, HistoryTable historyTable) {
    for (int i = head; i < tail; i++) {
      int m = move[i];

      if (m == transpositionMove) {
        value[i] = Integer.MAX_VALUE;
      } else if (Move.getTargetPiece(m) != IntPiece.NOPIECE) {
        value[i] = getMVVLVARating(m);
      } else if (m == primaryKillerMove) {
        value[i] = 0;
      } else if (m == secondaryKillerMove) {
        value[i] = -1;
      } else {
        // -2 because of the secondary killer move
        value[i] = historyTable.get(move[i]) - HistoryTable.MAX_HISTORYVALUE - 2;
      }
    }
  }

  /**
   * Rates the move list according to the history table.
   */
  public void rateFromHistory(HistoryTable historyTable) {
    for (int i = head; i < tail; i++) {
      value[i] = historyTable.get(move[i]);
    }
  }

  /**
   * Rates the move according to SEE.
   */
  public void rateFromSEE() {
    for (int i = head; i < tail; i++) {
      value[i] = MoveSee.seeMove(move[i], IntPiece.getColor(Move.getOriginPiece(move[i])));
    }
  }

  /**
   * Rates the move according to the MVV/LVA.
   */
  public void rateFromMVVLVA() {
    for (int i = head; i < tail; i++) {
      value[i] = getMVVLVARating(move[i]);
    }
  }

  /**
   * Rates the move according to the MVPD.
   */
  public void rateFromMVPD() {
    for (int i = head; i < tail; i++) {
      value[i] = getMVPDRating(move[i]);
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

    int chessman = IntPiece.getChessman(Move.getOriginPiece(move));

    value += Evaluation.VALUE_KING / Evaluation.getValueFromChessman(chessman);
    int target = Move.getTargetPiece(move);
    if (target != IntPiece.NOPIECE) {
      value += 10 * Evaluation.getValueFromPiece(target);
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

    int chessman = IntPiece.getChessman(Move.getOriginPiece(move));
    int target = Move.getTargetPiece(move);

    if (target != IntPiece.NOPIECE) {
      value += Evaluation.VALUE_KING * (Evaluation.getValueFromPiece(target) - Evaluation.getValueFromChessman(chessman));
      value += Evaluation.getValueFromPiece(target);
    } else {
      value -= Evaluation.getValueFromChessman(chessman);
    }

    return value;
  }

}
