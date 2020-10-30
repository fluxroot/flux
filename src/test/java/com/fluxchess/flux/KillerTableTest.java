/*
 * Copyright 2007-2020 Phokham Nonava
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KillerTableTest {

	@Test
	void testKillerMoves() {
		KillerTable killerMoves = new KillerTable();

		// Put a new killer move into the list
		int move1 = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		killerMoves.add(move1, 0);
		assertThat(move1).isEqualTo(killerMoves.getPrimaryKiller(0));
		assertThat(Move.NOMOVE).isEqualTo(killerMoves.getSecondaryKiller(0));

		// Put the same killer move into the list
		int move2 = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		killerMoves.add(move2, 0);
		assertThat(move2).isEqualTo(killerMoves.getPrimaryKiller(0));
		assertThat(Move.NOMOVE).isEqualTo(killerMoves.getSecondaryKiller(0));

		// Put a new killer move into the list
		int move3 = Move.createMove(MoveType.NORMAL, 1, 17, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		killerMoves.add(move3, 0);
		assertThat(move3).isEqualTo(killerMoves.getPrimaryKiller(0));
		assertThat(move2).isEqualTo(killerMoves.getSecondaryKiller(0));

		// Put a new killer move into the list twice
		int move4 = Move.createMove(MoveType.NORMAL, 2, 18, Piece.NOPIECE, Piece.NOPIECE, Piece.NOPIECE);
		killerMoves.add(move4, 0);
		killerMoves.add(move4, 0);
		assertThat(move4).isEqualTo(killerMoves.getPrimaryKiller(0));
		assertThat(move3).isEqualTo(killerMoves.getSecondaryKiller(0));
	}
}
