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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveSorterTest {

	@Test
	public void testSort() {
		MoveList list = new MoveList();
		
		list.move[list.tail] = 10;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 9;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 8;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 7;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 6;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 5;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 4;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 3;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 2;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 1;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;

		MoveSorter.sort(list);
		
		for (int i = 0; i < 10; i++) {
			assertEquals(10 - i, list.move[i]);
			assertEquals(10 - i, list.value[i]);
		}

		list = new MoveList();
		list.move[list.tail] = 3;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 4;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 6;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 1;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 10;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 9;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 2;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 8;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 5;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;
		list.move[list.tail] = 7;
		list.value[list.tail] = list.move[list.tail];
		list.tail++;

		MoveSorter.sort(list);
		
		for (int i = 0; i < 10; i++) {
			assertEquals(10 - i, list.move[i]);
			assertEquals(10 - i, list.value[i]);
		}
	}

}
