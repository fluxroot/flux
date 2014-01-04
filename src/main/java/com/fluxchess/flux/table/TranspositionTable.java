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
package com.fluxchess.flux.table;

import com.fluxchess.flux.Search;
import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.move.Move;
import com.fluxchess.flux.move.Score;
import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;

public final class TranspositionTable {

  // Size of one transposition entry
  public static final int ENTRYSIZE = 44;

  private final int size;

  // Entry
  private final TranspositionTableEntry[] entry;

  // Age
  private int currentAge = 0;

  // Number of slots used
  private int slotsUsed = 0;

  public TranspositionTable(int size) {
    assert size >= 1;

    this.size = size;

    // Initialize entry
    entry = new TranspositionTableEntry[size];
    for (int i = 0; i < entry.length; i++) {
      entry[i] = new TranspositionTableEntry();
    }

    currentAge = 0;
    slotsUsed = 0;
  }

  public void clear() {
    currentAge = 0;
    slotsUsed = 0;

    for (TranspositionTableEntry anEntry : entry) {
      anEntry.clear();
    }
  }

  public void increaseAge() {
    currentAge++;
    slotsUsed = 0;
  }

  /**
   * Puts the values into the TranspositionTable.
   *
   * @param zobristCode the zobrist code.
   * @param depth       the depth.
   * @param value       the value.
   * @param type        the value type.
   * @param move        the move.
   */
  public void put(long zobristCode, int depth, int value, int type, int move, boolean mateThreat, int height) {
    assert depth >= 0;
    assert type != Score.NOSCORE;
    assert height >= 0;

    int position = (int) (zobristCode % size);
    TranspositionTableEntry currentEntry = entry[position];

    //## BEGIN "always replace" Scheme
    if (currentEntry.zobristCode == 0 || currentEntry.age != currentAge) {
      // This is a new entry
      slotsUsed++;
      currentEntry.zobristCode = zobristCode;
      currentEntry.age = currentAge;
      currentEntry.depth = depth;
      currentEntry.setValue(value, height);
      currentEntry.type = type;
      currentEntry.move = move;
      currentEntry.mateThreat = mateThreat;
    } else if (currentEntry.zobristCode == zobristCode) {
      // The same zobrist key already exists
      if (depth >= currentEntry.depth && move != Move.NOMOVE) {
        currentEntry.depth = depth;
        currentEntry.setValue(value, height);
        currentEntry.type = type;
        currentEntry.move = move;
        currentEntry.mateThreat = mateThreat;
      }
    } else {
      // We have a collision. Overwrite existing entry
      currentEntry.zobristCode = zobristCode;
      currentEntry.depth = depth;
      currentEntry.setValue(value, height);
      currentEntry.type = type;
      currentEntry.move = move;
      currentEntry.mateThreat = mateThreat;
    }
    //## ENDOF "always replace" Scheme
  }

  /**
   * Returns the transposition table entry given the zobrist code.
   *
   * @param zobristCode the zobrist code.
   * @return the transposition table entry or null if there exists no entry.
   */
  public TranspositionTableEntry get(long zobristCode) {
    int position = (int) (zobristCode % size);
    TranspositionTableEntry currentEntry = entry[position];

    if (currentEntry.zobristCode == zobristCode && currentEntry.age == currentAge) {
      return currentEntry;
    } else {
      return null;
    }
  }

  /**
   * Returns the move list.
   *
   * @param board    the board.
   * @param depth    the maximum depth.
   * @param moveList the move list.
   * @return the move list.
   */
  public List<GenericMove> getMoveList(Board board, int depth, List<GenericMove> moveList) {
    assert board != null;
    assert depth >= 0;
    assert moveList != null;

    TranspositionTableEntry currentEntry = get(board.zobristCode);

    if (currentEntry == null
      || depth == 0
      || currentEntry.move == Move.NOMOVE) {
      return moveList;
    } else {
      moveList.add(Move.toGenericMove(currentEntry.move));

      board.makeMove(currentEntry.move);
      List<GenericMove> newMoveList = getMoveList(board, depth - 1, moveList);
      board.undoMove(currentEntry.move);

      return newMoveList;
    }
  }

  /**
   * Returns the permill of slots used.
   *
   * @return the permill of slots used.
   */
  public int getPermillUsed() {
    return (int) ((1000L * (long) slotsUsed) / (long) size);
  }

  public final class TranspositionTableEntry {

    public long zobristCode = 0;
    public int age = -1;
    public int depth = -1;
    private int value = -Search.INFINITY;
    public int type = Score.NOSCORE;
    public int move = Move.NOMOVE;
    public boolean mateThreat = false;

    public void clear() {
      zobristCode = 0;
      age = -1;
      depth = -1;
      value = -Search.INFINITY;
      type = Score.NOSCORE;
      move = Move.NOMOVE;
      mateThreat = false;
    }

    public int getValue(int height) {
      int value = this.value;
      if (value < -Search.CHECKMATE_THRESHOLD) {
        value += height;
      } else if (value > Search.CHECKMATE_THRESHOLD) {
        value -= height;
      }

      return value;
    }

    public void setValue(int value, int height) {
      // Normalize mate values
      if (value < -Search.CHECKMATE_THRESHOLD) {
        value -= height;
      } else if (value > Search.CHECKMATE_THRESHOLD) {
        value += height;
      }
      assert value <= Search.CHECKMATE || value >= -Search.CHECKMATE;

      this.value = value;
    }

  }

}
