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
import com.fluxchess.flux.Search;
import com.fluxchess.flux.board.*;
import com.fluxchess.flux.move.IntCastling;
import com.fluxchess.flux.move.IntMove;
import com.fluxchess.flux.move.MoveGenerator;
import com.fluxchess.flux.move.MoveSee;
import com.fluxchess.flux.table.EvaluationTable;
import com.fluxchess.flux.table.EvaluationTableEntry;
import com.fluxchess.flux.table.PawnTable;

import java.util.Arrays;

public final class Evaluation implements IEvaluation {

	// Our evaluation constants
	private static final int EVAL_PAWN_DOUBLED_OPENING = 10;
	private static final int EVAL_PAWN_DOUBLED_ENDGAME = 20;
	private static final int EVAL_PAWN_ISOLATED_OPENING = 10;
	private static final int EVAL_PAWN_ISOLATED_ENDGAME = 20;
	private static final int EVAL_PAWN_BACKWARD_OPENING = 15;
	private static final int EVAL_PAWN_BACKWARD_ENDGAME = 10;
	private static final int EVAL_PAWN_PASSER_MAXBONUS = 6 * 6 * 6;
	private static final int EVAL_PAWN_PASSER_OPENING_MIN = 10;
	private static final int EVAL_PAWN_PASSER_OPENING_MAX = 70;
	private static final int EVAL_PAWN_PASSER_ENDGAME_MIN = 20;
	private static final int EVAL_PAWN_PASSER_ENDGAME_MAX = 140;
	private static final int EVAL_PAWN_PASSER_FREE = 60;
	private static final int EVAL_PAWN_PASSER_UNSTOPPABLE = 800;
	private static final int EVAL_PAWN_MYKING_DISTANCE = 5;
	private static final int EVAL_PAWN_ENEMYKING_DISTANCE = 20;

	private static final int EVAL_KNIGHT_MOBILITY_BASE = -4;
	private static final int EVAL_KNIGHT_MOBILITYFACTOR_OPENING = 4;
	private static final int EVAL_KNIGHT_MOBILITYFACTOR_ENDGAME = 4;
	private static final int EVAL_KNIGHT_SAFETY = 10;

	private static final int EVAL_BISHOP_MOBILITY_BASE = -6;
	private static final int EVAL_BISHOP_MOBILITYFACTOR_OPENING = 5;
	private static final int EVAL_BISHOP_MOBILITYFACTOR_ENDGAME = 5;
	private static final int EVAL_BISHOP_SAFETY = 10;
	private static final int EVAL_BISHOP_PAIR = 50;

	private static final int EVAL_ROOK_MOBILITY_BASE = -7;
	private static final int EVAL_ROOK_MOBILITYFACTOR_OPENING = 2;
	private static final int EVAL_ROOK_MOBILITYFACTOR_ENDGAME = 4;
	private static final int EVAL_ROOK_SAFETY = 20;
	private static final int EVAL_ROOK_OPENFILE = 20;
	private static final int EVAL_ROOK_NEARKINGFILE = 10;
	private static final int EVAL_ROOK_SEVENTHRANK_OPENING = 20;
	private static final int EVAL_ROOK_SEVENTHRANK_ENDGAME = 40;
	private static final int EVAL_ROOK_SEVENTHRANK_BONUS = 10;

	private static final int EVAL_QUEEN_MOBILITY_BASE = -13;
	private static final int EVAL_QUEEN_MOBILITYFACTOR_OPENING = 1;
	private static final int EVAL_QUEEN_MOBILITYFACTOR_ENDGAME = 2;
	private static final int EVAL_QUEEN_SAFETY = 40;
	private static final int EVAL_QUEEN_SEVENTHRANK_OPENING = 10;
	private static final int EVAL_QUEEN_SEVENTHRANK_ENDGAME = 20;
	
//	private static final int EVAL_MATERIAL_QUEEN_BONUS = 2 * IntChessman.VALUE_ROOK - IntChessman.VALUE_QUEEN;
	
	private static final int EVAL_KING_ATTACK = 40;

	// Our attack table implementing Idea of Ed Schröder
	public static final byte[] KING_ATTACK_PATTERN = {
		// . P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P
		//     M M     M M     M M     M M     M M     M M     M M     M M
		//         R R R R         R R R R         R R R R         R R R R
		//                 Q Q Q Q Q Q Q Q                 Q Q Q Q Q Q Q Q
		//                                 K K K K K K K K K K K K K K K K
		   0,0,1,1,2,2,3,3,3,3,4,4,5,5,6,6,0,0,1,1,2,2,3,3,3,3,4,4,5,5,6,8
	};
	private static final int[] KING_ATTACK_EVAL = {
		0,0,128,192,224,240,248,252,254,255,256,256,256,256,256,256,256,256
	};
	private static final byte BIT_PAWN = 1 << 3;
	private static final byte BIT_MINOR = 1 << 4;
	private static final byte BIT_ROOK = 1 << 5;
	private static final byte BIT_QUEEN = 1 << 6;
	private static final byte BIT_KING = -128;
	private static final byte MASK_ATTACKERS = 31;
	private static final byte[][] attackTable = new byte[IntColor.ARRAY_DIMENSION][Hex88Board.BOARDSIZE];

	// Our pawn structure table. 8 + 2 -> 2 Sentinels for each side.
	private static final byte[][] pawnTable = new byte[IntColor.ARRAY_DIMENSION][10];
	
	// Our total values
	private static final int PHASE_INTERVAL = Hex88Board.GAMEPHASE_OPENING_VALUE - Hex88Board.GAMEPHASE_ENDGAME_VALUE;
	private static final int TOTAL_OPENING = 0;
	private static final int TOTAL_ENDGAME = 1;
	private static int[][] totalPawn = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalKnight = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalBishop = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalRook = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalQueen = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalKing = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalPawnStructure = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalPawnPasser = new int[IntColor.ARRAY_DIMENSION][2];
	private static int[][] totalPatterns = new int[IntColor.ARRAY_DIMENSION][2];
	private static int totalOpening = 0;
	private static int totalEndgame = 0;
	private static int total = 0;
	
	// Draw values
	private static final int DRAW_FACTOR = 16;
	private static int[] drawFactor = new int[IntColor.ARRAY_DIMENSION];

	// The hash tables
	private final EvaluationTable evaluationTable;
	private final PawnTable pawnHashtable;

	/**
	 * Creates a new Evaluation.
	 */
	public Evaluation(EvaluationTable newEvaluationTable, PawnTable newPawnTable) {
		this.evaluationTable = newEvaluationTable;
		this.pawnHashtable = newPawnTable;
	}

