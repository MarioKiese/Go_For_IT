package de.goforittechnologies.go_for_it;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Widgets
    TextView tvSensorValue;
    private Toolbar tbMain;

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

        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);

        getSupportActionBar().setTitle("Go For IT");

        tvSensorValue = findViewById(R.id.tvSensorValue);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        stepCounterHandler = new StepCounterHandler();
        stepCounterListener = new StepCounterListener(stepCounterHandler);

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

            tvSensorValue.setText(String.valueOf(msg.what));

        }
    }

}
