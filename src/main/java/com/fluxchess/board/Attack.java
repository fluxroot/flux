/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU Lesser General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.board;

/**
 * Attack
 * 
 * Notes: Ideas from Fruit
 *
 * @author Phokham Nonava
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

	public Attack(Attack attack) {
		assert attack != null;
		
		this.count = attack.count;
		System.arraycopy(attack.delta, 0, this.delta, 0, MAXATTACK);
		System.arraycopy(attack.position, 0, this.position, 0, MAXATTACK);
		this.numberOfMoves = attack.numberOfMoves;
	}
	
	public boolean isCheck() {
		return this.count != 0;
	}

}
