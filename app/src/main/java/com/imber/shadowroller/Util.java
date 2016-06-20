package com.imber.shadowroller;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class Util {
    public static final int SIMPLE_TEST = 0;
    public static final int EXTENDED_TEST = 1;
    public static final int PROBABILITY = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, RULE_OF_SIX, PUSH_THE_LIMIT})
    public @interface TestModifiers{};

    public static final int NONE = 0;
    public static final int RULE_OF_SIX = 1;
    public static final int PUSH_THE_LIMIT = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NORMAL, CRITICAL_SUCCESS, GLITCH, CRITICAL_GLITCH})
    public @interface RollStatus{};

    public static final int NORMAL = 0;
    public static final int CRITICAL_SUCCESS = 1;
    public static final int GLITCH = 2;
    public static final int CRITICAL_GLITCH = 3;

    public interface SimpleTestDiceListener {
        void onRollPerformed(ArrayList<int[]> output, @TestModifiers int modifier);
    }

    public interface ExtendedTestDiceListener {
        void onExtendedRollPerformed(ArrayList<int[]> output);
    }

    public interface ProbabilityDiceListener {
        void onProbabilityQueried(float[] probabilities, @TestModifiers int modifier);
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

    @Util.RollStatus
    public static int getRollStatus(int[] result) {
        return Util.NORMAL;
    }

}
