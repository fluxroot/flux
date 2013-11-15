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
package com.fluxchess.flux.search;

import com.fluxchess.flux.ChessLogger;
import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.InformationTimer;
import com.fluxchess.flux.board.*;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.move.*;
import com.fluxchess.flux.table.HistoryTable;
import com.fluxchess.flux.table.KillerTable;
import com.fluxchess.flux.table.TranspositionTable;
import com.fluxchess.flux.table.TranspositionTableEntry;
import com.fluxchess.jcpi.models.GenericMove;

import java.util.*;
import java.util.concurrent.Semaphore;

public final class Search implements Runnable {

  public static final int MAX_HEIGHT = 256;
  public static final int MAX_DEPTH = 64;
  public static final int MAX_MOVES = 4096;

  // Constants
  public static final int INFINITY = 200000;
  public static final int DRAW = 0;
  public static final int CHECKMATE = 100000;
  public static final int CHECKMATE_THRESHOLD = CHECKMATE - 1000;

  private static final int ASPIRATIONWINDOW = 20;
  private static final int[] ASPIRATIONWINDOW_ADJUSTMENT = new int[]{20, 20, 40, 80, 160, 320, CHECKMATE};

  private static final int TIMEEXTENSION_MARGIN = 30;

  // Objects
  private final ChessLogger logger = ChessLogger.getLogger();
  private InformationTimer info;
  private final Thread thread = new Thread(this);
  private final Semaphore semaphore = new Semaphore(0);

  // Search control
  private Timer timer = null;
  private boolean canStop = false;
  private boolean stopped = true;
  private boolean stopFlag = false;
  private boolean doTimeManagement = true;
  private boolean analyzeMode = false;

  // Search parameters
  private int searchDepth = 0;
  private long searchNodes = 0;
  private long searchTime = 0;
  private long searchTimeHard = 0;
  private long searchTimeStart = 0;
  private final long[] searchClock = new long[IntColor.ARRAY_DIMENSION];
  private final long[] searchClockIncrement = new long[IntColor.ARRAY_DIMENSION];
  private int searchMovesToGo = 0;
  private final MoveList searchMoveList = new MoveList();

  // Analyze parameters
  private int showPvNumber = 1;

  // Search logic
  private MoveGenerator moveGenerator;
  private Evaluation evaluation;
  private static Hex88Board board;
  private final int myColor;

  // Search tables
  private TranspositionTable transpositionTable;
  private static KillerTable killerTable;
  private static HistoryTable historyTable;

  // Search information
  private static final MoveList[] pvList = new MoveList[MAX_HEIGHT + 1];
  private static final HashMap<Integer, PrincipalVariation> multiPvMap = new HashMap<>(MAX_MOVES);
  private Result bestResult = null;
  private final int[] timeTable;

  // Static initialization
  static {
    for (int i = 0; i < pvList.length; i++) {
      pvList[i] = new MoveList();
    }
  }

  public Search(Evaluation newEvaluation, Hex88Board newBoard, TranspositionTable newTranspositionTable, InformationTimer newInfo, int[] timeTable) {
    assert newEvaluation != null;
    assert newBoard != null;
    assert newTranspositionTable != null;
    assert newInfo != null;

    analyzeMode = Configuration.analyzeMode;

    evaluation = newEvaluation;
    board = newBoard;
    myColor = newBoard.activeColor;

    transpositionTable = newTranspositionTable;
    if (analyzeMode) {
      transpositionTable.increaseAge();
    }
    killerTable = new KillerTable();
    historyTable = new HistoryTable();

    moveGenerator = new MoveGenerator(newBoard, killerTable, historyTable);
    new MoveSee(newBoard);

    info = newInfo;
    info.setSearch(this);

    this.timeTable = timeTable;

    multiPvMap.clear();
  }

  public void run() {
    logger.debug("Analyzing fen " + board.getBoard().toString());
    stopped = false;
    canStop = false;
    bestResult = new Result();

    // Set the time managemnet
    if (doTimeManagement) {
      setTimeManagement();
      searchTimeStart = System.currentTimeMillis();
    }
    if (searchTime > 0) {
      startTimer();
    }

    // Go...
    semaphore.release();
    info.start();
    Result moveResult = getBestMove();
    info.stop();

    // Cancel the timer
    if (timer != null) {
      timer.cancel();
    }

    // Send the result
    if (moveResult.bestMove != IntMove.NOMOVE) {
      if (moveResult.ponderMove != IntMove.NOMOVE) {
        info.sendBestMove(IntMove.toGenericMove(moveResult.bestMove), IntMove.toGenericMove(moveResult.ponderMove));
      } else {
        info.sendBestMove(IntMove.toGenericMove(moveResult.bestMove), null);
      }
    } else {
      info.sendBestMove(null, null);
    }

    // Cleanup manually
    transpositionTable = null;
    info = null;
    evaluation = null;
  }

