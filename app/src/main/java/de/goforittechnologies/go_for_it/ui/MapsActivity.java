package de.goforittechnologies.go_for_it.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.services.LocationParcel;
import de.goforittechnologies.go_for_it.logic.services.LocationRouteService;

public class MapsActivity extends AppCompatActivity {

    private static final String TAG = "MapsActivity";

    // Widgets
    private MapView mapView;
    private Button btnStartLocation;
    private Button btnStopLocation;
    private TextView tvDistanceText;
    private TextView tvDistanceValue;
    private TextView tvCaloriesText;
    private TextView tvCaloriesValue;
    private TextView tvTimeText;
    private Chronometer chronometer;
    private Toolbar tbMaps;

    // Service
    private Intent locationRouteIntent;
    private BroadcastReceiver mLocationBroadcastReceiver;
    private LocationRouteService mLocationRouteService;
    private ServiceConnection mServiceConnection;
    private boolean mIsServiceBound;

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

        // Set toolbar
        tbMaps = findViewById(R.id.tbMaps);
        setSupportActionBar(tbMaps);
        getSupportActionBar().setTitle("Maps");

        // Configure map
        mapView = findViewById(R.id.mvMap);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        // Set widgets
        btnStartLocation = findViewById(R.id.btn_start_location);
        btnStopLocation = findViewById(R.id.btn_stop_location);
        tvDistanceText = findViewById(R.id.tvDistance);
        tvDistanceValue = findViewById(R.id.tvDistanceValue);
        tvCaloriesText = findViewById(R.id.tvCalories);
        tvCaloriesValue = findViewById(R.id.tvCaloriesValue);
        tvTimeText = findViewById(R.id.tvTime);
        chronometer = findViewById(R.id.chronometer);

        // Set location broadcast receiver
        mLocationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "onReceive: Location receiver got data");

                Bundle bundle = intent.getBundleExtra("Location");
                ArrayList<LocationParcel> data = bundle.getParcelableArrayList("Location");

                if (data != null) {

                    List<Location> route = convertToLocationList(data);

                    if (route != null) {

                        showRoute(route);
                        showDistance(route);

                    }

                }

            }

        };

        // Set broadcast manager
        LocalBroadcastManager.getInstance(MapsActivity.this).registerReceiver(mLocationBroadcastReceiver, new IntentFilter("LocationUpdate"));

        // Set shared preferences
        pref = getApplicationContext().getSharedPreferences("MapsPref", MODE_PRIVATE);
        editor = pref.edit();

        // Configure widgets
        if (pref.getBoolean("service_started", false)) {

            btnStartLocation.setEnabled(false);
            btnStopLocation.setEnabled(true);

        } else {

            btnStartLocation.setEnabled(true);
            btnStopLocation.setEnabled(false);

        }

        // Create location intent
        locationRouteIntent = new Intent(MapsActivity.this, LocationRouteService.class);
        mIsServiceBound = false;

        // Manage service binding
        if (pref.getBoolean("service_started", false)) {

            bindService();

            if (mIsServiceBound) {

                chronometer.setBase(mLocationRouteService.getmBaseTime());
                chronometer.start();

            }

        }

        // OnClickListener
        btnStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startService(locationRouteIntent);
                editor.putBoolean("service_started", true);
                editor.apply();
                bindService();

                btnStartLocation.setEnabled(false);
                btnStopLocation.setEnabled(true);

            }
        });

        btnStopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unbindService();
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.maps_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_routes_btn:



                return true;


            default:

                return false;

        }

    }

    @Override
    protected void onStart() {

        Log.i(TAG, "onStart: Start");

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {

        LocalBroadcastManager.getInstance(MapsActivity.this).unregisterReceiver(mLocationBroadcastReceiver);

        unbindService();


        super.onDestroy();
    }


    // Methods

    private void bindService() {

        if (mServiceConnection == null) {

            mServiceConnection = new ServiceConnection() {


                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

                    LocationRouteService.LocationBinder locationBinder = (LocationRouteService.LocationBinder) iBinder;
                    mLocationRouteService = locationBinder.getService();

                    // Only if service is started, get Base time from service
                    if (pref.getBoolean("service_started", false)) {

                        chronometer.setBase(mLocationRouteService.getmBaseTime());
                        chronometer.start();

                    }

                    mIsServiceBound = true;

                    Log.i(TAG, "bindService: connected");

                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                    mIsServiceBound = false;

                    Log.i(TAG, "unbindService: disconnected");

                }

            };

        }

        bindService(locationRouteIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void unbindService() {

        if (mIsServiceBound) {

            unbindService(mServiceConnection);
            mIsServiceBound = false;

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

    private List<Location> convertToLocationList(List<LocationParcel> sourceList) {

        List<Location> destList = new ArrayList<>();

        for (LocationParcel item : sourceList) {

            destList.add(item.getLocation());

        }

        return destList;

    }

    private double getDistance(List<Location> route) {

        double kilometers = 0.0;

        for (int i=0; i<route.size()-1; i++) {

            kilometers += route.get(i).distanceTo(route.get(i+1));

        }

        return kilometers;

    }

    private void showDistance(List<Location> route) {

        DecimalFormat df2 = new DecimalFormat(".##");

        String value = String.valueOf(df2.format(getDistance(route)));

        tvDistanceValue.setText(value);

    }

    private double getCalories() {

        double calories = 0.0;

        return calories;

    }

    private void showCalories() {



    }

}

