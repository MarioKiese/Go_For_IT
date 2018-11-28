package de.goforittechnologies.go_for_it.logic.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class StepCounterService extends IntentService implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensor;

    private int steps;

    public static final String NOTIFICATION = "de.goforittechnologies.go_for_it.receiver";

    public StepCounterService() {
        super("StepCounterService");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //publishResults(steps);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        steps = 0;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorManager.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        steps++;

        publishResults(steps);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void publishResults(int steps) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("steps", steps);
        sendBroadcast(intent);
    }

}
