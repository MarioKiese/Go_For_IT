package de.goforittechnologies.go_for_it.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.goforittechnologies.go_for_it.R;

/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class shows the current standing of the selected challenge.
 * Corresponding layout: res.layout.activity_challenge_detail.xml.
 *
 * The user can inform himself about the current steps made by himself since
 *  * stating the challenge underneath the "you" textView (same for opponent's
 *  * steps underneath the "rival" textView).
 *  *
 *  * In the center of this activity, the step-goal to archive is displayed.
 *  *
 *  * At the bottom under every step-value the user can see a progress bar with
 *  * his current progress to the stepgoal.
 */

public class ChallengeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeDetailActivity";

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - creating intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Set widgets
        // Widgets
        TextView tvStepsYou = findViewById(R.id.tvCurrentStepsYou);
        TextView tvStepsRival = findViewById(R.id.tvCurrentStepsRival);
        TextView tvStepTarget = findViewById(R.id.tvStepTarget);
        ProgressBar pbYou = findViewById(R.id.pbYou);
        ProgressBar pbRival = findViewById(R.id.pbRival);

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
