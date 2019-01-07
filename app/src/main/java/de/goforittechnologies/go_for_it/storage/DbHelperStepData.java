package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author  Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 *
 * This class is used to store and retrieve data from SQLite-Database.
 *
 * @see SQLiteOpenHelper
 * @see SQLiteDatabase
 */

class DbHelperStepData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperStepData";
    private static final String DB_NAME = "GoForIT.db";
    private static final int DB_VERSION = 1;

    String stepDataTableName;

    static final String COLUMN_ID = "_id";
    static final String COLUMN_STEPS = "steps";
    static final String COLUMN_TIMESTAMP = "timestamp";
    private int mode;

    private String SQL_CREATE;
    /**
     * constructor to create database and "create table" query (+ execution).
     * @param context context of usage
     */
    DbHelperStepData(Context context, String stepDataTable, int mode) {
        super(context,DB_NAME,null,DB_VERSION);
        this.mode = mode;
        this.stepDataTableName = stepDataTable;
        Log.d(TAG, "DbHelperMapData hat die Datenbank "
                + getDatabaseName() + " erzeugt.");

            SQL_CREATE = "CREATE TABLE " + stepDataTableName +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STEPS + " REAL NOT NULL," +
                    COLUMN_TIMESTAMP + " STRING NOT NULL)";
        createTable();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
    /**
     * method to execute sql command
     * @param
     *
     * @see SQLiteDatabase
     */
    private void createTable(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        if (mode == 1){
            try {
                Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: "
                        + SQL_CREATE + " angelegt.");
                sqLiteDatabase.execSQL(SQL_CREATE);
            }
            catch (Exception ex) {
                Log.e(TAG, "Fehler beim Anlegen der Tabelle: " +
                        ex.getMessage());
                ex.printStackTrace();
            }
        }
        else{
            Log.d(TAG,  SQL_CREATE + " referenced.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}