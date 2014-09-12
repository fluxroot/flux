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

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

/**
 * This class represents a move as a int value. The fields are represented by
 * the following bits.
 * <p/>
 * 0 -  6: the start position (required except Move.NULL)
 * 7 - 13: the end position (required except Move.NULL)
 * 14 - 16: the chessman (optional)
 * 17: the chessman color (optional)
 * 18 - 20: the target (optional)
 * 21: the target color (optional)
 * 22 - 24: the promotion chessman (optional)
 * 25 - 27: the type (required)
 */
final class Move {

  /**
   * Represents no move
   */
  static final int NOMOVE = -8;

  // Position value
  // We do not use 127 because there all bits are set
  private static final int INTERNAL_NOPOSITION = 126;

  // Bit operation values
  private static final int START_SHIFT = 0;
  private static final int START_MASK = Square.MASK << START_SHIFT;
  private static final int END_SHIFT = 7;
  private static final int END_MASK = Square.MASK << END_SHIFT;
  private static final int CHESSMAN_SHIFT = 14;
  private static final int CHESSMAN_MASK = PieceType.MASK << CHESSMAN_SHIFT;
  private static final int CHESSMAN_COLOR_SHIFT = 17;
  private static final int CHESSMAN_COLOR_MASK = Color.MASK << CHESSMAN_COLOR_SHIFT;
  private static final int CHESSMAN_PIECE_SHIFT = CHESSMAN_SHIFT;
  private static final int CHESSMAN_PIECE_MASK = Piece.PIECE_MASK << CHESSMAN_PIECE_SHIFT;
  private static final int TARGET_SHIFT = 18;
  private static final int TARGET_MASK = PieceType.MASK << TARGET_SHIFT;
  private static final int TARGET_COLOR_SHIFT = 21;
  private static final int TARGET_COLOR_MASK = Color.MASK << TARGET_COLOR_SHIFT;
  private static final int TARGET_PIECE_SHIFT = TARGET_SHIFT;
  private static final int TARGET_PIECE_MASK = Piece.PIECE_MASK << TARGET_PIECE_SHIFT;
  private static final int PROMOTION_SHIFT = 22;
  private static final int PROMOTION_MASK = PieceType.MASK << PROMOTION_SHIFT;
  private static final int MOVE_SHIFT = 25;
  private static final int MOVE_MASK = MoveType.MASK << MOVE_SHIFT;

  /**
   * IntMove cannot be instantiated.
   */
  private Move() {
  }

