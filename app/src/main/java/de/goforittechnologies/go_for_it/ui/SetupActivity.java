package de.goforittechnologies.go_for_it.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
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
import java.util.Random;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Mario Kiese and Tom Hammerbacher.
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class is used to configure the account setting of the user and to
 * create testdata.
 *
 * Corresponding layout: res.layout.activity_setup.xml
 *
 * INFO: creating test data is only for test purpose! it may influence
 *      * the activity's performance. please to not tap the buttons multiple
 *      * times in a row.
 * By klicking on the "smilys" image or the users profile picture, a userfoto
 * can be loaded up and linked to the firebase account.
 *
 * In the text-view in above the profile picture the user is able to change
 * his username.
 *
 * The two buttons in the bottom are used to create and delete testdata for
 * the november (11.) month, to test the given charts in the dashboard
 * activity without using the app for one month.
 * @see DashboardActivity
 * @see FirebaseAuth
 *
 */

public class SetupActivity extends AppCompatActivity {

    // Widgets
    private EditText etSetupName;
    private Button btSetup;
    private Button btCreateTestdata;
    private Button btDeleteTestdata;
    private ProgressBar pbSetup;
    private CircleImageView ivSetupImage;

    private Uri mainImageUri = null;
    private String userID;
    private boolean isChanged = false;
    private static final String TAG = "SetupActivity";

    // Shared preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // Firebase
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private Random rand;

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * - configure firebase-usage
     * - set click listeners
     * - set complete listeners
     * - create testdata
     * INFO: creating test data is only for test purpose! it may influence
     * the activity's performance. please to not tap the buttons multiple
     * times in a row.
     *
     * @see FirebaseAuth
     * @see DataSourceStepData

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar tbSetup = findViewById(R.id.tbSetup);
        setSupportActionBar(tbSetup);

        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        rand = new Random();

        etSetupName = findViewById(R.id.etSetupName);
        ivSetupImage = findViewById(R.id.ivSetupImage);
        btSetup = findViewById(R.id.btnSetup);
        btCreateTestdata = findViewById(R.id.btnCreateTestdata);
        btDeleteTestdata = findViewById(R.id.btnDeleteTestdata);
        pbSetup = findViewById(R.id.pbSetup);

        pbSetup.setVisibility(View.VISIBLE);
        btSetup.setEnabled(false);

        // Set shared preferences
        pref = getApplicationContext()
                .getSharedPreferences("MapsPref", MODE_PRIVATE);
        editor = pref.edit();

