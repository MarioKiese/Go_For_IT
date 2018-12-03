package de.goforittechnologies.go_for_it.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.StepCounterListener;
import de.goforittechnologies.go_for_it.storage.DataSourceMapData;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Widgets
    TextView tvSensorValue;
    private Toolbar tbMain;
    PieChart pieChart;

    SensorManager sensorManager;
    Sensor sensor;
    private StepCounterHandler stepCounterHandler;

    // Firebase
    private FirebaseAuth mAuth;

    StepCounterListener stepCounterListener;

    /*private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int steps;
                steps = bundle.getInt("steps");

                    tvSensorValue.setText(String.valueOf(steps));

            }
        }
    };*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Start");

        mAuth = FirebaseAuth.getInstance();

        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);

        getSupportActionBar().setTitle("Go For IT");

        //tvSensorValue = findViewById(R.id.tvSensorValue);
        pieChart = findViewById(R.id.pieChart);


        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(70.0f));
        entries.add(new PieEntry(30.0f));

        PieDataSet set = new PieDataSet(entries, "Label");
        set.setColors(new int[] { Color.WHITE, Color.BLACK });
        PieData data = new PieData(set);

        pieChart.setData(data);
        pieChart.setCenterTextColor(R.color.colorAccent);
        pieChart.setDrawHoleEnabled(true);
        pieChart.invalidate(); // refresh


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        stepCounterHandler = new StepCounterHandler();
        stepCounterListener = new StepCounterListener(stepCounterHandler);

        //test for DB usage

        DataSourceMapData dataSourceMapData = new DataSourceMapData(this, "Route1");
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceMapData.open();

        /*dataSourceMapData.createMapsData(40.0,50.0,60.0,61.0);
        dataSourceMapData.createMapsData(40.0,50.0,60.0,61.0);
        dataSourceMapData.createMapsData(40.0,50.0,60.0,61.0);
        dataSourceMapData.createMapsData(40.0,50.0,60.0,61.0);*/

        dataSourceMapData.getAllMapData();


        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");
        dataSourceMapData.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            sendToLogin();


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_logout_btn:

                logOut();

                return true;

            case R.id.action_settings_btn:

                Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(settingsIntent);

                return true;

            case R.id.action_location_btn:

                Intent locationIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(locationIntent);

                return true;

            default:

                return false;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* registerReceiver(receiver, new IntentFilter(
                StepCounterService.NOTIFICATION));*/

        sensorManager.registerListener(stepCounterListener, sensor, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(receiver);

        sensorManager.unregisterListener(stepCounterListener);
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }

    private void logOut() {

        mAuth.signOut();
        sendToLogin();

    }

    private class StepCounterHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            pieChart.setCenterText(String.valueOf(msg.what) + " Steps");
            pieChart.invalidate();

        }
    }

}
