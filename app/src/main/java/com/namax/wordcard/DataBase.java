package com.namax.wordcard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Random;

/**
 * Created by User on 001 01.11.16.
 */

public class DataBase {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wordCardDB";
    public static final String TABLE_WORDPAIRS= "wordpairs";

    public static final String KEY_ID = "_id";
    public static final String KEY_NATIVE_WORD = "native";
    public static final String KEY_TARGET_WORD = "target";

    private static final String DB_CREATE =
            "create table " + TABLE_WORDPAIRS + "("
                    + KEY_ID + " integer primary key,"
                    + KEY_NATIVE_WORD + " text,"
                    + KEY_TARGET_WORD + " text" +");";

    private final Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public DataBase(Context context) {
        this.context = context;
    }

    public void open() {
        dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public Cursor getAllData(){
        return sqLiteDatabase.query(TABLE_WORDPAIRS, null, null, null, null, null, null);
    }

    public String[] getRandomPair(){
        Cursor cursor = getAllData();
        int randomId = new Random(cursor.getCount()-1).nextInt();
        cursor.move(randomId);
        int nativeWordIndex = cursor.getColumnIndex(KEY_NATIVE_WORD);
        int targetWordIndex = cursor.getColumnIndex(KEY_TARGET_WORD);
        String nativeWord = cursor.getString(nativeWordIndex);
        String targetWord = cursor.getString(targetWordIndex);

        return new String[]{nativeWord, targetWord};
    }

    public void addPair(String nativeWord, String targetWord){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NATIVE_WORD, nativeWord);
        contentValues.put(KEY_TARGET_WORD, targetWord);
        sqLiteDatabase.insert(TABLE_WORDPAIRS, null, contentValues);
    }

    public void deletePair(long id){
        sqLiteDatabase.delete(TABLE_WORDPAIRS, KEY_ID + " = " + id, null);
    }

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists " + TABLE_WORDPAIRS);
            onCreate(sqLiteDatabase);
        }
    }
}
