/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.move;

import jcpi.data.GenericScore;


/**
 * IntScore
 *
 * @author Phokham Nonava
 */
public class IntScore {

	/**
	 * Represents no score
	 */
	public static final int NOSCORE = -9;

	/**
	 * IntScore values
	 */
	public static final int EXACT = 0;
	public static final int ALPHA = 1;
	public static final int BETA = 2;

	/**
	 * IntScore mask
	 */
	public static final int MASK = 0x3;

	/**
	 * IntScore cannot be instantiated.
	 */
	private IntScore() {
	}

	/**
	 * Returns the IntScore value of the GenericScore.
	 * 
	 * @param value the GenericScore.
	 * @return the IntScore value.
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
	 * Returns the GenericScore of the IntScore value.
	 * 
	 * @param value the IntScore value.
	 * @return the GenericScore.
	 */
	public static GenericScore valueOfIntScore(int value) {
		assert value != NOSCORE;

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
