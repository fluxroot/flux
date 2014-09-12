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

import com.fluxchess.jcpi.models.*;

import java.util.Random;

final class Position {

  /**
   * The size of the 0x88 board
   */
  static final int BOARDSIZE = 128;

  // The size of the history stack
  private static final int STACKSIZE = Search.MAX_MOVES;

  // Game phase thresholds
  static final int GAMEPHASE_OPENING_VALUE =
      Piece.VALUE_KING
          + 1 * Piece.VALUE_QUEEN
          + 2 * Piece.VALUE_ROOK
          + 2 * Piece.VALUE_BISHOP
          + 2 * Piece.VALUE_KNIGHT;
  static final int GAMEPHASE_ENDGAME_VALUE =
      Piece.VALUE_KING
          + 2 * Piece.VALUE_ROOK;
  private static final int GAMEPHASE_ENDGAME_COUNT = 2;

  private static final Random random = new Random(0);

  // The zobrist keys
  private static final long zobristActiveColor;
  private static final long[][][] zobristChessman = new long[PieceType.VALUES_SIZE][Color.ARRAY_DIMENSION][BOARDSIZE];
  private static final long[] zobristCastling = new long[Castling.ARRAY_DIMENSION];
  private static final long[] zobristEnPassant = new long[BOARDSIZE];

  //## BEGIN 0x88 Board Representation
  static final int[] board = new int[BOARDSIZE];
  //## ENDOF 0x88 Board Representation

  // The chessman lists.
  static final PositionList[] pawnList = new PositionList[Color.ARRAY_DIMENSION];
  static final PositionList[] knightList = new PositionList[Color.ARRAY_DIMENSION];
  static final PositionList[] bishopList = new PositionList[Color.ARRAY_DIMENSION];
  static final PositionList[] rookList = new PositionList[Color.ARRAY_DIMENSION];
  static final PositionList[] queenList = new PositionList[Color.ARRAY_DIMENSION];
  static final PositionList[] kingList = new PositionList[Color.ARRAY_DIMENSION];

  // Board stack
  private static final State[] states = new State[STACKSIZE];
  private int statesSize = 0;

  // Zobrist code
  long zobristCode = 0;

  // Pawn zobrist code
  long pawnZobristCode = 0;

  // En Passant square
  int enPassantSquare = Square.NOPOSITION;

  // Castling
  static int castling;
  private static final int[] castlingHistory = new int[STACKSIZE];
  private int castlingHistorySize = 0;

  // Capture
  int captureSquare = Square.NOPOSITION;
  private static final int[] captureHistory = new int[STACKSIZE];
  private int captureHistorySize = 0;

  // Half move clock
  int halfMoveClock = 0;

  // The half move number
  private int halfMoveNumber;

  // The active color
  int activeColor = Color.WHITE;

  // The material value and counter. We always keep the values current.
  static final int[] materialValue = new int[Color.ARRAY_DIMENSION];
  static final int[] materialCount = new int[Color.ARRAY_DIMENSION];
  static final int[] materialCountAll = new int[Color.ARRAY_DIMENSION];

  // The positional values. We always keep the values current.
  static final int[] positionValueOpening = new int[Color.ARRAY_DIMENSION];
  static final int[] positionValueEndgame = new int[Color.ARRAY_DIMENSION];

  // Attack
  private static final Attack[][] attackHistory = new Attack[STACKSIZE + 1][Color.ARRAY_DIMENSION];
  private int attackHistorySize = 0;
  private static final Attack tempAttack = new Attack();

  private static final class State {
    long zobristHistory = 0;
    long pawnZobristHistory = 0;
    int halfMoveClockHistory = 0;
    int enPassantHistory = 0;
    int captureSquareHistory = 0;
    final int[] positionValueOpening = new int[Color.ARRAY_DIMENSION];
    final int[] positionValueEndgame = new int[Color.ARRAY_DIMENSION];

    State() {
      clear();
    }

    void clear() {
      this.zobristHistory = 0;
      this.pawnZobristHistory = 0;
      this.halfMoveClockHistory = 0;
      this.enPassantHistory = 0;
      this.captureSquareHistory = 0;
      for (int color : Color.values) {
        this.positionValueOpening[color] = 0;
        this.positionValueEndgame[color] = 0;
      }
    }
  }

