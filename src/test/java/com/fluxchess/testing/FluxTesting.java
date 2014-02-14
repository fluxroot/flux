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
package com.fluxchess.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jcpi.AbstractCommunication;
import jcpi.AbstractEngine;
import jcpi.ICommunication;
import jcpi.commands.EngineAnalyzeCommand;
import jcpi.commands.EngineDebugCommand;
import jcpi.commands.EngineInitializeRequestCommand;
import jcpi.commands.EngineNewGameCommand;
import jcpi.commands.EngineQuitCommand;
import jcpi.commands.EngineReadyRequestCommand;
import jcpi.commands.EngineStartCalculatingCommand;
import jcpi.commands.EngineStopCalculatingCommand;
import jcpi.commands.GuiBestMoveCommand;
import jcpi.commands.GuiInformationCommand;
import jcpi.commands.GuiInitializeAnswerCommand;
import jcpi.commands.GuiReadyAnswerCommand;
import jcpi.commands.IEngineCommand;
import jcpi.commands.IGuiCommand;
import jcpi.data.GenericBoard;
import jcpi.data.GenericMove;

import com.fluxchess.Flux;

/**
 * FluxTesting
 *
 * @author Phokham Nonava
 */
public class FluxTesting extends AbstractCommunication implements ICommunication {

	BlockingQueue<IEngineCommand> commandQueue = new LinkedBlockingQueue<IEngineCommand>();
	List<GenericMove> moveList = new ArrayList<GenericMove>();
	
	public FluxTesting() {
		this.commandQueue.add(new EngineInitializeRequestCommand());
		this.commandQueue.add(new EngineDebugCommand(false, true));
		this.commandQueue.add(new EngineReadyRequestCommand("test"));
		this.commandQueue.add(new EngineNewGameCommand());
		this.commandQueue.add(new EngineAnalyzeCommand(new GenericBoard(GenericBoard.STANDARDSETUP), this.moveList));
		EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
		startCommand.setMoveTime(5000L);
		this.commandQueue.add(startCommand);
	}

	public static void main(String[] args) {
		FluxTesting testing = new FluxTesting();
		AbstractEngine engine = new Flux(testing);
		engine.run();
	}

	public void send(IGuiCommand command) {
		command.accept(this);
	}

	public IEngineCommand receive() {
		IEngineCommand command = null;
		try {
			command = this.commandQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assert command != null;
		
		System.out.println(command);

		return command;
	}

	public void visit(GuiInitializeAnswerCommand command) {
		System.out.println(command);
	}

	public void visit(GuiReadyAnswerCommand command) {
		System.out.println(command);
	}

	public void visit(GuiBestMoveCommand command) {
		System.out.println(command);
		if (command.bestMove != null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.moveList.add(command.bestMove);
			this.commandQueue.add(new EngineStopCalculatingCommand());
			this.commandQueue.add(new EngineAnalyzeCommand(new GenericBoard(GenericBoard.STANDARDSETUP), this.moveList));
			EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
			startCommand.setMoveTime(5000L);
			this.commandQueue.add(startCommand);
		} else {
			this.commandQueue.add(new EngineQuitCommand());
		}
	}

	public void visit(GuiInformationCommand command) {
		System.out.println(command);
	}

	public String toString() {
		return "FluxTesting Protocol";
	}

}
