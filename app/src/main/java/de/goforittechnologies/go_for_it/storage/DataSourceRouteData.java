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
 * This class is used to (re)convert objects representing routes into
 * database columns (storing and loading).
 *
 * @see DbHelperRouteData
 *
 */
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

    /**
     * method to create DbHelperRouteData object in same context.
     *
     * @param context given  context (of DataSourceRouteObject)
     *
     * @see DbHelperRouteData
     */
    public DataSourceRouteData(Context context) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperRouteData = new DbHelperRouteData(context);

    }
    /**
     * method to request database reference and open connection
     *
     * @see DbHelperRouteData
     */
    public void open() {
        Log.d(TAG,
                "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperRouteData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: "
                + database.getPath());
    }

    /**
     * method to close database connection
     *
     * @see DbHelperRouteData
     */
    public void close() {
        dbHelperRouteData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }
    /**
     * method to create new table in referenced database (binding operation
     * on created DbHelperRouteData-object.
     *
     * @see DbHelperRouteData
     */
    public void createTable() {

        dbHelperRouteData.createTable(TABLE_NAME);

    }

    /**
     * method to store routes in database table
     *
     * @param route name of route
     * @param steps steps of hole route
     * @param time time spent on route
     * @param calories calories burned on route
     * @param kilometers distance of route
     *
     * @see ContentValues
     * @see Cursor
     * @see DbHelperRouteData
     * @see RouteData
     */
    public void createRouteData(String route, int steps, String time,
                                double calories, double kilometers) {
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

    /**
     * method to create route out of returned cursor from database.
     *
     * @param cursor cursor connected to route.
     * @return route representing cursor information.
     *
     * @see RouteData
     * @see Cursor
     */
    private RouteData cursorToRouteData(Cursor cursor) {
        int idIndex =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_ID);
        int idRoute =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_ROUTE);
        int idSteps =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_STEPS);
        int idTime =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_TIME);
        int idCalories =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_CALORIES);
        int idKilometers =
                cursor.getColumnIndex(DbHelperRouteData.COLUMN_KILOMETERS);

        String route = cursor.getString(idRoute);
        int steps = cursor.getInt(idSteps);
        String time = cursor.getString(idTime);
        double calories = cursor.getDouble(idCalories);
        double kilometers = cursor.getDouble(idKilometers);
        //Don't know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);

        return new RouteData(route, steps, time, calories, kilometers, id);
    }
    /**
     * method to return List-object out of all table values from selected table
     *
     * @return List-object out of all table values
     *
     * @see RouteData
     * @see Cursor
     *
     */
    public List<RouteData> getAllRouteData() {
        List<RouteData> routeDataList = new ArrayList<>();

        Cursor cursor = database.query(TABLE_NAME,
                columns, null, null,
                null, null, null);

        cursor.moveToFirst();
        RouteData routeData;

        while(!cursor.isAfterLast()) {
            routeData = cursorToRouteData(cursor);
            routeDataList.add(routeData);
            Log.d(TAG, "ID: " + routeData.getId() + "," +
                    " Inhalt: " + routeData.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return routeDataList;
    }
    /**
     * method to delete route out of database.
     * @param routeData name of data-set that should be deleted.
     */
    public void deleteRouteData(RouteData routeData) {
        long id = routeData.getId();

        database.delete(TABLE_NAME,
                DbHelperRouteData.COLUMN_ID + "=" + id,
                null);

        Log.d(TAG, "Eintrag gelöscht! ID: " + id
                + routeData.toString());
    }

}
