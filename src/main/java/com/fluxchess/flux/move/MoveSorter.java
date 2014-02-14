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
package com.fluxchess.flux.move;

/**
 * We do the sorting with an insertion sort algorithm.
 * 
 * Notes: Ideas from http://www.cs.ubc.ca/~harrison/Java/sorting-demo.html
 */
public final class MoveSorter {

	/**
	 * Creates a new MoveSorter.
	 */
	public MoveSorter() {
	}

	/**
	 * Sorts the MoveList using insertion sort.
	 * 
	 * @param list the MoveList.
	 */
	public static void sort(MoveList list) {
		insertionsort(list, list.head, list.tail - 1);
	}

	/**
	 * This is an implementation of the insertion sort.
	 * 
	 * Note: Here insertionsort sorts the list in descending order!
	 * 
	 * @param list the MoveList.
	 * @param left the left/lower index.
	 * @param right the right/higher index.
	 */
	private static void insertionsort(MoveList list, int left, int right) {
		int i;
		int j;
		int move;
		int value;

		for (i = left + 1; i <= right; i++) {
			move = list.move[i];
			value = list.value[i];
			j = i;
			while ((j > left) && (list.value[j - 1] < value)) {
				list.move[j] = list.move[j - 1];
				list.value[j] = list.value[j - 1];
				j--;
			}
			list.move[j] = move;
			list.value[j] = value;
		}
	}

}
