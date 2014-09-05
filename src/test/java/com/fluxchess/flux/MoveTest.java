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

import com.fluxchess.jcpi.models.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoveTest {

  @Test
  public void testIntMove() {
    int move = Move.createMove(MoveType.NULL, Square.NOPOSITION, Square.NOPOSITION, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
    assertEquals(MoveType.NULL, Move.getType(move));
//		assertEquals(IntPosition.NOPOSITION, IntMove.getStart(move));
//		assertEquals(IntPosition.NOPOSITION, IntMove.getEnd(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getChessman(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getChessmanColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getTarget(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getTargetColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));

    move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.createPiece(Piece.PAWN, Color.WHITE), Piece.createPiece(Piece.QUEEN, Color.BLACK), Piece.NOPIECE);
    assertEquals(MoveType.NORMAL, Move.getType(move));
    assertEquals(0, Move.getStart(move));
    assertEquals(16, Move.getEnd(move));
    assertEquals(Piece.PAWN, Move.getChessman(move));
    assertEquals(Color.WHITE, Move.getChessmanColor(move));
    assertEquals(Piece.WHITE_PAWN, Move.getChessmanPiece(move));
    assertEquals(Piece.QUEEN, Move.getTarget(move));
    assertEquals(Color.BLACK, Move.getTargetColor(move));
    assertEquals(Piece.BLACK_QUEEN, Move.getTargetPiece(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));

    move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, Piece.QUEEN);
    assertEquals(Piece.QUEEN, Move.getPromotion(move));

    move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, Piece.QUEEN);
    int move2 = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, Piece.QUEEN);
    assertEquals(move, move2);

    GenericMove commandMove = new GenericMove(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R2), GenericPosition.valueOf(GenericFile.Fa, GenericRank.R4));
    move = Move.convertMove(commandMove, new Position(new GenericBoard(GenericBoard.STANDARDSETUP)));
    assertEquals(MoveType.PAWNDOUBLE, Move.getType(move));
    assertEquals(16, Move.getStart(move));
    assertEquals(48, Move.getEnd(move));
    assertEquals(Piece.PAWN, Move.getChessman(move));
    assertEquals(Color.WHITE, Move.getChessmanColor(move));
    assertEquals(Piece.WHITE_PAWN, Move.getChessmanPiece(move));
    assertEquals(Piece.NOPIECE, Move.getTarget(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getTargetColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));
  }

  @Test
  public void testSetEndPosition() {
    int move = Move.createMove(MoveType.NORMAL, Square.a2, Square.a3, Piece.createPiece(Piece.PAWN, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
    assertEquals(Square.a3, Move.getEnd(move));

    move = Move.setEndPosition(move, Square.a4);
    assertEquals(Square.a4, Move.getEnd(move));
  }

}
