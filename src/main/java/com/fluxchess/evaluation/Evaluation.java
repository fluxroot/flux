/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.evaluation;

import com.fluxchess.Configuration;
import com.fluxchess.Search;
import com.fluxchess.board.Hex88Board;
import com.fluxchess.board.IntColor;
import com.fluxchess.table.EvaluationTable;
import com.fluxchess.table.EvaluationTableEntry;
import com.fluxchess.table.PawnTable;

/**
 * Evaluation
 * 
 * @author Phokham Nonava
 */
public final class Evaluation {

	// Our total values
	private static int[] material = new int[IntColor.ARRAY_DIMENSION];
	private static int total = 0;
	
	// The hash tables
	private final EvaluationTable evaluationTable;
	private final PawnTable pawnHashtable;

	/**
	 * Creates a new Evaluation.
	 */
	public Evaluation(EvaluationTable newEvaluationTable, PawnTable newPawnTable) {
		assert newEvaluationTable != null;
		assert newPawnTable != null;

		this.evaluationTable = newEvaluationTable;
		this.pawnHashtable = newPawnTable;
	}

	/**
	 * Evaluates the board.
	 * 
	 * @param board the board.
	 * @return the evaluation value in centipawns.
	 */
	public int evaluate(Hex88Board board) {
		assert board != null;

		// Check the evaluation table
		if (Configuration.useEvaluationTable) {
			EvaluationTableEntry entry = this.evaluationTable.get(board.zobristCode);
			if (entry != null) {
				return entry.evaluation;
			}
		}

		// Initialize
		int myColor = board.activeColor;
		int enemyColor = IntColor.switchColor(myColor);
		total = 0;

		// Create tables
		AttackTableEvaluation.createAttackTable(myColor, board);
		AttackTableEvaluation.createAttackTable(enemyColor, board);
		PawnTableEvaluation.createPawnTable(myColor);
		PawnTableEvaluation.createPawnTable(enemyColor);

		// Evaluate draw
		int drawFactor = DrawEvaluation.evaluateDraw(board);
		if (drawFactor > 0) {
			// Evaluate material
			material[myColor] = MaterialEvaluation.evaluateMaterial(myColor);
			material[enemyColor] = MaterialEvaluation.evaluateMaterial(enemyColor);
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
			if (Configuration.usePawnTable && this.pawnHashtable.exists(pawnZobristCode)) {
				pawnStructureValue = this.pawnHashtable.getValue(pawnZobristCode);
			} else {
				pawnStructureValue = PawnStructureEvaluation.evaluatePawnStructure(myColor, enemyColor, board) - PawnStructureEvaluation.evaluatePawnStructure(enemyColor, myColor, board);
				if (Configuration.usePawnTable) {
					this.pawnHashtable.put(pawnZobristCode, pawnStructureValue);
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
			this.evaluationTable.put(board.zobristCode, total);
		}

		return total;
	}

}