  // Initialize the zobrist keys
  static {
    zobristActiveColor = Math.abs(random.nextLong());

    for (int chessman : PieceType.values) {
      for (int color : Color.values) {
        for (int i = 0; i < BOARDSIZE; i++) {
          zobristChessman[chessman][color][i] = Math.abs(random.nextLong());
        }
      }
    }

    zobristCastling[Castling.WHITE_KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[Castling.WHITE_QUEENSIDE] = Math.abs(random.nextLong());
    zobristCastling[Castling.BLACK_KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[Castling.BLACK_QUEENSIDE] = Math.abs(random.nextLong());
    zobristCastling[Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE] = zobristCastling[Castling.WHITE_KINGSIDE] ^ zobristCastling[Castling.WHITE_QUEENSIDE];
    zobristCastling[Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE] = zobristCastling[Castling.BLACK_KINGSIDE] ^ zobristCastling[Castling.BLACK_QUEENSIDE];

    for (int i = 0; i < BOARDSIZE; i++) {
      zobristEnPassant[i] = Math.abs(random.nextLong());
    }

    for (int i = 0; i < states.length; i++) {
      states[i] = new State();
    }
  }

  /**
   * Creates a new board.
   *
   * @param newBoard the board to setup our own board.
   */
  Position(GenericBoard newBoard) {
    // Initialize the position lists
    for (int color : Color.values) {
      pawnList[color] = new PositionList();
      knightList[color] = new PositionList();
      bishopList[color] = new PositionList();
      rookList[color] = new PositionList();
      queenList[color] = new PositionList();
      kingList[color] = new PositionList();
    }

    // Initialize the attack list
    for (int i = 0; i < attackHistory.length; i++) {
      for (int j = 0; j < Color.ARRAY_DIMENSION; j++) {
        attackHistory[i][j] = new Attack();
      }
    }

    // Initialize the material values and counters
    for (int color : Color.values) {
      materialValue[color] = 0;
      materialCount[color] = 0;
      materialCountAll[color] = 0;
    }

    // Initialize the positional values
    for (int color : Color.values) {
      positionValueOpening[color] = 0;
      positionValueEndgame[color] = 0;
    }

    // Initialize the board
    for (int position : Square.values) {
      board[position] = Piece.NOPIECE;

      GenericPiece genericPiece = newBoard.getPiece(Square.valueOfIntPosition(position));
      if (genericPiece != null) {
        int piece = Piece.createPiece(Piece.valueOfChessman(genericPiece.chessman), Color.valueOfColor(genericPiece.color));
        put(piece, position, true);
      }
    }

    // Initialize en passant
    if (newBoard.getEnPassant() != null) {
      this.enPassantSquare = Square.valueOfPosition(newBoard.getEnPassant());
      this.zobristCode ^= zobristEnPassant[Square.valueOfPosition(newBoard.getEnPassant())];
    }

    // Initialize castling
    castling = 0;
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.KINGSIDE) != null) {
      castling |= Castling.WHITE_KINGSIDE;
      this.zobristCode ^= zobristCastling[Castling.WHITE_KINGSIDE];
    }
    if (newBoard.getCastling(GenericColor.WHITE, GenericCastling.QUEENSIDE) != null) {
      castling |= Castling.WHITE_QUEENSIDE;
      this.zobristCode ^= zobristCastling[Castling.WHITE_QUEENSIDE];
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.KINGSIDE) != null) {
      castling |= Castling.BLACK_KINGSIDE;
      this.zobristCode ^= zobristCastling[Castling.BLACK_KINGSIDE];
    }
    if (newBoard.getCastling(GenericColor.BLACK, GenericCastling.QUEENSIDE) != null) {
      castling |= Castling.BLACK_QUEENSIDE;
      this.zobristCode ^= zobristCastling[Castling.BLACK_QUEENSIDE];
    }

    // Initialize the active color
    if (this.activeColor != Color.valueOfColor(newBoard.getActiveColor())) {
      this.activeColor = Color.valueOfColor(newBoard.getActiveColor());
      this.zobristCode ^= zobristActiveColor;
      this.pawnZobristCode ^= zobristActiveColor;
    }

    // Initialize the half move clock
    assert newBoard.getHalfMoveClock() >= 0;
    this.halfMoveClock = newBoard.getHalfMoveClock();

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
    assert piece != Piece.NOPIECE;
    assert (position & 0x88) == 0;
    assert board[position] == Piece.NOPIECE;

    // Store some variables for later use
    int chessman = Piece.getChessman(piece);
    int color = Piece.getColor(piece);