        firebaseFirestore.collection("Users").document(userID)
        .get().addOnCompleteListener(
        new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name =
                        task.getResult().getString("name");
                        String image =
                        task.getResult().getString("image");

                        etSetupName.setText(name);
                        mainImageUri = Uri.parse(image);

                        RequestOptions placeholderRequest =
                        new RequestOptions();
                        placeholderRequest
                        .placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this)
                        .setDefaultRequestOptions(placeholderRequest)
                        .load(image).into(ivSetupImage);
                    } else {

                        Toast.makeText(SetupActivity.this,
                        "Data does not exist", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,
                    "Firestore Retrieve Error : "
                    + error, Toast.LENGTH_SHORT).show();
                }

                pbSetup.setVisibility(View.INVISIBLE);
                btSetup.setEnabled(true);
            }
        });

        btDeleteTestdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSourceStepData dataSourceStepData;
                try {
                    dataSourceStepData = new DataSourceStepData(
                            SetupActivity.this,
                            "StepDataTABLE_11",1);

                    dataSourceStepData.open();
                    double[] day;
                    for (int i = 1; i <=31; i++){
                        for (int j = 0; j <24; j++){
                            dataSourceStepData.updateStepData(
                            0,i+ ":" +j);
                        }
                    }
                    dataSourceStepData.close();
                } catch (Exception e) {

                }

            }
        });

        btCreateTestdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DataSourceStepData dataSourceStepData;

                dataSourceStepData = new DataSourceStepData(
                        SetupActivity.this,
                        "StepDataTABLE_11", 0);


                dataSourceStepData.open();

                double[] day;
                for (int i = 1; i <=30; i++){
                    if (i%7 == 0 || i%7 == 6){
                        day = buildWeekendDay();

                    }
                    else if (i%7 == 1 ||i%7 == 3){
                        day = buildUniDay1();
                    }
                    else if(i%7 == 4){
                        day = buildWorkDay();
                    }
                    else {
                        day = buildUniDay2();
                    }

                    for (int j = 0; j <24; j++){
                        Log.d(TAG,
                        "onClick: Update i:j" +i+ ":" +j);
                        dataSourceStepData
                        .updateStepData(day[j],i+ ":" +j);

                    }
                }
                dataSourceStepData.close();
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

                        final StorageReference imagePath =
                        storageReference.child("profile_images")
                        .child(userID + ".jpg");

                        imagePath.putFile(mainImageUri)
                        .continueWithTask(
                        new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(
                            @NonNull Task<UploadTask.TaskSnapshot> task)
                            throws Exception {
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

                                    String error = task.getException()
                                    .getMessage();
                                    Toast.makeText(
                                    SetupActivity.this, "Error : "
                                        + error, Toast.LENGTH_LONG).show();
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

                    if(ContextCompat.checkSelfPermission(
                    SetupActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this,
                        "Permisson denied!", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(
                        SetupActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);

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

    /**
     * method to select image cropper to get image
     *
     * @param requestCode code for request (to select right activity)
     * @param resultCode code for result
     * @param data data
     *
     * @see CropImage
     */
    @Override
    protected void onActivityResult(
    int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                ivSetupImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode ==
                    CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(this, "Error : "
                + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    /**
     * method to store data in firestore account data
     *
     * @param task firestore request to upload
     * @param userName name of user
     *
     * @see Uri
     * @see FirebaseFirestore
     */
    private void storeDataInFirestore(Task<Uri> task, String userName) {

        Uri downloadUri;

        if (task != null) {

            downloadUri = task.getResult();
        } else {

            downloadUri = mainImageUri;
        }


        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", userID);
        userMap.put("name", userName);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(userID)
        .set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this,
                    "The user settings are updated",
                    Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(
                   SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,
                    "Firestore Error : " + error,
                    Toast.LENGTH_SHORT).show();
                }

                pbSetup.setVisibility(View.INVISIBLE);
            }
        });
        Toast.makeText(SetupActivity.this,
        "The image is uploaded",
        Toast.LENGTH_LONG).show();
    }

    /**
     * creating uni day test data set for one day
     * @return double array of 24 values for every hour of one day with step
     * values.
     */
    private double[] buildUniDay1(){
        double[] day = new double[24];
        day[0] = day[1] = day[2] = day[3] = day[4] = day[5] = day[23] = 0;
        day[6] =    60    + rand.nextInt(30-10)+10;
        day[7] =    100   + rand.nextInt(30-10)+10;
        day[8] =    150   + rand.nextInt(30-10)+10;
        day[9] =    80    + rand.nextInt(30-10)+10;
        day[10] =   1300  + rand.nextInt(30-10)+10;
        day[11] =   350   + rand.nextInt(30-10)+10;
        day[12] =   100   + rand.nextInt(30-10)+10;
        day[13] =   130   + rand.nextInt(30-10)+10;
        day[14] =   140   + rand.nextInt(30-10)+10;
        day[15] =   70    + rand.nextInt(30-10)+10;
        day[16] =   60    + rand.nextInt(30-10)+10;
        day[17] =   1200  + rand.nextInt(30-10)+10;
        day[18] =   450   + rand.nextInt(30-10)+10;
        day[19] =   100   + rand.nextInt(30-10)+10;
        day[20] =   60    + rand.nextInt(20-10)+10;
        day[21] =   150   + rand.nextInt(50-10)+10;
        day[22] =   60    + rand.nextInt(30-10)+10;
        return day;
    }
    /**
     * creating alternative uni day test data set for one day
     * @return double array of 24 values for every hour of one day with step
     * values.
     */
    private double[] buildUniDay2(){
        double[] day = new double[24];
        day[0] = day[1] = day[2] = day[3] = day[4] = day[5] = day[23] = 0;
        day[6] =    60    + rand.nextInt(30-10)+10;
        day[7] =    100   + rand.nextInt(30-10)+10;
        day[8] =    150   + rand.nextInt(30-10)+10;
        day[9] =    80    + rand.nextInt(30-10)+10;
        day[10] =   300  + rand.nextInt(30-10)+10;
        day[11] =   350   + rand.nextInt(30-10)+10;
        day[12] =   100   + rand.nextInt(30-10)+10;
        day[13] =   1500  + rand.nextInt(30-10)+10;
        day[14] =   500   + rand.nextInt(30-10)+10;
        day[15] =   70    + rand.nextInt(30-10)+10;
        day[16] =   60    + rand.nextInt(30-10)+10;
        day[17] =   400  + rand.nextInt(30-10)+10;
        day[18] =   450   + rand.nextInt(30-10)+10;
        day[19] =   100   + rand.nextInt(30-10)+10;
        day[20] =   60    + rand.nextInt(20-10)+10;
        day[21] =   150   + rand.nextInt(50-10)+10;
        day[22] =   60    + rand.nextInt(30-10)+10;
        return day;
    }
    /**
     * creating work day test data set for one day
     * @return double array of 24 values for every hour of one day with step
     * values.
     */
    private double[] buildWorkDay(){
        double[] day = new double[24];
        day[0] = day[1] = day[2] = day[3] = day[4] = day[5] = day[23] = 0;
        day[6] =    60    + rand.nextInt(30-10)+10;
        day[7] =    100   + rand.nextInt(30-10)+10;
        day[8] =    150   + rand.nextInt(30-10)+10;
        day[9] =    80    + rand.nextInt(30-10)+10;
        day[10] =   130   + rand.nextInt(30-10)+10;
        day[11] =   350   + rand.nextInt(30-10)+10;
        day[12] =   100   + rand.nextInt(30-10)+10;
        day[13] =   150   + rand.nextInt(30-10)+10;
        day[14] =   500   + rand.nextInt(30-10)+10;
        day[15] =   70    + rand.nextInt(30-10)+10;
        day[16] =   60    + rand.nextInt(30-10)+10;
        day[17] =   1200  + rand.nextInt(250-10)+10;
        day[18] =   1000  + rand.nextInt(100-10)+10;
        day[19] =   100   + rand.nextInt(30-10)+10;
        day[20] =   60    + rand.nextInt(20-10)+10;
        day[21] =   150   + rand.nextInt(50-10)+10;
        day[22] =   60    + rand.nextInt(30-10)+10;
        return day;

    }
    /**
     * creating weekend day test data set for one day
     * @return double array of 24 values for every hour of one day with step
     * values.
     */
    private double[] buildWeekendDay() {
        double[] day = new double[24];
        day[0] = day[1] = day[2] = day[3] = day[4] = day[5]
                = day[6] = day[7] = day[8] = day[23] = 0;

        day[9] = 80 + rand.nextInt(30 - 10) + 10;
        day[10] = 230 + rand.nextInt(30 - 10) + 10;
        day[11] = 250 + rand.nextInt(30 - 10) + 10;
        day[12] = 200 + rand.nextInt(30 - 10) + 10;
        day[13] = 250 + rand.nextInt(30 - 10) + 10;
        day[14] = 230 + rand.nextInt(30 - 10) + 10;
        day[15] = 210 + rand.nextInt(30 - 10) + 10;
        day[16] = 220 + rand.nextInt(30 - 10) + 10;
        day[17] = 1200 + rand.nextInt(250 - 10) + 10;
        day[18] = 1000 + rand.nextInt(100 - 10) + 10;
        day[19] = 100 + rand.nextInt(30 - 10) + 10;
        day[20] = 60 + rand.nextInt(20 - 10) + 10;
        day[21] = 150 + rand.nextInt(50 - 10) + 10;
        day[22] = 60 + rand.nextInt(30 - 10) + 10;
        return day;
    }

    private void saveAgeAndHeightInSharedPreferences(int age, float height) {

        editor.putInt("age", age);
        editor.apply();
        editor.putFloat("height", height);
        editor.apply();
    }
}
