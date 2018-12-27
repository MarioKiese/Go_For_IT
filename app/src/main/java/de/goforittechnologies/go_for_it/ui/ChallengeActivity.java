package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import de.goforittechnologies.go_for_it.R;

public class ChallengeActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeActivity";

    // Widgets
    private Toolbar tbChallenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        // Initialize widgets
        tbChallenges = findViewById(R.id.tbChallenges);
        setSupportActionBar(tbChallenges);
        getSupportActionBar().setTitle("Challenges");

    }
}
