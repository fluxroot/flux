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
package com.fluxchess.flux.table;

import com.fluxchess.flux.search.Search;

public final class RepetitionTable {

  private static final int MAXSIZE = Search.MAX_MOVES;

  private final long[] zobristCode = new long[MAXSIZE];
  private int size = 0;

  public RepetitionTable() {
  }

  public RepetitionTable(RepetitionTable table) {
    assert table != null;

    System.arraycopy(table.zobristCode, 0, zobristCode, 0, MAXSIZE);
    size = table.size;
  }

  /**
   * Puts a new zobrist code into the table.
   *
   * @param zobristCode the zobrist code.
   */
  public void put(long zobristCode) {
    this.zobristCode[size++] = zobristCode;
  }

  /**
   * Removes the zobrist code from the table.
   *
   * @param zobristCode the zobrist code.
   */
  public void remove(long zobristCode) {
    int index = -1;

    // Find the zobrist code from the end of the list
    for (int i = size - 1; i >= 0; i--) {
      if (this.zobristCode[i] == zobristCode) {
        index = i;
        break;
      }
    }

    // Remove and shift
    if (index != -1) {
      for (int i = index + 1; i < size; i++) {
        this.zobristCode[index] = this.zobristCode[i];
        index++;
      }

      size--;

      return;
    }

    // We did not find the zobrist code
    throw new IllegalArgumentException();
  }

  /**
   * Returns whether or not the zobrist code exists in the table.
   *
   * @param zobristCode the zobrist code.
   * @return true if the zobrist code exists in the table, false otherwise.
   */
  public boolean exists(long zobristCode) {
    for (int i = size - 1; i >= 0; i--) {
      if (this.zobristCode[i] == zobristCode) {
        return true;
      }
    }

    return false;
  }

}
