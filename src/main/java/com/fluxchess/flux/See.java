/*
 * Copyright (C) 2007-2014 Phokham Nonava
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

/**
 * Notes: Ideas from Fruit
 */
final class See {

  private static Position board = null;
  private static final List[] chessmanList = new List[Color.ARRAY_DIMENSION];

  private static final class List {
    private static final int MAXSIZE = 16;

    final int[] chessman = new int[MAXSIZE];
    final int[] position = new int[MAXSIZE];
    int size = 0;

    List() {
    }
  }

  See(Position newBoard) {
    board = newBoard;
    chessmanList[Color.WHITE] = new List();
    chessmanList[Color.BLACK] = new List();
  }

  static int seeMove(int move, int myColor) {
    int start = Move.getStart(move);
    int end = Move.getEnd(move);
    int target = Move.getTarget(move);
    int type = Move.getType(move);

    // Get the enemy color
    int enemyColor = Color.switchColor(myColor);

    // Clear the chessman list
    List myList = chessmanList[myColor];
    List enemyList = chessmanList[enemyColor];
    myList.size = 0;
    enemyList.size = 0;

    // Get the attacker value
    int attackerValue;
    if (type == MoveType.PAWNPROMOTION) {
      attackerValue = Piece.getValueFromChessman(Move.getPromotion(move));
    } else {
      attackerValue = Piece.getValueFromChessman(Move.getChessman(move));
    }

    // We have no target for now
    int value = 0;

    // Get the target value
    if (target != Piece.NOPIECE) {
      value = Piece.getValueFromChessman(target);
    }
    if (type == MoveType.PAWNPROMOTION) {
      value += Piece.getValueFromChessman(Move.getPromotion(move)) - Piece.VALUE_PAWN;
    }

    // Find all attackers
    addAllAttackers(myList, end, myColor);
    addAllAttackers(enemyList, end, enemyColor);

    // Find the attacker hiding behind the en-passant
    if (type == MoveType.ENPASSANT) {
      if (myColor == Color.WHITE) {
        addHiddenAttacker(end - 16, end);
      } else {
        assert myColor == Color.BLACK;

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
    if (value == Piece.VALUE_KING) {
      return value;
    }

    // Get the attacker value
    int attackerValue;
    int chessman = Piece.getChessman(attacker);
    if (chessman == PieceType.PAWN
        && ((targetPosition > 111 && myColor == Color.WHITE)
        || (targetPosition < 8 && myColor == Color.BLACK))) {
      // Adjust on promotion
      value += Piece.VALUE_QUEEN - Piece.VALUE_PAWN;
      attackerValue = Piece.VALUE_QUEEN;
    } else {
      attackerValue = Piece.getValueFromChessman(chessman);
    }

    // Add the hidden attacker
    addHiddenAttacker(attackerPosition, targetPosition);

    value -= makeCapture(targetPosition, enemyColor, myColor, attackerValue);

    return value;
  }

  private static void addAllAttackers(List list, int targetPosition, int myColor) {
    // Pawn attacks
    int sign = 1;
    int pawn = Piece.WHITE_PAWN;
    if (myColor == Color.BLACK) {
      sign = -1;
      pawn = Piece.BLACK_PAWN;
    } else {
      assert myColor == Color.WHITE;
    }
    int pawnPosition = targetPosition - sign * 15;
    if ((pawnPosition & 0x88) == 0 && Position.board[pawnPosition] == pawn) {
      list.chessman[list.size] = pawn;
      list.position[list.size] = pawnPosition;
      list.size++;
    }
    pawnPosition = targetPosition - sign * 17;
    if ((pawnPosition & 0x88) == 0 && Position.board[pawnPosition] == pawn) {
      list.chessman[list.size] = pawn;
      list.position[list.size] = pawnPosition;
      list.size++;
    }

    // Knight attacks
    PositionList tempPositionList = Position.knightList[myColor];
    for (int i = 0; i < tempPositionList.size; i++) {
      int position = tempPositionList.position[i];
      if (board.canAttack(PieceType.KNIGHT, myColor, position, targetPosition)) {
        list.chessman[list.size] = Position.board[position];
        list.position[list.size] = position;
        list.size++;
      }
    }

    // Bishop attacks
    tempPositionList = Position.bishopList[myColor];
    for (int i = 0; i < tempPositionList.size; i++) {
      int position = tempPositionList.position[i];
      if (board.canAttack(PieceType.BISHOP, myColor, position, targetPosition)) {
        int bishop = Position.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, bishop, position, true);
        } else {
          addAttacker(list, bishop, position, false);
        }
      }
    }

    // Rook attacks
    tempPositionList = Position.rookList[myColor];
    for (int i = 0; i < tempPositionList.size; i++) {
      int position = tempPositionList.position[i];
      if (board.canAttack(PieceType.ROOK, myColor, position, targetPosition)) {
        int rook = Position.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, rook, position, true);
        } else {
          addAttacker(list, rook, position, false);
        }
      }
    }

    // Queen attacks
    tempPositionList = Position.queenList[myColor];
    for (int i = 0; i < tempPositionList.size; i++) {
      int position = tempPositionList.position[i];
      if (board.canAttack(PieceType.QUEEN, myColor, position, targetPosition)) {
        int queen = Position.board[position];
        if (hasHiddenAttacker(position, targetPosition)) {
          addAttacker(list, queen, position, true);
        } else {
          addAttacker(list, queen, position, false);
        }
      }
    }

    // King attacks
    assert Position.kingList[myColor].size == 1;
    int position = Position.kingList[myColor].position[0];
    if (board.canAttack(PieceType.KING, myColor, position, targetPosition)) {
      list.chessman[list.size] = Position.board[position];
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
      int attacker = Position.board[attackerPosition];
      if (attacker == Piece.NOPIECE) {
        attackerPosition += delta;
      } else {
        if (board.canSliderPseudoAttack(attacker, attackerPosition, targetPosition)) {
          if (hasHiddenAttacker(attackerPosition, targetPosition)) {
            addAttacker(chessmanList[Piece.getColor(attacker)], attacker, attackerPosition, true);
          } else {
            addAttacker(chessmanList[Piece.getColor(attacker)], attacker, attackerPosition, false);
          }
        }
        break;
      }
    }
  }

  private static boolean hasHiddenAttacker(int chessmanPosition, int targetPosition) {
    int vector = Attack.vector[targetPosition - chessmanPosition + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    // Get the reverse delta
    int delta = Attack.deltas[chessmanPosition - targetPosition + 127];

    // Find the hidden attacker
    int end = chessmanPosition + delta;
    while ((end & 0x88) == 0) {
      int chessman = Position.board[end];
      if (chessman == Piece.NOPIECE) {
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

  private static boolean shiftAttacker(List list) {
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

  private static void addAttacker(List list, int attacker, int attackerPosition, boolean hasHiddenAttacker) {
    int attackerValue = Piece.getValueFromPiece(attacker);
    int index = -1;
    for (int i = 0; i < list.size; i++) {
      int chessman = list.chessman[i];
      int position = list.position[i];
      if ((!hasHiddenAttacker && Piece.getValueFromPiece(chessman) > attackerValue)
          || (hasHiddenAttacker && Piece.getValueFromPiece(chessman) >= attackerValue)) {
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
