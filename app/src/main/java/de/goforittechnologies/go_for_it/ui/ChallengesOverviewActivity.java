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

public class ChallengesOverviewActivity extends AppCompatActivity {

    private static final String TAG = "ChallengesOverviewActy";

    // Widgets
    private Toolbar tbChallenges;
    private TextView tvRequestsListEmptyText;
    private TextView tvActiveChallengesListEmptyText;
    private ListView lvRequests;
    private ListView lvActiveChallenges;
    private ProgressBar pbRequests;

    // Member variables
    private List<Request> requestsList;
    private RequestsAdapter requestsAdapter;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String userID;

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
        tvActiveChallengesListEmptyText = findViewById(R.id.tvActiveChallengesEmtpyListText);
        lvActiveChallenges = findViewById(R.id.lvActiveChallenges);
        lvActiveChallenges.setEmptyView(tvActiveChallengesListEmptyText);
        pbRequests = findViewById(R.id.pbRequests);
        pbRequests.setVisibility(View.VISIBLE);

        // Set member variables
        requestsList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(ChallengesOverviewActivity.this, requestsList);
        lvRequests.setAdapter(requestsAdapter);

        // Configure Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        userID = currentUser.getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(userID).collection("Requests").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e!=null){

                    Log.d(TAG,"Error : " + e.getMessage());
                    pbRequests.setVisibility(View.INVISIBLE);
                    return;
                } else {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String requestID = (String)doc.getDocument().get("requestId");
                            Log.d(TAG, "onEvent: Request ID found: " + requestID);
                            Toast.makeText(ChallengesOverviewActivity.this, "Request ID found: " + requestID, Toast.LENGTH_SHORT).show();

                            if (requestID != null) {

                                firebaseFirestore.collection("Requests").document(requestID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                        if (e != null) {

                                            Log.d(TAG,"Error : " + e.getMessage());
                                            return;
                                        } else {

                                            if (documentSnapshot != null && documentSnapshot.exists()) {

                                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                                                Request request = documentSnapshot.toObject(Request.class);
                                                Log.d(TAG, "onComplete: Firestore data converted to object");
                                                Log.d(TAG, "onComplete: TargetUserID : " + request.getTargetUserID() + " UserID : " + userID);

                                                if (request.getTargetUserID().equals(userID) && request.getStatus().equals("pending")) {

                                                    Log.d(TAG, "onComplete: Request is compatible with userID");

                                                    requestsList.add(request);
                                                    requestsAdapter.notifyDataSetChanged();
                                                }
                                                else if(request.getTargetUserID().equals(userID) && (request.getStatus().equals("accepted") || request.getStatus().equals("declined"))) {

                                                    Log.d(TAG, "onEvent: Remove request from request list");

                                                    for (int i=0; i<requestsList.size(); i++) {

                                                        if (requestsList.get(i).getId().equals(request.getId())) {

                                                            requestsList.remove(i);
                                                            requestsAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG, "Current data: null");
                                            }
                                        }
                                    }
                                });
                            } else {

                                Log.d(TAG, "onEvent: Request ID is null");
                                Toast.makeText(ChallengesOverviewActivity.this, "Request ID is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    pbRequests.setVisibility(View.INVISIBLE);
                }
            }
        });

        lvRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChallengesOverviewActivity.this);
                dialogBuilder.setTitle("Challenge request!");

                // Set up the buttons
                dialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Request request = (Request)adapterView.getItemAtPosition(position);
                        // Source user data
                        String sourceUserID = request.getSourceUserID();
                        String sourceUserName = request.getSourceUserName();
                        String sourceUserImage = request.getSourceUserImage();
                        User sourceUser = new User(sourceUserID, sourceUserName, sourceUserImage);
                        // Target user data
                        String targetUserID = request.getTargetUserID();
                        String targetUserName = request.getTargetUserName();
                        String targetUserImage = request.getTargetUserImage();
                        User targetUser = new User(targetUserID, targetUserName, targetUserImage);

                        String challengeID = "";
                        String requestID = request.getId();
                        int stepTarget = request.getStepTarget();
                        String status = "running";

                        Challenge challenge = new Challenge(challengeID, requestID, stepTarget, sourceUser, targetUser, status);

                        manageChallenge(challenge);
                    }
                });

                dialogBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Request request = (Request)adapterView.getItemAtPosition(position);
                        updateRequestStatus(request.getId(), "declined");

                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
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

    private void manageChallenge(Challenge challenge) {

        updateRequestStatus(challenge.getRequestID(), "accepted");
        String challengeID = addChallengeInFirestore(challenge);
        addChallengeToFirebaseUsers(challenge.getUser1().getId(), challenge.getUser2().getId(), challengeID);
        startChallengeService(userID);
    }

    private void updateRequestStatus(String requestID, String newStatus) {

        firebaseFirestore.collection("Requests").document(requestID).update("status", newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(ChallengesOverviewActivity.this, "Request is updated", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(ChallengesOverviewActivity.this, "Request update failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String addChallengeInFirestore(Challenge challenge) {

        String result;

        DocumentReference docRef = firebaseFirestore.collection("Challenges").document();
        result = docRef.getId();
        challenge.setId(result);
        docRef.set(challenge).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Request is stored in Firestore");
                    Toast.makeText(ChallengesOverviewActivity.this, "Challenge is stored in Firestore", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(ChallengesOverviewActivity.this, "Write Challenge in Firestore failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return result;
    }

    private void addChallengeToFirebaseUsers(String user1ID, String user2ID, String challengeID) {

        Map<String, String> challengeMap = new HashMap<>();
        challengeMap.put("challengeId", challengeID);

        firebaseFirestore.collection("Users").document(user1ID).collection("Challenges").document().set(challengeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Challenge ist stored for source user / user 1");
                    Toast.makeText(ChallengesOverviewActivity.this, "Challenge ist stored for source user / user 1", Toast.LENGTH_SHORT).show();
                } else {

                    Log.d(TAG, "onComplete: Write challenge ID to user 1 failed");
                    Toast.makeText(ChallengesOverviewActivity.this, "Write challenge ID to user 1 failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseFirestore.collection("Users").document(user2ID).collection("Challenges").document().set(challengeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Challenge ist stored for target user / user 2");
                    Toast.makeText(ChallengesOverviewActivity.this, "Challenge ist stored for target user / user 2", Toast.LENGTH_SHORT).show();
                } else {

                    Log.d(TAG, "onComplete: Write challenge ID to user 2 failed");
                    Toast.makeText(ChallengesOverviewActivity.this, "Write challenge ID to user 2 failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startChallengeService(String userID) {

        Intent challengeServiceIntent = new Intent(ChallengesOverviewActivity.this, ChallengeStepCounterService.class);
        challengeServiceIntent.putExtra("userID", userID);
        startService(challengeServiceIntent);
    }
}
