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
package com.fluxchess.flux;

import com.fluxchess.jcpi.models.GenericMove;

import java.util.*;
import java.util.concurrent.Semaphore;

public final class Search implements Runnable {

  /**
   * The maximum number of moves.
   */
  public static final int MAX_MOVES = 4096;

  private static final int ASPIRATIONWINDOW = 20;
  private static final int ASPIRATIONWINDOW_ADJUSTMENT = 200;

  private static final int TIMEEXTENSION_MARGIN = 30;

  private static final int NULLMOVE_DEPTH = 2;
  private static final int NULLMOVE_REDUCTION;
  private static final int NULLMOVE_VERIFICATIONREDUCTION = 3;

  private static final int IID_DEPTH = 2;

  private static final int LMR_DEPTH = 3;
  private static final int LMR_MOVENUMBER_MINIMUM = 3;

  private static final int FUTILITY_FRONTIERMARGIN = 2 * Piece.VALUE_PAWN;
  private static final int FUTILITY_PREFRONTIERMARGIN = Piece.VALUE_ROOK;
  private static final int FUTILITY_QUIESCENTMARGIN = Piece.VALUE_PAWN;

  // Objects
  private InformationTimer info;
  private Thread thread = new Thread(this);
  private Semaphore semaphore = new Semaphore(0);

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
  private final long[] searchClock = new long[Color.ARRAY_DIMENSION];
  private final long[] searchClockIncrement = new long[Color.ARRAY_DIMENSION];
  private int searchMovesToGo = 0;
  private final MoveList searchMoveList = new MoveList();

  // Analyze parameters
  private int showPvNumber = 1;

  // Search logic
  private Evaluation evaluation;
  private static Position board;
  private final int myColor;

  // Search tables
  private TranspositionTable transpositionTable;
  private static KillerTable killerTable;
  private static HistoryTable historyTable;

  // Search information
  private static final MoveList[] pvList = new MoveList[Depth.MAX_HEIGHT + 1];
  private static final HashMap<Integer, PrincipalVariation> multiPvMap = new HashMap<Integer, PrincipalVariation>(MAX_MOVES);
  private Result bestResult = null;
  private final int[] timeTable;

  private static final class Result {
    public int bestMove = Move.NOMOVE;
    public int ponderMove = Move.NOMOVE;
    public int value = Bound.NOVALUE;
    public int resultValue = -Value.INFINITY;
    public long time = -1;
    public int moveNumber = 0;
    public int depth = 0;

    /**
     * Creates a new Result.
     */
    public Result() {
    }
  }

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

  public Search(Evaluation newEvaluation, Position newBoard, TranspositionTable newTranspositionTable, InformationTimer newInfo, int[] timeTable) {
    assert newEvaluation != null;
    assert newBoard != null;
    assert newTranspositionTable != null;
    assert newInfo != null;

    this.analyzeMode = Configuration.analyzeMode;

    this.evaluation = newEvaluation;
    board = newBoard;
    this.myColor = newBoard.activeColor;

    this.transpositionTable = newTranspositionTable;
    if (this.analyzeMode) {
      this.transpositionTable.increaseAge();
    }
    killerTable = new KillerTable();
    historyTable = new HistoryTable();

    new MoveGenerator(newBoard, killerTable, historyTable);
    new See(newBoard);

    this.info = newInfo;
    this.info.setSearch(this);

    this.timeTable = timeTable;

    multiPvMap.clear();
  }

  public void run() {
    this.stopped = false;
    this.canStop = false;
    this.bestResult = new Result();

    // Set the time managemnet
    if (this.doTimeManagement) {
      setTimeManagement();
      this.searchTimeStart = System.currentTimeMillis();
    }
    if (this.searchTime > 0) {
      startTimer();
    }

    // Go...
    this.semaphore.release();
    this.info.start();
    Result moveResult = getBestMove();
    this.info.stop();

    // Cancel the timer
    if (this.timer != null) {
      this.timer.cancel();
    }

    // Send the result
    if (moveResult.bestMove != Move.NOMOVE) {
      if (moveResult.ponderMove != Move.NOMOVE) {
        this.info.sendBestMove(Move.toCommandMove(moveResult.bestMove), Move.toCommandMove(moveResult.ponderMove));
      } else {
        this.info.sendBestMove(Move.toCommandMove(moveResult.bestMove), null);
      }
    } else {
      this.info.sendBestMove(null, null);
    }

    // Cleanup manually
    this.transpositionTable = null;
    this.info = null;
    this.evaluation = null;
  }

  public void start() {
    this.thread.start();
    try {
      // Wait for initialization
      this.semaphore.acquire();
    } catch (InterruptedException e) {
      // Do nothing
    }
  }

