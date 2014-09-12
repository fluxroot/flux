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

import java.security.SecureRandom;

final class Random {

  private static final SecureRandom random = new SecureRandom();

  private Random() {
  }

  static long next() {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);

    long hash = 0;
    for (int i = 0; i < bytes.length; ++i) {
      hash ^= ((long) (bytes[i] & 0xFF)) << ((i * 8) % 64);
    }

    return hash;
  }

}
