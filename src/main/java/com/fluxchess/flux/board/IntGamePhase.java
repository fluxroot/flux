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

public class IntGamePhase {

	/**
	 * Represents no game phase
	 */
	public static final int NOGAMEPHASE = -5;

	/**
	 * GamePhase values
	 */
	public static final int OPENING = 0;
	public static final int MIDDLE = 1;
	public static final int ENDGAME = 2;

	/**
	 * GamePhase array
	 */
	public static final int[] values = {
		OPENING,
		MIDDLE,
		ENDGAME
	};

	/**
	 * GamePhase mask
	 */
	public static final int MASK = 0x2;

	/**
	 * IntGamePhase cannot be instantiated.
	 */
	private IntGamePhase() {
	}

}
