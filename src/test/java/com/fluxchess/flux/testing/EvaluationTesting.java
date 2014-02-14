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

import com.fluxchess.flux.board.Hex88Board;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.evaluation.IEvaluation;
import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.flux.move.MoveSee;
import com.fluxchess.flux.table.EvaluationTable;
import com.fluxchess.flux.table.PawnTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EvaluationTesting {

	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String token = reader.readLine();
			while (!token.equalsIgnoreCase("quit")) {
				IEvaluation evaluation = new Evaluation(new EvaluationTable(1024), new PawnTable(1024));
				try {
					Hex88Board board = new Hex88Board(new GenericBoard(token));
					new MoveSee(board);
					evaluation.print(board);
				} catch (IllegalNotationException e) {
					e.printStackTrace();
					break;
				}
				token = reader.readLine();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
