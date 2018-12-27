package de.goforittechnologies.go_for_it.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.goforittechnologies.go_for_it.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    // Widgets
    private EditText etSetupName;
    private Button btSetup;
    private ProgressBar pbSetup;
    private CircleImageView ivSetupImage;

    private Uri mainImageUri = null;
    private String userID;
    private boolean isChanged = false;

    // Firebase
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar tbSetup = findViewById(R.id.tbSetup);
        setSupportActionBar(tbSetup);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        etSetupName = findViewById(R.id.etSetupName);
        ivSetupImage = findViewById(R.id.ivSetupImage);
        btSetup = findViewById(R.id.btnSetup);
        pbSetup = findViewById(R.id.pbSetup);

        pbSetup.setVisibility(View.VISIBLE);
        btSetup.setEnabled(false);

        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        etSetupName.setText(name);
                        mainImageUri = Uri.parse(image);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(ivSetupImage);
                    } else {

                        Toast.makeText(SetupActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                    }
                    
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Retrieve Error : " + error, Toast.LENGTH_SHORT).show();
                }

                pbSetup.setVisibility(View.INVISIBLE);
                btSetup.setEnabled(true);
            }
        });

        btSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = etSetupName.getText().toString();
                pbSetup.setVisibility(View.VISIBLE);

                if (isChanged) {

                    if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                        userID = firebaseAuth.getCurrentUser().getUid();

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

                                    storeDataInFirestore(task, userName);

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                                    pbSetup.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                } else {

                    storeDataInFirestore(null, userName);
                }

            }
        });

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

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void storeDataInFirestore(Task<Uri> task, String userName) {

        Uri downloadUri;

        if (task != null) {

            downloadUri = task.getResult();
        } else {

            downloadUri = mainImageUri;
        }


        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "The user settings are updated", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error : " + error, Toast.LENGTH_SHORT).show();
                }

                pbSetup.setVisibility(View.INVISIBLE);
            }
        });
        Toast.makeText(SetupActivity.this, "The image is uploaded", Toast.LENGTH_LONG).show();
    }
}
