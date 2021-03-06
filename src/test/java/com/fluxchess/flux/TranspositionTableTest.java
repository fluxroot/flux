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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TranspositionTableTest {

	@Test
	void testTranspositionTable() {
		TranspositionTable table = new TranspositionTable(10);
		int move1 = Move.createMove(MoveType.NORMAL, Square.a2, Square.a3, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);

		// Put an entry into the table
		table.put(1L, 1, 100, Bound.EXACT, move1, false, 0);

		TranspositionTable.TranspositionTableEntry entry = table.get(1L);
		assertThat(entry).isNotNull();

		assertThat(1).isEqualTo(entry.depth);
		assertThat(100).isEqualTo(entry.getValue(0));
		assertThat(Bound.EXACT).isEqualTo(entry.type);
		assertThat(move1).isEqualTo(entry.move);

		// Overwrite the entry with a new one
		table.put(1L, 2, 200, Bound.LOWER, move1, false, 0);

		entry = table.get(1L);
		assertThat(entry).isNotNull();

		assertThat(2).isEqualTo(entry.depth);
		assertThat(200).isEqualTo(entry.getValue(0));
		assertThat(Bound.LOWER).isEqualTo(entry.type);
		assertThat(move1).isEqualTo(entry.move);

		// Put an mate entry into the table
		table.put(2L, 0, Value.CHECKMATE - 5, Bound.EXACT, move1, false, 3);

		entry = table.get(2L);
		assertThat(entry).isNotNull();

		assertThat(0).isEqualTo(entry.depth);
		assertThat(Value.CHECKMATE - 4).isEqualTo(entry.getValue(2));
		assertThat(Bound.EXACT).isEqualTo(entry.type);
		assertThat(move1).isEqualTo(entry.move);

		// Increase the age
		table.increaseAge();

		assertThat(table.get(2L)).isNull();
	}

	@Test
	void testSize() {
		System.out.println("Testing Transposition Table size:");
		int[] megabytes = {4, 8, 16, 32, 64, 128, 256};
		for (int i : megabytes) {
			int numberOfEntries = i * 1024 * 1024 / TranspositionTable.ENTRYSIZE;

			System.gc();
			long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			new TranspositionTable(numberOfEntries);
			long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / (1024 * 1024);
			System.out.println("Transposition Table size " + i + " = " + hashAllocation);
		}
	}
}
