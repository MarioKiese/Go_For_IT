package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataSourceStepData {

    private static final String TAG = "DataSourceStepData";
    private SQLiteDatabase database;
    private DbHelperStepData dbHelperStepData;

    private String[] columns = {
            DbHelperStepData.COLUMN_ID,
            DbHelperStepData.COLUMN_STEPS,
            DbHelperStepData.COLUMN_TIMESTAMP,
    };


    public DataSourceStepData(Context context,
                              String stepDataTableName, int mode) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperStepData = new DbHelperStepData(context,
                stepDataTableName, mode);

    }

    public void open() {
        Log.d(TAG,
                "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperStepData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " +
                database.getPath());
    }

    public void close() {
        dbHelperStepData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public void createStepData(double steps, String time) {
        ContentValues values = new ContentValues();
        values.put(DbHelperStepData.COLUMN_STEPS, steps);
        values.put(DbHelperStepData.COLUMN_TIMESTAMP, time);

        long insertId = database.insert(dbHelperStepData.stepDataTableName,
                null, values);


        Cursor cursor = database.query(dbHelperStepData.stepDataTableName,
                columns, DbHelperStepData.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        StepData stepData = cursorToStepData(cursor);
        cursor.close();

    }

    public StepData updateStepData(double steps, String time) {
        ContentValues values = new ContentValues();
        values.put(DbHelperStepData.COLUMN_STEPS, steps);
        values.put(DbHelperStepData.COLUMN_TIMESTAMP, time);

        int rowsAffected = database.update(dbHelperStepData.stepDataTableName,
                values, DbHelperStepData.COLUMN_TIMESTAMP + "='" +
                        time + "'",null);

        Log.d(TAG, "createStepData: rowsAffected: "+ rowsAffected);
        Cursor cursor = database.query(dbHelperStepData.stepDataTableName,
                columns, DbHelperStepData.COLUMN_TIMESTAMP + "='"
                        + time + "'", null, null,
                null, null);

        cursor.moveToFirst();
        StepData stepData = cursorToStepData(cursor);
        cursor.close();

        return stepData;
    }

    private StepData cursorToStepData(Cursor cursor) {
        int idIndex =
                cursor.getColumnIndex(DbHelperStepData.COLUMN_ID);
        int idSteps =
                cursor.getColumnIndex(DbHelperStepData.COLUMN_STEPS);
        int idTimestamp =
                cursor.getColumnIndex(DbHelperStepData.COLUMN_TIMESTAMP);
        double steps =
                cursor.getDouble(idSteps);
        String timedayhour =
                cursor.getString(idTimestamp);

        //Dont know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);

        TimeStamp time =
                new TimeStamp(dbHelperStepData.stepDataTableName, timedayhour);
        StepData stepData= new StepData(id, steps, time);
        return stepData;
    }

    public List<double[]> getAllStepData() {

        List<StepData> entireStepDataList = new ArrayList<>();
        List<double[]> stepDataList = new ArrayList<>();
        StepData stepData;

        Log.d(TAG,
                "getAllStepData: "+ columns[0] + columns[1] + columns[2]);
        Cursor cursor = database.query(dbHelperStepData.stepDataTableName,
                columns, null, null,
                null, null, null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            stepData = cursorToStepData(cursor);
            entireStepDataList.add(stepData);
            Log.d(TAG, "ID: " + stepData.getId() +
                    ", Inhalt: " + stepData.toString());
            cursor.moveToNext();
        }
        cursor.close();

        double[] day = new double[24];
        int i = 0;
        int currentDay = 0;

        while (i < entireStepDataList.size()){
            currentDay = entireStepDataList.get(i).getTime().getDay();
            if (entireStepDataList.get(i).getTime().getDay() == currentDay){
                for (int j = 0; j <24; j++){
                    if (i+j < entireStepDataList.size() ){
                        day[j] = entireStepDataList.get(i+j).getSteps();
                        Log.d(TAG, "getAllStepData: i&j:steps "
                                + i + "&"+ j +
                                ":"+ day[j]);
                    }
                }
                i = i + 24;
                stepDataList.add(day);
                day = new double[24];
            }
        }
        return stepDataList;
    }

}