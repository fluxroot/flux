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
package com.fluxchess.evaluation;

/**
 * EVAL_KNIGHT_SAFETY
 *
 * @author Phokham Nonava
 */
public final class EVAL_KNIGHT_SAFETY extends Parameter {

    public EVAL_KNIGHT_SAFETY() {
        super("EVAL_KNIGHT_SAFETY");
    }

    @Override
    public void setValue(int value) {
        KnightEvaluation.EVAL_KNIGHT_SAFETY = value;
    }

    @Override
    public int getValue() {
        return KnightEvaluation.EVAL_KNIGHT_SAFETY;
    }

}
