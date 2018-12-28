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
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
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
    private TextView tvSensorValue;
    private TextView tvMaxSteps;
    private TextView tvStepGoalValue;
    private RatingBar rbStepGoal;
    private Toolbar tbMain;
    private PieChart pieChartSteps;
    private Button btnConfirmStepGoal;
    public static int count = 0;
    private Boolean firstTime = null;
    private Calendar calendar;
    private double stepsForCurrentDay;
    private int stepGoal = -1;
    private List<PieEntry> entries = new ArrayList<>();
    private PieDataSet set;
    //Service
    private BroadcastReceiver mStepsBroadcastReceiver;

    //Sensor
    SensorManager sensorManager;
    Sensor sensor;
    private static int counter = 0;

    // Firebase
    private FirebaseAuth mAuth;

    StepCounterListener stepCounterListener;
    SharedPreferences mPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Start");
        calendar = Calendar.getInstance(TimeZone.getDefault());

        mAuth = FirebaseAuth.getInstance();

        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);

        tvMaxSteps = findViewById(R.id.tvMaxSteps);
        tvStepGoalValue = findViewById(R.id.tvStepGoalValue);
        btnConfirmStepGoal = findViewById(R.id.btnConfirmStepGoal);
        rbStepGoal = findViewById(R.id.rbStepGoal);
        Intent stepIntent = new Intent(MainActivity.this, StepCounterService.class);
        startService(stepIntent);

        DataSourceStepData dataSourceStepData;

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

        //
        String dbName = "StepDataTABLE_"+ (calendar.get(Calendar.MONTH)+1);
        dataSourceStepData =
                new DataSourceStepData(this,dbName, 0);

        List<double[]> stepList = null;
        try {
            dataSourceStepData.open();
            stepList = dataSourceStepData.getAllStepData();
            dataSourceStepData.close();

            tvMaxSteps.setText(String.valueOf(getMaxForMonth(stepList)));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            for (int i = 0; i <= hour; i++){
                stepsForCurrentDay += stepList.get(day - 1)[i];
            }
            pieChartSteps.invalidate();
            Log.d(TAG, "onCreate: stepsForcurrentDay: " + stepsForCurrentDay);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set stepgoal (star rating)
        rbStepGoal.setMax(6);
        rbStepGoal.invalidate();
        btnConfirmStepGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float cntStar = rbStepGoal.getRating();

                if (stepGoal == -1) {
                    mPreferences = MainActivity.this.getSharedPreferences("Step_Goal",
                            Context.MODE_PRIVATE);
                    stepGoal = mPreferences.getInt("stepgoal", (int) (cntStar * 2000));

                    if (stepGoal != -1) {
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("stepgoal", (int) (cntStar * 2000));
                        editor.commit();
                    }
                }
                stepGoal = mPreferences.getInt("stepgoal", 5000);
                tvStepGoalValue.setText("Schrittziel: " + stepGoal + " Schritte");


            }

        });


        getSupportActionBar().setTitle("Go For IT");
        pieChartSteps = findViewById(R.id.pieChart);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Set steps broadcast receiver
        mStepsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: Step receiver got data");
                double steps = stepsForCurrentDay +
                        intent.getDoubleExtra("Steps", 0);
                pieChartSteps.setCenterText(String.valueOf((int)steps) + " Steps");
                pieChartSteps.invalidate();

                entries = new ArrayList<>();
                entries.add(new PieEntry((float)(stepGoal - steps)));
                entries.add(new PieEntry((float)steps));
                set = new PieDataSet(entries, "Label");
                set.setColors(new int[] { Color.DKGRAY, Color.WHITE });
                PieData data = new PieData(set);

                pieChartSteps.setData(data);
                pieChartSteps.setCenterTextColor(R.color.colorAccent);
                pieChartSteps.setDrawHoleEnabled(true);
                pieChartSteps.invalidate(); // refresh
            }
        };

        //set broadcast manager
        LocalBroadcastManager.getInstance(MainActivity.this)
                .registerReceiver(mStepsBroadcastReceiver,new IntentFilter("StepsUpdate"));

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
            mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }

    private double getMaxForMonth(List<double[]> inputList){
        double max = 0;
        double value = 0;
        int i = 0;

        while(i < inputList.size()){
            for (int j = 0; j < 24; j++){
                try {
                    value += inputList.get(i)[j];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (value > max){
                    max = value;
                }
            }
            i++;
        }
        return max;
    }
}
