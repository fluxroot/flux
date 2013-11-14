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

// TODO: Rewrite unit test
//import static org.junit.Assert.assertEquals;
//
//import java.util.ArrayList;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//
//import org.junit.Test;
//
//import com.fluxchess.flux.Flux;
//import com.fluxchess.jcpi.AbstractCommunication;
//import com.fluxchess.jcpi.AbstractEngine;
//import com.fluxchess.jcpi.IGui;
//import com.fluxchess.jcpi.commands.EngineAnalyzeCommand;
//import com.fluxchess.jcpi.commands.EngineInitializeRequestCommand;
//import com.fluxchess.jcpi.commands.EngineNewGameCommand;
//import com.fluxchess.jcpi.commands.EngineQuitCommand;
//import com.fluxchess.jcpi.commands.EngineStartCalculatingCommand;
//import com.fluxchess.jcpi.commands.GuiBestMoveCommand;
//import com.fluxchess.jcpi.commands.GuiInformationCommand;
//import com.fluxchess.jcpi.commands.GuiInitializeAnswerCommand;
//import com.fluxchess.jcpi.commands.GuiQuitCommand;
//import com.fluxchess.jcpi.commands.GuiReadyAnswerCommand;
//import com.fluxchess.jcpi.commands.IEngineCommand;
//import com.fluxchess.jcpi.commands.IGuiCommand;
//import com.fluxchess.jcpi.data.GenericBoard;
//import com.fluxchess.jcpi.data.GenericMove;
//import com.fluxchess.jcpi.data.IllegalNotationException;
//
//public class SearchTest extends AbstractCommunication implements IGui {
//
//    BlockingQueue<IEngineCommand> commandQueue = new LinkedBlockingQueue<>();
//    boolean found = false;
//
//    public SearchTest() {
//        try {
//            commandQueue.add(new EngineInitializeRequestCommand());
//            commandQueue.add(new EngineNewGameCommand());
//            commandQueue.add(new EngineAnalyzeCommand(new GenericBoard("5n2/B3K3/2p2Np1/4k3/7P/3bN1P1/2Prn1P1/1q6 w - -"), new ArrayList<GenericMove>()));
//            EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
//            startCommand.setDepth(3);
//            commandQueue.add(startCommand);
//        } catch (IllegalNotationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testMate30() {
//        AbstractEngine engine = new Flux(this);
//        engine.run();
//        assertEquals(found, true);
//    }
//
//    public void send(IGuiCommand command) {
//        command.accept(this);
//    }
//
//    public IEngineCommand receive() {
//        IEngineCommand command = null;
//        try {
//            command = commandQueue.take();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        assert command != null;
//
//        System.out.println(command);
//
//        return command;
//    }
//
//    public void visit(GuiInitializeAnswerCommand command) {
//        System.out.println(command);
//    }
//
//    public void visit(GuiReadyAnswerCommand command) {
//        System.out.println(command);
//    }
//
//    public void visit(GuiBestMoveCommand command) {
//        commandQueue.add(new EngineQuitCommand());
//        System.out.println(command);
//    }
//
//    public void visit(GuiInformationCommand command) {
//        if (command.getMate() != null) {
//            if (command.getMate() == 30) {
//                found = true;
//            }
//        }
//        System.out.println(command);
//    }
//
//    public void visit(GuiQuitCommand command) {
//        System.out.println(command);
//    }
//
//    public String toString() {
//        return "FluxTesting Protocol";
//    }
//
//}
