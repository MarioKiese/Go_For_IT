package de.goforittechnologies.go_for_it;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView ivSetupImage;
    private Uri mainImageUri = null;

    private EditText etSetupName;
    private Button btSetup;
    private ProgressBar pbSetup;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar tbSetup = findViewById(R.id.tbSetup);
        setSupportActionBar(tbSetup);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        etSetupName = findViewById(R.id.etSetupName);
        btSetup = findViewById(R.id.btnSetup);
        pbSetup = findViewById(R.id.pbSetup);

        btSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = etSetupName.getText().toString();

                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                    String userID = firebaseAuth.getCurrentUser().getUid();
                    pbSetup.setVisibility(View.VISIBLE);

                    final StorageReference imagePath = storageReference.child("profile_images").child(userID + ".jpg");
                    imagePath.putFile(mainImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {

                                throw task.getException();

                            }

                            return imagePath.getDownloadUrl();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();
                                Toast.makeText(SetupActivity.this, "The image is uploaded", Toast.LENGTH_LONG).show();

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();

                            }

                            pbSetup.setVisibility(View.INVISIBLE);

                        }
                    });


                }

            }
        });

        ivSetupImage = findViewById(R.id.ivSetupImage);

        ivSetupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Permisson denied!", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);

                    }

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                ivSetupImage.setImageURI(mainImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
