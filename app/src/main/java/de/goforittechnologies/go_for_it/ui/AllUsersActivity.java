package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.User;

public class AllUsersActivity extends AppCompatActivity {

    private static final String TAG = "AllUsersActivity";

    // Widgets
    private TextView tvUserEmptyListText;
    private ListView lvAllUsers;

    // Member variables
    private List<User> usersList;
    private UsersAdapter usersAdapter;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Set widgets
        tvUserEmptyListText = findViewById(R.id.tvUsersEmptyListText);
        lvAllUsers = findViewById(R.id.lvUsers);
        lvAllUsers.setEmptyView(tvUserEmptyListText);

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

                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        Log.d(TAG, "onEvent: Document ID : " + doc.getDocument().getId());
                        Log.d(TAG, "onEvent: User ID : " + currentUser.getUid());

                        if (!doc.getDocument().getId().equals(currentUser.getUid())) {

                            Log.d(TAG, "onEvent: Document ID : " + doc.getDocument().getId());
                            User user = doc.getDocument().toObject(User.class);
                            usersList.add(user);
                            usersAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
