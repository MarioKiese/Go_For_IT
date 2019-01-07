package de.goforittechnologies.go_for_it.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import de.goforittechnologies.go_for_it.R;
/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 * @see AppCompatActivity
 *
 *
 * This class is used to log into the firebase account
 * @see FirebaseAuth
 * @see FirebaseUser
 *
 * Corresponding layout: res.layout.activity_login
 *
 * The user can type in username and password.
 *
 * The user can log himself in by klicking the "login" button.
 *
 * The user can create a new firebase account by klicking "create new
 * account" at the bottom.
 * @see RegisterActivity
 *
 *
 */

public class LoginActivity extends AppCompatActivity {

    // Widgets
    private EditText etLoginMailText;
    private EditText etLoginPasswordText;
    private ProgressBar pbLogin;

    private FirebaseAuth mAuth;

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - configure firebase-usage
     * - set click listeners
     * - set complete listeners
     *
     * @see FirebaseAuth
     * @see FirebaseUser
     * @see OnCompleteListener
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginMailText = findViewById(R.id.etLoginMail);
        etLoginPasswordText = findViewById(R.id.etLoginPassword);
        Button btLogin = findViewById(R.id.btnLogin);
        Button btLoginRegister = findViewById(R.id.btnLoginRegister);
        pbLogin = findViewById(R.id.pbLogin);

        mAuth = FirebaseAuth.getInstance();

        btLogin.setOnClickListener(view -> {

            String loginEmail = etLoginMailText.getText().toString();
            String loginPassword = etLoginPasswordText.getText().toString();

            if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty
                    (loginPassword)) {

                pbLogin.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener(new
                        OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            sendToMain();

                        } else {

                            String e = Objects.requireNonNull(task.getException()).getMessage();
                            Toast.makeText(LoginActivity.this,
                                    "Error : " +
                                    e, Toast.LENGTH_LONG).show();

                        }

                        pbLogin.setVisibility(View.INVISIBLE);

                    }
                });

            }

        });

        btLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerIntent = new Intent(
                LoginActivity.this,
                RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

    }

    /**
     * method to test if current user is authenticated to log in and navigate
     * him to main activity if authentication is correct.
     *
     * @see FirebaseUser
     * @see FirebaseAuth
     * @see MainActivity
     */
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            sendToMain();

        }
    }

    /**
     * method to navigate user to main activity via Intent
     *
     * @see MainActivity
     */
    private void sendToMain() {

        Intent loginIntent =
        new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();

    }
}
