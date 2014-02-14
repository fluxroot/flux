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

import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;

public final class PrincipalVariation implements Comparable<PrincipalVariation> {

	public final int moveNumber;
	public final int value;
	public final int type;
	public final int sortValue;
	public final List<GenericMove> pv;
	public final int depth;
	public final int maxDepth;
	public final int hash;
	public final long nps;
	public final long time;
	public final long totalNodes;

	public PrincipalVariation(int moveNumber, int value, int type, int sortValue, List<GenericMove> pv, int depth, int maxDepth, int hash, long nps, long time, long totalNodes) {
		this.moveNumber = moveNumber;
		this.value = value;
		this.type = type;
		this.sortValue = sortValue;
		this.pv = pv;
		this.depth = depth;
		this.maxDepth = maxDepth;
		this.hash = hash;
		this.nps = nps;
		this.time = time;
		this.totalNodes = totalNodes;
	}

	public int compareTo(PrincipalVariation o) {
		int result;
		if (this.depth > o.depth) {
			result = -1;
		} else if (this.depth == o.depth) {
			if (this.sortValue > o.sortValue) {
				result = -1;
			} else if (this.sortValue == o.sortValue) {
				if (this.moveNumber < o.moveNumber) {
					result = -1;
				} else if (this.moveNumber == o.moveNumber) {
					result = 0;
				} else {
					result = 1;
				}
			} else {
				result = 1;
			}
		} else {
			result = 1;
		}

		return result;
	}

}
