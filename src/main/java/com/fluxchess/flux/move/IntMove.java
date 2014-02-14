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
package com.fluxchess.flux.move;

import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.board.IntChessman;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.board.IntPosition;
import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

/**
 * This class represents a move as a int value. The fields are represented by
 * the following bits.
 * 
 *  0 -  6: the start position (required except Move.NULL)
 *  7 - 13: the end position (required except Move.NULL)
 * 14 - 16: the chessman (optional)
 *      17: the chessman color (optional)
 * 18 - 20: the target (optional)
 *      21: the target color (optional)
 * 22 - 24: the promotion chessman (optional)
 * 25 - 27: the type (required)
 */
public final class IntMove {

	/**
	 * Represents no move
	 */
	public static final int NOMOVE = -8;
	
	/**
	 * Null move
	 */
	public static final int NULLMOVE;
	
	/**
	 * Move values
	 */
	public static final int NORMAL = 0;
	public static final int PAWNDOUBLE = 1;
	public static final int PAWNPROMOTION = 2;
	public static final int ENPASSANT = 3;
	public static final int CASTLING = 4;
	public static final int NULL = 5;

	/**
	 * IntMove array
	 */
	public static final int[] values = {
		NORMAL,
		PAWNDOUBLE,
		PAWNPROMOTION,
		ENPASSANT,
		CASTLING,
		NULL
	};

	/**
	 * Move mask
	 */
	public static final int MASK = 0x7;

	/**
	 * IntMove
	 */
	public static final int INTMOVE_MASK = 0xFFFFFFF;
	public static final int INTMOVE_SIZE = 28;

	// Position value
	// We do not use 127 because there all bits are set
	private static final int INTERNAL_NOPOSITION = 126;

	// Bit operation values
	private static final int START_SHIFT = 0;
	private static final int START_MASK = IntPosition.MASK << START_SHIFT;
	private static final int END_SHIFT = 7;
	private static final int END_MASK = IntPosition.MASK << END_SHIFT;
	private static final int CHESSMAN_SHIFT = 14;
	private static final int CHESSMAN_MASK = IntChessman.MASK << CHESSMAN_SHIFT;
	private static final int CHESSMAN_COLOR_SHIFT = 17;
	private static final int CHESSMAN_COLOR_MASK = IntColor.MASK << CHESSMAN_COLOR_SHIFT;
	private static final int CHESSMAN_PIECE_SHIFT = CHESSMAN_SHIFT;
	private static final int CHESSMAN_PIECE_MASK = IntChessman.PIECE_MASK << CHESSMAN_PIECE_SHIFT;
	private static final int TARGET_SHIFT = 18;
	private static final int TARGET_MASK = IntChessman.MASK << TARGET_SHIFT;
	private static final int TARGET_COLOR_SHIFT = 21;
	private static final int TARGET_COLOR_MASK = IntColor.MASK << TARGET_COLOR_SHIFT;
	private static final int TARGET_PIECE_SHIFT = TARGET_SHIFT;
	private static final int TARGET_PIECE_MASK = IntChessman.PIECE_MASK << TARGET_PIECE_SHIFT;
	private static final int PROMOTION_SHIFT = 22;
	private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;
	private static final int MOVE_SHIFT = 25;
	private static final int MOVE_MASK = MASK << MOVE_SHIFT;

	static {
		NULLMOVE = IntMove.createMove(IntMove.NULL, IntPosition.NOPOSITION, IntPosition.NOPOSITION, IntChessman.NOPIECE, IntChessman.NOPIECE, IntChessman.NOPIECE);
	}

	/**
	 * IntMove cannot be instantiated.
	 */
	private IntMove() {
	}

