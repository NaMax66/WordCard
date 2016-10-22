package com.namax.wordcard;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static String LOG_TAG = "wordcard_logs";

    EditText editTextNativeLng, editTextTargetLng;
    Button addFirstBtn, nextFirstBtn;
    LinearLayout firstList;

    DBHelper dbHelper;
    SQLiteDatabase database;
    int nextID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextNativeLng = (EditText) findViewById(R.id.editTextNativeLng);
        editTextTargetLng = (EditText) findViewById(R.id.editTextTargetLng);

        addFirstBtn = (Button) findViewById(R.id.addFirstBtn);
        addFirstBtn.setOnClickListener(this);

        nextFirstBtn = (Button) findViewById(R.id.nextFirstBtn);
        nextFirstBtn.setOnClickListener(this);

        firstList = (LinearLayout) findViewById(R.id.firstList);

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_WORDPAIRS, null, null, null, null, null, null);
        if (cursor.getCount() == 0) nextID = 0;
        else {
            cursor.moveToLast();

            int columnIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int lastIndex = cursor.getInt(columnIndex);
            nextID = lastIndex + 1; // допустим последний индекс 0 (всего 1 элемент), значит следующий id = 1;
        }
        cursor.close();
        Log.d(LOG_TAG, "next ID is " + nextID);

    }

    @Override
    public void onClick(View view) {

        String nativeWord = editTextNativeLng.getText().toString();
        nativeWord = nativeWord.trim();
        String targetWord = editTextTargetLng.getText().toString();
        targetWord = targetWord.trim();

        switch (view.getId())
        {
            case R.id.addFirstBtn:
                if (nativeWord.equals("") || targetWord.equals("")) return;
                else {
                    addPairToDatabase(nativeWord, targetWord);
                    addPairToList(nativeWord, targetWord);
                    break;
                }

            case R.id.nextFirstBtn:
                Intent intent = new Intent(this, AddWidgetActivity.class);
                startActivity(intent);

        }

        dbHelper = new DBHelper(this);

    }

    private void addPairToDatabase(String nativeWord, String targetWord) { //добавляем строки в базу данных, с которой будет работаь виджет

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_NATIVE_WORD, nativeWord);
        contentValues.put(DBHelper.KEY_TARGET_WORD, targetWord);
        database.insert(DBHelper.TABLE_WORDPAIRS, null, contentValues);
        Cursor cursor = database.query(DBHelper.TABLE_WORDPAIRS, null, null, null, null, null, null);
        Log.d(LOG_TAG, "current row count: " + cursor.getCount());
        cursor.close();
    }

    private void addPairToList(String nativeWord, String targetWord) {
        float scaledDensity = this.getResources().getDisplayMetrics().scaledDensity;//масштаб
        LinearLayout.LayoutParams pairParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pairParams.topMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);

        TextView pair = new TextView(this);
        pair.setText(nativeWord + " - " + targetWord);
        pair.setTextSize(getResources().getDimension(R.dimen.hdpi_text_size)/scaledDensity); //так не пойдет, нужна другая константа
        pair.setGravity(Gravity.CENTER_HORIZONTAL);
        pair.setId(nextID++); //устанавливаем id и увеличиваем на 1
        firstList.addView(pair, 0, pairParams);
        Log.d(LOG_TAG, "current TextView ID: " + pair.getId());
        Log.d(LOG_TAG, "next TextView will have ID: " + nextID);
        registerForContextMenu(pair);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { // взял из проекта калькулятор
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, v.getId(), Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        firstList.removeView(firstList.findViewById(item.getItemId()));
        String id = "" + item.getItemId();
        database.delete(DBHelper.TABLE_WORDPAIRS, DBHelper.KEY_ID + "= ?", new String[] {id});
        return super.onContextItemSelected(item);
    }
}
