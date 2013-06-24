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
package com.fluxchess.flux.table;

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

    public EvaluationTable(int size) {
        assert size >= 1;

        this.size = size;

        // Initialize entry
        entry = new EvaluationTableEntry[size];
        for (int i = 0; i < entry.length; i++) {
            entry[i] = new EvaluationTableEntry();
        }

        // Initialize locks
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    /**
     * Puts a zobrist code and evaluation value into the table.
     *
     * @param zobristCode the zobrist code.
     * @param value the evaluation value.
     */
    public void put(long zobristCode, int value) {
        int position = (int) (zobristCode % size);

        writeLock.lock();
        try {
            EvaluationTableEntry currentEntry = entry[position];

            currentEntry.zobristCode = zobristCode;
            currentEntry.evaluation = value;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the evaluation table entry given the zobrist code.
     *
     * @param zobristCode the zobrist code.
     * @return the evaluation table entry or null if there exists no entry.
     */
    public EvaluationTableEntry get(long zobristCode) {
        int position = (int) (zobristCode % size);

        readLock.lock();
        try {
            EvaluationTableEntry currentEntry = entry[position];

            if (currentEntry.zobristCode == zobristCode) {
                return currentEntry;
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

}
