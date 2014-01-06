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

import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.models.IntPiece;

/**
 * Notes: Ideas from Fruit
 */
public final class MoveSee {

  private static Board board = null;
  private static final SeeList[] chessmanList = new SeeList[IntColor.values.length];

  public MoveSee(Board newBoard) {
    board = newBoard;
    chessmanList[IntColor.WHITE] = new SeeList();
    chessmanList[IntColor.BLACK] = new SeeList();
  }

  public static int seeMove(int move, int myColor) {
    int originPosition = Move.getOriginPosition(move);
    int targetPosition = Move.getTargetPosition(move);
    int type = Move.getType(move);

    // Get the enemy color
    int enemyColor = IntColor.opposite(myColor);

    // Clear the chessman list
    SeeList myList = chessmanList[myColor];
    SeeList enemyList = chessmanList[enemyColor];
    myList.head = 0;
    myList.size = 0;
    enemyList.head = 0;
    enemyList.size = 0;

    // Get the attacker value
    int attackerValue;
    if (type == Move.Type.PAWNPROMOTION) {
      attackerValue = Evaluation.getValueFromChessman(Move.getPromotion(move));
    } else {
      attackerValue = Evaluation.getValueFromChessman(IntPiece.getChessman(Move.getOriginPiece(move)));
    }

    // We have no target for now
    int value = 0;

    // Get the target value
    int target = Move.getTargetPiece(move);
    if (target != IntPiece.NOPIECE) {
      value = Evaluation.getValueFromPiece(target);
    }
    if (type == Move.Type.PAWNPROMOTION) {
      value += Evaluation.getValueFromChessman(Move.getPromotion(move)) - Evaluation.VALUE_PAWN;
    }

    // Find all attackers
    addAllAttackers(myList, targetPosition, myColor);
    addAllAttackers(enemyList, targetPosition, enemyColor);

    // Find the attacker hiding behind the en-passant
    if (type == Move.Type.ENPASSANT) {
      if (myColor == IntColor.WHITE) {
        addHiddenAttacker(targetPosition - 16, targetPosition);
      } else {
        assert myColor == IntColor.BLACK;

        addHiddenAttacker(targetPosition + 16, targetPosition);
      }
    }

    // Remove the chessman from the attackers
    int position = -1;
    for (int i = 0; i < myList.size; i++) {
      if (originPosition == myList.position[i]) {
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
    addHiddenAttacker(originPosition, targetPosition);

    // Make the capture
    value -= makeCapture(targetPosition, enemyColor, myColor, attackerValue);

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
    if (value == Evaluation.VALUE_KING) {
      return value;
    }

    // Get the attacker value
    int attackerValue;
    int chessman = IntPiece.getChessman(attacker);
    if (chessman == IntChessman.PAWN
      && ((targetPosition > 111 && myColor == IntColor.WHITE)
      || (targetPosition < 8 && myColor == IntColor.BLACK))) {
      // Adjust on promotion
      value += Evaluation.VALUE_QUEEN - Evaluation.VALUE_PAWN;
      attackerValue = Evaluation.VALUE_QUEEN;
    } else {
      attackerValue = Evaluation.getValueFromChessman(chessman);
    }

    // Add the hidden attacker
    addHiddenAttacker(attackerPosition, targetPosition);

    value -= makeCapture(targetPosition, enemyColor, myColor, attackerValue);

    return value;
  }

  private static void addAllAttackers(SeeList list, int targetPosition, int myColor) {
    // Pawn attacks
    int sign = 1;
    int pawn = IntPiece.WHITEPAWN;
    if (myColor == IntColor.BLACK) {
      sign = -1;
      pawn = IntPiece.BLACKPAWN;
    } else {
      assert myColor == IntColor.WHITE;
    }
    int pawnPosition = targetPosition - sign * 15;
    if ((pawnPosition & 0x88) == 0 && board.board[pawnPosition] == pawn) {
      list.chessman[list.size] = pawn;
      list.position[list.size] = pawnPosition;
      list.size++;
    }
    pawnPosition = targetPosition - sign * 17;
    if ((pawnPosition & 0x88) == 0 && board.board[pawnPosition] == pawn) {
      list.chessman[list.size] = pawn;
      list.position[list.size] = pawnPosition;
      list.size++;
    }

    // Knight attacks
    for (long positions = board.knightList[myColor].positions; positions != 0; positions &= positions - 1) {
      int position = ChessmanList.next(positions);
      if (board.canAttack(IntChessman.KNIGHT, myColor, position, targetPosition)) {
        list.chessman[list.size] = board.board[position];
        list.position[list.size] = position;
        list.size++;
      }
    }

    // Bishop attacks
    for (long positions = board.bishopList[myColor].positions; positions != 0; positions &= positions - 1) {
      int position = ChessmanList.next(positions);
      if (board.canAttack(IntChessman.BISHOP, myColor, position, targetPosition)) {
        int bishop = board.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, bishop, position, true);
        } else {
          addAttacker(list, bishop, position, false);
        }
      }
    }

    // Rook attacks
    for (long positions = board.rookList[myColor].positions; positions != 0; positions &= positions - 1) {
      int position = ChessmanList.next(positions);
      if (board.canAttack(IntChessman.ROOK, myColor, position, targetPosition)) {
        int rook = board.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, rook, position, true);
        } else {
          addAttacker(list, rook, position, false);
        }
      }
    }

    // Queen attacks
    for (long positions = board.queenList[myColor].positions; positions != 0; positions &= positions - 1) {
      int position = ChessmanList.next(positions);
      if (board.canAttack(IntChessman.QUEEN, myColor, position, targetPosition)) {
        int queen = board.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, queen, position, true);
        } else {
          addAttacker(list, queen, position, false);
        }
      }
    }

    // King attacks
    assert board.kingList[myColor].size() == 1;
    int position = ChessmanList.next(board.kingList[myColor].positions);
    if (board.canAttack(IntChessman.KING, myColor, position, targetPosition)) {
      list.chessman[list.size] = board.board[position];
      list.position[list.size] = position;
      list.size++;
    }
  }

  private static void addHiddenAttacker(int chessmanPosition, int targetPosition) {
    int vector = Attack.vector[targetPosition - chessmanPosition + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return;
    }

    // Get the reverse delta
    int delta = Attack.deltas[chessmanPosition - targetPosition + 127];

    // Find the hidden attacker
    int attackerPosition = chessmanPosition + delta;
    while ((attackerPosition & 0x88) == 0) {
      int attacker = board.board[attackerPosition];
      if (attacker == IntPiece.NOPIECE) {
        attackerPosition += delta;
      } else {
        if (board.canSliderPseudoAttack(attacker, attackerPosition, targetPosition)) {
          if (hasHiddenAttacker(attackerPosition, targetPosition)) {
            addAttacker(chessmanList[IntPiece.getColor(attacker)], attacker, attackerPosition, true);
          } else {
            addAttacker(chessmanList[IntPiece.getColor(attacker)], attacker, attackerPosition, false);
          }
        }
        break;
      }
    }
  }

  private static boolean hasHiddenAttacker(int chessmanPosition, int endPosition) {
    int vector = Attack.vector[endPosition - chessmanPosition + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    // Get the reverse delta
    int delta = Attack.deltas[chessmanPosition - endPosition + 127];

    // Find the hidden attacker
    int targetPosition = chessmanPosition + delta;
    while ((targetPosition & 0x88) == 0) {
      int chessman = board.board[targetPosition];
      if (chessman == IntPiece.NOPIECE) {
        targetPosition += delta;
      } else {
        if (board.canSliderPseudoAttack(chessman, targetPosition, endPosition)) {
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
    int attackerValue = Evaluation.getValueFromPiece(attacker);
    int index = -1;
    for (int i = 0; i < list.size; i++) {
      int chessman = list.chessman[i];
      int position = list.position[i];
      if ((!hasHiddenAttacker && Evaluation.getValueFromPiece(chessman) > attackerValue)
        || (hasHiddenAttacker && Evaluation.getValueFromPiece(chessman) >= attackerValue)) {
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
