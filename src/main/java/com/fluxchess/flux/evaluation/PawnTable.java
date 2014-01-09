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

public final class PawnTable {

  public static final int ENTRYSIZE = 12;

  private final int size;

  private final long[] zobristCode;
  private final int[] value;

  public PawnTable(int size) {
    assert size >= 1;

    this.size = size;
    zobristCode = new long[size];
    value = new int[size];
  }

  /**
   * Puts a zobrist code and value into the table.
   *
   * @param zobristCode the zobrist code.
   * @param value       the value.
   */
  public void put(long zobristCode, int value) {
    int index = (int) (zobristCode % size);

    this.zobristCode[index] = zobristCode;
    this.value[index] = value;
  }

  /**
   * Returns whether or not this zobrist code exists in the table.
   *
   * @param zobristCode the zobrist code.
   * @return true if the zobrist code exists in the table, false otherwise.
   */
  public boolean exists(long zobristCode) {
    int index = (int) (zobristCode % size);

    return this.zobristCode[index] == zobristCode;
  }

  /**
   * Returns the value given the zobrist code.
   *
   * @param zobristCode the zobrist code.
   * @return the value.
   */
  public int getValue(long zobristCode) {
    int index = (int) (zobristCode % size);

    if (this.zobristCode[index] == zobristCode) {
      return value[index];
    }

    throw new IllegalArgumentException();
  }

}