  public void start() {
    thread.start();
    try {
      // Wait for initialization
      semaphore.acquire();
    } catch (InterruptedException e) {
      logger.debug(e.getMessage());
      // Do nothing
    }
  }

  public void stop() {
    stopped = true;
    canStop = true;
    try {
      // Wait for the thread to die
      thread.join();
    } catch (InterruptedException e) {
      logger.debug(e.getMessage());
      // Do nothing
    }
  }

  public void ponderhit() {
    // Enable time management
    doTimeManagement = true;

    // Set time management parameters
    setTimeManagement();
    searchTimeStart = System.currentTimeMillis();

    // Start our hard stop timer
    startTimer();

    // Check whether we have already a result
    assert bestResult != null;
    if (bestResult.bestMove != IntMove.NOMOVE) {
      canStop = true;

      // Check if we have a checkmate
      if (Math.abs(bestResult.resultValue) > CHECKMATE_THRESHOLD
        && bestResult.depth >= (CHECKMATE - Math.abs(bestResult.resultValue))) {
        stopped = true;
      }

      // Check if we have only one move to make
      else if (bestResult.moveNumber == 1) {
        stopped = true;
      }
    }
  }

  public boolean isStopped() {
    return !thread.isAlive();
  }

  public void setSearchDepth(int searchDepth) {
    assert searchDepth > 0;

    this.searchDepth = searchDepth;
    if (searchDepth > MAX_DEPTH) {
      searchDepth = MAX_DEPTH;
    }
    doTimeManagement = false;
  }

  public void setSearchNodes(long searchNodes) {
    assert searchNodes > 0;

    this.searchNodes = searchNodes;
    searchDepth = MAX_DEPTH;
    doTimeManagement = false;
  }

  public void setSearchTime(long searchTime) {
    assert searchTime > 0;

    this.searchTime = searchTime;
    searchTimeHard = searchTime;
    searchDepth = MAX_DEPTH;
    doTimeManagement = false;
  }

  public void setSearchClock(int side, long timeLeft) {
    assert timeLeft > 0;

    searchClock[side] = timeLeft;
  }

  public void setSearchClockIncrement(int side, long timeIncrement) {
    assert timeIncrement > 0;

    searchClockIncrement[side] = timeIncrement;
  }

  public void setSearchMovesToGo(int searchMovesToGo) {
    assert searchMovesToGo > 0;

    this.searchMovesToGo = searchMovesToGo;
  }

  public void setSearchInfinite() {
    searchDepth = MAX_DEPTH;
    doTimeManagement = false;
    analyzeMode = true;
  }

  public void setSearchPonder() {
    searchDepth = MAX_DEPTH;
    doTimeManagement = false;
  }

  public void setSearchMoveList(List<GenericMove> moveList) {
    for (GenericMove move : moveList) {
      searchMoveList.move[searchMoveList.tail++] = IntMove.convertMove(move, board);
    }
  }

  private void startTimer() {
    // Only start timer if we have a hard time limit
    if (searchTimeHard > 0) {
      timer = new Timer(true);
      timer.schedule(new TimerTask() {
        public void run() {
          stop();
        }
      }, searchTimeHard);
    }
  }

  private void setTimeManagement() {
    // Dynamic time allocation
    searchDepth = MAX_DEPTH;

    if (searchClock[myColor] > 0) {
      // We received a time control.

      // Check the moves to go
      if (searchMovesToGo < 1 || searchMovesToGo > 40) {
        searchMovesToGo = 40;
      }

      // Check the increment
      if (searchClockIncrement[myColor] < 1) {
        searchClockIncrement[myColor] = 0;
      }

      // Set the maximum search time
      long maxSearchTime = (long) (searchClock[myColor] * 0.95) - 1000L;
      if (maxSearchTime < 0) {
        maxSearchTime = 0;
      }

      // Set the search time
      searchTime = (maxSearchTime + (searchMovesToGo - 1) * searchClockIncrement[myColor]) / searchMovesToGo;
      if (searchTime > maxSearchTime) {
        searchTime = maxSearchTime;
      }

      // Set the hard limit search time
      searchTimeHard = (maxSearchTime + (searchMovesToGo - 1) * searchClockIncrement[myColor]) / 8;
      if (searchTimeHard < searchTime) {
        searchTimeHard = searchTime;
      }
      if (searchTimeHard > maxSearchTime) {
        searchTimeHard = maxSearchTime;
      }
    } else {
      // We received no time control. Search for 2 seconds.
      searchTime = 2000L;

      // Stop hard after +50% of the allocated time
      searchTimeHard = searchTime + searchTime / 2;
    }
  }

