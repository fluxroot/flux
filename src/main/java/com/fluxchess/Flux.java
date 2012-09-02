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
package com.fluxchess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jcpi.AbstractCommunication;
import jcpi.AbstractEngine;
import jcpi.commands.EngineAnalyzeCommand;
import jcpi.commands.EngineDebugCommand;
import jcpi.commands.EngineInitializeRequestCommand;
import jcpi.commands.EngineNewGameCommand;
import jcpi.commands.EnginePonderHitCommand;
import jcpi.commands.EngineReadyRequestCommand;
import jcpi.commands.EngineSetOptionCommand;
import jcpi.commands.EngineStartCalculatingCommand;
import jcpi.commands.EngineStopCalculatingCommand;
import jcpi.commands.GuiInformationCommand;
import jcpi.commands.GuiInitializeAnswerCommand;
import jcpi.commands.GuiReadyAnswerCommand;
import jcpi.data.GenericBoard;
import jcpi.data.GenericColor;
import jcpi.data.GenericMove;
import jcpi.data.Option;
import jcpi.standardio.StandardIoCommunication;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntColor;
import com.fluxchess.evaluation.Evaluation;
import com.fluxchess.move.IntMove;
import com.fluxchess.table.EvaluationTable;
import com.fluxchess.table.PawnTable;
import com.fluxchess.table.TranspositionTable;

/**
 * This is the main entry class.
 *
 * @author Phokham Nonava
 */
public final class Flux extends AbstractEngine {

	private Hex88Board board = null;
	private TranspositionTable transpositionTable;
	private EvaluationTable evaluationTable;
	private PawnTable pawnTable;
	private final int[] timeTable = new int[Search.MAX_HEIGHT + 1];
	private Search search;

