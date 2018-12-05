package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceRouteData;
import de.goforittechnologies.go_for_it.storage.RouteData;

public class RoutesActivity extends AppCompatActivity {

    private static final String TAG = "RoutesActivity";

    // Widgets
    private ListView lvRoutes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        lvRoutes = findViewById(R.id.lvRoutes);

        showAllListEntries();

    }

    private void showAllListEntries() {

        DataSourceRouteData dataSourceRouteData = new DataSourceRouteData(this, "Routes", 1);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet!");
        dataSourceRouteData.open();

        List<RouteData> routeDataList = dataSourceRouteData.getAllRouteData();

        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen!");
        dataSourceRouteData.close();

        ArrayAdapter<RouteData> routeDataArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, routeDataList);
        lvRoutes.setAdapter(routeDataArrayAdapter);

    }

}
