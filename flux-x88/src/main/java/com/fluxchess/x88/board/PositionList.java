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
package com.fluxchess.x88.board;

public final class PositionList {

  public static final int MAXSIZE = 64;

  public final int[] positions = new int[MAXSIZE];
  public int size = 0;

  /**
   * Adds the position to the list.
   *
   * @param position the position.
   */
  public void add(int position) {
    assert (position & 0x88) == 0;
    assert size >= 0 && size < PositionList.MAXSIZE;

    positions[size++] = position;

    assert size > 0 && size <= PositionList.MAXSIZE;
  }

  /**
   * Removes the position from the list.
   *
   * @param position the position.
   */
  public void remove(int position) {
    assert (position & 0x88) == 0;
    assert size > 0 && size <= PositionList.MAXSIZE;

    --size;

    // Iterate over the list from the end
    int oldPosition = positions[size];
    for (int i = size - 1; i >= 0; --i) {
      if (oldPosition != position) {
        oldPosition ^= positions[i];
        positions[i] ^= oldPosition;
        oldPosition ^= positions[i];
      } else {
        break;
      }
    }

    assert oldPosition == position;
    assert size >= 0 && size < PositionList.MAXSIZE;
  }

}
