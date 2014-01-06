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

import com.fluxchess.flux.search.Search;
import com.fluxchess.jcpi.models.*;

import java.util.Random;

public final class Board {

  /**
   * The size of the 0x88 board
   */
  public static final int BOARDSIZE = 128;

  // The size of the history stack
  private static final int STACKSIZE = Search.MAX_MOVES;

  private static final Random random = new Random(0);

  // The zobrist keys
  private static final long zobristActiveColor;
  private static final long[][][] zobristChessman = new long[IntChessman.values.length][IntColor.values.length][BOARDSIZE];
  private static final long[] zobristCastling = new long[IntCastling.ARRAY_DIMENSION];
  private static final long[] zobristEnPassant = new long[BOARDSIZE];

  //## BEGIN 0x88 Board Representation
  public final int[] board = new int[BOARDSIZE];
  //## ENDOF 0x88 Board Representation

  // The chessman lists.
  public final ChessmanList[] pawnList = new ChessmanList[IntColor.values.length];
  public final ChessmanList[] knightList = new ChessmanList[IntColor.values.length];
  public final ChessmanList[] bishopList = new ChessmanList[IntColor.values.length];
  public final ChessmanList[] rookList = new ChessmanList[IntColor.values.length];
  public final ChessmanList[] queenList = new ChessmanList[IntColor.values.length];
  public final ChessmanList[] kingList = new ChessmanList[IntColor.values.length];

  // Board stack
  private final StackEntry[] stack = new StackEntry[STACKSIZE];
  private int stackSize = 0;

  // Zobrist code
  public long zobristCode = 0;

  // Pawn zobrist code
  public long pawnZobristCode = 0;

  // En Passant square
  public int enPassantPosition = Position.NOPOSITION;

  // Castling
  public int castling;
  private final int[] castlingHistory = new int[STACKSIZE];
  private int castlingHistorySize = 0;

  // Capture
  public int capturePosition = Position.NOPOSITION;
  private final int[] captureHistory = new int[STACKSIZE];
  private int captureHistorySize = 0;

  // Half move clock
  public int halfMoveClock = 0;

  // The half move number
  private int halfMoveNumber;

  // The active color
  public int activeColor = IntColor.WHITE;

  // Our repetition table
  private final RepetitionTable repetitionTable;

  // Attack
  private final Attack[][] attackHistory = new Attack[STACKSIZE + 1][IntColor.values.length];
  private int attackHistorySize = 0;
  private final Attack tempAttack = new Attack();

  // Initialize the zobrist keys
  static {
    zobristActiveColor = Math.abs(random.nextLong());

    for (int chessman : IntChessman.values) {
      for (int color : IntColor.values) {
        for (int i = 0; i < BOARDSIZE; i++) {
          zobristChessman[chessman][color][i] = Math.abs(random.nextLong());
        }
      }
    }

    zobristCastling[IntCastling.WHITE_KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntCastling.WHITE_QUEENSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntCastling.BLACK_KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntCastling.BLACK_QUEENSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE] = zobristCastling[IntCastling.WHITE_KINGSIDE] ^ zobristCastling[IntCastling.WHITE_QUEENSIDE];
    zobristCastling[IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE] = zobristCastling[IntCastling.BLACK_KINGSIDE] ^ zobristCastling[IntCastling.BLACK_QUEENSIDE];

    for (int i = 0; i < BOARDSIZE; i++) {
      zobristEnPassant[i] = Math.abs(random.nextLong());
    }
  }

