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

import com.fluxchess.jcpi.models.GenericColor;

public final class IntColor {

	/**
	 * Represents no color
	 */
	public static final int NOCOLOR = -4;

	/**
	 * IntColor values
	 */
	public static final int WHITE = 0;
	public static final int BLACK = 1;

	/**
	 * IntColor constants
	 */
	public static final int ARRAY_DIMENSION = 2;

	/**
	 * IntColor array
	 */
	public static final int[] values = {
		WHITE,
		BLACK
	};

	/**
	 * IntColor mask
	 */
	public static final int MASK = 0x1;

	/**
	 * IntColor cannot be instantiated.
	 */
	private IntColor() {
	}

	/**
	 * Returns the IntColor value of the GenericColor.
	 * 
	 * @param color the GenericColor.
	 * @return the IntColor value.
	 */
	public static int valueOfColor(GenericColor color) {
		assert color != null;

		switch (color) {
		case WHITE:
			return WHITE;
		case BLACK:
			return BLACK;
		default:
			assert false : color;
			break;
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Returns the GenericColor of the color value.
	 * 
	 * @param color the color value.
	 * @return the GenericColor.
	 */
	public static GenericColor valueOfIntColor(int color) {
		assert color != NOCOLOR;

		switch (color) {
		case WHITE:
			return GenericColor.WHITE;
		case BLACK:
			return GenericColor.BLACK;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns the opposite color.
	 * 
	 * @param color the color.
	 * @return the opposite color.
	 */
	public static int switchColor(int color) {
		assert color != NOCOLOR && (color == WHITE || color == BLACK);

		assert (color ^ MASK) == WHITE || (color ^ MASK) == BLACK;
		return color ^ MASK;
	}

	public static boolean isValidColor(int color) {
		for (int colorValue : values) {
			if (color == colorValue) {
				return true;
			}
		}
		
		return false;
	}

}
