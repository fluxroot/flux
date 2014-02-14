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

import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.move.IntMove;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KillerTableTest {

	@Test
	public void testKillerMoves() {
		KillerTable killerMoves = new KillerTable();
		
		// Put a new killer move into the list
		int move1 = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		killerMoves.add(move1, 0);
		assertEquals(move1, killerMoves.getPrimaryKiller(0));
		assertEquals(IntMove.NOMOVE, killerMoves.getSecondaryKiller(0));

		// Put the same killer move into the list
		int move2 = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		killerMoves.add(move2, 0);
		assertEquals(move2, killerMoves.getPrimaryKiller(0));
		assertEquals(IntMove.NOMOVE, killerMoves.getSecondaryKiller(0));

		// Put a new killer move into the list
		int move3 = IntMove.createMove(IntMove.NORMAL, 1, 17, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		killerMoves.add(move3, 0);
		assertEquals(move3, killerMoves.getPrimaryKiller(0));
		assertEquals(move2, killerMoves.getSecondaryKiller(0));

		// Put a new killer move into the list twice
		int move4 = IntMove.createMove(IntMove.NORMAL, 2, 18, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		killerMoves.add(move4, 0);
		killerMoves.add(move4, 0);
		assertEquals(move4, killerMoves.getPrimaryKiller(0));
		assertEquals(move3, killerMoves.getSecondaryKiller(0));
	}

}
