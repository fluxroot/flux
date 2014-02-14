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
package com.fluxchess.flux;

import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;

public interface ISearch {

	/**
	 * The maximum number of plies.
	 */
	public static final int MAX_HEIGHT = 256;
	
	/**
	 * The maximum number of depth.
	 */
	public static final int MAX_DEPTH = 64;
	
	/**
	 * The maximum number of moves.
	 */
	public static final int MAX_MOVES = 4096;
	
	public abstract void start();
	public abstract void stop();
	public abstract void ponderhit();
	public abstract boolean isStopped();

	public abstract void setSearchDepth(int searchDepth);
	public abstract void setSearchNodes(long searchNodes);
	public abstract void setSearchTime(long searchTime);
	public abstract void setSearchClock(int side, long timeLeft);
	public abstract void setSearchClockIncrement(int side, long timeIncrement);
	public abstract void setSearchMovesToGo(int searchMovesToGo);
	public abstract void setSearchInfinite();
	public abstract void setSearchPonder();
	public abstract void setSearchMoveList(List<GenericMove> moveList);

}