  public void stop() {
    this.stopped = true;
    this.canStop = true;
    try {
      // Wait for the thread to die
      this.thread.join();
    } catch (InterruptedException e) {
      // Do nothing
    }
  }

  public void ponderhit() {
    // Enable time management
    this.doTimeManagement = true;

    // Set time management parameters
    setTimeManagement();
    this.searchTimeStart = System.currentTimeMillis();

    // Start our hard stop timer
    startTimer();

    // Check whether we have already a result
    assert this.bestResult != null;
    if (this.bestResult.bestMove != Move.NOMOVE) {
      this.canStop = true;

      // Check if we have a checkmate
      if (Math.abs(this.bestResult.resultValue) > Value.CHECKMATE_THRESHOLD
          && this.bestResult.depth >= (Value.CHECKMATE - Math.abs(this.bestResult.resultValue))) {
        this.stopped = true;
      }

      // Check if we have only one move to make
      else if (this.bestResult.moveNumber == 1) {
        this.stopped = true;
      }
    }
  }

  public boolean isStopped() {
    return !this.thread.isAlive();
  }

  public void setSearchDepth(int searchDepth) {
    assert searchDepth > 0;

    this.searchDepth = searchDepth;
    if (this.searchDepth > Depth.MAX_DEPTH) {
      this.searchDepth = Depth.MAX_DEPTH;
    }
    this.doTimeManagement = false;
  }

  public void setSearchNodes(long searchNodes) {
    assert searchNodes > 0;

    this.searchNodes = searchNodes;
    this.searchDepth = Depth.MAX_DEPTH;
    this.doTimeManagement = false;
  }

  public void setSearchTime(long searchTime) {
    assert searchTime > 0;

    this.searchTime = searchTime;
    this.searchTimeHard = this.searchTime;
    this.searchDepth = Depth.MAX_DEPTH;
    this.doTimeManagement = false;
  }

  public void setSearchClock(int side, long timeLeft) {
    assert timeLeft > 0;

    this.searchClock[side] = timeLeft;
  }

  public void setSearchClockIncrement(int side, long timeIncrement) {
    assert timeIncrement > 0;

    this.searchClockIncrement[side] = timeIncrement;
  }

  public void setSearchMovesToGo(int searchMovesToGo) {
    assert searchMovesToGo > 0;

    this.searchMovesToGo = searchMovesToGo;
  }

  public void setSearchInfinite() {
    this.searchDepth = Depth.MAX_DEPTH;
    this.doTimeManagement = false;
    this.analyzeMode = true;
  }

  public void setSearchPonder() {
    this.searchDepth = Depth.MAX_DEPTH;
    this.doTimeManagement = false;
  }

  public void setSearchMoveList(List<GenericMove> moveList) {
    for (GenericMove move : moveList) {
      this.searchMoveList.moves[this.searchMoveList.tail++] = Move.convertMove(move, board);
    }
  }

  private void startTimer() {
    // Only start timer if we have a hard time limit
    if (this.searchTimeHard > 0) {
      this.timer = new Timer(true);
      this.timer.schedule(new TimerTask() {
        public void run() {
          stop();
        }
      }, this.searchTimeHard);
    }
  }

  private void setTimeManagement() {
    // Dynamic time allocation
    this.searchDepth = Depth.MAX_DEPTH;

    if (this.searchClock[this.myColor] > 0) {
      // We received a time control.

      // Check the moves to go
      if (this.searchMovesToGo < 1 || this.searchMovesToGo > 40) {
        this.searchMovesToGo = 40;
      }

      // Check the increment
      if (this.searchClockIncrement[this.myColor] < 1) {
        this.searchClockIncrement[this.myColor] = 0;
      }

      // Set the maximum search time
      long maxSearchTime = (long) (this.searchClock[this.myColor] * 0.95) - 1000L;
      if (maxSearchTime < 0) {
        maxSearchTime = 0;
      }

      // Set the search time
      this.searchTime = (maxSearchTime + (this.searchMovesToGo - 1) * this.searchClockIncrement[this.myColor]) / this.searchMovesToGo;
      if (this.searchTime > maxSearchTime) {
        this.searchTime = maxSearchTime;
      }

      // Set the hard limit search time
      this.searchTimeHard = (maxSearchTime + (this.searchMovesToGo - 1) * this.searchClockIncrement[this.myColor]) / 8;
      if (this.searchTimeHard < this.searchTime) {
        this.searchTimeHard = this.searchTime;
      }
      if (this.searchTimeHard > maxSearchTime) {
        this.searchTimeHard = maxSearchTime;
      }
    } else {
      // We received no time control. Search for 2 seconds.
      this.searchTime = 2000L;

      // Stop hard after +50% of the allocated time
      this.searchTimeHard = this.searchTime + this.searchTime / 2;
    }
  }

