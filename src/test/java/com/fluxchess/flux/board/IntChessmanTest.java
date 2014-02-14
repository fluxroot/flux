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

import com.fluxchess.jcpi.models.GenericChessman;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntChessmanTest {

	@Test
	public void testIntChessman() {
		assertEquals(IntChessman.valueOfChessman(GenericChessman.KING), IntChessman.KING);
		assertEquals(IntChessman.valueOfIntChessman(IntChessman.KING), GenericChessman.KING);
		
		int piece = IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE);

		assertEquals(IntChessman.PAWN, IntChessman.getChessman(piece));
		assertEquals(IntColor.WHITE, IntChessman.getColor(piece));
		assertEquals(IntColor.BLACK, IntChessman.getColorOpposite(piece));
		assertEquals(IntChessman.VALUE_PAWN, IntChessman.getValueFromPiece(piece));
		assertFalse(IntChessman.isSliding(piece));

		piece = IntChessman.createPiece(IntChessman.ROOK, IntColor.BLACK);
		
		assertEquals(IntChessman.ROOK, IntChessman.getChessman(piece));
		assertEquals(IntColor.BLACK, IntChessman.getColor(piece));
		assertEquals(IntColor.WHITE, IntChessman.getColorOpposite(piece));
		assertEquals(500, IntChessman.getValueFromPiece(piece));
		assertTrue(IntChessman.isSliding(piece));

		piece = IntChessman.createPromotion(IntChessman.QUEEN, IntColor.BLACK);
		
		assertEquals(IntChessman.QUEEN, IntChessman.getChessman(piece));
		assertEquals(IntColor.BLACK, IntChessman.getColor(piece));
		assertEquals(IntColor.WHITE, IntChessman.getColorOpposite(piece));
		assertEquals(IntChessman.VALUE_QUEEN, IntChessman.getValueFromPiece(piece));
		assertTrue(IntChessman.isSliding(piece));
		
		piece = IntChessman.createPiece(IntChessman.KNIGHT, IntColor.BLACK);
		
		assertEquals(IntChessman.KNIGHT, IntChessman.getChessman(piece));
		assertEquals(IntColor.BLACK, IntChessman.getColor(piece));
		assertEquals(IntColor.WHITE, IntChessman.getColorOpposite(piece));
		assertEquals(IntChessman.VALUE_KNIGHT, IntChessman.getValueFromPiece(piece));
		assertFalse(IntChessman.isSliding(piece));
	}

}
