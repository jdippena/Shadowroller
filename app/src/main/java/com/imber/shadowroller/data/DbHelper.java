package com.imber.shadowroller.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.imber.shadowroller.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    public static final int VERSION = 1;
    public static final String NAME = "shadowroller.db";
    public static final int NUMBER_OF_DICE = 50;

    public static final String SQL_CREATE_HISTORY = "CREATE TABLE " +
            DbContract.HistoryTable.TABLE_NAME + "(" +
            DbContract.HistoryTable._ID + " INTEGER PRIMARY KEY, " +
            DbContract.HistoryTable.HITS + " INTEGER NOT NULL, " +
            DbContract.HistoryTable.OUTPUT + " TEXT NOT NULL, " +
            DbContract.HistoryTable.MODIFIER + " INTEGER NOT NULL, " +
            DbContract.HistoryTable.STATUS + " INTEGER NOT NULL," +
            DbContract.HistoryTable.TEST_TYPE + " INTEGER NOT NULL );";

    public static final String SQL_CREATE_COMMON_ROLLS = "CREATE TABLE " +
            DbContract.CommonRollsTable.TABLE_NAME + "( " +
            DbContract.CommonRollsTable._ID + " INTEGER PRIMARY KEY, " +
            DbContract.CommonRollsTable.NAME + " TEXT NOT NULL, " +
            DbContract.CommonRollsTable.DICE + " INTEGER NOT NULL, " +
            DbContract.CommonRollsTable.EDGE + " INTEGER NOT NULL );";

    public static final String SQL_CREATE_NORMAL_PROBABILITY = "CREATE TABLE " +
            DbContract.NormalProbabilityTable.TABLE_NAME + "( " +
            DbContract.NormalProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public static final String SQL_CREATE_RULE_OF_SIX_PROBABILITY = "CREATE TABLE " +
            DbContract.RuleOfSixProbabilityTable.TABLE_NAME + "( " +
            DbContract.RuleOfSixProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public static final String SQL_CREATE_PUSH_THE_LIMIT_PROBABILITY = "CREATE TABLE " +
            DbContract.PushTheLimitProbabilityTable.TABLE_NAME + "( " +
            DbContract.PushTheLimitProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public static final String SQL_CREATE_NORMAL_PROBABILITY_CUMULATIVE = "CREATE TABLE " +
            DbContract.NormalCumulativeProbabilityTable.TABLE_NAME + "( " +
            DbContract.NormalCumulativeProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public static final String SQL_CREATE_RULE_OF_SIX_PROBABILITY_CUMULATIVE = "CREATE TABLE " +
            DbContract.RuleOfSixCumulativeProbabilityTable.TABLE_NAME + "( " +
            DbContract.RuleOfSixCumulativeProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public static final String SQL_CREATE_PUSH_THE_LIMIT_PROBABILITY_CUMULATIVE = "CREATE TABLE " +
            DbContract.PushTheLimitCumulativeProbabilityTable.TABLE_NAME + "( " +
            DbContract.PushTheLimitCumulativeProbabilityTable._ID + " INTEGER PRIMARY KEY, " +
            makeSqlColumnNames() + ");";

    public DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HISTORY);
        db.execSQL(SQL_CREATE_COMMON_ROLLS);
        db.execSQL(SQL_CREATE_NORMAL_PROBABILITY);
        db.execSQL(SQL_CREATE_RULE_OF_SIX_PROBABILITY);
        db.execSQL(SQL_CREATE_PUSH_THE_LIMIT_PROBABILITY);
        db.execSQL(SQL_CREATE_NORMAL_PROBABILITY_CUMULATIVE);
        db.execSQL(SQL_CREATE_RULE_OF_SIX_PROBABILITY_CUMULATIVE);
        db.execSQL(SQL_CREATE_PUSH_THE_LIMIT_PROBABILITY_CUMULATIVE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.HistoryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.CommonRollsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.NormalProbabilityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.RuleOfSixProbabilityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.PushTheLimitProbabilityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.NormalCumulativeProbabilityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.RuleOfSixCumulativeProbabilityTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.PushTheLimitCumulativeProbabilityTable.TABLE_NAME);
        onCreate(db);
    }

    private static String makeSqlColumnNames() {
        String cols = "";
        for (int i = 1; i < NUMBER_OF_DICE; i++) {
            cols += DbContract.getColumnNameFromDiceNumber(i) + " REAL NOT NULL, ";
        }
        cols += DbContract.getColumnNameFromDiceNumber(NUMBER_OF_DICE) + " REAL NOT NULL " ;
        return cols;
    }

    private static ContentValues[] probabilityFileToContentValues(AssetManager manager, String filename) {
        InputStream input;
        try {
            input = manager.open(filename);
            byte[] buf = new byte[2*4];
            input.read(buf, 0, 8);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buf, 0, 8);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int n = byteBuffer.getInt(0);
            int m = byteBuffer.getInt(4);
            ArrayList<ContentValues> values = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                ContentValues probabilityValues = new ContentValues(m);
                int rowSize = m*8;
                buf = new byte[rowSize];
                input.read(buf, 0, rowSize);
                byteBuffer = ByteBuffer.wrap(buf, 0, rowSize);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int j = 1; j < m; j++) {
                    probabilityValues.put(DbContract.getColumnNameFromDiceNumber(j), byteBuffer.getDouble(j*8));
                }
                values.add(probabilityValues);
            }
            ContentValues[] returnValues = new ContentValues[values.size()];
            values.toArray(returnValues);
            input.close();
            return returnValues;
        } catch (IOException e) {
            Log.e(TAG, "probabilityFileToContentValues: Error reading file");
            e.printStackTrace();
        }
        return null;
    }

    public static void initializeProbabilityTables(final Context context) {
        DbHelper dbHelper = new DbHelper(context);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), VERSION, VERSION);
        AssetManager assetManager = context.getAssets();
        for (Util.TestModifier modifier : Util.TestModifier.values()) {
            for (boolean cumulative : new boolean[] {true, false}) {
                String filename = Util.getProbabilityFilename(context.getResources(), modifier, cumulative);
                ContentValues[] values = probabilityFileToContentValues(assetManager, filename);
                context.getContentResolver()
                        .bulkInsert(Util.getContentUriFromModifier(modifier, cumulative), values);
            }
        }
    }
}
