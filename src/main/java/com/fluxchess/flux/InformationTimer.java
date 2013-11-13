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

/**
 * InformationTimer
 */
public final class InformationTimer {

  private final IProtocol protocol;
  private final TranspositionTable transpositionTable;
  private Search search = null;

  // GuiInformationCommand values
  public int currentDepth = 0;
  public int currentMaxDepth = 0;
  public long totalTimeStart = 0;
  public long totalNodes = 0;

  // Used for output status
  private long currentTimeStart = 0;

  // Additional GuiInformationCommand values
  private GenericMove currentMove = null;
  private int currentMoveNumber = 0;

  /**
   * Creates a new InformationTimer
   *
   * @param protocol the protocol.
   */
  public InformationTimer(IProtocol protocol, TranspositionTable transpositionTable) {
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
  public void setSearch(Search search) {
    this.search = search;
  }

  /**
   * Starts the InformationTimer.
   */
  public void start() {
    if (search == null) throw new IllegalStateException();

    // Set the current time
    totalTimeStart = System.currentTimeMillis();
    currentTimeStart = totalTimeStart;
  }

  /**
   * Stops the InformationTimer.
   */
  public void stop() {
    // Do nothing
  }

  /**
   * Sets the current depth.
   *
   * @param currentDepth the current depth.
   */
  public void setCurrentDepth(int currentDepth) {
    assert currentDepth >= 0;

    this.currentDepth = currentDepth;
    currentMaxDepth = currentDepth;
  }

  /**
   * Sets the current maximum reached depth.
   *
   * @param currentDepth the current depth.
   */
  public void setCurrentMaxDepth(int currentDepth) {
    assert currentDepth >= 0;

    if (currentDepth > currentMaxDepth) {
      currentMaxDepth = currentDepth;
    }
  }

  /**
   * Sends the best move and ponder move.
   *
   * @param bestMove   the best move or null if there's no best move.
   * @param ponderMove the ponder move or null if there's no ponder move.
   */
  public void sendBestMove(GenericMove bestMove, GenericMove ponderMove) {
    protocol.send(new ProtocolBestMoveCommand(bestMove, ponderMove));
  }

  /**
   * Sends the current move and current move number.
   *
   * @param currentMove       the current move.
   * @param currentMoveNumber the current move number.
   */
  public void sendInformationMove(GenericMove currentMove, int currentMoveNumber) {
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
  }

  /**
   * Sends the refutations information.
   *
   * @param refutationList the current refutation move list.
   */
  public void sendInformationRefutations(List<GenericMove> refutationList) {
    assert refutationList != null;

    // Safety guard: Reduce output pollution
    long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
    if (currentTimeDelta >= 1000) {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setRefutationList(refutationList);

      protocol.send(command);
    }
  }

  /**
   * Sends the current depth.
   */
  public void sendInformationDepth() {
    // Safety guard: Reduce output pollution
    long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
    if (currentTimeDelta >= 1000) {
      ProtocolInformationCommand command = new ProtocolInformationCommand();

      command.setDepth(currentDepth);
      command.setMaxDepth(currentMaxDepth);

      protocol.send(command);
    }
  }

  /**
   * Sends the current status.
   */
  public void sendInformationStatus() {
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
  }

  /**
   * Sends the current status.
   */
  public void sendInformationSummary() {
    ProtocolInformationCommand command = new ProtocolInformationCommand();

    command.setDepth(currentDepth);
    command.setMaxDepth(currentMaxDepth);
    command.setHash(transpositionTable.getPermillUsed());
    command.setNps(getCurrentNps());
    command.setTime(System.currentTimeMillis() - totalTimeStart);
    command.setNodes(totalNodes);

    protocol.send(command);

    currentTimeStart = System.currentTimeMillis();
  }

  /**
   * Sends the centipawn information.
   *
   * @param currentCentipawns the current centipawn value.
   * @param currentMoveList   the current move list/principal variation.
   */
  public void sendInformationCentipawns(PrincipalVariation pv, int pvNumber) {
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
  }

  /**
   * Sends the mate information.
   *
   * @param currentMateDepth the current mate depth.
   * @param currentMoveList  the current move list/principal variation.
   */
  public void sendInformationMate(PrincipalVariation pv, int currentMateDepth, int pvNumber) {
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
  }

  /**
   * Returns the current nps.
   *
   * @return the current nps.
   */
  public long getCurrentNps() {
    long currentNps = 0;
    long currentTimeDelta = System.currentTimeMillis() - totalTimeStart;
    if (currentTimeDelta >= 1000) {
      currentNps = (totalNodes * 1000) / currentTimeDelta;
    }

    return currentNps;
  }

}
