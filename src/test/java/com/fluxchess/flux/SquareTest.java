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
package com.fluxchess.flux;

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SquareTest {

  @Test
  public void testIntPosition() {
    Assert.assertEquals(0, Square.valueOfPosition(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1)));
    assertEquals(119, Square.valueOfPosition(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8)));

    assertEquals(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R1), Square.valueOfIntPosition(0));
    assertEquals(GenericPosition.valueOf(GenericFile.Fh, GenericRank.R8), Square.valueOfIntPosition(119));

    int[] rank1 = {Square.a1, Square.b1, Square.c1, Square.d1, Square.e1, Square.f1, Square.g1, Square.h1};
    for (int position : rank1) {
      assertEquals(Square.rank1, Square.getRank(position));
      assertEquals(Square.rank1, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank8, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank2 = {Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2};
    for (int position : rank2) {
      assertEquals(Square.rank2, Square.getRank(position));
      assertEquals(Square.rank2, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank7, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank3 = {Square.a3, Square.b3, Square.c3, Square.d3, Square.e3, Square.f3, Square.g3, Square.h3};
    for (int position : rank3) {
      assertEquals(Square.rank3, Square.getRank(position));
      assertEquals(Square.rank3, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank6, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank4 = {Square.a4, Square.b4, Square.c4, Square.d4, Square.e4, Square.f4, Square.g4, Square.h4};
    for (int position : rank4) {
      assertEquals(Square.rank4, Square.getRank(position));
      assertEquals(Square.rank4, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank5, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank5 = {Square.a5, Square.b5, Square.c5, Square.d5, Square.e5, Square.f5, Square.g5, Square.h5};
    for (int position : rank5) {
      assertEquals(Square.rank5, Square.getRank(position));
      assertEquals(Square.rank5, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank4, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank6 = {Square.a6, Square.b6, Square.c6, Square.d6, Square.e6, Square.f6, Square.g6, Square.h6};
    for (int position : rank6) {
      assertEquals(Square.rank6, Square.getRank(position));
      assertEquals(Square.rank6, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank3, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank7 = {Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7};
    for (int position : rank7) {
      assertEquals(Square.rank7, Square.getRank(position));
      assertEquals(Square.rank7, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank2, Square.getRelativeRank(position, Color.BLACK));
    }
    int[] rank8 = {Square.a8, Square.b8, Square.c8, Square.d8, Square.e8, Square.f8, Square.g8, Square.h8};
    for (int position : rank8) {
      assertEquals(Square.rank8, Square.getRank(position));
      assertEquals(Square.rank8, Square.getRelativeRank(position, Color.WHITE));
      assertEquals(Square.rank1, Square.getRelativeRank(position, Color.BLACK));
    }

    int[] fileA = {Square.a1, Square.a2, Square.a3, Square.a4, Square.a5, Square.a6, Square.a7, Square.a8};
    for (int position : fileA) {
      assertEquals(Square.fileA, Square.getFile(position));
    }
    int[] fileB = {Square.b1, Square.b2, Square.b3, Square.b4, Square.b5, Square.b6, Square.b7, Square.b8};
    for (int position : fileB) {
      assertEquals(Square.fileB, Square.getFile(position));
    }
    int[] fileC = {Square.c1, Square.c2, Square.c3, Square.c4, Square.c5, Square.c6, Square.c7, Square.c8};
    for (int position : fileC) {
      assertEquals(Square.fileC, Square.getFile(position));
    }
    int[] fileD = {Square.d1, Square.d2, Square.d3, Square.d4, Square.d5, Square.d6, Square.d7, Square.d8};
    for (int position : fileD) {
      assertEquals(Square.fileD, Square.getFile(position));
    }
    int[] fileE = {Square.e1, Square.e2, Square.e3, Square.e4, Square.e5, Square.e6, Square.e7, Square.e8};
    for (int position : fileE) {
      assertEquals(Square.fileE, Square.getFile(position));
    }
    int[] fileF = {Square.f1, Square.f2, Square.f3, Square.f4, Square.f5, Square.f6, Square.f7, Square.f8};
    for (int position : fileF) {
      assertEquals(Square.fileF, Square.getFile(position));
    }
    int[] fileG = {Square.g1, Square.g2, Square.g3, Square.g4, Square.g5, Square.g6, Square.g7, Square.g8};
    for (int position : fileG) {
      assertEquals(Square.fileG, Square.getFile(position));
    }
    int[] fileH = {Square.h1, Square.h2, Square.h3, Square.h4, Square.h5, Square.h6, Square.h7, Square.h8};
    for (int position : fileH) {
      assertEquals(Square.fileH, Square.getFile(position));
    }
  }

}
