/*
 * Copyright (C) 2007-2014 Phokham Nonava
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
  static boolean useAspirationWindows = true;
  static boolean useInternalIterativeDeepening = true;

  // Pruning
  static boolean useMateDistancePruning = true;
  static boolean useNullMovePruning = true;
  static boolean useVerifiedNullMovePruning = true;
  static boolean useFutilityPruning = true;
  static boolean useExtendedFutilityPruning = true;
  static boolean useLateMoveReduction = true;
  static boolean useLateMoveReductionResearch = true;
  static boolean useDeltaPruning = true;
  static boolean useMinorPromotionPruning = true;

  // Tables
  static boolean useTranspositionTable = true;
  static boolean useKillerTable = true;
  static boolean useHistoryTable = true;
  static boolean useEvaluationTable = true;
  static boolean usePawnTable = true;

  // Extensions
  static boolean useSingleReplyExtension = true;
  static boolean useRecaptureExtension = true;
  static boolean useCheckExtension = true;
  static boolean usePawnExtension = true;
  static boolean useMateThreatExtension = true;

  static boolean ponder = true;
  static final CheckboxOption ponderOption = Options.newPonderOption(ponder);

  static final int defaultShowPvNumber = 1;
  static int showPvNumber = defaultShowPvNumber;
  static final SpinnerOption multiPVOption = Options.newMultiPVOption(showPvNumber, 1, 256);

  static final int defaultTranspositionTableSize = 16;
  static int transpositionTableSize = defaultTranspositionTableSize;
  static final SpinnerOption hashOption = Options.newHashOption(transpositionTableSize, 4, 256);

  static final ButtonOption clearHashOption = new ButtonOption("Clear Hash");

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
