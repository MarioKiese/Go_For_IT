package de.goforittechnologies.go_for_it.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.RouteData;

/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see ArrayAdapter
 *
 * This adapter is used to convert list entries into dynamic list views with
 * a specific layout.
 *
 * @see RouteData
 *
 *
 */

public class RoutesAdapter extends ArrayAdapter<RouteData> {

    private Context mContext;
    private List<RouteData> routesList = new ArrayList<>();
    /**
     *  constructor to connect context and list with member variables
     * @param context
     * @param list
     */
    public RoutesAdapter(Context context, List<RouteData> list) {
        super(context, 0, list);
        mContext = context;
        routesList = list;
    }
    /**
     * method to fill view elements with data content from challenge objects
     *
     * @param position list position
     * @param convertView View
     * @param parent viewGroup
     *
     * @return view to display
     *
     * @see RouteData
     */
    @NonNull
    @Override
    public View getView(int position,
    @Nullable View convertView,
    @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
            .inflate(R.layout.route_list_item, parent, false);
        }

        RouteData currentRouteData = routesList.get(position);

        TextView tvRouteName = listItem.findViewById(R.id.tvRouteName);
        tvRouteName.setText(currentRouteData.getRoute());

        TextView tvRouteStepsValue =
        listItem.findViewById(R.id.tvRouteStepsValue);
        tvRouteStepsValue.setText(String.valueOf(currentRouteData.getSteps()));

        TextView tvRouteTimeValue =
        listItem.findViewById(R.id.tvRouteTimeValue);
        tvRouteTimeValue.setText(currentRouteData.getTime());

        TextView tvRouteDistanceValue =
        listItem.findViewById(R.id.tvRouteDistanceValue);
        tvRouteDistanceValue
        .setText(String.valueOf(currentRouteData.getKilometers()));

        TextView tvRouteCaloriesValue =
        listItem.findViewById(R.id.tvRouteCaloriesValue);
        tvRouteCaloriesValue
        .setText(String.valueOf(currentRouteData.getCalories()));

        return listItem;
    }
}
