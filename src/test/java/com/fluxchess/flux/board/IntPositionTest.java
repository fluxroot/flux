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
package com.fluxchess.flux.board;

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntPositionTest {

	@Test
	public void testIntPosition() {
		assertEquals(0, IntPosition.valueOfPosition(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1)));
		assertEquals(119, IntPosition.valueOfPosition(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8)));

		assertEquals(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1), IntPosition.valueOfIntPosition(0));
		assertEquals(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8), IntPosition.valueOfIntPosition(119));

		int[] rank1 = { IntPosition.a1, IntPosition.b1, IntPosition.c1, IntPosition.d1, IntPosition.e1, IntPosition.f1, IntPosition.g1, IntPosition.h1 };
		for (int position : rank1) {
			assertEquals(IntPosition.rank1, IntPosition.getRank(position));
			assertEquals(IntPosition.rank1, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank8, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank2 = { IntPosition.a2, IntPosition.b2, IntPosition.c2, IntPosition.d2, IntPosition.e2, IntPosition.f2, IntPosition.g2, IntPosition.h2 };
		for (int position : rank2) {
			assertEquals(IntPosition.rank2, IntPosition.getRank(position));
			assertEquals(IntPosition.rank2, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank7, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank3 = { IntPosition.a3, IntPosition.b3, IntPosition.c3, IntPosition.d3, IntPosition.e3, IntPosition.f3, IntPosition.g3, IntPosition.h3 };
		for (int position : rank3) {
			assertEquals(IntPosition.rank3, IntPosition.getRank(position));
			assertEquals(IntPosition.rank3, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank6, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank4 = { IntPosition.a4, IntPosition.b4, IntPosition.c4, IntPosition.d4, IntPosition.e4, IntPosition.f4, IntPosition.g4, IntPosition.h4 };
		for (int position : rank4) {
			assertEquals(IntPosition.rank4, IntPosition.getRank(position));
			assertEquals(IntPosition.rank4, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank5, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank5 = { IntPosition.a5, IntPosition.b5, IntPosition.c5, IntPosition.d5, IntPosition.e5, IntPosition.f5, IntPosition.g5, IntPosition.h5 };
		for (int position : rank5) {
			assertEquals(IntPosition.rank5, IntPosition.getRank(position));
			assertEquals(IntPosition.rank5, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank4, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank6 = { IntPosition.a6, IntPosition.b6, IntPosition.c6, IntPosition.d6, IntPosition.e6, IntPosition.f6, IntPosition.g6, IntPosition.h6 };
		for (int position : rank6) {
			assertEquals(IntPosition.rank6, IntPosition.getRank(position));
			assertEquals(IntPosition.rank6, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank3, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank7 = { IntPosition.a7, IntPosition.b7, IntPosition.c7, IntPosition.d7, IntPosition.e7, IntPosition.f7, IntPosition.g7, IntPosition.h7 };
		for (int position : rank7) {
			assertEquals(IntPosition.rank7, IntPosition.getRank(position));
			assertEquals(IntPosition.rank7, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank2, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}
		int[] rank8 = { IntPosition.a8, IntPosition.b8, IntPosition.c8, IntPosition.d8, IntPosition.e8, IntPosition.f8, IntPosition.g8, IntPosition.h8 };
		for (int position : rank8) {
			assertEquals(IntPosition.rank8, IntPosition.getRank(position));
			assertEquals(IntPosition.rank8, IntPosition.getRelativeRank(position, IntColor.WHITE));
			assertEquals(IntPosition.rank1, IntPosition.getRelativeRank(position, IntColor.BLACK));
		}

		int[] fileA = { IntPosition.a1, IntPosition.a2, IntPosition.a3, IntPosition.a4, IntPosition.a5, IntPosition.a6, IntPosition.a7, IntPosition.a8 };
		for (int position : fileA) {
			assertEquals(IntPosition.fileA, IntPosition.getFile(position));
		}
		int[] fileB = { IntPosition.b1, IntPosition.b2, IntPosition.b3, IntPosition.b4, IntPosition.b5, IntPosition.b6, IntPosition.b7, IntPosition.b8 };
		for (int position : fileB) {
			assertEquals(IntPosition.fileB, IntPosition.getFile(position));
		}
		int[] fileC = { IntPosition.c1, IntPosition.c2, IntPosition.c3, IntPosition.c4, IntPosition.c5, IntPosition.c6, IntPosition.c7, IntPosition.c8 };
		for (int position : fileC) {
			assertEquals(IntPosition.fileC, IntPosition.getFile(position));
		}
		int[] fileD = { IntPosition.d1, IntPosition.d2, IntPosition.d3, IntPosition.d4, IntPosition.d5, IntPosition.d6, IntPosition.d7, IntPosition.d8 };
		for (int position : fileD) {
			assertEquals(IntPosition.fileD, IntPosition.getFile(position));
		}
		int[] fileE = { IntPosition.e1, IntPosition.e2, IntPosition.e3, IntPosition.e4, IntPosition.e5, IntPosition.e6, IntPosition.e7, IntPosition.e8 };
		for (int position : fileE) {
			assertEquals(IntPosition.fileE, IntPosition.getFile(position));
		}
		int[] fileF = { IntPosition.f1, IntPosition.f2, IntPosition.f3, IntPosition.f4, IntPosition.f5, IntPosition.f6, IntPosition.f7, IntPosition.f8 };
		for (int position : fileF) {
			assertEquals(IntPosition.fileF, IntPosition.getFile(position));
		}
		int[] fileG = { IntPosition.g1, IntPosition.g2, IntPosition.g3, IntPosition.g4, IntPosition.g5, IntPosition.g6, IntPosition.g7, IntPosition.g8 };
		for (int position : fileG) {
			assertEquals(IntPosition.fileG, IntPosition.getFile(position));
		}
		int[] fileH = { IntPosition.h1, IntPosition.h2, IntPosition.h3, IntPosition.h4, IntPosition.h5, IntPosition.h6, IntPosition.h7, IntPosition.h8 };
		for (int position : fileH) {
			assertEquals(IntPosition.fileH, IntPosition.getFile(position));
		}
	}

}
