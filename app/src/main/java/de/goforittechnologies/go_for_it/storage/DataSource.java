package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource {

    private static final String TAG = "DataSource";

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private String[] columns = {
            DbHelper.COLUMN_ID,
            DbHelper.COLUMN_Longitude,
            DbHelper.COLUMN_Altitude,
            DbHelper.COLUMN_Latitude,
            DbHelper.COLUMN_Height
    };


    public DataSource(Context context) {

        Log.d(TAG, "DataSource erzeugt DbHelper");
        dbHelper = new DbHelper(context);

    }

    public void open() {
        Log.d(TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public MapData createMapsData(double lon, double alt, double lat, double hei) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_Longitude, lon);
        values.put(DbHelper.COLUMN_Altitude, alt);
        values.put(DbHelper.COLUMN_Latitude, lat);
        values.put(DbHelper.COLUMN_Height, hei);

        long insertId = database.insert(DbHelper.TABLE_STEPS, null, values);

        Cursor cursor = database.query(DbHelper.TABLE_STEPS,
                columns, DbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        MapData mapData = cursorToStepData(cursor);
        cursor.close();

        return mapData;
    }


    private MapData cursorToStepData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelper.COLUMN_ID);
        int idLongitude = cursor.getColumnIndex(DbHelper.COLUMN_Longitude);
        int idLatitude = cursor.getColumnIndex(DbHelper.COLUMN_Latitude);
        int idAltitude = cursor.getColumnIndex(DbHelper.COLUMN_Altitude);
        int idHeight = cursor.getColumnIndex(DbHelper.COLUMN_Height);

        double longitude = cursor.getDouble(idLongitude);
        double latitude = cursor.getDouble(idLatitude);
        double altitude = cursor.getDouble(idAltitude);
        double height = cursor.getDouble(idHeight);
        long id = cursor.getLong(idIndex);

        MapData mapData = new MapData(longitude,latitude,altitude,height);

        return mapData;
    }
}
