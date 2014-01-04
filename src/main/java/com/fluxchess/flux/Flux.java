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

import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.Move;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.evaluation.EvaluationTable;
import com.fluxchess.flux.evaluation.PawnTable;
import com.fluxchess.flux.search.InformationTimer;
import com.fluxchess.flux.search.Search;
import com.fluxchess.flux.search.TranspositionTable;
import com.fluxchess.jcpi.AbstractEngine;
import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericColor;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IntColor;
import com.fluxchess.jcpi.options.AbstractOption;
import com.fluxchess.jcpi.protocols.IProtocolHandler;

import java.util.Arrays;
import java.util.List;

public final class Flux extends AbstractEngine {

  private Board board = null;
  private TranspositionTable transpositionTable;
  private EvaluationTable evaluationTable;
  private PawnTable pawnTable;
  private final int[] timeTable = new int[Search.MAX_HEIGHT + 1];
  private Search search;

  public static void main(String[] args) {
    new Flux().run();
  }

  public Flux() {
    initialize();
  }

  public Flux(IProtocolHandler handler) {
    super(handler);

    initialize();
  }

  private void initialize() {
    // Set the protocol
    ChessLogger.setProtocol(getProtocol());

    initializeTranspositionTable();
    initializeEvaluationTable();
    initializePawnTable();

    // Create a new search
    search = new Search(new Evaluation(evaluationTable, pawnTable), new Board(new GenericBoard(GenericBoard.STANDARDSETUP)), transpositionTable, new InformationTimer(getProtocol(), transpositionTable), timeTable);
  }

  private void initializeTranspositionTable() {
    ChessLogger.getLogger().debug("Using Transposition Table size of " + Configuration.transpositionTableSize + " MB");
    int numberOfEntries = Configuration.transpositionTableSize * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
    transpositionTable = new TranspositionTable(numberOfEntries);
  }

  private void initializeEvaluationTable() {
    ChessLogger.getLogger().debug("Using Evaluation Table size of " + Configuration.evaluationTableSize + " MB");
    int numberOfEntries = Configuration.evaluationTableSize * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
    evaluationTable = new EvaluationTable(numberOfEntries);
  }

  private void initializePawnTable() {
    ChessLogger.getLogger().debug("Using Pawn Table size of " + Configuration.pawnTableSize + " MB");
    int numberOfEntries = Configuration.pawnTableSize * 1024 * 1024 / PawnTable.ENTRYSIZE;
    pawnTable = new PawnTable(numberOfEntries);
  }

  protected void quit() {
    ChessLogger.getLogger().debug("Received Quit command.");

    // Stop calculating
    new EngineStopCalculatingCommand().accept(this);
  }

  public void receive(EngineInitializeRequestCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received Protocol command.");

    // Stop calculating
    new EngineStopCalculatingCommand().accept(this);

    initializeTranspositionTable();
    initializeEvaluationTable();
    initializePawnTable();

    // Send the initialization commands
    ProtocolInitializeAnswerCommand initializeCommand = new ProtocolInitializeAnswerCommand(VersionInfo.current().toString(), Configuration.author);
    for (AbstractOption option : Configuration.options) {
      initializeCommand.addOption(option);
    }
    getProtocol().send(initializeCommand);
  }

