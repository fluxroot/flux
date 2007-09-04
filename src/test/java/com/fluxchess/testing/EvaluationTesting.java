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
package com.fluxchess.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jcpi.data.GenericBoard;
import jcpi.data.IllegalNotationException;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.evaluation.Evaluation;
import com.fluxchess.evaluation.IEvaluation;
import com.fluxchess.move.MoveSee;
import com.fluxchess.table.EvaluationTable;
import com.fluxchess.table.PawnTable;

/**
 * EvaluationTesting
 *
 * @author Phokham Nonava
 */
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
