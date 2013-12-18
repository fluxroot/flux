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

public final class BitPieceList {

  public long list = 0;

  public BitPieceList() {
  }

  public BitPieceList(BitPieceList list) {
    assert list != null;

    this.list = list.list;
  }

  static int toX88Position(int position) {
    assert position >= 0 && position < Long.SIZE;

    return ((position & ~7) << 1) | (position & 7);
  }

  static int toBitPosition(int position) {
    assert (position & 0x88) == 0;

    return ((position & ~7) >>> 1) | (position & 7);
  }

  public static int next(long positions) {
    return BitPieceList.toX88Position(Long.numberOfTrailingZeros(positions));
  }

  public int size() {
    return Long.bitCount(list);
  }

  public void add(int position) {
    assert (position & 0x88) == 0;
    assert (list & (1L << toBitPosition(position))) == 0 : String.format("list = %d, 0x88 position = %d, bit position = %d", list, position, toBitPosition(position));

    list |= 1L << toBitPosition(position);
  }

  public void remove(int position) {
    assert (position & 0x88) == 0;
    assert (list & (1L << toBitPosition(position))) != 0 : String.format("list = %d, 0x88 position = %d, bit position = %d", list, position, toBitPosition(position));

    list &= ~(1L << toBitPosition(position));
  }

}
