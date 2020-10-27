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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieceSquareTableTest {

	@Test
	public void testGetPositionValue() {
		for (int phase : GamePhase.values) {
			for (GenericChessman chessman : GenericChessman.values()) {
				for (int position = 0; position < Position.BOARDSIZE; position++) {
					if ((position & 0x88) == 0) {
						assertEquals(PieceSquareTable.getValue(phase, Piece.valueOfChessman(chessman), Color.WHITE, position), PieceSquareTable.getValue(phase, Piece.valueOfChessman(chessman), Color.BLACK, 119 - position));
					} else {
						position += 7;
					}
				}
			}
		}
	}
}
