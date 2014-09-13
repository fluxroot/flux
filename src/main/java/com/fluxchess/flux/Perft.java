/*
 * Copyright (C) 2007-2014 Phokham Nonava
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

import com.fluxchess.jcpi.models.GenericBoard;

import java.util.concurrent.TimeUnit;

final class Perft {

  private static final int MAX_DEPTH = 6;

  void run() {
    Position position = new Position(new GenericBoard(GenericBoard.STANDARDSETUP));
    int depth = MAX_DEPTH;

    new MoveGenerator(position, new KillerTable(), new HistoryTable());
    new See(position);

    System.out.format("Testing %s at depth %d%n", position.toString(), depth);

    long startTime = System.currentTimeMillis();
    long result = miniMax(position, depth, 0);
    long endTime = System.currentTimeMillis();

    long duration = endTime - startTime;

    System.out.format(
        "Nodes: %d%nDuration: %02d:%02d:%02d.%03d%n",
        result,
        TimeUnit.MILLISECONDS.toHours(duration),
        TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
        duration - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration))
    );

    System.out.format("n/ms: %d%n", result / duration);
  }

  private static long miniMax(Position board, int depth, int ply) {
    if (depth == 0) {
      return 1;
    }

    long totalNodes = 0;

    Attack attack = board.getAttack(board.activeColor);
    MoveGenerator.initializeMain(attack, 0, Move.NOMOVE);

    int move;
    while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
      board.makeMove(move);
      totalNodes += miniMax(board, depth - 1, ply + 1);
      board.undoMove(move);
    }

    MoveGenerator.destroy();

    return totalNodes;
  }

}
