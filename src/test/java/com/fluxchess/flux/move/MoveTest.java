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

import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.jcpi.models.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveTest {

  @Test
  public void testIntMove() {
    int move = Move.createMove(Move.NULL, IntPosition.NOPOSITION, IntPosition.NOPOSITION, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
    assertEquals(Move.NULL, Move.getType(move));
//      assertEquals(IntPosition.NOPOSITION, Move.getStart(move));
//      assertEquals(IntPosition.NOPOSITION, Move.getEnd(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getChessman(move));
//      assertEquals(IntColor.NOCOLOR, Move.getChessmanColor(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getTarget(move));
//      assertEquals(IntColor.NOCOLOR, Move.getTargetColor(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getPromotion(move));

    move = Move.createMove(Move.NORMAL, 0, 16, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.createPiece(IntChessman.QUEEN, IntColor.BLACK), IntChessman.NOPIECE);
    assertEquals(Move.NORMAL, Move.getType(move));
    assertEquals(0, Move.getStart(move));
    assertEquals(16, Move.getEnd(move));
    assertEquals(IntChessman.PAWN, Move.getChessman(move));
    assertEquals(IntColor.WHITE, Move.getChessmanColor(move));
    assertEquals(IntChessman.WHITE_PAWN, Move.getChessmanPiece(move));
    assertEquals(IntChessman.QUEEN, Move.getTarget(move));
    assertEquals(IntColor.BLACK, Move.getTargetColor(move));
    assertEquals(IntChessman.BLACK_QUEEN, Move.getTargetPiece(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getPromotion(move));

    move = Move.createMove(Move.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
    assertEquals(IntChessman.QUEEN, Move.getPromotion(move));

    move = Move.createMove(Move.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
    int move2 = Move.createMove(Move.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
    assertEquals(move, move2);

    GenericMove genericMove = new GenericMove(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R2), GenericPosition.valueOf(GenericFile.Fa, GenericRank.R4));
    move = Move.convertMove(genericMove, new Board(new GenericBoard(GenericBoard.STANDARDSETUP)));
    assertEquals(Move.PAWNDOUBLE, Move.getType(move));
    assertEquals(16, Move.getStart(move));
    assertEquals(48, Move.getEnd(move));
    assertEquals(IntChessman.PAWN, Move.getChessman(move));
    assertEquals(IntColor.WHITE, Move.getChessmanColor(move));
    assertEquals(IntChessman.WHITE_PAWN, Move.getChessmanPiece(move));
    assertEquals(IntChessman.NOPIECE, Move.getTarget(move));
//      assertEquals(IntColor.NOCOLOR, Move.getTargetColor(move));
//      assertEquals(IntChessman.NOCHESSMAN, Move.getPromotion(move));
  }

  @Test
  public void testSetEndPosition() {
    int move = Move.createMove(Move.NORMAL, IntPosition.a2, IntPosition.a3, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
    assertEquals(IntPosition.a3, Move.getEnd(move));

    move = Move.setEndPosition(move, IntPosition.a4);
    assertEquals(IntPosition.a4, Move.getEnd(move));
  }

}
