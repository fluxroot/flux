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

import com.fluxchess.jcpi.models.GenericChessman;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PieceTest {

	@Test
	void testIntChessman() {
		assertThat(Piece.valueOfChessman(GenericChessman.KING)).isEqualTo(PieceType.KING);
		assertThat(Piece.valueOfIntChessman(PieceType.KING)).isEqualTo(GenericChessman.KING);

		int piece = Piece.createPiece(PieceType.PAWN, Color.WHITE);

		assertThat(PieceType.PAWN).isEqualTo(Piece.getChessman(piece));
		assertThat(Color.WHITE).isEqualTo(Piece.getColor(piece));
		assertThat(Color.BLACK).isEqualTo(Piece.getColorOpposite(piece));
		assertThat(Piece.VALUE_PAWN).isEqualTo(Piece.getValueFromPiece(piece));
		assertThat(Piece.isSliding(piece)).isFalse();

		piece = Piece.createPiece(PieceType.ROOK, Color.BLACK);

		assertThat(PieceType.ROOK).isEqualTo(Piece.getChessman(piece));
		assertThat(Color.BLACK).isEqualTo(Piece.getColor(piece));
		assertThat(Color.WHITE).isEqualTo(Piece.getColorOpposite(piece));
		assertThat(500).isEqualTo(Piece.getValueFromPiece(piece));
		assertThat(Piece.isSliding(piece)).isTrue();

		piece = Piece.createPromotion(PieceType.QUEEN, Color.BLACK);

		assertThat(PieceType.QUEEN).isEqualTo(Piece.getChessman(piece));
		assertThat(Color.BLACK).isEqualTo(Piece.getColor(piece));
		assertThat(Color.WHITE).isEqualTo(Piece.getColorOpposite(piece));
		assertThat(Piece.VALUE_QUEEN).isEqualTo(Piece.getValueFromPiece(piece));
		assertThat(Piece.isSliding(piece)).isTrue();

		piece = Piece.createPiece(PieceType.KNIGHT, Color.BLACK);

		assertThat(PieceType.KNIGHT).isEqualTo(Piece.getChessman(piece));
		assertThat(Color.BLACK).isEqualTo(Piece.getColor(piece));
		assertThat(Color.WHITE).isEqualTo(Piece.getColorOpposite(piece));
		assertThat(Piece.VALUE_KNIGHT).isEqualTo(Piece.getValueFromPiece(piece));
		assertThat(Piece.isSliding(piece)).isFalse();
	}
}
