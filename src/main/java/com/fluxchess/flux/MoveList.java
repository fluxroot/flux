/*
 * Copyright (C) 2007-2014 Phokham Nonava
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
package com.fluxchess.flux;

final class MoveList {

  private static final int MAXSIZE = 4096;
  private static final int HISTORYSIZE = Depth.MAX_PLY + 1;

  final int[] moves = new int[MAXSIZE];
  final int[] values = new int[MAXSIZE];
  int head = 0;
  int index = 0;
  int tail = 0;

  private final int[] historyHead = new int[HISTORYSIZE];
  private final int[] historyIndex = new int[HISTORYSIZE];
  private final int[] historyTail = new int[HISTORYSIZE];
  private int historyCount = 0;

  MoveList() {
  }

  void newList() {
    assert this.historyCount < HISTORYSIZE;

    this.historyHead[this.historyCount] = this.head;
    this.historyIndex[this.historyCount] = this.index;
    this.historyTail[this.historyCount] = this.tail;
    this.historyCount++;

    this.head = this.tail;
    this.index = this.tail;
  }

  void deleteList() {
    assert this.historyCount > 0;

    this.historyCount--;
    this.head = this.historyHead[this.historyCount];
    this.index = this.historyIndex[this.historyCount];
    this.tail = this.historyTail[this.historyCount];
  }

  void resetList() {
    this.tail = this.head;
    this.index = this.head;
  }

  int getLength() {
    return this.tail - this.head;
  }

  /**
   * Sorts the MoveList using insertion sort.
   *
   */
  void sort() {
    this.insertionsort(head, tail - 1);
  }

  /**
   * This is an implementation of the insertion sort.
   * <p/>
   * Note: Here insertionsort sorts the list in descending order!
   *
   * @param left  the left/lower index.
   * @param right the right/higher index.
   */
  private void insertionsort(int left, int right) {
    int i;
    int j;
    int move;
    int value;

    for (i = left + 1; i <= right; i++) {
      move = moves[i];
      value = values[i];
      j = i;
      while ((j > left) && (values[j - 1] < value)) {
        moves[j] = moves[j - 1];
        values[j] = values[j - 1];
        j--;
      }
      moves[j] = move;
      values[j] = value;
    }
  }

  void rateEvasion(int transpositionMove, int primaryKillerMove, int secondaryKillerMove, HistoryTable historyTable) {
    for (int i = head; i < tail; i++) {
      int move = moves[i];

      if (move == transpositionMove) {
        values[i] = Integer.MAX_VALUE;
      } else if (Move.getTarget(move) != Piece.NOPIECE) {
        values[i] = getMVVLVARating(move);
      } else if (move == primaryKillerMove) {
        values[i] = 0;
      } else if (move == secondaryKillerMove) {
        values[i] = -1;
      } else {
        // -2 because of the secondary killer move
        values[i] = historyTable.get(moves[i]) - HistoryTable.MAX_HISTORYVALUE - 2;
      }
    }
  }

  /**
   * Rates the move list according to the history table.
   *
   */
  void rateFromHistory(HistoryTable historyTable) {
    for (int i = head; i < tail; i++) {
      values[i] = historyTable.get(moves[i]);
    }
  }

  /**
   * Rates the move according to the MVV/LVA.
   */
  void rateFromMVVLVA() {
    for (int i = head; i < tail; i++) {
      values[i] = getMVVLVARating(moves[i]);
    }
  }

  private int getMVVLVARating(int move) {
    int value = 0;

    int chessman = Move.getChessman(move);
    int target = Move.getTarget(move);

    value += Piece.VALUE_KING / Piece.getValueFromChessman(chessman);
    if (target != Piece.NOPIECE) {
      value += 10 * Piece.getValueFromChessman(target);
    }

    assert value >= (Piece.VALUE_KING / Piece.VALUE_KING) && value <= (Piece.VALUE_KING / Piece.VALUE_PAWN) + 10 * Piece.VALUE_QUEEN;

    return value;
  }

}
