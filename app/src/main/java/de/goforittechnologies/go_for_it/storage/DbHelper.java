package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";
    public static final String DB_NAME = "GoForIT.db";
    public static final int DB_VERSION = 1;

    public static final String MAP_DATA_TABLE = "MapDataTable";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Altitude = "altitude";
    public static final String COLUMN_Longitude = "longitude";
    public static final String COLUMN_Latitude = "latitude";
    public static final String COLUMN_Height = "heigth";

    public static final String SQL_CREATE =
            "CREATE TABLE " + MAP_DATA_TABLE +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_Altitude + " REAL NOT NULL, " +
                    COLUMN_Latitude + " REAL NOT NULL, " +
                    COLUMN_Longitude + " REAL NOT NULL, " +
                    COLUMN_Height + " REAL NOT NULL);";


    public DbHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
        Log.d(TAG, "DbHelper hat die Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            Log.d(TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            sqLiteDatabase.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
