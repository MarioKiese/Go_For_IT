package de.goforittechnologies.go_for_it.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import de.goforittechnologies.go_for_it.R;

public class ChallengesOverviewActivity extends AppCompatActivity {

    private static final String TAG = "ChallengesOverviewActivity";

    // Widgets
    private Toolbar tbChallenges;
    private TextView tvRequestsListEmptyText;
    private TextView tvActiveChallengesListEmptyText;
    private ListView lvRequests;
    private ListView lvActiveChallenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges_overview);

        // Initialize widgets
        tbChallenges = findViewById(R.id.tbChallenges);
        setSupportActionBar(tbChallenges);
        getSupportActionBar().setTitle("Challenges");

        tvRequestsListEmptyText = findViewById(R.id.tvRequestsEmtpyListText);
        lvRequests = findViewById(R.id.lvRequests);
        lvRequests.setEmptyView(tvRequestsListEmptyText);
        tvActiveChallengesListEmptyText = findViewById(R.id.tvActiveChallengesEmtpyListText);
        lvActiveChallenges = findViewById(R.id.lvActiveChallenges);
        lvActiveChallenges.setEmptyView(tvActiveChallengesListEmptyText);


    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.challenges_overview_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_new_challenge_btn:

                Intent allUsersIntent = new Intent(ChallengesOverviewActivity.this, AllUsersActivity.class);
                startActivity(allUsersIntent);
                return true;

            default:

                return false;

        }

    }
}
