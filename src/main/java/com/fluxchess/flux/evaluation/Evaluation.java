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
package com.fluxchess.flux.evaluation;

import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.board.Board;
import com.fluxchess.flux.board.ChessmanList;
import com.fluxchess.flux.board.IntGamePhase;
import com.fluxchess.flux.board.Position;
import com.fluxchess.flux.search.Search;
import com.fluxchess.jcpi.models.*;

public final class Evaluation {

  public static final int VALUE_PAWN = 100;
  public static final int VALUE_KNIGHT = 325;
  public static final int VALUE_BISHOP = 325;
  public static final int VALUE_ROOK = 500;
  public static final int VALUE_QUEEN = 975;
  public static final int VALUE_KING = 20000;

  // Game phase thresholds
  private static final int GAMEPHASE_OPENING_VALUE =
    Evaluation.VALUE_KING
      + 1 * Evaluation.VALUE_QUEEN
      + 2 * Evaluation.VALUE_ROOK
      + 2 * Evaluation.VALUE_BISHOP
      + 2 * Evaluation.VALUE_KNIGHT
      + 8 * Evaluation.VALUE_PAWN;
  private static final int GAMEPHASE_ENDGAME_VALUE =
    Evaluation.VALUE_KING
      + 1 * Evaluation.VALUE_QUEEN
      + 1 * Evaluation.VALUE_ROOK;
  private static final int GAMEPHASE_INTERVAL = GAMEPHASE_OPENING_VALUE - GAMEPHASE_ENDGAME_VALUE;
  private static final int GAMEPHASE_ENDGAME_COUNT = 2;

  // Our total values
  private static final int[] material = new int[IntColor.values.length];

  // The hash tables
  private final EvaluationTable evaluationTable;
  private final PawnTable pawntable;

  public Evaluation(EvaluationTable evaluationTable, PawnTable pawnTable) {
    assert evaluationTable != null;
    assert pawnTable != null;

    this.evaluationTable = evaluationTable;
    this.pawntable = pawnTable;
  }

  /**
   * Evaluates the board.
   *
   * @param board the board.
   * @return the evaluation value in centipawns.
   */
  public int evaluate(Board board) {
    assert board != null;

    // Check the evaluation table
    if (Configuration.useEvaluationTable) {
      EvaluationTable.EvaluationTableEntry entry = evaluationTable.get(board.zobristCode);
      if (entry != null) {
        return entry.evaluation;
      }
    }

    // Initialize
    int myColor = board.activeColor;
    int enemyColor = IntColor.opposite(myColor);
    int total = 0;

    // Create tables
    AttackTableEvaluation.getInstance().createAttackTable(myColor, board);
    AttackTableEvaluation.getInstance().createAttackTable(enemyColor, board);
    PawnTableEvaluation.getInstance().createPawnTable(myColor, board);
    PawnTableEvaluation.getInstance().createPawnTable(enemyColor, board);

    // Evaluate draw
    int drawFactor = DrawEvaluation.evaluateDraw(board);
    if (drawFactor > 0) {
      // Evaluate material
      material[myColor] = MaterialEvaluation.evaluateMaterial(myColor, board);
      material[enemyColor] = MaterialEvaluation.evaluateMaterial(enemyColor, board);
      total += material[myColor] - material[enemyColor];

      // Evaluate position
      total += PositionValueEvaluation.evaluatePositionValue(myColor, board) - PositionValueEvaluation.evaluatePositionValue(enemyColor, board);

      // Evaluate knights
      total += KnightEvaluation.evaluateKnight(myColor, enemyColor, board) - KnightEvaluation.evaluateKnight(enemyColor, myColor, board);

      // Evaluate bishops
      total += BishopEvaluation.evaluateBishop(myColor, enemyColor, board) - BishopEvaluation.evaluateBishop(enemyColor, myColor, board);

      // Evaluate rooks
      total += RookEvaluation.evaluateRook(myColor, enemyColor, board) - RookEvaluation.evaluateRook(enemyColor, myColor, board);

      // Evaluate queens
      total += QueenEvaluation.evaluateQueen(myColor, enemyColor, board) - QueenEvaluation.evaluateQueen(enemyColor, myColor, board);

      // Evaluate kings
      total += KingEvaluation.evaluateKing(myColor, enemyColor, board) - KingEvaluation.evaluateKing(enemyColor, myColor, board);

      // Evaluate the pawn structures
      long pawnZobristCode = board.pawnZobristCode;
      int pawnStructureValue = 0;
      if (Configuration.usePawnTable && pawntable.exists(pawnZobristCode)) {
        pawnStructureValue = pawntable.getValue(pawnZobristCode);
      } else {
        pawnStructureValue = PawnStructureEvaluation.evaluatePawnStructure(myColor, enemyColor, board) - PawnStructureEvaluation.evaluatePawnStructure(enemyColor, myColor, board);
        if (Configuration.usePawnTable) {
          pawntable.put(pawnZobristCode, pawnStructureValue);
        }
      }
      total += pawnStructureValue;

      // Evaluate the pawn passer
      total += PawnPasserEvaluation.evaluatePawnPasser(myColor, enemyColor, board) - PawnPasserEvaluation.evaluatePawnPasser(enemyColor, myColor, board);

      // Evaluate known patterns
      total += PatternEvaluation.evaluatePatterns(myColor, board) - PatternEvaluation.evaluatePatterns(enemyColor, board);
    } else {
      assert drawFactor == 0;
    }

    // Draw factor
    total = (total * drawFactor) / DrawEvaluation.DRAW_FACTOR;

    if (total < -Search.CHECKMATE_THRESHOLD) {
      total = -Search.CHECKMATE_THRESHOLD;
    } else if (total > Search.CHECKMATE_THRESHOLD) {
      total = Search.CHECKMATE_THRESHOLD;
    }

    // Store the result and return
    if (Configuration.useEvaluationTable) {
      evaluationTable.put(board.zobristCode, total);
    }

    return total;
  }

