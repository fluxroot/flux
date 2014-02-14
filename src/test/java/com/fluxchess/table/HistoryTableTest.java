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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fluxchess.board.IntChessman;
import com.fluxchess.move.IntMove;
import com.fluxchess.table.HistoryTable;

/**
 * HistoryTableTest
 *
 * @author Phokham Nonava
 */
public class HistoryTableTest {

	@Test
	public void testHistoryTable() {
		HistoryTable table = new HistoryTable();
		
		int move1 = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.PAWN, IntChessman.NOPIECE, IntChessman.NOPIECE);
		table.add(move1, 1);
		assertEquals(1, table.get(move1));

		int move2 = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.PAWN, IntChessman.NOPIECE, IntChessman.NOPIECE);
		table.add(move2, 1);
		assertEquals(2, table.get(move2));
	}

}
