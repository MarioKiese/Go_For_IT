package de.goforittechnologies.go_for_it.ui;

import android.content.BroadcastReceiver;
import android.annotation.SuppressLint;
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
import android.support.v7.view.menu.MenuBuilder;
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
import java.util.Objects;
import java.util.TimeZone;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.StepCounterListener;
import de.goforittechnologies.go_for_it.logic.services.StepCounterService;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;

import static android.content.SharedPreferences.*;

/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class creates the main overview of the "Go_for_IT" android app.
 * Corresponding layout: res.layout.activity_main.xml
 *
 * The user can navigate to the different functions using the button in the
 * top right corner.
 *
 * The pie-chart displayes the steps you need to walk to archive the stepgoal.
 *
 * The surrounding Infocards display the values of active minutes that day,
 * the maximal steps on one day of the current month,
 * the burned calories on that day (based on average weight of 75 kg)
 * the walked distance on that day (based on average footlength of 65cm).
 *
 * The user is able to set the step-goal by seleting stars of the rating-bar
 * (2000 steps per star).
 * To Confirm the step-goal the user can use the button below the rating-bar
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //________________________________________________________________________//
    // declaring (&initialising) widgets
    //________________________________________________________________________//
    // displayed elements
    private TextView tvStepGoalValue;
    private TextView tvBurnedCalories;
    private TextView tvTravelledDistance;
    private RatingBar rbStepGoal;
    private PieChart pieChartSteps;
    // status variables
   private double stepsForCurrentDay;
   private int stepGoal = 0;
   private Boolean firstTime = null;
   private List<PieEntry> entries = new ArrayList<>();
   private PieDataSet set;

    //shared preferences
   SharedPreferences mPreferences;

    StepCounterListener stepCounterListener;

    //Sensor
    SensorManager sensorManager;
    Sensor sensor;

    // Firebase
    private FirebaseAuth mAuth;



    //________________________________________________________________________//
    // onCreate method
    //________________________________________________________________________//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Start");

        //_________________________________________________//
        // declaring &initialising
        //_________________________________________________//
        mPreferences = MainActivity.this
                .getSharedPreferences("first_time",
                Context.MODE_PRIVATE);
        DataSourceStepData dataSourceStepData;
        Intent stepIntent = new Intent(MainActivity.this,
                StepCounterService.class);
        startService(stepIntent);
        //time sensitive variables
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        mAuth = FirebaseAuth.getInstance();
        //_________________________________________________//
        // linking views by id
        //_________________________________________________//

        TextView tvMaxSteps         = findViewById(R.id.tvMaxSteps);
        tvStepGoalValue             = findViewById(R.id.tvStepGoalValue);
        tvBurnedCalories            = findViewById(R.id.tvBurnedCalories);
        tvTravelledDistance         = findViewById(R.id.tvTravelledDistance);
        TextView tvActiveMinutes    = findViewById(R.id.tvActiveMinutes);
        Button btnConfirmStepGoal   = findViewById(R.id.btnConfirmStepGoal);
        rbStepGoal                  = findViewById(R.id.rbStepGoal);
        Toolbar tbMain              = findViewById(R.id.tbMain);
        pieChartSteps               = findViewById(R.id.pieChart);

        //_________________________________________________//
        // setting data for first activity-overview
        //_________________________________________________//
        setSupportActionBar(tbMain);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Go For IT");
        stepGoal = mPreferences.getInt("stepgoal", 0);
        Log.d(TAG, "onClick: stepGoal: "+ stepGoal );
        tvStepGoalValue.setText("Schrittziel: " + stepGoal + " Schritte");

        //_________________________________________________//
        // creating table on first use for data storage
        //_________________________________________________//
        if (isFirstTime()){
            //Create empty database for current month on first Start
            for (int m = 1; m <=1; m++ ){
                dataSourceStepData = new DataSourceStepData(this,
                        "StepDataTABLE_"+ m,1);

                dataSourceStepData.open();
                for (int i = 1; i <=31; i++){
                    for (int j = 0; j <24; j++){
                        dataSourceStepData
                        .createStepData(0,i+":"+j);
                        //Log.d(TAG, "onCreate: StepDataEmpty:" + i + ":" +j);
                    }
                }
                dataSourceStepData.close();
            }
        }

        //_________________________________________________//
        // selecting data from current day for displaying
        //_________________________________________________//
        String dbName = "StepDataTABLE_"+ (calendar.get(Calendar.MONTH)+1);
        dataSourceStepData =
                new DataSourceStepData(this,dbName, 0);

        List<double[]> stepList = null;
        try {
            dataSourceStepData.open();
            stepList = dataSourceStepData.getAllStepData();
            dataSourceStepData.close();
            int activeMinutes = 0;
            tvMaxSteps.setText(String.valueOf((int)getMaxForMonth(stepList)));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            for (int i = 0; i <= hour; i++){
                stepsForCurrentDay += stepList.get(day - 1)[i];
                //activeminutes = steps/(127.5 (steps/min))
                // average: 0.65 m = 1 step
                // average: 5 km/h = 5000m / h = 83,333m /min = 127.5 steps/min
                activeMinutes += (int)((stepList.get(day - 1)[i])/127.5);
            }

            tvActiveMinutes.setText(String.valueOf(activeMinutes));
            Log.d(TAG, "onCreate: stepsForcurrentDay: " +
                    stepsForCurrentDay);
        } catch (Exception e) {
            e.printStackTrace();
        }

        entries.add(new PieEntry((float) 0));
        set = new PieDataSet(entries, "");
        set.setColors( Color.WHITE);
        PieData data = new PieData(set);
        pieChartSteps.setCenterText((int)stepsForCurrentDay + " Steps");
        pieChartSteps.setData(data);
        //just for safety of no chart-data can be loaded
        pieChartSteps.setNoDataText("Make Steps to calculate data");
        pieChartSteps.getDescription().setEnabled(false);
        pieChartSteps.setDrawHoleEnabled(true);
        pieChartSteps.invalidate();

        //_________________________________________________//
        // set step goal
        //_________________________________________________//
        rbStepGoal.setMax(6);
        rbStepGoal.invalidate();
        btnConfirmStepGoal.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                float cntStar = rbStepGoal.getRating();

                    Editor editor = mPreferences.edit();
                    editor.putInt("stepgoal", (int) (cntStar * 2000));
                    editor.apply();

                stepGoal = mPreferences.getInt("stepgoal", 0);
                Log.d(TAG, "onClick: stepGoal: "+ stepGoal );
                tvStepGoalValue.setText("Schrittziel: " + stepGoal
                        + " Schritte");
            }
        });

        //_________________________________________________//
        // prepare Sensor Usage
        //_________________________________________________//
        sensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_STEP_COUNTER);

        //_________________________________________________//
        // update pieChart and infocards
        //_________________________________________________//
        // Set steps broadcast receiver
        //Service usage
        BroadcastReceiver mStepsBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: Step receiver got data");
                double steps = stepsForCurrentDay +
                        intent.getDoubleExtra("Steps", 0);
                pieChartSteps.setCenterText(String.valueOf((int) steps) +
                        " Steps");
                entries = new ArrayList<>();
                if (stepGoal - steps < 0) {
                    entries.add(new PieEntry(0f));
                } else {
                    entries.add(new PieEntry((float) (stepGoal - steps)));
                }
                entries.add(new PieEntry((float) steps));
                set = new PieDataSet(entries, "");
                set.setColors(Color.DKGRAY, Color.WHITE);
                PieData data = new PieData(set);
                pieChartSteps.setData(data);
                pieChartSteps.setCenterTextColor(R.color.colorAccent);
                pieChartSteps.setDrawHoleEnabled(true);
                pieChartSteps.invalidate();
                //formula: average steplenght: 0.65m
                double distance = steps * 0.65;
                tvTravelledDistance.setText(String.valueOf((int) distance) +
                        " m");
                //formula: average burned calories per kilometer:
                // weight (70 kilogram) * 0,75
                tvBurnedCalories.setText(
                        String.valueOf((int) (70 * 0.75 * distance / 1000)) +
                                " kcal");
            }
        };

        //_________________________________________________//
        // set broadcast manager
        //_________________________________________________//

        LocalBroadcastManager.getInstance(MainActivity.this)
                .registerReceiver(mStepsBroadcastReceiver,new IntentFilter(
                        "GeneralStepsUpdate"));

    }
    //________________________________________________________________________//
    // onStart method
    //________________________________________________________________________//

    /**
     * authenticate the user for login
     */
    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            sendToLogin();

        }

    }

    //________________________________________________________________________//
    // onCreateOptionsMenu method
    //________________________________________________________________________//

    /**
     *
     * @param menu
     * @return true
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    //________________________________________________________________________//
    // onOptionsItemSelected method
    //________________________________________________________________________//

    /**
     * method to detect the selected item in the toolbar.
     *
     * @param item Item from Menu.
     * @return true if one item was selected, otherwise false.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_location_btn:

                Intent locationIntent = new Intent(
                        MainActivity.this,
                        MapActivity.class);
                startActivity(locationIntent);
                return true;

            case R.id.action_challenge_btn:

                Intent challengesOverviewIntent = new Intent(
                        MainActivity.this,
                        ChallengesOverviewActivity.class);
                startActivity(challengesOverviewIntent);
                return true;

            case R.id.action_settings_btn:

                Intent settingsIntent = new Intent(
                        MainActivity.this,
                        SetupActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_logout_btn:

                logOut();
                return true;

            case R.id.action_dashboard_btn:

                Intent dashBoardIntent
                = new Intent(MainActivity.this,
                DashboardActivity.class);
                startActivity(dashBoardIntent);
                return true;

            default:

                return false;

        }
    }

    //________________________________________________________________________//
    // onResume method
    //________________________________________________________________________//

    /**
     * register step-counter listener
     */
    @Override
    protected void onResume() {
        super.onResume();
       /* registerReceiver(receiver, new IntentFilter(
                StepCounterService.NOTIFICATION));*/

        sensorManager.registerListener(stepCounterListener, sensor,
                SensorManager.SENSOR_DELAY_UI);

    }

    //________________________________________________________________________//
    // onPause method
    //________________________________________________________________________//

    /**
     * unregister step-counter listener
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(stepCounterListener);
    }

    //________________________________________________________________________//
    // sendToLogin method
    //________________________________________________________________________//

    /**
     * login user and start MainActivity
     */
    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this,
                LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }

    //________________________________________________________________________//
    // logOut method
    //________________________________________________________________________//

    /**
     * logging user out of firebase account
     */
    private void logOut() {

        mAuth.signOut();
        sendToLogin();

    }

    //________________________________________________________________________//
    // isFirstTime method
    //________________________________________________________________________//

    /**
     *
     * @return true if the app starts first time after installation,
     * return false otherwise.
     */
    private boolean isFirstTime() {
        if (firstTime == null) {
            mPreferences = this.getSharedPreferences("first_time",
                    Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
            }
        }
        return firstTime;
    }

    //________________________________________________________________________//
    // getMaxForMonth method
    //________________________________________________________________________//

    /**
     *
     * @param inputList list of steps (list representing one month
     * out of 24 double arrays representing 24 hours of one day).
     * @return maximum step value out of adding 24 hours of each day.
     */
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