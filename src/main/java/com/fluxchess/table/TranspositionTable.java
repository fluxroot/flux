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
package com.fluxchess.table;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import com.fluxchess.board.Hex88Board;
import com.fluxchess.jcpi.data.GenericMove;
import com.fluxchess.move.IntMove;
import com.fluxchess.move.IntScore;

/**
 * TranspositionTable
 *
 * @author Phokham Nonava
 */
public final class TranspositionTable {

    // Size of one transposition entry
    public static final int ENTRYSIZE = 44;

    private final int size;

    // Entry
    private final TranspositionTableEntry[] entry;

    // Age
    private int currentAge = 0;

    // Number of slots used
    private int slotsUsed = 0;

    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    /**
     * Creates a new TranspositionTable.
     *
     * @param size the size.
     */
    public TranspositionTable(int size) {
        assert size >= 1;

        this.size = size;

        // Initialize entry
        entry = new TranspositionTableEntry[size];
        for (int i = 0; i < entry.length; i++) {
            entry[i] = new TranspositionTableEntry();
        }

        currentAge = 0;
        slotsUsed = 0;

        // Initialize locks
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    /**
     * Clears the Transposition Table.
     */
    public void clear() {
        writeLock.lock();
        try {
            currentAge = 0;
            slotsUsed = 0;

            for (int i = 0; i < entry.length; i++) {
                entry[i].clear();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Increase the age of the Transposition Table.
     */
    public void increaseAge() {
        writeLock.lock();
        try {
            currentAge++;
            slotsUsed = 0;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Puts the values into the TranspositionTable.
     *
     * @param zobristCode the zobrist code.
     * @param depth the depth.
     * @param value the value.
     * @param type the value type.
     * @param move the move.
     */
    public void put(long zobristCode, int depth, int value, int type, int move, boolean mateThreat, int height) {
        assert depth >= 0;
        assert type != IntScore.NOSCORE;
        assert height >= 0;

        int position = (int) (zobristCode % size);

        writeLock.lock();
        try {
            TranspositionTableEntry currentEntry = entry[position];

            //## BEGIN "always replace" Scheme
            if (currentEntry.zobristCode == 0 || currentEntry.age != currentAge) {
                // This is a new entry
                slotsUsed++;
                currentEntry.zobristCode = zobristCode;
                currentEntry.age = currentAge;
                currentEntry.depth = depth;
                currentEntry.setValue(value, height);
                currentEntry.type = type;
                currentEntry.move = move;
                currentEntry.mateThreat = mateThreat;
            } else if (currentEntry.zobristCode == zobristCode) {
                // The same zobrist key already exists
                if (depth >= currentEntry.depth && move != IntMove.NOMOVE) {
                    currentEntry.depth = depth;
                    currentEntry.setValue(value, height);
                    currentEntry.type = type;
                    currentEntry.move = move;
                    currentEntry.mateThreat = mateThreat;
                }
            } else {
                // We have a collision. Overwrite existing entry
                currentEntry.zobristCode = zobristCode;
                currentEntry.depth = depth;
                currentEntry.setValue(value, height);
                currentEntry.type = type;
                currentEntry.move = move;
                currentEntry.mateThreat = mateThreat;
            }
            //## ENDOF "always replace" Scheme
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the transposition table entry given the zobrist code.
     *
     * @param zobristCode the zobrist code.
     * @return the transposition table entry or null if there exists no entry.
     */
    public TranspositionTableEntry get(long zobristCode) {
        int position = (int) (zobristCode % size);

        readLock.lock();
        try {
            TranspositionTableEntry currentEntry = entry[position];

            if (currentEntry.zobristCode == zobristCode && currentEntry.age == currentAge) {
                return currentEntry;
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the move list.
     *
     * @param board the board.
     * @param depth the maximum depth.
     * @param moveList the move list.
     * @return the move list.
     */
    public List<GenericMove> getMoveList(Hex88Board board, int depth, List<GenericMove> moveList) {
        assert board != null;
        assert depth >= 0;
        assert moveList != null;

        readLock.lock();
        try {
            TranspositionTableEntry currentEntry = get(board.zobristCode);

            if (currentEntry == null
                    || depth == 0
                    || currentEntry.move == IntMove.NOMOVE) {
                return moveList;
            } else {
                moveList.add(IntMove.toGenericMove(currentEntry.move));

                board.makeMove(currentEntry.move);
                List<GenericMove> newMoveList = getMoveList(board, depth - 1, moveList);
                board.undoMove(currentEntry.move);

                return newMoveList;
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the permill of slots used.
     *
     * @return the permill of slots used.
     */
    public int getPermillUsed() {
        readLock.lock();
        try {
            return (int) ((1000L * (long) slotsUsed) / (long) size);
        } finally {
            readLock.unlock();
        }
    }

}
