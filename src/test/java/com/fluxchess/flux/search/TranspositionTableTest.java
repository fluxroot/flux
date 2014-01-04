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
package com.fluxchess.flux.search;

import com.fluxchess.flux.board.*;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TranspositionTableTest {

  private static final Logger LOG = LoggerFactory.getLogger(TranspositionTableTest.class);

  @Test
  public void testTranspositionTable() {
    TranspositionTable table = new TranspositionTable(10);
    int move1 = Move.createMove(Move.NORMAL, Position.a2, Position.a3, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);

    // Put an entry into the table
    table.put(1L, 1, 100, Score.EXACT, move1, false, 0);

    TranspositionTable.TranspositionTableEntry entry = table.get(1L);
    assertNotNull(entry);

    assertEquals(1, entry.depth);
    assertEquals(100, entry.getValue(0));
    assertEquals(Score.EXACT, entry.type);
    assertEquals(move1, entry.move);
    assertEquals(100, table.getPermillUsed());

    // Overwrite the entry with a new one
    table.put(1L, 2, 200, Score.BETA, move1, false, 0);

    entry = table.get(1L);
    assertNotNull(entry);

    assertEquals(2, entry.depth);
    assertEquals(200, entry.getValue(0));
    assertEquals(Score.BETA, entry.type);
    assertEquals(move1, entry.move);
    assertEquals(100, table.getPermillUsed());

    // Put an mate entry into the table
    table.put(2L, 0, Search.CHECKMATE - 5, Score.EXACT, move1, false, 3);

    entry = table.get(2L);
    assertNotNull(entry);

    assertEquals(0, entry.depth);
    assertEquals(Search.CHECKMATE - 4, entry.getValue(2));
    assertEquals(Score.EXACT, entry.type);
    assertEquals(move1, entry.move);
    assertEquals(200, table.getPermillUsed());

    // Increase the age
    table.increaseAge();

    assertNull(table.get(2L));

    assertEquals(0, table.getPermillUsed());
  }

  @Ignore("Enable this method and increase the heap size if you want to test memory consumption")
//  @Test
  public void testSize() {
    LOG.info("Testing Transposition Table size:");
    int[] megabytes = {4, 8, 16, 32, 64, 128, 256};
    for (int i : megabytes) {
      int numberOfEntries = i * 1024 * 1024 / TranspositionTable.ENTRYSIZE;

      System.gc();
      long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      new TranspositionTable(numberOfEntries);
      long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

      long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / (1024 * 1024);
      LOG.info("Transposition Table size " + i + " = " + hashAllocation);
    }
  }

}
