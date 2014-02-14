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

public final class EvaluationTable {

	// Size of one evaluation entry
	public static final int ENTRYSIZE = 28;

	private final int size;
	
	private final EvaluationTableEntry[] entry;
	
	public EvaluationTable(int newSize) {
		assert newSize >= 1;

		this.size = newSize;

		// Initialize entry
		this.entry = new EvaluationTableEntry[newSize];
		for (int i = 0; i < this.entry.length; i++) {
			this.entry[i] = new EvaluationTableEntry();
		}
	}

	/**
	 * Puts a zobrist code and evaluation value into the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @param newEvaluation the evaluation value.
	 */
	public void put(long newZobristCode, int newEvaluation) {
		int position = (int) (newZobristCode % this.size);
		EvaluationTableEntry currentEntry = this.entry[position];

		currentEntry.zobristCode = newZobristCode;
		currentEntry.evaluation = newEvaluation;
	}

	/**
	 * Returns the evaluation table entry given the zobrist code.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return the evaluation table entry or null if there exists no entry.
	 */
	public EvaluationTableEntry get(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);
		EvaluationTableEntry currentEntry = this.entry[position];

		if (currentEntry.zobristCode == newZobristCode) {
			return currentEntry;
		} else {
			return null;
		}
	}
	
}
