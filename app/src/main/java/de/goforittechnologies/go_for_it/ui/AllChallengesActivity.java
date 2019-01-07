package de.goforittechnologies.go_for_it.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.Challenge;
/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class shows all finished challenges
 * Corresponding layout: res.layout.activity_all_challenges.xml.
 *
 * The user can inform himself about the finished challenges.
 * In one list, he can see the two opponents, the step-goal value and the
 * winner of the challenge
 */
public class AllChallengesActivity extends AppCompatActivity {

    private static final String TAG = "AllChallengesActivity";

    // Widgets
    private Toolbar tbAllChallenges;
    private ProgressBar pbAllChallenges;
    private ListView lvAllChallenges;
    private TextView tvAllChallengesListEmptyText;

    // Member variables
    private List<Challenge> allChallengesList;
    private AllChallengesAdapter allChallengesAdapter;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String userID;
    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - creating intent
     */
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
        allChallengesList = new ArrayList<>();
        allChallengesAdapter = new AllChallengesAdapter
                (AllChallengesActivity.this,
                allChallengesList);
        lvAllChallenges.setAdapter(allChallengesAdapter);
        pbAllChallenges = findViewById(R.id.pbAllChallenges);
        pbAllChallenges.setVisibility(View.VISIBLE);

        // Configure Firebase
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Users").document(userID)
                .collection
                ("Challenges")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if (e!=null){

                    Log.d(TAG,"Error : " + e.getMessage());
                    pbAllChallenges.setVisibility(View.INVISIBLE);
                } else {

                    for (DocumentChange doc :
                            queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String challengeID =
                            (String)doc.getDocument().get("challengeId");
                            Log.d(TAG,
                            "onEvent: Challenge ID found: " + challengeID);

                            if (challengeID != null) {

                                firebaseFirestore
                                .collection("Challenges")
                                .document(challengeID)
                                .addSnapshotListener(
                                    new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(
                                    @Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {

                                        if (e != null) {

                                            Log.d(TAG,"Error : "
                                            + e.getMessage());
                                        } else {

                                            if (documentSnapshot != null
                                            && documentSnapshot.exists()) {

                                                Log.d(TAG,
                                                "Current data: " +
                                                documentSnapshot.getData());
                                                Challenge challenge =
                                                documentSnapshot
                                                .toObject(Challenge.class);
                                                Log.d(TAG,
                                                "onComplete: " +
                                                "Firestore data " +
                                                "converted to object");

                                                if (challenge.getStatus()
                                                        .equals("finished")) {

                                                    Log.d(TAG,
                                                        "onComplete: " +
                                                            "Challenge is " +
                                                            "finished");

                                                    allChallengesList
                                                            .add(challenge);
                                                    allChallengesAdapter
                                                    .notifyDataSetChanged();
                                                }
                                            } else {
                                                Log.d(TAG,
                                                    "Current data: null");
                                            }
                                        }
                                    }
                                });
                            } else {

                                Log.d(TAG,
                                "onEvent: Challenge ID is null");
                            }
                        }
                    }
                    pbAllChallenges.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
