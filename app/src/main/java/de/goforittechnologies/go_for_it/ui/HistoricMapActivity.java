package de.goforittechnologies.go_for_it.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;

/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 * @see AppCompatActivity
 *
 *
 * This class shows a selected route out of the RoutesListActivity
 * @see RoutesListActivity
 *
 * Corresponding layout: res.layout.activity_historic_map
 *
 * The user can navigate through the openstreetmap by swiping and zooming
 * with two fingers.
 * alternative: zooming with two buttons in the bottom (only visible when
 * navigate over map).
 * @see MapView
 *
 */

public class HistoricMapActivity extends AppCompatActivity {

    // Widgets
    private MapView mapView;

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - initialising map data for viewing
     *
     * @see MapView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_map);

        // Configure map
        mapView = findViewById(R.id.mvHistoricMap);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        Intent intent = getIntent();

        Bundle bundle = intent.getBundleExtra("Map");
        ArrayList<Location> data = bundle.getParcelableArrayList("Map");

        if (data != null) {

            showRoute(data);

        }

    }

    /**
     *
     * method to display route in activity
     *
     * @param route route that should be shown
     *
     * @see GeoPoint
     * @see Polyline
     * @see MapView
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
}
