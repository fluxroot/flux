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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jcpi.data.GenericBoard;
import jcpi.data.IllegalNotationException;

import com.fluxchess.board.Hex88Board;
import com.fluxchess.move.MoveSee;
import com.fluxchess.table.EvaluationTable;
import com.fluxchess.table.PawnTable;

/**
 * EvaluationOptimization
 *
 * @author Phokham Nonava
 */
public final class EvaluationOptimization {

	private final Map<GenericBoard, Integer> solutions = new HashMap<GenericBoard, Integer>();
	private final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	private double rss = Double.MAX_VALUE;

	public EvaluationOptimization(BufferedReader file) throws IOException, IllegalNotationException {
		if (file == null) throw new IllegalArgumentException();

		// Build solution database
		String line = file.readLine();
		while (line != null) {
			String[] tokens = line.split(";");
			
			if (tokens.length == 2) {
				String fen = tokens[0].trim();
				int value = Integer.parseInt(tokens[1].trim());
				
				this.solutions.put(new GenericBoard(fen), value);
			}

			line = file.readLine();
		}
		
		// Build parameter list
		this.parameters.add(new EVAL_KNIGHT_MOBILITY_BASE());
		this.parameters.add(new EVAL_KNIGHT_MOBILITYFACTOR());
		this.parameters.add(new EVAL_KNIGHT_SAFETY());
		this.parameters.add(new EVAL_BISHOP_MOBILITY_BASE());
		this.parameters.add(new EVAL_BISHOP_MOBILITYFACTOR());
		this.parameters.add(new EVAL_BISHOP_PAIR());
		this.parameters.add(new EVAL_BISHOP_SAFETY());
		this.parameters.add(new EVAL_ROOK_MOBILITY_BASE());
		this.parameters.add(new EVAL_ROOK_SAFETY());
		this.parameters.add(new EVAL_QUEEN_MOBILITY_BASE());
		this.parameters.add(new EVAL_QUEEN_SAFETY());
	}
	
	public static void main(String[] args) {
		try {
			BufferedReader file = null;
			try {
				file = new BufferedReader(new FileReader("evaluation.db"));
			} catch (FileNotFoundException e) {
				file = new BufferedReader(new FileReader("src/test/resources/evaluation.db"));
			}

			EvaluationOptimization optimization = new EvaluationOptimization(file);

			optimization.calculate();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalNotationException e) {
			e.printStackTrace();
		}
	}

	public void calculate() {
		loop(0);
	}
	
	private void print() {
		System.out.println("Found new parameter solution with rss = " + this.rss);
		for (Parameter parameter : this.parameters) {
			parameter.print();
		}
	}
	
	private double loop(int i) {
		if (i < this.parameters.size()) {
			Parameter parameter = this.parameters.get(i);

			double baserss = loop(i + 1);
			double newrss = baserss;

			// +
			for (int increment = 1; increment <= parameter.range; increment++) {
				parameter.set(parameter.defaultValue + increment);
				double looprss = loop(i + 1);
				
				if (looprss >= newrss) {
					break;
				} else {
					newrss = looprss;
				}
			}
			
			if (newrss == baserss) {
				// -
				for (int increment = 1; increment <= parameter.range; increment++) {
					parameter.set(parameter.defaultValue - increment);
					double looprss = loop(i + 1);

					if (looprss >= newrss) {
						break;
					} else {
						newrss = looprss;
					}
				}
			}

			return newrss;
		} else {
			return evaluate();
		}
	}
	
	private double evaluate() {
		double newrss = 0;
		for (Entry<GenericBoard, Integer> entry : this.solutions.entrySet()) {
			GenericBoard genericBoard = entry.getKey();
			int value = entry.getValue();

			Hex88Board board = new Hex88Board(genericBoard);
			new MoveSee(board);
			Evaluation evaluation = new Evaluation(new EvaluationTable(1), new PawnTable(1));
			int result = evaluation.evaluate(board);
			
			newrss += Math.pow(value - result, 2);
		}
		
		if (newrss < rss) {
			for (Parameter parameter : this.parameters) {
				parameter.store();
			}
			
			rss = newrss;

			print();
		}
		
		return newrss;
	}

}
