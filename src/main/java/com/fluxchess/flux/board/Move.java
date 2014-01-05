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

import com.fluxchess.jcpi.models.*;

/**
 * This class represents a move as a int value. The fields are represented by
 * the following bits.
 * <p/>
 *  0 -  2: the type (required)
 *  3 -  9: the start position (required except Move.NULL)
 * 10 - 16: the end position (required except Move.NULL)
 * 17 - 21: the chessman piece (required)
 * 22 - 26: the target piece (optional)
 * 27 - 29: the promotion chessman (optional)
 */
public final class Move {

  public static final class Type {
    public static final int MASK = 0x7;

    public static final int NORMAL = 0;
    public static final int PAWNDOUBLE = 1;
    public static final int PAWNPROMOTION = 2;
    public static final int ENPASSANT = 3;
    public static final int CASTLING = 4;
    public static final int NULL = 5;
    public static final int NOTYPE = 6;

    public static final int[] values = {
      NORMAL,
      PAWNDOUBLE,
      PAWNPROMOTION,
      ENPASSANT,
      CASTLING,
      NULL
    };

    private Type() {
    }
  }

  public static final int NOMOVE = -8;

  public static final int NULLMOVE;

  public static final int INTMOVE_MASK = 0xFFFFFFF;
  public static final int INTMOVE_SIZE = 28;

  // Position value
  // We do not use 127 because there all bits are set
  private static final int INTERNAL_NOPOSITION = 126;

  // Bit operation values
  private static final int MOVE_SHIFT = 0;
  private static final int MOVE_MASK = Type.MASK << MOVE_SHIFT;
  private static final int START_SHIFT = 3;
  private static final int START_MASK = Position.MASK << START_SHIFT;
  private static final int END_SHIFT = 10;
  private static final int END_MASK = Position.MASK << END_SHIFT;
  private static final int CHESSMAN_PIECE_SHIFT = 17;
  private static final int CHESSMAN_PIECE_MASK = IntPiece.MASK << CHESSMAN_PIECE_SHIFT;
  private static final int TARGET_PIECE_SHIFT = 22;
  private static final int TARGET_PIECE_MASK = IntPiece.MASK << TARGET_PIECE_SHIFT;
  private static final int PROMOTION_SHIFT = 27;
  private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;

