/*
 * Copyright 2007-2013 the original author or authors.
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
package com.fluxchess.x88.board;

import com.fluxchess.jcpi.models.*;
import com.fluxchess.x88.Configuration;

public final class X88Board {

  /**
   * The size of the 0x88 board
   */
  public static final int BOARDSIZE = 128;

  // The size of the history stack
  private static final int STACKSIZE = Configuration.MAX_GAME_MOVES;

  //## BEGIN 0x88 Board Representation
  public final int[] board = new int[BOARDSIZE];
  //## ENDOF 0x88 Board Representation

  // The chessman lists.
  public final PositionList[] pawnList = new PositionList[IntColor.ARRAY_DIMENSION];
  public final PositionList[] knightList = new PositionList[IntColor.ARRAY_DIMENSION];
  public final PositionList[] bishopList = new PositionList[IntColor.ARRAY_DIMENSION];
  public final PositionList[] rookList = new PositionList[IntColor.ARRAY_DIMENSION];
  public final PositionList[] queenList = new PositionList[IntColor.ARRAY_DIMENSION];
  public final PositionList[] kingList = new PositionList[IntColor.ARRAY_DIMENSION];

  // Board stack
  private final StackEntry[] stack = new StackEntry[STACKSIZE];
  private int stackSize = 0;

  // En Passant square
  public int enPassantSquare = X88Position.NOPOSITION;

  // Castling
  public int castling;
  private final int[] castlingHistory = new int[STACKSIZE];
  private int castlingHistorySize = 0;

  // Capture
  public int captureSquare = X88Position.NOPOSITION;
  private final int[] captureHistory = new int[STACKSIZE];
  private int captureHistorySize = 0;

  // Half move clock
  public int halfMoveClock = 0;

  // The half move number
  private int halfMoveNumber;

  // The active color
  public int activeColor = IntColor.WHITE;

  // Attack
  private final Attack[][] attackHistory = new Attack[STACKSIZE + 1][IntColor.ARRAY_DIMENSION];
  private int attackHistorySize = 0;
  private final Attack tempAttack = new Attack();

  public X88Board(GenericBoard newBoard) {
    for (int i = 0; i < stack.length; i++) {
      stack[i] = new StackEntry();
    }

    // Initialize the position lists
    for (int color : IntColor.values) {
      pawnList[color] = new PositionList();
      knightList[color] = new PositionList();
      bishopList[color] = new PositionList();
      rookList[color] = new PositionList();
      queenList[color] = new PositionList();
      kingList[color] = new PositionList();
    }

    // Initialize the attack list
    for (int i = 0; i < attackHistory.length; i++) {
      for (int j = 0; j < IntColor.ARRAY_DIMENSION; j++) {
        attackHistory[i][j] = new Attack();
      }
    }

    // Initialize the board
    for (int position : X88Position.values) {
      board[position] = IntChessman.NOPIECE;

      GenericPiece genericPiece = newBoard.getPiece(X88Position.valueOfIntPosition(position));
      if (genericPiece != null) {
        int piece = IntChessman.createPiece(IntChessman.valueOfChessman(genericPiece.chessman), IntColor.valueOfColor(genericPiece.color));
        put(piece, position);
      }
    }

    // Initialize en passant
    if (newBoard.getEnPassant() != null) {
      enPassantSquare = X88Position.valueOfPosition(newBoard.getEnPassant());
    }

    // Initialize castling
    castling = 0;
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.KINGSIDE) != null) {
      castling |= IntCastling.WHITE_KINGSIDE;
    }
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.QUEENSIDE) != null) {
      castling |= IntCastling.WHITE_QUEENSIDE;
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.KINGSIDE) != null) {
      castling |= IntCastling.BLACK_KINGSIDE;
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.QUEENSIDE) != null) {
      castling |= IntCastling.BLACK_QUEENSIDE;
    }

    // Initialize the active color
    if (activeColor != IntColor.valueOfColor(newBoard.getActiveColor())) {
      activeColor = IntColor.valueOfColor(newBoard.getActiveColor());
    }

    // Initialize the half move clock
    assert newBoard.getHalfMoveClock() >= 0;
    halfMoveClock = newBoard.getHalfMoveClock();

    // Initialize the full move number
    assert newBoard.getFullMoveNumber() > 0;
    setFullMoveNumber(newBoard.getFullMoveNumber());
  }

  /**
   * Puts the piece on the board at the given position.
   *
   * @param piece    the piece.
   * @param position the position.
   */
  private void put(int piece, int position) {
    assert piece != IntChessman.NOPIECE;
    assert (position & 0x88) == 0;
    assert board[position] == IntChessman.NOPIECE;

    // Store some variables for later use
    int chessman = IntChessman.getChessman(piece);
    int color = IntChessman.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color].add(position);
        break;
      case IntChessman.KNIGHT:
        knightList[color].add(position);
        break;
      case IntChessman.BISHOP:
        bishopList[color].add(position);
        break;
      case IntChessman.ROOK:
        rookList[color].add(position);
        break;
      case IntChessman.QUEEN:
        queenList[color].add(position);
        break;
      case IntChessman.KING:
        kingList[color].add(position);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[position] = piece;
  }

  /**
   * Removes the piece from the board at the given position.
   *
   * @param position the position.
   * @return the removed piece.
   */
  private int remove(int position) {
    assert (position & 0x88) == 0;
    assert board[position] != IntChessman.NOPIECE;

    // Get the piece
    int piece = board[position];

    // Store some variables for later use
    int chessman = IntChessman.getChessman(piece);
    int color = IntChessman.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color].remove(position);
        break;
      case IntChessman.KNIGHT:
        knightList[color].remove(position);
        break;
      case IntChessman.BISHOP:
        bishopList[color].remove(position);
        break;
      case IntChessman.ROOK:
        rookList[color].remove(position);
        break;
      case IntChessman.QUEEN:
        queenList[color].remove(position);
        break;
      case IntChessman.KING:
        kingList[color].remove(position);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[position] = IntChessman.NOPIECE;

    return piece;
  }

  /**
   * Moves the piece from the start position to the end position.
   *
   * @param start  the start position.
   * @param end    the end position.
   * @return the moved piece.
   */
  private int move(int start, int end) {
    assert (start & 0x88) == 0;
    assert (end & 0x88) == 0;
    assert board[start] != IntChessman.NOPIECE;
    assert board[end] == IntChessman.NOPIECE;

    // Get the piece
    int piece = board[start];

    // Store some variables for later use
    int chessman = IntChessman.getChessman(piece);
    int color = IntChessman.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color].remove(start);
        pawnList[color].add(end);
        break;
      case IntChessman.KNIGHT:
        knightList[color].remove(start);
        knightList[color].add(end);
        break;
      case IntChessman.BISHOP:
        bishopList[color].remove(start);
        bishopList[color].add(end);
        break;
      case IntChessman.ROOK:
        rookList[color].remove(start);
        rookList[color].add(end);
        break;
      case IntChessman.QUEEN:
        queenList[color].remove(start);
        queenList[color].add(end);
        break;
      case IntChessman.KING:
        kingList[color].remove(start);
        kingList[color].add(end);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[start] = IntChessman.NOPIECE;
    board[end] = piece;

    return piece;
  }

  public GenericBoard getBoard() {
    GenericBoard newBoard = new GenericBoard();

    // Set chessmen
    for (GenericColor color : GenericColor.values()) {
      int intColor = IntColor.valueOfColor(color);

      for (int index = 0; index < pawnList[intColor].size; index++) {
        int intPosition = pawnList[intColor].positions[index];
        assert intPosition != X88Position.NOPOSITION;
        assert IntChessman.getChessman(board[intPosition]) == IntChessman.PAWN;
        assert IntChessman.getColor(board[intPosition]) == intColor;

        GenericPosition position = X88Position.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.PAWN), position);
      }

      for (int index = 0; index < knightList[intColor].size; index++) {
        int intPosition = knightList[intColor].positions[index];
        assert intPosition != X88Position.NOPOSITION;
        assert IntChessman.getChessman(board[intPosition]) == IntChessman.KNIGHT;
        assert IntChessman.getColor(board[intPosition]) == intColor;

        GenericPosition position = X88Position.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KNIGHT), position);
      }

      for (int index = 0; index < bishopList[intColor].size; index++) {
        int intPosition = bishopList[intColor].positions[index];
        assert intPosition != X88Position.NOPOSITION;
        assert IntChessman.getChessman(board[intPosition]) == IntChessman.BISHOP;
        assert IntChessman.getColor(board[intPosition]) == intColor;

        GenericPosition position = X88Position.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.BISHOP), position);
      }

      for (int index = 0; index < rookList[intColor].size; index++) {
        int intPosition = rookList[intColor].positions[index];
        assert intPosition != X88Position.NOPOSITION;
        assert IntChessman.getChessman(board[intPosition]) == IntChessman.ROOK;
        assert IntChessman.getColor(board[intPosition]) == intColor;

        GenericPosition position = X88Position.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.ROOK), position);
      }

      for (int index = 0; index < queenList[intColor].size; index++) {
        int intPosition = queenList[intColor].positions[index];
        assert intPosition != X88Position.NOPOSITION;
        assert IntChessman.getChessman(board[intPosition]) == IntChessman.QUEEN;
        assert IntChessman.getColor(board[intPosition]) == intColor;

        GenericPosition position = X88Position.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.QUEEN), position);
      }

      assert kingList[intColor].size == 1;
      int intPosition = kingList[intColor].positions[0];
      assert intPosition != X88Position.NOPOSITION;
      assert IntChessman.getChessman(board[intPosition]) == IntChessman.KING;
      assert IntChessman.getColor(board[intPosition]) == intColor;

      GenericPosition position = X88Position.valueOfIntPosition(intPosition);
      newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KING), position);
    }

    // Set active color
    newBoard.setActiveColor(IntColor.valueOfIntColor(activeColor));

    // Set castling
    if ((castling & IntCastling.WHITE_KINGSIDE) != 0) {
      newBoard.setCastling(GenericColor.WHITE, GenericCastling.KINGSIDE, GenericFile.Fh);
    }
    if ((castling & IntCastling.WHITE_QUEENSIDE) != 0) {
      newBoard.setCastling(GenericColor.WHITE, GenericCastling.QUEENSIDE, GenericFile.Fa);
    }
    if ((castling & IntCastling.BLACK_KINGSIDE) != 0) {
      newBoard.setCastling(GenericColor.BLACK, GenericCastling.KINGSIDE, GenericFile.Fh);
    }
    if ((castling & IntCastling.BLACK_QUEENSIDE) != 0) {
      newBoard.setCastling(GenericColor.BLACK, GenericCastling.QUEENSIDE, GenericFile.Fa);
    }

    // Set en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      newBoard.setEnPassant(X88Position.valueOfIntPosition(enPassantSquare));
    }

    // Set half move clock
    newBoard.setHalfMoveClock(halfMoveClock);

    // Set full move number
    newBoard.setFullMoveNumber(getFullMoveNumber());

    return newBoard;
  }

  public int getFullMoveNumber() {
    return halfMoveNumber / 2;
  }

  private void setFullMoveNumber(int fullMoveNumber) {
    assert fullMoveNumber > 0;

    halfMoveNumber = fullMoveNumber * 2;
    if (activeColor == IntColor.valueOfColor(GenericColor.BLACK)) {
      halfMoveNumber++;
    }
  }

  /**
   * Returns whether this move checks the opponent king.
   *
   * @param move the move.
   * @return true if this move checks the opponent king.
   */
  public boolean isCheckingMove(int move) {
    assert move != X88Move.NOMOVE;

    int chessmanColor = X88Move.getChessmanColor(move);
    int endPosition = X88Move.getEnd(move);
    int enemyKingColor = IntColor.switchColor(chessmanColor);
    int enemyKingPosition = kingList[enemyKingColor].positions[0];

    switch (X88Move.getType(move)) {
      case X88Move.NORMAL:
      case X88Move.PAWNDOUBLE:
        int chessman = X88Move.getChessman(move);

        // Direct attacks
        if (canAttack(chessman, chessmanColor, endPosition, enemyKingPosition)) {
          return true;
        }

        int startPosition = X88Move.getStart(move);

        if (isPinned(startPosition, enemyKingColor)) {
          // We are pinned. Test if we move on the line.
          int attackDeltaStart = AttackVector.delta[enemyKingPosition - startPosition + 127];
          int attackDeltaEnd = AttackVector.delta[enemyKingPosition - endPosition + 127];
          return attackDeltaStart != attackDeltaEnd;
        }
        // Indirect attacks
        break;
      case X88Move.PAWNPROMOTION:
      case X88Move.ENPASSANT:
        // We do a slow test for complex moves
        makeMove(move);
        boolean isCheck = isAttacked(enemyKingPosition, chessmanColor);
        undoMove(move);
        return isCheck;
      case X88Move.CASTLING:
        int rookEnd = X88Position.NOPOSITION;

        if (endPosition == X88Position.g1) {
          assert chessmanColor == IntColor.WHITE;
          rookEnd = X88Position.f1;
        } else if (endPosition == X88Position.g8) {
          assert chessmanColor == IntColor.BLACK;
          rookEnd = X88Position.f8;
        } else if (endPosition == X88Position.c1) {
          assert chessmanColor == IntColor.WHITE;
          rookEnd = X88Position.d1;
        } else if (endPosition == X88Position.c8) {
          assert chessmanColor == IntColor.BLACK;
          rookEnd = X88Position.d8;
        } else {
          assert false : endPosition;
        }

        return canAttack(IntChessman.ROOK, chessmanColor, rookEnd, enemyKingPosition);
      case X88Move.NULL:
        assert false;
        break;
      default:
        assert false : X88Move.getType(move);
        break;
    }

    return false;
  }

  public boolean isPinned(int chessmanPosition, int kingColor) {
    assert chessmanPosition != X88Position.NOPOSITION;
    assert kingColor != IntColor.NOCOLOR;

    int myKingPosition = kingList[kingColor].positions[0];

    // We can only be pinned on an attack line
    int vector = AttackVector.vector[myKingPosition - chessmanPosition + 127];
    if (vector == AttackVector.N || vector == AttackVector.K) {
      // No line
      return false;
    }

    int delta = AttackVector.delta[myKingPosition - chessmanPosition + 127];

    // Walk towards the king
    int end = chessmanPosition + delta;
    assert (end & 0x88) == 0;
    while (board[end] == IntChessman.NOPIECE) {
      end += delta;
      assert (end & 0x88) == 0;
    }
    if (end != myKingPosition) {
      // There's a blocker between me and the king
      return false;
    }

    // Walk away from the king
    end = chessmanPosition - delta;
    while ((end & 0x88) == 0) {
      int attacker = board[end];
      if (attacker != IntChessman.NOPIECE) {
        int attackerColor = IntChessman.getColor(attacker);

        return kingColor != attackerColor && canSliderPseudoAttack(attacker, end, myKingPosition);
      } else {
        end -= delta;
      }
    }

    return false;
  }

  /**
   * Returns whether or not the attacker can attack the target position. The
   * method does not check if a slider can reach the position.
   *
   * @param attacker       the attacker.
   * @param targetPosition the target position.
   * @return if the attacker can attack the target position.
   */
  public boolean canSliderPseudoAttack(int attacker, int attackerPosition, int targetPosition) {
    assert attacker != IntChessman.NOPIECE;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector = AttackVector.N;

    switch (IntChessman.getChessman(attacker)) {
      case IntChessman.PAWN:
        break;
      case IntChessman.KNIGHT:
        break;
      case IntChessman.BISHOP:
        attackVector = AttackVector.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case AttackVector.u:
          case AttackVector.d:
          case AttackVector.D:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.ROOK:
        attackVector = AttackVector.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case AttackVector.s:
          case AttackVector.S:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.QUEEN:
        attackVector = AttackVector.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case AttackVector.u:
          case AttackVector.d:
          case AttackVector.s:
          case AttackVector.D:
          case AttackVector.S:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.KING:
        break;
      default:
        assert false : IntChessman.getChessman(attacker);
        break;
    }

    return false;
  }

  /**
   * Returns the attacks on the king.
   *
   * @param color the Color of the target king.
   */
  public Attack getAttack(int color) {
    Attack attack = attackHistory[attackHistorySize][color];
    if (attack.count != Attack.NOATTACK) {
      return attack;
    }

    assert kingList[color].size == 1;

    int attackerColor = IntColor.switchColor(color);
    getAttack(attack, kingList[color].positions[0], attackerColor, false);

    return attack;
  }

  /**
   * Returns whether or not the position is attacked.
   *
   * @param targetPosition the X88Position.
   * @param attackerColor  the attacker color.
   * @return true if the position is attacked, false otherwise.
   */
  public boolean isAttacked(int targetPosition, int attackerColor) {
    assert (targetPosition & 0x88) == 0;
    assert attackerColor != IntColor.NOCOLOR;

    return getAttack(tempAttack, targetPosition, attackerColor, true);
  }

  /**
   * Returns all attacks to the target position.
   *
   * @param attack         the attack to fill the information.
   * @param targetPosition the target position.
   * @param attackerColor  the attacker color.
   * @param stop           whether we should only check.
   * @return true if the position can be attacked, false otherwise.
   */
  private boolean getAttack(Attack attack, int targetPosition, int attackerColor, boolean stop) {
    assert attack != null;
    assert targetPosition != X88Position.NOPOSITION;
    assert attackerColor != IntColor.NOCOLOR;

    attack.count = 0;

    // Pawn attacks
    int pawnPiece = IntChessman.WHITE_PAWN;
    int sign = -1;
    if (attackerColor == IntColor.BLACK) {
      pawnPiece = IntChessman.BLACK_PAWN;
      sign = 1;
    } else {
      assert attackerColor == IntColor.WHITE;
    }
    int pawnAttackerPosition = targetPosition + sign * 15;
    if ((pawnAttackerPosition & 0x88) == 0) {
      int pawn = board[pawnAttackerPosition];
      if (pawn != IntChessman.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert AttackVector.delta[targetPosition - pawnAttackerPosition + 127] == sign * -15;
        attack.position[attack.count] = pawnAttackerPosition;
        attack.delta[attack.count] = sign * -15;
        attack.count++;
      }
    }
    pawnAttackerPosition = targetPosition + sign * 17;
    if ((pawnAttackerPosition & 0x88) == 0) {
      int pawn = board[pawnAttackerPosition];
      if (pawn != IntChessman.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert AttackVector.delta[targetPosition - pawnAttackerPosition + 127] == sign * -17;
        attack.position[attack.count] = pawnAttackerPosition;
        attack.delta[attack.count] = sign * -17;
        attack.count++;
      }
    }
    PositionList tempChessmanList = knightList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.positions[index];
      assert IntChessman.getChessman(board[attackerPosition]) == IntChessman.KNIGHT;
      assert attackerPosition != X88Position.NOPOSITION;
      assert board[attackerPosition] != IntChessman.NOPIECE;
      assert attackerColor == IntChessman.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.KNIGHT, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    tempChessmanList = bishopList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.positions[index];
      assert IntChessman.getChessman(board[attackerPosition]) == IntChessman.BISHOP;
      assert attackerPosition != X88Position.NOPOSITION;
      assert board[attackerPosition] != IntChessman.NOPIECE;
      assert attackerColor == IntChessman.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.BISHOP, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    tempChessmanList = rookList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.positions[index];
      assert IntChessman.getChessman(board[attackerPosition]) == IntChessman.ROOK;
      assert attackerPosition != X88Position.NOPOSITION;
      assert board[attackerPosition] != IntChessman.NOPIECE;
      assert attackerColor == IntChessman.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.ROOK, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    tempChessmanList = queenList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.positions[index];
      assert IntChessman.getChessman(board[attackerPosition]) == IntChessman.QUEEN;
      assert attackerPosition != X88Position.NOPOSITION;
      assert board[attackerPosition] != IntChessman.NOPIECE;
      assert attackerColor == IntChessman.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.QUEEN, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    assert kingList[attackerColor].size == 1;
    int attackerPosition = kingList[attackerColor].positions[0];
    assert IntChessman.getChessman(board[attackerPosition]) == IntChessman.KING;
    assert attackerPosition != X88Position.NOPOSITION;
    assert board[attackerPosition] != IntChessman.NOPIECE;
    assert attackerColor == IntChessman.getColor(board[attackerPosition]);
    if (canAttack(IntChessman.KING, attackerColor, attackerPosition, targetPosition)) {
      if (stop) {
        return true;
      }
      int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];
      assert attackDelta != 0;
      attack.position[attack.count] = attackerPosition;
      attack.delta[attack.count] = attackDelta;
      attack.count++;
    }

    return false;
  }

  /**
   * Returns whether or not the attacker can attack the target position.
   *
   * @param attackerChessman the attacker chessman.
   * @param attackerColor    the attacker color.
   * @param attackerPosition the attacker position.
   * @param targetPosition   the target position.
   * @return if the attacker can attack the target position.
   */
  public boolean canAttack(int attackerChessman, int attackerColor, int attackerPosition, int targetPosition) {
    assert attackerChessman != IntChessman.NOPIECE;
    assert attackerColor != IntColor.NOCOLOR;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector = AttackVector.vector[targetPosition - attackerPosition + 127];

    switch (attackerChessman) {
      case IntChessman.PAWN:
        if (attackVector == AttackVector.u && attackerColor == IntColor.WHITE) {
          return true;
        } else if (attackVector == AttackVector.d && attackerColor == IntColor.BLACK) {
          return true;
        }
        break;
      case IntChessman.KNIGHT:
        if (attackVector == AttackVector.K) {
          return true;
        }
        break;
      case IntChessman.BISHOP:
        switch (attackVector) {
          case AttackVector.u:
          case AttackVector.d:
            return true;
          case AttackVector.D:
            if (canSliderAttack(attackerPosition, targetPosition)) {
              return true;
            }
            break;
          default:
            break;
        }
        break;
      case IntChessman.ROOK:
        switch (attackVector) {
          case AttackVector.s:
            return true;
          case AttackVector.S:
            if (canSliderAttack(attackerPosition, targetPosition)) {
              return true;
            }
            break;
          default:
            break;
        }
        break;
      case IntChessman.QUEEN:
        switch (attackVector) {
          case AttackVector.u:
          case AttackVector.d:
          case AttackVector.s:
            return true;
          case AttackVector.D:
          case AttackVector.S:
            if (canSliderAttack(attackerPosition, targetPosition)) {
              return true;
            }
            break;
          default:
            break;
        }
        break;
      case IntChessman.KING:
        switch (attackVector) {
          case AttackVector.u:
          case AttackVector.d:
          case AttackVector.s:
            return true;
          default:
            break;
        }
        break;
      default:
        assert false : attackerChessman;
        break;
    }

    return false;
  }

  /**
   * Returns whether or not the slider can attack the target position.
   *
   * @param attackerPosition the attacker position.
   * @param targetPosition   the target position.
   * @return true if the slider can attack the target position.
   */
  private boolean canSliderAttack(int attackerPosition, int targetPosition) {
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackDelta = AttackVector.delta[targetPosition - attackerPosition + 127];

    int end = attackerPosition + attackDelta;
    while ((end & 0x88) == 0 && end != targetPosition && board[end] == IntChessman.NOPIECE) {
      end += attackDelta;
    }

    return end == targetPosition;
  }

  public void makeMove(int move) {
    // Get current stack entry
    StackEntry currentStackEntry = stack[stackSize];

    // Save history
    currentStackEntry.halfMoveClockHistory = halfMoveClock;
    currentStackEntry.enPassantHistory = enPassantSquare;
    currentStackEntry.captureSquareHistory = captureSquare;

    // Update stack size
    stackSize++;
    assert stackSize < STACKSIZE;

    int type = X88Move.getType(move);

    switch (type) {
      case X88Move.NORMAL:
        makeMoveNormal(move);
        break;
      case X88Move.PAWNDOUBLE:
        makeMovePawnDouble(move);
        break;
      case X88Move.PAWNPROMOTION:
        makeMovePawnPromotion(move);
        break;
      case X88Move.ENPASSANT:
        makeMoveEnPassant(move);
        break;
      case X88Move.CASTLING:
        makeMoveCastling(move);
        break;
      case X88Move.NULL:
        makeMoveNull();
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Update half move number
    halfMoveNumber++;

    // Update active color
    activeColor = IntColor.switchColor(activeColor);

    // Update attack list
    attackHistorySize++;
    attackHistory[attackHistorySize][IntColor.WHITE].count = Attack.NOATTACK;
    attackHistory[attackHistorySize][IntColor.BLACK].count = Attack.NOATTACK;
  }

  public void undoMove(int move) {
    int type = X88Move.getType(move);

    // Update attack list
    attackHistorySize--;

    // Update active color
    activeColor = IntColor.switchColor(activeColor);

    // Update half move number
    halfMoveNumber--;

    // Update stack size
    stackSize--;
    assert stackSize >= 0;

    // Get current stack entry
    StackEntry currentStackEntry = stack[stackSize];

    // Restore zobrist history
    halfMoveClock = currentStackEntry.halfMoveClockHistory;
    enPassantSquare = currentStackEntry.enPassantHistory;
    captureSquare = currentStackEntry.captureSquareHistory;

    switch (type) {
      case X88Move.NORMAL:
        undoMoveNormal(move);
        break;
      case X88Move.PAWNDOUBLE:
        undoMovePawnDouble(move);
        break;
      case X88Move.PAWNPROMOTION:
        undoMovePawnPromotion(move);
        break;
      case X88Move.ENPASSANT:
        undoMoveEnPassant(move);
        break;
      case X88Move.CASTLING:
        undoMoveCastling(move);
        break;
      case X88Move.NULL:
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  private void makeMoveNormal(int move) {
    // Save the castling rights
    castlingHistory[castlingHistorySize++] = castling;
    int newCastling = castling;

    // Save the captured chessman
    int endPosition = X88Move.getEnd(move);
    int target = IntChessman.NOPIECE;
    if (board[endPosition] != IntChessman.NOPIECE) {
      target = remove(endPosition);
      assert X88Move.getTarget(move) != IntChessman.NOPIECE : X88Move.toString(move);
      captureHistory[captureHistorySize++] = target;
      captureSquare = endPosition;

      switch (endPosition) {
        case X88Position.a1:
          newCastling &= ~IntCastling.WHITE_QUEENSIDE;
          break;
        case X88Position.a8:
          newCastling &= ~IntCastling.BLACK_QUEENSIDE;
          break;
        case X88Position.h1:
          newCastling &= ~IntCastling.WHITE_KINGSIDE;
          break;
        case X88Position.h8:
          newCastling &= ~IntCastling.BLACK_KINGSIDE;
          break;
        case X88Position.e1:
          newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
          break;
        case X88Position.e8:
          newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
          break;
        default:
          break;
      }
      if (newCastling != castling) {
        assert (newCastling ^ castling) == IntCastling.WHITE_KINGSIDE
          || (newCastling ^ castling) == IntCastling.WHITE_QUEENSIDE
          || (newCastling ^ castling) == IntCastling.BLACK_KINGSIDE
          || (newCastling ^ castling) == IntCastling.BLACK_QUEENSIDE
          || (newCastling ^ castling) == (IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE)
          || (newCastling ^ castling) == (IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        castling = newCastling;
      }
    } else {
      captureSquare = X88Position.NOPOSITION;
    }

    // Move the piece
    int startPosition = X88Move.getStart(move);
    int piece = move(startPosition, endPosition);
    int chessman = IntChessman.getChessman(piece);

    // Update castling
    switch (startPosition) {
      case X88Position.a1:
        newCastling &= ~IntCastling.WHITE_QUEENSIDE;
        break;
      case X88Position.a8:
        newCastling &= ~IntCastling.BLACK_QUEENSIDE;
        break;
      case X88Position.h1:
        newCastling &= ~IntCastling.WHITE_KINGSIDE;
        break;
      case X88Position.h8:
        newCastling &= ~IntCastling.BLACK_KINGSIDE;
        break;
      case X88Position.e1:
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case X88Position.e8:
        newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        break;
      default:
        break;
    }
    if (newCastling != castling) {
      assert (newCastling ^ castling) == IntCastling.WHITE_KINGSIDE
        || (newCastling ^ castling) == IntCastling.WHITE_QUEENSIDE
        || (newCastling ^ castling) == IntCastling.BLACK_KINGSIDE
        || (newCastling ^ castling) == IntCastling.BLACK_QUEENSIDE
        || (newCastling ^ castling) == (IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE)
        || (newCastling ^ castling) == (IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
      castling = newCastling;
    }

    // Update en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      enPassantSquare = X88Position.NOPOSITION;
    }

    // Update half move clock
    if (chessman == IntChessman.PAWN || target != IntChessman.NOPIECE) {
      halfMoveClock = 0;
    } else {
      halfMoveClock++;
    }
  }

  private void undoMoveNormal(int move) {
    // Move the chessman
    int startPosition = X88Move.getStart(move);
    int endPosition = X88Move.getEnd(move);
    move(endPosition, startPosition);

    // Restore the captured chessman
    int target = X88Move.getTarget(move);
    if (target != IntChessman.NOPIECE) {
      put(captureHistory[--captureHistorySize], endPosition);
    }

    // Restore the castling rights
    castling = castlingHistory[--castlingHistorySize];
  }

  private void makeMovePawnPromotion(int move) {
    // Remove the pawn at the start position
    int startPosition = X88Move.getStart(move);
    int pawn = remove(startPosition);
    assert IntChessman.getChessman(pawn) == IntChessman.PAWN;
    int pawnColor = IntChessman.getColor(pawn);
    assert IntChessman.getChessman(pawn) == X88Move.getChessman(move);
    assert pawnColor == X88Move.getChessmanColor(move);

    // Save the captured chessman
    int endPosition = X88Move.getEnd(move);
    int target = IntChessman.NOPIECE;
    if (board[endPosition] != IntChessman.NOPIECE) {
      // Save the castling rights
      castlingHistory[castlingHistorySize++] = castling;
      int newCastling = castling;

      target = remove(endPosition);
      assert X88Move.getTarget(move) != IntChessman.NOPIECE;
      captureHistory[captureHistorySize++] = target;
      captureSquare = endPosition;

      switch (endPosition) {
        case X88Position.a1:
          newCastling &= ~IntCastling.WHITE_QUEENSIDE;
          break;
        case X88Position.a8:
          newCastling &= ~IntCastling.BLACK_QUEENSIDE;
          break;
        case X88Position.h1:
          newCastling &= ~IntCastling.WHITE_KINGSIDE;
          break;
        case X88Position.h8:
          newCastling &= ~IntCastling.BLACK_KINGSIDE;
          break;
        case X88Position.e1:
          newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
          break;
        case X88Position.e8:
          newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
          break;
        default:
          break;
      }
      if (newCastling != castling) {
        assert (newCastling ^ castling) == IntCastling.WHITE_KINGSIDE
          || (newCastling ^ castling) == IntCastling.WHITE_QUEENSIDE
          || (newCastling ^ castling) == IntCastling.BLACK_KINGSIDE
          || (newCastling ^ castling) == IntCastling.BLACK_QUEENSIDE
          || (newCastling ^ castling) == (IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE)
          || (newCastling ^ castling) == (IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        castling = newCastling;
      }
    } else {
      captureSquare = X88Position.NOPOSITION;
    }

    // Create the promotion chessman
    int promotion = X88Move.getPromotion(move);
    int promotionPiece = IntChessman.createPromotion(promotion, pawnColor);
    put(promotionPiece, endPosition);

    // Update en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      enPassantSquare = X88Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnPromotion(int move) {
    // Remove the promotion chessman at the end position
    int endPosition = X88Move.getEnd(move);
    remove(endPosition);

    // Restore the captured chessman
    int target = X88Move.getTarget(move);
    if (target != IntChessman.NOPIECE) {
      put(captureHistory[--captureHistorySize], endPosition);

      // Restore the castling rights
      castling = castlingHistory[--castlingHistorySize];
    }

    // Put the pawn at the start position
    int pawnChessman = X88Move.getChessman(move);
    int pawnColor = X88Move.getChessmanColor(move);
    int pawnPiece = IntChessman.createPiece(pawnChessman, pawnColor);
    put(pawnPiece, X88Move.getStart(move));
  }

  private void makeMovePawnDouble(int move) {
    // Move the pawn
    int startPosition = X88Move.getStart(move);
    int endPosition = X88Move.getEnd(move);
    int pawn = move(startPosition, endPosition);
    int pawnColor = IntChessman.getColor(pawn);

    assert IntChessman.getChessman(pawn) == IntChessman.PAWN;
    assert (startPosition >>> 4 == 1 && pawnColor == IntColor.WHITE) || (startPosition >>> 4 == 6 && pawnColor == IntColor.BLACK) : getBoard().toString() + ":" + X88Move.toString(move);
    assert (endPosition >>> 4 == 3 && pawnColor == IntColor.WHITE) || (endPosition >>> 4 == 4 && pawnColor == IntColor.BLACK);
    assert Math.abs(startPosition - endPosition) == 32;

    // Update the capture square
    captureSquare = X88Position.NOPOSITION;

    // Calculate the en passant position
    int targetPosition;
    if (pawnColor == IntColor.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      assert pawnColor == IntColor.BLACK;

      targetPosition = endPosition + 16;
    }

    assert (targetPosition & 0x88) == 0;
    assert Math.abs(startPosition - targetPosition) == 16;

    // Update en passant
    enPassantSquare = targetPosition;

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnDouble(int move) {
    // Move the pawn
    move(X88Move.getEnd(move), X88Move.getStart(move));
  }

  private void makeMoveCastling(int move) {
    // Save the castling rights
    castlingHistory[castlingHistorySize++] = castling;
    int newCastling = castling;

    // Move the king
    int kingStartPosition = X88Move.getStart(move);
    int kingEndPosition = X88Move.getEnd(move);
    int king = move(kingStartPosition, kingEndPosition);
    assert IntChessman.getChessman(king) == IntChessman.KING;

    // Get the rook positions
    int rookStartPosition;
    int rookEndPosition;
    switch (kingEndPosition) {
      case X88Position.g1:
        rookStartPosition = X88Position.h1;
        rookEndPosition = X88Position.f1;
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case X88Position.c1:
        rookStartPosition = X88Position.a1;
        rookEndPosition = X88Position.d1;
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case X88Position.g8:
        rookStartPosition = X88Position.h8;
        rookEndPosition = X88Position.f8;
        newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        break;
      case X88Position.c8:
        rookStartPosition = X88Position.a8;
        rookEndPosition = X88Position.d8;
        newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    int rook = move(rookStartPosition, rookEndPosition);
    assert IntChessman.getChessman(rook) == IntChessman.ROOK;

    // Update castling
    assert (newCastling ^ castling) == IntCastling.WHITE_KINGSIDE
      || (newCastling ^ castling) == IntCastling.WHITE_QUEENSIDE
      || (newCastling ^ castling) == IntCastling.BLACK_KINGSIDE
      || (newCastling ^ castling) == IntCastling.BLACK_QUEENSIDE
      || (newCastling ^ castling) == (IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE)
      || (newCastling ^ castling) == (IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
    castling = newCastling;

    // Update the capture square
    captureSquare = X88Position.NOPOSITION;

    // Update en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      enPassantSquare = X88Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock++;
  }

  private void undoMoveCastling(int move) {
    int kingEndPosition = X88Move.getEnd(move);

    // Get the rook positions
    int rookStartPosition;
    int rookEndPosition;
    switch (kingEndPosition) {
      case X88Position.g1:
        rookStartPosition = X88Position.h1;
        rookEndPosition = X88Position.f1;
        break;
      case X88Position.c1:
        rookStartPosition = X88Position.a1;
        rookEndPosition = X88Position.d1;
        break;
      case X88Position.g8:
        rookStartPosition = X88Position.h8;
        rookEndPosition = X88Position.f8;
        break;
      case X88Position.c8:
        rookStartPosition = X88Position.a8;
        rookEndPosition = X88Position.d8;
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    move(rookEndPosition, rookStartPosition);

    // Move the king
    move(kingEndPosition, X88Move.getStart(move));

    // Restore the castling rights
    castling = castlingHistory[--castlingHistorySize];
  }

  private void makeMoveEnPassant(int move) {
    // Move the pawn
    int startPosition = X88Move.getStart(move);
    int endPosition = X88Move.getEnd(move);
    int pawn = move(startPosition, endPosition);
    assert IntChessman.getChessman(pawn) == IntChessman.PAWN;
    int pawnColor = IntChessman.getColor(pawn);

    // Calculate the en passant position
    int targetPosition;
    if (pawnColor == IntColor.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      assert pawnColor == IntColor.BLACK;

      targetPosition = endPosition + 16;
    }

    // Remove the captured pawn
    int target = remove(targetPosition);
    assert X88Move.getTarget(move) != IntChessman.NOPIECE;
    assert IntChessman.getChessman(target) == IntChessman.PAWN;
    assert IntChessman.getColor(target) == IntColor.switchColor(pawnColor);
    captureHistory[captureHistorySize++] = target;

    // Update the capture square
    // This is the end position of the move, not the en passant position
    captureSquare = endPosition;

    // Update en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      enPassantSquare = X88Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMoveEnPassant(int move) {
    // Move the pawn
    int endPosition = X88Move.getEnd(move);
    int pawn = move(endPosition, X88Move.getStart(move));

    // Calculate the en passant position
    int targetPosition;
    if (IntChessman.getColor(pawn) == IntColor.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      assert IntChessman.getColor(pawn) == IntColor.BLACK;

      targetPosition = endPosition + 16;
    }

    // Restore the captured pawn
    put(captureHistory[--captureHistorySize], targetPosition);
  }

  private void makeMoveNull() {
    // Update the capture square
    captureSquare = X88Position.NOPOSITION;

    // Update en passant
    if (enPassantSquare != X88Position.NOPOSITION) {
      enPassantSquare = X88Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock++;
  }

  public String toString() {
    return getBoard().toString();
  }

  private final class StackEntry {

    public int halfMoveClockHistory = 0;
    public int enPassantHistory = 0;
    public int captureSquareHistory = 0;

    public StackEntry() {
      clear();
    }

    public void clear() {
      halfMoveClockHistory = 0;
      enPassantHistory = 0;
      captureSquareHistory = 0;
    }

  }

}
