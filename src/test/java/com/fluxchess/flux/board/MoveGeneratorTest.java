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
package com.fluxchess.flux.board;

import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class MoveGeneratorTest {

  @Test
  public void testPerft() throws IOException, IllegalNotationException {
    for (int i = 1; i < 4; i++) {
      try (InputStream inputStream = MoveGeneratorTest.class.getResourceAsStream("/perftsuite.epd")) {
        BufferedReader file = new BufferedReader(new InputStreamReader(inputStream));

        String line = file.readLine();
        while (line != null) {
          String[] tokens = line.split(";");

          if (tokens.length > i) {
            String[] data = tokens[i].trim().split(" ");
            int depth = Integer.parseInt(data[0].substring(1));
            int nodes = Integer.parseInt(data[1]);

            GenericBoard genericBoard = new GenericBoard(tokens[0].trim());
            Board board = new Board(genericBoard);
            new MoveSee(board);

            long result = miniMax(board, new MoveGenerator(board, new KillerTable(), new HistoryTable()), depth);
            if (nodes != result) {
              throw new AssertionError(findMissingMoves(board, new MoveGenerator(board, new KillerTable(), new HistoryTable()), depth));
            }
          }

          line = file.readLine();
        }
      }
    }
  }

  private long miniMax(Board board, MoveGenerator moveGenerator, int depth) {
    if (depth == 0) {
      return 1;
    }

    long totalNodes = 0;

    Attack attack = board.getAttack(board.activeColor);
    moveGenerator.initializeMain(attack, 0, Move.NOMOVE);

    int move = moveGenerator.getNextMove();
    while (move != Move.NOMOVE) {
      boolean isCheckingMove = board.isCheckingMove(move);
      GenericBoard oldBoard = board.getBoard();
      int captureSquare = board.capturePosition;

      board.makeMove(move);
      boolean isCheckingMoveReal = board.getAttack(board.activeColor).isCheck();
      assertEquals(oldBoard.toString() + ", " + Move.toGenericMove(move).toString(), isCheckingMoveReal, isCheckingMove);
      totalNodes += miniMax(board, moveGenerator, depth - 1);
      board.undoMove(move);

      assert captureSquare == board.capturePosition;

      move = moveGenerator.getNextMove();
    }

    moveGenerator.destroy();

    return totalNodes;
  }

  private String findMissingMoves(Board board, MoveGenerator moveGenerator, int depth) {
    String message = "";

    // Get expected moves from JCPI
    GenericBoard genericBoard = board.getBoard();
    Collection<GenericMove> expectedMoves = new HashSet<>(Arrays.asList(
      com.fluxchess.jcpi.utils.MoveGenerator.getGenericMoves(genericBoard)
    ));

    // Get actual moves
    Collection<GenericMove> actualMoves = new HashSet<>();
    MoveList moves = new MoveList();

    Attack attack = board.getAttack(board.activeColor);
    moveGenerator.initializeMain(attack, 0, Move.NOMOVE);

    int move = moveGenerator.getNextMove();
    while (move != Move.NOMOVE) {
      moves.move[moves.tail++] = move;
      actualMoves.add(Move.toGenericMove(move));

      move = moveGenerator.getNextMove();
    }

    moveGenerator.destroy();

    // Compare expected and actual moves
    Collection<GenericMove> invalidMoves = new HashSet<>(actualMoves);
    invalidMoves.removeAll(expectedMoves);

    Collection<GenericMove> missingMoves = new HashSet<>(expectedMoves);
    missingMoves.removeAll(actualMoves);

    if (invalidMoves.isEmpty() && missingMoves.isEmpty()) {
      if (depth <= 1) {
        return message;
      }

      for (int i = moves.head; i < moves.tail; ++i) {
        move = moves.move[i];

        board.makeMove(move);
        message += miniMax(board, moveGenerator, depth - 1);
        board.undoMove(move);

        if (!message.isEmpty()) {
          break;
        }
      }
    } else {
      message += String.format("Failed check for board: %s%n", genericBoard);
      message += String.format("Expected: %s%n", expectedMoves);
      message += String.format("  Actual: %s%n", actualMoves);
      message += String.format(" Missing: %s%n", missingMoves);
      message += String.format(" Invalid: %s%n", invalidMoves);
    }

    return message;
  }

}
