package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataSourceRouteData {

    private static final String TAG = "DataSourceRouteData";

    private SQLiteDatabase database;
    private DbHelperRouteData dbHelperRouteData;
    private static final String TABLE_NAME = "Routes";
    private String[] columns = {
            DbHelperRouteData.COLUMN_ID,
            DbHelperRouteData.COLUMN_ROUTE,
            DbHelperRouteData.COLUMN_STEPS,
            DbHelperRouteData.COLUMN_TIME,
            DbHelperRouteData.COLUMN_CALORIES,
            DbHelperRouteData.COLUMN_KILOMETERS
    };

    public DataSourceRouteData(Context context) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperRouteData = new DbHelperRouteData(context);

    }

    public void open() {
        Log.d(TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperRouteData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelperRouteData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public void createTable() {

        dbHelperRouteData.createTable(TABLE_NAME);

    }

    public void createRouteData(String route, int steps, String time, double calories, double kilometers) {
        ContentValues values = new ContentValues();
        values.put(DbHelperRouteData.COLUMN_ROUTE, route);
        values.put(DbHelperRouteData.COLUMN_STEPS, steps);
        values.put(DbHelperRouteData.COLUMN_TIME, time);
        values.put(DbHelperRouteData.COLUMN_CALORIES, calories);
        values.put(DbHelperRouteData.COLUMN_KILOMETERS, kilometers);

        long insertId = database.insert(TABLE_NAME, null, values);

        Cursor cursor = database.query(TABLE_NAME,
                columns, DbHelperRouteData.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        cursor.close();

    }


    private RouteData cursorToRouteData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelperRouteData.COLUMN_ID);
        int idRoute = cursor.getColumnIndex(DbHelperRouteData.COLUMN_ROUTE);
        int idSteps = cursor.getColumnIndex(DbHelperRouteData.COLUMN_STEPS);
        int idTime = cursor.getColumnIndex(DbHelperRouteData.COLUMN_TIME);
        int idCalories = cursor.getColumnIndex(DbHelperRouteData.COLUMN_CALORIES);
        int idKilometers = cursor.getColumnIndex(DbHelperRouteData.COLUMN_KILOMETERS);

        String route = cursor.getString(idRoute);
        int steps = cursor.getInt(idSteps);
        String time = cursor.getString(idTime);
        double calories = cursor.getDouble(idCalories);
        double kilometers = cursor.getDouble(idKilometers);
        //Don't know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);

        return new RouteData(route, steps, time, calories, kilometers, id);
    }

    public List<RouteData> getAllRouteData() {
        List<RouteData> routeDataList = new ArrayList<>();

        Cursor cursor = database.query(TABLE_NAME,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        RouteData routeData;

        while(!cursor.isAfterLast()) {
            routeData = cursorToRouteData(cursor);
            routeDataList.add(routeData);
            Log.d(TAG, "ID: " + routeData.getId() + ", Inhalt: " + routeData.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return routeDataList;
    }

    public void deleteRouteData(RouteData routeData) {
        long id = routeData.getId();

        database.delete(TABLE_NAME,
                DbHelperRouteData.COLUMN_ID + "=" + id,
                null);

        Log.d(TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + routeData.toString());
    }

}