  public Board(GenericBoard newBoard) {
    // Initialize repetition table
    repetitionTable = new RepetitionTable();

    for (int i = 0; i < stack.length; i++) {
      stack[i] = new StackEntry();
    }

    // Initialize the position lists
    for (int color : IntColor.values) {
      pawnList[color] = new ChessmanList();
      knightList[color] = new ChessmanList();
      bishopList[color] = new ChessmanList();
      rookList[color] = new ChessmanList();
      queenList[color] = new ChessmanList();
      kingList[color] = new ChessmanList();
    }

    // Initialize the attack list
    for (int i = 0; i < attackHistory.length; i++) {
      for (int j = 0; j < IntColor.values.length; j++) {
        attackHistory[i][j] = new Attack();
      }
    }

    // Initialize the board
    for (int position : Position.values) {
      board[position] = IntPiece.NOPIECE;

      GenericPiece genericPiece = newBoard.getPiece(Position.toGenericPosition(position));
      if (genericPiece != null) {
        int piece = IntPiece.valueOf(genericPiece);
        put(piece, position, true);
      }
    }

    // Initialize en passant
    if (newBoard.getEnPassant() != null) {
      enPassantPosition = Position.valueOf(newBoard.getEnPassant());
      zobristCode ^= zobristEnPassant[Position.valueOf(newBoard.getEnPassant())];
    }

    // Initialize castling
    castling = 0;
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.KINGSIDE) != null) {
      castling |= IntCastling.WHITE_KINGSIDE;
      zobristCode ^= zobristCastling[IntCastling.WHITE_KINGSIDE];
    }
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.QUEENSIDE) != null) {
      castling |= IntCastling.WHITE_QUEENSIDE;
      zobristCode ^= zobristCastling[IntCastling.WHITE_QUEENSIDE];
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.KINGSIDE) != null) {
      castling |= IntCastling.BLACK_KINGSIDE;
      zobristCode ^= zobristCastling[IntCastling.BLACK_KINGSIDE];
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.QUEENSIDE) != null) {
      castling |= IntCastling.BLACK_QUEENSIDE;
      zobristCode ^= zobristCastling[IntCastling.BLACK_QUEENSIDE];
    }

    // Initialize the active color
    if (activeColor != IntColor.valueOf(newBoard.getActiveColor())) {
      activeColor = IntColor.valueOf(newBoard.getActiveColor());
      zobristCode ^= zobristActiveColor;
      pawnZobristCode ^= zobristActiveColor;
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
   * @param update   true if we should update, false otherwise.
   */
  private void put(int piece, int position, boolean update) {
    assert piece != IntPiece.NOPIECE;
    assert (position & 0x88) == 0;
    assert board[position] == IntPiece.NOPIECE;

    // Store some variables for later use
    int chessman = IntPiece.getChessman(piece);
    int color = IntPiece.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color].add(position);
        if (update) {
          pawnZobristCode ^= zobristChessman[IntChessman.PAWN][color][position];
        }
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
    if (update) {
      zobristCode ^= zobristChessman[chessman][color][position];
    }
  }

  /**
   * Removes the piece from the board at the given position.
   *
   * @param position the position.
   * @param update   true if we should update, false otherwise.
   * @return the removed piece.
   */
  private int remove(int position, boolean update) {
    assert (position & 0x88) == 0;
    assert board[position] != IntPiece.NOPIECE;

    // Get the piece
    int piece = board[position];

    // Store some variables for later use
    int chessman = IntPiece.getChessman(piece);
    int color = IntPiece.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color].remove(position);
        if (update) {
          pawnZobristCode ^= zobristChessman[IntChessman.PAWN][color][position];
        }
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
    board[position] = IntPiece.NOPIECE;
    if (update) {
      zobristCode ^= zobristChessman[chessman][color][position];
    }

    return piece;
  }

  /**
   * Moves the piece from the start position to the end position.
   *
   * @param originPosition  the start position.
   * @param targetPosition    the end position.
   * @param update true if we should update, false otherwise.
   * @return the moved piece.
   */
  private int move(int originPosition, int targetPosition, boolean update) {
    assert (originPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;
    assert board[originPosition] != IntPiece.NOPIECE;
    assert board[targetPosition] == IntPiece.NOPIECE;

    // Get the piece
    int originPiece = board[originPosition];

    // Store some variables for later use
    int originChessman = IntPiece.getChessman(originPiece);
    int originColor = IntPiece.getColor(originPiece);

    switch (originChessman) {
      case IntChessman.PAWN:
        pawnList[originColor].remove(originPosition);
        pawnList[originColor].add(targetPosition);
        if (update) {
          long[] tempZobristChessman = zobristChessman[IntChessman.PAWN][originColor];
          pawnZobristCode ^= tempZobristChessman[originPosition];
          pawnZobristCode ^= tempZobristChessman[targetPosition];
        }
        break;
      case IntChessman.KNIGHT:
        knightList[originColor].remove(originPosition);
        knightList[originColor].add(targetPosition);
        break;
      case IntChessman.BISHOP:
        bishopList[originColor].remove(originPosition);
        bishopList[originColor].add(targetPosition);
        break;
      case IntChessman.ROOK:
        rookList[originColor].remove(originPosition);
        rookList[originColor].add(targetPosition);
        break;
      case IntChessman.QUEEN:
        queenList[originColor].remove(originPosition);
        queenList[originColor].add(targetPosition);
        break;
      case IntChessman.KING:
        kingList[originColor].remove(originPosition);
        kingList[originColor].add(targetPosition);
        break;
      default:
        assert false : originChessman;
        break;
    }

    // Update
    board[originPosition] = IntPiece.NOPIECE;
    board[targetPosition] = originPiece;
    if (update) {
      long[] tempZobristChessman = zobristChessman[originChessman][originColor];
      zobristCode ^= tempZobristChessman[originPosition];
      zobristCode ^= tempZobristChessman[targetPosition];
    }

    return originPiece;
  }

  public GenericBoard getBoard() {
    GenericBoard newBoard = new GenericBoard();

    // Set chessmen
    for (GenericColor color : GenericColor.values()) {
      int intColor = IntColor.valueOf(color);

      for (long positions = pawnList[intColor].positions; positions != 0; positions &= positions - 1) {
        int intPosition = ChessmanList.next(positions);
        assert intPosition != Position.NOPOSITION;
        assert IntPiece.getChessman(board[intPosition]) == IntChessman.PAWN;
        assert IntPiece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Position.toGenericPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.PAWN), position);
      }

      for (long positions = knightList[intColor].positions; positions != 0; positions &= positions - 1) {
        int intPosition = ChessmanList.next(positions);
        assert intPosition != Position.NOPOSITION;
        assert IntPiece.getChessman(board[intPosition]) == IntChessman.KNIGHT;
        assert IntPiece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Position.toGenericPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KNIGHT), position);
      }

      for (long positions = bishopList[intColor].positions; positions != 0; positions &= positions - 1) {
        int intPosition = ChessmanList.next(positions);
        assert intPosition != Position.NOPOSITION;
        assert IntPiece.getChessman(board[intPosition]) == IntChessman.BISHOP;
        assert IntPiece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Position.toGenericPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.BISHOP), position);
      }

      for (long positions = rookList[intColor].positions; positions != 0; positions &= positions - 1) {
        int intPosition = ChessmanList.next(positions);
        assert intPosition != Position.NOPOSITION;
        assert IntPiece.getChessman(board[intPosition]) == IntChessman.ROOK;
        assert IntPiece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Position.toGenericPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.ROOK), position);
      }

      for (long positions = queenList[intColor].positions; positions != 0; positions &= positions - 1) {
        int intPosition = ChessmanList.next(positions);
        assert intPosition != Position.NOPOSITION;
        assert IntPiece.getChessman(board[intPosition]) == IntChessman.QUEEN;
        assert IntPiece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Position.toGenericPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.QUEEN), position);
      }

      assert kingList[intColor].size() == 1;
      int intPosition = ChessmanList.next(kingList[intColor].positions);
      assert intPosition != Position.NOPOSITION;
      assert IntPiece.getChessman(board[intPosition]) == IntChessman.KING;
      assert IntPiece.getColor(board[intPosition]) == intColor;

      GenericPosition position = Position.toGenericPosition(intPosition);
      newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KING), position);
    }

    // Set active color
    newBoard.setActiveColor(IntColor.toGenericColor(activeColor));

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
    if (enPassantPosition != Position.NOPOSITION) {
      newBoard.setEnPassant(Position.toGenericPosition(enPassantPosition));
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
    if (activeColor == IntColor.valueOf(GenericColor.BLACK)) {
      halfMoveNumber++;
    }
  }

  /**
   * Returns whether this board state is a repetition.
   *
   * @return true if this board state is a repetition, false otherwise.
   */
  public boolean isRepetition() {
    return repetitionTable.exists(zobristCode);
  }

  /**
   * Returns whether this move checks the opponent king.
   *
   * @param move the move.
   * @return true if this move checks the opponent king.
   */
  public boolean isCheckingMove(int move) {
    assert move != Move.NOMOVE;

    int chessmanColor = IntPiece.getColor(Move.getOriginPiece(move));
    int targetPosition = Move.getTargetPosition(move);
    int enemyKingColor = IntColor.opposite(chessmanColor);
    int enemyKingPosition = ChessmanList.next(kingList[enemyKingColor].positions);

    switch (Move.getType(move)) {
      case Move.Type.NORMAL:
      case Move.Type.PAWNDOUBLE:
        int chessman = IntPiece.getChessman(Move.getOriginPiece(move));

        // Direct attacks
        if (canAttack(chessman, chessmanColor, targetPosition, enemyKingPosition)) {
          return true;
        }

        int originPosition = Move.getOriginPosition(move);

        if (isPinned(originPosition, enemyKingColor)) {
          // We are pinned. Test if we move on the line.
          int attackDeltaOrigin = Attack.deltas[enemyKingPosition - originPosition + 127];
          int attackDeltaTarget = Attack.deltas[enemyKingPosition - targetPosition + 127];
          return attackDeltaOrigin != attackDeltaTarget;
        }
        // Indirect attacks
        break;
      case Move.Type.PAWNPROMOTION:
      case Move.Type.ENPASSANT:
        // We do a slow test for complex moves
        makeMove(move);
        boolean isCheck = isAttacked(enemyKingPosition, chessmanColor);
        undoMove(move);
        return isCheck;
      case Move.Type.CASTLING:
        int rookTargetPosition = Position.NOPOSITION;

        if (targetPosition == Position.g1) {
          assert chessmanColor == IntColor.WHITE;
          rookTargetPosition = Position.f1;
        } else if (targetPosition == Position.g8) {
          assert chessmanColor == IntColor.BLACK;
          rookTargetPosition = Position.f8;
        } else if (targetPosition == Position.c1) {
          assert chessmanColor == IntColor.WHITE;
          rookTargetPosition = Position.d1;
        } else if (targetPosition == Position.c8) {
          assert chessmanColor == IntColor.BLACK;
          rookTargetPosition = Position.d8;
        } else {
          assert false : targetPosition;
        }

        return canAttack(IntChessman.ROOK, chessmanColor, rookTargetPosition, enemyKingPosition);
      case Move.Type.NULL:
        assert false;
        break;
      default:
        assert false : Move.getType(move);
        break;
    }

    return false;
  }

  public boolean isPinned(int chessmanPosition, int kingColor) {
    assert chessmanPosition != Position.NOPOSITION;
    assert kingColor != IntColor.NOCOLOR;

    int myKingPosition = ChessmanList.next(kingList[kingColor].positions);

    // We can only be pinned on an attack line
    int vector = Attack.vector[myKingPosition - chessmanPosition + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    int delta = Attack.deltas[myKingPosition - chessmanPosition + 127];

    // Walk towards the king
    int position = chessmanPosition + delta;
    assert (position & 0x88) == 0;
    while (board[position] == IntPiece.NOPIECE) {
      position += delta;
      assert (position & 0x88) == 0;
    }
    if (position != myKingPosition) {
      // There's a blocker between me and the king
      return false;
    }

    // Walk away from the king
    position = chessmanPosition - delta;
    while ((position & 0x88) == 0) {
      int attacker = board[position];
      if (attacker != IntPiece.NOPIECE) {
        int attackerColor = IntPiece.getColor(attacker);

        return kingColor != attackerColor && canSliderPseudoAttack(attacker, position, myKingPosition);
      } else {
        position -= delta;
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
    assert attacker != IntPiece.NOPIECE;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector;

    switch (IntPiece.getChessman(attacker)) {
      case IntChessman.PAWN:
        break;
      case IntChessman.KNIGHT:
        break;
      case IntChessman.BISHOP:
        attackVector = Attack.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case Attack.u:
          case Attack.d:
          case Attack.D:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.ROOK:
        attackVector = Attack.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case Attack.s:
          case Attack.S:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.QUEEN:
        attackVector = Attack.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case Attack.u:
          case Attack.d:
          case Attack.s:
          case Attack.D:
          case Attack.S:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.KING:
        break;
      default:
        assert false : IntPiece.getChessman(attacker);
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

    assert kingList[color].size() == 1;

    int attackerColor = IntColor.opposite(color);
    getAttack(attack, ChessmanList.next(kingList[color].positions), attackerColor, false);

    return attack;
  }

  /**
   * Returns whether or not the position is attacked.
   *
   * @param targetPosition the Position.
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
    assert targetPosition != Position.NOPOSITION;
    assert attackerColor != IntColor.NOCOLOR;

    attack.count = 0;

    // Pawn attacks
    int pawnPiece = IntPiece.WHITEPAWN;
    int sign = -1;
    if (attackerColor == IntColor.BLACK) {
      pawnPiece = IntPiece.BLACKPAWN;
      sign = 1;
    } else {
      assert attackerColor == IntColor.WHITE;
    }
    int pawnAttackerPosition = targetPosition + sign * 15;
    if ((pawnAttackerPosition & 0x88) == 0) {
      int pawn = board[pawnAttackerPosition];
      if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert Attack.deltas[targetPosition - pawnAttackerPosition + 127] == sign * -15;
        attack.position[attack.count] = pawnAttackerPosition;
        attack.delta[attack.count] = sign * -15;
        attack.count++;
      }
    }
    pawnAttackerPosition = targetPosition + sign * 17;
    if ((pawnAttackerPosition & 0x88) == 0) {
      int pawn = board[pawnAttackerPosition];
      if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert Attack.deltas[targetPosition - pawnAttackerPosition + 127] == sign * -17;
        attack.position[attack.count] = pawnAttackerPosition;
        attack.delta[attack.count] = sign * -17;
        attack.count++;
      }
    }
    for (long positions = knightList[attackerColor].positions; positions != 0; positions &= positions - 1) {
      int attackerPosition = ChessmanList.next(positions);
      assert IntPiece.getChessman(board[attackerPosition]) == IntChessman.KNIGHT;
      assert attackerPosition != Position.NOPOSITION;
      assert board[attackerPosition] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.KNIGHT, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long positions = bishopList[attackerColor].positions; positions != 0; positions &= positions - 1) {
      int attackerPosition = ChessmanList.next(positions);
      assert IntPiece.getChessman(board[attackerPosition]) == IntChessman.BISHOP;
      assert attackerPosition != Position.NOPOSITION;
      assert board[attackerPosition] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.BISHOP, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long positions = rookList[attackerColor].positions; positions != 0; positions &= positions - 1) {
      int attackerPosition = ChessmanList.next(positions);
      assert IntPiece.getChessman(board[attackerPosition]) == IntChessman.ROOK;
      assert attackerPosition != Position.NOPOSITION;
      assert board[attackerPosition] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.ROOK, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long positions = queenList[attackerColor].positions; positions != 0; positions &= positions - 1) {
      int attackerPosition = ChessmanList.next(positions);
      assert IntPiece.getChessman(board[attackerPosition]) == IntChessman.QUEEN;
      assert attackerPosition != Position.NOPOSITION;
      assert board[attackerPosition] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerPosition]);
      if (canAttack(IntChessman.QUEEN, attackerColor, attackerPosition, targetPosition)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];
        assert attackDelta != 0;
        attack.position[attack.count] = attackerPosition;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    assert kingList[attackerColor].size() == 1;
    int attackerPosition = ChessmanList.next(kingList[attackerColor].positions);
    assert IntPiece.getChessman(board[attackerPosition]) == IntChessman.KING;
    assert attackerPosition != Position.NOPOSITION;
    assert board[attackerPosition] != IntPiece.NOPIECE;
    assert attackerColor == IntPiece.getColor(board[attackerPosition]);
    if (canAttack(IntChessman.KING, attackerColor, attackerPosition, targetPosition)) {
      if (stop) {
        return true;
      }
      int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];
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
    assert attackerChessman != IntChessman.NOCHESSMAN;
    assert attackerColor != IntColor.NOCOLOR;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector = Attack.vector[targetPosition - attackerPosition + 127];

    switch (attackerChessman) {
      case IntChessman.PAWN:
        if (attackVector == Attack.u && attackerColor == IntColor.WHITE) {
          return true;
        } else if (attackVector == Attack.d && attackerColor == IntColor.BLACK) {
          return true;
        }
        break;
      case IntChessman.KNIGHT:
        if (attackVector == Attack.K) {
          return true;
        }
        break;
      case IntChessman.BISHOP:
        switch (attackVector) {
          case Attack.u:
          case Attack.d:
            return true;
          case Attack.D:
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
          case Attack.s:
            return true;
          case Attack.S:
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
          case Attack.u:
          case Attack.d:
          case Attack.s:
            return true;
          case Attack.D:
          case Attack.S:
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
          case Attack.u:
          case Attack.d:
          case Attack.s:
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

    int attackDelta = Attack.deltas[targetPosition - attackerPosition + 127];

    int position = attackerPosition + attackDelta;
    while ((position & 0x88) == 0 && position != targetPosition && board[position] == IntPiece.NOPIECE) {
      position += attackDelta;
    }

    return position == targetPosition;
  }

  public void makeMove(int move) {
    // Get current stack entry
    StackEntry currentStackEntry = stack[stackSize];

    // Save history
    currentStackEntry.zobristCode = zobristCode;
    currentStackEntry.pawnZobristCode = pawnZobristCode;
    currentStackEntry.halfMoveClock = halfMoveClock;
    currentStackEntry.enPassant = enPassantPosition;
    currentStackEntry.capturePosition = capturePosition;

    // Update stack size
    stackSize++;
    assert stackSize < STACKSIZE;

    int type = Move.getType(move);

    switch (type) {
      case Move.Type.NORMAL:
        repetitionTable.put(zobristCode);
        makeMoveNormal(move);
        break;
      case Move.Type.PAWNDOUBLE:
        repetitionTable.put(zobristCode);
        makeMovePawnDouble(move);
        break;
      case Move.Type.PAWNPROMOTION:
        repetitionTable.put(zobristCode);
        makeMovePawnPromotion(move);
        break;
      case Move.Type.ENPASSANT:
        repetitionTable.put(zobristCode);
        makeMoveEnPassant(move);
        break;
      case Move.Type.CASTLING:
        repetitionTable.put(zobristCode);
        makeMoveCastling(move);
        break;
      case Move.Type.NULL:
        makeMoveNull();
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Update half move number
    halfMoveNumber++;

    // Update active color
    activeColor = IntColor.opposite(activeColor);
    zobristCode ^= zobristActiveColor;
    pawnZobristCode ^= zobristActiveColor;

    // Update attack list
    attackHistorySize++;
    attackHistory[attackHistorySize][IntColor.WHITE].count = Attack.NOATTACK;
    attackHistory[attackHistorySize][IntColor.BLACK].count = Attack.NOATTACK;
  }

  public void undoMove(int move) {
    int type = Move.getType(move);

    // Update attack list
    attackHistorySize--;

    // Update active color
    activeColor = IntColor.opposite(activeColor);

    // Update half move number
    halfMoveNumber--;

    // Update stack size
    stackSize--;
    assert stackSize >= 0;

    // Get current stack entry
    StackEntry currentStackEntry = stack[stackSize];

    // Restore zobrist history
    zobristCode = currentStackEntry.zobristCode;
    pawnZobristCode = currentStackEntry.pawnZobristCode;
    halfMoveClock = currentStackEntry.halfMoveClock;
    enPassantPosition = currentStackEntry.enPassant;
    capturePosition = currentStackEntry.capturePosition;

    switch (type) {
      case Move.Type.NORMAL:
        undoMoveNormal(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.PAWNDOUBLE:
        undoMovePawnDouble(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.PAWNPROMOTION:
        undoMovePawnPromotion(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.ENPASSANT:
        undoMoveEnPassant(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.CASTLING:
        undoMoveCastling(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.NULL:
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
    int targetPosition = Move.getTargetPosition(move);
    int target = IntPiece.NOPIECE;
    if (board[targetPosition] != IntPiece.NOPIECE) {
      target = remove(targetPosition, true);
      assert Move.getTargetPiece(move) != IntPiece.NOPIECE : Move.toString(move);
      captureHistory[captureHistorySize++] = target;
      capturePosition = targetPosition;

      switch (targetPosition) {
        case Position.a1:
          newCastling &= ~IntCastling.WHITE_QUEENSIDE;
          break;
        case Position.a8:
          newCastling &= ~IntCastling.BLACK_QUEENSIDE;
          break;
        case Position.h1:
          newCastling &= ~IntCastling.WHITE_KINGSIDE;
          break;
        case Position.h8:
          newCastling &= ~IntCastling.BLACK_KINGSIDE;
          break;
        case Position.e1:
          newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
          break;
        case Position.e8:
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
        zobristCode ^= zobristCastling[newCastling ^ castling];
        castling = newCastling;
      }
    } else {
      capturePosition = Position.NOPOSITION;
    }

    // Move the piece
    int originPosition = Move.getOriginPosition(move);
    int originPiece = move(originPosition, targetPosition, true);

    // Update castling
    switch (originPosition) {
      case Position.a1:
        newCastling &= ~IntCastling.WHITE_QUEENSIDE;
        break;
      case Position.a8:
        newCastling &= ~IntCastling.BLACK_QUEENSIDE;
        break;
      case Position.h1:
        newCastling &= ~IntCastling.WHITE_KINGSIDE;
        break;
      case Position.h8:
        newCastling &= ~IntCastling.BLACK_KINGSIDE;
        break;
      case Position.e1:
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case Position.e8:
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
      zobristCode ^= zobristCastling[newCastling ^ castling];
      castling = newCastling;
    }

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
      enPassantPosition = Position.NOPOSITION;
    }

    // Update half move clock
    if (IntPiece.getChessman(originPiece) == IntChessman.PAWN || target != IntPiece.NOPIECE) {
      halfMoveClock = 0;
    } else {
      halfMoveClock++;
    }
  }

  private void undoMoveNormal(int move) {
    // Move the chessman
    int originPosition = Move.getOriginPosition(move);
    int targetPosition = Move.getTargetPosition(move);
    move(targetPosition, originPosition, false);

    // Restore the captured chessman
    if (Move.getTargetPiece(move) != IntPiece.NOPIECE) {
      put(captureHistory[--captureHistorySize], targetPosition, false);
    }

    // Restore the castling rights
    castling = castlingHistory[--castlingHistorySize];
  }

  private void makeMovePawnPromotion(int move) {
    // Remove the pawn at the origin position
    int originPosition = Move.getOriginPosition(move);
    int pawnPiece = remove(originPosition, true);
    assert IntPiece.getChessman(pawnPiece) == IntChessman.PAWN;
    int pawnColor = IntPiece.getColor(pawnPiece);
    assert IntPiece.getChessman(pawnPiece) == IntPiece.getChessman(Move.getOriginPiece(move));
    assert pawnColor == IntPiece.getColor(Move.getOriginPiece(move));

    // Save the captured chessman
    int targetPosition = Move.getTargetPosition(move);
    int targetPiece;
    if (board[targetPosition] != IntPiece.NOPIECE) {
      // Save the castling rights
      castlingHistory[castlingHistorySize++] = castling;
      int newCastling = castling;

      targetPiece = remove(targetPosition, true);
      assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
      captureHistory[captureHistorySize++] = targetPiece;
      capturePosition = targetPosition;

      switch (targetPosition) {
        case Position.a1:
          newCastling &= ~IntCastling.WHITE_QUEENSIDE;
          break;
        case Position.a8:
          newCastling &= ~IntCastling.BLACK_QUEENSIDE;
          break;
        case Position.h1:
          newCastling &= ~IntCastling.WHITE_KINGSIDE;
          break;
        case Position.h8:
          newCastling &= ~IntCastling.BLACK_KINGSIDE;
          break;
        case Position.e1:
          newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
          break;
        case Position.e8:
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
        zobristCode ^= zobristCastling[newCastling ^ castling];
        castling = newCastling;
      }
    } else {
      capturePosition = Position.NOPOSITION;
    }

    // Create the promotion chessman
    int promotion = Move.getPromotion(move);
    int promotionPiece = IntPiece.valueOf(promotion, pawnColor);
    put(promotionPiece, targetPosition, true);

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
      enPassantPosition = Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnPromotion(int move) {
    // Remove the promotion chessman at the end position
    int targetPosition = Move.getTargetPosition(move);
    remove(targetPosition, false);

    // Restore the captured chessman
    if (Move.getTargetPiece(move) != IntPiece.NOPIECE) {
      put(captureHistory[--captureHistorySize], targetPosition, false);

      // Restore the castling rights
      castling = castlingHistory[--castlingHistorySize];
    }

    // Put the pawn at the origin position
    put(Move.getOriginPiece(move), Move.getOriginPosition(move), false);
  }

  private void makeMovePawnDouble(int move) {
    // Move the pawn
    int originPosition = Move.getOriginPosition(move);
    int targetPosition = Move.getTargetPosition(move);
    int pawnPiece = move(originPosition, targetPosition, true);
    int pawnColor = IntPiece.getColor(pawnPiece);

    assert IntPiece.getChessman(pawnPiece) == IntChessman.PAWN;
    assert (originPosition >>> 4 == 1 && pawnColor == IntColor.WHITE) || (originPosition >>> 4 == 6 && pawnColor == IntColor.BLACK) : getBoard().toString() + ":" + Move.toString(move);
    assert (targetPosition >>> 4 == 3 && pawnColor == IntColor.WHITE) || (targetPosition >>> 4 == 4 && pawnColor == IntColor.BLACK);
    assert Math.abs(originPosition - targetPosition) == 32;

    // Update the capture square
    capturePosition = Position.NOPOSITION;

    // Calculate the en passant position
    int capturePosition;
    if (pawnColor == IntColor.WHITE) {
      capturePosition = targetPosition - 16;
    } else {
      capturePosition = targetPosition + 16;
    }

    assert (capturePosition & 0x88) == 0;
    assert Math.abs(originPosition - capturePosition) == 16;

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
    }

    enPassantPosition = capturePosition;
    zobristCode ^= zobristEnPassant[capturePosition];

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnDouble(int move) {
    // Move the pawn
    move(Move.getTargetPosition(move), Move.getOriginPosition(move), false);
  }

  private void makeMoveCastling(int move) {
    // Save the castling rights
    castlingHistory[castlingHistorySize++] = castling;
    int newCastling = castling;

    // Move the king
    int kingOriginPosition = Move.getOriginPosition(move);
    int kingTargetPosition = Move.getTargetPosition(move);
    int king = move(kingOriginPosition, kingTargetPosition, true);
    assert IntPiece.getChessman(king) == IntChessman.KING;

    // Get the rook positions
    int rookOriginPosition;
    int rookTargetPosition;
    switch (kingTargetPosition) {
      case Position.g1:
        rookOriginPosition = Position.h1;
        rookTargetPosition = Position.f1;
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case Position.c1:
        rookOriginPosition = Position.a1;
        rookTargetPosition = Position.d1;
        newCastling &= ~(IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE);
        break;
      case Position.g8:
        rookOriginPosition = Position.h8;
        rookTargetPosition = Position.f8;
        newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        break;
      case Position.c8:
        rookOriginPosition = Position.a8;
        rookTargetPosition = Position.d8;
        newCastling &= ~(IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    int rook = move(rookOriginPosition, rookTargetPosition, true);
    assert IntPiece.getChessman(rook) == IntChessman.ROOK;

    // Update castling
    assert (newCastling ^ castling) == IntCastling.WHITE_KINGSIDE
      || (newCastling ^ castling) == IntCastling.WHITE_QUEENSIDE
      || (newCastling ^ castling) == IntCastling.BLACK_KINGSIDE
      || (newCastling ^ castling) == IntCastling.BLACK_QUEENSIDE
      || (newCastling ^ castling) == (IntCastling.WHITE_KINGSIDE | IntCastling.WHITE_QUEENSIDE)
      || (newCastling ^ castling) == (IntCastling.BLACK_KINGSIDE | IntCastling.BLACK_QUEENSIDE);
    zobristCode ^= zobristCastling[newCastling ^ castling];
    castling = newCastling;

    // Update the capture square
    capturePosition = Position.NOPOSITION;

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
      enPassantPosition = Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock++;
  }

  private void undoMoveCastling(int move) {
    int kingTargetPosition = Move.getTargetPosition(move);

    // Get the rook positions
    int rookOriginPosition;
    int rookTargetPosition;
    switch (kingTargetPosition) {
      case Position.g1:
        rookOriginPosition = Position.h1;
        rookTargetPosition = Position.f1;
        break;
      case Position.c1:
        rookOriginPosition = Position.a1;
        rookTargetPosition = Position.d1;
        break;
      case Position.g8:
        rookOriginPosition = Position.h8;
        rookTargetPosition = Position.f8;
        break;
      case Position.c8:
        rookOriginPosition = Position.a8;
        rookTargetPosition = Position.d8;
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    move(rookTargetPosition, rookOriginPosition, false);

    // Move the king
    move(kingTargetPosition, Move.getOriginPosition(move), false);

    // Restore the castling rights
    castling = castlingHistory[--castlingHistorySize];
  }

  private void makeMoveEnPassant(int move) {
    // Move the pawn
    int originPosition = Move.getOriginPosition(move);
    int targetPosition = Move.getTargetPosition(move);
    int pawn = move(originPosition, targetPosition, true);
    assert IntPiece.getChessman(pawn) == IntChessman.PAWN;
    int pawnColor = IntPiece.getColor(pawn);

    // Calculate the en passant position
    int capturePosition;
    if (pawnColor == IntColor.WHITE) {
      capturePosition = targetPosition - 16;
    } else {
      assert pawnColor == IntColor.BLACK;

      capturePosition = targetPosition + 16;
    }

    // Remove the captured pawn
    int target = remove(capturePosition, true);
    assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
    assert IntPiece.getChessman(target) == IntChessman.PAWN;
    assert IntPiece.getColor(target) == IntColor.opposite(pawnColor);
    captureHistory[captureHistorySize++] = target;

    // Update the capture square
    // This is the end position of the move, not the en passant position
    this.capturePosition = targetPosition;

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
      enPassantPosition = Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMoveEnPassant(int move) {
    // Move the pawn
    int targetPosition = Move.getTargetPosition(move);
    int pawnPiece = move(targetPosition, Move.getOriginPosition(move), false);

    // Calculate the en passant position
    int capturePosition;
    if (IntPiece.getColor(pawnPiece) == IntColor.WHITE) {
      capturePosition = targetPosition - 16;
    } else {
      assert IntPiece.getColor(pawnPiece) == IntColor.BLACK;

      capturePosition = targetPosition + 16;
    }

    // Restore the captured pawn
    put(captureHistory[--captureHistorySize], capturePosition, false);
  }

  private void makeMoveNull() {
    // Update the capture square
    capturePosition = Position.NOPOSITION;

    // Update en passant
    if (enPassantPosition != Position.NOPOSITION) {
      zobristCode ^= zobristEnPassant[enPassantPosition];
      enPassantPosition = Position.NOPOSITION;
    }

    // Update half move clock
    halfMoveClock++;
  }

  public String toString() {
    return getBoard().toString();
  }

  private final class StackEntry {

    public long zobristCode = 0;
    public long pawnZobristCode = 0;
    public int halfMoveClock = 0;
    public int enPassant = 0;
    public int capturePosition = 0;

    public StackEntry() {
      clear();
    }

    public void clear() {
      zobristCode = 0;
      pawnZobristCode = 0;
      halfMoveClock = 0;
      enPassant = 0;
      capturePosition = 0;
    }

  }

}
