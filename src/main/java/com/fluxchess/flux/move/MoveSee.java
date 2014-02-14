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

import com.fluxchess.flux.board.*;

/**
 * Notes: Ideas from Fruit
 */
public final class MoveSee {

	private static Hex88Board board = null;
	private static SeeList[] chessmanList = new SeeList[IntColor.ARRAY_DIMENSION];

	public MoveSee(Hex88Board newBoard) {
		board = newBoard;
		chessmanList[IntColor.WHITE] = new SeeList();
		chessmanList[IntColor.BLACK] = new SeeList();
	}

	public static int seeMove(int move, int myColor) {
		int start = IntMove.getStart(move);
		int end = IntMove.getEnd(move);
		int target = IntMove.getTarget(move);
		int type = IntMove.getType(move);
		
		// Get the enemy color
		int enemyColor = IntColor.switchColor(myColor);

		// Clear the chessman list
		SeeList myList = chessmanList[myColor];
		SeeList enemyList = chessmanList[enemyColor];
		myList.head = 0;
		myList.size = 0;
		enemyList.head = 0;
		enemyList.size = 0;
		
		// Get the attacker value
		int attackerValue = 0;
		if (type == IntMove.PAWNPROMOTION) {
			attackerValue = IntChessman.getValueFromChessman(IntMove.getPromotion(move));
		} else {
			attackerValue = IntChessman.getValueFromChessman(IntMove.getChessman(move));
		}

		// We have no target for now
		int value = 0;

		// Get the target value
		if (target != IntChessman.NOPIECE) {
			value = IntChessman.getValueFromChessman(target);
		}
		if (type == IntMove.PAWNPROMOTION) {
			value += IntChessman.getValueFromChessman(IntMove.getPromotion(move)) - IntChessman.VALUE_PAWN;
		}

		// Find all attackers
		addAllAttackers(myList, end, myColor);
		addAllAttackers(enemyList, end, enemyColor);

		// Find the attacker hiding behind the en-passant
		if (type == IntMove.ENPASSANT) {
			if (myColor == IntColor.WHITE) {
				addHiddenAttacker(end - 16, end);
			} else {
				assert myColor == IntColor.BLACK;

				addHiddenAttacker(end + 16, end);
			}
		}
		
		// Remove the chessman from the attackers
		int position = -1;
		for (int i = 0; i < myList.size; i++) {
			if (start == myList.position[i]) {
				position = i;
				break;
			}
		}
		if (position != -1) {
			for (int i = position; i < myList.size - 1; i++) {
				myList.chessman[i] = myList.chessman[i + 1];
				myList.position[i] = myList.position[i + 1];
			}
			myList.size--;
		}
		
		// Add the hidden attacker
		addHiddenAttacker(start, end);

		// Make the capture
		value -= makeCapture(end, enemyColor, myColor, attackerValue);
		
		return value;
	}

	private static int makeCapture(int targetPosition, int myColor, int enemyColor, int targetValue) {
		// Get the next attacker
		int attacker = chessmanList[myColor].chessman[0];
		int attackerPosition = chessmanList[myColor].position[0];
		if (!shiftAttacker(chessmanList[myColor])) {
			// We have no attacker left
			return 0;
		}

		// Set the target value
		int value = targetValue;

		// If we capture the king, we cannot go futher
		if (value == IntChessman.VALUE_KING) {
			return value;
		}

		// Get the attacker value
		int attackerValue = 0;
		int chessman = IntChessman.getChessman(attacker);
		if (chessman == IntChessman.PAWN
				&& ((targetPosition > 111 && myColor == IntColor.WHITE)
						|| (targetPosition < 8 && myColor == IntColor.BLACK))) {
			// Adjust on promotion
			value += IntChessman.VALUE_QUEEN - IntChessman.VALUE_PAWN;
			attackerValue = IntChessman.VALUE_QUEEN;
		} else {
			attackerValue = IntChessman.getValueFromChessman(chessman);
		}

		// Add the hidden attacker
		addHiddenAttacker(attackerPosition, targetPosition);
		
		value -= makeCapture(targetPosition, enemyColor, myColor, attackerValue);
		
		return value;
	}

