package de.goforittechnologies.go_for_it.logic.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author  Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 *
 * This service is used to log the location and steps of the device this
 * application is
 * installed on.
 *
 */

public class LocationRouteService extends Service implements LocationListener,
        SensorEventListener {

    private static final String TAG = "LocationRouteService";

    // Location management
    private LocationManager mLocationManager;
    private ArrayList<Location> mRoute = new ArrayList<>();
    private long mBaseTime;

    // Step management
    private int mSteps;
    private SensorManager mSensorManager;

    // Binder
    private IBinder mBinder = new LocationBinder();


    public LocationRouteService() {
    }

    /**
     *
     * @param intent Intent for connecting service.
     * @return IBinder to connect service to activity.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind: connected");
        return mBinder;
    }

    /**
     * method to declare and initialise service functions and variables
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate: Start");

        // Set base time for chronometer
        mBaseTime = SystemClock.elapsedRealtime();
        Log.i(TAG, "onCreate: Base time: " + mBaseTime);

        // Create notification for foreground service
        createNotification();

        // Configure location manager
        mRoute.clear();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null) {

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            }

            HandlerThread handlerThread = new
                    HandlerThread("LocationHandlerThread");
            handlerThread.start();
            // Now get the Looper from the HandlerThread
            Looper looper = handlerThread.getLooper();
            // Request location updates to be called back on the HandlerThread
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationRouteService.this, looper);
            // TODO: How to get the best accurate provider, for so long we just use GPS    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, LocationRouteService.this, looper);
            //TODO Prove functionality and necessity for users use
            Location lastKnownLocation =
                    mLocationManager.getLastKnownLocation(
                            LocationManager.NETWORK_PROVIDER);
            mRoute.add(lastKnownLocation);
            sendLocationMessageToActivity(mRoute);

        }

        // Configure steps manager
        mSteps = 0;
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        Sensor mStepSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(LocationRouteService.this,
                mStepSensor, SensorManager.SENSOR_DELAY_UI);

    }

    /**
     * method to unregister LocationRouteService and remove updates
     * @see LocationManager
     */
    @Override
    public void onDestroy() {

        mLocationManager.removeUpdates(LocationRouteService.this);
        mSensorManager.unregisterListener(LocationRouteService.this);
        super.onDestroy();

    }

    /**
     * method to log the location for storing location and
     * send current location to corresponding activity for displaying.
     *
     * @param location input location data to store and display
     */
    // Location
    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        mRoute.add(location);
        sendLocationMessageToActivity(mRoute);

    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }


    @Override
    public void onProviderEnabled(String s) {

    }


    @Override
    public void onProviderDisabled(String s) {

    }

    /**
     * method to create intent to transfer the location ArrayList to
     * corresponding activity
     * @param route ArrayLIst out of location point to build route
     */
    // Communication methods
    private void sendLocationMessageToActivity(ArrayList<Location> route) {

        Intent locationIntent = new Intent("LocationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("Location", route);
        locationIntent.putExtra("Location", bundle);
        LocalBroadcastManager.getInstance(LocationRouteService.this)
                .sendBroadcast(locationIntent);

    }

    public long getmBaseTime() {

        return mBaseTime;
    }

    /**
     * method to create notification "Background location service in foreground"
     */
    private void createNotification() {

        Intent notificationIntent = new Intent(this,
                LocationRouteService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

        Notification notification = null;

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "de.goforittechnologies.go_for_it";
            String channelName = "My Background Service";
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
                    .setContentText("Your route is being recorded!")
                    .setContentIntent(pendingIntent)
                    .setTicker("2")
                    .build();
        }

        startForeground(12345678, notification);
    }

    // StepCounter

    /**
     * method to count steps while recording location route and send
     * information to corresponding activity
     * @param sensorEvent event triggered on sensor-event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mSteps++;
        sendStepMessageToActivity(mSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class LocationBinder extends Binder {

        public LocationRouteService getService() {

            return LocationRouteService.this;
        }
    }

    /**
     * mathod to send step-value to activity via intent
     * @param steps
     */

    private void sendStepMessageToActivity(int steps) {

        Intent stepIntent = new Intent("StepsUpdate");
        stepIntent.putExtra("Steps", steps);
        LocalBroadcastManager.getInstance(LocationRouteService.this)
                .sendBroadcast(stepIntent);

    }

}