  public void receive(EngineSetOptionCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received SetOption command.");

    if (command.name.equalsIgnoreCase(Configuration.ponderOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      Configuration.ponder = Boolean.parseBoolean(command.value);
    } else if (command.name.equalsIgnoreCase(Configuration.multiPVOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      try {
        Configuration.showPvNumber = new Integer(command.value);
      } catch (NumberFormatException e) {
        Configuration.showPvNumber = 1;
      }
    } else if (command.name.equalsIgnoreCase(Configuration.hashOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      try {
        Configuration.transpositionTableSize = new Integer(command.value);
      } catch (NumberFormatException e) {
        Configuration.transpositionTableSize = 16;
      }
      initializeTranspositionTable();
    } else if (command.name.equalsIgnoreCase(Configuration.clearHashOption.name)) {
      transpositionTable.clear();
    } else if (command.name.equalsIgnoreCase(Configuration.evaluationTableOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      try {
        Configuration.evaluationTableSize = new Integer(command.value);
      } catch (NumberFormatException e) {
        Configuration.evaluationTableSize = 4;
      }
      initializeEvaluationTable();
    } else if (command.name.equalsIgnoreCase(Configuration.pawnTableOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      try {
        Configuration.pawnTableSize = new Integer(command.value);
      } catch (NumberFormatException e) {
        Configuration.pawnTableSize = 4;
      }
      initializePawnTable();
    } else if (command.name.equalsIgnoreCase(Configuration.uciShowRefutationsOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      Configuration.showRefutations = Boolean.parseBoolean(command.value);
    } else if (command.name.equalsIgnoreCase(Configuration.uciAnalyzeModeOption.name)) {
      if (command.value == null) throw new IllegalArgumentException();

      Configuration.analyzeMode = Boolean.parseBoolean(command.value);
    }
  }

  public void receive(EngineDebugCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received Debug command.");

    ProtocolInformationCommand infoCommand = new ProtocolInformationCommand();

    boolean state = ChessLogger.getDebug();
    if (command.toggle) {
      state = !state;
    } else {
      state = command.debug;
    }

    if (state) {
      infoCommand.setString("Turning on debugging mode");
    } else {
      infoCommand.setString("Turning off debugging mode");
    }
    getProtocol().send(infoCommand);

    ChessLogger.setDebug(state);
  }

  public void receive(EngineReadyRequestCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received ReadyRequest command.");

    // Send a pong back
    getProtocol().send(new ProtocolReadyAnswerCommand(command.token));
  }

  public void receive(EngineNewGameCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received New command.");

    // Stop calculating
    new EngineStopCalculatingCommand().accept(this);

    // Clear the hash table
    transpositionTable.clear();

    // Clear time table
    Arrays.fill(timeTable, 0);
  }

  public void receive(EngineAnalyzeCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received Analyze command.");

    if (!search.isStopped()) {
      search.stop();
    }

    // Create a new board
    board = new Board(command.board);

    // Make all moves
    List<GenericMove> moveList = command.moves;
    for (GenericMove move : moveList) {
      int newMove = Move.convertMove(move, board);
      board.makeMove(newMove);
    }
  }

  public void receive(EnginePonderHitCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received PonderHit command.");

    if (!search.isStopped()) {
      search.ponderhit();
    } else {
      ChessLogger.getLogger().debug("There is no search active.");
    }
  }

  public void receive(EngineStartCalculatingCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received StartCalculating command.");

    if (board != null) {
      if (search.isStopped()) {
        // Create a new search
        search = new Search(new Evaluation(evaluationTable, pawnTable), board, transpositionTable, new InformationTimer(getProtocol(), transpositionTable), timeTable);

        // Set all search parameters
        if (command.getDepth() != null && command.getDepth() > 0) {
          search.setSearchDepth(command.getDepth());
        }
        if (command.getNodes() != null && command.getNodes() > 0) {
          search.setSearchNodes(command.getNodes());
        }
        if (command.getMoveTime() != null && command.getMoveTime() > 0) {
          search.setSearchTime(command.getMoveTime());
        }
        for (GenericColor side : GenericColor.values()) {
          if (command.getClock(side) != null && command.getClock(side) > 0) {
            search.setSearchClock(IntColor.valueOf(side), command.getClock(side));
          }
          if (command.getClockIncrement(side) != null && command.getClockIncrement(side) > 0) {
            search.setSearchClockIncrement(IntColor.valueOf(side), command.getClockIncrement(side));
          }
        }
        if (command.getMovesToGo() != null && command.getMovesToGo() > 0) {
          search.setSearchMovesToGo(command.getMovesToGo());
        }
        if (command.getInfinite()) {
          search.setSearchInfinite();
        }
        if (command.getPonder()) {
          search.setSearchPonder();
        }
        if (command.getSearchMoveList() != null) {
          search.setSearchMoveList(command.getSearchMoveList());
        }

        // Go...
        search.start();
        board = null;
      } else {
        ChessLogger.getLogger().debug("There is already a search running.");
      }
    } else {
      ChessLogger.getLogger().debug("Please do a position command first.");
    }
  }

  public void receive(EngineStopCalculatingCommand command) {
    if (command == null) throw new IllegalArgumentException();

    ChessLogger.getLogger().debug("Received StopCalculating command.");

    if (!search.isStopped()) {
      // Stop the search
      search.stop();
    } else {
      ChessLogger.getLogger().debug("There is no search active.");
    }
  }

}
