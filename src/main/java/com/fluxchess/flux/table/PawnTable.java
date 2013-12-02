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

public final class PawnTable {

  public static final int ENTRYSIZE = 16;

  private final int size;

  private final long[] zobristCode;
  private final int[] opening;
  private final int[] endgame;

  public PawnTable(int size) {
    assert size >= 1;

    this.size = size;
    zobristCode = new long[size];
    opening = new int[size];
    endgame = new int[size];
  }

  /**
   * Puts a zobrist code and opening and endgame value into the table.
   *
   * @param zobristCode the zobrist code.
   * @param opening     the opening value.
   * @param endgame     the endgame value.
   */
  public void put(long zobristCode, int opening, int endgame) {
    int position = (int) (zobristCode % size);

    this.zobristCode[position] = zobristCode;
    this.opening[position] = opening;
    this.endgame[position] = endgame;
  }

  /**
   * Returns whether or not this zobrist code exists in the table.
   *
   * @param zobristCode the zobrist code.
   * @return true if the zobrist code exists in the table, false otherwise.
   */
  public boolean exists(long zobristCode) {
    int position = (int) (zobristCode % size);

    return this.zobristCode[position] == zobristCode;
  }

  /**
   * Returns the opening given the zobrist code.
   *
   * @param zobristCode the zobrist code.
   * @return the opening value.
   */
  public int getOpening(long zobristCode) {
    int position = (int) (zobristCode % size);

    if (this.zobristCode[position] == zobristCode) {
      return opening[position];
    }

    throw new IllegalArgumentException();
  }

  /**
   * Returns the endgame given the zobrist code.
   *
   * @param zobristCode the zobrist code.
   * @return the endgame value.
   */
  public int getEndgame(long zobristCode) {
    int position = (int) (zobristCode % size);

    if (this.zobristCode[position] == zobristCode) {
      return endgame[position];
    }

    throw new IllegalArgumentException();
  }

}
