package de.goforittechnologies.go_for_it;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;


public class StepCounterListener implements SensorEventListener {

    private Handler handler;

    public StepCounterListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int steps = (int) sensorEvent.values[0];
        handler.sendEmptyMessage(steps);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
