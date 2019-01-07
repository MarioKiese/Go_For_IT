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

import java.util.List;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.Challenge;
import de.hdodenhof.circleimageview.CircleImageView;
/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see ArrayAdapter
 *
 * This adapter is used to convert list entries into dynamic list views with
 * a specific layout.
 *
 * @see Challenge
 * @see CircleImageView
 *
 */
class AllChallengesAdapter extends ArrayAdapter<Challenge> {

    private Context mContext;
    private List<Challenge> challengesList;
    /**
     *  constructor to connect context and list with member variables
     * @param context
     * @param list
     */
    public AllChallengesAdapter(@NonNull Context context,
                                List<Challenge> list) {
        super(context, 0, list);
        mContext = context;
        challengesList = list;
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
     * @see Challenge
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull
            ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
                    .inflate(R.layout.all_challenges_list_item,
                    parent, false);
        }

        Challenge currentChallenge = challengesList.get(position);

        CircleImageView ivUser1 = listItem
                .findViewById(R.id.ivUser1Challenge);
        Glide.with(mContext).load(currentChallenge.getUser1().getImage())
                .into(ivUser1);

        CircleImageView ivUser2 = listItem
                .findViewById(R.id.ivUser2Challenge);
        Glide.with(mContext).load(currentChallenge.getUser2().getImage())
                .into(ivUser2);

        TextView tvUser1Name = listItem.findViewById(R.id
                .tvUser1NameChallenge);
        tvUser1Name.setText(currentChallenge.getUser1().getName());

        TextView tvUser2Name = listItem.findViewById(R.id
                .tvUser2NameChallenge);
        tvUser2Name.setText(currentChallenge.getUser2().getName());

        TextView tvStepTargetValue = listItem.findViewById(R.id
                .tvChallengeStepTargetValue);
        tvStepTargetValue.setText(String.valueOf(currentChallenge
                .getStepTarget()));

        TextView tvWinner = listItem.findViewById(R.id
                .tvChallengeWinner);
        tvWinner.setText(String.valueOf(currentChallenge
                .getWinner().getName()));

        return listItem;
    }
}
