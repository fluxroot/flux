/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.board;

/**
 * Hex88BoardStackEntry
 *
 * @author Phokham Nonava
 */
public final class Hex88BoardStackEntry {

	public long zobristHistory = 0;
	public long pawnZobristHistory = 0;
	public int halfMoveClockHistory = 0;
	public int enPassantHistory = 0;
	public int captureSquareHistory = 0;

	public Hex88BoardStackEntry() {
		clear();
	}

	public Hex88BoardStackEntry(Hex88BoardStackEntry entry) {
		assert entry != null;
		
		this.zobristHistory = entry.zobristHistory;
		this.pawnZobristHistory = entry.pawnZobristHistory;
		this.halfMoveClockHistory = entry.halfMoveClockHistory;
		this.enPassantHistory = entry.enPassantHistory;
		this.captureSquareHistory = entry.captureSquareHistory;
	}
	
	public void clear() {
		this.zobristHistory = 0;
		this.pawnZobristHistory = 0;
		this.halfMoveClockHistory = 0;
		this.enPassantHistory = 0;
		this.captureSquareHistory = 0;
	}

}
