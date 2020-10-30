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

class MoveListTest {

	@Test
	void testRemove() {
		MoveList moveList = new MoveList();

		moveList.newList();
		assertThat(0).isEqualTo(moveList.getLength());
		moveList.moves[moveList.tail++] = 1;
		assertThat(1).isEqualTo(moveList.getLength());

		moveList.newList();
		assertThat(0).isEqualTo(moveList.getLength());
		moveList.moves[moveList.tail++] = 1;
		moveList.moves[moveList.tail++] = 1;
		moveList.moves[moveList.tail++] = 1;
		assertThat(3).isEqualTo(moveList.getLength());

		moveList.deleteList();
		assertThat(1).isEqualTo(moveList.getLength());

		moveList.deleteList();
		assertThat(0).isEqualTo(moveList.getLength());
	}

	@Test
	void testSort() {
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
			assertThat(10 - i).isEqualTo(list.moves[i]);
			assertThat(10 - i).isEqualTo(list.values[i]);
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
			assertThat(10 - i).isEqualTo(list.moves[i]);
			assertThat(10 - i).isEqualTo(list.values[i]);
		}
	}
}