  static {
    NULLMOVE = Move.createMove(Move.Type.NULL, Position.NOPOSITION, Position.NOPOSITION, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
  }

  private Move() {
  }

  /**
   * Get the Move.
   *
   * @param type      the type.
   * @param start     the start position.
   * @param end       the end position.
   * @param piece     the piece.
   * @param target    the target piece.
   * @param promotion the promotion.
   * @return the Move.
   */
  public static int createMove(int type, int start, int end, int piece, int target, int promotion) {
    assert type != NOMOVE;
    assert (type == Type.NULL && start == Position.NOPOSITION) || (start & 0x88) == 0;
    assert (type == Type.NULL && end == Position.NOPOSITION) || (end & 0x88) == 0;

    int move = 0;

    // Check for special case Move.NULL
    if (type == Type.NULL) {
      start = INTERNAL_NOPOSITION;
      end = INTERNAL_NOPOSITION;
    }

    // Encode start
    move |= start << START_SHIFT;

    // Encode end
    move |= end << END_SHIFT;

    // Encode piece
    assert piece == IntPiece.NOPIECE
      || (IntPiece.getChessman(piece) == IntChessman.PAWN)
      || (IntPiece.getChessman(piece) == IntChessman.KNIGHT)
      || (IntPiece.getChessman(piece) == IntChessman.BISHOP)
      || (IntPiece.getChessman(piece) == IntChessman.ROOK)
      || (IntPiece.getChessman(piece) == IntChessman.QUEEN)
      || (IntPiece.getChessman(piece) == IntChessman.KING);
    assert piece == IntPiece.NOPIECE
      || (IntPiece.getColor(piece) == IntColor.WHITE)
      || (IntPiece.getColor(piece) == IntColor.BLACK);
    move |= piece << CHESSMAN_PIECE_SHIFT;

    // Encode target
    assert target == IntPiece.NOPIECE
      || (IntPiece.getChessman(target) == IntChessman.PAWN)
      || (IntPiece.getChessman(target) == IntChessman.KNIGHT)
      || (IntPiece.getChessman(target) == IntChessman.BISHOP)
      || (IntPiece.getChessman(target) == IntChessman.ROOK)
      || (IntPiece.getChessman(target) == IntChessman.QUEEN);
    assert target == IntPiece.NOPIECE
      || (IntPiece.getColor(target) == IntColor.WHITE)
      || (IntPiece.getColor(target) == IntColor.BLACK);
    move |= target << TARGET_PIECE_SHIFT;

    // Encode promotion
    assert promotion == IntChessman.NOCHESSMAN
      || (promotion == IntChessman.KNIGHT)
      || (promotion == IntChessman.BISHOP)
      || (promotion == IntChessman.ROOK)
      || (promotion == IntChessman.QUEEN);
    move |= promotion << PROMOTION_SHIFT;

    // Encode move
    assert (type == Type.NORMAL)
      || (type == Type.PAWNDOUBLE)
      || (type == Type.PAWNPROMOTION)
      || (type == Type.ENPASSANT)
      || (type == Type.CASTLING)
      || (type == Type.NULL);
    move |= type << MOVE_SHIFT;

    return move;
  }

  /**
   * Sets the end position value in the move.
   *
   * @param move        the move.
   * @param endPosition the end position.
   * @return the move.
   */
  public static int setEndPosition(int move, int endPosition) {
    assert move != Move.NOMOVE;
    assert endPosition != Position.NOPOSITION;

    // Zero out the end position
    move &= ~END_MASK;

    // Encode the end position
    move |= endPosition << END_SHIFT;

    return move;
  }

  /**
   * Sets the end position value and the target piece in the move.
   *
   * @param move        the move.
   * @param endPosition the end position.
   * @param target      the target piece.
   * @return the move.
   */
  public static int setEndPositionAndTarget(int move, int endPosition, int target) {
    assert move != Move.NOMOVE;
    assert endPosition != Position.NOPOSITION;
    assert target != IntPiece.NOPIECE;

    // Zero out the end position and the target piece
    move &= ~END_MASK;
    move &= ~TARGET_PIECE_MASK;

    // Encode the end position
    move |= endPosition << END_SHIFT;

    // Encode target
    assert (IntPiece.getChessman(target) == IntChessman.PAWN)
      || (IntPiece.getChessman(target) == IntChessman.KNIGHT)
      || (IntPiece.getChessman(target) == IntChessman.BISHOP)
      || (IntPiece.getChessman(target) == IntChessman.ROOK)
      || (IntPiece.getChessman(target) == IntChessman.QUEEN);
    assert (IntPiece.getColor(target) == IntColor.WHITE)
      || (IntPiece.getColor(target) == IntColor.BLACK);
    move |= target << TARGET_PIECE_SHIFT;

    return move;
  }

  /**
   * Sets the promotion chessman in the move.
   *
   * @param move      the move.
   * @param promotion the promotion chessman.
   * @return the move.
   */
  public static int setPromotion(int move, int promotion) {
    assert move != Move.NOMOVE;
    assert promotion != IntChessman.NOCHESSMAN;

    // Zero out the promotion chessman
    move &= ~PROMOTION_MASK;

    // Encode the end position
    move |= promotion << PROMOTION_SHIFT;

    return move;
  }

  /**
   * Get the start position value from the move.
   *
   * @param move the move.
   * @return the start position value of the move.
   */
  public static int getStart(int move) {
    assert move != NOMOVE;

    int position = (move & START_MASK) >>> START_SHIFT;

    assert getType(move) != Move.Type.NULL;
    assert position != INTERNAL_NOPOSITION;
    assert (position & 0x88) == 0;

    return position;
  }

  /**
   * Get the end position value from the move.
   *
   * @param move the move.
   * @return the end position value of the move.
   */
  public static int getEnd(int move) {
    assert move != NOMOVE;

    int position = (move & END_MASK) >>> END_SHIFT;

    assert getType(move) != Move.Type.NULL;
    assert position != INTERNAL_NOPOSITION;
    assert (position & 0x88) == 0;

    return position;
  }

  /**
   * Returns the piece from the move.
   *
   * @param move the move.
   * @return the piece.
   */
  public static int getOriginPiece(int move) {
    assert move != NOMOVE;

    return (move & CHESSMAN_PIECE_MASK) >>> CHESSMAN_PIECE_SHIFT;
  }

  /**
   * Returns the target piece from the move.
   *
   * @param move the move.
   * @return the piece.
   */
  public static int getTargetPiece(int move) {
    assert move != NOMOVE;

    return (move & TARGET_PIECE_MASK) >>> TARGET_PIECE_SHIFT;
  }

  /**
   * Get the promotion chessman from the move.
   *
   * @param move the move.
   * @return the promotion chessman.
   */
  public static int getPromotion(int move) {
    assert move != NOMOVE;

    int promotion = (move & PROMOTION_MASK) >>> PROMOTION_SHIFT;
    assert (promotion == IntChessman.KNIGHT)
      || (promotion == IntChessman.BISHOP)
      || (promotion == IntChessman.ROOK)
      || (promotion == IntChessman.QUEEN);

    return promotion;
  }

  /**
   * Get the type from the move.
   *
   * @param move the move.
   * @return the type.
   */
  public static int getType(int move) {
    assert move != NOMOVE;

    int type = (move & MOVE_MASK) >>> MOVE_SHIFT;
    assert isValidMove(type);

    return type;
  }

  /**
   * Returns the Move from the GenericMove.
   *
   * @param move  the GenericMove.
   * @param board the Board.
   * @return the Move.
   */
  public static int convertMove(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    if (isPawnPromotion(move, board)) {
      int promotion;
      if (move.promotion == null) {
        // TODO: maybe better throw IllegalArgumentException()
        promotion = IntChessman.QUEEN;
      } else {
        promotion = IntChessman.valueOf(move.promotion);
      }
      return createMove(Type.PAWNPROMOTION, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(move.to)], promotion);
    } else if (isPawnDouble(move, board)) {
      return createMove(Type.PAWNDOUBLE, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    } else if (isEnPassant(move, board)) {
      return createMove(Type.ENPASSANT, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(GenericPosition.valueOf(move.to.file, move.from.rank))], IntChessman.NOCHESSMAN);
    } else if (isCastling(move, board)) {
      return createMove(Type.CASTLING, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    } else {
      return createMove(Type.NORMAL, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(move.to)], IntChessman.NOCHESSMAN);
    }
  }