  private void sendInformation(PrincipalVariation pv, int pvNumber) {
    if (Math.abs(pv.value) > CHECKMATE_THRESHOLD) {
      // Calculate the mate distance
      int mateDepth = CHECKMATE - Math.abs(pv.value);
      info.sendInformationMate(pv, Integer.signum(pv.value) * (mateDepth + 1) / 2, pvNumber);
      logger.debug("Mate value: " + pv.value + ", Mate depth: " + mateDepth);
    } else {
      info.sendInformationCentipawns(pv, pvNumber);
    }
  }

  private Result getBestMove() {
    //## BEGIN Root Move List
    MoveList rootMoveList = new MoveList();

    PrincipalVariation pv = null;
    int transpositionMove = IntMove.NOMOVE;
    int transpositionDepth = -1;
    int transpositionValue = 0;
    int transpositionType = IntScore.NOSCORE;
    if (Configuration.useTranspositionTable) {
      TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        List<GenericMove> moveList = transpositionTable.getMoveList(board, entry.depth, new ArrayList<GenericMove>());
        if (moveList.size() != 0) {
          pv = new PrincipalVariation(
            1,
            entry.getValue(0),
            entry.type,
            entry.getValue(0),
            moveList,
            entry.depth,
            entry.depth,
            0,
            0,
            0,
            0
          );
        }
        transpositionMove = entry.move;
        transpositionDepth = entry.depth;
        transpositionValue = entry.getValue(0);
        transpositionType = entry.type;
      }
    }

    Attack attack = board.getAttack(board.activeColor);
    boolean isCheck = attack.isCheck();

    if (searchMoveList.getLength() == 0) {
      moveGenerator.initializeMain(attack, 0, transpositionMove);

      int move = IntMove.NOMOVE;
      while ((move = moveGenerator.getNextMove()) != IntMove.NOMOVE) {
        rootMoveList.move[rootMoveList.tail++] = move;
      }

      moveGenerator.destroy();
    } else {
      for (int i = searchMoveList.head; i < searchMoveList.tail; i++) {
        rootMoveList.move[rootMoveList.tail++] = searchMoveList.move[i];
      }
    }

    // Check if we cannot move
    if (rootMoveList.getLength() == 0) {
      // This position is a checkmate or stalemate
      return bestResult;
    }

    // Adjust pv number
    showPvNumber = Configuration.showPvNumber;
    if (Configuration.showPvNumber > rootMoveList.getLength()) {
      showPvNumber = rootMoveList.getLength();
    }
    //## ENDOF Root Move List

    int alpha = -CHECKMATE;
    int beta = CHECKMATE;

    int initialDepth = 1;
    int equalResults = 0;
    if (!analyzeMode
      && transpositionDepth > 1
      && transpositionType == IntScore.EXACT
      && Math.abs(transpositionValue) < CHECKMATE_THRESHOLD
      && pv != null) {
      bestResult.bestMove = transpositionMove;
      bestResult.resultValue = transpositionValue;
      bestResult.value = transpositionType;
      bestResult.time = 0;
      bestResult.moveNumber = rootMoveList.getLength();

      initialDepth = transpositionDepth;
      equalResults = transpositionDepth - 2;
    }

