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
package com.fluxchess.move;

import static org.junit.Assert.assertEquals;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

import org.junit.Test;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntChessman;
import com.fluxchess.board.IntColor;
import com.fluxchess.board.IntPosition;
import com.fluxchess.move.IntMove;

/**
 * IntMoveTest
 *
 * @author Phokham Nonava
 */
public class IntMoveTest {

	@Test
	public void testIntMove() {
		int move = IntMove.createMove(IntMove.NULL, IntPosition.NOPOSITION, IntPosition.NOPOSITION, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
		assertEquals(IntMove.NULL, IntMove.getType(move));
//		assertEquals(IntPosition.NOPOSITION, IntMove.getStart(move));
//		assertEquals(IntPosition.NOPOSITION, IntMove.getEnd(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getChessman(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getChessmanColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getTarget(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getTargetColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));

		move = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.createPiece(IntChessman.QUEEN, IntColor.BLACK), IntChessman.NOPIECE);
		assertEquals(IntMove.NORMAL, IntMove.getType(move));
		assertEquals(0, IntMove.getStart(move));
		assertEquals(16, IntMove.getEnd(move));
		assertEquals(IntChessman.PAWN, IntMove.getChessman(move));
		assertEquals(IntColor.WHITE, IntMove.getChessmanColor(move));
		assertEquals(IntChessman.WHITE_PAWN, IntMove.getChessmanPiece(move));
		assertEquals(IntChessman.QUEEN, IntMove.getTarget(move));
		assertEquals(IntColor.BLACK, IntMove.getTargetColor(move));
		assertEquals(IntChessman.BLACK_QUEEN, IntMove.getTargetPiece(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));

		move = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
		assertEquals(IntChessman.QUEEN, IntMove.getPromotion(move));

		move = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
		int move2 = IntMove.createMove(IntMove.NORMAL, 0, 16, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.QUEEN);
		assertEquals(move, move2);

		GenericMove commandMove = new GenericMove(GenericPosition.valueOf(GenericFile.Fa, GenericRank.R2), GenericPosition.valueOf(GenericFile.Fa, GenericRank.R4));
		move = IntMove.convertMove(commandMove, new Hex88Board(new GenericBoard(GenericBoard.STANDARDSETUP)));
		assertEquals(IntMove.PAWNDOUBLE, IntMove.getType(move));
		assertEquals(16, IntMove.getStart(move));
		assertEquals(48, IntMove.getEnd(move));
		assertEquals(IntChessman.PAWN, IntMove.getChessman(move));
		assertEquals(IntColor.WHITE, IntMove.getChessmanColor(move));
		assertEquals(IntChessman.WHITE_PAWN, IntMove.getChessmanPiece(move));
		assertEquals(IntChessman.NOPIECE, IntMove.getTarget(move));
//		assertEquals(IntColor.NOCOLOR, IntMove.getTargetColor(move));
//		assertEquals(IntChessman.NOCHESSMAN, IntMove.getPromotion(move));
	}

	@Test
	public void testSetEndPosition() {
		int move = IntMove.createMove(IntMove.NORMAL, IntPosition.a2, IntPosition.a3, IntChessman.createPiece(IntChessman.PAWN, IntColor.WHITE), IntChessman.NOPIECE, IntChessman.NOPIECE);
		assertEquals(IntPosition.a3, IntMove.getEnd(move));
		
		move = IntMove.setEndPosition(move, IntPosition.a4);
		assertEquals(IntPosition.a4, IntMove.getEnd(move));
	}

}
