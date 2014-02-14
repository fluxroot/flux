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

public final class PawnTable {

	public static final int ENTRYSIZE = 16;

	private final int size;
	
	private final long[] zobristCode;
	private final int[] opening;
	private final int[] endgame;
	
	public PawnTable(int newSize) {
		assert newSize >= 1;

		this.size = newSize;
		this.zobristCode = new long[this.size];
		this.opening = new int[this.size];
		this.endgame = new int[this.size];
	}

	/**
	 * Puts a zobrist code and opening and endgame value into the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @param newOpening the opening value.
	 * @param newEndgame the endgame value.
	 */
	public void put(long newZobristCode, int newOpening, int newEndgame) {
		int position = (int) (newZobristCode % this.size);

		this.zobristCode[position] = newZobristCode;
		this.opening[position] = newOpening;
		this.endgame[position] = newEndgame;
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
	 * Returns the opening given the zobrist code.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return the opening value.
	 */
	public int getOpening(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);

		if (this.zobristCode[position] == newZobristCode) {
			return this.opening[position];
		}
		
		throw new IllegalArgumentException();
	}
	
	/**
	 * Returns the endgame given the zobrist code.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return the endgame value.
	 */
	public int getEndgame(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);

		if (this.zobristCode[position] == newZobristCode) {
			return this.endgame[position];
		}
		
		throw new IllegalArgumentException();
	}

}
