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

import static org.junit.Assert.assertEquals;

public class MoveRaterTest {

  //	@Test
  public void testRate() {
    MoveRater rater = new MoveRater(new HistoryTable());

    MoveList list = new MoveList();

    // Pawn -> Rook
    int move1 = Move.createMove(Move.NORMAL, 16, 0, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move1;

    // Knight -> Rook
    int move2 = Move.createMove(Move.NORMAL, 1, 0, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move2;

    // Pawn -> Knight
    int move3 = Move.createMove(Move.NORMAL, 16, 1, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move3;

    // Rook -> Knight
    int move4 = Move.createMove(Move.NORMAL, 0, 1, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move4;

    // Rook -> Rook
    int move5 = Move.createMove(Move.NORMAL, 0, 7, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move5;

    // King -> Empty
    int move6 = Move.createMove(Move.NORMAL, 4, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move6;

    // Pawn -> Pawn
    int move7 = Move.createMove(Move.NORMAL, 16, 17, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move7;

    // Pawn -> Empty
    int move8 = Move.createMove(Move.NORMAL, 16, 32, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    list.moves[list.tail++] = move8;

    rater.rateFromMVVLVA(list);
    list.sort();

    assertEquals(move1, list.moves[0]);
    assertEquals(move2, list.moves[1]);
    assertEquals(move5, list.moves[2]);
    assertEquals(move3, list.moves[3]);
    assertEquals(move4, list.moves[4]);
    assertEquals(move7, list.moves[5]);
    assertEquals(move8, list.moves[6]);
    assertEquals(move6, list.moves[7]);
  }

}
