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
package com.fluxchess.flux.evaluation;

import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntGamePhase;
import com.fluxchess.jcpi.models.GenericChessman;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionValuesTest {

	@Test
	public void testGetPositionValue() {
		for (int phase : IntGamePhase.values) {
			for (GenericChessman chessman : GenericChessman.values()) {
				for (int position = 0; position < Hex88Board.BOARDSIZE; position++) {
					if ((position & 0x88) == 0) {
						assertEquals(PositionValues.getPositionValue(phase, IntChessman.valueOfChessman(chessman), IntColor.WHITE, position), PositionValues.getPositionValue(phase, IntChessman.valueOfChessman(chessman), IntColor.BLACK, 119 - position));
					} else {
						position += 7;
					}
				}
			}
		}
	}

}
