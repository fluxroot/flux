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
package com.fluxchess.flux.move;

import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.table.HistoryTable;

public final class MoveRater {

	private static HistoryTable historyTable;
	
	/**
	 * Creates a new MoveRater.
	 * 
	 * @param newBoard the board.
	 * @param newHistoryTable the history table.
	 */
	public MoveRater(HistoryTable newHistoryTable) {
		historyTable = newHistoryTable;
	}
	
	public void rateEvasion(MoveList moveList, int transpositionMove, int primaryKillerMove, int secondaryKillerMove) {
		for (int i = moveList.head; i < moveList.tail; i++) {
			int move = moveList.move[i];

			if (move == transpositionMove) {
				moveList.value[i] = Integer.MAX_VALUE;
			} else if (IntMove.getTarget(move) != IntChessman.NOPIECE) {
				moveList.value[i] = getMVVLVARating(move);
			} else if (move == primaryKillerMove) {
				moveList.value[i] = 0;
			} else if (move == secondaryKillerMove) {
				moveList.value[i] = -1;
			} else {
				// -2 because of the secondary killer move
				moveList.value[i] = historyTable.get(moveList.move[i]) - HistoryTable.MAX_HISTORYVALUE - 2;
			}
		}
	}
	
	/**
	 * Rates the move list according to the history table.
	 * 
	 * @param moveList the move list.
	 */
	public void rateFromHistory(MoveList moveList) {
		for (int i = moveList.head; i < moveList.tail; i++) {
			moveList.value[i] = historyTable.get(moveList.move[i]);
		}
	}

	/**
	 * Rates the move according to SEE.
	 * 
	 * @param moveList the move list.
	 */
	public void rateFromSEE(MoveList moveList) {
		for (int i = moveList.head; i < moveList.tail; i++) {
			moveList.value[i] = MoveSee.seeMove(moveList.move[i], IntMove.getChessmanColor(moveList.move[i]));
		}
	}

	/**
	 * Rates the move according to the MVV/LVA.
	 * 
	 * @param moveList the move list.
	 */
	public void rateFromMVVLVA(MoveList moveList) {
		for (int i = moveList.head; i < moveList.tail; i++) {
			moveList.value[i] = getMVVLVARating(moveList.move[i]);
		}
	}

	private int getMVVLVARating(int move) {
		int value = 0;

		int chessman = IntMove.getChessman(move);
		int target = IntMove.getTarget(move);

		value += IntChessman.VALUE_KING / IntChessman.getValueFromChessman(chessman);
		if (target != IntChessman.NOPIECE) {
			value += 10 * IntChessman.getValueFromChessman(target);
		}
		
		assert value >= (IntChessman.VALUE_KING / IntChessman.VALUE_KING) && value <= (IntChessman.VALUE_KING / IntChessman.VALUE_PAWN) + 10 * IntChessman.VALUE_QUEEN;
		
		return value;
	}

}
