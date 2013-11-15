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

import com.fluxchess.flux.InformationTimer;
import com.fluxchess.flux.move.PrincipalVariation;
import com.fluxchess.flux.table.*;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Parameter {

  public final long searchNodes;
  public final int showPvNumber;
  public final boolean analyzeMode;
  public final HashMap<Integer, PrincipalVariation> multiPvMap;

  public final AtomicBoolean stopped;
  public final AtomicBoolean canStop;
  public final InformationTimer info;

  public final TranspositionTable transpositionTable;
  public final EvaluationTable evaluationTable;
  public final PawnTable pawnTable;
  public final KillerTable killerTable;
  public final HistoryTable historyTable;

  public Parameter(
    long searchNodes,
    int showPvNumber,
    boolean analyzeMode,
    HashMap<Integer, PrincipalVariation> multiPvMap,
    AtomicBoolean stopped,
    AtomicBoolean canStop,
    InformationTimer info,
    TranspositionTable transpositionTable,
    EvaluationTable evaluationTable,
    PawnTable pawnTable,
    KillerTable killerTable,
    HistoryTable historyTable
  ) {
    this.searchNodes = searchNodes;
    this.showPvNumber = showPvNumber;
    this.analyzeMode = analyzeMode;
    this.multiPvMap = multiPvMap;

    this.stopped = stopped;
    this.canStop = canStop;
    this.info = info;

    this.transpositionTable = transpositionTable;
    this.evaluationTable = evaluationTable;
    this.pawnTable = pawnTable;
    this.killerTable = killerTable;
    this.historyTable = historyTable;
  }

}
