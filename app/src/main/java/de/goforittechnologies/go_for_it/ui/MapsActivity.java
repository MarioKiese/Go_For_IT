package de.goforittechnologies.go_for_it.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.services.LocationRouteService;
import de.goforittechnologies.go_for_it.logic.services.LocationParcel;

public class MapsActivity extends AppCompatActivity {

    private static final String TAG = "MapsActivity";

    // Widgets
    private MapView mapView;
    private Button btnStartLocation;
    private Button btnStopLocation;
    private Chronometer chronometer;

    // Service
    private Intent locationRouteIntent;
    private BroadcastReceiver mBroadcastReceiver;

    // Shared preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    // Permissions
    private static final int PERMISSION_ALL = 1;
    private static final String [] PERMISSIONS = {

        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        // Check permission for location and storage
        if (Build.VERSION.SDK_INT >= 23) {

            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }

        }

        // Configure map
        mapView = findViewById(R.id.mvMap);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        // Set broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "onReceive: Got data!");

                Bundle bundle = intent.getBundleExtra("Location");
                ArrayList<LocationParcel> data = bundle.getParcelableArrayList("Location");

                if (data != null) {

                    List<Location> route = convertToLocationList(data);

                    if (route != null) {

                        showRoute(route);

                    }

                }

            }
        };

        // Set broadcast manager
        LocalBroadcastManager.getInstance(MapsActivity.this).registerReceiver(mBroadcastReceiver, new IntentFilter("LocationUpdate"));

        // Set shared preferences
        pref = getApplicationContext().getSharedPreferences("MapsPref", MODE_PRIVATE);
        editor = pref.edit();

        // Set widgets
        btnStartLocation = findViewById(R.id.btn_start_location);
        btnStopLocation = findViewById(R.id.btn_stop_location);
        chronometer = findViewById(R.id.chronometer);

        // Configure widgets
        if (pref.getBoolean("service_started", false)) {

            btnStartLocation.setEnabled(false);
            btnStopLocation.setEnabled(true);

        } else {

            btnStartLocation.setEnabled(true);
            btnStopLocation.setEnabled(false);

        }

        // OnClickListener
        btnStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationRouteIntent = new Intent(MapsActivity.this, LocationRouteService.class);
                startService(locationRouteIntent);

                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();

                btnStartLocation.setEnabled(false);
                btnStopLocation.setEnabled(true);

                editor.putBoolean("service_started", true);
                editor.apply();

            }
        });

        btnStopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopService(new Intent(MapsActivity.this, LocationRouteService.class));

                chronometer.stop();

                btnStartLocation.setEnabled(true);
                btnStopLocation.setEnabled(false);

                editor.putBoolean("service_started", false);
                editor.apply();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {

        LocalBroadcastManager.getInstance(MapsActivity.this).unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }

    private void showRoute(List<Location> route) {

        List<GeoPoint> geoPoints = new ArrayList<>();
        Polyline polyline = new Polyline();
        polyline.setGeodesic(true);
        polyline.setColor(Color.BLUE);
        polyline.setWidth(5);
        polyline.setWidth(20f);


        if (!route.isEmpty()) {

            mapView.getController().setCenter(new GeoPoint(route.get(route.size()-1)));
            mapView.getOverlayManager().clear();

            if (route.size() > 1) {

                for (int i=0; i<route.size(); i++) {

                    GeoPoint point = new GeoPoint(route.get(i));
                    geoPoints.add(point);

                }

                polyline.setPoints(geoPoints);
                mapView.getOverlayManager().add(polyline);

            }

        }

    }

    public boolean hasPermissions(Context context, String[] permissions) {

        if (context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    return false;

                }

            }

        }

        return true;

    }

    private List<Location> convertToLocationList(List<LocationParcel> sourceList) {

        List<Location> destList = new ArrayList<>();

        for (LocationParcel item : sourceList) {

            destList.add(item.getLocation());

        }

        return destList;

    }

}
