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

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquareTest {

	@Test
	void testIntPosition() {
		assertThat(0).isEqualTo(Square.valueOfPosition(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1)));
		assertThat(119).isEqualTo(Square.valueOfPosition(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8)));

		assertThat(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1)).isEqualTo(Square.valueOfIntPosition(0));
		assertThat(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8)).isEqualTo(Square.valueOfIntPosition(119));

		int[] rank1 = {Square.a1, Square.b1, Square.c1, Square.d1, Square.e1, Square.f1, Square.g1, Square.h1};
		for (int position : rank1) {
			assertThat(Rank.r1).isEqualTo(Square.getRank(position));
			assertThat(Rank.r1).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r8).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank2 = {Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2};
		for (int position : rank2) {
			assertThat(Rank.r2).isEqualTo(Square.getRank(position));
			assertThat(Rank.r2).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r7).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank3 = {Square.a3, Square.b3, Square.c3, Square.d3, Square.e3, Square.f3, Square.g3, Square.h3};
		for (int position : rank3) {
			assertThat(Rank.r3).isEqualTo(Square.getRank(position));
			assertThat(Rank.r3).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r6).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank4 = {Square.a4, Square.b4, Square.c4, Square.d4, Square.e4, Square.f4, Square.g4, Square.h4};
		for (int position : rank4) {
			assertThat(Rank.r4).isEqualTo(Square.getRank(position));
			assertThat(Rank.r4).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r5).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank5 = {Square.a5, Square.b5, Square.c5, Square.d5, Square.e5, Square.f5, Square.g5, Square.h5};
		for (int position : rank5) {
			assertThat(Rank.r5).isEqualTo(Square.getRank(position));
			assertThat(Rank.r5).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r4).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank6 = {Square.a6, Square.b6, Square.c6, Square.d6, Square.e6, Square.f6, Square.g6, Square.h6};
		for (int position : rank6) {
			assertThat(Rank.r6).isEqualTo(Square.getRank(position));
			assertThat(Rank.r6).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r3).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank7 = {Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7};
		for (int position : rank7) {
			assertThat(Rank.r7).isEqualTo(Square.getRank(position));
			assertThat(Rank.r7).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r2).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}
		int[] rank8 = {Square.a8, Square.b8, Square.c8, Square.d8, Square.e8, Square.f8, Square.g8, Square.h8};
		for (int position : rank8) {
			assertThat(Rank.r8).isEqualTo(Square.getRank(position));
			assertThat(Rank.r8).isEqualTo(Square.getRelativeRank(position, Color.WHITE));
			assertThat(Rank.r1).isEqualTo(Square.getRelativeRank(position, Color.BLACK));
		}

		int[] fileA = {Square.a1, Square.a2, Square.a3, Square.a4, Square.a5, Square.a6, Square.a7, Square.a8};
		for (int position : fileA) {
			assertThat(File.a).isEqualTo(Square.getFile(position));
		}
		int[] fileB = {Square.b1, Square.b2, Square.b3, Square.b4, Square.b5, Square.b6, Square.b7, Square.b8};
		for (int position : fileB) {
			assertThat(File.b).isEqualTo(Square.getFile(position));
		}
		int[] fileC = {Square.c1, Square.c2, Square.c3, Square.c4, Square.c5, Square.c6, Square.c7, Square.c8};
		for (int position : fileC) {
			assertThat(File.c).isEqualTo(Square.getFile(position));
		}
		int[] fileD = {Square.d1, Square.d2, Square.d3, Square.d4, Square.d5, Square.d6, Square.d7, Square.d8};
		for (int position : fileD) {
			assertThat(File.d).isEqualTo(Square.getFile(position));
		}
		int[] fileE = {Square.e1, Square.e2, Square.e3, Square.e4, Square.e5, Square.e6, Square.e7, Square.e8};
		for (int position : fileE) {
			assertThat(File.e).isEqualTo(Square.getFile(position));
		}
		int[] fileF = {Square.f1, Square.f2, Square.f3, Square.f4, Square.f5, Square.f6, Square.f7, Square.f8};
		for (int position : fileF) {
			assertThat(File.f).isEqualTo(Square.getFile(position));
		}
		int[] fileG = {Square.g1, Square.g2, Square.g3, Square.g4, Square.g5, Square.g6, Square.g7, Square.g8};
		for (int position : fileG) {
			assertThat(File.g).isEqualTo(Square.getFile(position));
		}
		int[] fileH = {Square.h1, Square.h2, Square.h3, Square.h4, Square.h5, Square.h6, Square.h7, Square.h8};
		for (int position : fileH) {
			assertThat(File.h).isEqualTo(Square.getFile(position));
		}
	}
}
