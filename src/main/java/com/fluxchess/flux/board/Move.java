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
 *  0 -  2: type (required)
 *  3 -  9: origin square (required except Move.NULL)
 * 10 - 16: target square (required except Move.NULL)
 * 17 - 21: origin piece (required)
 * 22 - 26: target piece (optional)
 * 27 - 29: promotion chessman (optional)
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

  public static final int NULLMOVE;

  // Bit operation values
  private static final int TYPE_SHIFT = 0;
  private static final int TYPE_MASK = Type.MASK << TYPE_SHIFT;
  private static final int ORIGINSQUARE_SHIFT = 3;
  private static final int ORIGINSQUARE_MASK = Square.MASK << ORIGINSQUARE_SHIFT;
  private static final int TARGETSQUARE_SHIFT = 10;
  private static final int TARGETSQUARE_MASK = Square.MASK << TARGETSQUARE_SHIFT;
  private static final int ORIGINPIECE_SHIFT = 17;
  private static final int ORIGINPIECE_MASK = IntPiece.MASK << ORIGINPIECE_SHIFT;
  private static final int TARGETPIECE_SHIFT = 22;
  private static final int TARGETPIECE_MASK = IntPiece.MASK << TARGETPIECE_SHIFT;
  private static final int PROMOTION_SHIFT = 27;
  private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;

  public static final int NOMOVE = (Type.NOTYPE << TYPE_SHIFT)
    | (Square.NOSQUARE << ORIGINSQUARE_SHIFT)
    | (Square.NOSQUARE << TARGETSQUARE_SHIFT)
    | (IntPiece.NOPIECE << ORIGINPIECE_SHIFT)
    | (IntPiece.NOPIECE << TARGETPIECE_SHIFT)
    | (IntChessman.NOCHESSMAN << PROMOTION_SHIFT);

  static {
    NULLMOVE = Move.valueOf(Move.Type.NULL, Square.NOSQUARE, Square.NOSQUARE, IntPiece.NOPIECE, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
  }

  private Move() {
  }

  public static int valueOf(GenericMove genericMove, Board board) {
    assert genericMove != null;
    assert board != null;

    if (isPawnPromotion(genericMove, board)) {
      int promotion;
      if (genericMove.promotion == null) {
        // TODO: maybe better throw IllegalArgumentException()
        promotion = IntChessman.QUEEN;
      } else {
        promotion = IntChessman.valueOf(genericMove.promotion);
      }
      return valueOf(Type.PAWNPROMOTION, Square.valueOf(genericMove.from), Square.valueOf(genericMove.to), board.board[Square.valueOf(genericMove.from)], board.board[Square.valueOf(genericMove.to)], promotion);
    } else if (isPawnDouble(genericMove, board)) {
      return valueOf(Type.PAWNDOUBLE, Square.valueOf(genericMove.from), Square.valueOf(genericMove.to), board.board[Square.valueOf(genericMove.from)], IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    } else if (isEnPassant(genericMove, board)) {
      return valueOf(Type.ENPASSANT, Square.valueOf(genericMove.from), Square.valueOf(genericMove.to), board.board[Square.valueOf(genericMove.from)], board.board[Square.valueOf(GenericPosition.valueOf(genericMove.to.file, genericMove.from.rank))], IntChessman.NOCHESSMAN);
    } else if (isCastling(genericMove, board)) {
      return valueOf(Type.CASTLING, Square.valueOf(genericMove.from), Square.valueOf(genericMove.to), board.board[Square.valueOf(genericMove.from)], IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
    } else {
      return valueOf(Type.NORMAL, Square.valueOf(genericMove.from), Square.valueOf(genericMove.to), board.board[Square.valueOf(genericMove.from)], board.board[Square.valueOf(genericMove.to)], IntChessman.NOCHESSMAN);
    }
  }

  public static int valueOf(int type, int originSquare, int targetSquare, int originPiece, int targetPiece, int promotion) {
    int move = 0;

    // Encode type
    assert type == Type.NORMAL
      || type == Type.PAWNDOUBLE
      || type == Type.PAWNPROMOTION
      || type == Type.ENPASSANT
      || type == Type.CASTLING
      || type == Type.NULL;
    move |= type << TYPE_SHIFT;

    // Encode origin square
    assert (type == Type.NULL && originSquare == Square.NOSQUARE) || (originSquare & 0x88) == 0;
    move |= originSquare << ORIGINSQUARE_SHIFT;

    // Encode target square
    assert (type == Type.NULL && targetSquare == Square.NOSQUARE) || (targetSquare & 0x88) == 0;
    move |= targetSquare << TARGETSQUARE_SHIFT;

    // Encode origin piece
    assert (type == Type.NULL && originPiece == IntPiece.NOPIECE) || IntPiece.isValid(originPiece);
    move |= originPiece << ORIGINPIECE_SHIFT;

    // Encode target piece
    assert IntPiece.isValid(targetPiece) || targetPiece == IntPiece.NOPIECE;
    move |= targetPiece << TARGETPIECE_SHIFT;

    // Encode promotion
    assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
      || promotion == IntChessman.NOCHESSMAN;
    move |= promotion << PROMOTION_SHIFT;

    return move;
  }

  public static GenericMove toGenericMove(int move) {
    assert move != NOMOVE;

    int type = getType(move);
    int originSquare = getOriginSquare(move);
    int targetSquare = getTargetSquare(move);

    switch (type) {
      case Type.NORMAL:
      case Type.PAWNDOUBLE:
      case Type.ENPASSANT:
      case Type.CASTLING:
        return new GenericMove(Square.toGenericPosition(originSquare), Square.toGenericPosition(targetSquare));
      case Type.PAWNPROMOTION:
        return new GenericMove(Square.toGenericPosition(originSquare), Square.toGenericPosition(targetSquare), IntChessman.toGenericChessman(getPromotion(move)));
      case Type.NULL:
        // TODO:
        return null;
      default:
        throw new IllegalArgumentException();
    }
  }

  public static int getType(int move) {
    int type = (move & TYPE_MASK) >>> TYPE_SHIFT;
    assert type == Type.NORMAL
      || type == Type.PAWNDOUBLE
      || type == Type.PAWNPROMOTION
      || type == Type.ENPASSANT
      || type == Type.CASTLING
      || type == Type.NULL;

    return type;
  }

  public static int getOriginSquare(int move) {
    assert move != NOMOVE;

    int originSquare = (move & ORIGINSQUARE_MASK) >>> ORIGINSQUARE_SHIFT;

    assert getType(move) != Move.Type.NULL;
    assert (originSquare & 0x88) == 0;

    return originSquare;
  }

  public static int getTargetSquare(int move) {
    assert move != NOMOVE;

    int targetSquare = (move & TARGETSQUARE_MASK) >>> TARGETSQUARE_SHIFT;

    assert getType(move) != Move.Type.NULL;
    assert (targetSquare & 0x88) == 0;

    return targetSquare;
  }

  public static int setTargetSquare(int move, int targetSquare) {
    assert move != Move.NOMOVE;
    assert targetSquare != Square.NOSQUARE;

    // Zero out target square
    move &= ~TARGETSQUARE_MASK;

    // Encode target square
    assert (targetSquare & 0x88) == 0;
    move |= targetSquare << TARGETSQUARE_SHIFT;

    return move;
  }

  public static int setTargetSquareAndPiece(int move, int targetSquare, int targetPiece) {
    assert move != Move.NOMOVE;
    assert targetSquare != Square.NOSQUARE;
    assert targetPiece != IntPiece.NOPIECE;

    // Zero out target square and target piece
    move &= ~TARGETSQUARE_MASK;
    move &= ~TARGETPIECE_MASK;

    // Encode target square
    assert (targetSquare & 0x88) == 0;
    move |= targetSquare << TARGETSQUARE_SHIFT;

    // Encode target piece
    assert IntPiece.isValid(targetPiece) || targetPiece == IntPiece.NOPIECE;
    move |= targetPiece << TARGETPIECE_SHIFT;

    return move;
  }

  public static int getOriginPiece(int move) {
    int originPiece = (move & ORIGINPIECE_MASK) >>> ORIGINPIECE_SHIFT;
    assert IntPiece.isValid(originPiece);

    return originPiece;
  }

  public static int getTargetPiece(int move) {
    int targetPiece = (move & TARGETPIECE_MASK) >>> TARGETPIECE_SHIFT;
    assert IntPiece.isValid(targetPiece) || targetPiece == IntPiece.NOPIECE;

    return targetPiece;
  }

  public static int getPromotion(int move) {
    assert move != NOMOVE;

    int promotion = (move & PROMOTION_MASK) >>> PROMOTION_SHIFT;
    assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
      || promotion == IntChessman.NOCHESSMAN;

    return promotion;
  }

  public static int setPromotion(int move, int promotion) {
    assert move != Move.NOMOVE;
    assert promotion != IntChessman.NOCHESSMAN;

    // Zero out promotion chessman
    move &= ~PROMOTION_MASK;

    // Encode promotion
    assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
      || promotion == IntChessman.NOCHESSMAN;
    move |= promotion << PROMOTION_SHIFT;

    return move;
  }

  private static boolean isPawnPromotion(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int originPiece = board.board[Square.valueOf(move.from)];
    if (originPiece != IntPiece.NOPIECE) {
      if ((originPiece == IntPiece.WHITEPAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R8)
        || (originPiece == IntPiece.BLACKPAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R1)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isPawnDouble(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int originPiece = board.board[Square.valueOf(move.from)];
    if (originPiece != IntPiece.NOPIECE) {
      if ((originPiece == IntPiece.WHITEPAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R4)
        || (originPiece == IntPiece.BLACKPAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R5)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isEnPassant(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    GenericPosition genericPosition = GenericPosition.valueOf(move.to.file, move.from.rank);

    int originPiece = board.board[Square.valueOf(move.from)];
    int targetPiece = board.board[Square.valueOf(genericPosition)];
    if (originPiece != IntPiece.NOPIECE && targetPiece != IntPiece.NOPIECE) {
      if (IntPiece.getChessman(originPiece) == IntChessman.PAWN && IntPiece.getChessman(targetPiece) == IntChessman.PAWN) {
        if (IntPiece.getColor(originPiece) == IntColor.opposite(IntPiece.getColor(targetPiece))) {
          if (board.enPassantSquare == Square.valueOf(move.to)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean isCastling(GenericMove move, Board board) {
    assert move != null;
    assert board != null;

    int originPiece = board.board[Square.valueOf(move.from)];
    if (originPiece != IntPiece.NOPIECE) {
      if (IntPiece.getChessman(originPiece) == IntChessman.KING) {
        if (move.from == GenericPosition.e1 && move.to == GenericPosition.g1) {
          // Castling WHITE kingside.
          return true;
        } else if (move.from == GenericPosition.e1 && move.to == GenericPosition.c1) {
          // Castling WHITE queenside.
          return true;
        } else if (move.from == GenericPosition.e8 && move.to == GenericPosition.g8) {
          // Castling BLACK kingside.
          return true;
        } else if (move.from == GenericPosition.e8 && move.to == GenericPosition.c8) {
          // Castling BLACK queenside.
          return true;
        }
      }
    }

    return false;
  }

  public static String toString(int move) {
    String string = "";

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
    string += ":";
    string += IntPiece.toGenericPiece(getOriginPiece(move));

    string += ":";
    string += toGenericMove(move).toString();

    if (getTargetPiece(move) != IntPiece.NOPIECE) {
      string += ":";
      string += IntPiece.toGenericPiece(getTargetPiece(move));
    }

    if (getType(move) == Type.PAWNPROMOTION) {
      string += ":";
      string += IntChessman.toGenericChessman(getPromotion(move));
    }

    return string;
  }

}
