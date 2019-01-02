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
import de.goforittechnologies.go_for_it.storage.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private List<User> mUserList = new ArrayList<>();

    public UsersAdapter(Context context, List<User> userList) {
        super(context, 0, userList);

        mContext = context;
        mUserList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.all_users_list_item, parent, false);
        }

        User currentUser = mUserList.get(position);

        CircleImageView ivUser = listItem.findViewById(R.id.ivUser);
        Glide.with(mContext).load(currentUser.getImage()).into(ivUser);

        TextView tvUserName = listItem.findViewById(R.id.tvUser);
        tvUserName.setText(currentUser.getName());

        return listItem;
    }
}
