package de.goforittechnologies.go_for_it.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.goforittechnologies.go_for_it.R;

public class ChallengeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeDetailActivity";

    // Widgets
    private TextView tvStepsYou;
    private TextView tvStepsRival;
    private TextView tvStepTarget;
    private ProgressBar pbYou;
    private ProgressBar pbRival;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Set widgets
        tvStepsYou = findViewById(R.id.tvCurrentStepsYou);
        tvStepsRival = findViewById(R.id.tvCurrentStepsRival);
        tvStepTarget = findViewById(R.id.tvStepTarget);
        pbYou = findViewById(R.id.pbYou);
        pbRival = findViewById(R.id.pbRival);

        // Get intent extras
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int stepsYou = bundle.getInt("stepsYou");
        int stepsRival = bundle.getInt("stepsRival");
        int stepTarget = bundle.getInt("stepTarget");

        // Fill widgets with data
        tvStepsYou.setText(String.valueOf(stepsYou));
        tvStepsRival.setText(String.valueOf(stepsRival));
        tvStepTarget.setText(String.valueOf(stepTarget));
        pbYou.setMax(stepTarget);
        pbYou.setProgress(stepsYou);
        pbRival.setMax(stepTarget);
        pbRival.setProgress(stepsRival);
    }
}
