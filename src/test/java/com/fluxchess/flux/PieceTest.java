/*
 * Copyright (C) 2007-2014 Phokham Nonava
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

import com.fluxchess.jcpi.models.GenericChessman;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class PieceTest {

  @Test
  public void testIntChessman() {
    Assert.assertEquals(Piece.valueOfChessman(GenericChessman.KING), PieceType.KING);
    assertEquals(Piece.valueOfIntChessman(PieceType.KING), GenericChessman.KING);

    int piece = Piece.createPiece(PieceType.PAWN, Color.WHITE);

    assertEquals(PieceType.PAWN, Piece.getChessman(piece));
    assertEquals(Color.WHITE, Piece.getColor(piece));
    assertEquals(Color.BLACK, Piece.getColorOpposite(piece));
    assertEquals(Piece.VALUE_PAWN, Piece.getValueFromPiece(piece));
    assertFalse(Piece.isSliding(piece));

    piece = Piece.createPiece(PieceType.ROOK, Color.BLACK);

    assertEquals(PieceType.ROOK, Piece.getChessman(piece));
    assertEquals(Color.BLACK, Piece.getColor(piece));
    assertEquals(Color.WHITE, Piece.getColorOpposite(piece));
    assertEquals(500, Piece.getValueFromPiece(piece));
    assertTrue(Piece.isSliding(piece));

    piece = Piece.createPromotion(PieceType.QUEEN, Color.BLACK);

    assertEquals(PieceType.QUEEN, Piece.getChessman(piece));
    assertEquals(Color.BLACK, Piece.getColor(piece));
    assertEquals(Color.WHITE, Piece.getColorOpposite(piece));
    assertEquals(Piece.VALUE_QUEEN, Piece.getValueFromPiece(piece));
    assertTrue(Piece.isSliding(piece));

    piece = Piece.createPiece(PieceType.KNIGHT, Color.BLACK);

    assertEquals(PieceType.KNIGHT, Piece.getChessman(piece));
    assertEquals(Color.BLACK, Piece.getColor(piece));
    assertEquals(Color.WHITE, Piece.getColorOpposite(piece));
    assertEquals(Piece.VALUE_KNIGHT, Piece.getValueFromPiece(piece));
    assertFalse(Piece.isSliding(piece));
  }

}
