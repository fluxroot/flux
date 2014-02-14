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

import com.fluxchess.jcpi.models.GenericChessman;

/**
 * This class represents a chessman as a int value. The fields are
 * represented by the following bits.<br/>
 * <br/>
 * <code>0 -  2</code>: the type (required)<br/>
 * <code>     3</code>: the color (required)<br/>
 *<br/>
 */
public final class IntChessman {

	/**
	 * Represents no piece
	 */
	public static final int NOPIECE = 4;
	
	/**
	 * Chessman values
	 */
	public static final int PAWN = 1;
	public static final int KNIGHT = 2;
	public static final int KING = 3;
	public static final int BISHOP = 5;
	public static final int ROOK = 6;
	public static final int QUEEN = 7;
	
	/**
	 * Bit operations
	 */
	public static final int BIT_SLIDING = 4;
	public static final int BIT_DIAGONAL_SLIDING = 1;
	public static final int BIT_STRAIGHT_SLIDING = 2;
	
	/**
	 * Chessman array
	 */
	public static final int CHESSMAN_VALUE_SIZE = 8;
	public static final int[] values = {
		PAWN,
		KNIGHT,
		BISHOP,
		ROOK,
		QUEEN,
		KING
	};
	
	/**
	 * Chessman mask
	 */
	public static final int MASK = 0x7;
	
	/**
	 * Chessman
	 */
	public static final int INTCHESSMAN_MASK = 0x7FF;
	public static final int INTCHESSMAN_SIZE = 11;

	// Bit operation values
	private static final int CHESSMAN_SHIFT = 0;
	private static final int CHESSMAN_MASK = MASK << CHESSMAN_SHIFT;
	private static final int COLOR_SHIFT = 3;
	private static final int COLOR_MASK = IntColor.MASK << COLOR_SHIFT;
	
