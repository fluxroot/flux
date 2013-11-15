/*
 * Copyright 2007-2013 the original author or authors.
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

import com.fluxchess.flux.move.IntScore;
import com.fluxchess.flux.move.PrincipalVariation;
import com.fluxchess.flux.table.TranspositionTable;
import com.fluxchess.jcpi.commands.IProtocol;
import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.commands.ProtocolInformationCommand;
import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class InformationTimer {

  private final IProtocol protocol;
  private final TranspositionTable transpositionTable;

  private final Lock readLock;
  private final Lock writeLock;

  // GuiInformationCommand values
  private int currentDepth = 0;
  private int currentMaxDepth = 0;
  private long totalTimeStart = 0;
  private long totalNodes = 0;

  // Used for output status
  private long currentTimeStart = 0;

  // Additional GuiInformationCommand values
  private GenericMove currentMove = null;
  private int currentMoveNumber = 0;

  public InformationTimer(IProtocol protocol, TranspositionTable transpositionTable) {
    assert protocol != null;
    assert transpositionTable != null;

    this.protocol = protocol;
    this.transpositionTable = transpositionTable;

    // Initialize locks
    ReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }

  public void start() {
    writeLock.lock();
    try {
      // Set the current time
      totalTimeStart = System.currentTimeMillis();
      currentTimeStart = totalTimeStart;
    } finally {
      writeLock.unlock();
    }
  }

  public void stop() {
    // Do nothing
  }

  public int getCurrentDepth() {
    readLock.lock();
    try {
      return currentDepth;
    } finally {
      readLock.unlock();
    }
  }

  public int getCurrentMaxDepth() {
    readLock.lock();
    try {
      return currentMaxDepth;
    } finally {
      readLock.unlock();
    }
  }

  public long getTotalTimeStart() {
    readLock.lock();
    try {
      return totalTimeStart;
    } finally {
      readLock.unlock();
    }
  }

  public long getTotalNodes() {
    readLock.lock();
    try {
      return totalNodes;
    } finally {
      readLock.unlock();
    }
  }

  public void incrementTotalNodes() {
    writeLock.lock();
    try {
      totalNodes++;
    } finally {
      writeLock.unlock();
    }
  }

  public void setCurrentDepth(int currentDepth) {
    writeLock.lock();
    try {
      assert currentDepth >= 0;

      this.currentDepth = currentDepth;
      currentMaxDepth = currentDepth;
    } finally {
      writeLock.unlock();
    }
  }

  public void setCurrentMaxDepth(int currentDepth) {
    writeLock.lock();
    try {
      assert currentDepth >= 0;

      if (currentDepth > currentMaxDepth) {
        currentMaxDepth = currentDepth;
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendBestMove(GenericMove bestMove, GenericMove ponderMove) {
    writeLock.lock();
    try {
      protocol.send(new ProtocolBestMoveCommand(bestMove, ponderMove));
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationMove(GenericMove currentMove, int currentMoveNumber) {
    writeLock.lock();
    try {
      assert currentMove != null;
      assert currentMoveNumber >= 0;

      this.currentMove = currentMove;
      this.currentMoveNumber = currentMoveNumber;

      // Safety guard: Reduce output pollution
      long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
      if (currentTimeDelta >= 1000) {
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setCurrentMove(currentMove);
        command.setCurrentMoveNumber(currentMoveNumber);

        protocol.send(command);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationRefutations(List<GenericMove> refutationList) {
    writeLock.lock();
    try {
      assert refutationList != null;

      // Safety guard: Reduce output pollution
      long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
      if (currentTimeDelta >= 1000) {
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setRefutationList(refutationList);

        protocol.send(command);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationDepth() {
    writeLock.lock();
    try {
      // Safety guard: Reduce output pollution
      long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
      if (currentTimeDelta >= 1000) {
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setDepth(currentDepth);
        command.setMaxDepth(currentMaxDepth);

        protocol.send(command);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationStatus() {
    writeLock.lock();
    try {
      long currentTimeDelta = System.currentTimeMillis() - currentTimeStart;
      if (currentTimeDelta >= 1000) {
        // Only output after a delay of 1 second
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setDepth(currentDepth);
        command.setMaxDepth(currentMaxDepth);
        command.setHash(transpositionTable.getPermillUsed());
        command.setNps(getCurrentNps());
        command.setTime(System.currentTimeMillis() - totalTimeStart);
        command.setNodes(totalNodes);

        if (currentMove != null) {
          command.setCurrentMove(currentMove);
          command.setCurrentMoveNumber(currentMoveNumber);
        }

        protocol.send(command);

        currentTimeStart = System.currentTimeMillis();
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationSummary() {
    writeLock.lock();
    try {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setDepth(currentDepth);
      command.setMaxDepth(currentMaxDepth);
      command.setHash(transpositionTable.getPermillUsed());
      command.setNps(getCurrentNps());
      command.setTime(System.currentTimeMillis() - totalTimeStart);
      command.setNodes(totalNodes);

      protocol.send(command);

      currentTimeStart = System.currentTimeMillis();
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationCentipawns(PrincipalVariation pv, int pvNumber) {
    writeLock.lock();
    try {
      assert pv != null;
      assert pvNumber >= 1;

      if (pvNumber <= Configuration.showPvNumber) {
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setDepth(pv.depth);
        command.setMaxDepth(pv.maxDepth);
        command.setHash(pv.hash);
        command.setNps(pv.nps);
        command.setTime(pv.time);
        command.setNodes(pv.totalNodes);

        command.setCentipawns(pv.value);
        command.setValue(IntScore.valueOfIntScore(pv.type));
        command.setMoveList(pv.pv);

        if (Configuration.showPvNumber > 1) {
          command.setPvNumber(pvNumber);
        }

        protocol.send(command);

        currentTimeStart = System.currentTimeMillis();
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void sendInformationMate(PrincipalVariation pv, int currentMateDepth, int pvNumber) {
    writeLock.lock();
    try {
      assert pv != null;
      assert pvNumber >= 1;

      if (pvNumber <= Configuration.showPvNumber) {
        ProtocolInformationCommand command = new ProtocolInformationCommand();

        command.setDepth(pv.depth);
        command.setMaxDepth(pv.maxDepth);
        command.setHash(pv.hash);
        command.setNps(pv.nps);
        command.setTime(pv.time);
        command.setNodes(pv.totalNodes);

        command.setMate(currentMateDepth);
        command.setValue(IntScore.valueOfIntScore(pv.type));
        command.setMoveList(pv.pv);

        if (Configuration.showPvNumber > 1) {
          command.setPvNumber(pvNumber);
        }

        protocol.send(command);

        currentTimeStart = System.currentTimeMillis();
      }
    } finally {
      writeLock.unlock();
    }
  }

  public long getCurrentNps() {
    writeLock.lock();
    try {
      long currentNps = 0;
      long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
      if (currentTimeDelta >= 1000) {
        currentNps = (totalNodes * 1000) / currentTimeDelta;
      }

      return currentNps;
    } finally {
      writeLock.unlock();
    }
  }

}
