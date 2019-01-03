package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DbHelperStepData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperStepData";
    private static final String DB_NAME = "GoForIT.db";
    private static final int DB_VERSION = 1;

    String stepDataTableName = "StepDataTableDefault";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_STEPS = "steps";
    static final String COLUMN_TIMESTAMP = "timestamp";
    private int mode;

    private String SQL_CREATE = "";

    DbHelperStepData(Context context, String stepDataTable, int mode) {
        super(context,DB_NAME,null,DB_VERSION);
        this.mode = mode;
        this.stepDataTableName = stepDataTable;
        Log.d(TAG, "DbHelperMapData hat die Datenbank " + getDatabaseName() + " erzeugt.");

            SQL_CREATE = "CREATE TABLE " + stepDataTableName +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STEPS + " REAL NOT NULL," +
                    COLUMN_TIMESTAMP + " STRING NOT NULL)";
        createTable();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
    private void createTable(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        if (mode == 1){
            try {
                Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
                sqLiteDatabase.execSQL(SQL_CREATE);
            }
            catch (Exception ex) {
                Log.e(TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
            }
        }
        else{
            Log.d(TAG,  SQL_CREATE + " angelegt.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}