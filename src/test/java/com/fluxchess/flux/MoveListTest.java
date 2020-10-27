/*
 * Copyright (C) 2007-2014 Phokham Nonava
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveListTest {

	@Test
	public void testRemove() {
		MoveList moveList = new MoveList();

		moveList.newList();
		assertEquals(0, moveList.getLength());
		moveList.moves[moveList.tail++] = 1;
		assertEquals(1, moveList.getLength());

		moveList.newList();
		assertEquals(0, moveList.getLength());
		moveList.moves[moveList.tail++] = 1;
		moveList.moves[moveList.tail++] = 1;
		moveList.moves[moveList.tail++] = 1;
		assertEquals(3, moveList.getLength());

		moveList.deleteList();
		assertEquals(1, moveList.getLength());

		moveList.deleteList();
		assertEquals(0, moveList.getLength());
	}

	@Test
	public void testSort() {
		MoveList list = new MoveList();

		list.moves[list.tail] = 10;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 9;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 8;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 7;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 6;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 5;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 4;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 3;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 2;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 1;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;

		list.sort();

		for (int i = 0; i < 10; i++) {
			assertEquals(10 - i, list.moves[i]);
			assertEquals(10 - i, list.values[i]);
		}

		list = new MoveList();
		list.moves[list.tail] = 3;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 4;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 6;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 1;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 10;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 9;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 2;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 8;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 5;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;
		list.moves[list.tail] = 7;
		list.values[list.tail] = list.moves[list.tail];
		list.tail++;

		list.sort();

		for (int i = 0; i < 10; i++) {
			assertEquals(10 - i, list.moves[i]);
			assertEquals(10 - i, list.values[i]);
		}
	}
}