	/**
	 * Creates a new Flux.
	 * 
	 * @param communication the AbstractCommunication.
	 */
	public Flux(AbstractCommunication communication) {
		super(communication);

		// Set the protocol
		ChessLogger.setProtocol(communication);

		// Transposition Table
		int megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_Hash)).defaultValue);;
		int numberOfEntries = megabyteValue * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
		transpositionTable = new TranspositionTable(numberOfEntries);
		
		// Evaluation Table
		megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashEvaluation)).defaultValue);
		numberOfEntries = megabyteValue * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
		evaluationTable = new EvaluationTable(numberOfEntries);

		// Pawn Table
		megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashPawn)).defaultValue);
		numberOfEntries = megabyteValue * 1024 * 1024 / PawnTable.ENTRYSIZE;
		pawnTable = new PawnTable(numberOfEntries);

		// Run GC
		Runtime.getRuntime().gc();

		// Create a new search
		search = new Search(new Evaluation(evaluationTable, pawnTable), new Hex88Board(new GenericBoard(GenericBoard.STANDARDSETUP)), transpositionTable, new InformationTimer(communication, transpositionTable), timeTable);
	}
	
	/**
	 * The main function.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		AbstractEngine engine = new Flux(new StandardIoCommunication());
		engine.run();
	}

	public void visit(EngineInitializeRequestCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received Protocol command.");
		
		// Stop calculating
		new EngineStopCalculatingCommand().accept(this);

		// Load the configuration
		Configuration.loadConfiguration();

		// Transposition Table
		int megabyteValue;
		try {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_Hash)).getValue());
		} catch (NumberFormatException e) {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_Hash)).defaultValue);;
		}
		int numberOfEntries = megabyteValue * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
		transpositionTable = new TranspositionTable(numberOfEntries);
		
		// Evaluation Table
		try {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashEvaluation)).getValue());;
		} catch (NumberFormatException e) {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashEvaluation)).defaultValue);
		}
		numberOfEntries = megabyteValue * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
		evaluationTable = new EvaluationTable(numberOfEntries);

		// Pawn Table
		try {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashPawn)).getValue());;
		} catch (NumberFormatException e) {
			megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashPawn)).defaultValue);
		}
		numberOfEntries = megabyteValue * 1024 * 1024 / PawnTable.ENTRYSIZE;
		pawnTable = new PawnTable(numberOfEntries);

		// Run GC
		Runtime.getRuntime().gc();
		
		// Send the initialization commands
		GuiInitializeAnswerCommand initializeCommand = new GuiInitializeAnswerCommand(Configuration.name, Configuration.author);
		for (Iterator<Option> iter = Configuration.configuration.values().iterator(); iter.hasNext();) {
			Option option = iter.next();
			initializeCommand.addOption(option);
		}
		communication.send(initializeCommand);
	}

	protected void quit() {
		ChessLogger.getLogger().debug("Received Quit command.");

		// Stop calculating
		new EngineStopCalculatingCommand().accept(this);
	}

	public void visit(EngineReadyRequestCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received ReadyRequest command.");

		// Send a pong back
		communication.send(new GuiReadyAnswerCommand(command.token));
	}

	public void visit(EngineDebugCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received Debug command.");

		GuiInformationCommand infoCommand = new GuiInformationCommand();

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
		communication.send(infoCommand);

		ChessLogger.setDebug(state);
	}

	public void visit(EngineNewGameCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received New command.");

		// Stop calculating
		new EngineStopCalculatingCommand().accept(this);
		
		// Clear the hash table
		transpositionTable.clear();
		
		// Clear time table
		Arrays.fill(timeTable, 0);
	}

	public void visit(EngineAnalyzeCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received Analyze command.");

		if (!search.isStopped()) {
			search.stop();
		}

		// Create a new board
		board = new Hex88Board(command.board);
		
		// Make all moves
		List<GenericMove> moveList = command.moveList;
		for (GenericMove move : moveList) {
			int newMove = IntMove.convertMove(move, board);
			board.makeMove(newMove);
		}
	}

	public void visit(EnginePonderHitCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received PonderHit command.");

		if (!search.isStopped()) {
			search.ponderhit();
		} else {
			ChessLogger.getLogger().debug("There is no search active.");
		}
	}

	public void visit(EngineStartCalculatingCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received StartCalculating command.");

		if (board != null) {
			if (search.isStopped()) {
				// Create a new search
				search = new Search(new Evaluation(evaluationTable, pawnTable), board, transpositionTable, new InformationTimer(communication, transpositionTable), timeTable);

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
						search.setSearchClock(IntColor.valueOfColor(side), command.getClock(side));
					}
					if (command.getClockIncrement(side) != null && command.getClockIncrement(side) > 0) {
						search.setSearchClockIncrement(IntColor.valueOfColor(side), command.getClockIncrement(side));
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

	public void visit(EngineStopCalculatingCommand command) {
		assert command != null;
		
		ChessLogger.getLogger().debug("Received StopCalculating command.");

		if (!search.isStopped()) {
			// Stop the search
			search.stop();
		} else {
			ChessLogger.getLogger().debug("There is no search active.");
		}
	}

	public void visit(EngineSetOptionCommand command) {
		assert command != null;

		Configuration.setOption(command.name, command.value);

		ChessLogger.getLogger().debug("Received SetOption command.");

		if (command.name.equalsIgnoreCase(Configuration.KEY_ClearHash)) {
			// Clear our hash table
			transpositionTable.clear();
		} else if (command.name.equalsIgnoreCase(Configuration.KEY_Hash)) {
			// Set the new size of the hash table
			int megabyteValue;
			try {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_Hash)).getValue());
			} catch (NumberFormatException e) {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_Hash)).defaultValue);;
				ChessLogger.getLogger().debug(e.getMessage());
			}
			ChessLogger.getLogger().debug("Using Transposition Table size of " + megabyteValue + " MB");
			int numberOfEntries = megabyteValue * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
			transpositionTable = new TranspositionTable(numberOfEntries);

			// Run GC
			Runtime.getRuntime().gc();
		} else if (command.name.equalsIgnoreCase(Configuration.KEY_HashEvaluation)) {
			// Set the new size of the evaluation hash table
			int megabyteValue;
			try {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashEvaluation)).getValue());;
			} catch (NumberFormatException e) {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashEvaluation)).defaultValue);
				ChessLogger.getLogger().debug(e.getMessage());
			}
			ChessLogger.getLogger().debug("Using Evaluation Table size of " + megabyteValue + " MB");
			int numberOfEntries = megabyteValue * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
			evaluationTable = new EvaluationTable(numberOfEntries);

			// Run GC
			Runtime.getRuntime().gc();
		} else if (command.name.equalsIgnoreCase(Configuration.KEY_HashPawn)) {
			// Set the new size of the pawn hash table
			int megabyteValue;
			try {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashPawn)).getValue());;
			} catch (NumberFormatException e) {
				megabyteValue = Integer.parseInt(((Option) Configuration.configuration.get(Configuration.KEY_HashPawn)).defaultValue);
				ChessLogger.getLogger().debug(e.getMessage());
			}
			ChessLogger.getLogger().debug("Using Pawn Table size of " + megabyteValue + " MB");
			int numberOfEntries = megabyteValue * 1024 * 1024 / PawnTable.ENTRYSIZE;
			pawnTable = new PawnTable(numberOfEntries);

			// Run GC
			Runtime.getRuntime().gc();
		}
	}

}
