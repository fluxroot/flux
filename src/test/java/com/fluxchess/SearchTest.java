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
package com.fluxchess;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jcpi.AbstractCommunication;
import jcpi.AbstractEngine;
import jcpi.ICommunication;
import jcpi.commands.EngineAnalyzeCommand;
import jcpi.commands.EngineInitializeRequestCommand;
import jcpi.commands.EngineNewGameCommand;
import jcpi.commands.EngineQuitCommand;
import jcpi.commands.EngineStartCalculatingCommand;
import jcpi.commands.GuiBestMoveCommand;
import jcpi.commands.GuiInformationCommand;
import jcpi.commands.GuiInitializeAnswerCommand;
import jcpi.commands.GuiReadyAnswerCommand;
import jcpi.commands.IEngineCommand;
import jcpi.commands.IGuiCommand;
import jcpi.data.GenericBoard;
import jcpi.data.GenericMove;
import jcpi.data.IllegalNotationException;

import org.junit.Test;

import com.fluxchess.Flux;

/**
 * SearchTest
 *
 * @author Phokham Nonava
 */
public class SearchTest extends AbstractCommunication implements ICommunication {

	BlockingQueue<IEngineCommand> commandQueue = new LinkedBlockingQueue<IEngineCommand>();
	boolean found = false;
	
	public SearchTest() {
		try {
			this.commandQueue.add(new EngineInitializeRequestCommand());
			this.commandQueue.add(new EngineNewGameCommand());
			this.commandQueue.add(new EngineAnalyzeCommand(new GenericBoard("5n2/B3K3/2p2Np1/4k3/7P/3bN1P1/2Prn1P1/1q6 w - -"), new ArrayList<GenericMove>()));
			EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
			startCommand.setDepth(3);
			this.commandQueue.add(startCommand);
		} catch (IllegalNotationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMate30() {
		AbstractEngine engine = new Flux(this);
		engine.run();
		assertEquals(this.found, true);
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
		this.commandQueue.add(new EngineQuitCommand());
		System.out.println(command);
	}

	public void visit(GuiInformationCommand command) {
		if (command.getMate() != null) {
			if (command.getMate() == 30) {
				this.found = true;
			}
		}
		System.out.println(command);
	}

	public String toString() {
		return "FluxTesting Protocol";
	}

}
