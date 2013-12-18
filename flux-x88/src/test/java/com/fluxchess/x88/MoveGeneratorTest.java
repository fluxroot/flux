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
package com.fluxchess.x88;

import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.x88.board.Attack;
import com.fluxchess.x88.board.MoveGenerator;
import com.fluxchess.x88.board.X88Board;
import com.fluxchess.x88.board.X88Move;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoveGeneratorTest {

  private static final Logger LOG = LoggerFactory.getLogger(MoveGeneratorTest.class);

  @Test
  public void testPerft() {
    for (int i = 1; i < 4; i++) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(PerftPerformance.class.getResourceAsStream("/perftsuite.epd")))) {
        String line = reader.readLine();
        while (line != null) {
          String[] tokens = line.split(";");
          if (tokens.length > i) {
            String fen = tokens[0].trim();
            GenericBoard genericBoard = new GenericBoard(fen);
            X88Board board = new X88Board(genericBoard);
            MoveGenerator moveGenerator = new MoveGenerator(board);

            String[] data = tokens[i].trim().split(" ");
            int depth = Integer.parseInt(data[0].substring(1));
            int nodesNumber = Integer.parseInt(data[1]);

            long startTime = System.currentTimeMillis();
            int result = miniMax(board, moveGenerator, depth);
            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;

            LOG.info(String.format(
              "Tested %s at depth %d with nodes number %d: %02d:%02d:%02d.%03d",
              fen,
              depth,
              nodesNumber,
              TimeUnit.MILLISECONDS.toHours(duration),
              TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
              TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
              duration - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration))
            ));
            assertEquals(fen, nodesNumber, result);
          }

          line = reader.readLine();
        }
      } catch (IOException | IllegalNotationException e) {
        fail();
      }
    }
  }

  private int miniMax(X88Board board, MoveGenerator moveGenerator, int depth) {
    if (depth == 0) {
      return 1;
    }

    int totalNodes = 0;

    Attack attack = board.getAttack(board.activeColor);
    moveGenerator.initializeMain(attack);

    int move = moveGenerator.getNextMove();
    while (move != X88Move.NOMOVE) {
      boolean isCheckingMove = board.isCheckingMove(move);
      GenericBoard oldBoard = board.getBoard();

      int captureSquare = board.captureSquare;
      board.makeMove(move);
      boolean isCheckingMoveReal = board.getAttack(board.activeColor).isCheck();
      assertEquals(oldBoard.toString() + ", " + X88Move.toGenericMove(move).toString(), isCheckingMoveReal, isCheckingMove);
      int nodes = miniMax(board, moveGenerator, depth - 1);
      board.undoMove(move);
      assert captureSquare == board.captureSquare;

      totalNodes += nodes;
      move = moveGenerator.getNextMove();
    }

    moveGenerator.destroy();

    return totalNodes;
  }

}
