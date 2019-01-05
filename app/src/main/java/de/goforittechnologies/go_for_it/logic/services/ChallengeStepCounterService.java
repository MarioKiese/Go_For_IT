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

import de.goforittechnologies.go_for_it.storage.Challenge;
import de.goforittechnologies.go_for_it.storage.User;
import de.goforittechnologies.go_for_it.ui.ChallengesOverviewActivity;

public class ChallengeStepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "ChallengeStepCounterSer";

    // Step management
    private int currentSteps;
    private int oldSteps;
    private static final int STEPS_UPLOAD_RATE = 10;
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
        if (diff >= STEPS_UPLOAD_RATE) {

            oldSteps = currentSteps;
            getChallengeIDs(userID, diff);
            /*ArrayList<String> challengeIDs = getChallengeIDs(userID, diff);
            for (String currentChallengeID : challengeIDs)  {

                Log.d(TAG, "onSensorChanged: Test currentChallengeID: " + currentChallengeID);
                writeStepsInChallenge(currentChallengeID, diff);
            }*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void getChallengeIDs(String userID, int stepsToAdd) {

        //ArrayList<String> resultList = new ArrayList<>();
        firebaseFirestore.collection("Users").document(userID).collection("Challenges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Getting challenge IDs");
                    List<DocumentSnapshot> list = task.getResult().getDocuments();
                    for (DocumentSnapshot doc : list) {

                        String challengeID = doc.getString("challengeId");
                        writeStepsInChallenge(challengeID, stepsToAdd);
                        //resultList.add(challengeID);
                        Log.d(TAG, "Challenge id : " + challengeID);
                    }
                } else {

                    Log.d(TAG, "onComplete: Get challenge IDs failed!");
                }
            }
        });
        //return resultList;
    }

    private void writeStepsInChallenge(String challengeID, int stepsToAdd) {

        Log.d(TAG, "writeStepsInChallenge: Enter method writeStepsInChallenge");

        firebaseFirestore.collection("Challenges").document(challengeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Getting challenge successful");

                    if (task.getResult().exists()) {

                        Challenge challenge = task.getResult().toObject(Challenge.class);
                        User user1 = challenge.getUser1();
                        if (userID.equals(user1.getId())) {

                            int currentSteps = challenge.getStepsUser1();
                            int newSteps = currentSteps + stepsToAdd;
                            firebaseFirestore.collection("Challenges").document(challengeID).update("stepsUser1", newSteps).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Log.d(TAG, "onComplete: Challenge steps updated");
                                    } else {

                                        Log.d(TAG, "onComplete: Error updating challenge steps");
                                    }
                                }
                            });
                        } else {

                            int currentSteps = challenge.getStepsUser2();
                            int newSteps = currentSteps + stepsToAdd;
                            firebaseFirestore.collection("Challenges").document(challengeID).update("stepsUser2", newSteps).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Log.d(TAG, "onComplete: Challenge steps updated");
                                    } else {

                                        Log.d(TAG, "onComplete: Error updating challenge steps");
                                    }
                                }
                            });
                        }
                    }
                } else {

                    Log.d(TAG, "onComplete: Error getting challenge");
                }
            }
        });
    }

    public class LocationBinder extends Binder {

        public ChallengeStepCounterService getService() {

            return ChallengeStepCounterService.this;
        }
    }
}
