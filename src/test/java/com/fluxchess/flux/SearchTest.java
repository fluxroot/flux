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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fluxchess.jcpi.protocols.IProtocolHandler;
import com.fluxchess.jcpi.AbstractEngine;
import com.fluxchess.jcpi.commands.EngineAnalyzeCommand;
import com.fluxchess.jcpi.commands.EngineInitializeRequestCommand;
import com.fluxchess.jcpi.commands.EngineNewGameCommand;
import com.fluxchess.jcpi.commands.EngineQuitCommand;
import com.fluxchess.jcpi.commands.EngineStartCalculatingCommand;
import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.commands.ProtocolInformationCommand;
import com.fluxchess.jcpi.commands.ProtocolInitializeAnswerCommand;
import com.fluxchess.jcpi.commands.ProtocolReadyAnswerCommand;
import com.fluxchess.jcpi.commands.IEngineCommand;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;

import org.junit.Test;

import com.fluxchess.flux.Flux;

/**
 * SearchTest
 *
 * @author Phokham Nonava
 */
public class SearchTest implements IProtocolHandler {

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

	public void send(ProtocolInitializeAnswerCommand command) {
		System.out.println(command);
	}

	public void send(ProtocolReadyAnswerCommand command) {
		System.out.println(command);
	}

	public void send(ProtocolBestMoveCommand command) {
		this.commandQueue.add(new EngineQuitCommand());
		System.out.println(command);
	}

	public void send(ProtocolInformationCommand command) {
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
