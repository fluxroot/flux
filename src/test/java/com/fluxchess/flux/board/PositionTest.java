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
import com.fluxchess.jcpi.models.IntColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionTest {

  @Test
  public void testIntPosition() {
    assertEquals(0, Position.valueOfPosition(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1)));
    assertEquals(119, Position.valueOfPosition(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8)));

    assertEquals(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1), Position.valueOfIntPosition(0));
    assertEquals(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8), Position.valueOfIntPosition(119));

    int[] rank1 = {Position.a1, Position.b1, Position.c1, Position.d1, Position.e1, Position.f1, Position.g1, Position.h1};
    for (int position : rank1) {
      assertEquals(Position.rank1, Position.getRank(position));
      assertEquals(Position.rank1, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank8, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank2 = {Position.a2, Position.b2, Position.c2, Position.d2, Position.e2, Position.f2, Position.g2, Position.h2};
    for (int position : rank2) {
      assertEquals(Position.rank2, Position.getRank(position));
      assertEquals(Position.rank2, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank7, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank3 = {Position.a3, Position.b3, Position.c3, Position.d3, Position.e3, Position.f3, Position.g3, Position.h3};
    for (int position : rank3) {
      assertEquals(Position.rank3, Position.getRank(position));
      assertEquals(Position.rank3, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank6, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank4 = {Position.a4, Position.b4, Position.c4, Position.d4, Position.e4, Position.f4, Position.g4, Position.h4};
    for (int position : rank4) {
      assertEquals(Position.rank4, Position.getRank(position));
      assertEquals(Position.rank4, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank5, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank5 = {Position.a5, Position.b5, Position.c5, Position.d5, Position.e5, Position.f5, Position.g5, Position.h5};
    for (int position : rank5) {
      assertEquals(Position.rank5, Position.getRank(position));
      assertEquals(Position.rank5, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank4, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank6 = {Position.a6, Position.b6, Position.c6, Position.d6, Position.e6, Position.f6, Position.g6, Position.h6};
    for (int position : rank6) {
      assertEquals(Position.rank6, Position.getRank(position));
      assertEquals(Position.rank6, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank3, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank7 = {Position.a7, Position.b7, Position.c7, Position.d7, Position.e7, Position.f7, Position.g7, Position.h7};
    for (int position : rank7) {
      assertEquals(Position.rank7, Position.getRank(position));
      assertEquals(Position.rank7, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank2, Position.getRelativeRank(position, IntColor.BLACK));
    }
    int[] rank8 = {Position.a8, Position.b8, Position.c8, Position.d8, Position.e8, Position.f8, Position.g8, Position.h8};
    for (int position : rank8) {
      assertEquals(Position.rank8, Position.getRank(position));
      assertEquals(Position.rank8, Position.getRelativeRank(position, IntColor.WHITE));
      assertEquals(Position.rank1, Position.getRelativeRank(position, IntColor.BLACK));
    }

    int[] fileA = {Position.a1, Position.a2, Position.a3, Position.a4, Position.a5, Position.a6, Position.a7, Position.a8};
    for (int position : fileA) {
      assertEquals(Position.fileA, Position.getFile(position));
    }
    int[] fileB = {Position.b1, Position.b2, Position.b3, Position.b4, Position.b5, Position.b6, Position.b7, Position.b8};
    for (int position : fileB) {
      assertEquals(Position.fileB, Position.getFile(position));
    }
    int[] fileC = {Position.c1, Position.c2, Position.c3, Position.c4, Position.c5, Position.c6, Position.c7, Position.c8};
    for (int position : fileC) {
      assertEquals(Position.fileC, Position.getFile(position));
    }
    int[] fileD = {Position.d1, Position.d2, Position.d3, Position.d4, Position.d5, Position.d6, Position.d7, Position.d8};
    for (int position : fileD) {
      assertEquals(Position.fileD, Position.getFile(position));
    }
    int[] fileE = {Position.e1, Position.e2, Position.e3, Position.e4, Position.e5, Position.e6, Position.e7, Position.e8};
    for (int position : fileE) {
      assertEquals(Position.fileE, Position.getFile(position));
    }
    int[] fileF = {Position.f1, Position.f2, Position.f3, Position.f4, Position.f5, Position.f6, Position.f7, Position.f8};
    for (int position : fileF) {
      assertEquals(Position.fileF, Position.getFile(position));
    }
    int[] fileG = {Position.g1, Position.g2, Position.g3, Position.g4, Position.g5, Position.g6, Position.g7, Position.g8};
    for (int position : fileG) {
      assertEquals(Position.fileG, Position.getFile(position));
    }
    int[] fileH = {Position.h1, Position.h2, Position.h3, Position.h4, Position.h5, Position.h6, Position.h7, Position.h8};
    for (int position : fileH) {
      assertEquals(Position.fileH, Position.getFile(position));
    }

    int[] darkSquares = {
      Position.a1, Position.c1, Position.e1, Position.g1,
      Position.b2, Position.d2, Position.f2, Position.h2,
      Position.a3, Position.c3, Position.e3, Position.g3,
      Position.b4, Position.d4, Position.f4, Position.h4,
      Position.a5, Position.c5, Position.e5, Position.g5,
      Position.b6, Position.d6, Position.f6, Position.h6,
      Position.a7, Position.c7, Position.e7, Position.g7,
      Position.b8, Position.d8, Position.f8, Position.h8
    };
    int[] lightSquares = {
      Position.b1, Position.d1, Position.f1, Position.h1,
      Position.a2, Position.c2, Position.e2, Position.g2,
      Position.b3, Position.d3, Position.f3, Position.h3,
      Position.a4, Position.c4, Position.e4, Position.g4,
      Position.b5, Position.d5, Position.f5, Position.h5,
      Position.a6, Position.c6, Position.e6, Position.g6,
      Position.b7, Position.d7, Position.f7, Position.h7,
      Position.a8, Position.c8, Position.e8, Position.g8
    };
    for (int position : darkSquares) {
      assertEquals(IntColor.BLACK, Position.getFieldColor(position));
    }
    for (int position : lightSquares) {
      assertEquals(IntColor.WHITE, Position.getFieldColor(position));
    }
  }

}
