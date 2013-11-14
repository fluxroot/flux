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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class PawnTable {

  public static final int ENTRYSIZE = 12;

  private final int size;

  private final long[] zobristCode;
  private final int[] value;

  private final Lock readLock;
  private final Lock writeLock;

  public PawnTable(int size) {
    assert size >= 1;

    this.size = size;
    zobristCode = new long[size];
    value = new int[size];

    // Initialize locks
    ReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }

  /**
   * Puts a zobrist code and value into the table.
   *
   * @param zobristCode the zobrist code.
   * @param value       the value.
   */
  public void put(long zobristCode, int value) {
    int position = (int) (zobristCode % size);

    writeLock.lock();
    try {
      this.zobristCode[position] = zobristCode;
      this.value[position] = value;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Returns whether or not this zobrist code exists in the table.
   *
   * @param zobristCode the zobrist code.
   * @return true if the zobrist code exists in the table, false otherwise.
   */
  public boolean exists(long zobristCode) {
    int position = (int) (zobristCode % size);

    readLock.lock();
    try {
      return this.zobristCode[position] == zobristCode;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns the value given the zobrist code.
   *
   * @param zobristCode the zobrist code.
   * @return the value.
   */
  public int getValue(long zobristCode) {
    int position = (int) (zobristCode % size);

    readLock.lock();
    try {
      if (this.zobristCode[position] == zobristCode) {
        return value[position];
      }

      throw new IllegalArgumentException();
    } finally {
      readLock.unlock();
    }
  }

}
