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
package com.fluxchess.flux.move;

import com.fluxchess.flux.ISearch;

public final class MoveList {

	private static final int MAXSIZE = 4096;
	private static final int HISTORYSIZE = ISearch.MAX_HEIGHT + 1;

	public final int[] move = new int[MAXSIZE];
	public final int[] value = new int[MAXSIZE];
	public int head = 0;
	public int index = 0;
	public int tail = 0;

	private final int[] historyHead = new int[HISTORYSIZE];
	private final int[] historyIndex = new int[HISTORYSIZE];
	private final int[] historyTail = new int[HISTORYSIZE];
	private int historyCount = 0;

	public MoveList() {
	}

	public void newList() {
		assert this.historyCount < HISTORYSIZE;

		this.historyHead[this.historyCount] = this.head;
		this.historyIndex[this.historyCount] = this.index;
		this.historyTail[this.historyCount] = this.tail;
		this.historyCount++;

		this.head = this.tail;
		this.index = this.tail;
	}

	public void deleteList() {
		assert this.historyCount > 0;

		this.historyCount--;
		this.head = this.historyHead[this.historyCount];
		this.index = this.historyIndex[this.historyCount];
		this.tail = this.historyTail[this.historyCount];
	}

	public void resetList() {
		this.tail = this.head;
		this.index = this.head;
	}

	public int getLength() {
		return this.tail - this.head;
	}

}
