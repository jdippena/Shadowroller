package com.imber.shadowroller.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Provider extends ContentProvider {
    private static final String TAG = "Provider";

    private DbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int HISTORY = 0;
    private static final int COMMON_ROLLS = 1;
    private static final int PROBABILITY_NORMAL = 2;
    private static final int PROBABILITY_RULE_OF_SIX = 3;
    private static final int PROBABILITY_PUSH_THE_LIMIT = 4;
    private static final int PROBABILITY_NORMAL_CUMULATIVE = 5;
    private static final int PROBABILITY_RULE_OF_SIX_CUMULATIVE = 6;
    private static final int PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE = 7;
    private static final int PROBABILITY_NORMAL_WITH_DICE = 8;
    private static final int PROBABILITY_RULE_OF_SIX_WITH_DICE = 9;
    private static final int PROBABILITY_PUSH_THE_LIMIT_WITH_DICE = 10;
    private static final int PROBABILITY_NORMAL_CUMULATIVE_WITH_DICE = 11;
    private static final int PROBABILITY_RULE_OF_SIX_CUMULATIVE_WITH_DICE = 12;
    private static final int PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE_WITH_DICE = 13;

    static {
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_HISTORY, HISTORY);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_COMMON_ROLLS, COMMON_ROLLS);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_NORMAL, PROBABILITY_NORMAL);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_RULE_OF_SIX, PROBABILITY_RULE_OF_SIX);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_PUSH_THE_LIMIT, PROBABILITY_PUSH_THE_LIMIT);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_NORMAL_CUMULATIVE, PROBABILITY_NORMAL_CUMULATIVE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_RULE_OF_SIX_CUMULATIVE, PROBABILITY_RULE_OF_SIX_CUMULATIVE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_PUSH_THE_LIMIT_CUMULATIVE, PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_NORMAL + "/#", PROBABILITY_NORMAL_WITH_DICE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_RULE_OF_SIX + "/#", PROBABILITY_RULE_OF_SIX_WITH_DICE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_PUSH_THE_LIMIT + "/#", PROBABILITY_PUSH_THE_LIMIT_WITH_DICE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_NORMAL_CUMULATIVE + "/#", PROBABILITY_NORMAL_CUMULATIVE_WITH_DICE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_RULE_OF_SIX_CUMULATIVE + "/#", PROBABILITY_RULE_OF_SIX_CUMULATIVE_WITH_DICE);
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY, DbContract.PATH_PUSH_THE_LIMIT_CUMULATIVE + "/#", PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE_WITH_DICE);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                return DbContract.HistoryTable.CONTENT_TYPE;
            case COMMON_ROLLS:
                return DbContract.CommonRollsTable.CONTENT_TYPE;
            case PROBABILITY_NORMAL:
                return DbContract.NormalProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_RULE_OF_SIX:
                return DbContract.RuleOfSixProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_PUSH_THE_LIMIT:
                return DbContract.PushTheLimitProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_NORMAL_CUMULATIVE:
                return DbContract.NormalCumulativeProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_RULE_OF_SIX_CUMULATIVE:
                return DbContract.RuleOfSixCumulativeProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE:
                return DbContract.PushTheLimitCumulativeProbabilityTable.CONTENT_TYPE;
            case PROBABILITY_NORMAL_WITH_DICE:
                return DbContract.NormalProbabilityTable.CONTENT_ITEM_TYPE;
            case PROBABILITY_RULE_OF_SIX_WITH_DICE:
                return DbContract.RuleOfSixProbabilityTable.CONTENT_ITEM_TYPE;
            case PROBABILITY_PUSH_THE_LIMIT_WITH_DICE:
                return DbContract.PushTheLimitProbabilityTable.CONTENT_ITEM_TYPE;
            case PROBABILITY_NORMAL_CUMULATIVE_WITH_DICE:
                return DbContract.NormalCumulativeProbabilityTable.CONTENT_ITEM_TYPE;
            case PROBABILITY_RULE_OF_SIX_CUMULATIVE_WITH_DICE:
                return DbContract.RuleOfSixCumulativeProbabilityTable.CONTENT_ITEM_TYPE;
            case PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE_WITH_DICE:
                return DbContract.PushTheLimitCumulativeProbabilityTable.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                cursor = db.query(DbContract.HistoryTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case COMMON_ROLLS:
                cursor = db.query(DbContract.CommonRollsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_NORMAL_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.NormalProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_RULE_OF_SIX_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.RuleOfSixProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_PUSH_THE_LIMIT_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.PushTheLimitProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_NORMAL_CUMULATIVE_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.NormalCumulativeProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_RULE_OF_SIX_CUMULATIVE_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.RuleOfSixCumulativeProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE_WITH_DICE:
                projection = new String[] {DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri))};
                cursor = db.query(DbContract.PushTheLimitCumulativeProbabilityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri retUri = null;
        long id;
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                id = db.insert(DbContract.HistoryTable.TABLE_NAME, null, values);
                retUri = DbContract.HistoryTable.buildUriFromId(id);
                break;
            case COMMON_ROLLS:
                id = db.insert(DbContract.CommonRollsTable.TABLE_NAME, null, values);
                retUri = DbContract.CommonRollsTable.buildUriFromId(id);
                break;
            case PROBABILITY_NORMAL:
                id = db.insert(DbContract.NormalProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.NormalProbabilityTable.buildUriFromId(id);
                break;
            case PROBABILITY_RULE_OF_SIX:
                id = db.insert(DbContract.RuleOfSixProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.RuleOfSixProbabilityTable.buildUriFromId(id);
                break;
            case PROBABILITY_PUSH_THE_LIMIT:
                id = db.insert(DbContract.PushTheLimitProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.PushTheLimitProbabilityTable.buildUriFromId(id);
                break;
            case PROBABILITY_NORMAL_CUMULATIVE:
                id = db.insert(DbContract.NormalCumulativeProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.NormalCumulativeProbabilityTable.buildUriFromId(id);
                break;
            case PROBABILITY_RULE_OF_SIX_CUMULATIVE:
                id = db.insert(DbContract.RuleOfSixCumulativeProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.RuleOfSixCumulativeProbabilityTable.buildUriFromId(id);
                break;
            case PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE:
                id = db.insert(DbContract.PushTheLimitCumulativeProbabilityTable.TABLE_NAME, null, values);
                retUri = DbContract.PushTheLimitCumulativeProbabilityTable.buildUriFromId(id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int numDeleted = 0;
        if (selection == null) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                numDeleted = db.delete(DbContract.HistoryTable.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMON_ROLLS:
                numDeleted = db.delete(DbContract.CommonRollsTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }
        return numDeleted;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                return bulkInsertHelper(uri, values, DbContract.HistoryTable.TABLE_NAME);
            case COMMON_ROLLS:
                return bulkInsertHelper(uri, values, DbContract.CommonRollsTable.TABLE_NAME);
            case PROBABILITY_NORMAL:
                return bulkInsertHelper(uri, values, DbContract.NormalProbabilityTable.TABLE_NAME);
            case PROBABILITY_RULE_OF_SIX:
                return bulkInsertHelper(uri, values, DbContract.RuleOfSixProbabilityTable.TABLE_NAME);
            case PROBABILITY_PUSH_THE_LIMIT:
                return bulkInsertHelper(uri, values, DbContract.PushTheLimitProbabilityTable.TABLE_NAME);
            case PROBABILITY_NORMAL_CUMULATIVE:
                return bulkInsertHelper(uri, values, DbContract.NormalCumulativeProbabilityTable.TABLE_NAME);
            case PROBABILITY_RULE_OF_SIX_CUMULATIVE:
                return bulkInsertHelper(uri, values, DbContract.RuleOfSixCumulativeProbabilityTable.TABLE_NAME);
            case PROBABILITY_PUSH_THE_LIMIT_CUMULATIVE:
                return bulkInsertHelper(uri, values, DbContract.PushTheLimitCumulativeProbabilityTable.TABLE_NAME);
        }
        return super.bulkInsert(uri, values);
    }

    private int bulkInsertHelper(Uri uri, ContentValues[] values, String tableName) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsInserted = 0;
        long id;
        db.beginTransaction();
        for (ContentValues vals : values) {
            id = db.insert(tableName, null, vals);
            if (id != -1) {
                rowsInserted++;
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }
}