    //## BEGIN Iterative Deepening
    for (int currentDepth = initialDepth; currentDepth <= searchDepth; currentDepth++) {
      info.setCurrentDepth(currentDepth);
      info.sendInformationDepth();

      // Create a new result
      Result moveResult = new Result();

      // Set the start time
      long startTime = System.currentTimeMillis();

      int value;
      if (currentDepth == initialDepth && initialDepth > 1) {
        value = transpositionValue;
        pvList[0].resetList();
        sendInformation(pv, 1);

        moveResult.bestMove = transpositionMove;
        moveResult.resultValue = transpositionValue;
        moveResult.value = transpositionType;
        moveResult.moveNumber = rootMoveList.getLength();
      } else {
        // Do the Alpha-Beta search
        value = alphaBetaRoot(currentDepth, alpha, beta, 0, rootMoveList, isCheck, moveResult);
      }

      //## BEGIN Aspiration Windows
      if (!(stopped && canStop) && Configuration.useAspirationWindows && showPvNumber <= 1 && currentDepth >= transpositionDepth) {
        int adjustmentIndex = 0;
        while (adjustmentIndex < ASPIRATIONWINDOW_ADJUSTMENT.length && (value <= alpha || value >= beta)) {
          int newAspirationWindow = ASPIRATIONWINDOW_ADJUSTMENT[adjustmentIndex];

          alpha -= newAspirationWindow;
          beta += newAspirationWindow;
          if (alpha < -CHECKMATE) {
            alpha = -CHECKMATE;
          }
          if (beta > CHECKMATE) {
            beta = CHECKMATE;
          }

          Result moveResultAdjustment = new Result();

          // Do the Alpha-Beta search again
          value = alphaBetaRoot(currentDepth, alpha, beta, 0, rootMoveList, isCheck, moveResultAdjustment);

          if (stopped && canStop) {
            break;
          }

          // We have a new move result
          moveResult = moveResultAdjustment;

          adjustmentIndex++;
        }

        // Adjust aspiration window
        alpha = value - ASPIRATIONWINDOW;
        beta = value + ASPIRATIONWINDOW;
        if (alpha < -CHECKMATE) {
          alpha = -CHECKMATE;
        }
        if (beta > CHECKMATE) {
          beta = CHECKMATE;
        }
      }
      //## ENDOF Aspiration Windows

      // Set the end time
      long endTime = System.currentTimeMillis();
      moveResult.time = endTime - startTime;
      moveResult.depth = currentDepth;

      // Set the used time
      if (currentDepth > initialDepth) {
        if (timeTable[currentDepth] == 0) {
          timeTable[currentDepth] += moveResult.time;
        } else {
          timeTable[currentDepth] += moveResult.time;
          timeTable[currentDepth] /= 2;
        }
      }

      // Prepare the move result
      if (moveResult.bestMove != IntMove.NOMOVE) {
        // Count all equal results
        if (moveResult.bestMove == bestResult.bestMove) {
          equalResults++;
        } else {
          equalResults = 0;
        }

        if (doTimeManagement) {
          //## BEGIN Time Control
          boolean timeExtended = false;

          // Check value change
          if (moveResult.resultValue + TIMEEXTENSION_MARGIN < bestResult.resultValue
            || moveResult.resultValue - TIMEEXTENSION_MARGIN > bestResult.resultValue) {
            timeExtended = true;
          }

          // Check equal results
          else if (equalResults < 1) {
            timeExtended = true;
          }

          // Set the needed time for the next iteration
          long nextIterationTime = timeTable[currentDepth + 1];
          if (timeTable[currentDepth + 1] == 0) {
            nextIterationTime = (moveResult.time * 2);
          }

          // Check if we cannot finish the next iteration on time
          if (searchTimeStart + searchTimeHard < System.currentTimeMillis() + nextIterationTime) {
            // Clear table
            if (currentDepth == initialDepth) {
              for (int i = currentDepth + 1; i < timeTable.length; i++) {
                timeTable[i] = 0;
              }
            } else {
              for (int i = currentDepth + 1; i < timeTable.length; i++) {
                timeTable[i] += timeTable[i - 1] * 2;
                timeTable[i] /= 2;
              }
            }
            stopFlag = true;
          }

          // Check time limit
          else if (!timeExtended
            && searchTimeStart + searchTime < System.currentTimeMillis() + nextIterationTime) {
            // Clear table
            if (currentDepth == initialDepth) {
              for (int i = currentDepth + 1; i < timeTable.length; i++) {
                timeTable[i] = 0;
              }
            } else {
              for (int i = currentDepth + 1; i < timeTable.length; i++) {
                timeTable[i] += timeTable[i - 1] * 2;
                timeTable[i] /= 2;
              }
            }
            stopFlag = true;
          }

          // Check if this is an easy recapture
          else if (!timeExtended
            && IntMove.getEnd(moveResult.bestMove) == board.captureSquare
            && IntChessman.getValueFromChessman(IntMove.getTarget(moveResult.bestMove)) >= IntChessman.VALUE_KNIGHT
            && equalResults > 4) {
            stopFlag = true;
          }

          // Check if we have a checkmate
          else if (Math.abs(value) > CHECKMATE_THRESHOLD
            && currentDepth >= (CHECKMATE - Math.abs(value))) {
            stopFlag = true;
          }

          // Check if we have only one move to make
          else if (moveResult.moveNumber == 1) {
            stopFlag = true;
          }
          //## ENDOF Time Control
        }

        // Update the best result.
        bestResult = moveResult;

        if (pvList[0].tail > 1) {
          // We found a line. Set the ponder move.
          bestResult.ponderMove = pvList[0].move[1];
        }
      } else {
        // We found no best move.
        // Perhaps we have a checkmate or we got a stop request?
        break;
      }

      // Check if we can stop the search
      if (stopFlag) {
        break;
      }

      canStop = true;

      if (stopped) {
        break;
      }
    }
    //## ENDOF Iterative Deepening