  private void sendInformation(PrincipalVariation pv, int pvNumber) {
    if (Math.abs(pv.value) > Value.CHECKMATE_THRESHOLD) {
      // Calculate the mate distance
      int mateDepth = Value.CHECKMATE - Math.abs(pv.value);
      this.info.sendInformationMate(pv, Integer.signum(pv.value) * (mateDepth + 1) / 2, pvNumber);
    } else {
      this.info.sendInformationCentipawns(pv, pvNumber);
    }
  }

  private Result getBestMove() {
    //## BEGIN Root Move List
    MoveList rootMoveList = new MoveList();

    PrincipalVariation pv = null;
    int transpositionMove = Move.NOMOVE;
    int transpositionDepth = -1;
    int transpositionValue = 0;
    int transpositionType = Bound.NOVALUE;
    if (Configuration.useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = this.transpositionTable.get(board.zobristCode);
      if (entry != null) {
        List<GenericMove> moveList = this.transpositionTable.getMoveList(board, entry.depth, new ArrayList<GenericMove>());
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

    if (this.searchMoveList.getLength() == 0) {
      MoveGenerator.initializeMain(attack, 0, transpositionMove);

      int move = Move.NOMOVE;
      while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
        rootMoveList.moves[rootMoveList.tail++] = move;
      }

      MoveGenerator.destroy();
    } else {
      for (int i = this.searchMoveList.head; i < this.searchMoveList.tail; i++) {
        rootMoveList.moves[rootMoveList.tail++] = this.searchMoveList.moves[i];
      }
    }

    // Check if we cannot move
    if (rootMoveList.getLength() == 0) {
      // This position is a checkmate or stalemate
      return this.bestResult;
    }

    // Adjust pv number
    this.showPvNumber = Configuration.showPvNumber;
    if (Configuration.showPvNumber > rootMoveList.getLength()) {
      this.showPvNumber = rootMoveList.getLength();
    }
    //## ENDOF Root Move List

    int alpha = -Value.CHECKMATE;
    int beta = Value.CHECKMATE;

    int initialDepth = 1;
    int equalResults = 0;
    if (!this.analyzeMode
        && transpositionDepth > 1
        && transpositionType == Bound.EXACT
        && Math.abs(transpositionValue) < Value.CHECKMATE_THRESHOLD
        && pv != null) {
      this.bestResult.bestMove = transpositionMove;
      this.bestResult.resultValue = transpositionValue;
      this.bestResult.value = transpositionType;
      this.bestResult.time = 0;
      this.bestResult.moveNumber = rootMoveList.getLength();

      initialDepth = transpositionDepth;
      equalResults = transpositionDepth - 2;
    }

    //## BEGIN Iterative Deepening
    for (int currentDepth = initialDepth; currentDepth <= this.searchDepth; currentDepth++) {
      this.info.setCurrentDepth(currentDepth);
      this.info.sendInformationDepth();

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
      // Notes: Ideas from Ed Schröder. We open the aspiration window
      // twice, as the first adjustment should be wide enough.
      if (!(this.stopped && this.canStop) && Configuration.useAspirationWindows && this.showPvNumber <= 1 && currentDepth >= transpositionDepth) {
        // First adjustment
        if (value <= alpha || value >= beta) {
          if (value <= alpha) {
            alpha -= ASPIRATIONWINDOW_ADJUSTMENT;
          } else if (value >= beta) {
            beta += ASPIRATIONWINDOW_ADJUSTMENT;
          } else {
            assert false : "Alpha: " + alpha + ", Beta: " + beta + ", Value: " + value;
          }
          if (alpha < -Value.CHECKMATE) {
            alpha = -Value.CHECKMATE;
          }
          if (beta > Value.CHECKMATE) {
            beta = Value.CHECKMATE;
          }

          // Do the Alpha-Beta search again
          value = alphaBetaRoot(currentDepth, alpha, beta, 0, rootMoveList, isCheck, moveResult);

          if (!(this.stopped && this.canStop)) {
            // Second adjustment
            // Open window to full width
            if (value <= alpha || value >= beta) {
              alpha = -Value.CHECKMATE;
              beta = Value.CHECKMATE;

              // Do the Alpha-Beta search again
              value = alphaBetaRoot(currentDepth, alpha, beta, 0, rootMoveList, isCheck, moveResult);
            }
          }
        }

        // Adjust aspiration window
        alpha = value - ASPIRATIONWINDOW;
        beta = value + ASPIRATIONWINDOW;
        if (alpha < -Value.CHECKMATE) {
          alpha = -Value.CHECKMATE;
        }
        if (beta > Value.CHECKMATE) {
          beta = Value.CHECKMATE;
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
        if (moveResult.bestMove == this.bestResult.bestMove) {
          equalResults++;
        } else {
          equalResults = 0;
        }

        if (this.doTimeManagement) {
          //## BEGIN Time Control
          boolean timeExtended = false;

          // Check value change
          if (moveResult.resultValue + TIMEEXTENSION_MARGIN < this.bestResult.resultValue
              || moveResult.resultValue - TIMEEXTENSION_MARGIN > this.bestResult.resultValue) {
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
          if (this.searchTimeStart + this.searchTimeHard < System.currentTimeMillis() + nextIterationTime) {
            // Clear table
            if (currentDepth == initialDepth) {
              for (int i = currentDepth + 1; i < this.timeTable.length; i++) {
                this.timeTable[i] = 0;
              }
            } else {
              for (int i = currentDepth + 1; i < this.timeTable.length; i++) {
                this.timeTable[i] += this.timeTable[i - 1] * 2;
                this.timeTable[i] /= 2;
              }
            }
            this.stopFlag = true;
          }

          // Check time limit
          else if (!timeExtended
              && this.searchTimeStart + this.searchTime < System.currentTimeMillis() + nextIterationTime) {
            // Clear table
            if (currentDepth == initialDepth) {
              for (int i = currentDepth + 1; i < this.timeTable.length; i++) {
                this.timeTable[i] = 0;
              }
            } else {
              for (int i = currentDepth + 1; i < this.timeTable.length; i++) {
                this.timeTable[i] += this.timeTable[i - 1] * 2;
                this.timeTable[i] /= 2;
              }
            }
            this.stopFlag = true;
          }

          // Check if this is an easy recapture
          else if (!timeExtended
              && Move.getEnd(moveResult.bestMove) == board.captureSquare
              && Piece.getValueFromChessman(Move.getTarget(moveResult.bestMove)) >= Piece.VALUE_KNIGHT
              && equalResults > 4) {
            this.stopFlag = true;
          }

          // Check if we have a checkmate
          else if (Math.abs(value) > Value.CHECKMATE_THRESHOLD
              && currentDepth >= (Value.CHECKMATE - Math.abs(value))) {
            this.stopFlag = true;
          }

          // Check if we have only one move to make
          else if (moveResult.moveNumber == 1) {
            this.stopFlag = true;
          }
          //## ENDOF Time Control
        }

        // Update the best result.
        this.bestResult = moveResult;

        if (pvList[0].tail > 1) {
          // We found a line. Set the ponder move.
          this.bestResult.ponderMove = pvList[0].moves[1];
        }
      } else {
        // We found no best move.
        // Perhaps we have a checkmate or we got a stop request?
        break;
      }

      // Check if we can stop the search
      if (this.stopFlag) {
        break;
      }

      this.canStop = true;

      if (this.stopped) {
        break;
      }
    }
    //## ENDOF Iterative Deepening

    // Update all stats
    this.info.sendInformationSummary();

    return this.bestResult;
  }

  private void updateSearch(int height) {
    assert this.transpositionTable.getPermillUsed() >= 0 && this.transpositionTable.getPermillUsed() <= 1000;

    this.info.totalNodes++;
    this.info.setCurrentMaxDepth(height);
    this.info.sendInformationStatus();

    if (this.searchNodes > 0 && this.searchNodes <= this.info.totalNodes) {
      // Hard stop on number of nodes
      this.stopped = true;
    }

    // Reset
    pvList[height].resetList();
  }

  private int alphaBetaRoot(int depth, int alpha, int beta, int height, MoveList rootMoveList, boolean isCheck, Result moveResult) {
    updateSearch(height);

    // Abort conditions
    if ((this.stopped && this.canStop) || height == Depth.MAX_HEIGHT) {
      return this.evaluation.evaluate(board);
    }

    // Initialize
    int hashType = Bound.ALPHA;
    int bestValue = -Value.INFINITY;
    int bestMove = Move.NOMOVE;
    int oldAlpha = alpha;
    PrincipalVariation lastMultiPv = null;
    PrincipalVariation bestPv = null;
    PrincipalVariation firstPv = null;

    // Initialize the move number
    int currentMoveNumber = 0;

    // Initialize Single-Response Extension
    boolean isSingleReply;
    if (isCheck && rootMoveList.getLength() == 1) {
      isSingleReply = true;
    } else {
      isSingleReply = false;
    }

    for (int j = rootMoveList.head; j < rootMoveList.tail; j++) {
      int move = rootMoveList.moves[j];

      // Update the information if we evaluate a new move.
      currentMoveNumber++;
      this.info.sendInformationMove(Move.toCommandMove(move), currentMoveNumber);

      // Extension
      int newDepth = getNewDepth(depth, move, isSingleReply, false);

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (bestValue == -Value.INFINITY) {
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

      if (this.stopped && this.canStop) {
        break;
      }

      // Store value
      int sortValue;
      int moveType;
      if (value <= alpha) {
        value = alpha;
        moveType = Bound.ALPHA;
        rootMoveList.values[j] = oldAlpha;
        sortValue = -Value.INFINITY;
      } else if (value >= beta) {
        value = beta;
        moveType = Bound.BETA;
        rootMoveList.values[j] = beta;
        sortValue = Value.INFINITY;
      } else {
        moveType = Bound.EXACT;
        rootMoveList.values[j] = value;
        sortValue = value;
      }

      // Add pv to list
      List<GenericMove> commandMoveList = new ArrayList<GenericMove>();
      commandMoveList.add(Move.toCommandMove(move));
      for (int i = pvList[height + 1].head; i < pvList[height + 1].tail; i++) {
        commandMoveList.add(Move.toCommandMove(pvList[height + 1].moves[i]));
      }
      PrincipalVariation pv = new PrincipalVariation(
          currentMoveNumber,
          value,
          moveType,
          sortValue,
          commandMoveList,
          this.info.currentDepth,
          this.info.currentMaxDepth,
          this.transpositionTable.getPermillUsed(),
          this.info.getCurrentNps(),
          System.currentTimeMillis() - this.info.totalTimeStart,
          this.info.totalNodes);
      multiPvMap.put(move, pv);

      // Save first pv
      if (currentMoveNumber == 1) {
        firstPv = pv;
      }

      // Show refutations
      if (Configuration.showRefutations) {
        this.info.sendInformationRefutations(commandMoveList);
      }

      // Show multi pv
      if (this.showPvNumber > 1) {
        assert currentMoveNumber <= this.showPvNumber || lastMultiPv != null;
        if (currentMoveNumber <= this.showPvNumber || pv.compareTo(lastMultiPv) < 0) {
          PriorityQueue<PrincipalVariation> tempPvList = new PriorityQueue<PrincipalVariation>(multiPvMap.values());
          for (int i = 1; i <= this.showPvNumber && !tempPvList.isEmpty(); i++) {
            lastMultiPv = tempPvList.remove();
            sendInformation(lastMultiPv, i);
          }
        }
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          bestMove = move;
          bestPv = pv;
          hashType = Bound.EXACT;
          alpha = value;

          if (depth > 1 && this.showPvNumber <= 1) {
            // Send pv information for depth > 1
            // Print the best move as soon as we get a new one
            // This is really an optimistic assumption
            sendInformation(bestPv, 1);
          }

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Bound.BETA;
            break;
          }
        }
      }

      if (this.showPvNumber > 1) {
        // Reset alpha to get the real value of the next move
        assert oldAlpha == -Value.CHECKMATE;
        alpha = oldAlpha;
      }
    }

    if (!(this.stopped && this.canStop)) {
      this.transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, false, height);
    }

    if (depth == 1 && this.showPvNumber <= 1 && bestPv != null) {
      // Send pv information for depth 1
      // On depth 1 we have no move ordering available
      // To reduce the output we only print the best move here
      sendInformation(bestPv, 1);
    }

    if (this.showPvNumber <= 1 && bestPv == null && firstPv != null) {
      // We have a fail low
      assert oldAlpha == alpha;

      PrincipalVariation resultPv = new PrincipalVariation(
          firstPv.moveNumber,
          firstPv.value,
          firstPv.type,
          firstPv.sortValue,
          firstPv.pv,
          firstPv.depth,
          this.info.currentMaxDepth,
          this.transpositionTable.getPermillUsed(),
          this.info.getCurrentNps(),
          System.currentTimeMillis() - this.info.totalTimeStart,
          this.info.totalNodes);
      sendInformation(resultPv, 1);
    }

    moveResult.bestMove = bestMove;
    moveResult.resultValue = bestValue;
    moveResult.value = hashType;
    moveResult.moveNumber = currentMoveNumber;

    if (Configuration.useTranspositionTable) {
      TranspositionTable.TranspositionTableEntry entry = this.transpositionTable.get(board.zobristCode);
      if (entry != null) {
        for (int i = rootMoveList.head; i < rootMoveList.tail; i++) {
          if (rootMoveList.moves[i] == entry.move) {
            rootMoveList.values[i] = Value.INFINITY;
            break;
          }
        }
      }
    }

    rootMoveList.sort();

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
    if ((this.stopped && this.canStop) || height == Depth.MAX_HEIGHT) {
      return this.evaluation.evaluate(board);
    }

    // Check the repetition table and fifty move rule
    if (board.isRepetition() || board.halfMoveClock >= 100) {
      return Value.DRAW;
    }

    //## BEGIN Mate Distance Pruning
    if (Configuration.useMateDistancePruning) {
      int value = -Value.CHECKMATE + height;
      if (value > alpha) {
        alpha = value;
        if (value >= beta) {
          return value;
        }
      }
      value = -(-Value.CHECKMATE + height + 1);
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
      TranspositionTable.TranspositionTableEntry entry = this.transpositionTable.get(board.zobristCode);
      if (entry != null) {
        transpositionMove = entry.move;
        mateThreat = entry.mateThreat;

        if (!pvNode && entry.depth >= depth) {
          int value = entry.getValue(height);
          int type = entry.type;

          switch (type) {
            case Bound.BETA:
              if (value >= beta) {
                return value;
              }
              break;
            case Bound.ALPHA:
              if (value <= alpha) {
                return value;
              }
              break;
            case Bound.EXACT:
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
    int evalValue = Value.INFINITY;
    if (Configuration.useNullMovePruning) {
      if (!pvNode
          && depth >= NULLMOVE_DEPTH
          && doNull
          && !isCheck
          && !mateThreat
          && board.getGamePhase() != GamePhase.ENDGAME
          && (evalValue = this.evaluation.evaluate(board)) >= beta) {
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
        if (value < -Value.CHECKMATE_THRESHOLD) {
          mateThreat = true;
        }

        if (value >= beta) {
          // Do not return unproven mate values
          if (value > Value.CHECKMATE_THRESHOLD) {
            value = Value.CHECKMATE_THRESHOLD;
          }

          if (!(this.stopped && this.canStop)) {
            // Store the value into the transposition table
            this.transpositionTable.put(board.zobristCode, depth, value, Bound.BETA, Move.NOMOVE, mateThreat, height);
          }

          return value;
        }
      }
    }
    //## ENDOF Null-Move Forward Pruning

    // Initialize
    int hashType = Bound.ALPHA;
    int bestValue = -Value.INFINITY;
    int bestMove = Move.NOMOVE;
    int searchedMoves = 0;

    //## BEGIN Internal Iterative Deepening
    if (Configuration.useInternalIterativeDeepening) {
      if (pvNode
          && depth >= IID_DEPTH
          && transpositionMove == Move.NOMOVE) {
        int oldAlpha = alpha;
        int oldBeta = beta;
        alpha = -Value.CHECKMATE;
        beta = Value.CHECKMATE;

        for (int newDepth = 1; newDepth < depth; newDepth++) {
          int value = alphaBeta(newDepth, alpha, beta, height, true, false);

          //## BEGIN Aspiration Windows
          if (!(this.stopped && this.canStop) && Configuration.useAspirationWindows) {
            // First adjustment
            if (value <= alpha || value >= beta) {
              if (value <= alpha) {
                alpha -= ASPIRATIONWINDOW_ADJUSTMENT;
              } else if (value >= beta) {
                beta += ASPIRATIONWINDOW_ADJUSTMENT;
              } else {
                assert false : "Alpha: " + alpha + ", Beta: " + beta + ", Value: " + value;
              }
              if (alpha < -Value.CHECKMATE) {
                alpha = -Value.CHECKMATE;
              }
              if (beta > Value.CHECKMATE) {
                beta = Value.CHECKMATE;
              }

              // Do the Alpha-Beta search again
              value = alphaBeta(newDepth, alpha, beta, height, true, false);

              if (!(this.stopped && this.canStop)) {
                // Second adjustment
                // Open window to full width
                if (value <= alpha || value >= beta) {
                  alpha = -Value.CHECKMATE;
                  beta = Value.CHECKMATE;

                  // Do the Alpha-Beta search again
                  value = alphaBeta(newDepth, alpha, beta, height, true, false);
                }
              }
            }

            // Adjust aspiration window
            alpha = value - ASPIRATIONWINDOW;
            beta = value + ASPIRATIONWINDOW;
            if (alpha < -Value.CHECKMATE) {
              alpha = -Value.CHECKMATE;
            }
            if (beta > Value.CHECKMATE) {
              beta = Value.CHECKMATE;
            }
          }
          //## ENDOF Aspiration Windows

          if (this.stopped && this.canStop) {
            return oldAlpha;
          }
        }

        alpha = oldAlpha;
        beta = oldBeta;

        if (pvList[height].getLength() > 0) {
          // Hopefully we have a transposition move now
          transpositionMove = pvList[height].moves[pvList[height].head];
        }
      }
    }
    //## ENDOF Internal Iterative Deepening

    // Initialize the move generator
    MoveGenerator.initializeMain(attack, height, transpositionMove);

    // Initialize Single-Response Extension
    boolean isSingleReply;
    if (isCheck && attack.numberOfMoves == 1) {
      isSingleReply = true;
    } else {
      isSingleReply = false;
    }

    int move = Move.NOMOVE;
    while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
      //## BEGIN Minor Promotion Pruning
      if (Configuration.useMinorPromotionPruning
          && !this.analyzeMode
          && Move.getType(move) == Move.PAWNPROMOTION
          && Move.getPromotion(move) != Piece.QUEEN) {
        assert Move.getPromotion(move) == Piece.ROOK || Move.getPromotion(move) == Piece.BISHOP || Move.getPromotion(move) == Piece.KNIGHT;
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
          assert Move.getType(move) != Move.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          if (evalValue == Value.INFINITY) {
            // Store evaluation
            evalValue = this.evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_PREFRONTIERMARGIN;

          // Add the target value to the eval
          int target = Move.getTarget(move);
          if (target != Piece.NOPIECE) {
            value += Piece.getValueFromChessman(target);
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
          assert Move.getType(move) != Move.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          if (evalValue == Value.INFINITY) {
            // Store evaluation
            evalValue = this.evaluation.evaluate(board);
          }
          int value = evalValue + FUTILITY_FRONTIERMARGIN;

          // Add the target value to the eval
          int target = Move.getTarget(move);
          if (target != Piece.NOPIECE) {
            value += Piece.getValueFromChessman(target);
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
            && Move.getTarget(move) == Piece.NOPIECE
            && !isDangerousMove(move)) {
          assert !board.isCheckingMove(move);
          assert Move.getType(move) != Move.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          newDepth--;
          reduced = true;
        }
      }
      //## ENDOF Late Move Reduction

      // Do move
      board.makeMove(move);

      //## BEGIN Principal Variation Search
      int value;
      if (!pvNode || bestValue == -Value.INFINITY) {
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

      if (this.stopped && this.canStop) {
        break;
      }

      // Update
      searchedMoves++;

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          bestMove = move;
          hashType = Bound.EXACT;
          alpha = value;

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Bound.BETA;
            break;
          }
        }
      }
    }

    MoveGenerator.destroy();

    // If we cannot move, check for checkmate and stalemate.
    if (bestValue == -Value.INFINITY) {
      if (isCheck) {
        // We have a check mate. This is bad for us, so return a -CHECKMATE.
        hashType = Bound.EXACT;
        bestValue = -Value.CHECKMATE + height;
      } else {
        // We have a stale mate. Return the draw value.
        hashType = Bound.EXACT;
        bestValue = Value.DRAW;
      }
    }

    if (!(this.stopped && this.canStop)) {
      if (bestMove != Move.NOMOVE) {
        addGoodMove(bestMove, depth, height);
      }
      this.transpositionTable.put(board.zobristCode, depth, bestValue, hashType, bestMove, mateThreat, height);
    }

    return bestValue;
  }

  private int quiescent(int checkingDepth, int alpha, int beta, int height, boolean pvNode, boolean useTranspositionTable) {
    updateSearch(height);

    // Abort conditions
    if ((this.stopped && this.canStop) || height == Depth.MAX_HEIGHT) {
      return this.evaluation.evaluate(board);
    }

    // Check the repetition table and fifty move rule
    if (board.isRepetition() || board.halfMoveClock >= 100) {
      return Value.DRAW;
    }

    //## BEGIN Mate Distance Pruning
    if (Configuration.useMateDistancePruning) {
      int value = -Value.CHECKMATE + height;
      if (value > alpha) {
        alpha = value;
        if (value >= beta) {
          return value;
        }
      }
      value = -(-Value.CHECKMATE + height + 1);
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
      TranspositionTable.TranspositionTableEntry entry = this.transpositionTable.get(board.zobristCode);
      if (entry != null) {
        assert entry.depth >= checkingDepth;
        int value = entry.getValue(height);
        int type = entry.type;

        switch (type) {
          case Bound.BETA:
            if (value >= beta) {
              return value;
            }
            break;
          case Bound.ALPHA:
            if (value <= alpha) {
              return value;
            }
            break;
          case Bound.EXACT:
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
    int hashType = Bound.ALPHA;
    int bestValue = -Value.INFINITY;
    int evalValue = Value.INFINITY;

    if (!isCheck) {
      // Stand pat
      int value = this.evaluation.evaluate(board);

      // Store evaluation
      evalValue = value;

      // Pruning
      bestValue = value;

      // Do we have a better value?
      if (value > alpha) {
        hashType = Bound.EXACT;
        alpha = value;

        // Is the value higher than beta?
        if (value >= beta) {
          // Cut-off

          hashType = Bound.BETA;

          if (useTranspositionTable) {
            assert checkingDepth == 0;
            this.transpositionTable.put(board.zobristCode, 0, bestValue, hashType, Move.NOMOVE, false, height);
          }

          return bestValue;
        }
      }
    } else {
      // Check Extension
      checkingDepth++;
    }

    // Initialize the move generator
    MoveGenerator.initializeQuiescent(attack, checkingDepth >= 0);

    int move = Move.NOMOVE;
    while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
      //## BEGIN Futility Pruning
      if (Configuration.useDeltaPruning) {
        if (!pvNode
            && !isCheck
            && !board.isCheckingMove(move)
            && !isDangerousMove(move)) {
          assert Move.getTarget(move) != Piece.NOPIECE;
          assert Move.getType(move) != Move.PAWNPROMOTION : board.getBoard() + ", " + Move.toString(move);

          int value = evalValue + FUTILITY_QUIESCENTMARGIN;

          // Add the target value to the eval
          value += Piece.getValueFromChessman(Move.getTarget(move));

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

      if (this.stopped && this.canStop) {
        break;
      }

      // Pruning
      if (value > bestValue) {
        bestValue = value;
        addPv(pvList[height], pvList[height + 1], move);

        // Do we have a better value?
        if (value > alpha) {
          hashType = Bound.EXACT;
          alpha = value;

          // Is the value higher than beta?
          if (value >= beta) {
            // Cut-off

            hashType = Bound.BETA;
            break;
          }
        }
      }
    }

    MoveGenerator.destroy();

    if (bestValue == -Value.INFINITY) {
      assert isCheck;

      // We have a check mate. This is bad for us, so return a -CHECKMATE.
      bestValue = -Value.CHECKMATE + height;
    }

    if (useTranspositionTable) {
      if (!(this.stopped && this.canStop)) {
        this.transpositionTable.put(board.zobristCode, 0, bestValue, hashType, Move.NOMOVE, false, height);
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

    assert (Move.getEnd(move) != board.captureSquare) || (Move.getTarget(move) != Piece.NOPIECE);

    //## Recapture Extension
    if (Configuration.useRecaptureExtension
        && Move.getEnd(move) == board.captureSquare
        && See.seeMove(move, Move.getChessmanColor(move)) > 0) {
      newDepth++;
    }

    //## Check Extension
    else if (Configuration.useCheckExtension
        && board.isCheckingMove(move)) {
      newDepth++;
    }

    //## Pawn Extension
    else if (Configuration.usePawnExtension
        && Move.getChessman(move) == Piece.PAWN
        && Square.getRelativeRank(Move.getEnd(move), board.activeColor) == Square.rank7) {
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
    if (Position.materialCount[board.activeColor] == 0
        && Position.materialCount[Color.switchColor(board.activeColor)] == 1
        && Move.getTarget(move) != Piece.NOPIECE
        && Move.getTarget(move) != Piece.PAWN) {
      newDepth++;
    }

    return newDepth;
  }

  private static boolean isDangerousMove(int move) {
    int chessman = Move.getChessman(move);
    int relativeRank = Square.getRelativeRank(Move.getEnd(move), board.activeColor);
    if (chessman == Piece.PAWN && relativeRank >= Square.rank7) {
      return true;
    }

    int target = Move.getTarget(move);
    if (target == Piece.QUEEN) {
      return true;
    }

    return false;
  }

  private static void addPv(MoveList destination, MoveList source, int move) {
    assert destination != null;
    assert source != null;
    assert move != Move.NOMOVE;

    destination.resetList();

    destination.moves[destination.tail++] = move;

    for (int i = source.head; i < source.tail; i++) {
      destination.moves[destination.tail++] = source.moves[i];
    }
  }

  private static void addGoodMove(int move, int depth, int height) {
    assert move != Move.NOMOVE;

    if (Move.getTarget(move) != Piece.NOPIECE) {
      return;
    }

    int type = Move.getType(move);
    if (type == Move.PAWNPROMOTION || type == Move.NULL) {
      return;
    }

    assert type != Move.ENPASSANT;

    killerTable.add(move, height);
    historyTable.add(move, depth);
  }

}
