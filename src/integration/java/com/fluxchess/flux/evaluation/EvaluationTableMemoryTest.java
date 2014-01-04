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
package com.fluxchess.flux.evaluation;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationTableMemoryTest {

  private static final Logger LOG = LoggerFactory.getLogger(EvaluationTableMemoryTest.class);

  @Test
  public void testSize() {
    LOG.info("Testing Evaluation Table size:");
    int[] megabytes = {4, 8, 16, 32, 64};
    for (int i : megabytes) {
      int numberOfEntries = i * 1024 * 1024 / EvaluationTable.ENTRYSIZE;

      System.gc();
      long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      new EvaluationTable(numberOfEntries);
      long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

      long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / 1024 / 1024;
      LOG.info("Evaluation Table size " + i + " = " + hashAllocation);
    }
  }

}
