package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelperStepData extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperStepData";
    public static final String DB_NAME = "GoForIT.db";
    public static final int DB_VERSION = 1;

    public String stepDataTable = "StepDataTableDefault";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Altitude = "altitude";
    public static final String COLUMN_Longitude = "longitude";
    public static final String COLUMN_Latitude = "latitude";
    public static final String COLUMN_Height = "heigth";

    public DbHelperStepData(Context context, String stepDataTabel) {
        super(context,DB_NAME,null,DB_VERSION);
        this.stepDataTable = stepDataTabel;
        Log.d(TAG, "DbHelperMapData hat die Datenbank " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
