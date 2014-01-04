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
 *  0 -  6: the start position (required except Move.NULL)
 *  7 - 13: the end position (required except Move.NULL)
 * 14 - 16: the chessman (optional)
 * 17   18: the chessman color (optional)
 * 19 - 21: the target (optional)
 * 22 - 23: the target color (optional)
 * 24 - 26: the promotion chessman (optional)
 * 27 - 29: the type (required)
 */
public final class Move {

  public static final int NOMOVE = -8;

  public static final int NULLMOVE;

  public static final int NORMAL = 0;
  public static final int PAWNDOUBLE = 1;
  public static final int PAWNPROMOTION = 2;
  public static final int ENPASSANT = 3;
  public static final int CASTLING = 4;
  public static final int NULL = 5;

  public static final int[] values = {
    NORMAL,
    PAWNDOUBLE,
    PAWNPROMOTION,
    ENPASSANT,
    CASTLING,
    NULL
  };

  public static final int MASK = 0x7;

  public static final int INTMOVE_MASK = 0xFFFFFFF;
  public static final int INTMOVE_SIZE = 28;

  // Position value
  // We do not use 127 because there all bits are set
  private static final int INTERNAL_NOPOSITION = 126;

  // Bit operation values
  private static final int START_SHIFT = 0;
  private static final int START_MASK = Position.MASK << START_SHIFT;
  private static final int END_SHIFT = 7;
  private static final int END_MASK = Position.MASK << END_SHIFT;
  private static final int CHESSMAN_SHIFT = 14;
  private static final int CHESSMAN_MASK = IntChessman.MASK << CHESSMAN_SHIFT;
  private static final int CHESSMAN_COLOR_SHIFT = 17;
  private static final int CHESSMAN_COLOR_MASK = IntColor.MASK << CHESSMAN_COLOR_SHIFT;
  private static final int CHESSMAN_PIECE_SHIFT = CHESSMAN_SHIFT;
  private static final int CHESSMAN_PIECE_MASK = IntChessman.PIECE_MASK << CHESSMAN_PIECE_SHIFT;
  private static final int TARGET_SHIFT = 19;
  private static final int TARGET_MASK = IntChessman.MASK << TARGET_SHIFT;
  private static final int TARGET_COLOR_SHIFT = 22;
  private static final int TARGET_COLOR_MASK = IntColor.MASK << TARGET_COLOR_SHIFT;
  private static final int TARGET_PIECE_SHIFT = TARGET_SHIFT;
  private static final int TARGET_PIECE_MASK = IntChessman.PIECE_MASK << TARGET_PIECE_SHIFT;
  private static final int PROMOTION_SHIFT = 24;
  private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;
  private static final int MOVE_SHIFT = 27;
  private static final int MOVE_MASK = MASK << MOVE_SHIFT;

