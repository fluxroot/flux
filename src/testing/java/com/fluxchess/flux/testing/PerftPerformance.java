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

import com.fluxchess.flux.board.*;
import com.fluxchess.jcpi.models.GenericBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PerftPerformance {

  private static final Logger LOG = LoggerFactory.getLogger(PerftPerformance.class);

  public static void main(String[] args) {
    long totalNodes = 0;
    long totalTime = 0;

    GenericBoard genericBoard = new GenericBoard(GenericBoard.STANDARDSETUP);
    Board board = new Board(genericBoard);
    new MoveSee(board);
    KillerTable killerTable = new KillerTable();
    HistoryTable historyTable = new HistoryTable();
    MoveGenerator moveGenerator = new MoveGenerator(board, killerTable, historyTable);
    int depth = 6;

    LOG.info(String.format("Testing %s at depth %d", genericBoard.toString(), depth));

    for (int i = 1; i < 4; ++i) {
      long startTime = System.currentTimeMillis();
      totalNodes += miniMax(board, moveGenerator, depth);
      long endTime = System.currentTimeMillis();

      long duration = endTime - startTime;
      totalTime += duration;

      LOG.info(String.format(
        "Duration iteration %d: %02d:%02d:%02d.%03d",
        i,
        TimeUnit.MILLISECONDS.toHours(duration),
        TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
        duration - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration))
      ));
    }

    LOG.info(String.format("Total nodes per millisecond: %d", totalNodes / totalTime));
  }

  private static long miniMax(Board board, MoveGenerator moveGenerator, int depth) {
    if (depth == 0) {
      return 1;
    }

    Attack attack = board.getAttack(board.activeColor);
    moveGenerator.initializeMain(attack, 0, Move.NOMOVE);

    int totalNodes = 0;

    int move = moveGenerator.getNextMove();
    while (move != Move.NOMOVE) {

      board.makeMove(move);
      totalNodes += miniMax(board, moveGenerator, depth - 1);
      board.undoMove(move);

      move = moveGenerator.getNextMove();
    }

    moveGenerator.destroy();

    return totalNodes;
  }

}
