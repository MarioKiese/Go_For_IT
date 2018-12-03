package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelperMapData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperMapData";
    public static final String DB_NAME = "GoForIT_maps.db";
    public static final int DB_VERSION = 1;

    public String mapDataTable = "TableDefaultName";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Altitude = "altitude";
    public static final String COLUMN_Longitude = "longitude";
    public static final String COLUMN_Latitude = "latitude";
    public static final String COLUMN_Height = "heigth";

    public final String SQL_CREATE =
            "CREATE TABLE " + mapDataTable +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_Altitude + " REAL NOT NULL, " +
                    COLUMN_Latitude + " REAL NOT NULL, " +
                    COLUMN_Longitude + " REAL NOT NULL, " +
                    COLUMN_Height + " REAL NOT NULL);";


    public DbHelperMapData(Context context, String tableName) {
        super(context,DB_NAME,null,DB_VERSION);

        mapDataTable = tableName;

        Log.d(TAG, "DbHelperMapData hat die Datenbank " + getDatabaseName() + " erzeugt.");
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
