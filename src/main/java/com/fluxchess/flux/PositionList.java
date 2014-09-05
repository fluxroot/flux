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
package com.fluxchess.flux;

public final class PositionList {

  public final int MAXSIZE;

  public final int[] position;
  public int size = 0;

  public PositionList() {
    this.MAXSIZE = 64;
    this.position = new int[MAXSIZE];
  }

  public PositionList(int MAXSIZE) {
    this.MAXSIZE = MAXSIZE;
    this.position = new int[MAXSIZE];
  }

  /**
   * Adds the position to the list.
   *  @param position the position.
   *
   */
  void addPosition(int position) {
    assert (position & 0x88) == 0;
    assert this != null;
    assert size >= 0 && size < MAXSIZE;

    // Iterate over the list from the end
    int j = size;
    for (int i = size - 1; i >= 0; i--) {
      assert this.position[i] != position;
      if (this.position[i] > position) {
        this.position[j] = this.position[i];
        j--;
      } else {
        break;
      }
    }
    this.position[j] = position;
    size++;

    assert size > 0 && size <= MAXSIZE;
  }

  /**
   * Removes the position from the list.
   *  @param position the position.
   *
   */
  void removePosition(int position) {
    assert (position & 0x88) == 0;
    assert this != null;
    assert size > 0 && size <= MAXSIZE;

    // Iterate over the list from the beginning
    int j = 0;
    for (int i = 0; i < size; i++) {
      if (this.position[i] != position) {
        this.position[j] = this.position[i];
        j++;
      }
    }

    size--;

    assert size >= 0 && size < MAXSIZE;
  }

}
