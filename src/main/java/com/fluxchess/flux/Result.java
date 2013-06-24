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
package com.fluxchess.flux;

import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.IntScore;

/**
 * Result
 *
 * @author Phokham Nonava
 */
public final class Result {

    public int bestMove = IntMove.NOMOVE;
    public int ponderMove = IntMove.NOMOVE;
    public int value = IntScore.NOSCORE;
    public int resultValue = -Search.INFINITY;
    public long time = -1;
    public int moveNumber = 0;
    public int depth = 0;

    /**
     * Creates a new Result.
     */
    public Result() {
    }

}
