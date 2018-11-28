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

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegMailText;
    private EditText etRegPasswordText;
    private EditText etRegConfirmPasswordText;
    private Button btRegister;
    private Button btRegisterLogin;
    private ProgressBar pbRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etRegMailText = findViewById(R.id.etRegMail);
        etRegPasswordText = findViewById(R.id.etRegPassword);
        etRegConfirmPasswordText = findViewById(R.id.etRegConfirmPassword);
        btRegister = findViewById(R.id.btnRegister);
        btRegisterLogin = findViewById(R.id.btnRegisterLogin);
        pbRegister = findViewById(R.id.pbRegister);

        btRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = etRegMailText.getText().toString();
                String password = etRegPasswordText.getText().toString();
                String confirmPassword = etRegConfirmPasswordText.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {

                    if (password.equals(confirmPassword)) {

                        pbRegister.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {



                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                } else {

                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT).show();

                                }

                                pbRegister.setVisibility(View.INVISIBLE);

                            }
                        });

                    } else {

                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password field doesn't match!", Toast.LENGTH_SHORT).show();

                    }

                }

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

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
