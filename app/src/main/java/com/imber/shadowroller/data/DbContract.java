package com.imber.shadowroller.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

public final class DbContract {
    public static final String CONTENT_AUTHORITY = "com.imber.shadowroller.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HISTORY = "history";
    public static final String PATH_COMMON_ROLLS = "common_rolls";

    public static final String PATH_NORMAL = "normal";
    public static final String PATH_RULE_OF_SIX = "rule_of_six";
    public static final String PATH_PUSH_THE_LIMIT = "push_the_limit";
    public static final String PATH_NORMAL_CUMULATIVE = "normal_cumulative";
    public static final String PATH_RULE_OF_SIX_CUMULATIVE = "rule_of_six_cumulative";
    public static final String PATH_PUSH_THE_LIMIT_CUMULATIVE = "push_the_limit_cumulative";

    public static int getDiceNumberFromUri(Uri uri) {
        return Integer.parseInt(uri.getLastPathSegment());
    }

    public static String getColumnNameFromDiceNumber(int dice) {
        return "dice" + String.valueOf(dice);
    }

    public static final class HistoryTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);
        public static final String TABLE_NAME = "history";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;

        public static final String _ID = "_id";
        public static final String COMMON_ROLL = "common_roll";
        public static final String TEST_TYPE = "test_type";
        public static final String DICE = "dice";
        public static final String MODIFIER = "modifier";
        public static final String HITS = "hits";
        public static final String STATUS = "status";
        public static final String OUTPUT = "output";
        public static final String DATE = "date";

        public static Uri buildUriFromId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CommonRollsTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(BASE_CONTENT_URI, PATH_COMMON_ROLLS);
        public static final String TABLE_NAME = "common_rolls";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;

        public static final String FIREBASE_USERS = "users";
        public static final String FIREBASE_ID = "firebase_id";
        public static final String NAME ="name";
        public static final String DICE = "dice";
        public static final String HITS = "hits";
        public static final String ROLL_STATUS = "roll_status";
    }

    public static abstract class AbstractProbabilityTable {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI;
        public static String TABLE_NAME;
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;

        public static final String _ID = "_id";

        public static Uri buildUriFromId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class NormalProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_NORMAL);
        public static final String TABLE_NAME = "normal_probability";
    }

    public static final class RuleOfSixProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_RULE_OF_SIX);
        public static final String TABLE_NAME = "rule_of_six_probability";
    }

    public static final class PushTheLimitProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_PUSH_THE_LIMIT);
        public static final String TABLE_NAME = "push_the_limit_probability";
    }

    public static final class NormalCumulativeProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_NORMAL_CUMULATIVE);
        public static final String TABLE_NAME = "normal_probability_cumulative";
    }

    public static final class RuleOfSixCumulativeProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_RULE_OF_SIX_CUMULATIVE);
        public static final String TABLE_NAME = "rule_of_six_probability_cumulative";
    }

    public static final class PushTheLimitCumulativeProbabilityTable extends AbstractProbabilityTable {
        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(AbstractProbabilityTable.CONTENT_URI, PATH_PUSH_THE_LIMIT_CUMULATIVE);
        public static final String TABLE_NAME = "push_the_limit_probability_cumulative";
    }
}
