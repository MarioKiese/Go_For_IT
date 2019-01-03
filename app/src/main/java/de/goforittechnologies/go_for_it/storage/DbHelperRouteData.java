package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see SQLiteOpenHelper
 *
 *
 */

public class DbHelperRouteData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperRouteData";

    // Database configuration
    public static final String DB_NAME = "GoForIT_routes.db";
    public static final int DB_VERSION = 1;

    // Columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ROUTE = "route";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_KILOMETERS = "kilometers";

    public DbHelperRouteData(@Nullable Context context) {
        super(context,DB_NAME,null,DB_VERSION);

        Log.d(TAG, "DbHelperRouteData hat die Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createTable(String tableName) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ROUTE + " TEXT NOT NULL, " +
                COLUMN_STEPS + " INTEGER NOT NULL, " +
                COLUMN_TIME + " TEXT NOT NULL, " +
                COLUMN_CALORIES + " REAL NOT NULL, " +
                COLUMN_KILOMETERS + " REAL NOT NULL);";

        try{

            sqLiteDatabase.execSQL(SQL_CREATE);
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");

        } catch (Exception ex) {

            Log.e(TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());

        }

    }

}
