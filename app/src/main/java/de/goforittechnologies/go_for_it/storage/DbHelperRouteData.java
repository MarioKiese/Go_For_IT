package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelperRouteData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperRouteData";
    public static final String DB_NAME = "GoForIT_routes.db";
    public static final int DB_VERSION = 1;

    public String routeDataTable = "TableDefaultName";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ROUTE = "route";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_KILOMETERS = "kilometers";

    public final String SQL_CREATE =
            "CREATE TABLE " + routeDataTable +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ROUTE + " TEXT NOT NULL, " +
                    COLUMN_TIME + " TEXT NOT NULL, " +
                    COLUMN_CALORIES + " REAL NOT NULL, " +
                    COLUMN_KILOMETERS + " REAL NOT NULL);";


    public DbHelperRouteData(Context context, String tableName) {
        super(context,DB_NAME,null,DB_VERSION);

        routeDataTable = tableName;

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
