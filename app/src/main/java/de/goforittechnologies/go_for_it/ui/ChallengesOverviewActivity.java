package de.goforittechnologies.go_for_it.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.logic.services.ChallengeStepCounterService;
import de.goforittechnologies.go_for_it.storage.Challenge;
import de.goforittechnologies.go_for_it.storage.Request;
import de.goforittechnologies.go_for_it.storage.User;

/**
 * @author  Mario Kiese.
 * @version 0.8.
 * @see AppCompatActivity
 *
 *
 * This class shows all requested and active challenges.
 * Corresponding layout: res.layout.activity_challenges_overview.xml.
 *
 * The user can see all requested but not yet accepted challenges under
 * "request".
 * The user can see all accepted and not yet completed challenges under
 * "active challenges".
 *
 * By klicking the menu button in the top right corner,
 * the user is able to create new challenges.
 * @see AllUsersActivity
 */

public class ChallengesOverviewActivity extends AppCompatActivity {

    private static final String TAG = "ChallengesOverviewActy";

    // Widgets
    private Toolbar tbChallenges;
    private TextView tvRequestsListEmptyText;
    private TextView tvActiveChallengesListEmptyText;
    private ListView lvRequests;
    private ListView lvActiveChallenges;
    private ProgressBar pbRequests;
    private ProgressBar pbChallenges;

