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

/**
 * Notes: Ideas from Fruit
 */
public final class Attack {

	public static final int MAXATTACK = 16;

	/**
	 * Represents no attack
	 */
	public static final int NOATTACK = -3;
	
	public int count = NOATTACK;
	public int[] delta = new int[MAXATTACK];
	public int[] position = new int[MAXATTACK];
	public int numberOfMoves = -1;

	public Attack() {
	}

	public boolean isCheck() {
		return this.count != 0;
	}

}
