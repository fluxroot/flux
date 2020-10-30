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

import com.fluxchess.jcpi.models.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoveTest {

	@Test
	void testIntMove() {
		int move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.createPiece(PieceType.QUEEN, Color.BLACK), Piece.NOPIECE);
		assertThat(MoveType.NORMAL).isEqualTo(Move.getType(move));
		assertThat(0).isEqualTo(Move.getStart(move));
		assertThat(16).isEqualTo(Move.getEnd(move));
		assertThat(PieceType.PAWN).isEqualTo(Move.getChessman(move));
		assertThat(Color.WHITE).isEqualTo(Move.getChessmanColor(move));
		assertThat(Piece.WHITE_PAWN).isEqualTo(Move.getChessmanPiece(move));
		assertThat(PieceType.QUEEN).isEqualTo(Move.getTarget(move));
		assertThat(Color.BLACK).isEqualTo(Move.getTargetColor(move));
		assertThat(Piece.BLACK_QUEEN).isEqualTo(Move.getTargetPiece(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));

		move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, PieceType.QUEEN);
		assertThat(PieceType.QUEEN).isEqualTo(Move.getPromotion(move));

		move = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, PieceType.QUEEN);
		int move2 = Move.createMove(MoveType.NORMAL, 0, 16, Piece.NOPIECE, Piece.NOPIECE, PieceType.QUEEN);
		assertThat(move).isEqualTo(move2);

		GenericMove commandMove = new GenericMove(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R2), GenericPosition.valueOf(GenericFile.Fa, GenericRank.R4));
		move = Move.convertMove(commandMove, new Position(new GenericBoard(GenericBoard.STANDARDSETUP)));
		assertThat(MoveType.PAWNDOUBLE).isEqualTo(Move.getType(move));
		assertThat(16).isEqualTo(Move.getStart(move));
		assertThat(48).isEqualTo(Move.getEnd(move));
		assertThat(PieceType.PAWN).isEqualTo(Move.getChessman(move));
		assertThat(Color.WHITE).isEqualTo(Move.getChessmanColor(move));
		assertThat(Piece.WHITE_PAWN).isEqualTo(Move.getChessmanPiece(move));
		assertThat(Piece.NOPIECE).isEqualTo(Move.getTarget(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getTargetColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));
	}

	@Test
	void testSetEndPosition() {
		int move = Move.createMove(MoveType.NORMAL, Square.a2, Square.a3, Piece.createPiece(PieceType.PAWN, Color.WHITE), Piece.NOPIECE, Piece.NOPIECE);
		assertThat(Square.a3).isEqualTo(Move.getEnd(move));

		move = Move.setEndPosition(move, Square.a4);
		assertThat(Square.a4).isEqualTo(Move.getEnd(move));
	}
}
