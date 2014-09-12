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

import com.fluxchess.jcpi.models.GenericColor;

final class Color {

  /**
   * Represents no color
   */
  static final int NOCOLOR = -4;

  /**
   * IntColor values
   */
  static final int WHITE = 0;
  static final int BLACK = 1;

  /**
   * IntColor constants
   */
  static final int ARRAY_DIMENSION = 2;

  /**
   * IntColor array
   */
  static final int[] values = {
      WHITE,
      BLACK
  };

  /**
   * IntColor mask
   */
  static final int MASK = 0x1;

  /**
   * IntColor cannot be instantiated.
   */
  private Color() {
  }

  /**
   * Returns the IntColor value of the GenericColor.
   *
   * @param color the GenericColor.
   * @return the IntColor value.
   */
  static int valueOfColor(GenericColor color) {
    assert color != null;

    switch (color) {
      case WHITE:
        return WHITE;
      case BLACK:
        return BLACK;
      default:
        assert false : color;
        break;
    }

    throw new IllegalArgumentException();
  }

  /**
   * Returns the GenericColor of the color value.
   *
   * @param color the color value.
   * @return the GenericColor.
   */
  static GenericColor valueOfIntColor(int color) {
    assert color != NOCOLOR;

    switch (color) {
      case WHITE:
        return GenericColor.WHITE;
      case BLACK:
        return GenericColor.BLACK;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the opposite color.
   *
   * @param color the color.
   * @return the opposite color.
   */
  static int switchColor(int color) {
    assert color != NOCOLOR && (color == WHITE || color == BLACK);

    assert (color ^ MASK) == WHITE || (color ^ MASK) == BLACK;
    return color ^ MASK;
  }

  static boolean isValidColor(int color) {
    for (int colorValue : values) {
      if (color == colorValue) {
        return true;
      }
    }

    return false;
  }

}
