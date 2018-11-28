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

import de.goforittechnologies.go_for_it.R;

public class LoginActivity extends AppCompatActivity {

    // Widgets
    private EditText etLoginMailText;
    private EditText etLoginPasswordText;
    private Button btLogin;
    private Button btLoginRegister;
    private ProgressBar pbLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginMailText = findViewById(R.id.etLoginMail);
        etLoginPasswordText = findViewById(R.id.etLoginPassword);
        btLogin = findViewById(R.id.btnLogin);
        btLoginRegister = findViewById(R.id.btnLoginRegister);
        pbLogin = findViewById(R.id.pbLogin);

        mAuth = FirebaseAuth.getInstance();

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail = etLoginMailText.getText().toString();
                String loginPassword = etLoginPasswordText.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {

                    pbLogin.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                sendToMain();

                            } else {

                                String e = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + e, Toast.LENGTH_LONG).show();

                            }

                            pbLogin.setVisibility(View.INVISIBLE);

                        }
                    });

                }

            }
        });

        btLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            sendToMain();

        }
    }

    private void sendToMain() {

        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();

    }
}
