package com.namax.wordcard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MainActivity MainActivity}
 */
public class WordCardWidget extends AppWidgetProvider {

    private static final String ACTION_FLIP = "com.namax.wordcard.flip_card";
    private static final String NATIVE_WORD = "native_word";
    private static final String TARGET_WORD = "target_word";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        DataBase dataBase = new DataBase(context);
        dataBase.open();

        String[] pair = new String[] {"КартоСлов", "WordCards"};

        //TODO механизм получения нового слова засунь в приемник
        try { // не работает TODO дебажить
            pair = dataBase.getRandomPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nativeWord = pair[0];
        String targetWord = pair[1];

        sp.edit().putString(NATIVE_WORD, nativeWord).putString(TARGET_WORD, targetWord).commit();

        //находим виджет
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.word_card_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, targetWord);
        //активити с настройками

        Intent settingIntent = new Intent(context, MainActivity.class);
        settingIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        settingIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, settingIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.settingBtn, pendingIntent);

        //перелистываем карточку TODO взять засунутое бродксатом слово из SharedPref и присвоить текствьюхе
        Intent flipCard = new Intent(context, WordCardWidget.class);
        flipCard.setAction(ACTION_FLIP);
        flipCard.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, flipCard, 0);
        remoteViews.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) { //стандартный
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
//        for (int appWidgetId : appWidgetIds) {
//            WordCardWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
//        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equalsIgnoreCase(ACTION_FLIP)){
            int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null){
                appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                //если с ID все ок - получаем текущую пару из настроек
                SharedPreferences sp = context.getSharedPreferences(
                        MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);
                //TODO получить пару слов и засунуть в новые настройки текущее слово
            }
        }
    }
}

