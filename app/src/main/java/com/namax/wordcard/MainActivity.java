package com.namax.wordcard;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTEXT_MENU_DELETE_ID = 1;


    public final static String WIDGET_PREF = "widget_pref";
    final static String LOG_TAG = "wordcard_logs";
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    EditText editTextNativeLng, editTextTargetLng;
    Button addBtn;
    ListView wordList;
    DataBase database;
    SimpleCursorAdapter cursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null){
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            resultValue = new Intent();
            WordCardWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetID);
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_OK, resultValue);
        }


        editTextNativeLng = (EditText) findViewById(R.id.editTextNativeLng);
        editTextTargetLng = (EditText) findViewById(R.id.editTextTargetLng);

        addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(this);

        database = new DataBase(this);
        database.open();

        //сопоставляем данные из базы данных с соответстующими позициями в item_pair
        String[] from = new String[] {DataBase.KEY_NATIVE_WORD, DataBase.KEY_TARGET_WORD};
        int[] to = new int[] {R.id.tvNativeWord, R.id.tvTargetWord};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_pair, null, from, to, 0);
        wordList = (ListView) findViewById(R.id.wordList);
        wordList.setAdapter(cursorAdapter);


        registerForContextMenu(wordList);

        getSupportLoaderManager().initLoader(0, null, this); //исключение

    }

    @Override
    public void onClick(View view) {

        String nativeWord = editTextNativeLng.getText().toString();
        nativeWord = nativeWord.trim();
        String targetWord = editTextTargetLng.getText().toString();
        targetWord = targetWord.trim();

        switch (view.getId())
        {
            case R.id.btnAdd:
                if (nativeWord.equals("") || targetWord.equals("")) return;
                else {
                    database.addPair(nativeWord, targetWord);
                    getSupportLoaderManager().getLoader(0).forceLoad();

//                    addPairToDatabase(nativeWord, targetWord);
//                    addPairToList(nativeWord, targetWord);
                    break;
                }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CONTEXT_MENU_DELETE_ID){
            //получаем id пункта, он будет равен id пункта в базе данных
            AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            database.deletePair(contextMenuInfo.id);

            //получаем новый курсор, который приведет список в соответствие базе данных

            getSupportLoaderManager().getLoader(0).forceLoad();

            //TODO проверить что будет если удалить
            return true; // хз хачем
        }

        return super.onContextItemSelected(item);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, database);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class MyCursorLoader extends CursorLoader{

        DataBase dataBase;

        public MyCursorLoader(Context context, DataBase dataBase) {
            super(context);
            this.dataBase = dataBase;
        }

        @Override
        public Cursor loadInBackground() {
            return dataBase.getAllData();
        }
    }


}