    switch (chessman) {
      case PieceType.PAWN:
        pawnList[color].addPosition(position);
        materialCountAll[color]++;
        if (update) {
          this.pawnZobristCode ^= zobristChessman[PieceType.PAWN][color][position];
        }
        break;
      case PieceType.KNIGHT:
        knightList[color].addPosition(position);
        materialCount[color]++;
        materialCountAll[color]++;
        break;
      case PieceType.BISHOP:
        bishopList[color].addPosition(position);
        materialCount[color]++;
        materialCountAll[color]++;
        break;
      case PieceType.ROOK:
        rookList[color].addPosition(position);
        materialCount[color]++;
        materialCountAll[color]++;
        break;
      case PieceType.QUEEN:
        queenList[color].addPosition(position);
        materialCount[color]++;
        materialCountAll[color]++;
        break;
      case PieceType.KING:
        kingList[color].addPosition(position);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[position] = piece;
    materialValue[color] += Piece.getValueFromChessman(chessman);
    if (update) {
      this.zobristCode ^= zobristChessman[chessman][color][position];
      positionValueOpening[color] += PieceSquareTable.getValue(GamePhase.OPENING, chessman, color, position);
      positionValueEndgame[color] += PieceSquareTable.getValue(GamePhase.ENDGAME, chessman, color, position);
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
    assert board[position] != Piece.NOPIECE;

    // Get the piece
    int piece = board[position];

    // Store some variables for later use
    int chessman = Piece.getChessman(piece);
    int color = Piece.getColor(piece);

    switch (chessman) {
      case PieceType.PAWN:
        pawnList[color].removePosition(position);
        materialCountAll[color]--;
        if (update) {
          this.pawnZobristCode ^= zobristChessman[PieceType.PAWN][color][position];
        }
        break;
      case PieceType.KNIGHT:
        knightList[color].removePosition(position);
        materialCount[color]--;
        materialCountAll[color]--;
        break;
      case PieceType.BISHOP:
        bishopList[color].removePosition(position);
        materialCount[color]--;
        materialCountAll[color]--;
        break;
      case PieceType.ROOK:
        rookList[color].removePosition(position);
        materialCount[color]--;
        materialCountAll[color]--;
        break;
      case PieceType.QUEEN:
        queenList[color].removePosition(position);
        materialCount[color]--;
        materialCountAll[color]--;
        break;
      case PieceType.KING:
        kingList[color].removePosition(position);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[position] = Piece.NOPIECE;
    materialValue[color] -= Piece.getValueFromChessman(chessman);
    if (update) {
      this.zobristCode ^= zobristChessman[chessman][color][position];
      positionValueOpening[color] -= PieceSquareTable.getValue(GamePhase.OPENING, chessman, color, position);
      positionValueEndgame[color] -= PieceSquareTable.getValue(GamePhase.ENDGAME, chessman, color, position);
    }

    return piece;
  }

  /**
   * Moves the piece from the start position to the end position.
   *
   * @param start  the start position.
   * @param end    the end position.
   * @param update true if we should update, false otherwise.
   * @return the moved piece.
   */
  private int move(int start, int end, boolean update) {
    assert (start & 0x88) == 0;
    assert (end & 0x88) == 0;
    assert board[start] != Piece.NOPIECE;
    assert board[end] == Piece.NOPIECE;

    // Get the piece
    int piece = board[start];

    // Store some variables for later use
    int chessman = Piece.getChessman(piece);
    int color = Piece.getColor(piece);

    switch (chessman) {
      case PieceType.PAWN:
        pawnList[color].removePosition(start);
        pawnList[color].addPosition(end);
        if (update) {
          long[] tempZobristChessman = zobristChessman[PieceType.PAWN][color];
          this.pawnZobristCode ^= tempZobristChessman[start];
          this.pawnZobristCode ^= tempZobristChessman[end];
        }
        break;
      case PieceType.KNIGHT:
        knightList[color].removePosition(start);
        knightList[color].addPosition(end);
        break;
      case PieceType.BISHOP:
        bishopList[color].removePosition(start);
        bishopList[color].addPosition(end);
        break;
      case PieceType.ROOK:
        rookList[color].removePosition(start);
        rookList[color].addPosition(end);
        break;
      case PieceType.QUEEN:
        queenList[color].removePosition(start);
        queenList[color].addPosition(end);
        break;
      case PieceType.KING:
        kingList[color].removePosition(start);
        kingList[color].addPosition(end);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[start] = Piece.NOPIECE;
    board[end] = piece;
    if (update) {
      long[] tempZobristChessman = zobristChessman[chessman][color];
      this.zobristCode ^= tempZobristChessman[start];
      this.zobristCode ^= tempZobristChessman[end];
      positionValueOpening[color] -= PieceSquareTable.getValue(GamePhase.OPENING, chessman, color, start);
      positionValueEndgame[color] -= PieceSquareTable.getValue(GamePhase.ENDGAME, chessman, color, start);
      positionValueOpening[color] += PieceSquareTable.getValue(GamePhase.OPENING, chessman, color, end);
      positionValueEndgame[color] += PieceSquareTable.getValue(GamePhase.ENDGAME, chessman, color, end);
    }

    return piece;
  }

  /**
   * Returns the GenericBoard.
   *
   * @return the GenericBoard.
   */
  GenericBoard getBoard() {
    GenericBoard newBoard = new GenericBoard();

    // Set chessmen
    for (GenericColor color : GenericColor.values()) {
      int intColor = Color.valueOfColor(color);

      for (int index = 0; index < pawnList[intColor].size; index++) {
        int intPosition = pawnList[intColor].position[index];
        assert intPosition != Square.NOPOSITION;
        assert Piece.getChessman(board[intPosition]) == PieceType.PAWN;
        assert Piece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Square.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.PAWN), position);
      }

      for (int index = 0; index < knightList[intColor].size; index++) {
        int intPosition = knightList[intColor].position[index];
        assert intPosition != Square.NOPOSITION;
        assert Piece.getChessman(board[intPosition]) == PieceType.KNIGHT;
        assert Piece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Square.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KNIGHT), position);
      }

      for (int index = 0; index < bishopList[intColor].size; index++) {
        int intPosition = bishopList[intColor].position[index];
        assert intPosition != Square.NOPOSITION;
        assert Piece.getChessman(board[intPosition]) == PieceType.BISHOP;
        assert Piece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Square.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.BISHOP), position);
      }

      for (int index = 0; index < rookList[intColor].size; index++) {
        int intPosition = rookList[intColor].position[index];
        assert intPosition != Square.NOPOSITION;
        assert Piece.getChessman(board[intPosition]) == PieceType.ROOK;
        assert Piece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Square.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.ROOK), position);
      }

      for (int index = 0; index < queenList[intColor].size; index++) {
        int intPosition = queenList[intColor].position[index];
        assert intPosition != Square.NOPOSITION;
        assert Piece.getChessman(board[intPosition]) == PieceType.QUEEN;
        assert Piece.getColor(board[intPosition]) == intColor;

        GenericPosition position = Square.valueOfIntPosition(intPosition);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.QUEEN), position);
      }

      assert kingList[intColor].size == 1;
      int intPosition = kingList[intColor].position[0];
      assert intPosition != Square.NOPOSITION;
      assert Piece.getChessman(board[intPosition]) == PieceType.KING;
      assert Piece.getColor(board[intPosition]) == intColor;

      GenericPosition position = Square.valueOfIntPosition(intPosition);
      newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KING), position);
    }

    // Set active color
    newBoard.setActiveColor(Color.valueOfIntColor(this.activeColor));

    // Set castling
    if ((castling & Castling.WHITE_KINGSIDE) != 0) {
      newBoard.setCastling(GenericColor.WHITE, GenericCastling.KINGSIDE, GenericFile.Fh);
    }
    if ((castling & Castling.WHITE_QUEENSIDE) != 0) {
      newBoard.setCastling(GenericColor.WHITE, GenericCastling.QUEENSIDE, GenericFile.Fa);
    }
    if ((castling & Castling.BLACK_KINGSIDE) != 0) {
      newBoard.setCastling(GenericColor.BLACK, GenericCastling.KINGSIDE, GenericFile.Fh);
    }
    if ((castling & Castling.BLACK_QUEENSIDE) != 0) {
      newBoard.setCastling(GenericColor.BLACK, GenericCastling.QUEENSIDE, GenericFile.Fa);
    }

    // Set en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      newBoard.setEnPassant(Square.valueOfIntPosition(this.enPassantSquare));
    }

    // Set half move clock
    newBoard.setHalfMoveClock(this.halfMoveClock);

    // Set full move number
    newBoard.setFullMoveNumber(getFullMoveNumber());

    return newBoard;
  }

  /**
   * Returns the full move number.
   *
   * @return the full move number.
   */
  int getFullMoveNumber() {
    return this.halfMoveNumber / 2;
  }

  /**
   * Sets the full move number.
   *
   * @param fullMoveNumber the full move number.
   */
  private void setFullMoveNumber(int fullMoveNumber) {
    assert fullMoveNumber > 0;

    this.halfMoveNumber = fullMoveNumber * 2;
    if (this.activeColor == Color.valueOfColor(GenericColor.BLACK)) {
      this.halfMoveNumber++;
    }
  }

  /**
   * Returns the game phase.
   *
   * @return the game phase.
   */
  int getGamePhase() {
    if (materialValue[Color.WHITE] >= GAMEPHASE_OPENING_VALUE && materialValue[Color.BLACK] >= GAMEPHASE_OPENING_VALUE) {
      return GamePhase.OPENING;
    } else if (materialValue[Color.WHITE] <= GAMEPHASE_ENDGAME_VALUE || materialValue[Color.BLACK] <= GAMEPHASE_ENDGAME_VALUE
        || materialCount[Color.WHITE] <= GAMEPHASE_ENDGAME_COUNT || materialCount[Color.BLACK] <= GAMEPHASE_ENDGAME_COUNT) {
      return GamePhase.ENDGAME;
    } else {
      return GamePhase.MIDDLE;
    }
  }

  /**
   * Returns whether this board state is a repetition.
   *
   * @return true if this board state is a repetition, false otherwise.
   */
  boolean isRepetition() {
    int j = Math.max(0, statesSize - halfMoveClock);
    for (int i = statesSize - 2; i >= j; i -= 2) {
      if (zobristCode == states[i].zobristHistory) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether this move checks the opponent king.
   *
   * @param move the move.
   * @return true if this move checks the opponent king.
   */
  boolean isCheckingMove(int move) {
    assert move != Move.NOMOVE;

    int chessmanColor = Move.getChessmanColor(move);
    int endPosition = Move.getEnd(move);
    int enemyKingColor = Color.switchColor(chessmanColor);
    int enemyKingPosition = kingList[enemyKingColor].position[0];

    switch (Move.getType(move)) {
      case MoveType.NORMAL:
      case MoveType.PAWNDOUBLE:
        int chessman = Move.getChessman(move);

        // Direct attacks
        if (canAttack(chessman, chessmanColor, endPosition, enemyKingPosition)) {
          return true;
        }

        int startPosition = Move.getStart(move);

        if (isPinned(startPosition, enemyKingColor)) {
          // We are pinned. Test if we move on the line.
          int attackDeltaStart = Attack.deltas[enemyKingPosition - startPosition + 127];
          int attackDeltaEnd = Attack.deltas[enemyKingPosition - endPosition + 127];
          return attackDeltaStart != attackDeltaEnd;
        }
        // Indirect attacks
        break;
      case MoveType.PAWNPROMOTION:
      case MoveType.ENPASSANT:
        // We do a slow test for complex moves
        makeMove(move);
        boolean isCheck = isAttacked(enemyKingPosition, chessmanColor);
        undoMove(move);
        return isCheck;
      case MoveType.CASTLING:
        int rookEnd = Square.NOPOSITION;

        if (endPosition == Square.g1) {
          assert chessmanColor == Color.WHITE;
          rookEnd = Square.f1;
        } else if (endPosition == Square.g8) {
          assert chessmanColor == Color.BLACK;
          rookEnd = Square.f8;
        } else if (endPosition == Square.c1) {
          assert chessmanColor == Color.WHITE;
          rookEnd = Square.d1;
        } else if (endPosition == Square.c8) {
          assert chessmanColor == Color.BLACK;
          rookEnd = Square.d8;
        } else {
          assert false : endPosition;
        }

        return canAttack(PieceType.ROOK, chessmanColor, rookEnd, enemyKingPosition);
      default:
        assert false : Move.getType(move);
        break;
    }

    return false;
  }

  boolean isPinned(int chessmanPosition, int kingColor) {
    assert chessmanPosition != Square.NOPOSITION;
    assert kingColor != Color.NOCOLOR;

    int myKingPosition = kingList[kingColor].position[0];

    // We can only be pinned on an attack line
    int vector = Attack.vector[myKingPosition - chessmanPosition + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    int delta = Attack.deltas[myKingPosition - chessmanPosition + 127];

    // Walk towards the king
    int end = chessmanPosition + delta;
    assert (end & 0x88) == 0;
    while (board[end] == Piece.NOPIECE) {
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
      if (attacker != Piece.NOPIECE) {
        int attackerColor = Piece.getColor(attacker);
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
  boolean canSliderPseudoAttack(int attacker, int attackerPosition, int targetPosition) {
    assert attacker != Piece.NOPIECE;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector;

    switch (Piece.getChessman(attacker)) {
      case PieceType.PAWN:
        break;
      case PieceType.KNIGHT:
        break;
      case PieceType.BISHOP:
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
      case PieceType.ROOK:
        attackVector = Attack.vector[targetPosition - attackerPosition + 127];
        switch (attackVector) {
          case Attack.s:
          case Attack.S:
            return true;
          default:
            break;
        }
        break;
      case PieceType.QUEEN:
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
      case PieceType.KING:
        break;
      default:
        assert false : Piece.getChessman(attacker);
        break;
    }

    return false;
  }

  /**
   * Returns the attacks on the king.
   *
   * @param color the Color of the target king.
   */
  Attack getAttack(int color) {
    Attack attack = attackHistory[this.attackHistorySize][color];
    if (attack.count != Attack.NOATTACK) {
      return attack;
    }

    assert kingList[color].size == 1;

    int attackerColor = Color.switchColor(color);
    getAttack(attack, kingList[color].position[0], attackerColor, false);

    return attack;
  }

  /**
   * Returns whether or not the position is attacked.
   *
   * @param targetPosition the IntPosition.
   * @param attackerColor  the attacker color.
   * @return true if the position is attacked, false otherwise.
   */
  boolean isAttacked(int targetPosition, int attackerColor) {
    assert (targetPosition & 0x88) == 0;
    assert attackerColor != Color.NOCOLOR;

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
    assert targetPosition != Square.NOPOSITION;
    assert attackerColor != Color.NOCOLOR;

    attack.count = 0;

    // Pawn attacks
    int pawnPiece = Piece.WHITE_PAWN;
    int sign = -1;
    if (attackerColor == Color.BLACK) {
      pawnPiece = Piece.BLACK_PAWN;
      sign = 1;
    } else {
      assert attackerColor == Color.WHITE;
    }
    int pawnAttackerPosition = targetPosition + sign * 15;
    if ((pawnAttackerPosition & 0x88) == 0) {
      int pawn = board[pawnAttackerPosition];
      if (pawn != Piece.NOPIECE && pawn == pawnPiece) {
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
      if (pawn != Piece.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert Attack.deltas[targetPosition - pawnAttackerPosition + 127] == sign * -17;
        attack.position[attack.count] = pawnAttackerPosition;
        attack.delta[attack.count] = sign * -17;
        attack.count++;
      }
    }
    PositionList tempChessmanList = knightList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.position[index];
      assert Piece.getChessman(board[attackerPosition]) == PieceType.KNIGHT;
      assert attackerPosition != Square.NOPOSITION;
      assert board[attackerPosition] != Piece.NOPIECE;
      assert attackerColor == Piece.getColor(board[attackerPosition]);
      if (canAttack(PieceType.KNIGHT, attackerColor, attackerPosition, targetPosition)) {
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
    tempChessmanList = bishopList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.position[index];
      assert Piece.getChessman(board[attackerPosition]) == PieceType.BISHOP;
      assert attackerPosition != Square.NOPOSITION;
      assert board[attackerPosition] != Piece.NOPIECE;
      assert attackerColor == Piece.getColor(board[attackerPosition]);
      if (canAttack(PieceType.BISHOP, attackerColor, attackerPosition, targetPosition)) {
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
    tempChessmanList = rookList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.position[index];
      assert Piece.getChessman(board[attackerPosition]) == PieceType.ROOK;
      assert attackerPosition != Square.NOPOSITION;
      assert board[attackerPosition] != Piece.NOPIECE;
      assert attackerColor == Piece.getColor(board[attackerPosition]);
      if (canAttack(PieceType.ROOK, attackerColor, attackerPosition, targetPosition)) {
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
    tempChessmanList = queenList[attackerColor];
    for (int index = 0; index < tempChessmanList.size; index++) {
      int attackerPosition = tempChessmanList.position[index];
      assert Piece.getChessman(board[attackerPosition]) == PieceType.QUEEN;
      assert attackerPosition != Square.NOPOSITION;
      assert board[attackerPosition] != Piece.NOPIECE;
      assert attackerColor == Piece.getColor(board[attackerPosition]);
      if (canAttack(PieceType.QUEEN, attackerColor, attackerPosition, targetPosition)) {
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
    assert kingList[attackerColor].size == 1;
    int attackerPosition = kingList[attackerColor].position[0];
    assert Piece.getChessman(board[attackerPosition]) == PieceType.KING;
    assert attackerPosition != Square.NOPOSITION;
    assert board[attackerPosition] != Piece.NOPIECE;
    assert attackerColor == Piece.getColor(board[attackerPosition]);
    if (canAttack(PieceType.KING, attackerColor, attackerPosition, targetPosition)) {
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
  boolean canAttack(int attackerChessman, int attackerColor, int attackerPosition, int targetPosition) {
    assert attackerChessman != Piece.NOPIECE;
    assert attackerColor != Color.NOCOLOR;
    assert (attackerPosition & 0x88) == 0;
    assert (targetPosition & 0x88) == 0;

    int attackVector = Attack.vector[targetPosition - attackerPosition + 127];

    switch (attackerChessman) {
      case PieceType.PAWN:
        if (attackVector == Attack.u && attackerColor == Color.WHITE) {
          return true;
        } else if (attackVector == Attack.d && attackerColor == Color.BLACK) {
          return true;
        }
        break;
      case PieceType.KNIGHT:
        if (attackVector == Attack.K) {
          return true;
        }
        break;
      case PieceType.BISHOP:
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
      case PieceType.ROOK:
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
      case PieceType.QUEEN:
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
      case PieceType.KING:
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

    int end = attackerPosition + attackDelta;
    while ((end & 0x88) == 0 && end != targetPosition && board[end] == Piece.NOPIECE) {
      end += attackDelta;
    }

    return end == targetPosition;
  }

  /**
   * Makes the move.
   *
   * @param move the move.
   */
  void makeMove(int move) {
    // Get current stack entry
    State currentStackEntry = states[this.statesSize];

    // Save history
    currentStackEntry.zobristHistory = this.zobristCode;
    currentStackEntry.pawnZobristHistory = this.pawnZobristCode;
    currentStackEntry.halfMoveClockHistory = this.halfMoveClock;
    currentStackEntry.enPassantHistory = this.enPassantSquare;
    currentStackEntry.captureSquareHistory = this.captureSquare;
    for (int color : Color.values) {
      currentStackEntry.positionValueOpening[color] = positionValueOpening[color];
      currentStackEntry.positionValueEndgame[color] = positionValueEndgame[color];
    }

    // Update stack size
    this.statesSize++;
    assert this.statesSize < STACKSIZE;

    int type = Move.getType(move);

    switch (type) {
      case MoveType.NORMAL:
        makeMoveNormal(move);
        break;
      case MoveType.PAWNDOUBLE:
        makeMovePawnDouble(move);
        break;
      case MoveType.PAWNPROMOTION:
        makeMovePawnPromotion(move);
        break;
      case MoveType.ENPASSANT:
        makeMoveEnPassant(move);
        break;
      case MoveType.CASTLING:
        makeMoveCastling(move);
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Update half move number
    this.halfMoveNumber++;

    // Update active color
    this.activeColor = Color.switchColor(this.activeColor);
    this.zobristCode ^= zobristActiveColor;
    this.pawnZobristCode ^= zobristActiveColor;

    // Update attack list
    this.attackHistorySize++;
    attackHistory[this.attackHistorySize][Color.WHITE].count = Attack.NOATTACK;
    attackHistory[this.attackHistorySize][Color.BLACK].count = Attack.NOATTACK;
  }

  /**
   * Undo the move.
   *
   * @param move the IntMove.
   */
  void undoMove(int move) {
    int type = Move.getType(move);

    // Update attack list
    this.attackHistorySize--;

    // Update active color
    this.activeColor = Color.switchColor(this.activeColor);

    // Update half move number
    this.halfMoveNumber--;

    // Update stack size
    this.statesSize--;
    assert this.statesSize >= 0;

    // Get current stack entry
    State currentStackEntry = states[this.statesSize];

    // Restore zobrist history
    this.zobristCode = currentStackEntry.zobristHistory;
    this.pawnZobristCode = currentStackEntry.pawnZobristHistory;
    this.halfMoveClock = currentStackEntry.halfMoveClockHistory;
    this.enPassantSquare = currentStackEntry.enPassantHistory;
    this.captureSquare = currentStackEntry.captureSquareHistory;
    for (int color : Color.values) {
      positionValueOpening[color] = currentStackEntry.positionValueOpening[color];
      positionValueEndgame[color] = currentStackEntry.positionValueEndgame[color];
    }

    switch (type) {
      case MoveType.NORMAL:
        undoMoveNormal(move);
        break;
      case MoveType.PAWNDOUBLE:
        undoMovePawnDouble(move);
        break;
      case MoveType.PAWNPROMOTION:
        undoMovePawnPromotion(move);
        break;
      case MoveType.ENPASSANT:
        undoMoveEnPassant(move);
        break;
      case MoveType.CASTLING:
        undoMoveCastling(move);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  private void makeMoveNormal(int move) {
    // Save the castling rights
    castlingHistory[this.castlingHistorySize++] = castling;
    int newCastling = castling;

    // Save the captured chessman
    int endPosition = Move.getEnd(move);
    int target = Piece.NOPIECE;
    if (board[endPosition] != Piece.NOPIECE) {
      target = remove(endPosition, true);
      assert Move.getTarget(move) != Piece.NOPIECE : Move.toString(move);
      captureHistory[this.captureHistorySize++] = target;
      this.captureSquare = endPosition;

      switch (endPosition) {
        case Square.a1:
          newCastling &= ~Castling.WHITE_QUEENSIDE;
          break;
        case Square.a8:
          newCastling &= ~Castling.BLACK_QUEENSIDE;
          break;
        case Square.h1:
          newCastling &= ~Castling.WHITE_KINGSIDE;
          break;
        case Square.h8:
          newCastling &= ~Castling.BLACK_KINGSIDE;
          break;
        case Square.e1:
          newCastling &= ~(Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE);
          break;
        case Square.e8:
          newCastling &= ~(Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
          break;
        default:
          break;
      }
      if (newCastling != castling) {
        assert (newCastling ^ castling) == Castling.WHITE_KINGSIDE
            || (newCastling ^ castling) == Castling.WHITE_QUEENSIDE
            || (newCastling ^ castling) == Castling.BLACK_KINGSIDE
            || (newCastling ^ castling) == Castling.BLACK_QUEENSIDE
            || (newCastling ^ castling) == (Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE)
            || (newCastling ^ castling) == (Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
        this.zobristCode ^= zobristCastling[newCastling ^ castling];
        castling = newCastling;
      }
    } else {
      this.captureSquare = Square.NOPOSITION;
    }

    // Move the piece
    int startPosition = Move.getStart(move);
    int piece = move(startPosition, endPosition, true);
    int chessman = Piece.getChessman(piece);

    // Update castling
    switch (startPosition) {
      case Square.a1:
        newCastling &= ~Castling.WHITE_QUEENSIDE;
        break;
      case Square.a8:
        newCastling &= ~Castling.BLACK_QUEENSIDE;
        break;
      case Square.h1:
        newCastling &= ~Castling.WHITE_KINGSIDE;
        break;
      case Square.h8:
        newCastling &= ~Castling.BLACK_KINGSIDE;
        break;
      case Square.e1:
        newCastling &= ~(Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE);
        break;
      case Square.e8:
        newCastling &= ~(Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
        break;
      default:
        break;
    }
    if (newCastling != castling) {
      assert (newCastling ^ castling) == Castling.WHITE_KINGSIDE
          || (newCastling ^ castling) == Castling.WHITE_QUEENSIDE
          || (newCastling ^ castling) == Castling.BLACK_KINGSIDE
          || (newCastling ^ castling) == Castling.BLACK_QUEENSIDE
          || (newCastling ^ castling) == (Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE)
          || (newCastling ^ castling) == (Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
      this.zobristCode ^= zobristCastling[newCastling ^ castling];
      castling = newCastling;
    }

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
      this.enPassantSquare = Square.NOPOSITION;
    }

    // Update half move clock
    if (chessman == PieceType.PAWN || target != Piece.NOPIECE) {
      this.halfMoveClock = 0;
    } else {
      this.halfMoveClock++;
    }
  }

  private void undoMoveNormal(int move) {
    // Move the chessman
    int startPosition = Move.getStart(move);
    int endPosition = Move.getEnd(move);
    move(endPosition, startPosition, false);

    // Restore the captured chessman
    int target = Move.getTarget(move);
    if (target != Piece.NOPIECE) {
      put(captureHistory[--this.captureHistorySize], endPosition, false);
    }

    // Restore the castling rights
    castling = castlingHistory[--this.castlingHistorySize];
  }

  private void makeMovePawnPromotion(int move) {
    // Remove the pawn at the start position
    int startPosition = Move.getStart(move);
    int pawn = remove(startPosition, true);
    assert Piece.getChessman(pawn) == PieceType.PAWN;
    int pawnColor = Piece.getColor(pawn);
    assert Piece.getChessman(pawn) == Move.getChessman(move);
    assert pawnColor == Move.getChessmanColor(move);

    // Save the captured chessman
    int endPosition = Move.getEnd(move);
    int target;
    if (board[endPosition] != Piece.NOPIECE) {
      // Save the castling rights
      castlingHistory[this.castlingHistorySize++] = castling;
      int newCastling = castling;

      target = remove(endPosition, true);
      assert Move.getTarget(move) != Piece.NOPIECE;
      captureHistory[this.captureHistorySize++] = target;
      this.captureSquare = endPosition;

      switch (endPosition) {
        case Square.a1:
          newCastling &= ~Castling.WHITE_QUEENSIDE;
          break;
        case Square.a8:
          newCastling &= ~Castling.BLACK_QUEENSIDE;
          break;
        case Square.h1:
          newCastling &= ~Castling.WHITE_KINGSIDE;
          break;
        case Square.h8:
          newCastling &= ~Castling.BLACK_KINGSIDE;
          break;
        case Square.e1:
          newCastling &= ~(Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE);
          break;
        case Square.e8:
          newCastling &= ~(Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
          break;
        default:
          break;
      }
      if (newCastling != castling) {
        assert (newCastling ^ castling) == Castling.WHITE_KINGSIDE
            || (newCastling ^ castling) == Castling.WHITE_QUEENSIDE
            || (newCastling ^ castling) == Castling.BLACK_KINGSIDE
            || (newCastling ^ castling) == Castling.BLACK_QUEENSIDE
            || (newCastling ^ castling) == (Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE)
            || (newCastling ^ castling) == (Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
        this.zobristCode ^= zobristCastling[newCastling ^ castling];
        castling = newCastling;
      }
    } else {
      this.captureSquare = Square.NOPOSITION;
    }

    // Create the promotion chessman
    int promotion = Move.getPromotion(move);
    int promotionPiece = Piece.createPromotion(promotion, pawnColor);
    put(promotionPiece, endPosition, true);

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
      this.enPassantSquare = Square.NOPOSITION;
    }

    // Update half move clock
    this.halfMoveClock = 0;
  }

  private void undoMovePawnPromotion(int move) {
    // Remove the promotion chessman at the end position
    int endPosition = Move.getEnd(move);
    remove(endPosition, false);

    // Restore the captured chessman
    int target = Move.getTarget(move);
    if (target != Piece.NOPIECE) {
      put(captureHistory[--this.captureHistorySize], endPosition, false);

      // Restore the castling rights
      castling = castlingHistory[--this.castlingHistorySize];
    }

    // Put the pawn at the start position
    int pawnChessman = Move.getChessman(move);
    int pawnColor = Move.getChessmanColor(move);
    int pawnPiece = Piece.createPiece(pawnChessman, pawnColor);
    put(pawnPiece, Move.getStart(move), false);
  }

  private void makeMovePawnDouble(int move) {
    // Move the pawn
    int startPosition = Move.getStart(move);
    int endPosition = Move.getEnd(move);
    int pawn = move(startPosition, endPosition, true);
    int pawnColor = Piece.getColor(pawn);

    assert Piece.getChessman(pawn) == PieceType.PAWN;
    assert (startPosition >>> 4 == 1 && pawnColor == Color.WHITE) || (startPosition >>> 4 == 6 && pawnColor == Color.BLACK) : getBoard().toString() + ":" + Move.toString(move);
    assert (endPosition >>> 4 == 3 && pawnColor == Color.WHITE) || (endPosition >>> 4 == 4 && pawnColor == Color.BLACK);
    assert Math.abs(startPosition - endPosition) == 32;

    // Update the capture square
    this.captureSquare = Square.NOPOSITION;

    // Calculate the en passant position
    int targetPosition;
    if (pawnColor == Color.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      targetPosition = endPosition + 16;
    }

    assert (targetPosition & 0x88) == 0;
    assert Math.abs(startPosition - targetPosition) == 16;

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
    }

    this.enPassantSquare = targetPosition;
    this.zobristCode ^= zobristEnPassant[targetPosition];

    // Update half move clock
    this.halfMoveClock = 0;
  }

  private void undoMovePawnDouble(int move) {
    // Move the pawn
    move(Move.getEnd(move), Move.getStart(move), false);
  }

  private void makeMoveCastling(int move) {
    // Save the castling rights
    castlingHistory[this.castlingHistorySize++] = castling;
    int newCastling = castling;

    // Move the king
    int kingStartPosition = Move.getStart(move);
    int kingEndPosition = Move.getEnd(move);
    int king = move(kingStartPosition, kingEndPosition, true);
    assert Piece.getChessman(king) == PieceType.KING;

    // Get the rook positions
    int rookStartPosition;
    int rookEndPosition;
    switch (kingEndPosition) {
      case Square.g1:
        rookStartPosition = Square.h1;
        rookEndPosition = Square.f1;
        newCastling &= ~(Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE);
        break;
      case Square.c1:
        rookStartPosition = Square.a1;
        rookEndPosition = Square.d1;
        newCastling &= ~(Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE);
        break;
      case Square.g8:
        rookStartPosition = Square.h8;
        rookEndPosition = Square.f8;
        newCastling &= ~(Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
        break;
      case Square.c8:
        rookStartPosition = Square.a8;
        rookEndPosition = Square.d8;
        newCastling &= ~(Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    int rook = move(rookStartPosition, rookEndPosition, true);
    assert Piece.getChessman(rook) == PieceType.ROOK;

    // Update castling
    assert (newCastling ^ castling) == Castling.WHITE_KINGSIDE
        || (newCastling ^ castling) == Castling.WHITE_QUEENSIDE
        || (newCastling ^ castling) == Castling.BLACK_KINGSIDE
        || (newCastling ^ castling) == Castling.BLACK_QUEENSIDE
        || (newCastling ^ castling) == (Castling.WHITE_KINGSIDE | Castling.WHITE_QUEENSIDE)
        || (newCastling ^ castling) == (Castling.BLACK_KINGSIDE | Castling.BLACK_QUEENSIDE);
    this.zobristCode ^= zobristCastling[newCastling ^ castling];
    castling = newCastling;

    // Update the capture square
    this.captureSquare = Square.NOPOSITION;

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
      this.enPassantSquare = Square.NOPOSITION;
    }

    // Update half move clock
    this.halfMoveClock++;
  }

  private void undoMoveCastling(int move) {
    int kingEndPosition = Move.getEnd(move);

    // Get the rook positions
    int rookStartPosition;
    int rookEndPosition;
    switch (kingEndPosition) {
      case Square.g1:
        rookStartPosition = Square.h1;
        rookEndPosition = Square.f1;
        break;
      case Square.c1:
        rookStartPosition = Square.a1;
        rookEndPosition = Square.d1;
        break;
      case Square.g8:
        rookStartPosition = Square.h8;
        rookEndPosition = Square.f8;
        break;
      case Square.c8:
        rookStartPosition = Square.a8;
        rookEndPosition = Square.d8;
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    move(rookEndPosition, rookStartPosition, false);

    // Move the king
    move(kingEndPosition, Move.getStart(move), false);

    // Restore the castling rights
    castling = castlingHistory[--this.castlingHistorySize];
  }

  private void makeMoveEnPassant(int move) {
    // Move the pawn
    int startPosition = Move.getStart(move);
    int endPosition = Move.getEnd(move);
    int pawn = move(startPosition, endPosition, true);
    assert Piece.getChessman(pawn) == PieceType.PAWN;
    int pawnColor = Piece.getColor(pawn);

    // Calculate the en passant position
    int targetPosition;
    if (pawnColor == Color.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      assert pawnColor == Color.BLACK;

      targetPosition = endPosition + 16;
    }

    // Remove the captured pawn
    int target = remove(targetPosition, true);
    assert Move.getTarget(move) != Piece.NOPIECE;
    assert Piece.getChessman(target) == PieceType.PAWN;
    assert Piece.getColor(target) == Color.switchColor(pawnColor);
    captureHistory[this.captureHistorySize++] = target;

    // Update the capture square
    // This is the end position of the move, not the en passant position
    this.captureSquare = endPosition;

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
      this.enPassantSquare = Square.NOPOSITION;
    }

    // Update half move clock
    this.halfMoveClock = 0;
  }

  private void undoMoveEnPassant(int move) {
    // Move the pawn
    int endPosition = Move.getEnd(move);
    int pawn = move(endPosition, Move.getStart(move), false);

    // Calculate the en passant position
    int targetPosition;
    if (Piece.getColor(pawn) == Color.WHITE) {
      targetPosition = endPosition - 16;
    } else {
      assert Piece.getColor(pawn) == Color.BLACK;

      targetPosition = endPosition + 16;
    }

    // Restore the captured pawn
    put(captureHistory[--this.captureHistorySize], targetPosition, false);
  }

  void makeMoveNull() {
    // Get current stack entry
    State currentStackEntry = states[this.statesSize];

    // Save history
    currentStackEntry.zobristHistory = this.zobristCode;
    currentStackEntry.pawnZobristHistory = this.pawnZobristCode;
    currentStackEntry.halfMoveClockHistory = this.halfMoveClock;
    currentStackEntry.enPassantHistory = this.enPassantSquare;
    currentStackEntry.captureSquareHistory = this.captureSquare;
    for (int color : Color.values) {
      currentStackEntry.positionValueOpening[color] = positionValueOpening[color];
      currentStackEntry.positionValueEndgame[color] = positionValueEndgame[color];
    }

    // Update stack size
    this.statesSize++;
    assert this.statesSize < STACKSIZE;

    // Update the capture square
    this.captureSquare = Square.NOPOSITION;

    // Update en passant
    if (this.enPassantSquare != Square.NOPOSITION) {
      this.zobristCode ^= zobristEnPassant[this.enPassantSquare];
      this.enPassantSquare = Square.NOPOSITION;
    }

    // Update half move clock
    this.halfMoveClock++;

    // Update half move number
    this.halfMoveNumber++;

    // Update active color
    this.activeColor = Color.switchColor(this.activeColor);
    this.zobristCode ^= zobristActiveColor;
    this.pawnZobristCode ^= zobristActiveColor;

    // Update attack list
    this.attackHistorySize++;
    attackHistory[this.attackHistorySize][Color.WHITE].count = Attack.NOATTACK;
    attackHistory[this.attackHistorySize][Color.BLACK].count = Attack.NOATTACK;
  }

  void undoMoveNull() {
    // Update attack list
    this.attackHistorySize--;

    // Update active color
    this.activeColor = Color.switchColor(this.activeColor);

    // Update half move number
    this.halfMoveNumber--;

    // Update stack size
    this.statesSize--;
    assert this.statesSize >= 0;

    // Get current stack entry
    State currentStackEntry = states[this.statesSize];

    // Restore zobrist history
    this.zobristCode = currentStackEntry.zobristHistory;
    this.pawnZobristCode = currentStackEntry.pawnZobristHistory;
    this.halfMoveClock = currentStackEntry.halfMoveClockHistory;
    this.enPassantSquare = currentStackEntry.enPassantHistory;
    this.captureSquare = currentStackEntry.captureSquareHistory;
    for (int color : Color.values) {
      positionValueOpening[color] = currentStackEntry.positionValueOpening[color];
      positionValueEndgame[color] = currentStackEntry.positionValueEndgame[color];
    }
  }

  public String toString() {
    return getBoard().toString();
  }

}
