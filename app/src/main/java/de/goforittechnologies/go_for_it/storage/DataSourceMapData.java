package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
/**
 * @author  Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 *
 * This class is used to (re)convert objects representing location points into
 * database columns (storing and loading).
 *
 * @see DbHelperMapData
 *
 */
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

    /**
     * method to create DbHelperMapData object in same context.
     *
     * @param context given  context (of DataSourceMapObject)
     *
     * @see DbHelperMapData
     */
    public DataSourceMapData(Context context) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperMapData = new DbHelperMapData(context);

    }

    /**
     * method to request database reference and open connection
     *
     * @see DbHelperMapData
     */
    public void open() {
        Log.d(TAG,
                "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperMapData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: "
                + database.getPath());
    }
    /**
     * method to close database connection
     *
     * @see DbHelperMapData
     */
    public void close() {
        dbHelperMapData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    /**
     * method to create new table in referenced database (binding operation
     * on created DbHelperMapData-object.
     *
     * @param tableName name of created table
     *
     * @see DbHelperMapData
     */
    public void createTable(String tableName) {

        dbHelperMapData.createTable(tableName);

    }

    /**
     * method to store location-point and creating corresponding object.
     *
     * @param tableName name of table data should be stored in.
     * @param lon longitude value of location-point
     * @param lat latitude value of location point.
     * @param alt altitude value of location point.
     * @param hei height value of location point.
     * @return MapData object representing stored location-point.
     *
     * @see DbHelperMapData
     * @see MapData
     * @see ContentValues
     * @see Cursor
     */

    public MapData createMapsData(String tableName, double lon,
                                  double lat, double alt, double hei) {
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

    /**
     * method to create location-point out of returned cursor from database.
     *
     * @param cursor cursor connected to location-point.
     * @return location-point representing cursor information.
     *
     * @see MapData
     * @see Cursor
     */
    private MapData cursorToMapData(Cursor cursor) {
        int idIndex =
                cursor.getColumnIndex(DbHelperMapData.COLUMN_ID);
        int idLongitude =
                cursor.getColumnIndex(DbHelperMapData.COLUMN_Longitude);
        int idLatitude =
                cursor.getColumnIndex(DbHelperMapData.COLUMN_Latitude);
        int idAltitude =
                cursor.getColumnIndex(DbHelperMapData.COLUMN_Altitude);
        int idHeight =
                cursor.getColumnIndex(DbHelperMapData.COLUMN_Height);

        double longitude = cursor.getDouble(idLongitude);
        double latitude = cursor.getDouble(idLatitude);
        double altitude = cursor.getDouble(idAltitude);
        double height = cursor.getDouble(idHeight);
        //Dont know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);

        return new MapData(longitude,latitude,altitude,height, id);
    }

    /**
     * method to return List-object out of all table values from selected table
     * @param tableName name of table data should load out of.
     *
     * @return List-object out of all table values
     *
     * @see MapData
     * @see Cursor
     *
     */
    public List<MapData> getAllMapData(String tableName) {
        List<MapData> mapDataList = new ArrayList<>();

        Cursor cursor = database.query(tableName,
                columns, null, null,
                null, null, null);

        cursor.moveToFirst();
        MapData mapData;

        while(!cursor.isAfterLast()) {
            mapData = cursorToMapData(cursor);
            mapDataList.add(mapData);
            Log.d(TAG, "ID: " + mapData.getId() + ", " +
                    "Inhalt: " + mapData.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return mapDataList;
    }

    /**
     * method to delete selected table out of database.
     * @param tableName name of table that should be deleted.
     */
    public void deleteTable(String tableName) {

        dbHelperMapData.dropTable(tableName);

    }

}
