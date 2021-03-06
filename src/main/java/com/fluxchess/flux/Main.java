/*
 * Copyright 2007-2020 Phokham Nonava
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

import java.util.Scanner;

public final class Main {

	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				String arguments = args[0];
				for (int i = 1; i < args.length; ++i) {
					arguments += " " + args[i];
				}

				Scanner tokens = new Scanner(arguments);

				if (!tokens.hasNext()) {
					throw new IllegalArgumentException();
				}
				String token = tokens.next();

				if (token.equalsIgnoreCase("perft")) {
					new Perft().run();
				} else {
					throw new IllegalArgumentException("Unknown argument: " + token);
				}
			} else {
				new Flux().run();
			}
		} catch (Throwable t) {
			System.out.format("Exiting Flux due to an exception: %s%n", t.getLocalizedMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}
}
