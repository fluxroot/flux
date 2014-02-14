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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PawnTableTest {

	@Test
	public void testPawnTable() {
		PawnTable table = new PawnTable(1024);
		
		table.put(1, 1, 1);
		assertTrue(table.exists(1));
		assertEquals(1, table.getOpening(1));
		assertEquals(1, table.getEndgame(1));

		table.put(2, 2, 2);
		assertTrue(table.exists(2));
		assertEquals(2, table.getOpening(2));
		assertEquals(2, table.getEndgame(2));
	}

	@Test
	public void testSize() {
		System.out.println("Testing Pawn Table size:");
		int[] megabytes = { 4, 8, 16, 32, 64 };
		for (int i : megabytes) {
			int numberOfEntries = i * 1024 * 1024 / PawnTable.ENTRYSIZE;

			System.gc();
			long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			new PawnTable(numberOfEntries);
			long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

			long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / 1024 / 1024;
			System.out.println("Pawn Table size " + i + " = " + hashAllocation);
		}
	}

}