	/**
	 * Piece values
	 */
	public static final int WHITE_PAWN = (PAWN << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int WHITE_KNIGHT = (KNIGHT << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int WHITE_BISHOP = (BISHOP << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int WHITE_ROOK = (ROOK << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int WHITE_QUEEN = (QUEEN << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int WHITE_KING = (KING << CHESSMAN_SHIFT) | (IntColor.WHITE << COLOR_SHIFT);
	public static final int BLACK_PAWN = (PAWN << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);
	public static final int BLACK_KNIGHT = (KNIGHT << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);
	public static final int BLACK_BISHOP = (BISHOP << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);
	public static final int BLACK_ROOK = (ROOK << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);
	public static final int BLACK_QUEEN = (QUEEN << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);
	public static final int BLACK_KING = (KING << CHESSMAN_SHIFT) | (IntColor.BLACK << COLOR_SHIFT);

	public static final int[] pieceValues = {
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
	public static final int PIECE_MASK = 0xF;
	public static final int PIECE_SIZE = 4;
	public static final int PIECE_VALUE_SIZE = 16;

	/**
	 * Values
	 */
	public static final int VALUE_PAWN = 100;
	public static final int VALUE_KNIGHT = 325;
	public static final int VALUE_BISHOP = 325;
	public static final int VALUE_ROOK = 500;
	public static final int VALUE_QUEEN = 975;
	public static final int VALUE_KING = 20000;

	/**
	 * IntChessman cannot be instantiated.
	 */
	private IntChessman() {
	}

	/**
	 * Returns the chessman value of the GenericChessman.
	 * 
	 * @param chessman the GenericChessman.
	 * @return the chessman value.
	 */
	public static int valueOfChessman(GenericChessman chessman) {
		assert chessman != null;

		switch (chessman) {
		case PAWN:
			return PAWN;
		case KNIGHT:
			return KNIGHT;
		case BISHOP:
			return BISHOP;
		case ROOK:
			return ROOK;
		case QUEEN:
			return QUEEN;
		case KING:
			return KING;
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
	public static GenericChessman valueOfIntChessman(int chessman) {
		assert chessman != NOPIECE;

		switch (chessman) {
		case PAWN:
			return GenericChessman.PAWN;
		case KNIGHT:
			return GenericChessman.KNIGHT;
		case BISHOP:
			return GenericChessman.BISHOP;
		case ROOK:
			return GenericChessman.ROOK;
		case QUEEN:
			return GenericChessman.QUEEN;
		case KING:
			return GenericChessman.KING;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Get the piece from GenericChessman and GenericColor.
	 * 
	 * @param chessman the chessman.
	 * @param color the color.
	 * @return the piece.
	 */
	public static int createPiece(int chessman, int color) {
		assert chessman != NOPIECE;
		assert color != IntColor.NOCOLOR;
		
		int piece = 0;

		// Set the chessman
		piece |= chessman << CHESSMAN_SHIFT;
		assert (chessman == PAWN)
			|| (chessman == KNIGHT)
			|| (chessman == BISHOP)
			|| (chessman == ROOK)
			|| (chessman == QUEEN)
			|| (chessman == KING);

		// Set the color
		piece |= color << COLOR_SHIFT;
		assert (color == IntColor.WHITE)
			|| (color == IntColor.BLACK);

		return piece;
	}

	/**
	 * Get the piece for the promotion from GenericChessman and GenericColor. We do
	 * not set the position here, as we do not know the position yet.
	 * 
	 * @param chessman the chessman.
	 * @param color the color.
	 * @return the promotion piece.
	 */
	public static int createPromotion(int chessman, int color) {
		assert chessman != NOPIECE;
		assert color != IntColor.NOCOLOR;

		int piece = 0;

		// Set the promotion chessman
		piece |= chessman << CHESSMAN_SHIFT;
		assert (chessman == KNIGHT)
			|| (chessman == BISHOP)
			|| (chessman == ROOK)
			|| (chessman == QUEEN);

		// Set the color
		piece |= color << COLOR_SHIFT;
		assert (color == IntColor.WHITE)
			|| (color == IntColor.BLACK);

		return piece;
	}

	/**
	 * Get the chessman value from the piece.
	 * 
	 * @param piece the piece.
	 * @return the chessman value of the piece.
	 */
	public static int getChessman(int piece) {
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
	public static int getColor(int piece) {
		assert piece != NOPIECE;

		int color = (piece & COLOR_MASK) >>> COLOR_SHIFT;
		assert IntColor.isValidColor(color);

		return color;
	}

	/**
	 * Get the opposite IntColor value from the piece.
	 * 
	 * @param piece the piece.
	 * @return the opposite IntColor value of the piece.
	 */
	public static int getColorOpposite(int piece) {
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
	public static int getValueFromPiece(int piece) {
		assert piece != NOPIECE;

		int chessman = (piece & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;
		
		switch (chessman) {
		case PAWN:
			return VALUE_PAWN;
		case KNIGHT:
			return VALUE_KNIGHT;
		case BISHOP:
			return VALUE_BISHOP;
		case ROOK:
			return VALUE_ROOK;
		case QUEEN:
			return VALUE_QUEEN;
		case KING:
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
	public static int getValueFromChessman(int chessman) {
		assert chessman != NOPIECE;

		switch (chessman) {
		case PAWN:
			return VALUE_PAWN;
		case KNIGHT:
			return VALUE_KNIGHT;
		case BISHOP:
			return VALUE_BISHOP;
		case ROOK:
			return VALUE_ROOK;
		case QUEEN:
			return VALUE_QUEEN;
		case KING:
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
	public static boolean isSliding(int piece) {
		assert piece != NOPIECE;

		int chessman = (piece & CHESSMAN_MASK) >>> CHESSMAN_SHIFT;

		assert ((chessman & BIT_SLIDING) != 0 && (chessman == BISHOP || chessman == ROOK || chessman == QUEEN))
		|| ((chessman & BIT_SLIDING) == 0 && (chessman == PAWN || chessman == KNIGHT || chessman == KING));

		return (chessman & BIT_SLIDING) != 0;
	}

	public static boolean isValidChessman(int chessman) {
		for (int chessmanValue : values) {
			if (chessman == chessmanValue) {
				return true;
			}
		}
		
		return false;
	}

}