    // Update all stats
    info.sendInformationSummary();

    return bestResult;
  }

  private void updateSearch(int height) {
    assert transpositionTable.getPermillUsed() >= 0 && transpositionTable.getPermillUsed() <= 1000;

    info.totalNodes++;
    info.setCurrentMaxDepth(height);
    info.sendInformationStatus();

    if (searchNodes > 0 && searchNodes <= info.totalNodes) {
      // Hard stop on number of nodes
      stopped = true;
    }

    // Reset
    pvList[height].resetList();
  }

  /**
   * Returns the new possibly extended search depth.
   *
   * @param depth         the current depth.
   * @param move          the current move.
   * @param isSingleReply whether we are in check and have only one way out.
   * @param mateThreat    whether we have a mate threat.
   * @return the new possibly extended search depth.
   */
  private int getNewDepth(int depth, int move, boolean isSingleReply, boolean mateThreat) {
    int newDepth = depth - 1;

    assert (IntMove.getEnd(move) != board.captureSquare) || (IntMove.getTarget(move) != IntChessman.NOPIECE);

    //## Recapture Extension
    if (Configuration.useRecaptureExtension
      && IntMove.getEnd(move) == board.captureSquare
      && MoveSee.seeMove(move, IntMove.getChessmanColor(move)) > 0) {
      newDepth++;
    }

    //## Check Extension
    else if (Configuration.useCheckExtension
      && board.isCheckingMove(move)) {
      newDepth++;
    }

    //## Pawn Extension
    else if (Configuration.usePawnExtension
      && IntMove.getChessman(move) == IntChessman.PAWN
      && IntPosition.getRelativeRank(IntMove.getEnd(move), board.activeColor) == IntPosition.rank7) {
      newDepth++;
    }

    //## Single-Reply Extension
    else if (Configuration.useSingleReplyExtension
      && isSingleReply) {
      newDepth++;
    }

    //## Mate Threat Extension
    else if (Configuration.useMateThreatExtension
      && mateThreat) {
      newDepth++;
    }

    // Extend another ply if we enter a pawn endgame
    if (board.materialCount[board.activeColor] == 0
      && board.materialCount[IntColor.switchColor(board.activeColor)] == 1
      && IntMove.getTarget(move) != IntChessman.NOPIECE
      && IntMove.getTarget(move) != IntChessman.PAWN) {
      newDepth++;
    }

    return newDepth;
  }

  private static boolean isDangerousMove(int move) {
    int chessman = IntMove.getChessman(move);
    int relativeRank = IntPosition.getRelativeRank(IntMove.getEnd(move), board.activeColor);
    if (chessman == IntChessman.PAWN && relativeRank >= IntPosition.rank7) {
      return true;
    }

    int target = IntMove.getTarget(move);
    if (target == IntChessman.QUEEN) {
      return true;
    }

    return false;
  }

  private static void addPv(MoveList destination, MoveList source, int move) {
    assert destination != null;
    assert source != null;
    assert move != IntMove.NOMOVE;

    destination.resetList();

    destination.move[destination.tail++] = move;

    for (int i = source.head; i < source.tail; i++) {
      destination.move[destination.tail++] = source.move[i];
    }
  }

  private static void addGoodMove(int move, int depth, int height) {
    assert move != IntMove.NOMOVE;

    if (IntMove.getTarget(move) != IntChessman.NOPIECE) {
      return;
    }

    int type = IntMove.getType(move);
    if (type == IntMove.PAWNPROMOTION || type == IntMove.NULL) {
      return;
    }

    assert type != IntMove.ENPASSANT;

    killerTable.add(move, height);
    historyTable.add(move, depth);
  }

}
