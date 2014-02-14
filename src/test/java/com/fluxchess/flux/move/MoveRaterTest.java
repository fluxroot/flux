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
package com.fluxchess.flux.move;

import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.table.HistoryTable;

import static org.junit.Assert.assertEquals;

public class MoveRaterTest {

//	@Test
	public void testRate() {
		MoveRater rater = new MoveRater(new HistoryTable());

		MoveList list = new MoveList();

		// Pawn -> Rook
		int move1 = IntMove.createMove(IntMove.NORMAL, 16, 0, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move1;

		// Knight -> Rook
		int move2 = IntMove.createMove(IntMove.NORMAL, 1, 0, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move2;

		// Pawn -> Knight
		int move3 = IntMove.createMove(IntMove.NORMAL, 16, 1, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move3;

		// Rook -> Knight
		int move4 = IntMove.createMove(IntMove.NORMAL, 0, 1, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move4;

		// Rook -> Rook
		int move5 = IntMove.createMove(IntMove.NORMAL, 0, 7, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move5;

		// King -> Empty
		int move6 = IntMove.createMove(IntMove.NORMAL, 4, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move6;

		// Pawn -> Pawn
		int move7 = IntMove.createMove(IntMove.NORMAL, 16, 17, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move7;

		// Pawn -> Empty
		int move8 = IntMove.createMove(IntMove.NORMAL, 16, 32, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		list.move[list.tail++] = move8;

		rater.rateFromMVVLVA(list);
		MoveSorter.sort(list);

		assertEquals(move1, list.move[0]);
		assertEquals(move2, list.move[1]);
		assertEquals(move5, list.move[2]);
		assertEquals(move3, list.move[3]);
		assertEquals(move4, list.move[4]);
		assertEquals(move7, list.move[5]);
		assertEquals(move8, list.move[6]);
		assertEquals(move6, list.move[7]);
	}

}
