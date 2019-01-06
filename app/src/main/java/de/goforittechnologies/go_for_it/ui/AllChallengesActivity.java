package de.goforittechnologies.go_for_it.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.goforittechnologies.go_for_it.R;

public class AllChallengesActivity extends AppCompatActivity {

    // Widgets
    private Toolbar tbAllChallenges;
    private ProgressBar pbAllChallenges;
    private ListView lvAllChallenges;
    private TextView tvAllChallengesListEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_challenges);

        // Set widgets
        tbAllChallenges = findViewById(R.id.tbAllChallenges);
        setSupportActionBar(tbAllChallenges);
        tvAllChallengesListEmptyText = findViewById(R.id
                .tvAllChallengesEmptyListText);
        lvAllChallenges = findViewById(R.id.lvAllChallenges);
        lvAllChallenges.setEmptyView(tvAllChallengesListEmptyText);
        pbAllChallenges = findViewById(R.id.pbAllChallenges);
        pbAllChallenges.setVisibility(View.VISIBLE);


    }
}
