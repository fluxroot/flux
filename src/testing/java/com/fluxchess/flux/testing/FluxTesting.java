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
package com.fluxchess.flux.testing;

import com.fluxchess.flux.Flux;
import com.fluxchess.jcpi.AbstractEngine;
import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.protocols.IProtocolHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FluxTesting implements IProtocolHandler {

  final BlockingQueue<IEngineCommand> commandQueue = new LinkedBlockingQueue<IEngineCommand>();
  final List<GenericMove> moveList = new ArrayList<GenericMove>();

  public FluxTesting() {
    commandQueue.add(new EngineInitializeRequestCommand());
    commandQueue.add(new EngineDebugCommand(false, true));
    commandQueue.add(new EngineReadyRequestCommand("test"));
    commandQueue.add(new EngineNewGameCommand());
    commandQueue.add(new EngineAnalyzeCommand(new GenericBoard(GenericBoard.STANDARDSETUP), moveList));
    EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
    startCommand.setMoveTime(5000L);
    commandQueue.add(startCommand);
  }

  public static void main(String[] args) {
    FluxTesting testing = new FluxTesting();
    AbstractEngine engine = new Flux(testing);
    engine.run();
  }

  public IEngineCommand receive() {
    IEngineCommand command = null;
    try {
      command = commandQueue.take();
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
    System.out.println(command);
    if (command.bestMove != null) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      moveList.add(command.bestMove);
      commandQueue.add(new EngineStopCalculatingCommand());
      commandQueue.add(new EngineAnalyzeCommand(new GenericBoard(GenericBoard.STANDARDSETUP), moveList));
      EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
      startCommand.setMoveTime(5000L);
      commandQueue.add(startCommand);
    } else {
      commandQueue.add(new EngineQuitCommand());
    }
  }

  public void send(ProtocolInformationCommand command) {
    System.out.println(command);
  }

  public String toString() {
    return "FluxTesting Protocol";
  }

}
