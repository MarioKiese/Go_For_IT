package de.goforittechnologies.go_for_it.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.Request;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see ArrayAdapter
 *
 * This adapter is used to convert list entries into dynamic list views with
 * a specific layout.
 *
 * @see Request
 * @see CircleImageView
 *
 */
public class RequestsAdapter extends ArrayAdapter<Request> {



    private Context mContext;
    private List<Request> requestsList = new ArrayList<>();
    /**
     *  constructor to connect context and list with member variables
     * @param context
     * @param list
     */
    public RequestsAdapter(@NonNull Context context, List<Request> list) {
        super(context, 0, list);
        mContext = context;
        requestsList = list;
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
     * @see Request
     */
    @NonNull
    @Override
    public View getView(int position,
    @Nullable View convertView,
    @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
            .inflate(R.layout.request_list_item, parent, false);
        }

        Request currentRequest = requestsList.get(position);

        CircleImageView ivSourceUser
        = listItem.findViewById(R.id.ivSourceUser);
        Glide.with(mContext).load(currentRequest
        .getSourceUserImage()).into(ivSourceUser);

        TextView tvUserName =
        listItem.findViewById(R.id.tvUserName);
        tvUserName.setText(currentRequest.getSourceUserName());

        TextView tvRequestStepsValue =
        listItem.findViewById(R.id.tvRequestStepsValue);
        tvRequestStepsValue
        .setText(String.valueOf(currentRequest.getStepTarget()));

        return listItem;
    }
}
