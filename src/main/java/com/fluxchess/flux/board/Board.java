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
  private static final long[][] zobristCastling = new long[IntColor.values.length][IntCastling.values.length];
  private static final long[] zobristEnPassant = new long[BOARDSIZE];

  //## BEGIN 0x88 Board Representation
  public final int[] board = new int[BOARDSIZE];
  //## ENDOF 0x88 Board Representation

  // The chessman lists.
  public final long[] pawnList = new long[IntColor.values.length];
  public final long[] knightList = new long[IntColor.values.length];
  public final long[] bishopList = new long[IntColor.values.length];
  public final long[] rookList = new long[IntColor.values.length];
  public final long[] queenList = new long[IntColor.values.length];
  public final long[] kingList = new long[IntColor.values.length];

  // Board stack
  private final State[] stack = new State[STACKSIZE];
  private int stackSize = 0;

  // Zobrist code
  public long zobristCode = 0;

  // Pawn zobrist code
  public long pawnZobristCode = 0;

  // En Passant square
  public int enPassantSquare = Square.NOSQUARE;

  // Castling
  public final int[][] castling = new int[IntColor.values.length][IntCastling.values.length];

  // Capture
  public int captureSquare = Square.NOSQUARE;
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

  private final class State {
    public long zobristCode = 0;
    public long pawnZobristCode = 0;
    public final int[][] castling = new int[IntColor.values.length][IntCastling.values.length];
    public int halfMoveClock = 0;
    public int enPassant = Square.NOSQUARE;
    public int captureSquare = Square.NOSQUARE;

    public State() {
      for (int color : IntColor.values) {
        for (int castling : IntCastling.values) {
          this.castling[color][castling] = IntFile.NOFILE;
        }
      }
    }
  }

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

    zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE] = Math.abs(random.nextLong());
    zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE] = Math.abs(random.nextLong());

    for (int i = 0; i < BOARDSIZE; i++) {
      zobristEnPassant[i] = Math.abs(random.nextLong());
    }
  }

  public Board(GenericBoard newBoard) {
    // Initialize repetition table
    repetitionTable = new RepetitionTable();

    for (int i = 0; i < stack.length; i++) {
      stack[i] = new State();
    }

    // Initialize the attack list
    for (int i = 0; i < attackHistory.length; i++) {
      for (int j = 0; j < IntColor.values.length; j++) {
        attackHistory[i][j] = new Attack();
      }
    }

    // Initialize the board
    for (int square : Square.values) {
      board[square] = IntPiece.NOPIECE;

      GenericPiece genericPiece = newBoard.getPiece(Square.toGenericPosition(square));
      if (genericPiece != null) {
        int piece = IntPiece.valueOf(genericPiece);
        put(piece, square, true);
      }
    }

    // Initialize en passant
    if (newBoard.getEnPassant() != null) {
      enPassantSquare = Square.valueOf(newBoard.getEnPassant());
      zobristCode ^= zobristEnPassant[Square.valueOf(newBoard.getEnPassant())];
    }

    // Initialize castling
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        GenericFile genericFile = newBoard.getCastling(IntColor.toGenericColor(color), IntCastling.toGenericCastling(castling));
        if (genericFile != null) {
          this.castling[color][castling] = IntFile.valueOf(genericFile);
          zobristCode ^= zobristCastling[color][castling];
        } else {
          this.castling[color][castling] = IntFile.NOFILE;
        }
      }
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

  public GenericBoard getBoard() {
    GenericBoard newBoard = new GenericBoard();

    // Set chessmen
    for (GenericColor color : GenericColor.values()) {
      int intColor = IntColor.valueOf(color);

      for (long squares = pawnList[intColor]; squares != 0; squares &= squares - 1) {
        int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
        assert square != Square.NOSQUARE;
        assert IntPiece.getChessman(board[square]) == IntChessman.PAWN;
        assert IntPiece.getColor(board[square]) == intColor;

        GenericPosition genericPosition = Square.toGenericPosition(square);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.PAWN), genericPosition);
      }

      for (long squares = knightList[intColor]; squares != 0; squares &= squares - 1) {
        int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
        assert square != Square.NOSQUARE;
        assert IntPiece.getChessman(board[square]) == IntChessman.KNIGHT;
        assert IntPiece.getColor(board[square]) == intColor;

        GenericPosition genericPosition = Square.toGenericPosition(square);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KNIGHT), genericPosition);
      }

      for (long squares = bishopList[intColor]; squares != 0; squares &= squares - 1) {
        int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
        assert square != Square.NOSQUARE;
        assert IntPiece.getChessman(board[square]) == IntChessman.BISHOP;
        assert IntPiece.getColor(board[square]) == intColor;

        GenericPosition genericPosition = Square.toGenericPosition(square);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.BISHOP), genericPosition);
      }

      for (long squares = rookList[intColor]; squares != 0; squares &= squares - 1) {
        int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
        assert square != Square.NOSQUARE;
        assert IntPiece.getChessman(board[square]) == IntChessman.ROOK;
        assert IntPiece.getColor(board[square]) == intColor;

        GenericPosition genericPosition = Square.toGenericPosition(square);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.ROOK), genericPosition);
      }

      for (long squares = queenList[intColor]; squares != 0; squares &= squares - 1) {
        int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
        assert square != Square.NOSQUARE;
        assert IntPiece.getChessman(board[square]) == IntChessman.QUEEN;
        assert IntPiece.getColor(board[square]) == intColor;

        GenericPosition genericPosition = Square.toGenericPosition(square);
        newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.QUEEN), genericPosition);
      }

      assert Long.bitCount(kingList[intColor]) == 1;
      int square = Square.toX88Square(Long.numberOfTrailingZeros(kingList[intColor]));
      assert square != Square.NOSQUARE;
      assert IntPiece.getChessman(board[square]) == IntChessman.KING;
      assert IntPiece.getColor(board[square]) == intColor;

      GenericPosition genericPosition = Square.toGenericPosition(square);
      newBoard.setPiece(GenericPiece.valueOf(color, GenericChessman.KING), genericPosition);
    }

    // Set active color
    newBoard.setActiveColor(IntColor.toGenericColor(activeColor));

    // Set castling
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        if (this.castling[color][castling] != IntFile.NOFILE) {
          newBoard.setCastling(IntColor.toGenericColor(color), IntCastling.toGenericCastling(castling), IntFile.toGenericFile(this.castling[color][castling]));
        }
      }
    }

    // Set en passant
    if (enPassantSquare != Square.NOSQUARE) {
      newBoard.setEnPassant(Square.toGenericPosition(enPassantSquare));
    }

    // Set half move clock
    newBoard.setHalfMoveClock(halfMoveClock);

    // Set full move number
    newBoard.setFullMoveNumber(getFullMoveNumber());

    return newBoard;
  }

  public String toString() {
    return getBoard().toString();
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
   * Puts the piece on the board at the given square.
   *
   * @param piece    the piece.
   * @param square the square.
   * @param update   true if we should update, false otherwise.
   */
  private void put(int piece, int square, boolean update) {
    assert piece != IntPiece.NOPIECE;
    assert (square & 0x88) == 0;
    assert board[square] == IntPiece.NOPIECE;

    // Store some variables for later use
    int chessman = IntPiece.getChessman(piece);
    int color = IntPiece.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color] |= 1L << Square.toBitSquare(square);
        if (update) {
          pawnZobristCode ^= zobristChessman[IntChessman.PAWN][color][square];
        }
        break;
      case IntChessman.KNIGHT:
        knightList[color] |= 1L << Square.toBitSquare(square);
        break;
      case IntChessman.BISHOP:
        bishopList[color] |= 1L << Square.toBitSquare(square);
        break;
      case IntChessman.ROOK:
        rookList[color] |= 1L << Square.toBitSquare(square);
        break;
      case IntChessman.QUEEN:
        queenList[color] |= 1L << Square.toBitSquare(square);
        break;
      case IntChessman.KING:
        kingList[color] |= 1L << Square.toBitSquare(square);
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[square] = piece;
    if (update) {
      zobristCode ^= zobristChessman[chessman][color][square];
    }
  }

  /**
   * Removes the piece from the board at the given square.
   *
   * @param square the square.
   * @param update   true if we should update, false otherwise.
   * @return the removed piece.
   */
  private int remove(int square, boolean update) {
    assert (square & 0x88) == 0;
    assert board[square] != IntPiece.NOPIECE;

    // Get the piece
    int piece = board[square];

    // Store some variables for later use
    int chessman = IntPiece.getChessman(piece);
    int color = IntPiece.getColor(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        pawnList[color] &= ~(1L << Square.toBitSquare(square));
        if (update) {
          pawnZobristCode ^= zobristChessman[IntChessman.PAWN][color][square];
        }
        break;
      case IntChessman.KNIGHT:
        knightList[color] &= ~(1L << Square.toBitSquare(square));
        break;
      case IntChessman.BISHOP:
        bishopList[color] &= ~(1L << Square.toBitSquare(square));
        break;
      case IntChessman.ROOK:
        rookList[color] &= ~(1L << Square.toBitSquare(square));
        break;
      case IntChessman.QUEEN:
        queenList[color] &= ~(1L << Square.toBitSquare(square));
        break;
      case IntChessman.KING:
        kingList[color] &= ~(1L << Square.toBitSquare(square));
        break;
      default:
        assert false : chessman;
        break;
    }

    // Update
    board[square] = IntPiece.NOPIECE;
    if (update) {
      zobristCode ^= zobristChessman[chessman][color][square];
    }

    return piece;
  }

  public void makeMove(int move) {
    // Get current stack entry
    State currentState = stack[stackSize];

    // Save history
    currentState.zobristCode = zobristCode;
    currentState.pawnZobristCode = pawnZobristCode;
    currentState.halfMoveClock = halfMoveClock;
    currentState.enPassant = enPassantSquare;
    currentState.captureSquare = captureSquare;

    int type = Move.getType(move);

    switch (type) {
      case Move.Type.NORMAL:
        repetitionTable.put(zobristCode);
        makeMoveNormal(move, currentState);
        break;
      case Move.Type.PAWNDOUBLE:
        repetitionTable.put(zobristCode);
        makeMovePawnDouble(move);
        break;
      case Move.Type.PAWNPROMOTION:
        repetitionTable.put(zobristCode);
        makeMovePawnPromotion(move, currentState);
        break;
      case Move.Type.ENPASSANT:
        repetitionTable.put(zobristCode);
        makeMoveEnPassant(move);
        break;
      case Move.Type.CASTLING:
        repetitionTable.put(zobristCode);
        makeMoveCastling(move, currentState);
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

    // Update stack size
    stackSize++;
    assert stackSize < STACKSIZE;
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
    State currentState = stack[stackSize];

    // Restore zobrist history
    zobristCode = currentState.zobristCode;
    pawnZobristCode = currentState.pawnZobristCode;
    halfMoveClock = currentState.halfMoveClock;
    enPassantSquare = currentState.enPassant;
    captureSquare = currentState.captureSquare;

    switch (type) {
      case Move.Type.NORMAL:
        undoMoveNormal(move, currentState);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.PAWNDOUBLE:
        undoMovePawnDouble(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.PAWNPROMOTION:
        undoMovePawnPromotion(move, currentState);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.ENPASSANT:
        undoMoveEnPassant(move);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.CASTLING:
        undoMoveCastling(move, currentState);
        repetitionTable.remove(zobristCode);
        break;
      case Move.Type.NULL:
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  private void makeMoveNormal(int move, State entry) {
    // Save castling rights
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        entry.castling[color][castling] = this.castling[color][castling];
      }
    }

    // Save the captured chessman
    int targetSquare = Move.getTargetSquare(move);
    int target = IntPiece.NOPIECE;
    if (board[targetSquare] != IntPiece.NOPIECE) {
      target = remove(targetSquare, true);
      assert Move.getTargetPiece(move) != IntPiece.NOPIECE : Move.toString(move);
      captureHistory[captureHistorySize++] = target;
      captureSquare = targetSquare;

      switch (targetSquare) {
        case Square.a1:
          if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
            assert target == IntPiece.WHITEROOK;
            castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
          }
          break;
        case Square.a8:
          if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
            assert target == IntPiece.BLACKROOK;
            castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
          }
          break;
        case Square.h1:
          if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
            assert target == IntPiece.WHITEROOK;
            castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
          }
          break;
        case Square.h8:
          if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
            assert target == IntPiece.BLACKROOK;
            castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
          }
          break;
        default:
          break;
      }
    } else {
      captureSquare = Square.NOSQUARE;
    }

    // Move the piece
    int originSquare = Move.getOriginSquare(move);

    int originPiece = remove(originSquare, true);
    put(originPiece, targetSquare, true);

    // Update castling
    switch (originSquare) {
      case Square.a1:
        if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.WHITEROOK;
          castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
        }
        break;
      case Square.a8:
        if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.BLACKROOK;
          castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
        }
        break;
      case Square.h1:
        if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.WHITEROOK;
          castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
        }
        break;
      case Square.h8:
        if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.BLACKROOK;
          castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
        }
        break;
      case Square.e1:
        if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.WHITEKING;
          castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.WHITEKING;
          castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
        }
        break;
      case Square.e8:
        if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.BLACKKING;
          castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          assert Move.getOriginPiece(move) == IntPiece.BLACKKING;
          castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
        }
        break;
      default:
        break;
    }

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
      enPassantSquare = Square.NOSQUARE;
    }

    // Update half move clock
    if (IntPiece.getChessman(originPiece) == IntChessman.PAWN || target != IntPiece.NOPIECE) {
      halfMoveClock = 0;
    } else {
      halfMoveClock++;
    }
  }

  private void undoMoveNormal(int move, State entry) {
    // Move the chessman
    int originSquare = Move.getOriginSquare(move);
    int targetSquare = Move.getTargetSquare(move);
    int piece = remove(targetSquare, false);
    put(piece, originSquare, false);

    // Restore the captured chessman
    if (Move.getTargetPiece(move) != IntPiece.NOPIECE) {
      put(captureHistory[--captureHistorySize], targetSquare, false);
    }

    // Restore castling rights
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        if (entry.castling[color][castling] != this.castling[color][castling]) {
          this.castling[color][castling] = entry.castling[color][castling];
        }
      }
    }
  }

  private void makeMovePawnPromotion(int move, State entry) {
    // Remove the pawn at the origin square
    int originSquare = Move.getOriginSquare(move);
    int pawnPiece = remove(originSquare, true);
    assert IntPiece.getChessman(pawnPiece) == IntChessman.PAWN;
    int pawnColor = IntPiece.getColor(pawnPiece);
    assert IntPiece.getChessman(pawnPiece) == IntPiece.getChessman(Move.getOriginPiece(move));
    assert pawnColor == IntPiece.getColor(Move.getOriginPiece(move));

    // Save the captured chessman
    int targetSquare = Move.getTargetSquare(move);
    int targetPiece;
    if (board[targetSquare] != IntPiece.NOPIECE) {
      // Save castling rights
      for (int color : IntColor.values) {
        for (int castling : IntCastling.values) {
          entry.castling[color][castling] = this.castling[color][castling];
        }
      }

      targetPiece = remove(targetSquare, true);
      assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
      captureHistory[captureHistorySize++] = targetPiece;
      captureSquare = targetSquare;

      switch (targetSquare) {
        case Square.a1:
          if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
            assert targetPiece == IntPiece.WHITEROOK;
            castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
          }
          break;
        case Square.a8:
          if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
            assert targetPiece == IntPiece.BLACKROOK;
            castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
          }
          break;
        case Square.h1:
          if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
            assert targetPiece == IntPiece.WHITEROOK;
            castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
          }
          break;
        case Square.h8:
          if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
            assert targetPiece == IntPiece.BLACKROOK;
            castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
            zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
          }
          break;
        default:
          break;
      }
    } else {
      captureSquare = Square.NOSQUARE;
    }

    // Create the promotion chessman
    int promotion = Move.getPromotion(move);
    int promotionPiece = IntPiece.valueOf(promotion, pawnColor);
    put(promotionPiece, targetSquare, true);

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
      enPassantSquare = Square.NOSQUARE;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnPromotion(int move, State entry) {
    // Remove the promotion chessman at the end square
    int targetSquare = Move.getTargetSquare(move);
    remove(targetSquare, false);

    // Restore the captured chessman
    if (Move.getTargetPiece(move) != IntPiece.NOPIECE) {
      put(captureHistory[--captureHistorySize], targetSquare, false);

      // Restore castling rights
      for (int color : IntColor.values) {
        for (int castling : IntCastling.values) {
          if (entry.castling[color][castling] != this.castling[color][castling]) {
            this.castling[color][castling] = entry.castling[color][castling];
          }
        }
      }
    }

    // Put the pawn at the origin square
    put(Move.getOriginPiece(move), Move.getOriginSquare(move), false);
  }

  private void makeMovePawnDouble(int move) {
    // Move the pawn
    int originSquare = Move.getOriginSquare(move);
    int targetSquare = Move.getTargetSquare(move);
    int pawnPiece = remove(originSquare, true);
    put(pawnPiece, targetSquare, true);
    int pawnColor = IntPiece.getColor(pawnPiece);

    assert IntPiece.getChessman(pawnPiece) == IntChessman.PAWN;
    assert (originSquare >>> 4 == 1 && pawnColor == IntColor.WHITE) || (originSquare >>> 4 == 6 && pawnColor == IntColor.BLACK) : getBoard().toString() + ":" + Move.toString(move);
    assert (targetSquare >>> 4 == 3 && pawnColor == IntColor.WHITE) || (targetSquare >>> 4 == 4 && pawnColor == IntColor.BLACK);
    assert Math.abs(originSquare - targetSquare) == 32;

    // Update the capture square
    captureSquare = Square.NOSQUARE;

    // Calculate the en passant square
    int captureSquare;
    if (pawnColor == IntColor.WHITE) {
      captureSquare = targetSquare - 16;
    } else {
      captureSquare = targetSquare + 16;
    }

    assert (captureSquare & 0x88) == 0;
    assert Math.abs(originSquare - captureSquare) == 16;

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
    }

    enPassantSquare = captureSquare;
    zobristCode ^= zobristEnPassant[captureSquare];

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMovePawnDouble(int move) {
    // Move the pawn
    int piece = remove(Move.getTargetSquare(move), false);
    put(piece, Move.getOriginSquare(move), false);
  }

  private void makeMoveCastling(int move, State entry) {
    // Save castling rights
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        entry.castling[color][castling] = this.castling[color][castling];
      }
    }

    // Move the king
    int kingOriginSquare = Move.getOriginSquare(move);
    int kingTargetSquare = Move.getTargetSquare(move);
    int king = remove(kingOriginSquare, true);
    put(king, kingTargetSquare, true);
    assert IntPiece.getChessman(king) == IntChessman.KING;

    // Get the rook squares
    int rookOriginSquare;
    int rookTargetSquare;
    switch (kingTargetSquare) {
      case Square.g1:
        rookOriginSquare = Square.h1;
        rookTargetSquare = Square.f1;
        if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
        }
        break;
      case Square.c1:
        rookOriginSquare = Square.a1;
        rookTargetSquare = Square.d1;
        if (castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          castling[IntColor.WHITE][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          castling[IntColor.WHITE][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.WHITE][IntCastling.KINGSIDE];
        }
        break;
      case Square.g8:
        rookOriginSquare = Square.h8;
        rookTargetSquare = Square.f8;
        if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
        }
        break;
      case Square.c8:
        rookOriginSquare = Square.a8;
        rookTargetSquare = Square.d8;
        if (castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE) {
          castling[IntColor.BLACK][IntCastling.QUEENSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.QUEENSIDE];
        }
        if (castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE) {
          castling[IntColor.BLACK][IntCastling.KINGSIDE] = IntFile.NOFILE;
          zobristCode ^= zobristCastling[IntColor.BLACK][IntCastling.KINGSIDE];
        }
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    int rook = remove(rookOriginSquare, true);
    put(rook, rookTargetSquare, true);
    assert IntPiece.getChessman(rook) == IntChessman.ROOK;

    // Update the capture square
    captureSquare = Square.NOSQUARE;

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
      enPassantSquare = Square.NOSQUARE;
    }

    // Update half move clock
    halfMoveClock++;
  }

  private void undoMoveCastling(int move, State entry) {
    int kingTargetSquare = Move.getTargetSquare(move);

    // Get the rook squares
    int rookOriginSquare;
    int rookTargetSquare;
    switch (kingTargetSquare) {
      case Square.g1:
        rookOriginSquare = Square.h1;
        rookTargetSquare = Square.f1;
        break;
      case Square.c1:
        rookOriginSquare = Square.a1;
        rookTargetSquare = Square.d1;
        break;
      case Square.g8:
        rookOriginSquare = Square.h8;
        rookTargetSquare = Square.f8;
        break;
      case Square.c8:
        rookOriginSquare = Square.a8;
        rookTargetSquare = Square.d8;
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Move the rook
    int rook = remove(rookTargetSquare, false);
    put(rook, rookOriginSquare, false);

    // Move the king
    int king = remove(kingTargetSquare, false);
    put(king, Move.getOriginSquare(move), false);

    // Restore the castling rights
    for (int color : IntColor.values) {
      for (int castling : IntCastling.values) {
        if (entry.castling[color][castling] != this.castling[color][castling]) {
          this.castling[color][castling] = entry.castling[color][castling];
        }
      }
    }
  }

  private void makeMoveEnPassant(int move) {
    // Move the pawn
    int originSquare = Move.getOriginSquare(move);
    int targetSquare = Move.getTargetSquare(move);
    int pawn = remove(originSquare, true);
    put(pawn, targetSquare, true);
    assert IntPiece.getChessman(pawn) == IntChessman.PAWN;
    int pawnColor = IntPiece.getColor(pawn);

    // Calculate the en passant square
    int captureSquare;
    if (pawnColor == IntColor.WHITE) {
      captureSquare = targetSquare - 16;
    } else {
      assert pawnColor == IntColor.BLACK;

      captureSquare = targetSquare + 16;
    }

    // Remove the captured pawn
    int target = remove(captureSquare, true);
    assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
    assert IntPiece.getChessman(target) == IntChessman.PAWN;
    assert IntPiece.getColor(target) == IntColor.opposite(pawnColor);
    captureHistory[captureHistorySize++] = target;

    // Update the capture square
    // This is the target square of the move, not the en passant square
    this.captureSquare = targetSquare;

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
      enPassantSquare = Square.NOSQUARE;
    }

    // Update half move clock
    halfMoveClock = 0;
  }

  private void undoMoveEnPassant(int move) {
    // Move the pawn
    int targetSquare = Move.getTargetSquare(move);
    int pawnPiece = remove(targetSquare, false);
    put(pawnPiece, Move.getOriginSquare(move), false);

    // Calculate the en passant square
    int captureSquare;
    if (IntPiece.getColor(pawnPiece) == IntColor.WHITE) {
      captureSquare = targetSquare - 16;
    } else {
      assert IntPiece.getColor(pawnPiece) == IntColor.BLACK;

      captureSquare = targetSquare + 16;
    }

    // Restore the captured pawn
    put(captureHistory[--captureHistorySize], captureSquare, false);
  }

  private void makeMoveNull() {
    // Update the capture square
    captureSquare = Square.NOSQUARE;

    // Update en passant
    if (enPassantSquare != Square.NOSQUARE) {
      zobristCode ^= zobristEnPassant[enPassantSquare];
      enPassantSquare = Square.NOSQUARE;
    }

    // Update half move clock
    halfMoveClock++;
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
    int targetSquare = Move.getTargetSquare(move);
    int enemyKingColor = IntColor.opposite(chessmanColor);
    int enemyKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(kingList[enemyKingColor]));

    switch (Move.getType(move)) {
      case Move.Type.NORMAL:
      case Move.Type.PAWNDOUBLE:
        int chessman = IntPiece.getChessman(Move.getOriginPiece(move));

        // Direct attacks
        if (canAttack(chessman, chessmanColor, targetSquare, enemyKingSquare)) {
          return true;
        }

        int originSquare = Move.getOriginSquare(move);

        if (isPinned(originSquare, enemyKingColor)) {
          // We are pinned. Test if we move on the line.
          int attackDeltaOrigin = Attack.deltas[enemyKingSquare - originSquare + 127];
          int attackDeltaTarget = Attack.deltas[enemyKingSquare - targetSquare + 127];
          return attackDeltaOrigin != attackDeltaTarget;
        }
        // Indirect attacks
        break;
      case Move.Type.PAWNPROMOTION:
      case Move.Type.ENPASSANT:
        // We do a slow test for complex moves
        makeMove(move);
        boolean isCheck = isAttacked(enemyKingSquare, chessmanColor);
        undoMove(move);
        return isCheck;
      case Move.Type.CASTLING:
        int rookTargetSquare = Square.NOSQUARE;

        if (targetSquare == Square.g1) {
          assert chessmanColor == IntColor.WHITE;
          rookTargetSquare = Square.f1;
        } else if (targetSquare == Square.g8) {
          assert chessmanColor == IntColor.BLACK;
          rookTargetSquare = Square.f8;
        } else if (targetSquare == Square.c1) {
          assert chessmanColor == IntColor.WHITE;
          rookTargetSquare = Square.d1;
        } else if (targetSquare == Square.c8) {
          assert chessmanColor == IntColor.BLACK;
          rookTargetSquare = Square.d8;
        } else {
          assert false : targetSquare;
        }

        return canAttack(IntChessman.ROOK, chessmanColor, rookTargetSquare, enemyKingSquare);
      case Move.Type.NULL:
        assert false;
        break;
      default:
        assert false : Move.getType(move);
        break;
    }

    return false;
  }

  public boolean isPinned(int chessmanSquare, int kingColor) {
    assert chessmanSquare != Square.NOSQUARE;
    assert kingColor != IntColor.NOCOLOR;

    int myKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(kingList[kingColor]));

    // We can only be pinned on an attack line
    int vector = Attack.vector[myKingSquare - chessmanSquare + 127];
    if (vector == Attack.N || vector == Attack.K) {
      // No line
      return false;
    }

    int delta = Attack.deltas[myKingSquare - chessmanSquare + 127];

    // Walk towards the king
    int square = chessmanSquare + delta;
    assert (square & 0x88) == 0;
    while (board[square] == IntPiece.NOPIECE) {
      square += delta;
      assert (square & 0x88) == 0;
    }
    if (square != myKingSquare) {
      // There's a blocker between me and the king
      return false;
    }

    // Walk away from the king
    square = chessmanSquare - delta;
    while ((square & 0x88) == 0) {
      int attacker = board[square];
      if (attacker != IntPiece.NOPIECE) {
        int attackerColor = IntPiece.getColor(attacker);

        return kingColor != attackerColor && canSliderPseudoAttack(attacker, square, myKingSquare);
      } else {
        square -= delta;
      }
    }

    return false;
  }

  /**
   * Returns whether or not the attacker can attack the target square. The
   * method does not check if a slider can reach the square.
   *
   * @param attacker       the attacker.
   * @param targetSquare the target square.
   * @return if the attacker can attack the target square.
   */
  public boolean canSliderPseudoAttack(int attacker, int attackerSquare, int targetSquare) {
    assert attacker != IntPiece.NOPIECE;
    assert (attackerSquare & 0x88) == 0;
    assert (targetSquare & 0x88) == 0;

    int attackVector;

    switch (IntPiece.getChessman(attacker)) {
      case IntChessman.PAWN:
        break;
      case IntChessman.KNIGHT:
        break;
      case IntChessman.BISHOP:
        attackVector = Attack.vector[targetSquare - attackerSquare + 127];
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
        attackVector = Attack.vector[targetSquare - attackerSquare + 127];
        switch (attackVector) {
          case Attack.s:
          case Attack.S:
            return true;
          default:
            break;
        }
        break;
      case IntChessman.QUEEN:
        attackVector = Attack.vector[targetSquare - attackerSquare + 127];
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

    assert Long.bitCount(kingList[color]) == 1;

    int attackerColor = IntColor.opposite(color);
    getAttack(attack, Square.toX88Square(Long.numberOfTrailingZeros(kingList[color])), attackerColor, false);

    return attack;
  }

  /**
   * Returns whether or not the square is attacked.
   *
   * @param targetSquare the Square.
   * @param attackerColor  the attacker color.
   * @return true if the square is attacked, false otherwise.
   */
  public boolean isAttacked(int targetSquare, int attackerColor) {
    assert (targetSquare & 0x88) == 0;
    assert attackerColor != IntColor.NOCOLOR;

    return getAttack(tempAttack, targetSquare, attackerColor, true);
  }

  /**
   * Returns all attacks to the target square.
   *
   * @param attack         the attack to fill the information.
   * @param targetSquare the target square.
   * @param attackerColor  the attacker color.
   * @param stop           whether we should only check.
   * @return true if the square can be attacked, false otherwise.
   */
  private boolean getAttack(Attack attack, int targetSquare, int attackerColor, boolean stop) {
    assert attack != null;
    assert targetSquare != Square.NOSQUARE;
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
    int pawnAttackerSquare = targetSquare + sign * 15;
    if ((pawnAttackerSquare & 0x88) == 0) {
      int pawn = board[pawnAttackerSquare];
      if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert Attack.deltas[targetSquare - pawnAttackerSquare + 127] == sign * -15;
        attack.square[attack.count] = pawnAttackerSquare;
        attack.delta[attack.count] = sign * -15;
        attack.count++;
      }
    }
    pawnAttackerSquare = targetSquare + sign * 17;
    if ((pawnAttackerSquare & 0x88) == 0) {
      int pawn = board[pawnAttackerSquare];
      if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
        if (stop) {
          return true;
        }
        assert Attack.deltas[targetSquare - pawnAttackerSquare + 127] == sign * -17;
        attack.square[attack.count] = pawnAttackerSquare;
        attack.delta[attack.count] = sign * -17;
        attack.count++;
      }
    }
    for (long squares = knightList[attackerColor]; squares != 0; squares &= squares - 1) {
      int attackerSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      assert IntPiece.getChessman(board[attackerSquare]) == IntChessman.KNIGHT;
      assert attackerSquare != Square.NOSQUARE;
      assert board[attackerSquare] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerSquare]);
      if (canAttack(IntChessman.KNIGHT, attackerColor, attackerSquare, targetSquare)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];
        assert attackDelta != 0;
        attack.square[attack.count] = attackerSquare;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long squares = bishopList[attackerColor]; squares != 0; squares &= squares - 1) {
      int attackerSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      assert IntPiece.getChessman(board[attackerSquare]) == IntChessman.BISHOP;
      assert attackerSquare != Square.NOSQUARE;
      assert board[attackerSquare] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerSquare]);
      if (canAttack(IntChessman.BISHOP, attackerColor, attackerSquare, targetSquare)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];
        assert attackDelta != 0;
        attack.square[attack.count] = attackerSquare;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long squares = rookList[attackerColor]; squares != 0; squares &= squares - 1) {
      int attackerSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      assert IntPiece.getChessman(board[attackerSquare]) == IntChessman.ROOK;
      assert attackerSquare != Square.NOSQUARE;
      assert board[attackerSquare] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerSquare]);
      if (canAttack(IntChessman.ROOK, attackerColor, attackerSquare, targetSquare)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];
        assert attackDelta != 0;
        attack.square[attack.count] = attackerSquare;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    for (long squares = queenList[attackerColor]; squares != 0; squares &= squares - 1) {
      int attackerSquare = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      assert IntPiece.getChessman(board[attackerSquare]) == IntChessman.QUEEN;
      assert attackerSquare != Square.NOSQUARE;
      assert board[attackerSquare] != IntPiece.NOPIECE;
      assert attackerColor == IntPiece.getColor(board[attackerSquare]);
      if (canAttack(IntChessman.QUEEN, attackerColor, attackerSquare, targetSquare)) {
        if (stop) {
          return true;
        }
        int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];
        assert attackDelta != 0;
        attack.square[attack.count] = attackerSquare;
        attack.delta[attack.count] = attackDelta;
        attack.count++;
      }
    }
    assert Long.bitCount(kingList[attackerColor]) == 1;
    int attackerSquare = Square.toX88Square(Long.numberOfTrailingZeros(kingList[attackerColor]));
    assert IntPiece.getChessman(board[attackerSquare]) == IntChessman.KING;
    assert attackerSquare != Square.NOSQUARE;
    assert board[attackerSquare] != IntPiece.NOPIECE;
    assert attackerColor == IntPiece.getColor(board[attackerSquare]);
    if (canAttack(IntChessman.KING, attackerColor, attackerSquare, targetSquare)) {
      if (stop) {
        return true;
      }
      int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];
      assert attackDelta != 0;
      attack.square[attack.count] = attackerSquare;
      attack.delta[attack.count] = attackDelta;
      attack.count++;
    }

    return false;
  }

  /**
   * Returns whether or not the attacker can attack the target square.
   *
   * @param attackerChessman the attacker chessman.
   * @param attackerColor    the attacker color.
   * @param attackerSquare the attacker square.
   * @param targetSquare   the target square.
   * @return if the attacker can attack the target square.
   */
  public boolean canAttack(int attackerChessman, int attackerColor, int attackerSquare, int targetSquare) {
    assert attackerChessman != IntChessman.NOCHESSMAN;
    assert attackerColor != IntColor.NOCOLOR;
    assert (attackerSquare & 0x88) == 0;
    assert (targetSquare & 0x88) == 0;

    int attackVector = Attack.vector[targetSquare - attackerSquare + 127];

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
            if (canSliderAttack(attackerSquare, targetSquare)) {
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
            if (canSliderAttack(attackerSquare, targetSquare)) {
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
            if (canSliderAttack(attackerSquare, targetSquare)) {
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
   * Returns whether or not the slider can attack the target square.
   *
   * @param attackerSquare the attacker square..
   * @param targetSquare   the target square.
   * @return true if the slider can attack the target square.
   */
  private boolean canSliderAttack(int attackerSquare, int targetSquare) {
    assert (attackerSquare & 0x88) == 0;
    assert (targetSquare & 0x88) == 0;

    int attackDelta = Attack.deltas[targetSquare - attackerSquare + 127];

    int square = attackerSquare + attackDelta;
    while ((square & 0x88) == 0 && square != targetSquare && board[square] == IntPiece.NOPIECE) {
      square += attackDelta;
    }

    return square == targetSquare;
  }

}
