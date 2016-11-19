package com.namax.wordcard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MainActivity MainActivity}
 */
public class WordCardWidget extends AppWidgetProvider {

    private static final String ACTION_FLIP = "com.namax.wordcard.flip_card";
    private static final String ACTION_NEXT_CARD = "com.namax.wordcard.next_card";
    public static final String NATIVE_WORD = "native_word";
    public static final String TARGET_WORD = "target_word";
    static String[] pair;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //здесь определяем какое слово будет в карточке и какой цвет. Эти данные формируем в ресивере
        SharedPreferences sp = context.getSharedPreferences(MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        String NativeWord = sp.getString(NATIVE_WORD + appWidgetId, "WordCards");
        String TargetWord = sp.getString(TARGET_WORD + appWidgetId, "КартоСлов");
        boolean isTargetWord = sp.getBoolean(MainActivity.WIDGET_PREF + appWidgetId, true);

        String word = "";
        int color;

        if (isTargetWord) { //эти настройки есть в color но если ссылаться на них они приходят в виде цифр
            word = TargetWord;
            color = Color.parseColor("#ce546e7a");
        }
        else{
            word = NativeWord;
            color = Color.parseColor("#ce7a5465");
        }

        sp.edit().putBoolean(MainActivity.WIDGET_PREF + appWidgetId, isTargetWord).commit(); // сохраняем текущее состояние карточки, чтобы проверить его в ресивере

        //находим виджет, устанавливаем ему цвет и текст
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.word_card_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, word);
        remoteViews.setInt(R.id.wordCardWidget, "setBackgroundColor", color);

        //активити с настройками
        Intent settingIntent = new Intent(context, MainActivity.class);
        settingIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        settingIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, settingIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.settingBtn, pendingIntent);

        //перелистываем карточку
        Intent flipCard = new Intent(context, WordCardWidget.class);
        flipCard.setAction(ACTION_FLIP);
        flipCard.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, flipCard, 0);
        remoteViews.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        //следующая случайная карточка TODO сделать обновление карточки кадые 30 минут
        Intent nextCard = new Intent(context, WordCardWidget.class);
        nextCard.setAction(ACTION_NEXT_CARD);
        nextCard.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, nextCard, 0);
        remoteViews.setOnClickPendingIntent(R.id.nextCardBtn, pendingIntent);

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
        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();

        for (int appWidgetId : appWidgetIds) { //удаляем sp для удаленного виджета
            editor.remove(NATIVE_WORD + appWidgetId);
            editor.remove(TARGET_WORD + appWidgetId);
            editor.remove(MainActivity.WIDGET_PREF + appWidgetId);
        }
        editor.commit();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
//        Intent intent = new Intent(context, WordCardWidget.class);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Bundle extras = intent.getExtras();



        if (extras != null){
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        }
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            //если с ID все ок - получаем объект sp
            SharedPreferences sp = context.getSharedPreferences(
                    MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);

            if (intent.getAction().equalsIgnoreCase(ACTION_NEXT_CARD)){ //если нажата кнопка следующей карточки

                DataBase dataBase = new DataBase(context);
                dataBase.open();

                int elementCount = dataBase.getAllData().getCount();

                String currentNativeWord = sp.getString(TARGET_WORD + appWidgetId, "WordCards");

                try {
                    if (elementCount <= 1) { // если элемент 1 = делаем запрос и выходим
                        pair = dataBase.getRandomPair();
                    }
                    else do { // чтобы не повторялись элементы
                        pair = dataBase.getRandomPair();
                    }
                    while (pair[1].equals(currentNativeWord));

                } catch (Exception e) {
                    pair = new String[] {"КартоСлов", "WordCards"};
                    Log.e(MainActivity.LOG_TAG, Log.getStackTraceString(e));
                }
                dataBase.close();

                String nativeWord = pair[0];
                String targetWord = pair[1];

                sp.edit().putString(NATIVE_WORD + appWidgetId, nativeWord).putString(TARGET_WORD + appWidgetId, targetWord).commit();

            }
            if (intent.getAction().equalsIgnoreCase(ACTION_FLIP)){ //если нажата сама карточка

                boolean isTargetWord = sp.getBoolean(MainActivity.WIDGET_PREF + appWidgetId, true);

                if (isTargetWord){
                    sp.edit().putBoolean(MainActivity.WIDGET_PREF + appWidgetId, false).commit();
                }
                else sp.edit().putBoolean(MainActivity.WIDGET_PREF + appWidgetId, true).commit();
            }

            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
        }
    }
}

