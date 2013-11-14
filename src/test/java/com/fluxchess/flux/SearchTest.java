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

import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.jcpi.protocols.IProtocolHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class SearchTest {

  private static final Logger LOG = LoggerFactory.getLogger(SearchTest.class);

  @Test
  public void testMate30() throws IllegalNotationException {
    final BlockingQueue<IEngineCommand> queue = new LinkedBlockingQueue<>();
    final boolean[] found = {false};

    queue.add(new EngineInitializeRequestCommand());
    queue.add(new EngineNewGameCommand());
    queue.add(new EngineAnalyzeCommand(new GenericBoard("5n2/B3K3/2p2Np1/4k3/7P/3bN1P1/2Prn1P1/1q6 w - -"), new ArrayList<GenericMove>()));
    EngineStartCalculatingCommand startCommand = new EngineStartCalculatingCommand();
    startCommand.setDepth(2);
    queue.add(startCommand);

    new Flux(new IProtocolHandler() {
      @Override
      public IEngineCommand receive() throws IOException {
        try {
          return queue.take();
        } catch (InterruptedException e) {
          LOG.debug(e.getLocalizedMessage());
          return new EngineQuitCommand();
        }
      }

      @Override
      public void send(ProtocolInitializeAnswerCommand command) {
      }

      @Override
      public void send(ProtocolReadyAnswerCommand command) {
      }

      @Override
      public void send(ProtocolBestMoveCommand command) {
        queue.add(new EngineQuitCommand());
      }

      @Override
      public void send(ProtocolInformationCommand command) {
        if (command.getMate() != null && command.getMate() == 30) {
          found[0] = true;
        }
      }
    }).run();

    assertEquals(found[0], true);
  }

}
