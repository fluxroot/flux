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

final class Castling {

  /**
   * IntCastling values
   */
  static final int WHITE_KINGSIDE = 1 << 0;
  static final int WHITE_QUEENSIDE = 1 << 1;
  static final int BLACK_KINGSIDE = 1 << 2;
  static final int BLACK_QUEENSIDE = 1 << 3;

  /**
   * IntCastling constants
   */
  static final int ARRAY_DIMENSION = 16;

  /**
   * IntCastling cannot be instantiated.
   */
  private Castling() {
  }

}