  /**
   * Returns the value of the piece.
   *
   * @param piece the piece.
   * @return the value of the piece.
   */
  public static int getValueFromPiece(int piece) {
    assert piece != IntPiece.NOPIECE;

    int chessman = IntPiece.getChessman(piece);

    switch (chessman) {
      case IntChessman.PAWN:
        return VALUE_PAWN;
      case IntChessman.KNIGHT:
        return VALUE_KNIGHT;
      case IntChessman.BISHOP:
        return VALUE_BISHOP;
      case IntChessman.ROOK:
        return VALUE_ROOK;
      case IntChessman.QUEEN:
        return VALUE_QUEEN;
      case IntChessman.KING:
        return VALUE_KING;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the value of the chessman.
   *
   * @param chessman the chessman.
   * @return the value of the chessman.
   */
  public static int getValueFromChessman(int chessman) {
    assert chessman != IntChessman.NOCHESSMAN;

    switch (chessman) {
      case IntChessman.PAWN:
        return VALUE_PAWN;
      case IntChessman.KNIGHT:
        return VALUE_KNIGHT;
      case IntChessman.BISHOP:
        return VALUE_BISHOP;
      case IntChessman.ROOK:
        return VALUE_ROOK;
      case IntChessman.QUEEN:
        return VALUE_QUEEN;
      case IntChessman.KING:
        return VALUE_KING;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the evaluation value mix from the opening and endgame values depending on the current game phase.
   * This allows us to make a smooth transition from the opening to the endgame.
   *
   * @param myColor the color.
   * @param opening the opening evaluation value.
   * @param endgame the endgame evaluation value.
   * @return the evaluation value mix from the opening and endgame values depending on the current game phase.
   */
  public static int getGamePhaseEvaluation(int myColor, int opening, int endgame, Board board) {
    int intervalMaterial = materialValueAll(IntColor.WHITE, board);

    if (intervalMaterial >= GAMEPHASE_OPENING_VALUE) {
      intervalMaterial = GAMEPHASE_INTERVAL;
    } else if (intervalMaterial <= GAMEPHASE_ENDGAME_VALUE) {
      intervalMaterial = 0;
    } else {
      intervalMaterial -= GAMEPHASE_ENDGAME_VALUE;
    }

    return (((opening - endgame) * intervalMaterial) / GAMEPHASE_INTERVAL) + endgame;
  }

  public static int getGamePhase(Board board) {
    if (materialValueAll(IntColor.WHITE, board) >= GAMEPHASE_OPENING_VALUE && materialValueAll(IntColor.BLACK, board) >= GAMEPHASE_OPENING_VALUE) {
      return IntGamePhase.OPENING;
    } else if (materialValueAll(IntColor.WHITE, board) <= GAMEPHASE_ENDGAME_VALUE || materialValueAll(IntColor.BLACK, board) <= GAMEPHASE_ENDGAME_VALUE
      || materialCount(IntColor.WHITE, board) <= GAMEPHASE_ENDGAME_COUNT || materialCount(IntColor.BLACK, board) <= GAMEPHASE_ENDGAME_COUNT) {
      return IntGamePhase.ENDGAME;
    } else {
      return IntGamePhase.MIDDLE;
    }
  }

  public static int materialValueAll(int color, Board board) {
    return VALUE_PAWN * board.pawnList[color].size() +
      VALUE_KNIGHT * board.knightList[color].size() +
      VALUE_BISHOP * board.bishopList[color].size() +
      VALUE_ROOK * board.rookList[color].size() +
      VALUE_QUEEN * board.queenList[color].size() +
      VALUE_KING * board.kingList[color].size();
  }

  public static int materialCount(int color, Board board) {
    return board.knightList[color].size() +
      board.bishopList[color].size() +
      board.rookList[color].size() +
      board.queenList[color].size();
  }

  public static int materialCountAll(int color, Board board) {
    return board.pawnList[color].size() +
      board.knightList[color].size() +
      board.bishopList[color].size() +
      board.rookList[color].size() +
      board.queenList[color].size();
  }

}
