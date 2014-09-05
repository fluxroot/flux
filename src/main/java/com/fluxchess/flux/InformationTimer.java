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
package com.fluxchess.flux;

import com.fluxchess.jcpi.commands.IProtocol;
import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.commands.ProtocolInformationCommand;
import com.fluxchess.jcpi.models.GenericMove;

import java.util.List;

final class InformationTimer {

  private final IProtocol protocol;
  private final TranspositionTable transpositionTable;
  private Search search = null;

  // InfoCommand values
  int currentDepth = 0;
  int currentMaxDepth = 0;
  long totalTimeStart = 0;
  long totalNodes = 0;

  // Used for output status
  private long currentTimeStart = 0;

  // Additional InfoCommand values
  private GenericMove currentMove = null;
  private int currentMoveNumber = 0;

  /**
   * Creates a new InformationTimer
   *
   * @param protocol the protocol.
   */
  InformationTimer(IProtocol protocol, TranspositionTable transpositionTable) {
    assert protocol != null;
    assert transpositionTable != null;

    this.protocol = protocol;
    this.transpositionTable = transpositionTable;
  }

  /**
   * Sets the search.
   *
   * @param search the search.
   */
  void setSearch(Search search) {
    this.search = search;
  }

  /**
   * Starts the InformationTimer.
   */
  void start() {
    if (this.search == null) throw new IllegalStateException();

    // Set the current time
    this.totalTimeStart = System.currentTimeMillis();
    this.currentTimeStart = this.totalTimeStart;
  }

  /**
   * Stops the InformationTimer.
   */
  void stop() {
    // Do nothing
  }

  /**
   * Sets the current depth.
   *
   * @param currentDepth the current depth.
   */
  void setCurrentDepth(int currentDepth) {
    assert currentDepth >= 0;

    this.currentDepth = currentDepth;
    this.currentMaxDepth = currentDepth;
  }

  /**
   * Sets the current maximum reached depth.
   *
   * @param currentDepth the current depth.
   */
  void setCurrentMaxDepth(int currentDepth) {
    assert currentDepth >= 0;

    if (currentDepth > this.currentMaxDepth) {
      this.currentMaxDepth = currentDepth;
    }
  }

  /**
   * Sends the best move and ponder move.
   *
   * @param bestMove   the best move or null if there's no best move.
   * @param ponderMove the ponder move or null if there's no ponder move.
   */
  void sendBestMove(GenericMove bestMove, GenericMove ponderMove) {
    this.protocol.send(new ProtocolBestMoveCommand(bestMove, ponderMove));
  }

  /**
   * Sends the current move and current move number.
   *
   * @param currentMove       the current move.
   * @param currentMoveNumber the current move number.
   */
  void sendInformationMove(GenericMove currentMove, int currentMoveNumber) {
    assert currentMove != null;
    assert currentMoveNumber >= 0;

    this.currentMove = currentMove;
    this.currentMoveNumber = currentMoveNumber;

    // Safety guard: Reduce output pollution
    long currentTimeDelta = System.currentTimeMillis() - this.totalTimeStart;
    if (currentTimeDelta >= 1000) {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setCurrentMove(this.currentMove);
      command.setCurrentMoveNumber(this.currentMoveNumber);

      this.protocol.send(command);
    }
  }

  /**
   * Sends the refutations information.
   *
   * @param refutationList the current refutation move list.
   */
  void sendInformationRefutations(List<GenericMove> refutationList) {
    assert refutationList != null;

    // Safety guard: Reduce output pollution
    long currentTimeDelta = System.currentTimeMillis() - this.totalTimeStart;
    if (currentTimeDelta >= 1000) {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setRefutationList(refutationList);

      this.protocol.send(command);
    }
  }

  /**
   * Sends the current depth.
   */
  void sendInformationDepth() {
    // Safety guard: Reduce output pollution
    long currentTimeDelta = System.currentTimeMillis() - this.totalTimeStart;
    if (currentTimeDelta >= 1000) {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setDepth(this.currentDepth);
      command.setMaxDepth(this.currentMaxDepth);

      this.protocol.send(command);
    }
  }

  /**
   * Sends the current status.
   */
  void sendInformationStatus() {
    long currentTimeDelta = System.currentTimeMillis() - this.currentTimeStart;
    if (currentTimeDelta >= 1000) {
      // Only output after a delay of 1 second
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setDepth(this.currentDepth);
      command.setMaxDepth(this.currentMaxDepth);
      command.setHash(this.transpositionTable.getPermillUsed());
      command.setNps(getCurrentNps());
      command.setTime(System.currentTimeMillis() - this.totalTimeStart);
      command.setNodes(this.totalNodes);

      if (this.currentMove != null) {
        command.setCurrentMove(this.currentMove);
        command.setCurrentMoveNumber(this.currentMoveNumber);
      }

      this.protocol.send(command);

      this.currentTimeStart = System.currentTimeMillis();
    }
  }

  /**
   * Sends the current status.
   */
  void sendInformationSummary() {
    ProtocolInformationCommand command = new ProtocolInformationCommand();

    command.setDepth(this.currentDepth);
    command.setMaxDepth(this.currentMaxDepth);
    command.setHash(this.transpositionTable.getPermillUsed());
    command.setNps(getCurrentNps());
    command.setTime(System.currentTimeMillis() - this.totalTimeStart);
    command.setNodes(this.totalNodes);

    this.protocol.send(command);

    this.currentTimeStart = System.currentTimeMillis();
  }

  /**
   * Sends the centipawn information.
   */
  void sendInformationCentipawns(PrincipalVariation pv, int pvNumber) {
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
      command.setValue(Bound.valueOfIntValue(pv.type));
      command.setMoveList(pv.pv);

      if (Configuration.showPvNumber > 1) {
        command.setPvNumber(pvNumber);
      }

      this.protocol.send(command);

      this.currentTimeStart = System.currentTimeMillis();
    }
  }

  /**
   * Sends the mate information.
   *
   * @param currentMateDepth the current mate depth.
   */
  void sendInformationMate(PrincipalVariation pv, int currentMateDepth, int pvNumber) {
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
      command.setValue(Bound.valueOfIntValue(pv.type));
      command.setMoveList(pv.pv);

      if (Configuration.showPvNumber > 1) {
        command.setPvNumber(pvNumber);
      }

      this.protocol.send(command);

      this.currentTimeStart = System.currentTimeMillis();
    }
  }

  /**
   * Returns the current nps.
   *
   * @return the current nps.
   */
  long getCurrentNps() {
    long currentNps = 0;
    long currentTimeDelta = System.currentTimeMillis() - this.totalTimeStart;
    if (currentTimeDelta >= 1000) {
      currentNps = (this.totalNodes * 1000) / currentTimeDelta;
    }

    return currentNps;
  }

}
