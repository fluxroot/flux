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

final class Configuration {

  // Search
  static final boolean useAspirationWindows = true;
  static final boolean useInternalIterativeDeepening = true;

  // Pruning
  static final boolean useMateDistancePruning = true;
  static final boolean useNullMovePruning = true;
  static final boolean useVerifiedNullMovePruning = true;
  static final boolean useFutilityPruning = true;
  static final boolean useExtendedFutilityPruning = true;
  static final boolean useLateMoveReduction = true;
  static final boolean useLateMoveReductionResearch = true;
  static final boolean useDeltaPruning = true;
  static final boolean useMinorPromotionPruning = true;

  // Tables
  static final boolean useTranspositionTable = true;
  static final boolean useKillerTable = true;
  static final boolean useHistoryTable = true;
  static final boolean useEvaluationTable = true;
  static final boolean usePawnTable = true;

  // Extensions
  static final boolean useSingleReplyExtension = true;
  static final boolean useRecaptureExtension = true;
  static final boolean useCheckExtension = true;
  static final boolean usePawnExtension = true;
  static final boolean useMateThreatExtension = true;

  static boolean ponder = true;
  static final CheckboxOption ponderOption = Options.newPonderOption(ponder);

  static final int defaultShowPvNumber = 1;
  static int showPvNumber = defaultShowPvNumber;
  static final SpinnerOption multiPVOption = Options.newMultiPVOption(showPvNumber, 1, 256);

  static final int defaultTranspositionTableSize = 16;
  static int transpositionTableSize = defaultTranspositionTableSize;
  static final SpinnerOption hashOption = Options.newHashOption(transpositionTableSize, 4, 256);

  static final ButtonOption clearHashOption = new ButtonOption("Clear Hash");

  static final int defaultEvaluationTableSize = 4;

  static final int defaultPawnTableSize = 4;

  static boolean showRefutations = false;
  static final CheckboxOption uciShowRefutationsOption = Options.newUciShowRefutationsOption(showRefutations);

  static boolean analyzeMode = false;
  static final CheckboxOption uciAnalyzeModeOption = Options.newUciAnalyseModeOption(analyzeMode);

  static final AbstractOption[] options = new AbstractOption[]{
      ponderOption,
      multiPVOption,
      hashOption,
      clearHashOption,
      uciShowRefutationsOption,
      uciAnalyzeModeOption
  };

  private Configuration() {
  }

}
