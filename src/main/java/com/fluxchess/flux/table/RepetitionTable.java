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
package com.fluxchess.flux.table;

import com.fluxchess.flux.ISearch;

public final class RepetitionTable {

	private static final int MAXSIZE = ISearch.MAX_MOVES;

	private static long[] zobristCode;
	private int size = 0;

	/**
	 * Creates a new RepetitionTable.
	 */
	public RepetitionTable() {
		zobristCode = new long[MAXSIZE];
	}

	/**
	 * Puts a new zobrist code into the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 */
	public void put(long newZobristCode) {
		zobristCode[this.size++] = newZobristCode;
	}
	
	/**
	 * Removes the zobrist code from the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 */
	public void remove(long newZobristCode) {
		int index = -1;

		// Find the zobrist code from the end of the list
		for (int i = this.size - 1; i >= 0; i--) {
			if (zobristCode[i] == newZobristCode) {
				index = i;
				break;
			}
		}

		// Remove and shift
		if (index != -1) {
			for (int i = index + 1; i < this.size; i++) {
				zobristCode[index] = zobristCode[i];
				index++;
			}

			this.size--;

			return;
		}

		// We did not find the zobrist code
		throw new IllegalArgumentException();
	}
	
	/**
	 * Returns whether or not the zobrist code exists in the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return true if the zobrist code exists in the table, false otherwise.
	 */
	public boolean exists(long newZobristCode) {
		for (int i = this.size - 1; i >= 0; i--) {
			if (zobristCode[i] == newZobristCode) {
				return true;
			}
		}
		
		return false;
	}
	
}
