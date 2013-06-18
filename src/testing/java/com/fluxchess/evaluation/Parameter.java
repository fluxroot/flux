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
 * Parameter
 *
 * @author Phokham Nonava
 */
public abstract class Parameter {

    public final String name;
    public int defaultValue;
    public int defaultIncrement;

    private int increment = 1;

    public Parameter(String name) {
        this.name = name;
        store();
    }

    public void store() {
        defaultValue = getValue();
        defaultIncrement = increment;
    }

    public void print() {
        System.out.println(name + " = " + defaultValue);
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int value) {
        increment = value;
    }

    public abstract int getValue();
    public abstract void setValue(int value);

}
