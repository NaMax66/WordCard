package com.namax.wordcard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 022 22.10.16.
 */

public class DBHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wordCardDB";
    public static final String TABLE_WORDPAIRS= "wordpairs";

    public static final String KEY_ID = "_id";
    public static final String KEY_NATIVE_WORD = "native";
    public static final String KEY_TARGET_WORD = "target";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_WORDPAIRS +
                "(" + KEY_ID + " integer primary key,"
                + KEY_NATIVE_WORD + " text,"
                + KEY_TARGET_WORD + " text" +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_WORDPAIRS);
        onCreate(sqLiteDatabase);
    }
}
