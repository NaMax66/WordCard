package com.namax.wordcard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;

/**
 * Created by User on 001 01.11.16.
 */

public class DataBase {
    public static final String MY_WORD_BOOK_DB_NAME = "MyNoteBook";
    public static final String MY_WORD_BOOK_TABLE_NAME = "Mots";

    public static final String MY_WORD_BOOK_NATIVE_WORD = "descMot";
    public static final String MY_WORD_BOOK_TARGET_WORD = "mot";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wordCardDB";
    public static final String TABLE_WORDPAIRS = "wordpairs";

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

    public Cursor getData(String nativeWord, String targetWord){

        if (dbHelper == null)
        {
            DataBase dataBase = new DataBase(context);
            dataBase.open();
        }

        if (nativeWord == null) nativeWord = "";
        if (targetWord == null) targetWord = "";
        if (nativeWord.equals("") && targetWord.equals("")) return sqLiteDatabase.query(TABLE_WORDPAIRS, null, null, null, null, null, null);

        nativeWord = "%" + nativeWord + "%";
        targetWord = "%" + targetWord + "%";


        if (nativeWord.equals("%%")) return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_WORDPAIRS + //здесь ищим заданные слова
                " WHERE " + KEY_TARGET_WORD + " LIKE ?;", new String[]{targetWord});
        if (targetWord.equals("%%")) return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_WORDPAIRS + //здесь ищим заданные слова
                " WHERE " + KEY_NATIVE_WORD + " LIKE ?;", new String[]{nativeWord});

            return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_WORDPAIRS + //здесь ищим заданные слова
                " WHERE " + KEY_NATIVE_WORD + " LIKE ? OR " +
                KEY_TARGET_WORD + " LIKE ?;", new String[]{nativeWord, targetWord});
    }

    public void toLowerCase(){
        //хочу все в нижний регистр!!!
        if (dbHelper == null)
        {
            DataBase dataBase = new DataBase(context);
            dataBase.open();
        }
        Cursor cursor = getData(null, null);

        if (cursor != null){
            if (cursor.moveToFirst()){

                String nativeWord;
                String lowNatWord;

                do {
                    nativeWord = cursor.getString(cursor.getColumnIndex(KEY_NATIVE_WORD));
                    lowNatWord = nativeWord.toLowerCase();
                    sqLiteDatabase.execSQL("UPDATE " + TABLE_WORDPAIRS + " SET " + KEY_NATIVE_WORD + " = '" + lowNatWord
                            + "' WHERE " + KEY_NATIVE_WORD + " = '" + nativeWord + "'");

                }
                while (cursor.moveToNext());
            }
            cursor.close();

        } else Log.d(MainActivity.LOG_TAG, "Cursor is null");

    }

    public String[] getRandomPair(){

        Cursor cursor = getData(null, null);
        int countOfElements =  cursor.getCount();
        int randomRaw = (int)(Math.random() * countOfElements) + 1; // добавляем один, чтобы небыло нуля и последний элемент мог выбраться
        cursor.move(randomRaw);
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

    public void importFromExternalDB(String path, String externalDBName){

        if (dbHelper == null)
        {
            DataBase dataBase = new DataBase(context);
            dataBase.open();
        }

        switch (externalDBName){

            case MY_WORD_BOOK_DB_NAME:

                sqLiteDatabase.execSQL("ATTACH DATABASE " + "'" + path + "'" + " AS tempDb");
                sqLiteDatabase.execSQL("INSERT INTO " + TABLE_WORDPAIRS + "(" +
                        KEY_NATIVE_WORD + ", " + KEY_TARGET_WORD + ") " +
                "SELECT " + MY_WORD_BOOK_NATIVE_WORD + ", " + MY_WORD_BOOK_TARGET_WORD + " FROM " +
                "tempDb" + "." + MY_WORD_BOOK_TABLE_NAME + ";");

                sqLiteDatabase.execSQL("DETACH tempDb");
            break;
        }
    } //не работает на SONY

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
