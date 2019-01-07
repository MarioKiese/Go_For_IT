package de.goforittechnologies.go_for_it.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.services.LocationRouteService;
import de.goforittechnologies.go_for_it.storage.DataSourceMapData;
import de.goforittechnologies.go_for_it.storage.DataSourceRouteData;
import de.goforittechnologies.go_for_it.storage.RouteData;
/**
 * @author  Mario Kiese.
 * @version 0.8.
 * @see AppCompatActivity
 *
 *
 * This class is used to log into the firebase account.
 *
 *
 * Corresponding layout: res.layout.activity_login.
 *
 * The user can type in username and password.
 *
 * The user can log himself in by klicking the "login" button.
 *
 * The user can create a new firebase account by klicking "create new
 * account" at the bottom.
 * @see RegisterActivity
 *
 *
 */
public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    // Widgets
    private MapView mapView;
    private Button btnStartLocation;
    private Button btnStopLocation;
    private TextView tvStepsValue;
    private TextView tvDistanceValue;
    private TextView tvCaloriesText;
    private TextView tvCaloriesValue;
    private TextView tvTimeText;
    private Chronometer chronometer;
    private Toolbar tbMaps;

    // Service
    private Intent locationRouteIntent;
    private BroadcastReceiver mLocationBroadcastReceiver;
    private BroadcastReceiver mStepsBroadcastReceiver;
    private LocationRouteService mLocationRouteService;
    private ServiceConnection mServiceConnection;
    private boolean mIsServiceBound;

    // Route information
    private List<Location> mRoute;
    private int mSteps;
    private double mDistance;
    private double mCalories;
    private double mWeight;

    private LocationManager locationManager;

    // Shared preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // Database
    private DataSourceMapData dataSourceMapData;
    private DataSourceRouteData dataSourceRouteData;

    // Permissions
    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {

            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    };
    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - checking permissions for location and storage.
     * - initialise database.
     * - setup Broadcast receivers.
     * - setup LocalBroadcastManager.
     * - set click listeners.
     *
     * @see BroadcastReceiver
     * @see LocalBroadcastManager
     * @see MapActivity
     * @see Location
     * @see MapView
     * @see SharedPreferences
     * @see LocationRouteService
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        // Check permission for location and storage
        if (Build.VERSION.SDK_INT >= 23) {

            if (!hasPermissions(this, PERMISSIONS)) {

                ActivityCompat.requestPermissions(
                        this, PERMISSIONS, PERMISSION_ALL);
            }
        }

        // Initialize database
        initializeDatabase();

        // Configure map
        mapView = findViewById(R.id.mvMap);
        initializeMap();

        // Set widgets
        tbMaps = findViewById(R.id.tbMaps);
        setSupportActionBar(tbMaps);
        btnStartLocation = findViewById(R.id.btn_start_location);
        btnStopLocation = findViewById(R.id.btn_stop_location);
        TextView tvSteps = findViewById(R.id.tvSteps);
        tvStepsValue = findViewById(R.id.tvStepsValue);
        TextView tvDistanceText = findViewById(R.id.tvDistance);
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
                ArrayList<Location> data =
                        bundle.getParcelableArrayList("Location");

                if (data != null) {
                    mRoute = data;
                    showRoute(mRoute);
                    showDistance(mRoute);
                    showCalories(mDistance);
                }
            }
        };

        // Set steps broadcast receiver
        mStepsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive: Steps receiver got data");

                mSteps = intent.getIntExtra("Steps", 0);
                if (mSteps == 0) {

                    tvStepsValue.setText("-");
                } else {

                    tvStepsValue.setText(String.valueOf(mSteps));
                }
            }
        };

        // Set broadcast manager
        LocalBroadcastManager.getInstance(MapActivity.this)
                .registerReceiver(mLocationBroadcastReceiver,
                        new IntentFilter("LocationUpdate"));
        LocalBroadcastManager.getInstance(MapActivity.this)
                .registerReceiver(mStepsBroadcastReceiver,
                        new IntentFilter("StepsUpdate"));

        // Set shared preferences
        pref = getApplicationContext()
                .getSharedPreferences("MapsPref", MODE_PRIVATE);
        editor = pref.edit();

        // Get weight
        String weight = pref.getString("weight", "70");
        Log.d(TAG, "onCreate: Weight : " + weight);
        mWeight = Double.valueOf(weight);

        // Configure widgets
        if (pref.getBoolean("service_started", false)) {

            btnStartLocation.setEnabled(false);
            btnStopLocation.setEnabled(true);
        } else {

            btnStartLocation.setEnabled(true);
            btnStopLocation.setEnabled(false);
        }

        // Create location intent
        locationRouteIntent = new Intent(MapActivity.this,
                LocationRouteService.class);
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
        btnStartLocation.setOnClickListener(view -> {

            if (mRoute != null) {
                mRoute.clear();
            }
            mSteps = 0;
            mDistance = 0.0;
            mCalories = 0.0;
            tvStepsValue.setText("-");
            tvDistanceValue.setText("-");
            tvCaloriesValue.setText("-");

            startService(locationRouteIntent);
            editor.putBoolean("service_started", true);
            editor.apply();
            bindService();

            btnStartLocation.setEnabled(false);
            btnStopLocation.setEnabled(true);
        });

        btnStopLocation.setOnClickListener(view -> {

            unbindService();
            stopService(new Intent(MapActivity.this,
                    LocationRouteService.class));

            chronometer.stop();

            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(MapActivity.this);
            dialogBuilder.setTitle("Save route?");

            // Set up the input
            final EditText input = new EditText(MapActivity.this);

            // Specify the type of input expected
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogBuilder.setView(input);

            // Set up the buttons
            dialogBuilder.setPositiveButton("OK", null);

            dialogBuilder.setNegativeButton("Cancel",
                    (dialog, which) -> dialog.cancel());

            AlertDialog alertDialog = dialogBuilder.create();

            // Implementation of a View.OnClickListener to control
            // if dialog can be dismissed
            // (not given with DialogInterface.OnClickListener)
            alertDialog.setOnShowListener(new DialogInterface
                    .OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button positiveButton = alertDialog
                            .getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String routeName = input.getText().toString();

                            if (validateRouteName(routeName)) {
                                routeName = formatRouteName(routeName);
                                if (checkIfRouteNameExists(routeName)) {
                                    Toast.makeText(MapActivity.this,
                                    "Route name already exists! " +
                                    "Please enter " +
                                    "another name!",
                                    Toast.LENGTH_LONG).show();
                                } else {
                                    writeInDatabases(routeName);
                                    alertDialog.dismiss();
                                }

                            } else {
                                Toast.makeText(MapActivity.this,
                                        "Please enter a valid name",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });

            alertDialog.show();

            btnStartLocation.setEnabled(true);
            btnStopLocation.setEnabled(false);

            editor.putBoolean("service_started", false);
            editor.apply();

        });
    }

    /**
     * method to initialise the menu
     *
     * @param menu menu to build up
     * @return true of called
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.maps_menu, menu);

        return true;

    }

    /**
     *
     * method to switch depending on selected item of menu
     *
     * @param item selected menu item
     * @return true of called
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_routes_btn:

                Intent routesIntent = new Intent(MapActivity.this,
                        RoutesListActivity.class);
                startActivity(routesIntent);

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

    /**
     * method to unregister location and step broadcast receiver when
     * activity is destroyed.
     *
     * @see BroadcastReceiver
     */
    @Override
    protected void onDestroy() {

        // Unregister broadcast
        LocalBroadcastManager.getInstance(MapActivity.this)
                .unregisterReceiver(mLocationBroadcastReceiver);
        LocalBroadcastManager.getInstance(MapActivity.this)
                .unregisterReceiver(mStepsBroadcastReceiver);

        unbindService();

        // Close databases
        dataSourceMapData.close();
        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");

        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");
        dataSourceRouteData.close();

        super.onDestroy();
    }

    /**
     *
     * method to bind service to activity to simplify the communication
     * between service and activity
     *
     * @see ServiceConnection
     * @see ComponentName
     */
    private void bindService() {

        if (mServiceConnection == null) {

            mServiceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName componentName,
                                               IBinder iBinder) {

                    LocationRouteService.LocationBinder locationBinder =
                            (LocationRouteService.LocationBinder) iBinder;
                    mLocationRouteService = locationBinder.getService();

                    // Only if service is started, get Base time from service
                    if (pref.getBoolean("service_started", false)) {

                        chronometer.setBase(mLocationRouteService
                                .getmBaseTime());
                        chronometer.start();

                        mSteps = mLocationRouteService
                                .getmSteps();
                        if (mSteps == 0) {

                            tvStepsValue.setText("-");
                        } else {

                            tvStepsValue.setText(String
                                    .valueOf(mLocationRouteService
                                            .getmSteps()));
                        }
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

        bindService(locationRouteIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * method to unbind service from activity.
     *
     * @see ServiceConnection
     */
    private void unbindService() {

        if (mIsServiceBound) {

            unbindService(mServiceConnection);
            mIsServiceBound = false;
        }
    }

    /**
     * method to check if needed permissions are granted
     *
     * @param context current context of app
     * @param permissions array out of permissions to check
     * @return return true if permissions are granted, return false if not
     *
     * @see ActivityCompat
     * @see PackageManager
     */
    private boolean hasPermissions(Context context, String[] permissions) {

        if (context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * method to initialize map view for displaying in activity
     *
     * @see ActivityCompat
     * @see PackageManager
     * @see LocationManager
     */
    private void initializeMap() {

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat
            .checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location currentLocation
        = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (currentLocation != null) {

            GeoPoint currentGeoPoint = new GeoPoint(currentLocation);

            mapView.getController().setCenter(currentGeoPoint);
        } else {

            Toast.makeText(MapActivity.this,
            "Position is null", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * method to display route out of location points
     *
     * @param route route to display
     *
     * @see GeoPoint
     * @see Polyline
     */
    private void showRoute(List<Location> route) {

        List<GeoPoint> geoPoints = new ArrayList<>();
        Polyline polyline = new Polyline();
        polyline.setGeodesic(true);
        polyline.setColor(Color.BLUE);
        polyline.setWidth(5);
        polyline.setWidth(20f);

        if (!route.isEmpty()) {

            mapView.getController().setCenter(
                    new GeoPoint(route.get(route.size()-1)));
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

    /**
     * method to claculate distance out of location points
     *
     * @param route route for calculation basis
     * @return double value of distance
     *
     * @see Location
     */
    private double getDistance(List<Location> route) {

        double kilometers = 0.0;

        for (int i=0; i<route.size()-1; i++) {

            kilometers += route.get(i).distanceTo(route.get(i+1));
        }

        return kilometers;
    }

    /**
     * method to display distance in activity
     *
     * @param route route for calculation and display basis
     */
    private void showDistance(List<Location> route) {

        mDistance = getDistance(route);
        mDistance = Math.round(mDistance*100);
        mDistance = mDistance/100;
        String value = String.valueOf(mDistance);
        if (mDistance == 0.0) {

            tvDistanceValue.setText("-");
        } else {

            tvDistanceValue.setText(value);
        }
    }

    /**
     * method to calculate burned calories out of distance
     *
     * @return burned calories in kcal
     */

    private double getCalories(double distance) {

        double calories;
        calories = mWeight * 0.75 * distance / 1000.0;
        return calories;
    }

    /**
     * method to calculate and display burned calories
     */

    private void showCalories(double distance) {

        mCalories = getCalories(distance);
        mCalories = Math.round(mCalories*100);
        mCalories = mCalories/100;
        String value = String.valueOf(mCalories);
        if (mCalories == 0.0) {

            tvCaloriesValue.setText("-");
        } else {

            tvCaloriesValue.setText(value);
        }
    }

    /**
     * method to initialise database for storing map and route values
     *
     * @see DataSourceMapData
     * @see DataSourceRouteData
     *
     */
    private void initializeDatabase() {

        // Test writing in Map database
        dataSourceMapData = new DataSourceMapData(MapActivity.this);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceMapData.open();

        // Test writing in Route database
        dataSourceRouteData = new DataSourceRouteData(this);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceRouteData.open();
        dataSourceRouteData.createTable();
    }

    /**
     * method to insert route and location points into database
     *
     * @param routeName name of route that should be stored
     *
     * @see Location
     * @see DataSourceMapData
     * @see DataSourceRouteData
     */
    private void writeInDatabases(String routeName) {

        if (mRoute.size() > 1) {

            // Map data
            dataSourceMapData.createTable(routeName);

            for (Location locationPoint : mRoute) {

                dataSourceMapData.createMapsData(routeName,
                locationPoint.getLongitude(), locationPoint.getLatitude(),
                locationPoint.getAltitude(), 100.0);
            }

            dataSourceMapData.getAllMapData(routeName);

            // Route data
            dataSourceRouteData.createRouteData(routeName, mSteps,
            chronometer.getText().toString(), mCalories, mDistance);
            dataSourceRouteData.getAllRouteData();
        }
    }

    /**
     * method to check of route name is valid
     *
     * @param routeName name of route that should be checked
     *
     * @return true if route name is valid, return false otherwise
     *
     */
    private boolean validateRouteName(String routeName) {

        if (routeName.isEmpty()) {
            return false;
        } else return !consistsOfBlanks(routeName);
    }

    /**
     * method to check of route name only contains blanks
     *
     * @param routeName name of route that should be checked
     * @return true if name only contains blanks return false otherwise
     */
    private boolean consistsOfBlanks(String routeName) {

        char[] charArray = routeName.toCharArray();
        int countBlanks = 0;

        for (char charItem : charArray) {

            if (charItem == ' ') {
                countBlanks++;
            }
        }

        return countBlanks == charArray.length;
    }

    /**
     * method to filter specific characters out of route name.
     * only valid characters: a-z, A-Z, 0-9
     *
     * @param routeName route name that should be checked.
     * @return route name containing only valid characters.
     */
    private String formatRouteName(String routeName) {
        routeName = routeName.trim();
        routeName = routeName.replaceAll("[^a-zA-Z0-9]", "");
        return routeName;
    }

    /**
     * method to check if route name exists in route list
     *
     * @param routeName route name that should be checked
     * @return true of route name is existing in route list, return false
     * otherwise.
     *
     * @see RouteData
     * @see DataSourceRouteData
     */
    private boolean checkIfRouteNameExists(String routeName) {

        List<RouteData> routeDataList = dataSourceRouteData.getAllRouteData();
        ArrayList<String> routeNames = new ArrayList<>();

        for (RouteData routeData : routeDataList) {

            routeNames.add(routeData.getRoute());
        }

        return routeNames.contains(routeName);
    }

}