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
package com.fluxchess.flux.board;

import com.fluxchess.flux.search.Search;

public final class MoveList {

  private static final int MAXSIZE = 4096;
  private static final int HISTORYSIZE = Search.MAX_HEIGHT + 1;

  public final int[] move = new int[MAXSIZE];
  public final int[] value = new int[MAXSIZE];
  public int head = 0;
  public int index = 0;
  public int tail = 0;

  private final int[] historyHead = new int[HISTORYSIZE];
  private final int[] historyIndex = new int[HISTORYSIZE];
  private final int[] historyTail = new int[HISTORYSIZE];
  private int historyCount = 0;

  public void newList() {
    assert historyCount < HISTORYSIZE;

    historyHead[historyCount] = head;
    historyIndex[historyCount] = index;
    historyTail[historyCount] = tail;
    historyCount++;

    head = tail;
    index = tail;
  }

  public void deleteList() {
    assert historyCount > 0;

    historyCount--;
    head = historyHead[historyCount];
    index = historyIndex[historyCount];
    tail = historyTail[historyCount];
  }

  public void clear() {
    tail = head;
    index = head;
  }

  public int size() {
    return tail - head;
  }

}
