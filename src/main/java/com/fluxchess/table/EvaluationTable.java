/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.table;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * EvaluationTable
 *
 * @author Phokham Nonava
 */
public final class EvaluationTable {

	// Size of one evaluation entry
	public static final int ENTRYSIZE = 28;

	private final int size;
	
	private final EvaluationTableEntry[] entry;
	
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;
	
	public EvaluationTable(int newSize) {
		assert newSize >= 1;

		this.size = newSize;

		// Initialize entry
		this.entry = new EvaluationTableEntry[newSize];
		for (int i = 0; i < this.entry.length; i++) {
			this.entry[i] = new EvaluationTableEntry();
		}
		
		// Initialize locks
		this.lock = new ReentrantReadWriteLock();
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();
	}

	/**
	 * Puts a zobrist code and evaluation value into the table.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @param newEvaluation the evaluation value.
	 */
	public void put(long newZobristCode, int newEvaluation) {
		int position = (int) (newZobristCode % this.size);
		
		this.writeLock.lock();
		try {
			EvaluationTableEntry currentEntry = this.entry[position];

			currentEntry.zobristCode = newZobristCode;
			currentEntry.evaluation = newEvaluation;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Returns the evaluation table entry given the zobrist code.
	 * 
	 * @param newZobristCode the zobrist code.
	 * @return the evaluation table entry or null if there exists no entry.
	 */
	public EvaluationTableEntry get(long newZobristCode) {
		int position = (int) (newZobristCode % this.size);
		
		this.readLock.lock();
		try {
			EvaluationTableEntry currentEntry = this.entry[position];

			if (currentEntry.zobristCode == newZobristCode) {
				return currentEntry;
			} else {
				return null;
			}
		} finally {
			this.readLock.unlock();
		}
	}
	
}
