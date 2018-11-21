package de.goforittechnologies.go_for_it.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource {

    private static final String TAG = "DataSource";

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    public DataSource(Context context) {

        Log.d(TAG, "DataSource erzeugt DbHelper");
        dbHelper = new DbHelper(context);

    }


}