  /**
   * Get the IntMove.
   *
   * @param type      the type.
   * @param start     the start position.
   * @param end       the end position.
   * @param piece     the piece.
   * @param target    the target piece.
   * @param promotion the promotion.
   * @return the IntMove.
   */
  static int createMove(int type, int start, int end, int piece, int target, int promotion) {
    assert type != NOMOVE;
    assert (start & 0x88) == 0;
    assert (end & 0x88) == 0;

    int move = 0;

    // Encode start
    move |= start << START_SHIFT;

    // Encode end
    move |= end << END_SHIFT;

    // Encode piece
    assert piece == Piece.NOPIECE
        || (Piece.getChessman(piece) == PieceType.PAWN)
        || (Piece.getChessman(piece) == PieceType.KNIGHT)
        || (Piece.getChessman(piece) == PieceType.BISHOP)
        || (Piece.getChessman(piece) == PieceType.ROOK)
        || (Piece.getChessman(piece) == PieceType.QUEEN)
        || (Piece.getChessman(piece) == PieceType.KING);
    assert piece == Piece.NOPIECE
        || (Piece.getColor(piece) == Color.WHITE)
        || (Piece.getColor(piece) == Color.BLACK);
    move |= piece << CHESSMAN_PIECE_SHIFT;

    // Encode target
    assert target == Piece.NOPIECE
        || (Piece.getChessman(target) == PieceType.PAWN)
        || (Piece.getChessman(target) == PieceType.KNIGHT)
        || (Piece.getChessman(target) == PieceType.BISHOP)
        || (Piece.getChessman(target) == PieceType.ROOK)
        || (Piece.getChessman(target) == PieceType.QUEEN);
    assert target == Piece.NOPIECE
        || (Piece.getColor(target) == Color.WHITE)
        || (Piece.getColor(target) == Color.BLACK);
    move |= target << TARGET_PIECE_SHIFT;

    // Encode promotion
    assert promotion == Piece.NOPIECE
        || (promotion == PieceType.KNIGHT)
        || (promotion == PieceType.BISHOP)
        || (promotion == PieceType.ROOK)
        || (promotion == PieceType.QUEEN);
    move |= promotion << PROMOTION_SHIFT;

    // Encode move
    assert (type == MoveType.NORMAL)
        || (type == MoveType.PAWNDOUBLE)
        || (type == MoveType.PAWNPROMOTION)
        || (type == MoveType.ENPASSANT)
        || (type == MoveType.CASTLING);
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
  static int setEndPosition(int move, int endPosition) {
    assert move != Move.NOMOVE;
    assert endPosition != Square.NOPOSITION;

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
  static int setEndPositionAndTarget(int move, int endPosition, int target) {
    assert move != Move.NOMOVE;
    assert endPosition != Square.NOPOSITION;
    assert target != Piece.NOPIECE;

    // Zero out the end position and the target piece
    move &= ~END_MASK;
    move &= ~TARGET_PIECE_MASK;

    // Encode the end position
    move |= endPosition << END_SHIFT;

    // Encode target
    assert (Piece.getChessman(target) == PieceType.PAWN)
        || (Piece.getChessman(target) == PieceType.KNIGHT)
        || (Piece.getChessman(target) == PieceType.BISHOP)
        || (Piece.getChessman(target) == PieceType.ROOK)
        || (Piece.getChessman(target) == PieceType.QUEEN);
    assert (Piece.getColor(target) == Color.WHITE)
        || (Piece.getColor(target) == Color.BLACK);
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
  static int setPromotion(int move, int promotion) {
    assert move != Move.NOMOVE;
    assert promotion != Piece.NOPIECE;

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
  static int getStart(int move) {
    assert move != NOMOVE;

    int position = (move & START_MASK) >>> START_SHIFT;

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
  static int getEnd(int move) {
    assert move != NOMOVE;

    int position = (move & END_MASK) >>> END_SHIFT;

    assert position != INTERNAL_NOPOSITION;
    assert (position & 0x88) == 0;

    return position;
  }

  /**
   * Get the chessman from the IntMove.
   *
   * @param move the IntMove.
   * @return the chessman.
   */
  static int getChessman(int move) {
    assert move != NOMOVE;

    int chessman = (move & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;
    assert (chessman == PieceType.PAWN)
        || (chessman == PieceType.KNIGHT)
        || (chessman == PieceType.BISHOP)
        || (chessman == PieceType.ROOK)
        || (chessman == PieceType.QUEEN)
        || (chessman == PieceType.KING);

    return chessman;
  }

  /**
   * Get the chessman IntColor from the IntMove.
   *
   * @param move the IntMove.
   * @return the chessman IntColor.
   */
  static int getChessmanColor(int move) {
    assert move != NOMOVE;
    assert getChessman(move) != Piece.NOPIECE;

    int color = (move & CHESSMAN_COLOR_MASK) >>> CHESSMAN_COLOR_SHIFT;
    assert Color.isValidColor(color);

    return color;
  }

  /**
   * Returns the piece from the move.
   *
   * @param move the move.
   * @return the piece.
   */
  static int getChessmanPiece(int move) {
    assert move != NOMOVE;

    return (move & CHESSMAN_PIECE_MASK) >>> CHESSMAN_PIECE_SHIFT;
  }

  /**
   * Get the target chessman from the move.
   *
   * @param move the move.
   * @return the target chessman.
   */
  static int getTarget(int move) {
    assert move != NOMOVE;

    int chessman = (move & TARGET_MASK) >>> TARGET_SHIFT;
    assert (chessman == PieceType.PAWN)
        || (chessman == PieceType.KNIGHT)
        || (chessman == PieceType.BISHOP)
        || (chessman == PieceType.ROOK)
        || (chessman == PieceType.QUEEN)
        || (chessman == PieceType.KING)
        || (chessman == Piece.NOPIECE);

    return chessman;
  }

  /**
   * Get the target IntColor from the IntMove.
   *
   * @param move the move.
   * @return the target IntColor.
   */
  static int getTargetColor(int move) {
    assert move != NOMOVE;
    assert getTarget(move) != Piece.NOPIECE;

    int color = (move & TARGET_COLOR_MASK) >>> TARGET_COLOR_SHIFT;
    assert Color.isValidColor(color);

    return color;
  }

  /**
   * Returns the target piece from the move.
   *
   * @param move the move.
   * @return the piece.
   */
  static int getTargetPiece(int move) {
    assert move != NOMOVE;

    return (move & TARGET_PIECE_MASK) >>> TARGET_PIECE_SHIFT;
  }

  /**
   * Get the promotion chessman from the move.
   *
   * @param move the move.
   * @return the promotion chessman.
   */
  static int getPromotion(int move) {
    assert move != NOMOVE;

    int promotion = (move & PROMOTION_MASK) >>> PROMOTION_SHIFT;
    assert (promotion == PieceType.KNIGHT)
        || (promotion == PieceType.BISHOP)
        || (promotion == PieceType.ROOK)
        || (promotion == PieceType.QUEEN);

    return promotion;
  }

  /**
   * Get the type from the move.
   *
   * @param move the move.
   * @return the type.
   */
  static int getType(int move) {
    assert move != NOMOVE;

    int type = (move & MOVE_MASK) >>> MOVE_SHIFT;
    assert isValidMove(type);

    return type;
  }

  /**
   * Returns the IntMove from the CommandMove.
   *
   * @param move  the CommandMove.
   * @param board the Hex88Board.
   * @return the IntMove.
   */
  static int convertMove(GenericMove move, Position board) {
    assert move != null;
    assert board != null;

    if (isPawnPromotion(move, board)) {
      int promotion;
      if (move.promotion == null) {
        // TODO: maybe better throw IllegalArgumentException()
        promotion = PieceType.QUEEN;
      } else {
        promotion = Piece.valueOfChessman(move.promotion);
      }
      return createMove(MoveType.PAWNPROMOTION, Square.valueOfPosition(move.from), Square.valueOfPosition(move.to), Position.board[Square.valueOfPosition(move.from)], Position.board[Square.valueOfPosition(move.to)], promotion);
    } else if (isPawnDouble(move, board)) {
      return createMove(MoveType.PAWNDOUBLE, Square.valueOfPosition(move.from), Square.valueOfPosition(move.to), Position.board[Square.valueOfPosition(move.from)], Piece.NOPIECE, Piece.NOPIECE);
    } else if (isEnPassant(move, board)) {
      return createMove(MoveType.ENPASSANT, Square.valueOfPosition(move.from), Square.valueOfPosition(move.to), Position.board[Square.valueOfPosition(move.from)], Position.board[Square.valueOfPosition(GenericPosition.valueOf(move.to.file, move.from.rank))], Piece.NOPIECE);
    } else if (isCastling(move, board)) {
      return createMove(MoveType.CASTLING, Square.valueOfPosition(move.from), Square.valueOfPosition(move.to), Position.board[Square.valueOfPosition(move.from)], Piece.NOPIECE, Piece.NOPIECE);
    } else {
      return createMove(MoveType.NORMAL, Square.valueOfPosition(move.from), Square.valueOfPosition(move.to), Position.board[Square.valueOfPosition(move.from)], Position.board[Square.valueOfPosition(move.to)], Piece.NOPIECE);
    }
  }

  /**
   * Returns whether the CommandMove is a pawn promotion move.
   *
   * @param move  the CommandMove.
   * @param board the Hex88Board.
   * @return true if the CommandMove is a pawn promotion, false otherwise.
   */
  private static boolean isPawnPromotion(GenericMove move, Position board) {
    assert move != null;
    assert board != null;

    int position = Square.valueOfPosition(move.from);

    int piece = Position.board[position];
    if (piece != Piece.NOPIECE) {
      if ((piece == Piece.WHITE_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R8)
          || (piece == Piece.BLACK_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R1)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the CommandMove is a pawn double advance move.
   *
   * @param move  the CommandMove.
   * @param board the Hex88Board.
   * @return true if the CommandMove is a pawn double advance, false otherwise.
   */
  private static boolean isPawnDouble(GenericMove move, Position board) {
    assert move != null;
    assert board != null;

    int position = Square.valueOfPosition(move.from);

    int piece = Position.board[position];
    if (piece != Piece.NOPIECE) {
      if ((piece == Piece.WHITE_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R4)
          || (piece == Piece.BLACK_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R5)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether the CommandMove is a en passant move.
   *
   * @param move  the CommandMove.
   * @param board the Hex88Board.
   * @return true if the CommandMove is a en passant move, false otherwise.
   */
  private static boolean isEnPassant(GenericMove move, Position board) {
    assert move != null;
    assert board != null;

    int position = Square.valueOfPosition(move.from);
    GenericPosition targetPosition = GenericPosition.valueOf(move.to.file, move.from.rank);
    int targetIntPosition = Square.valueOfPosition(targetPosition);

    int piece = Position.board[position];
    int target = Position.board[targetIntPosition];
    if (piece != Piece.NOPIECE && target != Piece.NOPIECE) {
      if (Piece.getChessman(piece) == PieceType.PAWN && Piece.getChessman(target) == PieceType.PAWN) {
        if (Piece.getColor(piece) == Piece.getColorOpposite(target)) {
          if (board.enPassantSquare == Square.valueOfPosition(move.to)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Returns whether the CommandMove is a castling move.
   *
   * @param move  the CommandMove.
   * @param board the Hex88Board.
   * @return true if the CommandMove is a castling move, false otherwise.
   */
  private static boolean isCastling(GenericMove move, Position board) {
    assert move != null;
    assert board != null;

    int position = Square.valueOfPosition(move.from);

    int piece = Position.board[position];
    if (piece != Piece.NOPIECE) {
      if (Piece.getChessman(piece) == PieceType.KING) {
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
   * Returns the CommandMove from the move.
   *
   * @param move the move.
   * @return the CommandMove.
   */
  static GenericMove toCommandMove(int move) {
    assert move != NOMOVE;

    int type = getType(move);
    int start = getStart(move);
    int end = getEnd(move);

    switch (type) {
      case MoveType.NORMAL:
      case MoveType.PAWNDOUBLE:
      case MoveType.ENPASSANT:
      case MoveType.CASTLING:
        return new GenericMove(Square.valueOfIntPosition(start), Square.valueOfIntPosition(end));
      case MoveType.PAWNPROMOTION:
        return new GenericMove(Square.valueOfIntPosition(start), Square.valueOfIntPosition(end), Piece.valueOfIntChessman(getPromotion(move)));
      default:
        throw new IllegalArgumentException();
    }

  }

  static String toString(int move) {
    String string = "<";

    switch (getType(move)) {
      case MoveType.NORMAL:
        string += "NORMAL";
        break;
      case MoveType.PAWNDOUBLE:
        string += "PAWNDOUBLE";
        break;
      case MoveType.PAWNPROMOTION:
        string += "PAWNPROMOTION";
        break;
      case MoveType.ENPASSANT:
        string += "ENPASSANT";
        break;
      case MoveType.CASTLING:
        string += "CASTLING";
        break;
      default:
        throw new IllegalArgumentException();
    }

    if (getChessman(move) != Piece.NOPIECE) {
      string += ", (";
      string += Color.valueOfIntColor(getChessmanColor(move));
      string += "/";
      string += Piece.valueOfIntChessman(getChessman(move));
      string += ")";
    }

    string += ", ";
    string += toCommandMove(move).toString();

    if (getTarget(move) != Piece.NOPIECE) {
      string += ", (";
      string += Color.valueOfIntColor(getTargetColor(move));
      string += "/";
      string += Piece.valueOfIntChessman(getTarget(move));
      string += ")";
    }

    string += ">";

    return string;
  }

  static boolean isValidMove(int move) {
    for (int moveValue : MoveType.values) {
      if (move == moveValue) {
        return true;
      }
    }

    return false;
  }

}
