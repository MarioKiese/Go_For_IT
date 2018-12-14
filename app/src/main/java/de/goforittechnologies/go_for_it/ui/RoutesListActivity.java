package de.goforittechnologies.go_for_it.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceMapData;
import de.goforittechnologies.go_for_it.storage.DataSourceRouteData;
import de.goforittechnologies.go_for_it.storage.MapData;
import de.goforittechnologies.go_for_it.storage.RouteData;

public class RoutesListActivity extends AppCompatActivity {

    private static final String TAG = "RoutesListActivity";

    // Widgets
    private ListView lvRoutes;

    // Database
    DataSourceMapData dataSourceMapData;
    DataSourceRouteData dataSourceRouteData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        lvRoutes = findViewById(R.id.lvRoutes);

        // Initialize database
        initializeDatabase();

        showAllListEntries();

        lvRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                String routeName = ((RouteData)adapterView.getItemAtPosition(position)).getRoute();
                Log.d(TAG, "onItemClick: RouteName: " + routeName);
                ArrayList<Location> route = convertRouteToLocationList(routeName);
                sendRouteFromDatabaseToActivity(route);

            }
        });

        lvRoutes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RoutesListActivity.this);
                dialogBuilder.setTitle("Delete?")
                        .setMessage("Are you sure you want to delete " + adapterView.getItemAtPosition(position).toString()+"?")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();

                            }

                        })
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                RouteData routeData = (RouteData) lvRoutes.getItemAtPosition(position);
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

    @Override
    protected void onDestroy() {

        // Close databases
        dataSourceMapData.close();
        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");

        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");
        dataSourceRouteData.close();

        super.onDestroy();
    }

    private void initializeDatabase() {

        // Test writing in Map database
        dataSourceMapData = new DataSourceMapData(RoutesListActivity.this);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceMapData.open();

        // Test writing in Route database
        dataSourceRouteData = new DataSourceRouteData(RoutesListActivity.this);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceRouteData.open();

    }

    private void showAllListEntries() {

        List<RouteData> routeDataList = dataSourceRouteData.getAllRouteData();

        ArrayAdapter<RouteData> routeDataArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, routeDataList);
        lvRoutes.setAdapter(routeDataArrayAdapter);

    }

    private void deleteListEntry(RouteData routeData) {

        dataSourceRouteData.deleteRouteData(routeData);
        dataSourceMapData.deleteTable(routeData.getRoute());

    }

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

    private List<MapData> getRouteFromDatabase(String routeName) {

        List<MapData> route;

        route = dataSourceMapData.getAllMapData(routeName);

        return route;

    }

    // Communication methods
    private void sendRouteFromDatabaseToActivity(ArrayList<Location> route) {

        Intent historicMapIntent = new Intent(RoutesListActivity.this, HistoricMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("Map", route);
        historicMapIntent.putExtra("Map", bundle);
        startActivity(historicMapIntent);

    }

}
