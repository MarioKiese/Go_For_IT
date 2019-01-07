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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.goforittechnologies.go_for_it.storage.Challenge;
import de.goforittechnologies.go_for_it.storage.User;

/**
 * @author  Mario Kiese
 * @version 0.8.
 *
 * This service is used to log the steps of the challenge in the firestore
 * database.
 *
 */

public class ChallengeStepCounterService extends Service implements
SensorEventListener {


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

    /**
     *
     * @param intent Intent for connecting service.
     * @return IBinder to connect service to activity.
     */
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind: connected");
        return mBinder;
    }
    /**
     * method to declare and initialise service functions and variables
     */
    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "onCreate: Start");

        // Configure Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Configure steps manager
        currentSteps = 0;
        oldSteps = 0;
        sensorManager =
        (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(ChallengeStepCounterService.this,
                stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * method to select user id out of intent
     *
     * @param intent intent the user id should be "cut out"
     * @param flags no usage
     * @param startId no usage
     * @return code to restart service after destroying
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get userID from intent extra
        userID = intent.getStringExtra("userID");
        return START_STICKY;
    }

    /**
     * method to unregister listener on service destroy
     * @see SensorManager
     */
    @Override
    public void onDestroy() {

        sensorManager.unregisterListener(ChallengeStepCounterService.this);
        super.onDestroy();
    }

    /**
     * method to count and store steps
     *
     * @param sensorEvent event triggered on every noticed step
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        currentSteps++;
        int diff = currentSteps - oldSteps;
        if (diff >= STEPS_UPLOAD_RATE) {

            oldSteps = currentSteps;
            manageChallenges(userID, diff);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * method to inspect all challenges from user and filter running
     * challenges and add steps to them.
     *
     * @param userID id of user who's challenges should be checked
     * @param stepsToAdd step value to add to running challenges
     */
    private void manageChallenges(String userID, int stepsToAdd) {

        firebaseFirestore.collection("Users")
        .document(userID).collection("Challenges").get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Getting challenge IDs");
                    List<DocumentSnapshot> list =
                    task.getResult().getDocuments();
                    for (DocumentSnapshot doc : list) {

                        String challengeID =
                        doc.getString("challengeId");
                        writeStepsInChallenge(challengeID, stepsToAdd);
                        Log.d(TAG, "Challenge id : " + challengeID);
                    }
                } else {

                    Log.d(TAG,
                    "onComplete: Getting challenge IDs failed!");
                }
            }
        });
    }

    /**
     * method to write steps in challenge
     *
     * @param challengeID id of challenge which step value should be update
     * @param stepsToAdd step value that should be added to challenge
     */
    private void writeStepsInChallenge(String challengeID, int stepsToAdd) {

        Log.d(TAG,
        "writeStepsInChallenge: Enter method writeStepsInChallenge");

        firebaseFirestore.collection("Challenges")
        .document(challengeID).get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Getting challenge successful");

                    if (task.getResult().exists()) {

                        Challenge challenge =
                        task.getResult().toObject(Challenge.class);
                        String status = challenge.getStatus();
                        if (status.equals("running")) {

                            User user1 = challenge.getUser1();
                            User user2 = challenge.getUser2();

                            // Prove who of the two challenge users
                            // is the current user to know who is to be updated
                            if (userID.equals(user1.getId())) {

                                int currentSteps = challenge.getStepsUser1();
                                int newSteps = currentSteps + stepsToAdd;
                                int stepTarget = challenge.getStepTarget();
                                if (newSteps >= stepTarget) {

                                    // Update challenge status
                                    firebaseFirestore.collection(
                                    "Challenges")
                                    .document(challengeID).update("status",
                                    "finished").addOnCompleteListener
                                    (new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(
                                                @NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Log.d(TAG,
                                                "onComplete: " +
                                                "Challenge status updated");
                                            } else {

                                                Log.d(TAG,
                                                "onComplete: Error " +
                                                "updating challenge status");
                                            }
                                        }
                                    });

                                    Map<String, Object> userMap =
                                    new HashMap<>();
                                    userMap.put("id", user1.getId());
                                    userMap.put("image", user1.getImage());
                                    userMap.put("name", user1.getName());

                                    // Update challenge winner
                                    firebaseFirestore
                                    .collection("Challenges")
                                    .document(challengeID)
                                    .update("winner", userMap)
                                    .addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(
                                        @NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Log.d(TAG,
                                               "onComplete: " +
                                                       "Challenge winner " +
                                                     "updated");
                                            } else {

                                                Log.d(TAG,
                                                "onComplete: " +
                                                        "Error updating " +
                                                        "challenge winner");
                                            }
                                        }
                                    });
                                }

                                // Update challenge steps
                                firebaseFirestore
                                .collection("Challenges")
                                .document(challengeID)
                                .update("stepsUser1", newSteps)
                                .addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(
                                    @NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Log.d(TAG,
                                            "onComplete: " +
                                            "Challenge steps updated");
                                        } else {

                                            Log.d(TAG,
                                            "onComplete: " +
                                            "Error updating challenge steps");
                                        }
                                    }
                                });
                            } else {

                                int currentSteps = challenge.getStepsUser2();
                                int newSteps = currentSteps + stepsToAdd;
                                int stepTarget = challenge.getStepTarget();
                                if (newSteps >= stepTarget) {

                                    firebaseFirestore
                                    .collection("Challenges")
                                    .document(challengeID)
                                    .update("status", "finished")
                                    .addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(
                                        @NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Log.d(TAG,
                                                "onComplete: " +
                                                "Challenge status updated");
                                            } else {

                                                Log.d(TAG,
                                                "onComplete: Error " +
                                                "updating challenge status");
                                            }
                                        }
                                    });

                                    Map<String, Object> userMap =
                                    new HashMap<>();
                                    userMap.put("id", user2.getId());
                                    userMap.put("image", user2.getImage());
                                    userMap.put("name", user2.getName());
                                    firebaseFirestore
                                    .collection("Challenges")
                                    .document(challengeID)
                                    .update("winner", userMap)
                                    .addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(
                                        @NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Log.d(TAG,
                                                "onComplete: Challenge" +
                                                " winner updated");
                                            } else {

                                                Log.d(TAG,
                                                "onComplete: Error " +
                                                "updating challenge winner");
                                            }
                                        }
                                    });
                                }

                                firebaseFirestore
                                .collection("Challenges")
                                .document(challengeID)
                                .update("stepsUser2", newSteps)
                                .addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(
                                    @NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Log.d(TAG,
                                            "onComplete: Challenge " +
                                            "steps updated");
                                        } else {

                                            Log.d(TAG,
                                            "onComplete: Error updating" +
                                            " challenge steps");
                                        }
                                    }
                                });
                            }
                        }
                    }
                } else {

                    Log.d(TAG, "onComplete: Error getting challenge");
                }
            }
        });
    }

    /**
     * @author Mario Kiese.
     * @version 0.9.
     * This class is needed to return the ChallengeStepCounterService
     *
     * @see ChallengeStepCounterService
     *
     */
    public class LocationBinder extends Binder {

        public ChallengeStepCounterService getService() {

            return ChallengeStepCounterService.this;
        }
    }
}
