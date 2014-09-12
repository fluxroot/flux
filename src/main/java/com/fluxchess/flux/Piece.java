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

import com.fluxchess.jcpi.models.GenericChessman;

/**
 * This class represents a chessman as a int value. The fields are
 * represented by the following bits.<br/>
 * <br/>
 * <code>0 -  2</code>: the type (required)<br/>
 * <code>     3</code>: the color (required)<br/>
 * <br/>
 */
final class Piece {

  /**
   * Represents no piece
   */
  static final int NOPIECE = 4;

  /**
   * Bit operations
   */
  private static final int BIT_SLIDING = 4;

  // Bit operation values
  private static final int CHESSMAN_SHIFT = 0;
  private static final int CHESSMAN_MASK = PieceType.MASK << CHESSMAN_SHIFT;
  private static final int COLOR_SHIFT = 3;
  private static final int COLOR_MASK = Color.MASK << COLOR_SHIFT;

  /**
   * Piece values
   */
  static final int WHITE_PAWN = (PieceType.PAWN << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int WHITE_KNIGHT = (PieceType.KNIGHT << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int WHITE_BISHOP = (PieceType.BISHOP << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int WHITE_ROOK = (PieceType.ROOK << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int WHITE_QUEEN = (PieceType.QUEEN << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int WHITE_KING = (PieceType.KING << CHESSMAN_SHIFT) | (Color.WHITE << COLOR_SHIFT);
  static final int BLACK_PAWN = (PieceType.PAWN << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);
  static final int BLACK_KNIGHT = (PieceType.KNIGHT << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);
  static final int BLACK_BISHOP = (PieceType.BISHOP << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);
  static final int BLACK_ROOK = (PieceType.ROOK << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);
  static final int BLACK_QUEEN = (PieceType.QUEEN << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);
  static final int BLACK_KING = (PieceType.KING << CHESSMAN_SHIFT) | (Color.BLACK << COLOR_SHIFT);

  static final int[] pieceValues = {
      WHITE_PAWN,
      WHITE_KNIGHT,
      WHITE_BISHOP,
      WHITE_ROOK,
      WHITE_QUEEN,
      WHITE_KING,
      BLACK_PAWN,
      BLACK_KNIGHT,
      BLACK_BISHOP,
      BLACK_ROOK,
      BLACK_QUEEN,
      BLACK_KING
  };

  /**
   * Piece
   */
  static final int PIECE_MASK = 0xF;
  static final int PIECE_VALUE_SIZE = 16;

  /**
   * Values
   */
  static final int VALUE_PAWN = 100;
  static final int VALUE_KNIGHT = 325;
  static final int VALUE_BISHOP = 325;
  static final int VALUE_ROOK = 500;
  static final int VALUE_QUEEN = 975;
  static final int VALUE_KING = 20000;

  /**
   * IntChessman cannot be instantiated.
   */
  private Piece() {
  }

  /**
   * Returns the chessman value of the GenericChessman.
   *
   * @param chessman the GenericChessman.
   * @return the chessman value.
   */
  static int valueOfChessman(GenericChessman chessman) {
    assert chessman != null;

    switch (chessman) {
      case PAWN:
        return PieceType.PAWN;
      case KNIGHT:
        return PieceType.KNIGHT;
      case BISHOP:
        return PieceType.BISHOP;
      case ROOK:
        return PieceType.ROOK;
      case QUEEN:
        return PieceType.QUEEN;
      case KING:
        return PieceType.KING;
      default:
        assert false : chessman;
        break;
    }

    throw new IllegalArgumentException();
  }

  /**
   * Returns the GenericChessman of the chessman value.
   *
   * @param chessman the IntChessman value.
   * @return the GenericChessman.
   */
  static GenericChessman valueOfIntChessman(int chessman) {
    assert chessman != NOPIECE;

    switch (chessman) {
      case PieceType.PAWN:
        return GenericChessman.PAWN;
      case PieceType.KNIGHT:
        return GenericChessman.KNIGHT;
      case PieceType.BISHOP:
        return GenericChessman.BISHOP;
      case PieceType.ROOK:
        return GenericChessman.ROOK;
      case PieceType.QUEEN:
        return GenericChessman.QUEEN;
      case PieceType.KING:
        return GenericChessman.KING;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Get the piece from GenericChessman and GenericColor.
   *
   * @param chessman the chessman.
   * @param color    the color.
   * @return the piece.
   */
  static int createPiece(int chessman, int color) {
    assert chessman != NOPIECE;
    assert color != Color.NOCOLOR;

    int piece = 0;

    // Set the chessman
    piece |= chessman << CHESSMAN_SHIFT;
    assert (chessman == PieceType.PAWN)
        || (chessman == PieceType.KNIGHT)
        || (chessman == PieceType.BISHOP)
        || (chessman == PieceType.ROOK)
        || (chessman == PieceType.QUEEN)
        || (chessman == PieceType.KING);

    // Set the color
    piece |= color << COLOR_SHIFT;
    assert (color == Color.WHITE)
        || (color == Color.BLACK);

    return piece;
  }

  /**
   * Get the piece for the promotion from GenericChessman and GenericColor. We do
   * not set the position here, as we do not know the position yet.
   *
   * @param chessman the chessman.
   * @param color    the color.
   * @return the promotion piece.
   */
  static int createPromotion(int chessman, int color) {
    assert chessman != NOPIECE;
    assert color != Color.NOCOLOR;

    int piece = 0;

    // Set the promotion chessman
    piece |= chessman << CHESSMAN_SHIFT;
    assert (chessman == PieceType.KNIGHT)
        || (chessman == PieceType.BISHOP)
        || (chessman == PieceType.ROOK)
        || (chessman == PieceType.QUEEN);

    // Set the color
    piece |= color << COLOR_SHIFT;
    assert (color == Color.WHITE)
        || (color == Color.BLACK);

    return piece;
  }

  /**
   * Get the chessman value from the piece.
   *
   * @param piece the piece.
   * @return the chessman value of the piece.
   */
  static int getChessman(int piece) {
    assert piece != NOPIECE;

    int chessman = (piece & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;
    assert isValidChessman(chessman);

    return chessman;
  }

  /**
   * Get the color value from the piece.
   *
   * @param piece the piece.
   * @return the color value of the piece.
   */
  static int getColor(int piece) {
    assert piece != NOPIECE;

    int color = (piece & COLOR_MASK) >>> COLOR_SHIFT;
    assert Color.isValidColor(color);

    return color;
  }

  /**
   * Get the opposite IntColor value from the piece.
   *
   * @param piece the piece.
   * @return the opposite IntColor value of the piece.
   */
  static int getColorOpposite(int piece) {
    assert piece != NOPIECE;

    int color = ((piece & COLOR_MASK) ^ COLOR_MASK) >>> COLOR_SHIFT;

    return color;
  }

  /**
   * Returns the value of the piece.
   *
   * @param piece the piece.
   * @return the value of the piece.
   */
  static int getValueFromPiece(int piece) {
    assert piece != NOPIECE;

    int chessman = (piece & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;

    switch (chessman) {
      case PieceType.PAWN:
        return VALUE_PAWN;
      case PieceType.KNIGHT:
        return VALUE_KNIGHT;
      case PieceType.BISHOP:
        return VALUE_BISHOP;
      case PieceType.ROOK:
        return VALUE_ROOK;
      case PieceType.QUEEN:
        return VALUE_QUEEN;
      case PieceType.KING:
        return VALUE_KING;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the value of the chessman.
   *
   * @param chessman the chessman.
   * @return the value of the chessman.
   */
  static int getValueFromChessman(int chessman) {
    assert chessman != NOPIECE;

    switch (chessman) {
      case PieceType.PAWN:
        return VALUE_PAWN;
      case PieceType.KNIGHT:
        return VALUE_KNIGHT;
      case PieceType.BISHOP:
        return VALUE_BISHOP;
      case PieceType.ROOK:
        return VALUE_ROOK;
      case PieceType.QUEEN:
        return VALUE_QUEEN;
      case PieceType.KING:
        return VALUE_KING;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns whether the piece is a sliding piece.
   *
   * @param piece the piece.
   * @return true if the piece is a sliding piece, false otherwise.
   */
  static boolean isSliding(int piece) {
    assert piece != NOPIECE;

    int chessman = (piece & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;

    assert ((chessman & BIT_SLIDING) != 0 && (chessman == PieceType.BISHOP || chessman == PieceType.ROOK || chessman == PieceType.QUEEN))
        || ((chessman & BIT_SLIDING) == 0 && (chessman == PieceType.PAWN || chessman == PieceType.KNIGHT || chessman == PieceType.KING));

    return (chessman & BIT_SLIDING) != 0;
  }

  static boolean isValidChessman(int chessman) {
    for (int chessmanValue : PieceType.values) {
      if (chessman == chessmanValue) {
        return true;
      }
    }

    return false;
  }

}
