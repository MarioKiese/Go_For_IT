package de.goforittechnologies.go_for_it.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.StepCounterListener;
import de.goforittechnologies.go_for_it.logic.services.StepCounterService;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Widgets
    TextView tvSensorValue;
    private Toolbar tbMain;
    private PieChart pieChartSteps;
    public static int count = 0;
    private Boolean firstTime = null;
    private Calendar calendar;
    private double stepsForCurrentDay;
    //Service
    private BroadcastReceiver mStepsBroadcastReceiver;

    //Sensor
    SensorManager sensorManager;
    Sensor sensor;
    private static int counter = 0;

    // Firebase
    private FirebaseAuth mAuth;

    StepCounterListener stepCounterListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Start");
        calendar = Calendar.getInstance(TimeZone.getDefault());

        mAuth = FirebaseAuth.getInstance();

        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);

        Intent stepIntent = new Intent(MainActivity.this, StepCounterService.class);
        startService(stepIntent);

        //
        String dbName = "StepDataTABLE_"+ (calendar.get(Calendar.MONTH)+1);
        DataSourceStepData dataSourceStepData =
                new DataSourceStepData(this,dbName, 0);

        dataSourceStepData.open();
        List<double[]> stepList = dataSourceStepData.getAllStepData();
        dataSourceStepData.close();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        stepsForCurrentDay = stepList.get(day)[hour];

        getSupportActionBar().setTitle("Go For IT");
        pieChartSteps = findViewById(R.id.pieChart);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(70.0f));
        entries.add(new PieEntry(30.0f));
        PieDataSet set = new PieDataSet(entries, "Label");
        set.setColors(new int[] { Color.WHITE, Color.BLACK });
        PieData data = new PieData(set);

        pieChartSteps.setData(data);
        pieChartSteps.setCenterTextColor(R.color.colorAccent);
        pieChartSteps.setDrawHoleEnabled(true);
        pieChartSteps.invalidate(); // refresh

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Set steps broadcast receiver
        mStepsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: Step receiver got data");
                stepsForCurrentDay += intent.getDoubleExtra("Steps", 0);
                pieChartSteps.setCenterText(String.valueOf(stepsForCurrentDay) + " Steps");
                pieChartSteps.invalidate();
            }
        };

        //set broadcast manager
        LocalBroadcastManager.getInstance(MainActivity.this)
                .registerReceiver(mStepsBroadcastReceiver,new IntentFilter("StepsUpdate"));

        if (isFirstTime() == true){
            //Create empty database for one yeah on first Start
            for (int m = 11; m <=12; m++ ){
                 dataSourceStepData = new DataSourceStepData(this,
                        "StepDataTABLE_"+ m,1);

                dataSourceStepData.open();
                for (int i = 1; i <=31; i++){
                    for (int j = 0; j <24; j++){
                        dataSourceStepData.createStepData(0,i+":"+j);
                        //Log.d(TAG, "onCreate: StepDataEmpty:" + i + ":" +j);
                    }
                }
                dataSourceStepData.close();
            }
        }
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
            case R.id.action_dashboard_btn:
                Intent dashBoardIntent = new Intent(MainActivity.this,DashboardActivity.class);
                startActivity(dashBoardIntent);

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



    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }


}
