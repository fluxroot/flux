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

public final class TestPerftTable {

	private final int size = 4194304;
	private int currentAge = 0;

	private long[] zobristCode;
	private int[] nodeNumber;
	private int[] age;
	
	public TestPerftTable() {
		this.zobristCode = new long[this.size];
		this.nodeNumber = new int[this.size];
		this.age = new int[this.size];
	}

	public void put(long newZobristCode, int newNodeNumber) {
		int position = (int) (newZobristCode % this.size);

		this.zobristCode[position] = newZobristCode;
		this.nodeNumber[position] = newNodeNumber;
		this.age[position] = this.currentAge;
	}
	
	public int get(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);

		if (this.zobristCode[position] == newZobristCode && this.currentAge == this.age[position]) {
			return this.nodeNumber[position];
		} else {
			return 0;
		}
	}
	
	public void increaseAge() {
		this.currentAge++;
	}

}