  static {
    NULLMOVE = Move.createMove(Move.NULL, Position.NOPOSITION, Position.NOPOSITION, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
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
    assert (type == NULL && start == Position.NOPOSITION) || (start & 0x88) == 0;
    assert (type == NULL && end == Position.NOPOSITION) || (end & 0x88) == 0;

    int move = 0;

    // Check for special case Move.NULL
    if (type == NULL) {
      start = INTERNAL_NOPOSITION;
      end = INTERNAL_NOPOSITION;
    }

    // Encode start
    move |= start << START_SHIFT;

    // Encode end
    move |= end << END_SHIFT;

    // Encode piece
    assert piece == IntChessman.NOPIECE
      || (IntChessman.getChessman(piece) == IntChessman.PAWN)
      || (IntChessman.getChessman(piece) == IntChessman.KNIGHT)
      || (IntChessman.getChessman(piece) == IntChessman.BISHOP)
      || (IntChessman.getChessman(piece) == IntChessman.ROOK)
      || (IntChessman.getChessman(piece) == IntChessman.QUEEN)
      || (IntChessman.getChessman(piece) == IntChessman.KING);
    assert piece == IntChessman.NOPIECE
      || (IntChessman.getColor(piece) == IntColor.WHITE)
      || (IntChessman.getColor(piece) == IntColor.BLACK);
    move |= piece << CHESSMAN_PIECE_SHIFT;

    // Encode target
    assert target == IntChessman.NOPIECE
      || (IntChessman.getChessman(target) == IntChessman.PAWN)
      || (IntChessman.getChessman(target) == IntChessman.KNIGHT)
      || (IntChessman.getChessman(target) == IntChessman.BISHOP)
      || (IntChessman.getChessman(target) == IntChessman.ROOK)
      || (IntChessman.getChessman(target) == IntChessman.QUEEN);
    assert target == IntChessman.NOPIECE
      || (IntChessman.getColor(target) == IntColor.WHITE)
      || (IntChessman.getColor(target) == IntColor.BLACK);
    move |= target << TARGET_PIECE_SHIFT;

    // Encode promotion
    assert promotion == IntChessman.NOPIECE
      || (promotion == IntChessman.KNIGHT)
      || (promotion == IntChessman.BISHOP)
      || (promotion == IntChessman.ROOK)
      || (promotion == IntChessman.QUEEN);
    move |= promotion << PROMOTION_SHIFT;

    // Encode move
    assert (type == NORMAL)
      || (type == PAWNDOUBLE)
      || (type == PAWNPROMOTION)
      || (type == ENPASSANT)
      || (type == CASTLING)
      || (type == NULL);
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
    assert target != IntChessman.NOPIECE;

    // Zero out the end position and the target piece
    move &= ~END_MASK;
    move &= ~TARGET_PIECE_MASK;

    // Encode the end position
    move |= endPosition << END_SHIFT;

    // Encode target
    assert (IntChessman.getChessman(target) == IntChessman.PAWN)
      || (IntChessman.getChessman(target) == IntChessman.KNIGHT)
      || (IntChessman.getChessman(target) == IntChessman.BISHOP)
      || (IntChessman.getChessman(target) == IntChessman.ROOK)
      || (IntChessman.getChessman(target) == IntChessman.QUEEN);
    assert (IntChessman.getColor(target) == IntColor.WHITE)
      || (IntChessman.getColor(target) == IntColor.BLACK);
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
    assert promotion != IntChessman.NOPIECE;

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

    assert getType(move) != Move.NULL;
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

    assert getType(move) != Move.NULL;
    assert position != INTERNAL_NOPOSITION;
    assert (position & 0x88) == 0;

    return position;
  }

  /**
   * Get the chessman from the Move.
   *
   * @param move the Move.
   * @return the chessman.
   */
  public static int getChessman(int move) {
    assert move != NOMOVE;

    int chessman = (move & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;
    assert (chessman == IntChessman.PAWN)
      || (chessman == IntChessman.KNIGHT)
      || (chessman == IntChessman.BISHOP)
      || (chessman == IntChessman.ROOK)
      || (chessman == IntChessman.QUEEN)
      || (chessman == IntChessman.KING);

    return chessman;
  }

  /**
   * Get the chessman IntColor from the Move.
   *
   * @param move the Move.
   * @return the chessman IntColor.
   */
  public static int getChessmanColor(int move) {
    assert move != NOMOVE;
    assert getChessman(move) != IntChessman.NOPIECE;

    int color = (move & CHESSMAN_COLOR_MASK) >>> CHESSMAN_COLOR_SHIFT;
    assert IntColor.isValid(color);

    return color;
  }

  /**
   * Returns the piece from the move.
   *
   * @param move the move.
   * @return the piece.
   */
  public static int getChessmanPiece(int move) {
    assert move != NOMOVE;

    return (move & CHESSMAN_PIECE_MASK) >>> CHESSMAN_PIECE_SHIFT;
  }

  /**
   * Get the target chessman from the move.
   *
   * @param move the move.
   * @return the target chessman.
   */
  public static int getTarget(int move) {
    assert move != NOMOVE;

    int chessman = (move & TARGET_MASK) >>> TARGET_SHIFT;
    assert (chessman == IntChessman.PAWN)
      || (chessman == IntChessman.KNIGHT)
      || (chessman == IntChessman.BISHOP)
      || (chessman == IntChessman.ROOK)
      || (chessman == IntChessman.QUEEN)
      || (chessman == IntChessman.KING)
      || (chessman == IntChessman.NOPIECE);

    return chessman;
  }

  /**
   * Get the target IntColor from the Move.
   *
   * @param move the move.
   * @return the target IntColor.
   */
  public static int getTargetColor(int move) {
    assert move != NOMOVE;
    assert getTarget(move) != IntChessman.NOPIECE;

    int color = (move & TARGET_COLOR_MASK) >>> TARGET_COLOR_SHIFT;
    assert IntColor.isValid(color);

    return color;
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
        promotion = IntChessman.valueOfChessman(move.promotion);
      }
      return createMove(PAWNPROMOTION, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(move.to)], promotion);
    } else if (isPawnDouble(move, board)) {
      return createMove(PAWNDOUBLE, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], IntChessman.NOPIECE, IntChessman.NOPIECE);
    } else if (isEnPassant(move, board)) {
      return createMove(ENPASSANT, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(GenericPosition.valueOf(move.to.file, move.from.rank))], IntChessman.NOPIECE);
    } else if (isCastling(move, board)) {
      return createMove(CASTLING, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], IntChessman.NOPIECE, IntChessman.NOPIECE);
    } else {
      return createMove(NORMAL, Position.valueOfPosition(move.from), Position.valueOfPosition(move.to), board.board[Position.valueOfPosition(move.from)], board.board[Position.valueOfPosition(move.to)], IntChessman.NOPIECE);
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
    if (piece != IntChessman.NOPIECE) {
      if ((piece == IntChessman.WHITE_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R8)
        || (piece == IntChessman.BLACK_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R1)) {
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
    if (piece != IntChessman.NOPIECE) {
      if ((piece == IntChessman.WHITE_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R4)
        || (piece == IntChessman.BLACK_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R5)) {
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
    if (piece != IntChessman.NOPIECE && target != IntChessman.NOPIECE) {
      if (IntChessman.getChessman(piece) == IntChessman.PAWN && IntChessman.getChessman(target) == IntChessman.PAWN) {
        if (IntChessman.getColor(piece) == IntChessman.getColorOpposite(target)) {
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
    if (piece != IntChessman.NOPIECE) {
      if (IntChessman.getChessman(piece) == IntChessman.KING) {
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
      case NORMAL:
      case PAWNDOUBLE:
      case ENPASSANT:
      case CASTLING:
        return new GenericMove(Position.valueOfIntPosition(start), Position.valueOfIntPosition(end));
      case PAWNPROMOTION:
        return new GenericMove(Position.valueOfIntPosition(start), Position.valueOfIntPosition(end), IntChessman.valueOfIntChessman(getPromotion(move)));
      case NULL:
        // TODO:
        return null;
      default:
        throw new IllegalArgumentException();
    }

  }

  public static String toString(int move) {
    String string = "<";

    switch (getType(move)) {
      case NORMAL:
        string += "NORMAL";
        break;
      case PAWNDOUBLE:
        string += "PAWNDOUBLE";
        break;
      case PAWNPROMOTION:
        string += "PAWNPROMOTION";
        break;
      case ENPASSANT:
        string += "ENPASSANT";
        break;
      case CASTLING:
        string += "CASTLING";
        break;
      case NULL:
        string += "NULL";
        break;
      default:
        throw new IllegalArgumentException();
    }

    if (getChessman(move) != IntChessman.NOPIECE) {
      string += ", (";
      string += IntColor.toGenericColor(getChessmanColor(move));
      string += "/";
      string += IntChessman.valueOfIntChessman(getChessman(move));
      string += ")";
    }

    string += ", ";
    string += toGenericMove(move).toString();

    if (getTarget(move) != IntChessman.NOPIECE) {
      string += ", (";
      string += IntColor.toGenericColor(getTargetColor(move));
      string += "/";
      string += IntChessman.valueOfIntChessman(getTarget(move));
      string += ")";
    }

    string += ">";

    return string;
  }

  public static boolean isValidMove(int move) {
    for (int moveValue : values) {
      if (move == moveValue) {
        return true;
      }
    }

    return false;
  }

}
