package de.goforittechnologies.go_for_it.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataSourceStepData {

    private static final String TAG = "DataSourceMapData";
    private SQLiteDatabase database;
    private DbHelperStepData dbHelperStepData;

    private String[] columns = {
            DbHelperStepData.COLUMN_ID,
            DbHelperStepData.COLUMN_STEPS,
            DbHelperStepData.COLUMN_TIMESTAMP,
    };


    public DataSourceStepData(Context context, String stepDataTableName) {

        Log.d(TAG, "DataSourceMapData erzeugt DbHelperMapData");
        dbHelperStepData = new DbHelperStepData(context, stepDataTableName);

    }

    public void open() {
        Log.d(TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelperStepData.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelperStepData.close();
        Log.d(TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public StepData createStepData(double steps, String time) {
        ContentValues values = new ContentValues();
        values.put(DbHelperStepData.COLUMN_STEPS, steps);
        values.put(DbHelperStepData.COLUMN_TIMESTAMP, time);


        long insertId = database.insert(dbHelperStepData.stepDataTableName, null, values);

        Cursor cursor = database.query(dbHelperStepData.stepDataTableName,
                columns, DbHelperMapData.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        StepData stepData = cursorToStepData(cursor);
        cursor.close();

        return stepData;
    }

    private StepData cursorToStepData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelperStepData.COLUMN_ID);
        int idSteps = cursor.getColumnIndex(DbHelperStepData.COLUMN_STEPS);
        int idTimestamp = cursor.getColumnIndex(DbHelperStepData.COLUMN_TIMESTAMP);

        double steps = cursor.getDouble(idSteps);
        String timedayhour= cursor.getString(idTimestamp);


        //Dont know if necessary for database usage
        int id = (int)cursor.getLong(idIndex);



        TimeStamp time = new TimeStamp(dbHelperStepData.stepDataTableName,timedayhour);
        StepData stepData= new StepData(steps, time);
        return stepData;
    }

    public List<double[]> getAllStepData() {

        List<StepData> entireStepDataList = new ArrayList<>();
        List<double[]> stepDataList = new ArrayList<>();
        StepData stepData;


        Cursor cursor = database.query(dbHelperStepData.stepDataTableName,
                columns, null, null, null, null, null);

        cursor.moveToFirst();


        while(!cursor.isAfterLast()) {
            stepData = cursorToStepData(cursor);
            entireStepDataList.add(stepData);
            Log.d(TAG, "ID: " + stepData.getId() + ", Inhalt: " + stepData.toString());
            cursor.moveToNext();
        }

        cursor.close();


        double[] day = new double[24];
        int i = 0;
        int j = 0;
        int currentDay = 0;

        while (i < entireStepDataList.size()){
            currentDay = entireStepDataList.get(i).getTime().getDay();
            j = 0;
            while (entireStepDataList.get(i).getTime().getDay() == currentDay)
            {
                day[j] = entireStepDataList.get(i).getSteps();
                j++;
                i++;
            }
            stepDataList.add(day);
            day = new double[24];
        }
        return stepDataList;
    }

}