	/**
	 * Get the IntMove.
	 * 
	 * @param type the type.
	 * @param start the start position.
	 * @param end the end position.
	 * @param piece the piece.
	 * @param target the target piece.
	 * @param promotion the promotion.
	 * @return the IntMove.
	 */
	public static int createMove(int type, int start, int end, int piece, int target, int promotion) {
		assert type != NOMOVE;
		assert (type == NULL && start == IntPosition.NOPOSITION) || (start & 0x88) == 0;
		assert (type == NULL && end == IntPosition.NOPOSITION) || (end & 0x88) == 0;
		
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
	 * @param move the move.
	 * @param endPosition the end position.
	 * @return the move.
	 */
	public static int setEndPosition(int move, int endPosition) {
		assert move != IntMove.NOMOVE;
		assert endPosition != IntPosition.NOPOSITION;

		// Zero out the end position
		move &= ~END_MASK;

		// Encode the end position
		move |= endPosition << END_SHIFT;
		
		return move;
	}

	/**
	 * Sets the end position value and the target piece in the move.
	 * 
	 * @param move the move.
	 * @param endPosition the end position.
	 * @param target the target piece.
	 * @return the move.
	 */
	public static int setEndPositionAndTarget(int move, int endPosition, int target) {
		assert move != IntMove.NOMOVE;
		assert endPosition != IntPosition.NOPOSITION;
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
	 * @param move the move.
	 * @param promotion the promotion chessman.
	 * @return the move.
	 */
	public static int setPromotion(int move, int promotion) {
		assert move != IntMove.NOMOVE;
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

		assert getType(move) != IntMove.NULL;
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

		assert getType(move) != IntMove.NULL;
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
	 * Get the chessman IntColor from the IntMove.
	 * 
	 * @param move the IntMove.
	 * @return the chessman IntColor.
	 */
	public static int getChessmanColor(int move) {
		assert move != NOMOVE;
		assert getChessman(move) != IntChessman.NOPIECE;

		int color = (move & CHESSMAN_COLOR_MASK) >>> CHESSMAN_COLOR_SHIFT;
		assert IntColor.isValidColor(color);

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
	 * Get the target IntColor from the IntMove.
	 * 
	 * @param move the move.
	 * @return the target IntColor.
	 */
	public static int getTargetColor(int move) {
		assert move != NOMOVE;
		assert getTarget(move) != IntChessman.NOPIECE;
		
		int color = (move & TARGET_COLOR_MASK) >>> TARGET_COLOR_SHIFT;
		assert IntColor.isValidColor(color);

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
	 * Returns the IntMove from the CommandMove.
	 * 
	 * @param move the CommandMove.
	 * @param board the Hex88Board.
	 * @return the IntMove.
	 */
	public static int convertMove(GenericMove move, Hex88Board board) {
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
			return createMove(PAWNPROMOTION, IntPosition.valueOfPosition(move.from), IntPosition.valueOfPosition(move.to), Hex88Board.board[IntPosition.valueOfPosition(move.from)], Hex88Board.board[IntPosition.valueOfPosition(move.to)], promotion);
		} else if (isPawnDouble(move, board)) {
			return createMove(PAWNDOUBLE, IntPosition.valueOfPosition(move.from), IntPosition.valueOfPosition(move.to), Hex88Board.board[IntPosition.valueOfPosition(move.from)], IntChessman.NOPIECE, IntChessman.NOPIECE);
		} else if (isEnPassant(move, board)) {
			return createMove(ENPASSANT, IntPosition.valueOfPosition(move.from), IntPosition.valueOfPosition(move.to), Hex88Board.board[IntPosition.valueOfPosition(move.from)], Hex88Board.board[IntPosition.valueOfPosition(GenericPosition.valueOf(move.to.file, move.from.rank))], IntChessman.NOPIECE);
		} else if (isCastling(move, board)) {
			return createMove(CASTLING, IntPosition.valueOfPosition(move.from), IntPosition.valueOfPosition(move.to), Hex88Board.board[IntPosition.valueOfPosition(move.from)], IntChessman.NOPIECE, IntChessman.NOPIECE);
		} else {
			return createMove(NORMAL, IntPosition.valueOfPosition(move.from), IntPosition.valueOfPosition(move.to), Hex88Board.board[IntPosition.valueOfPosition(move.from)], Hex88Board.board[IntPosition.valueOfPosition(move.to)], IntChessman.NOPIECE);
		}
	}

	/**
	 * Returns whether the CommandMove is a pawn promotion move.
	 * 
	 * @param move the CommandMove.
	 * @param board the Hex88Board.
	 * @return true if the CommandMove is a pawn promotion, false otherwise.
	 */
	private static boolean isPawnPromotion(GenericMove move, Hex88Board board) {
		assert move != null;
		assert board != null;
		
		int position = IntPosition.valueOfPosition(move.from);
		
		int piece = Hex88Board.board[position];
		if (piece != IntChessman.NOPIECE) {
			if ((piece == IntChessman.WHITE_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R8)
					|| (piece == IntChessman.BLACK_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R1)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the CommandMove is a pawn double advance move.
	 * 
	 * @param move the CommandMove.
	 * @param board the Hex88Board.
	 * @return true if the CommandMove is a pawn double advance, false otherwise.
	 */
	private static boolean isPawnDouble(GenericMove move, Hex88Board board) {
		assert move != null;
		assert board != null;

		int position = IntPosition.valueOfPosition(move.from);

		int piece = Hex88Board.board[position];
		if (piece != IntChessman.NOPIECE) {
			if ((piece == IntChessman.WHITE_PAWN && move.from.rank == GenericRank.R2 && move.to.rank == GenericRank.R4)
				|| (piece == IntChessman.BLACK_PAWN && move.from.rank == GenericRank.R7 && move.to.rank == GenericRank.R5)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the CommandMove is a en passant move.
	 * 
	 * @param move the CommandMove.
	 * @param board the Hex88Board.
	 * @return true if the CommandMove is a en passant move, false otherwise.
	 */
	private static boolean isEnPassant(GenericMove move, Hex88Board board) {
		assert move != null;
		assert board != null;

		int position = IntPosition.valueOfPosition(move.from);
		GenericPosition targetPosition = GenericPosition.valueOf(move.to.file, move.from.rank);
		int targetIntPosition = IntPosition.valueOfPosition(targetPosition);

		int piece = Hex88Board.board[position];
		int target = Hex88Board.board[targetIntPosition];
		if (piece != IntChessman.NOPIECE && target != IntChessman.NOPIECE) {
			if (IntChessman.getChessman(piece) == IntChessman.PAWN && IntChessman.getChessman(target) == IntChessman.PAWN) {
				if (IntChessman.getColor(piece) == IntChessman.getColorOpposite(target)) {
					if (board.enPassantSquare == IntPosition.valueOfPosition(move.to)) {
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
	 * @param move the CommandMove.
	 * @param board the Hex88Board.
	 * @return true if the CommandMove is a castling move, false otherwise.
	 */
	private static boolean isCastling(GenericMove move, Hex88Board board) {
		assert move != null;
		assert board != null;

		int position = IntPosition.valueOfPosition(move.from);

		int piece = Hex88Board.board[position];
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
	 * Returns the CommandMove from the move.
	 * 
	 * @param move the move.
	 * @return the CommandMove.
	 */
	public static GenericMove toCommandMove(int move) {
		assert move != NOMOVE;
		
		int type = getType(move);
		int start = getStart(move);
		int end = getEnd(move);
		
		switch (type) {
		case NORMAL:
		case PAWNDOUBLE:
		case ENPASSANT:
		case CASTLING:
			return new GenericMove(IntPosition.valueOfIntPosition(start), IntPosition.valueOfIntPosition(end));
		case PAWNPROMOTION:
			return new GenericMove(IntPosition.valueOfIntPosition(start), IntPosition.valueOfIntPosition(end), IntChessman.valueOfIntChessman(getPromotion(move)));
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
			string += IntColor.valueOfIntColor(getChessmanColor(move));
			string += "/";
			string += IntChessman.valueOfIntChessman(getChessman(move));
			string += ")";
		}

		string += ", ";
		string += toCommandMove(move).toString();

		if (getTarget(move) != IntChessman.NOPIECE) {
			string += ", (";
			string += IntColor.valueOfIntColor(getTargetColor(move));
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
