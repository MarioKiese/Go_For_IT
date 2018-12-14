package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelperMapData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperMapData";

    // Database configuration
    public static final String DB_NAME = "GoForIT_maps.db";
    public static final int DB_VERSION = 1;

    // Columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Altitude = "altitude";
    public static final String COLUMN_Longitude = "longitude";
    public static final String COLUMN_Latitude = "latitude";
    public static final String COLUMN_Height = "heigth";

    public DbHelperMapData(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        Log.d(TAG, "DbHelperMapData hat die Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        Log.d(TAG, "onUpgrade: Wird aufgerufen!");
        
    }

    public void createTable(String tableName) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String SQL_CREATE = "CREATE TABLE " + tableName +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_Altitude + " REAL NOT NULL, " +
                COLUMN_Latitude + " REAL NOT NULL, " +
                COLUMN_Longitude + " REAL NOT NULL, " +
                COLUMN_Height + " REAL NOT NULL);";

        try{

            sqLiteDatabase.execSQL(SQL_CREATE);
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");


        } catch (Exception ex) {

            Log.e(TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());

        }

    }

    public void dropTable(String tableName) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String SQL_DROP = "DROP TABLE IF EXISTS " + tableName + ";";

        try{

            sqLiteDatabase.execSQL(SQL_DROP);
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_DROP + " gelöscht.");


        } catch (Exception ex) {

            Log.e(TAG, "Fehler beim Löschen der Tabelle: " + ex.getMessage());

        }

    }

}
