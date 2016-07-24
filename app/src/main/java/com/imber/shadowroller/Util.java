package com.imber.shadowroller;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.DrawableRes;

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

        public int toInt() {
            switch (this) {
                case SIMPLE_TEST: return 0;
                case EXTENDED_TEST: return 1;
                case PROBABILITY: return 2;
                default: return 0;
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

        public int toInt() {
            switch (this) {
                case NONE: return 0;
                case RULE_OF_SIX: return 1;
                case PUSH_THE_LIMIT: return 2;
                default: return 0;
            }
        }
    }

    public enum RollStatus {
        NORMAL, GLITCH, CRITICAL_GLITCH;

        public static RollStatus fromInt(int status) {
            switch (status) {
                case 0: return NORMAL;
                case 1: return GLITCH;
                case 2: return CRITICAL_GLITCH;
                default: return NORMAL;
            }
        }

        public int toInt() {
            switch (this) {
                case NORMAL: return 0;
                case GLITCH: return 1;
                case CRITICAL_GLITCH: return 2;
                default: return 0;
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

    public static int countOnes(int[] diceResult) {
        int ones = 0;
        for (int d : diceResult) {
            if (d == 1) ones++;
        }
        return ones;
    }

    public static int countSuccesses(int[] diceResult) {
        int successes = 0;
        for (int d : diceResult) {
            if (d == 5 || d == 6) successes++;
        }
        return successes;
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

    public static int getBoundedDiceNumber(Resources res, int dice, TestType testType) {
        int maxDiceNumberId = testType == TestType.PROBABILITY ?
                R.integer.max_probability_dice_number :
                R.integer.max_dice_number;
        return Math.min(Math.max(1, dice), res.getInteger(maxDiceNumberId));
    }

    public static RollStatus getRollStatus(int[] result) {
        int successes = countSuccesses(result);
        int ones = countOnes(result);
        if (ones > result.length / 2) {
            if (successes == 0) {
                return RollStatus.CRITICAL_GLITCH;
            }
            return RollStatus.GLITCH;
        }
        return RollStatus.NORMAL;
    }

    @DrawableRes
    public static int getResultCircleIdFromRollStatus(RollStatus status) {
        switch (status) {
            case NORMAL: return R.drawable.roll_result_circle;
            case GLITCH: return R.drawable.roll_result_circle_glitch;
            case CRITICAL_GLITCH: return R.drawable.roll_result_circle_critical_glitch;
            default: return R.drawable.roll_result_circle;
        }
    }

    public static int getColorFromRollStatus(Resources res, RollStatus status) {
        switch (status) {
            case GLITCH: return res.getColor(R.color.glitch);
            case CRITICAL_GLITCH: return res.getColor(R.color.critical_glitch);
            default: return res.getColor(R.color.gray);
        }
    }

    public static String getNameFromTestType(Resources res, TestType type) {
        switch (type) {
            case SIMPLE_TEST: return res.getString(R.string.name_test_simple);
            case EXTENDED_TEST: return res.getString(R.string.name_test_extended);
            default: return res.getString(R.string.list_item_history_default_name);
        }
    }

    public static String getNameFromTestModifier(Resources res, TestModifier modifier) {
        switch (modifier) {
            case RULE_OF_SIX: return res.getString(R.string.modifier_rule_of_six);
            case PUSH_THE_LIMIT: return res.getString(R.string.modifier_push_the_limit);
            default: return res.getString(R.string.list_item_history_default_name);
        }
    }

    public static String getNameFromRollStatus(Resources res, RollStatus status) {
        switch (status) {
            case GLITCH: return res.getString(R.string.glitch);
            case CRITICAL_GLITCH: return res.getString(R.string.critical_glitch);
            default: return "";
        }
    }

    public static String resultToOutput(int[] diceResult) {
        String display = "";
        for (int i = 0; i < diceResult.length - 1; i++) {
            display += String.valueOf(diceResult[i]) + ", ";
        }
        display += String.valueOf(diceResult[diceResult.length - 1]) + "\n";
        return display;
    }

    public static String resultToOutput(ArrayList<int[]> diceResult) {
        String display = "";
        int i = 1;
        for (int[] result : diceResult) {
            display += String.valueOf(i) + ": " + resultToOutput(result);
            i++;
        }
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

    public static void insertIntoHistoryTable(ContentResolver resolver, int dice, int hits, String output, boolean commonRoll, TestType type, TestModifier modifier, RollStatus rollStatus) {
        ContentValues values = new ContentValues(8);
        values.put(DbContract.HistoryTable.DICE, dice);
        values.put(DbContract.HistoryTable.HITS, hits);
        values.put(DbContract.HistoryTable.OUTPUT, output);
        values.put(DbContract.HistoryTable.COMMON_ROLL, commonRoll);
        values.put(DbContract.HistoryTable.TEST_TYPE, type.toInt());
        values.put(DbContract.HistoryTable.MODIFIER, modifier.toInt());
        values.put(DbContract.HistoryTable.STATUS, rollStatus.toInt());
        values.put(DbContract.HistoryTable.DATE, System.currentTimeMillis());

        resolver.insert(DbContract.HistoryTable.CONTENT_URI, values);
    }
}
