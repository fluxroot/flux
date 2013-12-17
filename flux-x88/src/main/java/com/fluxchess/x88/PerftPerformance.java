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
import com.fluxchess.x88.board.Attack;
import com.fluxchess.x88.board.MoveGenerator;
import com.fluxchess.x88.board.X88Board;
import com.fluxchess.x88.board.X88Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class PerftPerformance {

  private static final Logger LOG = LoggerFactory.getLogger(PerftPerformance.class);

  public static void main(String[] args) {
    long totalNodes = 0;
    long totalTime = 0;

    GenericBoard genericBoard = new GenericBoard(GenericBoard.STANDARDSETUP);
    X88Board board = new X88Board(genericBoard);
    MoveGenerator moveGenerator = new MoveGenerator(board);

    for (int i = 1; i < 4; ++i) {
      int depth = 6;

      long startTime = System.currentTimeMillis();
      int result = miniMax(board, moveGenerator, depth);
      long endTime = System.currentTimeMillis();

      long duration = endTime - startTime;
      totalNodes += result;
      totalTime += duration;

      LOG.info(String.format(
        "Tested %s at depth %d: %02d:%02d:%02d.%03d",
        genericBoard.toString(),
        depth,
        TimeUnit.MILLISECONDS.toHours(duration),
        TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
        duration - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration))
      ));
    }

    LOG.info(String.format("Total nodes per millisecond: %d", totalNodes / totalTime));
  }

  private static int miniMax(X88Board board, MoveGenerator moveGenerator, int depth) {
    if (depth == 0) {
      return 1;
    }

    int totalNodes = 0;

    Attack attack = board.getAttack(board.activeColor);
    moveGenerator.initializeMain(attack);

    int move = moveGenerator.getNextMove();
    while (move != X88Move.NOMOVE) {
      board.makeMove(move);
      int nodes = miniMax(board, moveGenerator, depth - 1);
      board.undoMove(move);

      totalNodes += nodes;
      move = moveGenerator.getNextMove();
    }

    moveGenerator.destroy();

    return totalNodes;
  }

}
