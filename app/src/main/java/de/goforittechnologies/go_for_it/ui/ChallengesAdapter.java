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
import de.goforittechnologies.go_for_it.storage.Challenge;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChallengesAdapter extends ArrayAdapter<Challenge> {

    private Context mContext;
    private List<Challenge> challengesList = new ArrayList<>();

    public ChallengesAdapter(@NonNull Context context, List<Challenge> list) {
        super(context, 0, list);
        mContext = context;
        challengesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
                    .inflate(R.layout.challenge_list_item, parent,
                            false);
        }

        Challenge currentChallenge = challengesList.get(position);

        CircleImageView ivSourceUser = listItem.findViewById(R.id
                .ivSourceUserChallenge);
        Glide.with(mContext).load(currentChallenge.getUser1().getImage())
                .into(ivSourceUser);

        TextView tvUserName = listItem.findViewById(R.id
                .tvSourceUserNameChallenge);
        tvUserName.setText(currentChallenge.getUser1().getName());

        TextView tvRequestStepsValue = listItem.findViewById(R.id
                .tvChallengeStepsValue);
        tvRequestStepsValue.setText(String.valueOf(currentChallenge
                .getStepTarget()));

        return listItem;
    }
}
