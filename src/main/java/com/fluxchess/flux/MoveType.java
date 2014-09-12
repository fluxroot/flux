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

final class MoveType {

  static final int MASK = 0x7;

  static final int NORMAL = 0;
  static final int PAWNDOUBLE = 1;
  static final int PAWNPROMOTION = 2;
  static final int ENPASSANT = 3;
  static final int CASTLING = 4;
  static final int NOMOVETYPE = 5;

  static final int[] values = {
      NORMAL, PAWNDOUBLE, PAWNPROMOTION, ENPASSANT, CASTLING
  };

  private MoveType() {
  }

  static boolean isValid(int movetype) {
    switch (movetype) {
      case NORMAL:
      case PAWNDOUBLE:
      case PAWNPROMOTION:
      case ENPASSANT:
      case CASTLING:
        return true;
      default:
        return false;
    }
  }

}
