package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataSourceMapData {

    private static final String TAG = "DataSourceMapData";

    private SQLiteDatabase database;
    private DbHelperMapData dbHelperMapData;
    private String[] columns = {
            DbHelperMapData.COLUMN_ID,
            DbHelperMapData.COLUMN_Longitude,
            DbHelperMapData.COLUMN_Altitude,
            DbHelperMapData.COLUMN_Latitude,
            DbHelperMapData.COLUMN_Height
    };

    public DataSourceMapData(Context context) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperMapData = new DbHelperMapData(context);

    }

    public void open() {
        Log.d(TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperMapData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelperMapData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public void createTable(String tableName) {

        dbHelperMapData.createTable(tableName);

    }

    public MapData createMapsData(String tableName, double lon,  double lat, double alt, double hei) {
        ContentValues values = new ContentValues();
        values.put(DbHelperMapData.COLUMN_Longitude, lon);
        values.put(DbHelperMapData.COLUMN_Latitude, lat);
        values.put(DbHelperMapData.COLUMN_Altitude, alt);
        values.put(DbHelperMapData.COLUMN_Height, hei);

        long insertId = database.insert(tableName, null, values);

        Cursor cursor = database.query(tableName,
                columns, DbHelperMapData.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        MapData mapData = cursorToMapData(cursor);
        cursor.close();

        return mapData;
    }


    private MapData cursorToMapData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelperMapData.COLUMN_ID);
        int idLongitude = cursor.getColumnIndex(DbHelperMapData.COLUMN_Longitude);
        int idLatitude = cursor.getColumnIndex(DbHelperMapData.COLUMN_Latitude);
        int idAltitude = cursor.getColumnIndex(DbHelperMapData.COLUMN_Altitude);
        int idHeight = cursor.getColumnIndex(DbHelperMapData.COLUMN_Height);

        double longitude = cursor.getDouble(idLongitude);
        double latitude = cursor.getDouble(idLatitude);
        double altitude = cursor.getDouble(idAltitude);
        double height = cursor.getDouble(idHeight);
        //Dont know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);

        return new MapData(longitude,latitude,altitude,height, id);
    }

    public List<MapData> getAllMapData(String tableName) {
        List<MapData> mapDataList = new ArrayList<>();

        Cursor cursor = database.query(tableName,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        MapData mapData;

        while(!cursor.isAfterLast()) {
            mapData = cursorToMapData(cursor);
            mapDataList.add(mapData);
            Log.d(TAG, "ID: " + mapData.getId() + ", Inhalt: " + mapData.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return mapDataList;
    }

    public void deleteTable(String tableName) {

        dbHelperMapData.dropTable(tableName);

    }

}
