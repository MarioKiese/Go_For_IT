package de.goforittechnologies.go_for_it;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import de.goforittechnologies.go_for_it.ui.LocationParcel;


public class LocationRouteService extends Service implements LocationListener {

    private static final String TAG = "LocationRouteService";

    private LocationManager mLocationManager;
    private ArrayList<LocationParcel> mRoute = new ArrayList<>();

    /*class LocationServiceBinder extends Binder {

        public LocationRouteService getService() {

            return LocationRouteService.this;

        }

    }*/

//    private IBinder mBinder = new LocationServiceBinder();

    public LocationRouteService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind: Start");

        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        }

        mRoute.clear();

    }

    @Override
    public void onDestroy() {

        mLocationManager.removeUpdates(this);

        super.onDestroy();

    }


    @Override
    public void onLocationChanged(Location location) {

                Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
                LocationParcel locationParcel = new LocationParcel(location);

                mRoute.add(locationParcel);
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

    private void sendLocationMessageToActivity(ArrayList<LocationParcel> route) {

        Intent locationIntent = new Intent("LocationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("Location", route);
        locationIntent.putExtra("Location", bundle);
        LocalBroadcastManager.getInstance(LocationRouteService.this).sendBroadcast(locationIntent);

    }

}
