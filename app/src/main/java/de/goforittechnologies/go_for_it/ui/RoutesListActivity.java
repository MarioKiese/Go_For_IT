package de.goforittechnologies.go_for_it.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceMapData;
import de.goforittechnologies.go_for_it.storage.DataSourceRouteData;
import de.goforittechnologies.go_for_it.storage.MapData;
import de.goforittechnologies.go_for_it.storage.RouteData;

/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class is used to list all saved routes
 *
 * Corresponding layout: res.layout.activity_routes_list.xml
 *
 * The user can select the saved routes to see the details
 *
 * The store the route information:
 * @see DataSourceRouteData
 * @see DataSourceMapData
 */

public class RoutesListActivity extends AppCompatActivity {

    private static final String TAG = "RoutesListActivity";

    private ListView lvRoutes;
    private Toolbar tbRoutes;

    // Database
    private DataSourceMapData dataSourceMapData;
    private DataSourceRouteData dataSourceRouteData;
    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - set click listeners
     *
     * @see DialogInterface
     * @see AdapterView
     * @see AlertDialog
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        // Set widgets
        tbRoutes = findViewById(R.id.tbRoutes);
        setSupportActionBar(tbRoutes);
        // Widgets
        TextView tvRoutesEmptyListText = findViewById(R.id.tvRoutesEmptyListText);
        lvRoutes = findViewById(R.id.lvRoutes);
        lvRoutes.setEmptyView(tvRoutesEmptyListText);

        // Initialize database
        initializeDatabase();

        showAllListEntries();

        lvRoutes.setOnItemClickListener((adapterView, view, position, id) -> {

            String routeName = ((RouteData)adapterView
            .getItemAtPosition(position)).getRoute();
            Log.d(TAG, "onItemClick: RouteName: " + routeName);
            ArrayList<Location> route =
            convertRouteToLocationList(routeName);
            sendRouteFromDatabaseToActivity(route);

        });

        lvRoutes.setOnItemLongClickListener(
        new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(
            AdapterView<?> adapterView, View view, int position, long id) {

                AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(RoutesListActivity.this);
                dialogBuilder.setTitle("Delete?")
                        .setMessage("Are you sure you want to delete " +
                        adapterView.getItemAtPosition(position).toString()+"?")
                        .setCancelable(true)
                        .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog,
                            int which) {

                                dialog.cancel();

                            }

                        })
                        .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                int which) {

                                RouteData routeData =
                                (RouteData) lvRoutes
                                .getItemAtPosition(position);
                                deleteListEntry(routeData);

                                showAllListEntries();

                            }
                        });

                AlertDialog alertDialog = dialogBuilder.create();

                alertDialog.show();

                return true;

            }
        });

    }

    /**
     * method to close database connections when destroying the activity
     *
     * @see DataSourceMapData
     * @see DataSourceRouteData
     */
    @Override
    protected void onDestroy() {

        // Close databases
        dataSourceMapData.close();
        Log.d(TAG, "onCreate: Die Datenquelle " +
        dataSourceMapData.getClass().getName() + " wird geschlossen!");

        Log.d(TAG, "onCreate: Die Datenquelle " +
        dataSourceRouteData.getClass().getName() + " wird geschlossen!");
        dataSourceRouteData.close();

        super.onDestroy();
    }

    /**
     * method to initialize database (connect to data source)
     *
     * @see DataSourceRouteData
     * @see DataSourceMapData
     */
    private void initializeDatabase() {

        // Test writing in Map database
        dataSourceMapData =
        new DataSourceMapData(RoutesListActivity.this);
        Log.d(TAG, "onCreate: Die Datenquelle " +
        dataSourceMapData.getClass().getName() + " wird geöffnet!");
        dataSourceMapData.open();

        // Test writing in Route database
        dataSourceRouteData =
        new DataSourceRouteData(RoutesListActivity.this);
        Log.d(TAG, "onCreate: Die Datenquelle " +
        dataSourceRouteData.getClass().getName() + " wird geöffnet!");
        dataSourceRouteData.open();

    }

    /**
     * method to show all entries of route list
     *
     * @see RouteData
     * @see RoutesAdapter
     */
    private void showAllListEntries() {

        List<RouteData> routeDataList = dataSourceRouteData.getAllRouteData();
        RoutesAdapter mRouteDataAdapter = new RoutesAdapter(RoutesListActivity.this,
                routeDataList);
        lvRoutes.setAdapter(mRouteDataAdapter);

    }

    /**
     * method to delete a selected route and the corresponding map out of
     * route list.
     *
     * @param routeData route data that should be deleted
     *
     * @see DataSourceMapData
     * @see DataSourceRouteData
     */
    private void deleteListEntry(RouteData routeData) {

        dataSourceRouteData.deleteRouteData(routeData);
        dataSourceMapData.deleteTable(routeData.getRoute());

    }

    /**
     * method to convert route data to location list
     *
     * @param routeName name of route that should be converted
     * @return array list out of locations
     *
     * @see Location
     * @see MapData
     */
    private ArrayList<Location> convertRouteToLocationList(String routeName) {

        ArrayList<Location> routeLocationParcel = new ArrayList<>();

        List<MapData> mapDataListFromDatabase = getRouteFromDatabase(routeName);

        for (MapData mapData : mapDataListFromDatabase) {

            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(mapData.getLatitude());
            location.setLongitude(mapData.getLongitude());
            location.setAltitude(mapData.getAltitude());

            routeLocationParcel.add(location);

        }

        return routeLocationParcel;

    }

    /**
     * method to select all map point from selected route out of database table.
     *
     * @param routeName name of route the map points should be selected.
     *
     * @return list out of map point from selected route.
     *
     * @see MapData
     * @see DataSourceMapData
     */
    private List<MapData> getRouteFromDatabase(String routeName) {

        List<MapData> route;

        route = dataSourceMapData.getAllMapData(routeName);

        return route;

    }

    /**
     * method to send routes from database to activity
     *
     * @param route list out of locations that should be send to activity
     *
     * @see Bundle
     * @see HistoricMapActivity
     * @see RoutesListActivity
     */

    private void sendRouteFromDatabaseToActivity(ArrayList<Location> route) {

        Intent historicMapIntent =
        new Intent(RoutesListActivity.this,
                HistoricMapActivity.class);
        Bundle bundle =
        new Bundle();
        bundle.putParcelableArrayList("Map", route);
        historicMapIntent.putExtra("Map", bundle);
        startActivity(historicMapIntent);

    }

}
