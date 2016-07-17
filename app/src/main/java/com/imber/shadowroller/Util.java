package com.imber.shadowroller;

import android.content.res.Resources;
import android.net.Uri;

import com.imber.shadowroller.data.DbContract;


import java.util.ArrayList;
import java.util.Random;

public class Util {
    private static final String TAG = "Util";

    public enum TestType {
        SIMPLE_TEST, EXTENDED_TEST, PROBABILITY;

        public static TestType fromInt(int type) {
            switch (type) {
                case 0: return SIMPLE_TEST;
                case 1: return EXTENDED_TEST;
                case 2: return PROBABILITY;
                default: return SIMPLE_TEST;
            }
        }
    }

    public enum TestModifier {
        NONE, RULE_OF_SIX, PUSH_THE_LIMIT;

        public static TestModifier fromInt(int modifier) {
            switch (modifier) {
                case 0: return NONE;
                case 1: return RULE_OF_SIX;
                case 2: return PUSH_THE_LIMIT;
                default: return NONE;
            }
        }
    }

    public enum RollStatus {
        NORMAL, CRITICAL_SUCCESS, GLITCH, CRITICAL_GLITCH;

        public static RollStatus fromInt(int status) {
            switch (status) {
                case 0: return NORMAL;
                case 1: return CRITICAL_SUCCESS;
                case 2: return GLITCH;
                case 3: return CRITICAL_GLITCH;
                default: return NORMAL;
            }
        }
    }

    public interface SimpleTestDiceListener {
        void onRollPerformed(ArrayList<int[]> output, TestModifier modifier);
    }

    public interface ExtendedTestDiceListener {
        void onExtendedRollPerformed(ArrayList<int[]> output);
    }

    public interface ProbabilityDiceListener {
        void onProbabilityQueried(Uri uri);
    }

    public static ArrayList<int[]> doSimpleRoll( Random random, int dice, TestModifier modifier) {
        ArrayList<int[]> result = new ArrayList<>();
        switch (modifier) {
            case NONE:
                result.add(doSingleRoll(random, dice));
                break;
            case RULE_OF_SIX:
                int numSixes = dice;
                do {
                    int[] roll = doSingleRoll(random, numSixes);
                    result.add(roll);
                    numSixes = countSixes(roll);
                } while (numSixes > 0);
                break;
            case PUSH_THE_LIMIT:
                int[] roll = doSingleRoll(random, dice);
                result.add(roll);
                int successes = countSuccesses(result);
                if (successes != dice) {
                    int[] reRoll = doSingleRoll(random, dice - successes);
                    result.add(reRoll);
                }
                break;
        }
        return result;
    }

    public static ArrayList<int[]> doExtendedRoll(Random random, int dice) {
        ArrayList<int[]> result = new ArrayList<>(dice);
        for (int i = 0; i < dice; i++) {
            result.add(Util.doSingleRoll(random, dice - i));
        }
        return result;
    }

    public static int[] doSingleRoll(Random random, int diceNumber) {
        int[] result = new int[diceNumber];
        for (int i = 0; i < diceNumber; i++) {
            result[i] = random.nextInt(6) + 1;
        }
        return result;
    }

    public static int countSixes(int[] diceResult) {
        int sixes = 0;
        for (int d : diceResult) {
            if (d == 6) sixes++;
        }
        return sixes;
    }

    public static int countSuccesses(ArrayList<int[]> diceResult) {
        int successes = 0;
        for (int[] roll : diceResult) {
            for (int d : roll) {
                if (d == 5 || d == 6) successes++;
            }
        }
        return successes;
    }

    public static int countSuccesses(int[] diceResult) {
        int successes = 0;
        for (int d : diceResult) {
            if (d == 5 || d == 6) successes++;
        }
        return successes;
    }

    public static int getBoundedDiceNumber(Resources res, int dice, TestType testType) {
        int maxDiceNumberId = testType == TestType.PROBABILITY ?
                R.integer.max_probability_dice_number :
                R.integer.max_dice_number;
        return Math.min(Math.max(1, dice), res.getInteger(maxDiceNumberId));
    }

    // TODO: implement this
    public static RollStatus getRollStatus(int[] result) {
        return RollStatus.NORMAL;
    }

    public static String resultToOutput(int[] diceResult) {
        String display = "";
        for (int i = 0; i < diceResult.length - 1; i++) {
            display += String.valueOf(diceResult[i]) + ", ";
        }
        display += String.valueOf(diceResult[diceResult.length - 1]) + "\n";
        return display;
    }

    public static String getProbabilityFilename(Resources res, TestModifier modifier, boolean cumulative) {
        String filename = "";
        switch (modifier) {
            case NONE:
                filename = res.getString(cumulative ?
                        R.string.file_name_probability_normal_cumulative :
                        R.string.file_name_probability_normal);
                break;
            case RULE_OF_SIX:
                filename = res.getString(cumulative ?
                        R.string.file_name_probability_rule_of_six_cumulative :
                        R.string.file_name_probability_rule_of_six);
                break;
            case PUSH_THE_LIMIT:
                filename = res.getString(cumulative ?
                        R.string.file_name_probability_push_the_limit_cumulative :
                        R.string.file_name_probability_push_the_limit);
                break;
        }
        return filename;
    }

    public static Uri buildProbabilityUri(Util.TestModifier modifier, int dice, boolean cumulative) {
        Uri uri = getContentUriFromModifier(modifier, cumulative);
        return uri.buildUpon().appendPath(String.valueOf(dice)).build();
    }

    public static Uri getContentUriFromModifier(Util.TestModifier modifier, boolean cumulative) {
        Uri uri = null;
        switch (modifier) {
            case NONE:
                uri = cumulative ?
                        DbContract.NormalCumulativeProbabilityTable.CONTENT_URI :
                        DbContract.NormalProbabilityTable.CONTENT_URI;
                break;
            case RULE_OF_SIX:
                uri = cumulative ?
                        DbContract.RuleOfSixCumulativeProbabilityTable.CONTENT_URI :
                        DbContract.RuleOfSixProbabilityTable.CONTENT_URI;
                break;
            case PUSH_THE_LIMIT:
                uri = cumulative ?
                        DbContract.PushTheLimitCumulativeProbabilityTable.CONTENT_URI :
                        DbContract.PushTheLimitProbabilityTable.CONTENT_URI;
                break;
        }
        return uri;
    }
}