	/**
	 * Prints the evaluation of the board.
	 */
	public void print(Hex88Board board) {
		evaluate(board);

		int myColor = board.activeColor;
		int enemyColor = IntColor.switchColor(myColor);

		System.out.printf("%20s:               (%5s:%5s)               (%5s:%5s)\n", "Colors",
				IntColor.valueOfIntColor(myColor).toString(),
				IntColor.valueOfIntColor(enemyColor).toString(),
				IntColor.valueOfIntColor(myColor).toString(),
				IntColor.valueOfIntColor(enemyColor).toString());
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Material",
				Hex88Board.materialValue[myColor] - Hex88Board.materialValue[enemyColor],
				Hex88Board.materialValue[myColor],
				Hex88Board.materialValue[enemyColor],
				Hex88Board.materialValue[myColor] - Hex88Board.materialValue[enemyColor],
				Hex88Board.materialValue[myColor],
				Hex88Board.materialValue[enemyColor]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Position",
				Hex88Board.positionValueOpening[myColor] - Hex88Board.positionValueOpening[enemyColor],
				Hex88Board.positionValueOpening[myColor],
				Hex88Board.positionValueOpening[enemyColor],
				Hex88Board.positionValueEndgame[myColor] - Hex88Board.positionValueEndgame[enemyColor],
				Hex88Board.positionValueEndgame[myColor],
				Hex88Board.positionValueEndgame[enemyColor]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Pawn",
				totalPawn[myColor][TOTAL_OPENING] - totalPawn[enemyColor][TOTAL_OPENING],
				totalPawn[myColor][TOTAL_OPENING],
				totalPawn[enemyColor][TOTAL_OPENING],
				totalPawn[myColor][TOTAL_ENDGAME] - totalPawn[enemyColor][TOTAL_ENDGAME],
				totalPawn[myColor][TOTAL_ENDGAME],
				totalPawn[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Knight",
				totalKnight[myColor][TOTAL_OPENING] - totalKnight[enemyColor][TOTAL_OPENING],
				totalKnight[myColor][TOTAL_OPENING],
				totalKnight[enemyColor][TOTAL_OPENING],
				totalKnight[myColor][TOTAL_ENDGAME] - totalKnight[enemyColor][TOTAL_ENDGAME],
				totalKnight[myColor][TOTAL_ENDGAME],
				totalKnight[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Bishop",
				totalBishop[myColor][TOTAL_OPENING] - totalBishop[enemyColor][TOTAL_OPENING],
				totalBishop[myColor][TOTAL_OPENING],
				totalBishop[enemyColor][TOTAL_OPENING],
				totalBishop[myColor][TOTAL_ENDGAME] - totalBishop[enemyColor][TOTAL_ENDGAME],
				totalBishop[myColor][TOTAL_ENDGAME],
				totalBishop[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Rook",
				totalRook[myColor][TOTAL_OPENING] - totalRook[enemyColor][TOTAL_OPENING],
				totalRook[myColor][TOTAL_OPENING],
				totalRook[enemyColor][TOTAL_OPENING],
				totalRook[myColor][TOTAL_ENDGAME] - totalRook[enemyColor][TOTAL_ENDGAME],
				totalRook[myColor][TOTAL_ENDGAME],
				totalRook[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Queen",
				totalQueen[myColor][TOTAL_OPENING] - totalQueen[enemyColor][TOTAL_OPENING],
				totalQueen[myColor][TOTAL_OPENING],
				totalQueen[enemyColor][TOTAL_OPENING],
				totalQueen[myColor][TOTAL_ENDGAME] - totalQueen[enemyColor][TOTAL_ENDGAME],
				totalQueen[myColor][TOTAL_ENDGAME],
				totalQueen[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total King",
				totalKing[myColor][TOTAL_OPENING] - totalKing[enemyColor][TOTAL_OPENING],
				totalKing[myColor][TOTAL_OPENING],
				totalKing[enemyColor][TOTAL_OPENING],
				totalKing[myColor][TOTAL_ENDGAME] - totalKing[enemyColor][TOTAL_ENDGAME],
				totalKing[myColor][TOTAL_ENDGAME],
				totalKing[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Pawn Structure",
				totalPawnStructure[myColor][TOTAL_OPENING] - totalPawnStructure[enemyColor][TOTAL_OPENING],
				totalPawnStructure[myColor][TOTAL_OPENING],
				totalPawnStructure[enemyColor][TOTAL_OPENING],
				totalPawnStructure[myColor][TOTAL_ENDGAME] - totalPawnStructure[enemyColor][TOTAL_ENDGAME],
				totalPawnStructure[myColor][TOTAL_ENDGAME],
				totalPawnStructure[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Pawn Passer",
				totalPawnPasser[myColor][TOTAL_OPENING] - totalPawnPasser[enemyColor][TOTAL_OPENING],
				totalPawnPasser[myColor][TOTAL_OPENING],
				totalPawnPasser[enemyColor][TOTAL_OPENING],
				totalPawnPasser[myColor][TOTAL_ENDGAME] - totalPawnPasser[enemyColor][TOTAL_ENDGAME],
				totalPawnPasser[myColor][TOTAL_ENDGAME],
				totalPawnPasser[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d (%5d:%5d) Endgame %5d (%5d:%5d)\n", "Total Patterns",
				totalPatterns[myColor][TOTAL_OPENING] - totalPatterns[enemyColor][TOTAL_OPENING],
				totalPatterns[myColor][TOTAL_OPENING],
				totalPatterns[enemyColor][TOTAL_OPENING],
				totalPatterns[myColor][TOTAL_ENDGAME] - totalPatterns[enemyColor][TOTAL_ENDGAME],
				totalPatterns[myColor][TOTAL_ENDGAME],
				totalPatterns[enemyColor][TOTAL_ENDGAME]);
		System.out.printf("%20s: Opening %5d               Endgame %5d\n", "Total",
				totalOpening,
				totalEndgame);
		System.out.printf("%20s: %5d\n", "Total Phase Mix", total);
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
		for (int color : IntColor.values) {
			// Zero our tables
			Arrays.fill(attackTable[color], (byte) 0);
			Arrays.fill(pawnTable[color], (byte) 0);
			
			// Set the total values to zero
			totalPawn[color][TOTAL_OPENING] = 0;
			totalPawn[color][TOTAL_ENDGAME] = 0;
			totalKnight[color][TOTAL_OPENING] = 0;
			totalKnight[color][TOTAL_ENDGAME] = 0;
			totalBishop[color][TOTAL_OPENING] = 0;
			totalBishop[color][TOTAL_ENDGAME] = 0;
			totalRook[color][TOTAL_OPENING] = 0;
			totalRook[color][TOTAL_ENDGAME] = 0;
			totalQueen[color][TOTAL_OPENING] = 0;
			totalQueen[color][TOTAL_ENDGAME] = 0;
			totalKing[color][TOTAL_OPENING] = 0;
			totalKing[color][TOTAL_ENDGAME] = 0;
			totalPawnStructure[color][TOTAL_OPENING] = 0;
			totalPawnStructure[color][TOTAL_ENDGAME] = 0;
			totalPawnPasser[color][TOTAL_OPENING] = 0;
			totalPawnPasser[color][TOTAL_ENDGAME] = 0;
			totalPatterns[color][TOTAL_OPENING] = 0;
			totalPatterns[color][TOTAL_ENDGAME] = 0;
			
			// Set the draw factor
			drawFactor[color] = DRAW_FACTOR;
		}
		int myColor = board.activeColor;
		int enemyColor = IntColor.switchColor(myColor);
		totalOpening = 0;
		totalEndgame = 0;
		total = 0;

		// Evaluate material
		int myMaterialValue = evaluateMaterial(myColor, enemyColor);
		int enemyMaterialValue = evaluateMaterial(enemyColor, myColor);
		totalOpening += myMaterialValue - enemyMaterialValue;
		totalEndgame += myMaterialValue - enemyMaterialValue;

		// Evaluate position
		totalOpening += Hex88Board.positionValueOpening[myColor] - Hex88Board.positionValueOpening[enemyColor];
		totalEndgame += Hex88Board.positionValueEndgame[myColor] - Hex88Board.positionValueEndgame[enemyColor];

		// Evaluate pawns
		evaluatePawn(myColor);
		evaluatePawn(enemyColor);
		totalOpening += totalPawn[myColor][TOTAL_OPENING] - totalPawn[enemyColor][TOTAL_OPENING];
		totalEndgame += totalPawn[myColor][TOTAL_ENDGAME] - totalPawn[enemyColor][TOTAL_ENDGAME];

		// Evaluate knights
		evaluateKnight(myColor, enemyColor, board);
		evaluateKnight(enemyColor, myColor, board);
		totalOpening += totalKnight[myColor][TOTAL_OPENING] - totalKnight[enemyColor][TOTAL_OPENING];
		totalEndgame += totalKnight[myColor][TOTAL_ENDGAME] - totalKnight[enemyColor][TOTAL_ENDGAME];

		// Evaluate bishops
		evaluateBishop(myColor, enemyColor, board);
		evaluateBishop(enemyColor, myColor, board);
		totalOpening += totalBishop[myColor][TOTAL_OPENING] - totalBishop[enemyColor][TOTAL_OPENING];
		totalEndgame += totalBishop[myColor][TOTAL_ENDGAME] - totalBishop[enemyColor][TOTAL_ENDGAME];

		// Evaluate rooks
		evaluateRook(myColor, enemyColor, board);
		evaluateRook(enemyColor, myColor, board);
		totalOpening += totalRook[myColor][TOTAL_OPENING] - totalRook[enemyColor][TOTAL_OPENING];
		totalEndgame += totalRook[myColor][TOTAL_ENDGAME] - totalRook[enemyColor][TOTAL_ENDGAME];

		// Evaluate queens
		evaluateQueen(myColor, enemyColor, board);
		evaluateQueen(enemyColor, myColor, board);
		totalOpening += totalQueen[myColor][TOTAL_OPENING] - totalQueen[enemyColor][TOTAL_OPENING];
		totalEndgame += totalQueen[myColor][TOTAL_ENDGAME] - totalQueen[enemyColor][TOTAL_ENDGAME];

		// Evaluate kings
		evaluateKing(myColor, enemyColor, board);
		evaluateKing(enemyColor, myColor, board);
		totalOpening += totalKing[myColor][TOTAL_OPENING] - totalKing[enemyColor][TOTAL_OPENING];
		totalEndgame += totalKing[myColor][TOTAL_ENDGAME] - totalKing[enemyColor][TOTAL_ENDGAME];
		
		// Evaluate draw
		evaluateDraw(board);
		if (drawFactor[myColor] == 0 && drawFactor[enemyColor] == 0) {
			return Search.DRAW;
		}

		// Evaluate the pawn structures
		long pawnZobristCode = board.pawnZobristCode;
		int pawnStructureOpening = 0;
		int pawnStructureEndgame = 0;
		if (Configuration.usePawnTable && this.pawnHashtable.exists(pawnZobristCode)) {
			pawnStructureOpening = this.pawnHashtable.getOpening(pawnZobristCode);
			pawnStructureEndgame = this.pawnHashtable.getEndgame(pawnZobristCode);
		} else {
			evaluatePawnStructure(myColor, enemyColor, board);
			evaluatePawnStructure(enemyColor, myColor, board);
			pawnStructureOpening = totalPawnStructure[myColor][TOTAL_OPENING] - totalPawnStructure[enemyColor][TOTAL_OPENING];
			pawnStructureEndgame = totalPawnStructure[myColor][TOTAL_ENDGAME] - totalPawnStructure[enemyColor][TOTAL_ENDGAME];
			if (Configuration.usePawnTable) {
				this.pawnHashtable.put(pawnZobristCode, pawnStructureOpening, pawnStructureEndgame);
			}
		}
		totalOpening += pawnStructureOpening;
		totalEndgame += pawnStructureEndgame;
		
		// Evaluate the pawn passer
		evaluatePawnPasser(myColor, enemyColor, board);
		evaluatePawnPasser(enemyColor, myColor, board);
		totalOpening += totalPawnPasser[myColor][TOTAL_OPENING] - totalPawnPasser[enemyColor][TOTAL_OPENING];
		totalEndgame += totalPawnPasser[myColor][TOTAL_ENDGAME] - totalPawnPasser[enemyColor][TOTAL_ENDGAME];

		// Evaluate known patterns
		evaluatePatterns(myColor, board);
		evaluatePatterns(enemyColor, board);
		totalOpening += totalPatterns[myColor][TOTAL_OPENING] - totalPatterns[enemyColor][TOTAL_OPENING];
		totalEndgame += totalPatterns[myColor][TOTAL_ENDGAME] - totalPatterns[enemyColor][TOTAL_ENDGAME];

		// Create evaluation mix
		// This allows us to make a smooth transition from the opening to the
		// ending
		int phase = (myMaterialValue + enemyMaterialValue) / 2;
		if (phase > Hex88Board.GAMEPHASE_OPENING_VALUE) {
			phase = PHASE_INTERVAL;
		} else if (phase < Hex88Board.GAMEPHASE_ENDGAME_VALUE) {
			phase = 0;
		} else {
			phase -= Hex88Board.GAMEPHASE_ENDGAME_VALUE;
		}
		total = (totalOpening * phase + totalEndgame * (PHASE_INTERVAL - phase)) / PHASE_INTERVAL;

		// Draw factor
		if (total > Search.DRAW) {
			total = (total * drawFactor[myColor]) / DRAW_FACTOR;
		} else if (total < Search.DRAW) {
			total = (total * drawFactor[enemyColor]) / DRAW_FACTOR;
		}
		
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

	private static int evaluateMaterial(int myColor, int enemyColor) {
		int myMaterialValue = Hex88Board.materialValue[myColor];

		// Correct material value based on Larry Kaufman's paper
		// TODO: Check this one
//		myMaterialValue += (Hex88Board.knightList[myColor].size * (Hex88Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 16;
//		myMaterialValue -= (Hex88Board.rookList[myColor].size * (Hex88Board.pawnList[myColor].size - 5) * IntChessman.VALUE_PAWN) / 8;

		// Queen + pawn vs. two rooks
		// TODO: Check this one
//		if ((Hex88Board.knightList[myColor].size + Hex88Board.bishopList[myColor].size >= 2)
//				&& (Hex88Board.knightList[enemyColor].size + Hex88Board.bishopList[enemyColor].size >= 2)) {
//			myMaterialValue += EVAL_MATERIAL_QUEEN_BONUS;
//		}
		
		return myMaterialValue;
	}
	
	private static void evaluateDraw(Hex88Board board) {
		for (int myColor : IntColor.values) {
			int enemyColor = IntColor.switchColor(myColor);
			byte[] enemyAttackTable = attackTable[enemyColor];
			
			assert Hex88Board.kingList[myColor].size != 0;
			assert Hex88Board.kingList[enemyColor].size != 0;
			
			if (Hex88Board.queenList[myColor].size == 0) {
				if (Hex88Board.rookList[myColor].size == 0) {
					if (Hex88Board.bishopList[myColor].size == 0) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KK*

								assert Hex88Board.materialCountAll[myColor] == 0;
								drawFactor[myColor] = 0;
							} else if (Hex88Board.pawnList[myColor].size == 1) {
								// KPK*
								
								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KPKN

													// Check the promotion path
													int delta = 16;
													if (myColor == IntColor.BLACK) {
														delta = -16;
													} else {
														assert myColor == IntColor.WHITE;
													}
													int end = Hex88Board.pawnList[myColor].position[0] + delta;
													while ((end & 0x88) == 0) {
														int chessman = Hex88Board.board[end];
														if ((chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & BIT_MINOR) != 0) {
															drawFactor[myColor] = 1;
															break;
														} else {
															end += delta;
														}
													}
												}
											}
										} else if (Hex88Board.bishopList[enemyColor].size == 1) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KPKB

													// Check the promotion path
													int delta = 16;
													if (myColor == IntColor.BLACK) {
														delta = -16;
													} else {
														assert myColor == IntColor.WHITE;
													}
													int end = Hex88Board.pawnList[myColor].position[0] + delta;
													while ((end & 0x88) == 0) {
														int chessman = Hex88Board.board[end];
														if ((chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == enemyColor) || (enemyAttackTable[end] & BIT_MINOR) != 0) {
															drawFactor[myColor] = 1;
															break;
														} else {
															end += delta;
														}
													}
												}
											}
										}
									}
								}
							}
						} else if (Hex88Board.knightList[myColor].size == 1) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KNK*

								drawFactor[myColor] = 0;
							}
						} else if (Hex88Board.knightList[myColor].size == 2) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KNNK*

								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KNNK

