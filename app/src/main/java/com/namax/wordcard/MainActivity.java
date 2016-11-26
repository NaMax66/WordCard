package com.namax.wordcard;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;



public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, PopupMenu.OnMenuItemClickListener {

    static String nativeWord;
    static String targetWord;

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

    TextView popupMarker;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //TODO добавить предложение добавить виджет


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        popupMarker = (TextView) findViewById(R.id.popupMarker);

        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            resultValue = new Intent();
            WordCardWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetID);
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_OK, resultValue);
        } //нужно при добавлении нового виджета


        editTextNativeLng = (EditText) findViewById(R.id.editTextNativeLng);
        editTextTargetLng = (EditText) findViewById(R.id.editTextTargetLng);

        editTextNativeLng.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                nativeWord = editTextNativeLng.getText().toString();
                getSupportLoaderManager().getLoader(0).forceLoad();
            }
        });

        editTextTargetLng.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                targetWord = editTextTargetLng.getText().toString();
                getSupportLoaderManager().getLoader(0).forceLoad();
            }
        });

        addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(this);

        database = new DataBase(this);
        database.open();

        getContentResolver(); // посмотрел здесь http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
        //иначе вылетает исключение java.lang.IllegalStateException: attempt to re-open an already-closed object: SQLiteDatabase
        //ошибка все равно возникает но только на тестах при перезапуске

        //сопоставляем данные из базы данных с соответстующими позициями в item_pair
        String[] from = new String[]{DataBase.KEY_NATIVE_WORD, DataBase.KEY_TARGET_WORD};
        int[] to = new int[]{R.id.tvNativeWord, R.id.tvTargetWord};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_pair, null, from, to, 0);
        wordList = (ListView) findViewById(R.id.wordList);

        wordList.setAdapter(cursorAdapter);
        wordList.setStackFromBottom(true);

        registerForContextMenu(wordList);

        getSupportLoaderManager().initLoader(0, null, this); //исключение

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    } //создал главное меню

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.makeBackup:
                makeBackup();
                break;
            case R.id.loadBackup:
                loadBackup();
                database.toLowerCase();
                break;
            case R.id.loadFromAnotherDB:
                makeBackup();

                //делаем архив нашей бд
                //остальное в OnMenuItemClick

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPopup(MenuItem item) { // этот метод прописан в menu_main.xml
        PopupMenu popupMenu = new PopupMenu(this, popupMarker);
        MenuInflater inflater = popupMenu.getMenuInflater();
        popupMenu.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.app_selection_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    public void showPopupTime(MenuItem item) {
        PopupMenu popupMenu = new PopupMenu(this, popupMarker);
        MenuInflater inflater = popupMenu.getMenuInflater();
        popupMenu.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.update_time_selector, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        long updTime = 0;

        switch (item.getItemId()) {

            case R.id.myWordBookApp:

                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose MyWordBook database"), 1001);
                //далее в onActivityResult
                break;
            case R.id.oneMinute:
                updTime = 60000;
                break;
            case R.id.fiveMinutes:
                updTime = 300000;
                break;
            case R.id.tenMinutes:
                updTime = 600000;
                break;
            case R.id.twentyMinutes:
                updTime = 1200000;
                break;
            case R.id.thirtyMinutes:
                updTime = 1800000;
                break;
        }

        Log.e(LOG_TAG, "updTime in MainActivity is " + updTime);

        if  (updTime != 0)
        {
            SharedPreferences sp = this.getSharedPreferences(MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);
            sp.edit().putInt(WordCardWidget.UPD_TIME, (int)updTime).commit();
            WordCardWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetID);
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1001) {
                Uri fileURI = data.getData();
                String path = fileURI.getPath();

                if (path.contains("MyNoteBook")){
                    try {
//                        File dbFile = new File(path);
//                        SQLiteDatabase externalDB = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                        database.importFromExternalDB(path, DataBase.MY_WORD_BOOK_DB_NAME);
                        getSupportLoaderManager().getLoader(0).forceLoad();
                        Toast.makeText(this, "Your base updated", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(this, "Вы выбрали неверный путь", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void loadBackup() {

        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()){
                File currentDB = getDatabasePath(DataBase.DATABASE_NAME);
                File backupPath = new File(Environment.getExternalStorageDirectory() + "/WordCards/");
                File backupDB = new File(backupPath, DataBase.DATABASE_NAME);

                if (backupDB.exists()){
                    database.close();
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    database.open();
                    getSupportLoaderManager().getLoader(0).forceLoad();
                }
                Toast.makeText(this, "Your database loaded", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup file is not exists", Toast.LENGTH_SHORT).show();
        }


    }

    private void makeBackup() {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                File currentDB = getDatabasePath(DataBase.DATABASE_NAME);
                Log.e(LOG_TAG, currentDB.toString());
                File backupPath = new File(sd.toString() + "/WordCards/");
                backupPath.mkdirs();
                File backupDB = new File(backupPath, DataBase.DATABASE_NAME);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }

            Toast.makeText(this, "Your database saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "I can't save your database :(((", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {

        String nativeWord = editTextNativeLng.getText().toString();
        nativeWord = nativeWord.trim();
        String targetWord = editTextTargetLng.getText().toString();
        targetWord = targetWord.trim();

        switch (view.getId()) {
            case R.id.btnAdd:
                if (nativeWord.equals("") || targetWord.equals("")) return;
                else {
                    database.addPair(nativeWord, targetWord);
                    getSupportLoaderManager().getLoader(0).forceLoad();
                    SharedPreferences sp = this.getSharedPreferences(MainActivity.WIDGET_PREF, Context.MODE_PRIVATE);
                    sp.edit().putString(WordCardWidget.NATIVE_WORD + widgetID, nativeWord)
                            .putString(WordCardWidget.TARGET_WORD + widgetID, targetWord).commit();
                    editTextTargetLng.setText("");
                    editTextNativeLng.setText("");
                    WordCardWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetID);
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
        if (item.getItemId() == CONTEXT_MENU_DELETE_ID) {
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
        WordCardWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetID);
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



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }



    static class MyCursorLoader extends CursorLoader {

        DataBase dataBase;

        public MyCursorLoader(Context context, DataBase dataBase) {
            super(context);
            this.dataBase = dataBase;
        }

        @Override
        public Cursor loadInBackground() {
            return dataBase.getData(nativeWord, targetWord);
        }
    }


}
