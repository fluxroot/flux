/*
 * Copyright (C) 2007-2014 Phokham Nonava
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

final class Depth {

  static final int MAX_PLY = 256;
  static final int MAX_DEPTH = 128;
  static final int MIN_DEPTH = -MAX_DEPTH;

  static final int NODEPTH = -MAX_PLY;

  private Depth() {
  }

  static boolean isValid(int depth) {
    return MIN_DEPTH <= depth && depth <= MAX_PLY;
  }

}