													drawFactor[myColor] = 0;
												}
											} else if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KNNKN

													drawFactor[myColor] = 0;
												}
											}
										}
									}
								}
							}
						}
					} else if (Hex88Board.bishopList[myColor].size == 1) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KBK*

								drawFactor[myColor] = 0;
							}
						} else if (Hex88Board.knightList[myColor].size == 1) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KBNK*

								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KBNKN

													drawFactor[myColor] = 1;
												}
											}
										}
									}
								}
							}
						}
					} else if (Hex88Board.bishopList[myColor].size == 2) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KBBK*

								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KBBKN

													drawFactor[myColor] = 8;
												}
											}
										} else if (Hex88Board.bishopList[enemyColor].size == 1) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KBBKB

													drawFactor[myColor] = 2;
												}
											}
										}
									}
								}
							}
						}
					}
				} else if (Hex88Board.rookList[myColor].size == 1) {
					if (Hex88Board.bishopList[myColor].size == 0) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KRK*

								if (Hex88Board.queenList[enemyColor].size == 0) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 1) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KRKN

													drawFactor[myColor] = 1;
												}
											}
										} else if (Hex88Board.bishopList[enemyColor].size == 1) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KRKB

													drawFactor[myColor] = 1;
												}
											}
										}
									} else if (Hex88Board.rookList[enemyColor].size == 1) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KRKR

													drawFactor[myColor] = 0;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} else if (Hex88Board.queenList[myColor].size == 1) {
				if (Hex88Board.rookList[myColor].size == 0) {
					if (Hex88Board.bishopList[myColor].size == 0) {
						if (Hex88Board.knightList[myColor].size == 0) {
							if (Hex88Board.pawnList[myColor].size == 0) {
								// KQK*

								if (Hex88Board.queenList[enemyColor].size == 1) {
									if (Hex88Board.rookList[enemyColor].size == 0) {
										if (Hex88Board.bishopList[enemyColor].size == 0) {
											if (Hex88Board.knightList[enemyColor].size == 0) {
												if (Hex88Board.pawnList[enemyColor].size == 0) {
													// KQKQ

													drawFactor[myColor] = 0;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} // for
	}

	private static void evaluatePawn(int myColor) {
		assert myColor != IntColor.NOCOLOR;

		// Initialize
		byte[] myAttackTable = attackTable[myColor];
		byte[] myPawnTable = pawnTable[myColor];
		PositionList myPawnList = Hex88Board.pawnList[myColor];
		
		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			int pawnFile = IntPosition.getFile(pawnPosition);
			int pawnRank = IntPosition.getRank(pawnPosition);
			
			// Fill attack table
			for (int j = 1; j < MoveGenerator.moveDeltaPawn.length; j++) {
				int delta = MoveGenerator.moveDeltaPawn[j];

				// Attack Table
				int targetPosition = pawnPosition;
				if (myColor == IntColor.WHITE) {
					targetPosition += delta;
				} else {
					assert myColor == IntColor.BLACK;
					
					targetPosition -= delta;
				}
				if ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_PAWN;
				}
			}

			// Fill pawn table
			int tableFile = pawnFile + 1;
			if (myPawnTable[tableFile] == 0
					|| (myPawnTable[tableFile] > pawnRank && myColor == IntColor.WHITE)
					|| (myPawnTable[tableFile] < pawnRank && myColor == IntColor.BLACK)) {
				// Set the rank to the lowest pawn rank
				myPawnTable[tableFile] = (byte) pawnRank;
			}
		}
	}
	
	private static void evaluateKnight(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;

		// Initialize
		int[] total = totalKnight[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		PositionList myKnightList = Hex88Board.knightList[myColor];
		
		// Evaluate each knight
		for (int i = 0; i < myKnightList.size; i++) {
			int knightPosition = myKnightList.position[i];

			int allMobility = EVAL_KNIGHT_MOBILITY_BASE;

			// Fill attack table and evaluate mobility
			for (int delta : MoveGenerator.moveDeltaKnight) {
				int targetPosition = knightPosition + delta;
				if ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_MINOR;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						allMobility++;
					} else {
						if (IntChessman.getColor(target) == enemyColor) {
							allMobility++;
						}
					}
				}
			}
			
			// Evaluate mobility
			total[TOTAL_OPENING] += EVAL_KNIGHT_MOBILITYFACTOR_OPENING * allMobility;
			total[TOTAL_ENDGAME] += EVAL_KNIGHT_MOBILITYFACTOR_ENDGAME * allMobility;

			// Evaluate safety
			if ((enemyAttackTable[knightPosition] & BIT_PAWN) == 0) {
				total[TOTAL_OPENING] += EVAL_KNIGHT_SAFETY;
				total[TOTAL_ENDGAME] += EVAL_KNIGHT_SAFETY;
			}
		}
	}
	
	private static void evaluateBishop(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalBishop[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		PositionList myBishopList = Hex88Board.bishopList[myColor];
		
		// Evaluate each bishop
		for (int i = 0; i < myBishopList.size; i++) {
			int bishopPosition = myBishopList.position[i];

			int allMobility = EVAL_BISHOP_MOBILITY_BASE;

			// Fill attack table and evaluate mobility
			for (int delta : MoveGenerator.moveDeltaBishop) {
				int targetPosition = bishopPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_MINOR;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						allMobility++;
						targetPosition += delta;
					} else {
						if (IntChessman.getColor(target) == enemyColor) {
							allMobility++;
						}
						break;
					}
				}
			}

			// Evaluate mobility
			total[TOTAL_OPENING] += EVAL_BISHOP_MOBILITYFACTOR_OPENING * allMobility;
			total[TOTAL_ENDGAME] += EVAL_BISHOP_MOBILITYFACTOR_ENDGAME * allMobility;

			// Evaluate safety
			if ((enemyAttackTable[bishopPosition] & BIT_PAWN) == 0) {
				total[TOTAL_OPENING] += EVAL_BISHOP_SAFETY;
				total[TOTAL_ENDGAME] += EVAL_BISHOP_SAFETY;
			}
		}

		// Evaluate bishop pair
		if (myBishopList.size >= 2) {
			total[TOTAL_OPENING] += EVAL_BISHOP_PAIR;
			total[TOTAL_ENDGAME] += EVAL_BISHOP_PAIR;
		}
	}
	
	private static void evaluateRook(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalRook[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		byte[] myPawnTable = pawnTable[myColor];
		byte[] enemyPawnTable = pawnTable[enemyColor];
		PositionList myRookList = Hex88Board.rookList[myColor];
		
		int totalRook7th = 0;
		
		// Evaluate each rook
		for (int i = 0; i < myRookList.size; i++) {
			int rookPosition = myRookList.position[i];
			int rookFile = IntPosition.getFile(rookPosition);
			int rookRank = IntPosition.getRank(rookPosition);
			int tableFile = rookFile + 1;

			int allMobility = EVAL_ROOK_MOBILITY_BASE;

			// Fill attack table and evaluate mobility
			for (int delta : MoveGenerator.moveDeltaRook) {
				int targetPosition = rookPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_ROOK;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						allMobility++;
						targetPosition += delta;
					} else {
						if (IntChessman.getColor(target) == enemyColor) {
							allMobility++;
						}
						break;
					}
				}
			}

			// Evaluate mobility
			total[TOTAL_OPENING] += EVAL_ROOK_MOBILITYFACTOR_OPENING * allMobility;
			total[TOTAL_ENDGAME] += EVAL_ROOK_MOBILITYFACTOR_ENDGAME * allMobility;
			
			// Evaluate safety
			if ((enemyAttackTable[rookPosition] & BIT_PAWN) == 0
					&& (enemyAttackTable[rookPosition] & BIT_MINOR) == 0) {
				total[TOTAL_OPENING] += EVAL_ROOK_SAFETY;
				total[TOTAL_ENDGAME] += EVAL_ROOK_SAFETY;
			}

			// Evaluate open file
			int totalOpenFile = 0;
			totalOpenFile -= EVAL_ROOK_OPENFILE / 2;
			if (myPawnTable[tableFile] == 0) {
				totalOpenFile += EVAL_ROOK_OPENFILE / 2;
				if (enemyPawnTable[tableFile] == 0) {
					totalOpenFile += EVAL_ROOK_OPENFILE / 2;
				}
				int kingPosition = Hex88Board.kingList[enemyColor].position[0];
				int kingFile = IntPosition.getFile(kingPosition);
				int delta = Math.abs(kingFile - rookFile);
				if (delta <= 1) {
					total[TOTAL_OPENING] += EVAL_ROOK_NEARKINGFILE;
					if (delta == 0) {
						total[TOTAL_OPENING] += EVAL_ROOK_NEARKINGFILE;
					}
				}
			}
			total[TOTAL_OPENING] += totalOpenFile;
			total[TOTAL_ENDGAME] += totalOpenFile;
			
			// Evaluate 7th rank
			int seventhRank = 6;
			int eighthRank = 7;
			if (myColor == IntColor.BLACK) {
				seventhRank = 1;
				eighthRank = 0;
			} else {
				assert myColor == IntColor.WHITE;
			}
			if (rookRank == seventhRank) {
				int kingPosition = Hex88Board.kingList[enemyColor].position[0];
				int kingRank = IntPosition.getRank(kingPosition);
				boolean enemyPawnExists = false;
				for (int j = 1; j < enemyPawnTable.length - 1; j++) {
					if (enemyPawnTable[j] == seventhRank) {
						enemyPawnExists = true;
						break;
					}
				}
				if (enemyPawnExists || kingRank == eighthRank) {
					totalRook7th++;
					total[TOTAL_OPENING] += EVAL_ROOK_SEVENTHRANK_OPENING;
					total[TOTAL_ENDGAME] += EVAL_ROOK_SEVENTHRANK_ENDGAME;
				}
			}
		}
		
		// Check whether we have both rooks on the 7th rank
		if (totalRook7th == 2) {
			total[TOTAL_OPENING] += EVAL_ROOK_SEVENTHRANK_BONUS;
			total[TOTAL_ENDGAME] += EVAL_ROOK_SEVENTHRANK_BONUS;
		}
	}
	
	private static void evaluateQueen(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalQueen[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		byte[] enemyPawnTable = pawnTable[enemyColor];
		PositionList myQueenList = Hex88Board.queenList[myColor];
		
		// Evaluate the queen
		for (int i = 0; i < myQueenList.size; i++) {
			int queenPosition = myQueenList.position[i];
			int queenRank = IntPosition.getRank(queenPosition);

			int allMobility = EVAL_QUEEN_MOBILITY_BASE;

			// Fill attack table and evaluate mobility
			for (int delta : MoveGenerator.moveDeltaQueen) {
				int targetPosition = queenPosition + delta;
				while ((targetPosition & 0x88) == 0) {
					myAttackTable[targetPosition]++;
					myAttackTable[targetPosition] |= BIT_QUEEN;
					
					int target = Hex88Board.board[targetPosition];
					if (target == IntChessman.NOPIECE) {
						allMobility++;
						targetPosition += delta;
					} else {
						if (IntChessman.getColor(target) == enemyColor) {
							allMobility++;
						}
						break;
					}
				}
			}

			// Evaluate mobility
			total[TOTAL_OPENING] += EVAL_QUEEN_MOBILITYFACTOR_OPENING * allMobility;
			total[TOTAL_ENDGAME] += EVAL_QUEEN_MOBILITYFACTOR_ENDGAME * allMobility;

			// Evaluate safety
			if ((enemyAttackTable[queenPosition] & BIT_PAWN) == 0
					&& (enemyAttackTable[queenPosition] & BIT_MINOR) == 0
					&& (enemyAttackTable[queenPosition] & BIT_ROOK) == 0) {
				total[TOTAL_OPENING] += EVAL_QUEEN_SAFETY;
				total[TOTAL_ENDGAME] += EVAL_QUEEN_SAFETY;
			}

			// Evaluate 7th rank
			int seventhRank = 6;
			int eighthRank = 7;
			if (myColor == IntColor.BLACK) {
				seventhRank = 1;
				eighthRank = 0;
			} else {
				assert myColor == IntColor.WHITE;
			}
			if (queenRank == seventhRank) {
				int kingPosition = Hex88Board.kingList[enemyColor].position[0];
				int kingRank = IntPosition.getRank(kingPosition);
				boolean enemyPawnExists = false;
				for (int j = 1; j < enemyPawnTable.length - 1; j++) {
					if (enemyPawnTable[j] == seventhRank) {
						enemyPawnExists = true;
						break;
					}
				}
				if (enemyPawnExists || kingRank == eighthRank) {
					total[TOTAL_OPENING] += EVAL_QUEEN_SEVENTHRANK_OPENING;
					total[TOTAL_ENDGAME] += EVAL_QUEEN_SEVENTHRANK_ENDGAME;
				}
			}
		}
	}
	
	private static void evaluateKing(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalKing[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		PositionList myKingList = Hex88Board.kingList[myColor];
		
		// Evaluate the king
		assert myKingList.size == 1;
		int kingPosition = myKingList.position[0];

		// Fill attack table
		for (int delta : MoveGenerator.moveDeltaKing) {
			int targetPosition = kingPosition + delta;
			if ((targetPosition & 0x88) == 0) {
				myAttackTable[targetPosition]++;
				myAttackTable[targetPosition] |= BIT_KING;
			}
		}

		// Evaluate king safety
		int attackedSquare = IntPosition.NOPOSITION;
		int attackCount = 0;
		byte flag = 0;

		int sign = 1;
		int castlingKingside = IntCastling.WHITE_KINGSIDE;
		int castlingQueenside = IntCastling.WHITE_QUEENSIDE;
		if (myColor == IntColor.BLACK) {
			sign = -1;
			castlingKingside = IntCastling.BLACK_KINGSIDE;
			castlingQueenside = IntCastling.BLACK_QUEENSIDE;
		} else {
			assert myColor == IntColor.WHITE;
		}
		attackedSquare = kingPosition + 1;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition - 1;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition - sign * 15;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition - sign * 16;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition - sign * 17;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition + sign * 17;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition + sign * 16;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}
		attackedSquare = kingPosition + sign * 15;
		if ((attackedSquare & 0x88) == 0 && enemyAttackTable[attackedSquare] != 0) {
			attackCount += 4;
			flag |= enemyAttackTable[attackedSquare];
			int chessman = Hex88Board.board[attackedSquare];
			if (chessman == IntChessman.NOPIECE || IntChessman.getColor(chessman) == enemyColor) {
				attackCount += 3;
			}
			if (myAttackTable[attackedSquare] == -127) {
				attackCount += 1;
			}
		}

		attackCount /= 4;
		assert attackCount >= 0 && attackCount <= 16;
		assert KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] >= 0 && KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] <= 8;

		int kingSafety = (KING_ATTACK_PATTERN[(flag >>> 3) & MASK_ATTACKERS] * EVAL_KING_ATTACK * KING_ATTACK_EVAL[attackCount]) / 256;
		assert kingSafety >= 0 && kingSafety <= 8 * EVAL_KING_ATTACK;

		total[TOTAL_OPENING] -= kingSafety;

		int castlingPositionKingside = IntPosition.WHITE_CASTLING_KINGSIDE;
		int castlingPositionQueenside = IntPosition.WHITE_CASTLING_QUEENSIDE;
		if (myColor == IntColor.BLACK) {
			castlingPositionKingside = IntPosition.BLACK_CASTLING_KINGSIDE;
			castlingPositionQueenside = IntPosition.BLACK_CASTLING_QUEENSIDE;
		} else {
			assert myColor == IntColor.WHITE;
		}

		// Evaluate pawn shield
		int positionPenalty = getPawnShieldPenalty(myColor, kingPosition);
		int castlingPenalty = positionPenalty;
		
		if ((Hex88Board.castling & castlingKingside) != 0) {
			int tempPenalty = getPawnShieldPenalty(myColor, castlingPositionKingside);
			if (tempPenalty < castlingPenalty) {
				castlingPenalty = tempPenalty;
			}
		}
		if ((Hex88Board.castling & castlingQueenside) != 0) {
			int tempPenalty = getPawnShieldPenalty(myColor, castlingPositionQueenside);
			if (tempPenalty < castlingPenalty) {
				castlingPenalty = tempPenalty;
			}
		}
		
		int pawnShieldPenalty = (positionPenalty + castlingPenalty) / 2;
		
		total[TOTAL_OPENING] -= pawnShieldPenalty;
	}
	
	private static void evaluatePawnStructure(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalPawnStructure[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyAttackTable = attackTable[enemyColor];
		byte[] myPawnTable = pawnTable[myColor];
		PositionList myPawnList = Hex88Board.pawnList[myColor];

		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			int pawnFile = IntPosition.getFile(pawnPosition);
			int pawnRank = IntPosition.getRank(pawnPosition);
			int tableFile = pawnFile + 1;

			// Doubled pawns
			if (myPawnTable[tableFile] != pawnRank) {
				total[TOTAL_OPENING] -= EVAL_PAWN_DOUBLED_OPENING;
				total[TOTAL_ENDGAME] -= EVAL_PAWN_DOUBLED_ENDGAME;
			}
			
			// Isolated pawn
			if (myPawnTable[tableFile - 1] == 0 && myPawnTable[tableFile + 1] == 0) {
				total[TOTAL_OPENING] -= EVAL_PAWN_ISOLATED_OPENING;
				total[TOTAL_ENDGAME] -= EVAL_PAWN_ISOLATED_ENDGAME;
			}
			
			// Backward pawn
			else if ((myAttackTable[pawnPosition] & BIT_PAWN) == 0) {
				// We are not protected, check whether we have a backward pawn here

				boolean backward = false;
				int sign = 1;
				if (myColor == IntColor.WHITE) {
					if ((myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] > pawnRank)
							&& (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] > pawnRank)) {
						// We are behind the left and right pawn
						backward = true;
					}
				} else {
					assert myColor == IntColor.BLACK;
					
					if ((myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] < pawnRank)
							&& (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] < pawnRank)) {
						// We are behind the left and right pawn
						backward = true;
						sign = -1;
					}
				}
				
				if (backward) {
					// Really backward?
					
					if (myPawnTable[tableFile + 1] == pawnRank + sign * 1
							|| myPawnTable[tableFile - 1] == pawnRank + sign * 1) {
						// We are protecting a buddy on the left or right side
						// Check whether we can advance
						assert ((pawnPosition + sign * 16) & 0x88) == 0;
						int chessman = Hex88Board.board[pawnPosition + sign * 16];
						if ((chessman == IntChessman.NOPIECE || IntChessman.getChessman(chessman) != IntChessman.PAWN)
								&& (enemyAttackTable[pawnPosition] & BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 16] & BIT_PAWN) == 0) {
							backward = false;
						}
					} else if (pawnRank == 1
							&& (myPawnTable[tableFile + 1] == pawnRank + sign * 2
									|| myPawnTable[tableFile - 1] == pawnRank + sign * 2)) {
						// We can do a pawn double advance
						assert ((pawnPosition + sign * 32) & 0x88) == 0;
						int chessman1 = Hex88Board.board[pawnPosition + sign * 16];
						int chessman2 = Hex88Board.board[pawnPosition + sign * 32];
						if ((chessman1 == IntChessman.NOPIECE || IntChessman.getChessman(chessman1) != IntChessman.PAWN)
								&& (chessman2 == IntChessman.NOPIECE || IntChessman.getChessman(chessman2) != IntChessman.PAWN)
								&& (enemyAttackTable[pawnPosition] & BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 16] & BIT_PAWN) == 0
								&& (enemyAttackTable[pawnPosition + sign * 32] & BIT_PAWN) == 0) {
							backward = false;
						}
					}

					if (backward) {
						total[TOTAL_OPENING] -= EVAL_PAWN_BACKWARD_OPENING;
						total[TOTAL_ENDGAME] -= EVAL_PAWN_BACKWARD_ENDGAME;
					}
				}
			}
		}
	}

	private static void evaluatePawnPasser(int myColor, int enemyColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalPawnPasser[myColor];
		byte[] myAttackTable = attackTable[myColor];
		byte[] enemyPawnTable = pawnTable[enemyColor];
		PositionList myPawnList = Hex88Board.pawnList[myColor];

		assert Hex88Board.kingList[enemyColor].size == 1;
		int enemyKingPosition = Hex88Board.kingList[enemyColor].position[0];
		int enemyKingFile = IntPosition.getFile(enemyKingPosition);
		int enemyKingRank = IntPosition.getRank(enemyKingPosition);
		assert Hex88Board.kingList[myColor].size == 1;
		int myKingPosition = Hex88Board.kingList[myColor].position[0];
		int myKingFile = IntPosition.getFile(myKingPosition);
		int myKingRank = IntPosition.getRank(myKingPosition);

		// Evaluate each pawn
		for (int i = 0; i < myPawnList.size; i++) {
			int pawnPosition = myPawnList.position[i];
			int pawnFile = IntPosition.getFile(pawnPosition);
			int pawnRank = IntPosition.getRank(pawnPosition);
			int pawn = Hex88Board.board[pawnPosition];
			int tableFile = pawnFile + 1;

			// Passed pawn
			boolean isPasser = false;
			int sign = 1;
			int rankBonus = pawnRank;
			if (myColor == IntColor.WHITE) {
				if ((enemyPawnTable[tableFile] == 0 || enemyPawnTable[tableFile] < pawnRank)
						&& (enemyPawnTable[tableFile + 1] == 0 || enemyPawnTable[tableFile + 1] <= pawnRank)
						&& (enemyPawnTable[tableFile - 1] == 0 || enemyPawnTable[tableFile - 1] <= pawnRank)) {
					isPasser = true;

					if ((myAttackTable[pawnPosition] & BIT_ROOK) != 0) {
						// We are protected by a rook
						// Check whether the rook is in front of us
						int endPosition = pawnPosition + 16;
						for (int j = pawnRank + 1; j <= 7; j++) {
							int chessman = Hex88Board.board[endPosition];
							if (chessman != IntChessman.NOPIECE) {
								if (IntChessman.getChessman(chessman) == IntChessman.ROOK && IntChessman.getColor(chessman) == myColor) {
									// We have no bad rook
									isPasser = false;
								}
								break;
							}
							endPosition += 16;
						}
					}
				}
			} else {
				assert myColor == IntColor.BLACK;
				
				if ((enemyPawnTable[tableFile] == 0 || enemyPawnTable[tableFile] > pawnRank)
					&& (enemyPawnTable[tableFile + 1] == 0 || enemyPawnTable[tableFile + 1] >= pawnRank)
					&& (enemyPawnTable[tableFile - 1] == 0 || enemyPawnTable[tableFile - 1] >= pawnRank)) {
					isPasser = true;
					sign = -1;
					rankBonus = 7 - pawnRank;

					if ((myAttackTable[pawnPosition] & BIT_ROOK) != 0) {
						// We are protected by a rook
						// Check whether the rook is in front of us
						int endPosition = pawnPosition - 16;
						for (int j = pawnRank - 1; j >= 0; j--) {
							int chessman = Hex88Board.board[endPosition];
							if (chessman != IntChessman.NOPIECE) {
								if (IntChessman.getChessman(chessman) == IntChessman.ROOK && IntChessman.getColor(chessman) == myColor) {
									// We have no bad rook
									isPasser = false;
								}
								break;
							}
							endPosition -= 16;
						}
					}
				}
			}
			if (isPasser) {
				int bonus = 0;
				if (rankBonus >= 3) {
					bonus = rankBonus * rankBonus * rankBonus;
				}

				// Evaluate opening value
				total[TOTAL_OPENING] += EVAL_PAWN_PASSER_OPENING_MIN + ((EVAL_PAWN_PASSER_OPENING_MAX - EVAL_PAWN_PASSER_OPENING_MIN) * bonus) / EVAL_PAWN_PASSER_MAXBONUS;
				
				// Evaluate endgame value
				int endgameMax = EVAL_PAWN_PASSER_ENDGAME_MAX;

				// King distance
				int myKingDistance = Math.max(Math.abs(pawnFile - myKingFile), Math.abs(pawnRank - myKingRank));
				int enemyKingDistance = Math.max(Math.abs(pawnFile - enemyKingFile), Math.abs(pawnRank - enemyKingRank));
				endgameMax -= myKingDistance * EVAL_PAWN_MYKING_DISTANCE;
				endgameMax += enemyKingDistance * EVAL_PAWN_ENEMYKING_DISTANCE;

				if (Hex88Board.materialCount[enemyColor] == 0) {
					// Unstoppable passer
					if (myColor == IntColor.WHITE) {
						// Is a friendly chessman blocking our promotion path?
						boolean pathClear = true;
						int endPosition = pawnPosition + 16;
						for (int j = pawnRank + 1; j <= 7; j++) {
							int chessman = Hex88Board.board[endPosition];
							if (chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == myColor) {
								pathClear = false;
							}
							endPosition += 16;
						}

						if (pathClear) {
							int promotionDistance = 7 - pawnRank;
							if (pawnRank == 1) {
								// We can do a pawn double move
								promotionDistance--;
							}

							int enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), 7 - enemyKingRank);

							int difference = enemyKingPromotionDistance - promotionDistance;
							if (board.activeColor == enemyColor) {
								difference--;
							}
							if (difference >= 1) {
								endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE + 7 - promotionDistance;
							}
							
							// King protected passer
							else if (((promotionDistance <= 2 && (myAttackTable[pawnPosition] & BIT_KING) != 0)
									|| (promotionDistance <= 3 && (myAttackTable[pawnPosition + 16] & BIT_KING) != 0 && board.activeColor == myColor))
									&& (myKingFile != pawnFile
											|| (pawnFile != IntPosition.fileA && pawnFile != IntPosition.fileH))) {
								endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
							}
						}
					} else {
						assert myColor == IntColor.BLACK;
						
						// Is a friendly chessman blocking our promotion path?
						boolean pathClear = true;
						int endPosition = pawnPosition - 16;
						for (int j = pawnRank - 1; j >= 0; j--) {
							int chessman = Hex88Board.board[endPosition];
							if (chessman != IntChessman.NOPIECE && IntChessman.getColor(chessman) == myColor) {
								pathClear = false;
							}
							endPosition -= 16;
						}

						if (pathClear) {
							int promotionDistance = pawnRank;
							if (pawnRank == 6) {
								// We can do a pawn double move
								promotionDistance--;
							}

							int enemyKingPromotionDistance = Math.max(Math.abs(pawnFile - enemyKingFile), enemyKingRank);

							int difference = enemyKingPromotionDistance - promotionDistance;
							if (board.activeColor == enemyColor) {
								difference--;
							}
							if (difference >= 1) {
								endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE + 7 - promotionDistance;
							}

							// King protected passer
							else if (((promotionDistance <= 2 && (myAttackTable[pawnPosition] & BIT_KING) != 0)
									|| (promotionDistance <= 3 && (myAttackTable[pawnPosition - 16] & BIT_KING) != 0 && board.activeColor == myColor))
									&& (myKingFile != pawnFile
											|| (pawnFile != IntPosition.fileA && pawnFile != IntPosition.fileH))) {
								endgameMax += EVAL_PAWN_PASSER_UNSTOPPABLE;
							}
						}
					}
				} else {
					// Free passer
					assert ((pawnPosition + sign * 16) & 0x88) == 0;
					if (Hex88Board.board[pawnPosition + sign * 16] == IntChessman.NOPIECE) {
						// TODO: Do we have to consider promotion moves?
						int move = IntMove.createMove(IntMove.NORMAL, pawnPosition, pawnPosition + sign * 16, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
						if (MoveSee.seeMove(move, myColor) >= 0) {
							endgameMax += EVAL_PAWN_PASSER_FREE;
						}
					}
				}
				
				// Evaluate endgame value
				total[TOTAL_ENDGAME] += EVAL_PAWN_PASSER_ENDGAME_MIN;
				if (endgameMax - EVAL_PAWN_PASSER_ENDGAME_MIN > 0) {
					total[TOTAL_ENDGAME] += ((endgameMax - EVAL_PAWN_PASSER_ENDGAME_MIN) * bonus) / EVAL_PAWN_PASSER_MAXBONUS;
				}
			}
		}
	}

	private static void evaluatePatterns(int myColor, Hex88Board board) {
		assert myColor != IntColor.NOCOLOR;
		assert board != null;

		// Initialize
		int[] total = totalPatterns[myColor];

		if (myColor == IntColor.WHITE) {
			// Trapped white bishop
			if (Hex88Board.board[IntPosition.a7] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.b6] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
				if (Hex88Board.board[IntPosition.c7] == IntChessman.BLACK_PAWN) {
					total[TOTAL_OPENING] -= 50;
					total[TOTAL_ENDGAME] -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.b8] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.c7] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
			}
			if (Hex88Board.board[IntPosition.h7] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.g6] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
				if (Hex88Board.board[IntPosition.f7] == IntChessman.BLACK_PAWN) {
					total[TOTAL_OPENING] -= 50;
					total[TOTAL_ENDGAME] -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.g8] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.f7] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
			}
			if (Hex88Board.board[IntPosition.a6] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.b5] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
			if (Hex88Board.board[IntPosition.h6] == IntChessman.WHITE_BISHOP
					&& Hex88Board.board[IntPosition.g5] == IntChessman.BLACK_PAWN) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}

			// Blocked center pawn
			if (Hex88Board.board[IntPosition.d2] == IntChessman.WHITE_PAWN
					&& Hex88Board.board[IntPosition.d3] != IntChessman.NOPIECE) {
				total[TOTAL_OPENING] -= 20;
				total[TOTAL_ENDGAME] -= 20;
				if (Hex88Board.board[IntPosition.c1] == IntChessman.WHITE_BISHOP) {
					total[TOTAL_OPENING] -= 30;
					total[TOTAL_ENDGAME] -= 30;
				}
			}
			if (Hex88Board.board[IntPosition.e2] == IntChessman.WHITE_PAWN
					&& Hex88Board.board[IntPosition.e3] != IntChessman.NOPIECE) {
				total[TOTAL_OPENING] -= 20;
				total[TOTAL_ENDGAME] -= 20;
				if (Hex88Board.board[IntPosition.f1] == IntChessman.WHITE_BISHOP) {
					total[TOTAL_OPENING] -= 30;
					total[TOTAL_ENDGAME] -= 30;
				}
			}

			// Blocked rook
			if ((Hex88Board.board[IntPosition.c1] == IntChessman.WHITE_KING
					|| Hex88Board.board[IntPosition.b1] == IntChessman.WHITE_KING)
					&& (Hex88Board.board[IntPosition.a1] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.a2] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.b1] == IntChessman.WHITE_ROOK)) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
			if ((Hex88Board.board[IntPosition.f1] == IntChessman.WHITE_KING
					|| Hex88Board.board[IntPosition.g1] == IntChessman.WHITE_KING)
					&& (Hex88Board.board[IntPosition.h1] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.h2] == IntChessman.WHITE_ROOK
							|| Hex88Board.board[IntPosition.g1] == IntChessman.WHITE_ROOK)) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
		} else {
			assert myColor == IntColor.BLACK;

			// Trapped black bishop
			if (Hex88Board.board[IntPosition.a2] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.b3] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
				if (Hex88Board.board[IntPosition.c2] == IntChessman.WHITE_PAWN) {
					total[TOTAL_OPENING] -= 50;
					total[TOTAL_ENDGAME] -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.b1] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.c2] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
			}
			if (Hex88Board.board[IntPosition.h2] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.g3] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
				if (Hex88Board.board[IntPosition.f2] == IntChessman.WHITE_PAWN) {
					total[TOTAL_OPENING] -= 50;
					total[TOTAL_ENDGAME] -= 50;
				}
			}
			if (Hex88Board.board[IntPosition.g1] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.f2] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 100;
				total[TOTAL_ENDGAME] -= 100;
			}
			if (Hex88Board.board[IntPosition.a3] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.b4] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
			if (Hex88Board.board[IntPosition.h3] == IntChessman.BLACK_BISHOP
					&& Hex88Board.board[IntPosition.g4] == IntChessman.WHITE_PAWN) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}

			// Blocked center pawn
			if (Hex88Board.board[IntPosition.d7] == IntChessman.BLACK_PAWN
					&& Hex88Board.board[IntPosition.d6] != IntChessman.NOPIECE) {
				total[TOTAL_OPENING] -= 20;
				total[TOTAL_ENDGAME] -= 20;
				if (Hex88Board.board[IntPosition.c8] == IntChessman.BLACK_BISHOP) {
					total[TOTAL_OPENING] -= 30;
					total[TOTAL_ENDGAME] -= 30;
				}
			}
			if (Hex88Board.board[IntPosition.e7] == IntChessman.BLACK_PAWN
					&& Hex88Board.board[IntPosition.e6] != IntChessman.NOPIECE) {
				total[TOTAL_OPENING] -= 20;
				total[TOTAL_ENDGAME] -= 20;
				if (Hex88Board.board[IntPosition.f8] == IntChessman.BLACK_BISHOP) {
					total[TOTAL_OPENING] -= 30;
					total[TOTAL_ENDGAME] -= 30;
				}
			}

			// Blocked rook
			if ((Hex88Board.board[IntPosition.c8] == IntChessman.BLACK_KING
					|| Hex88Board.board[IntPosition.b8] == IntChessman.BLACK_KING)
					&& (Hex88Board.board[IntPosition.a8] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.a7] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.b8] == IntChessman.BLACK_ROOK)) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
			if ((Hex88Board.board[IntPosition.f8] == IntChessman.BLACK_KING
					|| Hex88Board.board[IntPosition.g8] == IntChessman.BLACK_KING)
					&& (Hex88Board.board[IntPosition.h8] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.h7] == IntChessman.BLACK_ROOK
							|| Hex88Board.board[IntPosition.g8] == IntChessman.BLACK_ROOK)) {
				total[TOTAL_OPENING] -= 50;
				total[TOTAL_ENDGAME] -= 50;
			}
		}
	}

	private static int getPawnShieldPenalty(int myColor, int kingPosition) {
		assert myColor != IntColor.NOCOLOR;
		assert (kingPosition & 0x88) == 0;
		
		// Initialize
		byte[] myPawnTable = pawnTable[myColor];

		int kingFile = IntPosition.getFile(kingPosition);
		int kingRank = IntPosition.getRank(kingPosition);
		int tableFile = kingFile + 1;

		// Evaluate pawn shield
		int penalty = 0;
		if (myColor == IntColor.WHITE) {
			// Evaluate the file of the king
			if (myPawnTable[tableFile] == 0 || myPawnTable[tableFile] < kingRank) {
				penalty += 2 * 36;
			} else {
				penalty += 2 * (myPawnTable[tableFile] - 1) * (myPawnTable[tableFile] - 1);
			}
			// Evaluate the file at the left of the king
			if (kingFile != 0) {
				// We are not at the left border
				if (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] < kingRank) {
					penalty += 36;
				} else {
					penalty += (myPawnTable[tableFile - 1] - 1) * (myPawnTable[tableFile - 1] - 1);
				}
			}
			// Evaluate the file at the right of the king
			if (kingFile != 7) {
				// We are not at the right border
				if (myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] < kingRank) {
					penalty += 36;
				} else {
					penalty += (myPawnTable[tableFile + 1] - 1) * (myPawnTable[tableFile + 1] - 1);
				}
			}
		} else {
			assert myColor == IntColor.BLACK;

			// Evaluate the file of the king
			if (myPawnTable[tableFile] == 0 || myPawnTable[tableFile] > kingRank) {
				penalty += 2 * 36;
			} else {
				penalty += 2 * (7 - myPawnTable[tableFile] - 1) * (7 - myPawnTable[tableFile] - 1);
			}
			// Evaluate the file at the left of the king
			if (kingFile != 0) {
				// We are not at the left border
				if (myPawnTable[tableFile - 1] == 0 || myPawnTable[tableFile - 1] > kingRank) {
					penalty += 36;
				} else {
					penalty += (7 - myPawnTable[tableFile - 1] - 1) * (7 - myPawnTable[tableFile - 1] - 1);
				}
			}
			// Evaluate the file at the right of the king
			if (kingFile != 7) {
				// We are not at the right border
				if (myPawnTable[tableFile + 1] == 0 || myPawnTable[tableFile + 1] > kingRank) {
					penalty += 36;
				} else {
					penalty += (7 - myPawnTable[tableFile + 1] - 1) * (7 - myPawnTable[tableFile + 1] - 1);
				}
			}
		}
		
		return penalty;
	}

}
