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

import com.fluxchess.jcpi.options.*;

public final class Configuration {

	public static final String name = "Flux 2.2.2";
	public static final String author = "Phokham Nonava";
	
	// Search
	public static final boolean useAspirationWindows = true;
	public static final boolean useInternalIterativeDeepening = true;

	// Pruning
	public static final boolean useMateDistancePruning = true;
	public static final boolean useNullMovePruning = true;
	public static final boolean useVerifiedNullMovePruning = true;
	public static final boolean useFutilityPruning = true;
	public static final boolean useExtendedFutilityPruning = true;
	public static final boolean useLateMoveReduction = true;
	public static final boolean useLateMoveReductionResearch = true;
	public static final boolean useDeltaPruning = true;
	public static final boolean useMinorPromotionPruning = true;
	
	// Tables
	public static final boolean useTranspositionTable = true;
	public static final boolean useKillerTable = true;
	public static final boolean useHistoryTable = true;
	public static final boolean useEvaluationTable = true;
	public static final boolean usePawnTable = true;

	// Extensions
	public static final boolean useSingleReplyExtension = true;
	public static final boolean useRecaptureExtension = true;
	public static final boolean useCheckExtension = true;
	public static final boolean usePawnExtension = true;
	public static final boolean useMateThreatExtension = true;
	
	public static boolean ponder = true;
	public static final CheckboxOption ponderOption = Options.newPonderOption(ponder);
	
	public static final int defaultShowPvNumber = 1;
	public static int showPvNumber = defaultShowPvNumber;
	public static final SpinnerOption multiPVOption = Options.newMultiPVOption(showPvNumber, 1, 256);
	
	public static final int defaultTranspositionTableSize = 16;
	public static int transpositionTableSize = defaultTranspositionTableSize;
	public static final SpinnerOption hashOption = Options.newHashOption(transpositionTableSize, 4, 256);

	public static final ButtonOption clearHashOption = new ButtonOption("Clear Hash");

	public static final int defaultEvaluationTableSize = 4;
	public static int evaluationTableSize = defaultEvaluationTableSize;
	public static final SpinnerOption evaluationTableOption = new SpinnerOption("Evaluation Table", evaluationTableSize, 4, 64);
	
	public static final int defaultPawnTableSize = 4;
	public static int pawnTableSize = defaultPawnTableSize;
	public static final SpinnerOption pawnTableOption = new SpinnerOption("Pawn Table", pawnTableSize, 4, 64);

	public static boolean showRefutations = false;
	public static final CheckboxOption uciShowRefutationsOption = Options.newUciShowRefutationsOption(showRefutations);

	public static boolean analyzeMode = false;
	public static final CheckboxOption uciAnalyzeModeOption = Options.newUciAnalyseModeOption(analyzeMode);

	public static final AbstractOption[] options = new AbstractOption[]{
			ponderOption,
			multiPVOption,
			hashOption,
			clearHashOption,
			evaluationTableOption,
			pawnTableOption,
			uciShowRefutationsOption,
			uciAnalyzeModeOption
	};

	private Configuration() {
	}

}
