/*
 * Copyright 2007-2020 Phokham Nonava
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

import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;

final class TranspositionTable {

	// Size of one transposition entry
	static final int ENTRYSIZE = 44;

	private final int size;

	// Entry
	private final TranspositionTableEntry[] entry;

	// Age
	private int currentAge = 0;

	static final class TranspositionTableEntry {

		long zobristCode = 0;
		int age = -1;
		int depth = -1;
		private int value = -Value.INFINITY;
		int type = Bound.NOBOUND;
		int move = Move.NOMOVE;
		boolean mateThreat = false;

		TranspositionTableEntry() {
		}

		void clear() {
			this.zobristCode = 0;
			this.age = -1;
			this.depth = -1;
			this.value = -Value.INFINITY;
			this.type = Bound.NOBOUND;
			this.move = Move.NOMOVE;
			this.mateThreat = false;
		}

		int getValue(int height) {
			int value = this.value;
			if (value < -Value.CHECKMATE_THRESHOLD) {
				value += height;
			} else if (value > Value.CHECKMATE_THRESHOLD) {
				value -= height;
			}

			return value;
		}

		void setValue(int value, int height) {
			// Normalize mate values
			if (value < -Value.CHECKMATE_THRESHOLD) {
				value -= height;
			} else if (value > Value.CHECKMATE_THRESHOLD) {
				value += height;
			}
			assert value <= Value.CHECKMATE || value >= -Value.CHECKMATE;

			this.value = value;
		}
	}

	/**
	 * Creates a new TranspositionTable.
	 *
	 * @param newSize the size.
	 */
	TranspositionTable(int newSize) {
		assert newSize >= 1;

		this.size = newSize;

		// Initialize entry
		this.entry = new TranspositionTableEntry[newSize];
		for (int i = 0; i < this.entry.length; i++) {
			this.entry[i] = new TranspositionTableEntry();
		}

		this.currentAge = 0;
	}

	/**
	 * Clears the Transposition Table.
	 */
	void clear() {
		this.currentAge = 0;

		for (TranspositionTableEntry anEntry : this.entry) {
			anEntry.clear();
		}
	}

	/**
	 * Increase the age of the Transposition Table.
	 */
	void increaseAge() {
		this.currentAge++;
	}

	/**
	 * Puts the values into the TranspositionTable.
	 *
	 * @param zobristCode the zobrist code.
	 * @param depth       the depth.
	 * @param value       the value.
	 * @param type        the value type.
	 * @param move        the move.
	 */
	void put(long zobristCode, int depth, int value, int type, int move, boolean mateThreat, int height) {
		assert depth >= 0;
		assert type != Bound.NOBOUND;
		assert height >= 0;

		int position = (int) (zobristCode % this.size);
		TranspositionTableEntry currentEntry = this.entry[position];

		//## BEGIN "always replace" Scheme
		if (currentEntry.zobristCode == 0 || currentEntry.age != this.currentAge) {
			// This is a new entry
			currentEntry.zobristCode = zobristCode;
			currentEntry.age = this.currentAge;
			currentEntry.depth = depth;
			currentEntry.setValue(value, height);
			currentEntry.type = type;
			currentEntry.move = move;
			currentEntry.mateThreat = mateThreat;
		} else if (currentEntry.zobristCode == zobristCode) {
			// The same zobrist key already exists
			if (depth >= currentEntry.depth && move != Move.NOMOVE) {
				currentEntry.depth = depth;
				currentEntry.setValue(value, height);
				currentEntry.type = type;
				currentEntry.move = move;
				currentEntry.mateThreat = mateThreat;
			}
		} else {
			// We have a collision. Overwrite existing entry
			currentEntry.zobristCode = zobristCode;
			currentEntry.depth = depth;
			currentEntry.setValue(value, height);
			currentEntry.type = type;
			currentEntry.move = move;
			currentEntry.mateThreat = mateThreat;
		}
		//## ENDOF "always replace" Scheme
	}

	/**
	 * Returns the transposition table entry given the zobrist code.
	 *
	 * @param zobristCode the zobrist code.
	 * @return the transposition table entry or null if there exists no entry.
	 */
	TranspositionTableEntry get(long zobristCode) {
		int position = (int) (zobristCode % this.size);
		TranspositionTableEntry currentEntry = this.entry[position];

		if (currentEntry.zobristCode == zobristCode && currentEntry.age == this.currentAge) {
			return currentEntry;
		} else {
			return null;
		}
	}

	/**
	 * Returns the move list.
	 *
	 * @param board    the board.
	 * @param depth    the maximum depth.
	 * @param moveList the move list.
	 * @return the move list.
	 */
	List<GenericMove> getMoveList(Position board, int depth, List<GenericMove> moveList) {
		assert board != null;
		assert depth >= 0;
		assert moveList != null;

		TranspositionTableEntry currentEntry = get(board.zobristCode);

		if (currentEntry == null
				|| depth == 0
				|| currentEntry.move == Move.NOMOVE) {
			return moveList;
		} else {
			moveList.add(Move.toCommandMove(currentEntry.move));

			board.makeMove(currentEntry.move);
			List<GenericMove> newMoveList = getMoveList(board, depth - 1, moveList);
			board.undoMove(currentEntry.move);

			return newMoveList;
		}
	}
}
