package de.goforittechnologies.go_for_it;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    private static final String TAG = "MapsActivity";

    // Widgets
    private MapView mapView;
    private Button btnStartLocation;
    private Button btnStopLocation;
    private Chronometer chronometer;

    private RouteHandler routeHandler;
    private long elapsedTime;


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

        routeHandler = new RouteHandler();
        LocationRouteService.updateRouteHandler = routeHandler;

        elapsedTime = 0;

        // Check permission for location and storage
        if (Build.VERSION.SDK_INT >= 23) {

            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }

        }

        mapView = findViewById(R.id.mvMap);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        btnStartLocation = findViewById(R.id.btn_start_location);
        btnStopLocation = findViewById(R.id.btn_stop_location);
        chronometer = findViewById(R.id.chronometer);

        btnStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startService(new Intent(MapsActivity.this, LocationRouteService.class));
                LocationRouteService.updateRouteHandler = routeHandler;

                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();

                btnStartLocation.setEnabled(false);
                btnStopLocation.setEnabled(true);

            }
        });

        btnStopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopService(new Intent(MapsActivity.this, LocationRouteService.class));

                chronometer.stop();

                btnStartLocation.setEnabled(true);
                btnStopLocation.setEnabled(false);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        showRoute();
        LocationRouteService.updateRouteHandler = routeHandler;

    }

    private void showRoute() {

        List<Location> route = LocationRouteService.route;
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

    private class RouteHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            showRoute();

        }
    }

}

