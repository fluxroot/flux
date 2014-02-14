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

import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.board.IntColor;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.jcpi.AbstractEngine;
import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericColor;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.options.AbstractOption;
import com.fluxchess.jcpi.protocols.IProtocolHandler;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.table.EvaluationTable;
import com.fluxchess.flux.table.PawnTable;
import com.fluxchess.flux.table.TranspositionTable;

import java.util.Arrays;
import java.util.List;

/**
 * This is the main entry class.
 */
public final class Flux extends AbstractEngine {

	private Hex88Board board = null;
	private TranspositionTable transpositionTable;
	private EvaluationTable evaluationTable;
	private PawnTable pawnTable;
	private final int[] timeTable = new int[ISearch.MAX_HEIGHT + 1];
	private ISearch search;

	/**
	 * Creates a new Flux.
	 * 
	 * @param protocol the protocol.
	 */
	public Flux() {
		// Set the protocol
		ChessLogger.setProtocol(getProtocol());

		initialize();
	}

	public Flux(IProtocolHandler handler) {
		super(handler);

		// Set the protocol
		ChessLogger.setProtocol(handler);

		initialize();
	}
	
	private void initialize() {
		initializeTranspositionTable();
		initializeEvaluationTable();
		initializePawnTable();

		// Create a new search
		this.search = new Search(new Evaluation(this.evaluationTable, this.pawnTable), new Hex88Board(new GenericBoard(GenericBoard.STANDARDSETUP)), this.transpositionTable, new InformationTimer(getProtocol(), this.transpositionTable), this.timeTable);
	}

	private void initializeTranspositionTable() {
		int numberOfEntries = Configuration.transpositionTableSize * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
		transpositionTable = new TranspositionTable(numberOfEntries);

		Runtime.getRuntime().gc();
	}

	private void initializeEvaluationTable() {
		int numberOfEntries = Configuration.evaluationTableSize * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
		evaluationTable = new EvaluationTable(numberOfEntries);

		Runtime.getRuntime().gc();
	}

	private void initializePawnTable() {
		int numberOfEntries = Configuration.pawnTableSize * 1024 * 1024 / PawnTable.ENTRYSIZE;
		pawnTable = new PawnTable(numberOfEntries);

		Runtime.getRuntime().gc();
	}

