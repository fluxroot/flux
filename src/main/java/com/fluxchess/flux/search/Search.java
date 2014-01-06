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
package com.fluxchess.flux.search;

import com.fluxchess.flux.ChessLogger;
import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.board.*;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.jcpi.models.*;

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

  private static final int NULLMOVE_DEPTH = 2;
  private static final int NULLMOVE_REDUCTION;
  private static final int NULLMOVE_VERIFICATIONREDUCTION = 3;

  private static final int IID_DEPTH = 2;

  private static final int LMR_DEPTH = 3;
  private static final int LMR_MOVENUMBER_MINIMUM = 3;

  private static final int FUTILITY_FRONTIERMARGIN = 2 * Evaluation.VALUE_PAWN;
  private static final int FUTILITY_PREFRONTIERMARGIN = Evaluation.VALUE_ROOK;
  private static final int FUTILITY_QUIESCENTMARGIN = Evaluation.VALUE_PAWN;

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
  private final long[] searchClock = new long[IntColor.values.length];
  private final long[] searchClockIncrement = new long[IntColor.values.length];
  private int searchMovesToGo = 0;
  private final MoveList searchMoveList = new MoveList();

  // Analyze parameters
  private int showPvNumber = 1;

  // Search logic
  private MoveGenerator moveGenerator;
  private Evaluation evaluation;
  private static Board board;
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
    if (Configuration.useVerifiedNullMovePruning) {
      NULLMOVE_REDUCTION = 3;
    } else {
      NULLMOVE_REDUCTION = 2;
    }

    for (int i = 0; i < pvList.length; i++) {
      pvList[i] = new MoveList();
    }
  }

  public Search(Evaluation newEvaluation, Board newBoard, TranspositionTable newTranspositionTable, InformationTimer newInfo, int[] timeTable) {
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
    if (moveResult.bestMove != Move.NOMOVE) {
      if (moveResult.ponderMove != Move.NOMOVE) {
        info.sendBestMove(Move.toGenericMove(moveResult.bestMove), Move.toGenericMove(moveResult.ponderMove));
      } else {
        info.sendBestMove(Move.toGenericMove(moveResult.bestMove), null);
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
    if (bestResult.bestMove != Move.NOMOVE) {
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
      searchMoveList.move[searchMoveList.tail++] = Move.valueOf(move, board);
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
    int transpositionMove = Move.NOMOVE;
    int transpositionDepth = -1;
    int transpositionValue = 0;
    int transpositionType = Score.NOSCORE;
    if (Configuration.useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
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

    if (searchMoveList.size() == 0) {
      moveGenerator.initializeMain(attack, 0, transpositionMove);

      int move = Move.NOMOVE;
      while ((move = moveGenerator.getNextMove()) != Move.NOMOVE) {
        rootMoveList.move[rootMoveList.tail++] = move;
      }

      moveGenerator.destroy();
    } else {
      for (int i = searchMoveList.head; i < searchMoveList.tail; i++) {
        rootMoveList.move[rootMoveList.tail++] = searchMoveList.move[i];
      }
    }

    // Check if we cannot move
    if (rootMoveList.size() == 0) {
      // This position is a checkmate or stalemate
      return bestResult;
    }

    // Adjust pv number
    showPvNumber = Configuration.showPvNumber;
    if (Configuration.showPvNumber > rootMoveList.size()) {
      showPvNumber = rootMoveList.size();
    }
    //## ENDOF Root Move List

    int alpha = -CHECKMATE;
    int beta = CHECKMATE;

    int initialDepth = 1;
    int equalResults = 0;
    if (!analyzeMode
      && transpositionDepth > 1
      && transpositionType == Score.EXACT
      && Math.abs(transpositionValue) < CHECKMATE_THRESHOLD
      && pv != null) {
      bestResult.bestMove = transpositionMove;
      bestResult.resultValue = transpositionValue;
      bestResult.value = transpositionType;
      bestResult.time = 0;
      bestResult.moveNumber = rootMoveList.size();

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
        pvList[0].clear();
        sendInformation(pv, 1);

        moveResult.bestMove = transpositionMove;
        moveResult.resultValue = transpositionValue;
        moveResult.value = transpositionType;
        moveResult.moveNumber = rootMoveList.size();
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
      if (moveResult.bestMove != Move.NOMOVE) {
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
            && Move.getTargetPosition(moveResult.bestMove) == board.captureSquare
            && Evaluation.getValueFromPiece(Move.getTargetPiece(moveResult.bestMove)) >= Evaluation.VALUE_KNIGHT
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
    pvList[height].clear();
  }

  private int alphaBetaRoot(int depth, int alpha, int beta, int height, MoveList rootMoveList, boolean isCheck, Result moveResult) {
    updateSearch(height);

    // Abort conditions
    if ((stopped && canStop) || height == MAX_HEIGHT) {
      return evaluation.evaluate(board);
    }

    // Initialize
    int hashType = Score.ALPHA;
    int bestValue = -INFINITY;
    int bestMove = Move.NOMOVE;
    int oldAlpha = alpha;
    PrincipalVariation lastMultiPv = null;
    PrincipalVariation bestPv = null;
    PrincipalVariation firstPv = null;

    // Initialize the move number
    int currentMoveNumber = 0;

    // Initialize Single-Response Extension
    boolean isSingleReply = isCheck && rootMoveList.size() == 1;

    for (int j = rootMoveList.head; j < rootMoveList.tail; j++) {
      int move = rootMoveList.move[j];

      // Update the information if we evaluate a new move.
      currentMoveNumber++;
      info.sendInformationMove(Move.toGenericMove(move), currentMoveNumber);

      // Extension
      int newDepth = getNewDepth(depth, move, isSingleReply, false);

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (bestValue == -INFINITY) {
        // First move
        value = -alphaBeta(newDepth, -beta, -alpha, height + 1, true, true);
      } else {
        value = -alphaBeta(newDepth, -alpha - 1, -alpha, height + 1, false, true);
        if (value > alpha && value < beta) {
          // Research again
          value = -alphaBeta(newDepth, -beta, -alpha, height + 1, true, true);
        }
      }
      //## ENDOF Principal Variation Search

      // Undo move
      board.undoMove(move);

      if (stopped && canStop) {
        break;
      }

      // Store value
      int sortValue;
      int moveType;
      if (value <= alpha) {
        value = alpha;
        moveType = Score.ALPHA;
        rootMoveList.value[j] = oldAlpha;
        sortValue = -INFINITY;
      } else if (value >= beta) {
        value = beta;
        moveType = Score.BETA;
        rootMoveList.value[j] = beta;
        sortValue = INFINITY;
      } else {
        moveType = Score.EXACT;
        rootMoveList.value[j] = value;
        sortValue = value;
      }

      // Add pv to list
      List<GenericMove> genericMoveList = new ArrayList<>();
      genericMoveList.add(Move.toGenericMove(move));
      for (int i = pvList[height + 1].head; i < pvList[height + 1].tail; i++) {
        genericMoveList.add(Move.toGenericMove(pvList[height + 1].move[i]));
      }
      PrincipalVariation pv = new PrincipalVariation(
        currentMoveNumber,
        value,
        moveType,
        sortValue,
        genericMoveList,
        info.currentDepth,
        info.currentMaxDepth,
        transpositionTable.getPermillUsed(),
        info.getCurrentNps(),
        System.currentTimeMillis() - info.totalTimeStart,
        info.totalNodes);
      multiPvMap.put(move, pv);

      // Save first pv
      if (currentMoveNumber == 1) {
        firstPv = pv;
      }

      // Show refutations
      if (Configuration.showRefutations) {
        info.sendInformationRefutations(genericMoveList);
      }

      // Show multi pv
      if (showPvNumber > 1) {
        assert currentMoveNumber <= showPvNumber || lastMultiPv != null;
        if (currentMoveNumber <= showPvNumber || pv.compareTo(lastMultiPv) < 0) {
          PriorityQueue<PrincipalVariation> tempPvList = new PriorityQueue<>(multiPvMap.values());
          for (int i = 1; i <= showPvNumber && !tempPvList.isEmpty(); i++) {
            lastMultiPv = tempPvList.remove();
            sendInformation(lastMultiPv, i);
          }
        }
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        bestMove = move;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          bestPv = pv;
          hashType = Score.EXACT;
          alpha = value;

          if (depth > 1 && showPvNumber <= 1) {
            // Send pv information for depth > 1
            // Print the best move as soon as we get a new one
            // This is really an optimistic assumption
            sendInformation(bestPv, 1);
          }

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Score.BETA;
            break;
          }
        }
      }

      if (showPvNumber > 1) {
        // Reset alpha to get the real value of the next move
        assert oldAlpha == -CHECKMATE;
        alpha = oldAlpha;
      }
    }

    if (!(stopped && canStop)) {
      transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, false, height);
    }

    if (depth == 1 && showPvNumber <= 1 && bestPv != null) {
      // Send pv information for depth 1
      // On depth 1 we have no move ordering available
      // To reduce the output we only print the best move here
      sendInformation(bestPv, 1);
    }

    if (showPvNumber <= 1 && bestPv == null && firstPv != null) {
      // We have a fail low
      assert oldAlpha == alpha;

      PrincipalVariation resultPv = new PrincipalVariation(
        firstPv.moveNumber,
        firstPv.value,
        firstPv.type,
        firstPv.sortValue,
        firstPv.pv,
        firstPv.depth,
        info.currentMaxDepth,
        transpositionTable.getPermillUsed(),
        info.getCurrentNps(),
        System.currentTimeMillis() - info.totalTimeStart,
        info.totalNodes);
      sendInformation(resultPv, 1);
    }

    moveResult.bestMove = bestMove;
    moveResult.resultValue = bestValue;
    moveResult.value = hashType;
    moveResult.moveNumber = currentMoveNumber;

    if (Configuration.useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        for (int i = rootMoveList.head; i < rootMoveList.tail; i++) {
          if (rootMoveList.move[i] == entry.move) {
            rootMoveList.value[i] = INFINITY;
            break;
          }
        }
      }
    }

    MoveSorter.sort(rootMoveList);

    return bestValue;
  }

  private int alphaBeta(int depth, int alpha, int beta, int height, boolean pvNode, boolean doNull) {
    // We are at a leaf/horizon. So calculate that value.
    if (depth <= 0) {
      // Descend into quiescent
      return quiescent(0, alpha, beta, height, pvNode, true);
    }

    updateSearch(height);

    // Abort conditions
    if ((stopped && canStop) || height == MAX_HEIGHT) {
      return evaluation.evaluate(board);
    }

    // Check the repetition table and fifty move rule
    if (board.isRepetition() || board.halfMoveClock >= 100) {
      return DRAW;
    }

    //## BEGIN Mate Distance Pruning
    if (Configuration.useMateDistancePruning) {
      int value = -CHECKMATE + height;
      if (value > alpha) {
        alpha = value;
        if (value >= beta) {
          return value;
        }
      }
      value = -(-CHECKMATE + height + 1);
      if (value < beta) {
        beta = value;
        if (value <= alpha) {
          return value;
        }
      }
    }
    //## ENDOF Mate Distance Pruning

    // Check the transposition table first
    int transpositionMove = Move.NOMOVE;
    boolean mateThreat = false;
    if (Configuration.useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        transpositionMove = entry.move;
        mateThreat = entry.mateThreat;

        if (!pvNode && entry.depth >= depth) {
          int value = entry.getValue(height);
          int type = entry.type;

          switch (type) {
            case Score.BETA:
              if (value >= beta) {
                return value;
              }
              break;
            case Score.ALPHA:
              if (value <= alpha) {
                return value;
              }
              break;
            case Score.EXACT:
              return value;
            default:
              assert false;
              break;
          }
        }
      }
    }

    // Get the attack
    // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
    // idea.
    Attack attack = board.getAttack(board.activeColor);
    boolean isCheck = attack.isCheck();

    //## BEGIN Null-Move Pruning
    // Notes: Ideas from http://www.cs.biu.ac.il/~davoudo/pubs/vrfd_null.html
    int evalValue = INFINITY;
    if (Configuration.useNullMovePruning) {
      if (!pvNode
        && depth >= NULLMOVE_DEPTH
        && doNull
        && !isCheck
        && !mateThreat
        && Evaluation.getGamePhase(board) != IntGamePhase.ENDGAME
        && (evalValue = evaluation.evaluate(board)) >= beta) {
        // Depth reduction
        int newDepth = depth - 1 - NULLMOVE_REDUCTION;

        // Make the null move
        board.makeMove(Move.NULLMOVE);
        int value = -alphaBeta(newDepth, -beta, -beta + 1, height + 1, false, false);
        board.undoMove(Move.NULLMOVE);

        // Verify on beta exceeding
        if (Configuration.useVerifiedNullMovePruning) {
          if (depth > NULLMOVE_VERIFICATIONREDUCTION) {
            if (value >= beta) {
              newDepth = depth - NULLMOVE_VERIFICATIONREDUCTION;

              // Verify
              value = alphaBeta(newDepth, alpha, beta, height, true, false);

              if (value >= beta) {
                // Cut-off

                return value;
              }
            }
          }
        }

        // Check for mate threat
        if (value < -CHECKMATE_THRESHOLD) {
          mateThreat = true;
        }

        if (value >= beta) {
          // Do not return unproven mate values
          if (value > CHECKMATE_THRESHOLD) {
            value = CHECKMATE_THRESHOLD;
          }

          if (!(stopped && canStop)) {
            // Store the value into the transposition table
            transpositionTable.put(board.zobristCode, depth, value, Score.BETA, Move.NOMOVE, mateThreat, height);
          }

          return value;
        }
      }
    }
    //## ENDOF Null-Move Forward Pruning

    // Initialize
    int hashType = Score.ALPHA;
    int bestValue = -INFINITY;
    int bestMove = Move.NOMOVE;
    int searchedMoves = 0;

    //## BEGIN Internal Iterative Deepening
    if (Configuration.useInternalIterativeDeepening) {
      if (pvNode
        && depth >= IID_DEPTH
        && transpositionMove == Move.NOMOVE
        // alpha is not equal the initial -CHECKMATE anymore, because of depth >= IID_DEPTH
        // so alpha has a real value. Don't do IID if it's a checkmate value
        && Math.abs(alpha) < CHECKMATE_THRESHOLD) {
        int oldAlpha = alpha;
        int oldBeta = beta;
        alpha = -CHECKMATE;
        beta = CHECKMATE;

        for (int newDepth = 1; newDepth < depth; newDepth++) {
          alphaBeta(newDepth, alpha, beta, height, true, false);

          if (stopped && canStop) {
            return oldAlpha;
          }
        }

        alpha = oldAlpha;
        beta = oldBeta;

        if (pvList[height].size() > 0) {
          // Hopefully we have a transposition move now
          transpositionMove = pvList[height].move[pvList[height].head];
        }
      }
    }
    //## ENDOF Internal Iterative Deepening

    // Initialize the move generator
    moveGenerator.initializeMain(attack, height, transpositionMove);

    // Initialize Single-Response Extension
    boolean isSingleReply = isCheck && attack.numberOfMoves == 1;

    int move = Move.NOMOVE;
    while ((move = moveGenerator.getNextMove()) != Move.NOMOVE) {
      //## BEGIN Minor Promotion Pruning
      if (Configuration.useMinorPromotionPruning
        && !analyzeMode
        && Move.getType(move) == Move.Type.PAWNPROMOTION
        && Move.getPromotion(move) != IntChessman.QUEEN) {
        assert Move.getPromotion(move) == IntChessman.ROOK || Move.getPromotion(move) == IntChessman.BISHOP || Move.getPromotion(move) == IntChessman.KNIGHT;
        continue;
      }
      //## ENDOF Minor Promotion Pruning

      // Extension
      int newDepth = getNewDepth(depth, move, isSingleReply, mateThreat);

      //## BEGIN Extended Futility Pruning
      // Notes: Ideas from http://supertech.lcs.mit.edu/~heinz/dt/node18.html
      if (Configuration.useExtendedFutilityPruning) {
        if (!pvNode
          && depth == 2
          && newDepth == 1
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && !isDangerousMove(move)) {
          assert !board.isCheckingMove(move);
          assert Move.getType(move) != Move.Type.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          if (evalValue == INFINITY) {
            // Store evaluation
            evalValue = evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_PREFRONTIERMARGIN;

          // Add the target value to the eval
          int target = Move.getTargetPiece(move);
          if (target != IntPiece.NOPIECE) {
            value += Evaluation.getValueFromPiece(target);
          }

          // If we cannot reach alpha do not look at the move
          if (value <= alpha) {
            if (value > bestValue) {
              bestValue = value;
            }
            continue;
          }
        }
      }
      //## ENDOF Extended Futility Pruning

      //## BEGIN Futility Pruning
      // Notes: Ideas from http://supertech.lcs.mit.edu/~heinz/dt/node18.html
      if (Configuration.useFutilityPruning) {
        if (!pvNode
          && depth == 1
          && newDepth == 0
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && !isDangerousMove(move)) {
          assert !board.isCheckingMove(move);
          assert Move.getType(move) != Move.Type.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          if (evalValue == INFINITY) {
            // Store evaluation
            evalValue = evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_FRONTIERMARGIN;

          // Add the target value to the eval
          int target = Move.getTargetPiece(move);
          if (target != IntPiece.NOPIECE) {
            value += Evaluation.getValueFromPiece(target);
          }

          // If we cannot reach alpha do not look at the move
          if (value <= alpha) {
            if (value > bestValue) {
              bestValue = value;
            }
            continue;
          }
        }
      }
      //## ENDOF Futility Pruning

      //## BEGIN Late Move Reduction
      // Notes: Ideas from: http://www.glaurungchess.com/lmr.html
      boolean reduced = false;
      if (Configuration.useLateMoveReduction) {
        if (!pvNode
          && searchedMoves >= LMR_MOVENUMBER_MINIMUM
          && depth >= LMR_DEPTH
          && newDepth < depth
          && !isCheck
          && (Configuration.useCheckExtension || !board.isCheckingMove(move))
          && Move.getTargetPiece(move) == IntPiece.NOPIECE
          && !isDangerousMove(move)) {
          assert !board.isCheckingMove(move);
          assert Move.getType(move) != Move.Type.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          newDepth--;
          reduced = true;
        }
      }
      //## ENDOF Late Move Reduction

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (!pvNode || bestValue == -INFINITY) {
        // First move
        value = -alphaBeta(newDepth, -beta, -alpha, height + 1, pvNode, true);
      } else {
        if (newDepth >= depth) {
          value = -alphaBeta(depth - 1, -alpha - 1, -alpha, height + 1, false, true);
        } else {
          value = -alphaBeta(newDepth, -alpha - 1, -alpha, height + 1, false, true);
        }
        if (value > alpha && value < beta) {
          // Research again
          value = -alphaBeta(newDepth, -beta, -alpha, height + 1, true, true);
        }
      }
      //## ENDOF Principal Variation Search

      //## BEGIN Late Move Reduction Research
      if (Configuration.useLateMoveReductionResearch) {
        if (reduced && value >= beta) {
          // Research with original depth
          newDepth++;
          value = -alphaBeta(newDepth, -beta, -alpha, height + 1, pvNode, true);
        }
      }
      //## ENDOF Late Move Reduction Research

      // Undo move
      board.undoMove(move);

      if (stopped && canStop) {
        break;
      }

      // Update
      searchedMoves++;

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        bestMove = move;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          hashType = Score.EXACT;
          alpha = value;

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Score.BETA;
            break;
          }
        }
      }
    }

    moveGenerator.destroy();

    // If we cannot move, check for checkmate and stalemate.
    if (bestValue == -INFINITY) {
      if (isCheck) {
        // We have a check mate. This is bad for us, so return a -CHECKMATE.
        hashType = Score.EXACT;
        bestValue = -CHECKMATE + height;
      } else {
        // We have a stale mate. Return the draw value.
        hashType = Score.EXACT;
        bestValue = DRAW;
      }
    }

    if (!(stopped && canStop)) {
      if (bestMove != Move.NOMOVE) {
        addGoodMove(bestMove, depth, height);
      }
      transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, mateThreat, height);
    }

    return bestValue;
  }

  private int quiescent(int checkingDepth, int alpha, int beta, int height, boolean pvNode, boolean useTranspositionTable) {
    updateSearch(height);

    // Abort conditions
    if ((stopped && canStop) || height == MAX_HEIGHT) {
      return evaluation.evaluate(board);
    }

    // Check the repetition table and fifty move rule
    if (board.isRepetition() || board.halfMoveClock >= 100) {
      return DRAW;
    }

    //## BEGIN Mate Distance Pruning
    if (Configuration.useMateDistancePruning) {
      int value = -CHECKMATE + height;
      if (value > alpha) {
        alpha = value;
        if (value >= beta) {
          return value;
        }
      }
      value = -(-CHECKMATE + height + 1);
      if (value < beta) {
        beta = value;
        if (value <= alpha) {
          return value;
        }
      }
    }
    //## ENDOF Mate Distance Pruning

    // Check the transposition table first
    if (Configuration.useTranspositionTable && useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = transpositionTable.get(board.zobristCode);
      if (entry != null) {
        assert entry.depth >= checkingDepth;
        int value = entry.getValue(height);
        int type = entry.type;

        switch (type) {
          case Score.BETA:
            if (value >= beta) {
              return value;
            }
            break;
          case Score.ALPHA:
            if (value <= alpha) {
              return value;
            }
            break;
          case Score.EXACT:
            return value;
          default:
            assert false;
            break;
        }
      }
    }

    // Get the attack
    // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
    // idea.
    Attack attack = board.getAttack(board.activeColor);
    boolean isCheck = attack.isCheck();

    // Initialize
    int hashType = Score.ALPHA;
    int bestValue = -INFINITY;
    int evalValue = INFINITY;

    if (!isCheck) {
      // Stand pat
      int value = evaluation.evaluate(board);

      // Store evaluation
      evalValue = value;

      // Pruning
      bestValue = value;

      // Do we have a better value?
      if (value > alpha) {
        hashType = Score.EXACT;
        alpha = value;

        // Is the value higher than beta?
        if (value >= beta) {
          // Cut-off

          hashType = Score.BETA;

          if (useTranspositionTable) {
            assert checkingDepth == 0;
            transpositionTable.put(board.zobristCode, 0, bestValue, hashType, Move.NOMOVE, false, height);
          }

          return bestValue;
        }
      }
    } else {
      // Check Extension
      checkingDepth++;
    }

    // Initialize the move generator
    moveGenerator.initializeQuiescent(attack, checkingDepth >= 0);

    int move = Move.NOMOVE;
    while ((move = moveGenerator.getNextMove()) != Move.NOMOVE) {
      //## BEGIN Futility Pruning
      if (Configuration.useDeltaPruning) {
        if (!pvNode
          && !isCheck
          && !board.isCheckingMove(move)
          && !isDangerousMove(move)) {
          assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
          assert Move.getType(move) != Move.Type.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          int value = evalValue + FUTILITY_QUIESCENTMARGIN;

          // Add the target value to the eval
          value += Evaluation.getValueFromPiece(Move.getTargetPiece(move));

          // If we cannot reach alpha do not look at the move
          if (value <= alpha) {
            if (value > bestValue) {
              bestValue = value;
            }
            continue;
          }
        }
      }
      //## ENDOF Futility Pruning

      // Do move
      board.makeMove(move);

      // Recurse into Quiescent
      int value = -quiescent(checkingDepth - 1, -beta, -alpha, height + 1, pvNode, false);

      // Undo move
      board.undoMove(move);

      if (stopped && canStop) {
        break;
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          hashType = Score.EXACT;
          alpha = value;

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Score.BETA;
            break;
          }
        }
      }
    }

    moveGenerator.destroy();

    if (bestValue == -INFINITY) {
      assert isCheck;

      // We have a check mate. This is bad for us, so return a -CHECKMATE.
      bestValue = -CHECKMATE + height;
    }

    if (useTranspositionTable) {
      if (!(stopped && canStop)) {
        transpositionTable.put(board.zobristCode, 0, bestValue, hashType, Move.NOMOVE, false, height);
      }
    }

    return bestValue;
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

    assert (Move.getTargetPosition(move) != board.captureSquare) || (Move.getTargetPiece(move) != IntPiece.NOPIECE);

    //## Recapture Extension
    if (Configuration.useRecaptureExtension
      && Move.getTargetPosition(move) == board.captureSquare
      && MoveSee.seeMove(move, IntPiece.getColor(Move.getOriginPiece(move))) > 0) {
      newDepth++;
    }

    //## Check Extension
    else if (Configuration.useCheckExtension
      && board.isCheckingMove(move)) {
      newDepth++;
    }

    //## Pawn Extension
    else if (Configuration.usePawnExtension
      && IntPiece.getChessman(Move.getOriginPiece(move)) == IntChessman.PAWN
      && Position.getRelativeRank(Move.getTargetPosition(move), board.activeColor) == IntRank.R7) {
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
    if (Evaluation.materialCount(board.activeColor, board) == 0
      && Evaluation.materialCount(IntColor.opposite(board.activeColor), board) == 1
      && Move.getTargetPiece(move) != IntPiece.NOPIECE
      && IntPiece.getChessman(Move.getTargetPiece(move)) != IntChessman.PAWN) {
      newDepth++;
    }

    return newDepth;
  }

  private static boolean isDangerousMove(int move) {
    int chessman = IntPiece.getChessman(Move.getOriginPiece(move));
    int relativeRank = Position.getRelativeRank(Move.getTargetPosition(move), board.activeColor);
    if (chessman == IntChessman.PAWN && relativeRank >= IntRank.R7) {
      return true;
    }

    int target = Move.getTargetPiece(move);
    if (target != IntPiece.NOPIECE && IntPiece.getChessman(target) == IntChessman.QUEEN) {
      return true;
    }

    return false;
  }

  private static void addPv(MoveList destination, MoveList source, int move) {
    assert destination != null;
    assert source != null;
    assert move != Move.NOMOVE;

    destination.clear();

    destination.move[destination.tail++] = move;

    for (int i = source.head; i < source.tail; i++) {
      destination.move[destination.tail++] = source.move[i];
    }
  }

  private static void addGoodMove(int move, int depth, int height) {
    assert move != Move.NOMOVE;

    if (Move.getTargetPiece(move) != IntPiece.NOPIECE) {
      return;
    }

    int type = Move.getType(move);
    if (type == Move.Type.PAWNPROMOTION || type == Move.Type.NULL) {
      return;
    }

    assert type != Move.Type.ENPASSANT;

    killerTable.add(move, height);
    historyTable.add(move, depth);
  }

}
