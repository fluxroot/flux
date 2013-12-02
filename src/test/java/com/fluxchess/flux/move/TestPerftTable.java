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
package com.fluxchess.flux.move;

public final class TestPerftTable {

  private final int size = 4194304;
  private int currentAge = 0;

  private final long[] zobristCode;
  private final int[] nodeNumber;
  private final int[] age;

  public TestPerftTable() {
    zobristCode = new long[size];
    nodeNumber = new int[size];
    age = new int[size];
  }

  public void put(long newZobristCode, int newNodeNumber) {
    int position = (int) (newZobristCode % size);

    zobristCode[position] = newZobristCode;
    nodeNumber[position] = newNodeNumber;
    age[position] = currentAge;
  }

  public int get(long newZobristCode) {
    int position = (int) (newZobristCode % size);

    if (zobristCode[position] == newZobristCode && currentAge == age[position]) {
      return nodeNumber[position];
    } else {
      return 0;
    }
  }

  public void increaseAge() {
    currentAge++;
  }

}
