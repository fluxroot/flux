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

  private final class SeeList {
    private static final int MAXSIZE = 16;

    public final int[] chessman = new int[MAXSIZE];
    public final int[] square = new int[MAXSIZE];
    public int head = 0;
    public int size = 0;
  }

  public MoveSee(Board newBoard) {
    board = newBoard;
    chessmanList[IntColor.WHITE] = new SeeList();
    chessmanList[IntColor.BLACK] = new SeeList();
  }

  public static int seeMove(int move, int myColor) {
    int originSquare = Move.getOriginSquare(move);
    int targetSquare = Move.getTargetSquare(move);
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
    addAllAttackers(myList, targetSquare, myColor);
    addAllAttackers(enemyList, targetSquare, enemyColor);

    // Find the attacker hiding behind the en-passant
    if (type == Move.Type.ENPASSANT) {
      if (myColor == IntColor.WHITE) {
        addHiddenAttacker(targetSquare - 16, targetSquare);
      } else {
        assert myColor == IntColor.BLACK;

        addHiddenAttacker(targetSquare + 16, targetSquare);
      }
    }

    // Remove the chessman from the attackers
    int square = -1;
    for (int i = 0; i < myList.size; i++) {
      if (originSquare == myList.square[i]) {
        square = i;
        break;
      }
    }
    if (square != -1) {
      for (int i = square; i < myList.size - 1; i++) {
        myList.chessman[i] = myList.chessman[i + 1];
        myList.square[i] = myList.square[i + 1];
      }
      myList.size--;
    }

    // Add the hidden attacker
    addHiddenAttacker(originSquare, targetSquare);

    // Make the capture
    value -= makeCapture(targetSquare, enemyColor, myColor, attackerValue);

    return value;
  }

  private static int makeCapture(int targetSquare, int myColor, int enemyColor, int targetValue) {
    // Get the next attacker
    int attacker = chessmanList[myColor].chessman[0];
    int attackerSquare = chessmanList[myColor].square[0];
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
      && ((targetSquare > 111 && myColor == IntColor.WHITE)
      || (targetSquare < 8 && myColor == IntColor.BLACK))) {
      // Adjust on promotion
      value += Evaluation.VALUE_QUEEN - Evaluation.VALUE_PAWN;
      attackerValue = Evaluation.VALUE_QUEEN;
    } else {
      attackerValue = Evaluation.getValueFromChessman(chessman);
    }

    // Add the hidden attacker
    addHiddenAttacker(attackerSquare, targetSquare);

    value -= makeCapture(targetSquare, enemyColor, myColor, attackerValue);

    return value;
  }

  private static void addAllAttackers(SeeList list, int targetSquare, int myColor) {
    // Pawn attacks
    int sign = 1;
    int pawn = IntPiece.WHITEPAWN;
    if (myColor == IntColor.BLACK) {
      sign = -1;
      pawn = IntPiece.BLACKPAWN;
    } else {
      assert myColor == IntColor.WHITE;
    }
    int pawnSquare = targetSquare - sign * 15;
    if ((pawnSquare & 0x88) == 0 && board.board[pawnSquare] == pawn) {
      list.chessman[list.size] = pawn;
      list.square[list.size] = pawnSquare;
      list.size++;
    }
    pawnSquare = targetSquare - sign * 17;
    if ((pawnSquare & 0x88) == 0 && board.board[pawnSquare] == pawn) {
      list.chessman[list.size] = pawn;
      list.square[list.size] = pawnSquare;
      list.size++;
    }

    // Knight attacks
    for (long squares = board.knightList[myColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (board.canAttack(IntChessman.KNIGHT, myColor, square, targetSquare)) {
        list.chessman[list.size] = board.board[square];
        list.square[list.size] = square;
        list.size++;
      }
    }

    // Bishop attacks
    for (long squares = board.bishopList[myColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (board.canAttack(IntChessman.BISHOP, myColor, square, targetSquare)) {
        int bishop = board.board[square];
        if (hasHiddenAttacker(square, targetSquare)) {
          addAttacker(list, bishop, square, true);
        } else {
          addAttacker(list, bishop, square, false);
        }
      }
    }

    // Rook attacks
    for (long squares = board.rookList[myColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (board.canAttack(IntChessman.ROOK, myColor, square, targetSquare)) {
        int rook = board.board[square];
        if (hasHiddenAttacker(square, targetSquare)) {
          addAttacker(list, rook, square, true);
        } else {
          addAttacker(list, rook, square, false);
        }
      }
    }

    // Queen attacks
    for (long squares = board.queenList[myColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (board.canAttack(IntChessman.QUEEN, myColor, square, targetSquare)) {
        int queen = board.board[square];
        if (hasHiddenAttacker(square, targetSquare)) {
          addAttacker(list, queen, square, true);
        } else {
          addAttacker(list, queen, square, false);
        }
      }
    }

    // King attacks
    assert Long.bitCount(board.kingList[myColor]) == 1;
    int square = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[myColor]));
    if (board.canAttack(IntChessman.KING, myColor, square, targetSquare)) {
      list.chessman[list.size] = board.board[square];
      list.square[list.size] = square;
      list.size++;
    }
  }

  private static void addHiddenAttacker(int chessmanSquare, int targetSquare) {
    int vector = Attack.vector[targetSquare - chessmanSquare + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return;
    }

    // Get the reverse delta
    int delta = Attack.deltas[chessmanSquare - targetSquare + 127];

    // Find the hidden attacker
    int attackerSquare = chessmanSquare + delta;
    while ((attackerSquare & 0x88) == 0) {
      int attacker = board.board[attackerSquare];
      if (attacker == IntPiece.NOPIECE) {
        attackerSquare += delta;
      } else {
        if (board.canSliderPseudoAttack(attacker, attackerSquare, targetSquare)) {
          if (hasHiddenAttacker(attackerSquare, targetSquare)) {
            addAttacker(chessmanList[IntPiece.getColor(attacker)], attacker, attackerSquare, true);
          } else {
            addAttacker(chessmanList[IntPiece.getColor(attacker)], attacker, attackerSquare, false);
          }
        }
        break;
      }
    }
  }

  private static boolean hasHiddenAttacker(int chessmanSquare, int endSquare) {
    int vector = Attack.vector[endSquare - chessmanSquare + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    // Get the reverse delta
    int delta = Attack.deltas[chessmanSquare - endSquare + 127];

    // Find the hidden attacker
    int targetSquare = chessmanSquare + delta;
    while ((targetSquare & 0x88) == 0) {
      int chessman = board.board[targetSquare];
      if (chessman == IntPiece.NOPIECE) {
        targetSquare += delta;
      } else {
        if (board.canSliderPseudoAttack(chessman, targetSquare, endSquare)) {
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
        list.square[i] = list.square[i + 1];
      }
      list.size--;

      return true;
    }
  }

  private static void addAttacker(SeeList list, int attacker, int attackerSquare, boolean hasHiddenAttacker) {
    int attackerValue = Evaluation.getValueFromPiece(attacker);
    int index = -1;
    for (int i = 0; i < list.size; i++) {
      int chessman = list.chessman[i];
      int square = list.square[i];
      if ((!hasHiddenAttacker && Evaluation.getValueFromPiece(chessman) > attackerValue)
        || (hasHiddenAttacker && Evaluation.getValueFromPiece(chessman) >= attackerValue)) {
        // Insert the attacker at this square
        list.chessman[i] = attacker;
        list.square[i] = attackerSquare;
        attacker = chessman;
        attackerSquare = square;
        index = i + 1;
        break;
      }
    }
    if (index != -1) {
      // Shift all other attackers
      for (int i = index; i < list.size; i++) {
        int chessman = list.chessman[i];
        int square = list.square[i];
        list.chessman[i] = attacker;
        list.square[i] = attackerSquare;
        attacker = chessman;
        attackerSquare = square;
      }
    }
    list.chessman[list.size] = attacker;
    list.square[list.size] = attackerSquare;
    list.size++;
  }

}
