package de.goforittechnologies.go_for_it.ui;

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

import java.util.Random;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView ivSetupImage;
    private Uri mainImageUri = null;

    private EditText etSetupName;
    private Button btSetup;
    private Button btCreateTestdata;
    private Button btDeleteTestdata;
    private ProgressBar pbSetup;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar tbSetup = findViewById(R.id.tbSetup);
        setSupportActionBar(tbSetup);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        rand = new Random();

        etSetupName = findViewById(R.id.etSetupName);
        btSetup = findViewById(R.id.btnSetup);
        btCreateTestdata = findViewById(R.id.btnCreateTestdata);
        btDeleteTestdata = findViewById(R.id.btnDeleteTestdata);
        pbSetup = findViewById(R.id.pbSetup);

        btDeleteTestdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSourceStepData dataSourceStepData = null;
                try {
                    dataSourceStepData = new DataSourceStepData(
                            SetupActivity.this, "StepDataTABLE_11",1);
                } catch (Exception e) {
                    dataSourceStepData = new DataSourceStepData(
                            SetupActivity.this, "StepDataTABLE_11",0);
                }
                dataSourceStepData.open();
                double[] day;
                for (int i = 1; i <=31; i++){
                    for (int j = 0; j <24; j++){
                        dataSourceStepData.updateStepData(0,i+ ":" +j);
                    }
                }
                dataSourceStepData.close();
            }
        });

        btCreateTestdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DataSourceStepData dataSourceStepData = null;
                try {
                    dataSourceStepData = new DataSourceStepData(
                            SetupActivity.this, "StepDataTABLE_11",1);
                } catch (Exception e) {
                    dataSourceStepData = new DataSourceStepData(
                            SetupActivity.this, "StepDataTABLE_11",0);
                }

                dataSourceStepData.open();

                double[] day;
                for (int i = 1; i <=31; i++){
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
                        dataSourceStepData.updateStepData(day[j],i+ ":" +j);
                    }
                }

                dataSourceStepData.close();
            }
        });

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
                Toast.makeText(this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }

    }

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
}
