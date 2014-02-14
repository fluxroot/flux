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

import com.fluxchess.jcpi.models.GenericScore;

public class IntValue {

	/**
	 * Represents no value
	 */
	public static final int NOVALUE = -9;

	/**
	 * IntValue values
	 */
	public static final int EXACT = 0;
	public static final int ALPHA = 1;
	public static final int BETA = 2;

	/**
	 * IntValue mask
	 */
	public static final int MASK = 0x3;

	/**
	 * IntValue cannot be instantiated.
	 */
	private IntValue() {
	}

	/**
	 * Returns the IntValue value of the EnumValue.
	 * 
	 * @param value the EnumValue.
	 * @return the IntValue value.
	 */
	public static int valueOfValue(GenericScore value) {
		assert value != null;

		switch (value) {
		case EXACT:
			return EXACT;
		case ALPHA:
			return ALPHA;
		case BETA:
			return BETA;
		default:
			assert false : value;
			break;
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Returns the EnumValue of the IntValue value.
	 * 
	 * @param value the IntValue value.
	 * @return the EnumValue.
	 */
	public static GenericScore valueOfIntValue(int value) {
		assert value != NOVALUE;

		switch (value) {
		case EXACT:
			return GenericScore.EXACT;
		case ALPHA:
			return GenericScore.ALPHA;
		case BETA:
			return GenericScore.BETA;
		default:
			throw new IllegalArgumentException();
		}
	}

}
