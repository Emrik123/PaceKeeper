package com.example.pacekeeper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText speedInput;
    private TextView viewSpeed;
    private TextView viewStatus;
    private TextView currentActivity;
    private Button confirm;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location location;
    private double currentSpeed;
    private double speed;
    private List<ActivityTransition> transitions = new ArrayList<>();
    private ActivityTransitionRequest transitionRequest;
    //private PendingIntent myPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    //private PendingIntent myPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, );

    private TransitionsReceiver mTransitionsReceiver;
    private PendingIntent mActivityTransitionsPendingIntent;
    private boolean activityTrackingEnabled = false;
    private final boolean runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSpeed = 0;
        speedInput = findViewById(R.id.speedInput);
        confirm = findViewById(R.id.confirmButton);
        viewSpeed = findViewById(R.id.speedText);
        viewStatus = findViewById(R.id.viewStatus);
        currentActivity = findViewById(R.id.current_activity);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = speedInput.getText().toString();
                if (!input.isEmpty()) {
                    speed = Double.parseDouble(input);
                    Toast.makeText(MainActivity.this, "Speed stored.", Toast.LENGTH_SHORT).show();
                    start();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a valid speed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        /*Intent startIntent = new Intent(this, PermissionRationalActivity.class);
        startActivityForResult(startIntent, 0);*/

        mTransitionsReceiver = new TransitionsReceiver();

        Intent intent = new Intent("TRANSITIONS_RECEIVER_ACTION");
        mActivityTransitionsPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                currentSpeed = location.getSpeed();
                updateUI();
            }
        };
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mTransitionsReceiver, new IntentFilter("TRANSITIONS_RECEIVER_ACTION"));
    }

    @Override
    protected void onPause() {
        if (activityTrackingEnabled) {
            disableActivityTransitions();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mTransitionsReceiver);

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Start activity recognition if the permission was approved.
        if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
            enableActivityTransitions();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void enableActivityTransitions() {

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            return;
        }

        Task<Void> task = ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, mActivityTransitionsPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                activityTrackingEnabled = true;
                // Success should be logged?

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                // Failure should be logged?
            }
        });
    }

    private void disableActivityTransitions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            return;
        }

        Task<Void> task = ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mActivityTransitionsPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                activityTrackingEnabled = false;
                // Log
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log
            }
        });
    }

    private boolean activityRecognitionPermissionApproved() {

        // permission check for 29+.
        if (runningQOrLater) {

            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }


    private void start() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    //TODO idle

    public void updateUI(){
        currentSpeed = currentSpeed * 3.6;
        int roundedSpeed = (int) currentSpeed;
        String s1 = Double.toString(roundedSpeed);
        String s2 = getString(R.string.viewString);
        String s3 = getString(R.string.viewString2);
        viewStatus.setText(s1);
        //viewSpeed.setText(s2 + s1 + s3);
        //Toast.makeText(MainActivity.this,Double.toString(currentSpeed), Toast.LENGTH_SHORT).show();
        if(roundedSpeed == speed || (roundedSpeed >= speed -1 && roundedSpeed <= speed +1)){
            viewSpeed.setText(getString(R.string.reachedSpeed));
            viewStatus.setTextColor(Color.parseColor("green"));
        }else if(roundedSpeed > speed+1){
            viewSpeed.setText(getString(R.string.tooFast));
            viewStatus.setTextColor(Color.parseColor("red"));
        }else if(roundedSpeed < speed-1){
            viewSpeed.setText(getString(R.string.tooSlow));
            viewStatus.setTextColor(Color.parseColor("blue"));
        }
    }


    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            default:
                return "UNKNOWN";
        }
    }

    public class TransitionsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ActivityTransitionResult.hasResult(intent)) {

                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);

                if (result != null) {
                    for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                        String activityString = toActivityString(event.getActivityType());
                        currentActivity.setText(activityString);

                    }
                }

            }
        }
    }
}