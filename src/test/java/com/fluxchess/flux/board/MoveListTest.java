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

import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntPiece;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveListTest {

  @Test
  public void testRemove() {
    MoveList moveList = new MoveList();

    moveList.newList();
    assertEquals(0, moveList.size());
    moveList.move[moveList.tail++] = 1;
    assertEquals(1, moveList.size());

    moveList.newList();
    assertEquals(0, moveList.size());
    moveList.move[moveList.tail++] = 1;
    moveList.move[moveList.tail++] = 1;
    moveList.move[moveList.tail++] = 1;
    assertEquals(3, moveList.size());

    moveList.deleteList();
    assertEquals(1, moveList.size());

    moveList.deleteList();
    assertEquals(0, moveList.size());
  }

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

    list.sort();

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

    list.sort();

    for (int i = 0; i < 10; i++) {
      assertEquals(10 - i, list.move[i]);
      assertEquals(10 - i, list.value[i]);
    }
  }

  //  @Test
  public void testRate() {
    MoveList list = new MoveList();

    // Pawn -> Rook
    int move1 = Move.valueOf(Move.Type.NORMAL, 16, 0, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move1;

    // Knight -> Rook
    int move2 = Move.valueOf(Move.Type.NORMAL, 1, 0, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move2;

    // Pawn -> Knight
    int move3 = Move.valueOf(Move.Type.NORMAL, 16, 1, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move3;

    // Rook -> Knight
    int move4 = Move.valueOf(Move.Type.NORMAL, 0, 1, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move4;

    // Rook -> Rook
    int move5 = Move.valueOf(Move.Type.NORMAL, 0, 7, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move5;

    // King -> Empty
    int move6 = Move.valueOf(Move.Type.NORMAL, 4, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move6;

    // Pawn -> Pawn
    int move7 = Move.valueOf(Move.Type.NORMAL, 16, 17, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move7;

    // Pawn -> Empty
    int move8 = Move.valueOf(Move.Type.NORMAL, 16, 32, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    list.move[list.tail++] = move8;

    list.rateFromMVVLVA();
    list.sort();

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
