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

public final class Hex88BoardStackEntry {

	public long zobristHistory = 0;
	public long pawnZobristHistory = 0;
	public int halfMoveClockHistory = 0;
	public int enPassantHistory = 0;
	public int captureSquareHistory = 0;
	public final int[] positionValueOpening = new int[IntColor.ARRAY_DIMENSION];
	public final int[] positionValueEndgame = new int[IntColor.ARRAY_DIMENSION];

	public Hex88BoardStackEntry() {
		clear();
	}

	public void clear() {
		this.zobristHistory = 0;
		this.pawnZobristHistory = 0;
		this.halfMoveClockHistory = 0;
		this.enPassantHistory = 0;
		this.captureSquareHistory = 0;
		for (int color : IntColor.values) {
			this.positionValueOpening[color] = 0;
			this.positionValueEndgame[color] = 0;
		}
	}

}
