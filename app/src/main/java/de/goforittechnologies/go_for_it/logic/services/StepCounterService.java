package de.goforittechnologies.go_for_it.logic.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;

/**
 * @author  Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 *
 * This service is used to count steps made by user and transfer them to the
 * corresponding activity via intent.
 * @see SensorEventListener
 * @see Service
 *
 */

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private static final String TAG = "StepCounterService";
    private double steps;
    public static final String
            NOTIFICATION = "de.goforittechnologies.go_for_it.receiver";
    private boolean oldTimeSet = false;
    private long oldTime = 0;

    public StepCounterService() {
    }

    /**
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *  method to declare and initialise service functions and variables
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service started");

        // Create notification for foreground service
        createNotification();

        steps = 0;
        sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this,
                sensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * method used to store current step-value in database every 10 seconds
     * based on todays date.
     *
     * @param sensorEvent event triggered on sensor-event
     *
     * @see Calendar
     *
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        steps++;
        sendStepMessageToActivity(steps);
        Log.d(TAG, "onSensorChanged: Stepscount:"+ steps);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        if (!oldTimeSet){
            oldTime = System.currentTimeMillis();
            oldTimeSet = true;
        }
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - oldTime;
        if (diffTime >= 10000){
            oldTimeSet = false;
            String dbName = "StepDataTABLE_"+
                    (calendar.get(Calendar.MONTH)+1);
            updateDatabase(steps,dbName,calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY));
            Log.d(TAG, "onSensorChanged: Day " +
                    calendar.get(Calendar.DAY_OF_MONTH));
            Log.d(TAG, "onSensorChanged: Hour " +
                    calendar.get(Calendar.HOUR_OF_DAY));
            Log.d(TAG, "onSensorChanged: Month " +
                    (calendar.get(Calendar.MONTH)+1));

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * method to update step-value of current hour of todays date.
     *
     * @param steps value to store into database.
     * @param dbName name of database.
     * @param day day of timestamp for position in database table for current
     *           month.
     * @param hour hour of timestamp for position in database table for
     *             current month.
     *
     * @see DataSourceStepData
     */
    private void updateDatabase(double steps,
                                String dbName, int day, int hour){

        DataSourceStepData dataSourceStepData;
        try {
            dataSourceStepData = new DataSourceStepData(this,
                    dbName,0);
        } catch (Exception e) {
            e.printStackTrace();
            dataSourceStepData = new DataSourceStepData(this,
                    dbName,0);
        }

        dataSourceStepData.open();
        String timestamp = day +":" + hour;
        try {
            dataSourceStepData.updateStepData(steps,timestamp);
        } catch (Exception e) {
            e.printStackTrace();

        }

        dataSourceStepData.close();
    }

    /**
     * method to create Intent for transfer step-value to corresponding
     * activity.
     * @param steps value to sent to activity
     */
    private void sendStepMessageToActivity(double steps) {
        Intent stepsIntent = new Intent("GeneralStepsUpdate");
        stepsIntent.putExtra("Steps", steps);
        LocalBroadcastManager.getInstance(StepCounterService.this)
                .sendBroadcast(stepsIntent);
        Log.d(TAG, "sendStepMessageToActivity: Steps sent");
    }

    @SuppressWarnings("deprecation")
    private void createNotification() {

        Intent notificationIntent = new Intent(this,
                LocationRouteService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

        Notification notification;

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "de.goforittechnologies.go_for_it";
            String channelName = "Step Counter Service";
            NotificationChannel chan = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);


            notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Go For IT")
                    .setContentText("Your steps are counted!")
                    .setContentIntent(pendingIntent)
                    .build();
        } else {

            notification =
                    new NotificationCompat.Builder(StepCounterService.this)
                            .setContentTitle("Go For IT")
                            .setContentText("Your steps are counted!")
                            .setSmallIcon(R.drawable.logo)
                            .setContentIntent(pendingIntent)
                            .setStyle(new NotificationCompat.BigTextStyle())
                            .build();
        }

        startForeground(123456788, notification);
    }
}