	private static void addAllAttackers(SeeList list, int targetPosition, int myColor) {
		// Pawn attacks
		int sign = 1;
		int pawn = IntChessman.WHITE_PAWN;
		if (myColor == IntColor.BLACK) {
			sign = -1;
			pawn = IntChessman.BLACK_PAWN;
		} else {
			assert myColor == IntColor.WHITE;
		}
		int pawnPosition = targetPosition - sign * 15;
		if ((pawnPosition & 0x88) == 0 && Hex88Board.board[pawnPosition] == pawn) {
			list.chessman[list.size] = pawn;
			list.position[list.size] = pawnPosition;
			list.size++;
		}
		pawnPosition = targetPosition - sign * 17;
		if ((pawnPosition & 0x88) == 0 && Hex88Board.board[pawnPosition] == pawn) {
			list.chessman[list.size] = pawn;
			list.position[list.size] = pawnPosition;
			list.size++;
		}

		// Knight attacks
		PositionList tempPositionList = Hex88Board.knightList[myColor];
		for (int i = 0; i < tempPositionList.size; i++) {
			int position = tempPositionList.position[i];
			if (board.canAttack(IntChessman.KNIGHT, myColor, position, targetPosition)) {
				list.chessman[list.size] = Hex88Board.board[position];
				list.position[list.size] = position;
				list.size++;
			}
		}
		
		// Bishop attacks
		tempPositionList = Hex88Board.bishopList[myColor];
		for (int i = 0; i < tempPositionList.size; i++) {
			int position = tempPositionList.position[i];
			if (board.canAttack(IntChessman.BISHOP, myColor, position, targetPosition)) {
				int bishop = Hex88Board.board[position];
				if (hasHiddenAttacker(position, targetPosition)) {
					addAttacker(list, bishop, position, true);
				} else {
					addAttacker(list, bishop, position, false);
				}
			}
		}
		
		// Rook attacks
		tempPositionList = Hex88Board.rookList[myColor];
		for (int i = 0; i < tempPositionList.size; i++) {
			int position = tempPositionList.position[i];
			if (board.canAttack(IntChessman.ROOK, myColor, position, targetPosition)) {
				int rook = Hex88Board.board[position];
				if (hasHiddenAttacker(position, targetPosition)) {
					addAttacker(list, rook, position, true);
				} else {
					addAttacker(list, rook, position, false);
				}
			}
		}
		
		// Queen attacks
		tempPositionList = Hex88Board.queenList[myColor];
		for (int i = 0; i < tempPositionList.size; i++) {
			int position = tempPositionList.position[i];
			if (board.canAttack(IntChessman.QUEEN, myColor, position, targetPosition)) {
				int queen = Hex88Board.board[position];
				if (hasHiddenAttacker(position, targetPosition)) {
					addAttacker(list, queen, position, true);
				} else {
					addAttacker(list, queen, position, false);
				}
			}
		}
		
		// King attacks
		assert Hex88Board.kingList[myColor].size == 1;
		int position = Hex88Board.kingList[myColor].position[0];
		if (board.canAttack(IntChessman.KING, myColor, position, targetPosition)) {
			list.chessman[list.size] = Hex88Board.board[position];
			list.position[list.size] = position;
			list.size++;
		}
	}

	private static void addHiddenAttacker(int chessmanPosition, int targetPosition) {
		int vector = AttackVector.vector[targetPosition - chessmanPosition + 127];
		if (vector == AttackVector.N || vector == AttackVector.K) {
			// No line
			return;
		}

		// Get the reverse delta
		int delta = AttackVector.delta[chessmanPosition - targetPosition + 127];

		// Find the hidden attacker
		int attackerPosition = chessmanPosition + delta;
		while ((attackerPosition & 0x88) == 0) {
			int attacker = Hex88Board.board[attackerPosition];
			if (attacker == IntChessman.NOPIECE) {
				attackerPosition += delta;
			} else {
				if (board.canSliderPseudoAttack(attacker, attackerPosition, targetPosition)) {
					if (hasHiddenAttacker(attackerPosition, targetPosition)) {
						addAttacker(chessmanList[IntChessman.getColor(attacker)], attacker, attackerPosition, true);
					} else {
						addAttacker(chessmanList[IntChessman.getColor(attacker)], attacker, attackerPosition, false);
					}
				}
				break;
			}
		}
	}

	private static boolean hasHiddenAttacker(int chessmanPosition, int targetPosition) {
		int vector = AttackVector.vector[targetPosition - chessmanPosition + 127];
		if (vector == AttackVector.N || vector == AttackVector.K) {
			// No line
			return false;
		}

		// Get the reverse delta
		int delta = AttackVector.delta[chessmanPosition - targetPosition + 127];

		// Find the hidden attacker
		int end = chessmanPosition + delta;
		while ((end & 0x88) == 0) {
			int chessman = Hex88Board.board[end];
			if (chessman == IntChessman.NOPIECE) {
				end += delta;
			} else {
				if (board.canSliderPseudoAttack(chessman, end, targetPosition)) {
					return true;
				}
				break;
			}
		}
		
		return false;
	}

	private static boolean shiftAttacker(SeeList list) {
		if (list.size == 0) {
			return false;
		} else {
			// Shift all other attackers
			for (int i = 0; i < list.size - 1; i++) {
				list.chessman[i] = list.chessman[i + 1];
				list.position[i] = list.position[i + 1];
			}
			list.size--;
			
			return true;
		}
	}

	private static void addAttacker(SeeList list, int attacker, int attackerPosition, boolean hasHiddenAttacker) {
		int attackerValue = IntChessman.getValueFromPiece(attacker);
		int index = -1;
		for (int i = 0; i < list.size; i++) {
			int chessman = list.chessman[i];
			int position = list.position[i];
			if ((!hasHiddenAttacker && IntChessman.getValueFromPiece(chessman) > attackerValue)
					|| (hasHiddenAttacker && IntChessman.getValueFromPiece(chessman) >= attackerValue)) {
				// Insert the attacker at this position
				list.chessman[i] = attacker;
				list.position[i] = attackerPosition;
				attacker = chessman;
				attackerPosition = position;
				index = i + 1;
				break;
			}
		}
		if (index != -1) {
			// Shift all other attackers
			for (int i = index; i < list.size; i++) {
				int chessman = list.chessman[i];
				int position = list.position[i];
				list.chessman[i] = attacker;
				list.position[i] = attackerPosition;
				attacker = chessman;
				attackerPosition = position;
			}
		}
		list.chessman[list.size] = attacker;
		list.position[list.size] = attackerPosition;
		list.size++;
	}

}