  /**
   * Returns whether the GenericMove is a pawn promotion move.
   *
   * @param move  the GenericMove.
   * @param board the Board.
   * @return true if the GenericMove is a pawn promotion, false otherwise.
   */
  private static boolean isPawnPromotion(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int position = Position.valueOfPosition(move.from);

    int piece = board.board[position];
    if (piece != IntPiece.NOPIECE) {
      if ((piece == IntPiece.WHITEPAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R8)
        || (piece == IntPiece.BLACKPAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R1)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the GenericMove is a pawn double advance move.
   *
   * @param move  the GenericMove.
   * @param board the Board.
   * @return true if the GenericMove is a pawn double advance, false otherwise.
   */
  private static boolean isPawnDouble(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int position = Position.valueOfPosition(move.from);

    int piece = board.board[position];
    if (piece != IntPiece.NOPIECE) {
      if ((piece == IntPiece.WHITEPAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R4)
        || (piece == IntPiece.BLACKPAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R5)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the GenericMove is a en passant move.
   *
   * @param move  the GenericMove.
   * @param board the Board.
   * @return true if the GenericMove is a en passant move, false otherwise.
   */
  private static boolean isEnPassant(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int position = Position.valueOfPosition(move.from);
    GenericPosition targetPosition = GenericPosition.valueOf(move.to.file, move.from.rank);
    int targetIntPosition = Position.valueOfPosition(targetPosition);

    int piece = board.board[position];
    int target = board.board[targetIntPosition];
    if (piece != IntPiece.NOPIECE && target != IntPiece.NOPIECE) {
      if (IntPiece.getChessman(piece) == IntChessman.PAWN && IntPiece.getChessman(target) == IntChessman.PAWN) {
        if (IntPiece.getColor(piece) == IntColor.opposite(IntPiece.getColor(target))) {
          if (board.enPassantSquare == Position.valueOfPosition(move.to)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Returns whether the GenericMove is a castling move.
   *
   * @param move  the GenericMove.
   * @param board the Board.
   * @return true if the GenericMove is a castling move, false otherwise.
   */
  private static boolean isCastling(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int position = Position.valueOfPosition(move.from);

    int piece = board.board[position];
    if (piece != IntPiece.NOPIECE) {
      if (IntPiece.getChessman(piece) == IntChessman.KING) {
        if (move.from.file == GenericFile.Fe
          && move.from.rank == GenericRank.R1
          && move.to.file == GenericFile.Fg
          && move.to.rank == GenericRank.R1) {
          // Castling WHITE kingside.
          return true;
        } else if (move.from.file == GenericFile.Fe
          && move.from.rank == GenericRank.R1
          && move.to.file == GenericFile.Fc
          && move.to.rank == GenericRank.R1) {
          // Castling WHITE queenside.
          return true;
        } else if (move.from.file == GenericFile.Fe
          && move.from.rank == GenericRank.R8
          && move.to.file == GenericFile.Fg
          && move.to.rank == GenericRank.R8) {
          // Castling BLACK kingside.
          return true;
        } else if (move.from.file == GenericFile.Fe
          && move.from.rank == GenericRank.R8
          && move.to.file == GenericFile.Fc
          && move.to.rank == GenericRank.R8) {
          // Castling BLACK queenside.
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns the GenericMove from the move.
   *
   * @param move the move.
   * @return the GenericMove.
   */
  public static GenericMove toGenericMove(int move) {
    assert move != NOMOVE;

    int type = getType(move);
    int start = getStart(move);
    int end = getEnd(move);

    switch (type) {
      case Type.NORMAL:
      case Type.PAWNDOUBLE:
      case Type.ENPASSANT:
      case Type.CASTLING:
        return new GenericMove(Position.valueOfIntPosition(start), Position.valueOfIntPosition(end));
      case Type.PAWNPROMOTION:
        return new GenericMove(Position.valueOfIntPosition(start), Position.valueOfIntPosition(end), IntChessman.toGenericChessman(getPromotion(move)));
      case Type.NULL:
        // TODO:
        return null;
      default:
        throw new IllegalArgumentException();
    }

  }

  public static String toString(int move) {
    String string = "<";

    switch (getType(move)) {
      case Type.NORMAL:
        string += "NORMAL";
        break;
      case Type.PAWNDOUBLE:
        string += "PAWNDOUBLE";
        break;
      case Type.PAWNPROMOTION:
        string += "PAWNPROMOTION";
        break;
      case Type.ENPASSANT:
        string += "ENPASSANT";
        break;
      case Type.CASTLING:
        string += "CASTLING";
        break;
      case Type.NULL:
        string += "NULL";
        break;
      default:
        throw new IllegalArgumentException();
    }

    assert getOriginPiece(move) != IntPiece.NOPIECE;
    string += ", (";
    string += IntPiece.toGenericPiece(getOriginPiece(move));
    string += ")";

    string += ", ";
    string += toGenericMove(move).toString();

    if (getTargetPiece(move) != IntPiece.NOPIECE) {
      string += ", (";
      string += IntPiece.toGenericPiece(getTargetPiece(move));
      string += ")";
    }

    string += ">";

    return string;
  }

  public static boolean isValidMove(int move) {
    for (int moveValue : Type.values) {
      if (move == moveValue) {
        return true;
      }
    }

    return false;
  }

}
