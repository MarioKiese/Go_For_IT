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
class DbHelperMapData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperMapData";

    // Database configuration
    private static final String DB_NAME = "GoForIT_maps.db";
    private static final int DB_VERSION = 1;

    // Columns
    static final String COLUMN_ID = "_id";
    static final String COLUMN_Altitude = "altitude";
    static final String COLUMN_Longitude = "longitude";
    static final String COLUMN_Latitude = "latitude";
    static final String COLUMN_Height = "heigth";

    /**
     * constructor to create database
     * @param context context of usage
     */
    DbHelperMapData(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        Log.d(TAG, "DbHelperMapData hat die Datenbank " +
                getDatabaseName() +
                " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    /**
     * method to inform developers in Logcat for easier debugging
     * @param sqLiteDatabase SQLite-Database
     * @param i (not used)
     * @param i1 (not used)
     *
     * @see SQLiteDatabase
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        Log.d(TAG, "onUpgrade: Wird aufgerufen!");

    }

    /**
     * method to create "create table" query and execute sql command
     * @param tableName name of table that should be created
     *
     * @see SQLiteDatabase
     */
    void createTable(String tableName) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String SQL_CREATE = "CREATE TABLE " + tableName +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_Altitude + " REAL NOT NULL, " +
                COLUMN_Latitude + " REAL NOT NULL, " +
                COLUMN_Longitude + " REAL NOT NULL, " +
                COLUMN_Height + " REAL NOT NULL);";

        try{

            sqLiteDatabase.execSQL(SQL_CREATE);
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE +
                    " angelegt.");


        } catch (Exception ex) {

            Log.e(TAG, "Fehler beim Anlegen der Tabelle: " +
                    ex.getMessage());

        }

    }

    /**
     * method to create "drop table" query and execute sql command
     * @param tableName
     *
     * @see SQLiteDatabase
     */
    void dropTable(String tableName) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String SQL_DROP = "DROP TABLE IF EXISTS " + tableName + ";";

        try{

            sqLiteDatabase.execSQL(SQL_DROP);
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_DROP +
                    " gelöscht.");


        } catch (Exception ex) {

            Log.e(TAG, "Fehler beim Löschen der Tabelle: " +
                    ex.getMessage());

        }

    }

}