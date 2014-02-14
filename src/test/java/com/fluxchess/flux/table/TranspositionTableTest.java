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

import com.fluxchess.flux.Search;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.IntValue;
import org.junit.Test;

import static org.junit.Assert.*;

public class TranspositionTableTest {

	@Test
	public void testTranspositionTable() {
		TranspositionTable table = new TranspositionTable(10);
		int move1 = IntMove.createMove(IntMove.NORMAL, IntPosition.a2, IntPosition.a3, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);

		// Put an entry into the table
		table.put(1L, 1, 100, IntValue.EXACT, move1, false, 0);

		TranspositionTableEntry entry = table.get(1L);
		assertNotNull(entry);

		assertEquals(1, entry.depth);
		assertEquals(100, entry.getValue(0));
		assertEquals(IntValue.EXACT, entry.type);
		assertEquals(move1, entry.move);
		assertEquals(100, table.getPermillUsed());

		// Overwrite the entry with a new one
		table.put(1L, 2, 200, IntValue.BETA, move1, false, 0);

		entry = table.get(1L);
		assertNotNull(entry);

		assertEquals(2, entry.depth);
		assertEquals(200, entry.getValue(0));
		assertEquals(IntValue.BETA, entry.type);
		assertEquals(move1, entry.move);
		assertEquals(100, table.getPermillUsed());

		// Put an mate entry into the table
		table.put(2L, 0, Search.CHECKMATE - 5, IntValue.EXACT, move1, false, 3);

		entry = table.get(2L);
		assertNotNull(entry);

		assertEquals(0, entry.depth);
		assertEquals(Search.CHECKMATE - 4, entry.getValue(2));
		assertEquals(IntValue.EXACT, entry.type);
		assertEquals(move1, entry.move);
		assertEquals(200, table.getPermillUsed());

		// Increase the age
		table.increaseAge();
		
		assertNull(table.get(2L));
		
		assertEquals(0, table.getPermillUsed());
	}

	@Test
	public void testSize() {
		System.out.println("Testing Transposition Table size:");
		int[] megabytes = { 4, 8, 16, 32, 64, 128, 256 };
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