	/**
	 * The main function.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new Flux().run();
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

		initialize();

		// Send the initialization commands
		ProtocolInitializeAnswerCommand initializeCommand = new ProtocolInitializeAnswerCommand(Configuration.name, Configuration.author);
		for (AbstractOption option : Configuration.options) {
			initializeCommand.addOption(option);
		}
		getProtocol().send(initializeCommand);
	}

	public void receive(EngineReadyRequestCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received ReadyRequest command.");

		// Send a pong back
		getProtocol().send(new ProtocolReadyAnswerCommand(command.token));
	}

	public void receive(EngineDebugCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received Debug command.");

		ProtocolInformationCommand infoCommand = new ProtocolInformationCommand();
		if (command.debug) {
			infoCommand.setString("Turning on debugging mode");
		} else {
			infoCommand.setString("Turning off debugging mode");
		}
		getProtocol().send(infoCommand);
		ChessLogger.setDebug(command.debug);
	}

	public void receive(EngineNewGameCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received New command.");

		// Stop calculating
		new EngineStopCalculatingCommand().accept(this);
		
		// Clear the hash table
		this.transpositionTable.clear();
		
		// Clear time table
		Arrays.fill(this.timeTable, 0);
	}

	public void receive(EngineAnalyzeCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received Analyze command.");

		if (!this.search.isStopped()) {
			this.search.stop();
		}

		// Create a new board
		this.board = new Hex88Board(command.board);
		
		// Make all moves
		List<GenericMove> moveList = command.moves;
		for (GenericMove move : moveList) {
			int newMove = IntMove.convertMove(move, this.board);
			this.board.makeMove(newMove);
		}
	}

	public void receive(EnginePonderHitCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received PonderHit command.");

		if (!this.search.isStopped()) {
			this.search.ponderhit();
		} else {
			ChessLogger.getLogger().debug("There is no search active.");
		}
	}

	public void receive(EngineStartCalculatingCommand command) {
		if (command == null) throw new IllegalArgumentException();
		
		ChessLogger.getLogger().debug("Received StartCalculating command.");

		if (this.board != null) {
			if (this.search.isStopped()) {
				// Create a new search
				this.search = new Search(new Evaluation(this.evaluationTable, this.pawnTable), this.board, this.transpositionTable, new InformationTimer(getProtocol(), this.transpositionTable), this.timeTable);

				// Set all search parameters
				if (command.getDepth() != null && command.getDepth() > 0) {
					this.search.setSearchDepth(command.getDepth());
				}
				if (command.getNodes() != null && command.getNodes() > 0) {
					this.search.setSearchNodes(command.getNodes());
				}
				if (command.getMoveTime() != null && command.getMoveTime() > 0) {
					this.search.setSearchTime(command.getMoveTime());
				}
				for (GenericColor side : GenericColor.values()) {
					if (command.getClock(side) != null && command.getClock(side) > 0) {
						this.search.setSearchClock(IntColor.valueOfColor(side), command.getClock(side));
					}
					if (command.getClockIncrement(side) != null && command.getClockIncrement(side) > 0) {
						this.search.setSearchClockIncrement(IntColor.valueOfColor(side), command.getClockIncrement(side));
					}
				}
				if (command.getMovesToGo() != null && command.getMovesToGo() > 0) {
					this.search.setSearchMovesToGo(command.getMovesToGo());
				}
				if (command.getInfinite()) {
					this.search.setSearchInfinite();
				}
				if (command.getPonder()) {
					this.search.setSearchPonder();
				}
				if (command.getSearchMoveList() != null) {
					this.search.setSearchMoveList(command.getSearchMoveList());
				}

				// Go...
				this.search.start();
				this.board = null;
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

		if (!this.search.isStopped()) {
			// Stop the search
			this.search.stop();
		} else {
			ChessLogger.getLogger().debug("There is no search active.");
		}
	}

	public void receive(EngineSetOptionCommand command) {
		if (command == null) throw new IllegalArgumentException();

		ChessLogger.getLogger().debug("Received SetOption command.");

		if (command.name == null) throw new IllegalArgumentException();

		// ponder
		if (command.name.equalsIgnoreCase(Configuration.ponderOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			Configuration.ponder = Boolean.parseBoolean(command.value);
		}

		// showPvNumber
		else if (command.name.equalsIgnoreCase(Configuration.multiPVOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			try {
				Configuration.showPvNumber = new Integer(command.value);
			} catch (NumberFormatException e) {
				Configuration.showPvNumber = Configuration.defaultShowPvNumber;
			}
		}

		// transpositionTableSize
		else if (command.name.equalsIgnoreCase(Configuration.hashOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			try {
				Configuration.transpositionTableSize = new Integer(command.value);
			} catch (NumberFormatException e) {
				Configuration.transpositionTableSize = Configuration.defaultTranspositionTableSize;
			}
			initializeTranspositionTable();
		}

		// Clear Hash
		else if (command.name.equalsIgnoreCase(Configuration.clearHashOption.name)) {
			transpositionTable.clear();
		}

		// evaluationTableSize
		else if (command.name.equalsIgnoreCase(Configuration.evaluationTableOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			try {
				Configuration.evaluationTableSize = new Integer(command.value);
			} catch (NumberFormatException e) {
				Configuration.evaluationTableSize = Configuration.defaultEvaluationTableSize;
			}
			initializeEvaluationTable();
		}

		// pawnTableSize
		else if (command.name.equalsIgnoreCase(Configuration.pawnTableOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			try {
				Configuration.pawnTableSize = new Integer(command.value);
			} catch (NumberFormatException e) {
				Configuration.pawnTableSize = Configuration.defaultPawnTableSize;
			}
			initializePawnTable();
		}

		// showRefutations
		else if (command.name.equalsIgnoreCase(Configuration.uciShowRefutationsOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			Configuration.showRefutations = Boolean.parseBoolean(command.value);
		}

		// analyzeMode
		else if (command.name.equalsIgnoreCase(Configuration.uciAnalyzeModeOption.name)) {
			if (command.value == null) throw new IllegalArgumentException();

			Configuration.analyzeMode = Boolean.parseBoolean(command.value);
		}
	}

}
