package de.goforittechnologies.go_for_it.ui;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import de.goforittechnologies.go_for_it.storage.Request;
import de.goforittechnologies.go_for_it.storage.User;

/**
 * @author  Mario Kiese.
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class shows all firebase users to send them a challenge request
 * Corresponding layout: res.layout.activity_all_users.xml
 *
 * The user can choose an existing app-user with firebase account by klicking on their displayed
 * user. In the activated dialog, the user is able to type in a step goal both user want to archive.
 * Using "CANCEL" or "OK" a request can be sent or cancelled.
 */

public class AllUsersActivity extends AppCompatActivity {

    private static final String TAG = "AllUsersActivity";

    private ProgressBar pbAllUsers;

    // Member variables
    private List<User> usersList;
    private UsersAdapter usersAdapter;
    private String sourceUserName;
    private String sourceUserImage;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Set widgets
        // Widgets
        TextView tvUserEmptyListText = findViewById(R.id.tvUsersEmptyListText);
        ListView lvAllUsers = findViewById(R.id.lvUsers);
        lvAllUsers.setEmptyView(tvUserEmptyListText);
        Toolbar tbAllUsers = findViewById(R.id.tbAllUsers);
        setSupportActionBar(tbAllUsers);
        pbAllUsers = findViewById(R.id.pbAllUsers);
        pbAllUsers.setVisibility(View.VISIBLE);

        // Set member variables
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(AllUsersActivity.this, usersList);
        lvAllUsers.setAdapter(usersAdapter);

        // Configure Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e!=null){
                    Log.d(TAG,"Error : " + e.getMessage());
                    pbAllUsers.setVisibility(View.INVISIBLE);
                } else {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Log.d(TAG, "onEvent: Document ID : " + doc.getDocument().getId());
                            Log.d(TAG, "onEvent: User ID : " + currentUser.getUid());

                            if (!doc.getDocument().getId().equals(currentUser.getUid())) {

                                Log.d(TAG, "onEvent: Document ID : " + doc.getDocument().getId());
                                User user = doc.getDocument().toObject(User.class);
                                usersList.add(user);
                                usersAdapter.notifyDataSetChanged();
                            } else {

                                sourceUserName = (String)doc.getDocument().get("name");
                                sourceUserImage = (String)doc.getDocument().get("image");
                            }
                        }
                    }
                    pbAllUsers.setVisibility(View.INVISIBLE);
                }
            }
        });

        lvAllUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AllUsersActivity.this);
                dialogBuilder.setTitle("Challenge request! Type in your step challenge target:");

                // Set up the input
                final EditText input = new EditText(AllUsersActivity.this);

                // Specify the type of input expected
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);
                dialogBuilder.setView(input);

                // Set up the buttons
                dialogBuilder.setPositiveButton("OK", null);

                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();

                // Implementation of a View.OnClickListener to control if dialog can be dismissed (not given with DialogInterface.OnClickListener
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int stepTarget = Integer.valueOf(input.getText().toString());

                                if (stepTarget > 0) {
                                    Toast.makeText(AllUsersActivity.this, "Valid step target!", Toast.LENGTH_SHORT).show();

                                    User targetUser = ((User)adapterView.getItemAtPosition(position));
                                    String targetUserId = targetUser.getId();
                                    String sourceID = currentUser.getUid();
                                    String targetUserName = targetUser.getName();
                                    String challengeID = "";
                                    String requestID = "";
                                    String status = "pending";

                                    Request challengeRequest = new Request(requestID, stepTarget, sourceID, targetUserId, sourceUserName, targetUserName, sourceUserImage, challengeID, status);

                                    manageRequest(challengeRequest);

                                    Toast.makeText(AllUsersActivity.this, "UserID : " + targetUser, Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onClick: UserID " + targetUser);

                                    alertDialog.dismiss();
                                } else {
                                    Toast.makeText(AllUsersActivity.this, "Please enter a valid step target", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

                alertDialog.show();
            }
        });
    }

    private void manageRequest(Request challengeRequest) {

        String requestID = addRequestInFirestore(challengeRequest);

        addRequestToFirebaseUsers(challengeRequest.getSourceUserID(), challengeRequest.getTargetUserID(), requestID);
        
        //startChallengeService(requestID);
        
        
        
    }

    private String addRequestInFirestore(Request challengeRequest) {

        String result;

        DocumentReference docRef = firebaseFirestore.collection("Requests").document();
        result = docRef.getId();
        docRef.set(challengeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Request is stored in Firestore");
                    Toast.makeText(AllUsersActivity.this, "Request is stored in Firestore", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return result;
    }

    private void addRequestToFirebaseUsers(String sourceUserID, String targetUserID, String requestID) {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("requestId", requestID);

        firebaseFirestore.collection("Users").document(sourceUserID).collection("Requests").document().set(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Request ist stored for source user");
                    Toast.makeText(AllUsersActivity.this, "Request ist stored for source user", Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseFirestore.collection("Users").document(targetUserID).collection("Requests").document().set(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Request ist stored for target user");
                    Toast.makeText(AllUsersActivity.this, "Request ist stored for target user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
