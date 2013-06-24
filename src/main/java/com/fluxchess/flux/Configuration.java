/*
** Copyright 2007-2012 Phokham Nonava
**
** This file is part of Flux Chess.
**
** Flux Chess is free software: you can redistribute it and/or modify
** it under the terms of the GNU Lesser General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** Flux Chess is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fluxchess.flux;

import java.util.Hashtable;
import java.util.Properties;

import com.fluxchess.jcpi.data.Option;


/**
 * Configuration
 *
 * @author Phokham Nonava
 */
public final class Configuration {

    public static final String name = "Flux $version$";
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

    // Options
    public static int numberOfThreads = 1;
    public static boolean showRefutations = false;
    public static int showPvNumber = 1;
    public static boolean analyzeMode = false;

    public static final String KEY_Threads = "Threads";
    public static final String KEY_Ponder = "Ponder";
    public static final String KEY_MultiPv = "MultiPV";
    public static final String KEY_Hash = "Hash";
    public static final String KEY_ClearHash = "Clear Hash";
    public static final String KEY_HashEvaluation = "Evaluation Table";
    public static final String KEY_HashPawn = "Pawn Table";
    public static final String KEY_UCIShowRefutations = "UCI_ShowRefutations";
    public static final String KEY_UCIAnalyseMode = "UCI_AnalyseMode";

    private static final String TYPE_Check = "check";
    private static final String TYPE_Spin = "spin";
    private static final String TYPE_Button = "button";

    public static final Hashtable<String, Option> configuration = new Hashtable<String, Option>();

    static {
        configuration.put(KEY_Threads, new Option(KEY_Threads, TYPE_Spin, "1", "1", Integer.toString(Runtime.getRuntime().availableProcessors()), null));
        configuration.put(KEY_Ponder, new Option(KEY_Ponder, TYPE_Check, "true", null, null, null));
        configuration.put(KEY_MultiPv, new Option(KEY_MultiPv, TYPE_Spin, "1", "1", "256", null));
        configuration.put(KEY_Hash, new Option(KEY_Hash, TYPE_Spin, "16", "4", "256", null));
        configuration.put(KEY_ClearHash, new Option(KEY_ClearHash, TYPE_Button, null, null, null, null));
        configuration.put(KEY_HashEvaluation, new Option(KEY_HashEvaluation, TYPE_Spin, "4", "4", "64", null));
        configuration.put(KEY_HashPawn, new Option(KEY_HashPawn, TYPE_Spin, "4", "4", "16", null));
        configuration.put(KEY_UCIShowRefutations, new Option(KEY_UCIShowRefutations, TYPE_Check, "false", null, null, null));
        configuration.put(KEY_UCIAnalyseMode, new Option(KEY_UCIAnalyseMode, TYPE_Check, "false", null, null, null));
    }

    public Configuration() {
    }

    public static void setOption(String name, String value) {
        assert name != null;

        if (configuration.get(name) != null && !configuration.get(name).type.equals(TYPE_Button)) {
            assert value != null;
            configuration.get(name).setValue(value);

            if (name.equals(KEY_UCIShowRefutations)) {
                if (value.equalsIgnoreCase("true")) {
                    showRefutations = true;
                } else {
                    showRefutations = false;
                }
            } else if (name.equals(KEY_UCIAnalyseMode)) {
                if (value.equalsIgnoreCase("true")) {
                    analyzeMode = true;
                } else {
                    analyzeMode = false;
                }
            } else if (name.equals(KEY_MultiPv)) {
                try {
                    showPvNumber = new Integer(value);
                } catch (NumberFormatException e) {
                    showPvNumber = 1;
                }
            } else if (name.equals(KEY_Threads)) {
                try {
                    numberOfThreads = new Integer(value);
                } catch (NumberFormatException e) {
                    numberOfThreads = 1;
                }
            }
        }
    }

    public static void loadConfiguration() {
        // Get the default properties
        Properties defaultProperties = new Properties();
        for (Option option : configuration.values()) {
            assert option.name != null;

            if (!option.type.equals(TYPE_Button)) {
                assert option.defaultValue != null;
                defaultProperties.setProperty(option.name, option.defaultValue);
            }
        }
    }

}
