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
package com.fluxchess.flux.board;

/**
 * Notes: Ideas from Mediocre Chess and Bruce Moreland
 */
public final class AttackVector {

	public static final int N = 0; // Neutral
	public static final int D = 1; // Diagonal
	public static final int u = 2; // Diagonal up in one
	public static final int d = 3; // Diagonal down in one
	public static final int S = 4; // Straight
	public static final int s = 5; // Straight in one
	public static final int K = 6; // Knight
	
	public static final int[] vector = {
		N,N,N,N,N,N,N,N,                 //   0 -   7
		D,N,N,N,N,N,N,S,N,N,N,N,N,N,D,N, //   8 -  23
		N,D,N,N,N,N,N,S,N,N,N,N,N,D,N,N, //  24 -  39
		N,N,D,N,N,N,N,S,N,N,N,N,D,N,N,N, //  40 -  55
		N,N,N,D,N,N,N,S,N,N,N,D,N,N,N,N, //  56 -  71
		N,N,N,N,D,N,N,S,N,N,D,N,N,N,N,N, //  72 -  87
		N,N,N,N,N,D,K,S,K,D,N,N,N,N,N,N, //  88 - 103
		N,N,N,N,N,K,d,s,d,K,N,N,N,N,N,N, // 104 - 119
		S,S,S,S,S,S,s,N,s,S,S,S,S,S,S,N, // 120 - 135
		N,N,N,N,N,K,u,s,u,K,N,N,N,N,N,N, // 136 - 151
		N,N,N,N,N,D,K,S,K,D,N,N,N,N,N,N, // 152 - 167
		N,N,N,N,D,N,N,S,N,N,D,N,N,N,N,N, // 168 - 183
		N,N,N,D,N,N,N,S,N,N,N,D,N,N,N,N, // 184 - 199
		N,N,D,N,N,N,N,S,N,N,N,N,D,N,N,N, // 200 - 215
		N,D,N,N,N,N,N,S,N,N,N,N,N,D,N,N, // 216 - 231
		D,N,N,N,N,N,N,S,N,N,N,N,N,N,D,N, // 232 - 247
		N,N,N,N,N,N,N,N                  // 248 - 255
	};

	public static final int[] delta = {
		  0,  0,  0,  0,  0,  0,  0,  0,                                 //   0 -   7
		-17,  0,  0,  0,  0,  0,  0,-16,  0,  0,  0,  0,  0,  0,-15,  0, //   8 -  23
		  0,-17,  0,  0,  0,  0,  0,-16,  0,  0,  0,  0,  0,-15,  0,  0, //  24 -  39
		  0,  0,-17,  0,  0,  0,  0,-16,  0,  0,  0,  0,-15,  0,  0,  0, //  40 -  55
		  0,  0,  0,-17,  0,  0,  0,-16,  0,  0,  0,-15,  0,  0,  0,  0, //  56 -  71
		  0,  0,  0,  0,-17,  0,  0,-16,  0,  0,-15,  0,  0,  0,  0,  0, //  72 -  87
		  0,  0,  0,  0,  0,-17,-33,-16,-31,-15,  0,  0,  0,  0,  0,  0, //  88 - 103
		  0,  0,  0,  0,  0,-18,-17,-16,-15,-14,  0,  0,  0,  0,  0,  0, // 104 - 119
		 -1, -1, -1, -1, -1, -1, -1,  0,  1,  1,  1,  1,  1,  1,  1,  0, // 120 - 135
		  0,  0,  0,  0,  0, 14, 15, 16, 17, 18,  0,  0,  0,  0,  0,  0, // 136 - 151
		  0,  0,  0,  0,  0, 15, 31, 16, 33, 17,  0,  0,  0,  0,  0,  0, // 152 - 167
		  0,  0,  0,  0, 15,  0,  0, 16,  0,  0, 17,  0,  0,  0,  0,  0, // 168 - 183
		  0,  0,  0, 15,  0,  0,  0, 16,  0,  0,  0, 17,  0,  0,  0,  0, // 184 - 199
		  0,  0, 15,  0,  0,  0,  0, 16,  0,  0,  0,  0, 17,  0,  0,  0, // 200 - 215
		  0, 15,  0,  0,  0,  0,  0, 16,  0,  0,  0,  0,  0, 17,  0,  0, // 216 - 231
		 15,  0,  0,  0,  0,  0,  0, 16,  0,  0,  0,  0,  0,  0, 17,  0, // 232 - 247
		  0,  0,  0,  0,  0,  0,  0, 0                                   // 248 - 255
	};

}
