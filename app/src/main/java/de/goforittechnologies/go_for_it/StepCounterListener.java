package de.goforittechnologies.go_for_it;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;


public class StepCounterListener implements SensorEventListener {

    private Handler handler;
    private int steps = 0;

    public StepCounterListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        steps++;
        handler.sendEmptyMessage(steps);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
