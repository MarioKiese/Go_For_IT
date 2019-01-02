package de.goforittechnologies.go_for_it.ui;

import android.content.Context;
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
import de.goforittechnologies.go_for_it.storage.Request;

public class RequestsAdapter extends ArrayAdapter<Request> {

    private Context mContext;
    private List<Request> requestsList = new ArrayList<>();

    public RequestsAdapter(@NonNull Context context, List<Request> list) {
        super(context, 0, list);
        mContext = context;
        requestsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.request_list_item, parent, false);
        }

        Request currentRequest = requestsList.get(position);

        TextView tvUserName = listItem.findViewById(R.id.tvUserName);
        tvUserName.setText(currentRequest.getTargetUserID());

        TextView tvRequestStepsValue = listItem.findViewById(R.id.tvRequestStepsValue);
        tvRequestStepsValue.setText(String.valueOf(currentRequest.getStepTarget()));

        return listItem;
    }
}
