package com.imber.shadowroller.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

import java.util.Random;

public class ShadowrollerWidgetProvider extends AppWidgetProvider {
    private static final String ID_KEY = "app_widget_id_key";

    private static final String BUTTON_CLICKED_KEY = "button_clicked";
    private static final int BUTTON_CLICKED = 1;

    private static final String MINUS_CLICKED_ACTION = "minus_clicked";
    private static final String PLUS_CLICKED_ACTION = "plus_clicked";
    private static final String BIG_RED_BUTTON_CLICKED_ACTION = "big_red_button_clicked";

    private static final String DICE_KEY = "widget_dice";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int dice = context.getResources().getInteger(R.integer.default_dice_number);
        saveDiceNumber(context, dice);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            remoteViews.setOnClickPendingIntent(R.id.widget_minus_button,
                    getButtonPendingIntent(context, MINUS_CLICKED_ACTION, appWidgetId));
            remoteViews.setOnClickPendingIntent(R.id.widget_plus_button,
                    getButtonPendingIntent(context, PLUS_CLICKED_ACTION, appWidgetId));
            remoteViews.setOnClickPendingIntent(R.id.widget_big_red_button,
                    getButtonPendingIntent(context, BIG_RED_BUTTON_CLICKED_ACTION, appWidgetId));
            remoteViews.setTextViewText(R.id.widget_big_red_button, String.valueOf(dice));
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getIntExtra(BUTTON_CLICKED_KEY, 0) == BUTTON_CLICKED) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(ID_KEY, 0);
            int dice = getDiceNumber(context);
            switch (intent.getAction()) {
                case MINUS_CLICKED_ACTION:
                    setDice(context, manager, appWidgetId, dice - 1);
                    break;
                case PLUS_CLICKED_ACTION:
                    setDice(context, manager, appWidgetId, dice + 1);
                    break;
                case BIG_RED_BUTTON_CLICKED_ACTION:
                default:
                    int[] result = Util.doSingleRoll(new Random(), dice);
                    Util.RollStatus rollStatus = Util.getRollStatus(result);
                    setResult(context, manager, appWidgetId, Util.countSuccesses(result), rollStatus);
            }
        }
    }

    private PendingIntent getButtonPendingIntent(Context context, String buttonAction, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(buttonAction);
        intent.putExtra(ID_KEY, appWidgetId);
        intent.putExtra(BUTTON_CLICKED_KEY, BUTTON_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void setDice(Context context, AppWidgetManager manager, int appWidgetId, int newDice) {
        int dice = Util.getBoundedDiceNumber(context.getResources(), newDice, Util.TestType.SIMPLE_TEST);
        saveDiceNumber(context, dice);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        remoteViews.setTextViewText(R.id.widget_big_red_button, String.valueOf(dice));
        manager.updateAppWidget(appWidgetId, remoteViews);
    }

    private void setResult(Context context, AppWidgetManager manager, int appWidgetId, int hits, Util.RollStatus rollStatus) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        remoteViews.setTextViewText(R.id.widget_result_circle, String.valueOf(hits));
        int rollCircleId = Util.getResultCircleIdFromRollStatus(rollStatus);
        remoteViews.setInt(R.id.widget_result_circle, "setBackgroundResource", rollCircleId);
        manager.updateAppWidget(appWidgetId, remoteViews);
    }

    private void saveDiceNumber(Context context, int dice) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(DICE_KEY, dice);
        editor.apply();
    }

    private int getDiceNumber(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                DICE_KEY,
                context.getResources().getInteger(R.integer.default_dice_number));
    }
}
