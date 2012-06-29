/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.table;

/**
 * PawnTable
 *
 * @author Phokham Nonava
 */
public final class PawnTable {

	public static final int ENTRYSIZE = 12;

	private final int size;
	
	private final long[] zobristCode;
	private final int[] value;
	
	public PawnTable(int newSize) {
		assert newSize >= 1;

		this.size = newSize;
		this.zobristCode = new long[this.size];
		this.value = new int[this.size];
	}

	/**
	 * Puts a zobrist code and value into the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @param value the value.
	 */
	public void put(long newZobristCode, int value) {
		int position = (int) (newZobristCode % this.size);

		this.zobristCode[position] = newZobristCode;
		this.value[position] = value;
	}
	
	/**
	 * Returns whether or not this zobrist code exists in the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return true if the zobrist code exists in the table, false otherwise.
	 */
	public boolean exists(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);
		
		if (this.zobristCode[position] == newZobristCode) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the value given the zobrist code.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return the value.
	 */
	public int getValue(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);

		if (this.zobristCode[position] == newZobristCode) {
			return this.value[position];
		}
		
		throw new IllegalArgumentException();
	}
	
}