    // Member variables
    private List<Request> requestsList;
    private RequestsAdapter requestsAdapter;
    private List<Challenge> challengesList;
    private ChallengesAdapter challengesAdapter;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String userID;

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - configure firebase-usage
     * - set event listeners
     * - set click listeners
     *
     * @see EventListener
     * @see FirebaseAuth
     * @see FirebaseUser
     * @see AdapterView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges_overview);

        // Set widgets
        tbChallenges = findViewById(R.id.tbChallenges);
        setSupportActionBar(tbChallenges);
        tvRequestsListEmptyText = findViewById(R.id.tvRequestsEmtpyListText);
        lvRequests = findViewById(R.id.lvRequests);
        lvRequests.setEmptyView(tvRequestsListEmptyText);
        tvActiveChallengesListEmptyText = findViewById(
                R.id.tvActiveChallengesEmtpyListText);
        lvActiveChallenges = findViewById(
                R.id.lvActiveChallenges);
        lvActiveChallenges.setEmptyView(
                tvActiveChallengesListEmptyText);
        pbRequests = findViewById(R.id.pbRequests);
        pbRequests.setVisibility(View.VISIBLE);
        pbChallenges = findViewById(R.id.pbChallenges);
        pbChallenges.setVisibility(View.VISIBLE);

        // Set member variables
        requestsList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(
                ChallengesOverviewActivity.this, requestsList);
        lvRequests.setAdapter(requestsAdapter);
        challengesList = new ArrayList<>();
        challengesAdapter = new ChallengesAdapter(
        ChallengesOverviewActivity.this, challengesList);
        lvActiveChallenges.setAdapter(challengesAdapter);

        // Configure Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        userID = currentUser.getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Populate request ListView with firebase data
        fillRequestListViewWithFirebaseData();

        // Populate challenge ListView with firebase data
        fillChallengeListViewWithFirebaseData();

        lvRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long l) {

                AlertDialog.Builder dialogBuilder = new AlertDialog
                .Builder(ChallengesOverviewActivity.this);
                dialogBuilder.setTitle("Challenge request!");

                // Set up the buttons
                dialogBuilder.setPositiveButton("Accept",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Request request =
                        (Request)adapterView.getItemAtPosition(position);
                        // Source user data
                        String sourceUserID = request.getSourceUserID();
                        String sourceUserName = request.getSourceUserName();
                        String sourceUserImage = request.getSourceUserImage();
                        User sourceUser = new User(sourceUserID,
                        sourceUserName, sourceUserImage);
                        // Target user data
                        String targetUserID = request.getTargetUserID();
                        String targetUserName = request.getTargetUserName();
                        String targetUserImage = request.getTargetUserImage();
                        User targetUser = new User(targetUserID, targetUserName,
                        targetUserImage);

                        String challengeID = "";
                        String requestID = request.getId();
                        int stepTarget = request.getStepTarget();
                        String status = "running";

                        Challenge challenge = new Challenge(challengeID,
                        requestID, stepTarget, sourceUser, targetUser, status);

                        manageChallenge(challenge);
                    }
                });

                dialogBuilder.setNegativeButton("Decline",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Request request =
                        (Request)adapterView.getItemAtPosition(position);
                        updateRequestStatus(request.getId(),
                        "declined");

                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });

        lvActiveChallenges
        .setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
            View view, int position, long l) {

                Challenge currentChallenge =
                (Challenge)adapterView.getItemAtPosition(position);
                int stepsYou = userID.equals(currentChallenge.getUser1()
                .getId()) ? currentChallenge.getStepsUser1() :
                currentChallenge.getStepsUser2();
                int stepsRival = userID.equals(currentChallenge
                .getUser1().getId()) ? currentChallenge.getStepsUser2()
                : currentChallenge.getStepsUser1();
                int stepTarget = currentChallenge.getStepTarget();

                Intent challengeDetailIntent =
                new Intent(ChallengesOverviewActivity.this,
                ChallengeDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("stepsYou", stepsYou);
                bundle.putInt("stepsRival", stepsRival);
                bundle.putInt("stepTarget", stepTarget);
                challengeDetailIntent.putExtras(bundle);
                startActivity(challengeDetailIntent);
            }
        });
    }

    /**
     * method to implement menu in this activity.
     * info: "@SuppressLint("RestrictedApi")" because of usage of icons
     * inside the menu.
     * @param menu menu to show inside the activity.
     *
     * @return true if method is called.
     *
     * @see MenuBuilder
     */
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

    /**
     * method to switch to selected activity based on selected item in menu.
     *
     * @param item item select in menu.
     *
     * @return true if specific item is select, return false otherwise.
     *
     * @see AllChallengesActivity
     * @see AllUsersActivity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_new_challenge_btn:

                Intent allUsersIntent = new Intent(
                ChallengesOverviewActivity.this,
                AllUsersActivity.class);
                startActivity(allUsersIntent);
                return true;

            case R.id.action_all_challenges_btn:

                Intent allChallengesIntent = new Intent(
                ChallengesOverviewActivity.this,
                AllChallengesActivity.class);
                startActivity(allChallengesIntent);
                return true;

            default:

                return false;

        }
    }

    /**
     * method to prepare and fill request list out of firestore database.
     */
    private void fillRequestListViewWithFirebaseData() {

        firebaseFirestore.collection("Users")
        .document(userID).collection("Requests")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if (e!=null){

                    Log.d(TAG,"Error : " + e.getMessage());
                    pbRequests.setVisibility(View.INVISIBLE);
                } else {

                    for (DocumentChange doc : queryDocumentSnapshots
                            .getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String requestID = (String)doc.getDocument()
                                    .get("requestId");
                            Log.d(TAG,
                                "onEvent: Request ID found: " +
                                requestID);
                            Toast.makeText(
                            ChallengesOverviewActivity.this,
                                "Request ID found: " + requestID,
                                    Toast.LENGTH_SHORT).show();

                            if (requestID != null) {

                                firebaseFirestore
                                .collection("Requests")
                                .document(requestID)
                                .addSnapshotListener(
                                new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(
                                    @Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {

                                        if (e != null) {

                                            Log.d(TAG,"Error : " +
                                            e.getMessage());
                                        } else {

                                            if (documentSnapshot !=
                                            null && documentSnapshot.exists()) {

                                                Log.d(TAG,
                                                "Current data: " +
                                                documentSnapshot.getData());
                                                Request request =
                                                documentSnapshot
                                                .toObject(Request.class);
                                                Log.d(TAG,
                                                "onComplete: Firestore " +
                                                "data converted to object");
                                                Log.d(TAG,
                                                "onComplete:" +
                                                " TargetUserID : " +
                                                request.getTargetUserID() +
                                                " UserID : " + userID);

                                                if (request.getTargetUserID()
                                                .equals(userID) && request
                                                .getStatus()
                                                .equals("pending")) {

                                                    Log.d(TAG,
                                                    "onComplete: " +
                                                    "Request is compatible" +
                                                    " with userID");

                                                    requestsList
                                                    .add(request);
                                                    requestsAdapter
                                                    .notifyDataSetChanged();
                                                }
                                                else if(request
                                                .getTargetUserID()
                                                .equals(userID) &&
                                                (request.getStatus()
                                                .equals("accepted")
                                                || request.getStatus()
                                                .equals("declined"))) {

                                                    Log.d(TAG,
                                                    "onEvent: Remove" +
                                                    " request from request" +
                                                    " list");

                                                    for (int i=0;
                                                    i<requestsList.size();
                                                    i++) {

                                                        if (requestsList
                                                        .get(i).getId()
                                                        .equals(request
                                                        .getId())) {

                                                        requestsList
                                                        .remove(i);
                                                        requestsAdapter
                                                        .notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG,
                                                "Current data: null");
                                            }
                                        }
                                    }
                                });
                            } else {

                                Log.d(TAG, "onEvent: Request ID is null");
                                Toast.makeText(
                                ChallengesOverviewActivity.this,
                                "Request ID is null", Toast.LENGTH_SHORT)
                                .show();
                            }
                        }
                    }
                    pbRequests.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     *  method to prepare and fill challenge list out of firestore database.
     */
    private void fillChallengeListViewWithFirebaseData() {

        firebaseFirestore.collection("Users")
        .document(userID).collection("Challenges")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if (e!=null){

                    Log.d(TAG,"Error : " + e.getMessage());
                    pbChallenges.setVisibility(View.INVISIBLE);
                } else {

                    for (DocumentChange doc : queryDocumentSnapshots
                    .getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String challengeID = (String)doc.getDocument()
                            .get("challengeId");
                            Log.d(TAG, "onEvent: Challenge ID found: " +
                            challengeID);
                            Toast.makeText(ChallengesOverviewActivity
                            .this, "Challenge ID found: " + challengeID,
                            Toast.LENGTH_SHORT).show();

                            if (challengeID != null) {

                                firebaseFirestore.collection(
                                "Challenges")
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

                                            if (documentSnapshot !=
                                            null && documentSnapshot.exists()) {

                                                Log.d(TAG,
                                                "Current data: " +
                                                documentSnapshot.getData());
                                                Challenge challenge =
                                                documentSnapshot
                                                .toObject(Challenge.class);
                                                Log.d(TAG,
                                                "onComplete: " +
                                                "Firestore data converted" +
                                                " to object");



                                                if (challenge.getStatus()
                                                    .equals("running")) {

                                                    Log.d(TAG,
                                                    "onComplete: " +
                                                    "Challenge is " +
                                                    "active/running");

                                                    if (!listContainsChallenge(
                                                    challengesList,
                                                    challengeID)) {

                                                        challengesList
                                                        .add(challenge);
                                                        challengesAdapter
                                                        .notifyDataSetChanged();
                                                    }
                                                }
                                                else if(challenge
                                                .getStatus()
                                                .equals("finished")) {

                                                    Log.d(TAG,
                                                    "onEvent: Remove " +
                                                    "challenge from " +
                                                    "challenge list");

                                                    for (int i=0;
                                                     i<challengesList.size();
                                                     i++) {

                                                        if (challengesList
                                                        .get(i)
                                                        .getId()
                                                        .equals(challenge
                                                        .getId())) {

                                                        challengesList
                                                        .remove(i);
                                                        challengesAdapter
                                                        .notifyDataSetChanged();
                                                        }
                                                    }
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
                                "onEvent: " +
                                "Challenge ID is null");
                                Toast.makeText(
                                ChallengesOverviewActivity.this,
                                "Challenge ID is null",
                                Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    pbChallenges.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     *
     * method to check if challenge-list contains challenge
     *
     * @param list list to search in
     * @param challengeID if from searched challenge
     *
     * @return true if challenge-list contains challege, false if not.
     *
     * @see Challenge
     */
    private boolean listContainsChallenge(List<Challenge> list,
                                          String challengeID) {

        boolean result = false;
        if (list.isEmpty()) {

            result = false;
        } else {

            for (Challenge challenge : list) {

                String currentID = challenge.getId();
                if (currentID.equals(challengeID)) {

                    result = true;
                } else {

                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * method to add challenge in firestore database, add challenge in users
     * challenge list, start challenge service.
     *
     * @param challenge challenge to be stored, be connected to users, get
     *                  challenge service steps
     */
    private void manageChallenge(Challenge challenge) {

        updateRequestStatus(challenge.getRequestID(),
        "accepted");
        String challengeID = addChallengeInFirestore(challenge);
        addChallengeToFirebaseUsers(challenge.getUser1().getId(),
        challenge.getUser2().getId(), challengeID);
        startChallengeService(userID);
    }

    /**
     * method to change status of request (values: accepted, declined, pending.
     * @param requestID id of request which status should be changed
     * @param newStatus new status for request
     */
    private void updateRequestStatus(String requestID, String newStatus) {

        firebaseFirestore.collection("Requests")
        .document(requestID).update("status", newStatus)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Request is updated", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Request update failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * method to add challenge to firestore database
     *
     * @param challenge challenge that should be stored
     *
     * @return id from stored challenge
     */
    private String addChallengeInFirestore(Challenge challenge) {

        String result;

        DocumentReference docRef = firebaseFirestore.collection(
                "Challenges").document();
        result = docRef.getId();
        challenge.setId(result);
        docRef.set(challenge)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Request is stored in Firestore");
                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Challenge is stored in Firestore",
                    Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Write Challenge in Firestore failed",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });

        return result;
    }

    /**
     * method to add challenge into challenge list of involved users
     *
     * @param user1ID id of first user (initiator of challenge)
     * @param user2ID if of second user (acceptor of challenge)
     * @param challengeID id of challenge that should be added
     */
    private void addChallengeToFirebaseUsers(String user1ID,
    String user2ID, String challengeID) {

        Map<String, String> challengeMap = new HashMap<>();
        challengeMap.put("challengeId", challengeID);

        firebaseFirestore.collection("Users")
        .document(user1ID).collection("Challenges")
        .document().set(challengeMap)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG,
                    "onComplete: Challenge ist " +
                    "stored for source user / user 1");
                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Challenge ist stored for source user / user 1",
                    Toast.LENGTH_SHORT).show();
                } else {

                    Log.d(TAG, "onComplete: Write " +
                    "challenge ID to user 1 failed");
                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Write challenge ID to user 1 failed",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseFirestore.collection("Users")
        .document(user2ID).collection("Challenges")
        .document().set(challengeMap)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Challenge is " +
                    "stored for target user / user 2");
                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Challenge ist stored for target user / user 2",
                    Toast.LENGTH_SHORT).show();
                } else {

                    Log.d(TAG,
                    "onComplete: Write challenge ID to user 2 failed");
                    Toast.makeText(ChallengesOverviewActivity.this,
                    "Write challenge ID to user 2 failed",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * method to start challenge service for step counting
     * @param userID id of user who participate to challenge
     */
    private void startChallengeService(String userID) {

        Intent challengeServiceIntent = new Intent(
        ChallengesOverviewActivity.this,
        ChallengeStepCounterService.class);
        challengeServiceIntent.putExtra("userID", userID);
        startService(challengeServiceIntent);
    }
}
