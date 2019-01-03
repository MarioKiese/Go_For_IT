package de.goforittechnologies.go_for_it.logic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.ui.ChallengesOverviewActivity;

public class ChallengeStepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "ChallengeStepCounterSer";

    // Step management
    private int currentSteps;
    private int oldSteps;
    private SensorManager sensorManager;
    private Sensor stepSensor;

    // Firebase
    FirebaseFirestore firebaseFirestore;
    private String userID;

    // Binder
    private IBinder mBinder = new LocationBinder();

    public ChallengeStepCounterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind: connected");
        return mBinder;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "onCreate: Start");

        // Configure Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Configure steps manager
        currentSteps = 0;
        oldSteps = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(ChallengeStepCounterService.this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get userID from intent extra
        userID = intent.getStringExtra("userID");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        sensorManager.unregisterListener(ChallengeStepCounterService.this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        currentSteps++;
        int diff = currentSteps - oldSteps;
        if (diff >= 10) {

            oldSteps = currentSteps;
            ArrayList<String> challengeIDs = getChallengeIDs(userID);
            /*for (String id : challengeIDs)  {

                writeStepsInChallenge(diff);
            }*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private ArrayList<String> getChallengeIDs(String userID) {

        ArrayList<String> resultList = new ArrayList<>();
        firebaseFirestore.collection("Users").document(userID).collection("Challenges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Getting challenge IDs");
                    List<DocumentSnapshot> list = task.getResult().getDocuments();
                    for (DocumentSnapshot doc : list) {

                        String challengeID = doc.getString("challengeId");
                        resultList.add(challengeID);
                        Log.d(TAG, "Challenge id : " + challengeID);
                    }
                } else {

                    Log.d(TAG, "onComplete: Get challenge IDs failed!");
                }
            }
        });
        return resultList;
    }

    public class LocationBinder extends Binder {

        public ChallengeStepCounterService getService() {

            return ChallengeStepCounterService.this;
        }
    }
}
