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

import com.fluxchess.jcpi.models.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveTest {

  @Test
  public void testIntMove() {
    int move = Move.valueOf(Move.Type.NULL, Square.NOSQUARE, Square.NOSQUARE, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    assertEquals(Move.Type.NULL, Move.getType(move));
//      assertEquals(Square.NOSQUARE, Move.getOriginSquare(move));
//      assertEquals(Square.NOSQUARE, Move.getTargetSquare(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getOriginChessman(move));
//      assertEquals(IntColor.NOCOLOR, Move.getOriginColor(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getTargetChessman(move));
//      assertEquals(IntColor.NOCOLOR, Move.getTargetColor(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getPromotion(move));

    move = Move.valueOf(Move.Type.NORMAL, 0, 16, IntPiece.WHITEPAWN, IntPiece.BLACKQUEEN, IntChessman.NOCHESSMAN);
    assertEquals(Move.Type.NORMAL, Move.getType(move));
    assertEquals(0, Move.getOriginSquare(move));
    assertEquals(16, Move.getTargetSquare(move));
    assertEquals(IntPiece.WHITEPAWN, Move.getOriginPiece(move));
    assertEquals(IntPiece.BLACKQUEEN, Move.getTargetPiece(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getPromotion(move));

    move = Move.valueOf(Move.Type.NORMAL, 0, 16, IntPiece.WHITEROOK, IntPiece.NOPIECE, IntChessman.QUEEN);
    assertEquals(IntChessman.QUEEN, Move.getPromotion(move));

    move = Move.valueOf(Move.Type.NORMAL, 0, 16, IntPiece.WHITEROOK, IntPiece.NOPIECE, IntChessman.QUEEN);
    int move2 = Move.valueOf(Move.Type.NORMAL, 0, 16, IntPiece.WHITEROOK, IntPiece.NOPIECE, IntChessman.QUEEN);
    assertEquals(move, move2);

    GenericMove genericMove = new GenericMove(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R2), GenericPosition.valueOf(GenericFile.Fa, GenericRank.R4));
    move = Move.valueOf(genericMove, new Board(new GenericBoard(GenericBoard.STANDARDSETUP)));
    assertEquals(Move.Type.PAWNDOUBLE, Move.getType(move));
    assertEquals(16, Move.getOriginSquare(move));
    assertEquals(48, Move.getTargetSquare(move));
    assertEquals(IntChessman.PAWN, IntPiece.getChessman(Move.getOriginPiece(move)));
    assertEquals(IntColor.WHITE, IntPiece.getColor(Move.getOriginPiece(move)));
    assertEquals(IntPiece.WHITEPAWN, Move.getOriginPiece(move));
    assertEquals(IntPiece.NOPIECE, Move.getTargetPiece(move));
  }

  @Test
  public void testSetEndPosition() {
    int move = Move.valueOf(Move.Type.NORMAL, Square.a2, Square.a3, IntPiece.WHITEPAWN, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    assertEquals(Square.a3, Move.getTargetSquare(move));

    move = Move.setTargetSquare(move, Square.a4);
    assertEquals(Square.a4, Move.getTargetSquare(move));
  }

}
