package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {
    private EditText speedInput;
    private TextView speedDisplay;
    private TextView statusDisplay;
    private TextView distanceDisplay;
    private Button confirm;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location location;
    private double currentSpeed;
    private double speed;
    private double distance;
    private Vibrator vibrator;
    private VibrationEffect increaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{150,75,150,75,150},new int[]{255,0,255,0,255},-1); //Creates Vibration pattern for being too slow
    private VibrationEffect decreaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{900},new int[]{255},-1); //Creates Vibration pattern for being too fast


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSpeed = 0;
        speedInput = findViewById(R.id.leftNPicker);
        confirm = findViewById(R.id.confirmButton);
       // speedDisplay = findViewById(R.id.speedDisplay);
       // statusDisplay = findViewById(R.id.statusDisplay);
       // distanceDisplay = findViewById(R.id.distanceDisplay);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE); //Assigns vibrator to local variable
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = speedInput.getText().toString();
                if(!input.isEmpty()){
                    speed = Double.parseDouble(input);
                    Toast.makeText(MainActivity.this, "Speed stored." , Toast.LENGTH_SHORT).show();
                    start();
                }else{
                    Toast.makeText(MainActivity.this, "Please enter a valid speed." , Toast.LENGTH_SHORT).show();
                }
            }
        });

        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(location != null){
                    distance = distance + location.distanceTo(locationResult.getLastLocation());
                }
                location = locationResult.getLastLocation();
                currentSpeed = location.getSpeed();
                updateUI();
            }
        };
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            BlankFragment blankFragment = new BlankFragment();
            return;

        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        int roundedDistance = (int) distance;
        distanceDisplay.setText(Integer.toString(roundedDistance));
        currentSpeed = currentSpeed * 3.6;
        int roundedSpeed = (int) currentSpeed;
        String s1 = Double.toString(roundedSpeed);
        String s2 = getString(R.string.viewString);
        String s3 = getString(R.string.viewString2);
        speedDisplay.setText(s1);
        //viewSpeed.setText(s2 + s1 + s3);
        //Toast.makeText(MainActivity.this,Double.toString(currentSpeed), Toast.LENGTH_SHORT).show();
        if(roundedSpeed == speed || (roundedSpeed >= speed -1 && roundedSpeed <= speed +1)){
            statusDisplay.setText(getString(R.string.reachedSpeed));
            speedDisplay.setTextColor(Color.parseColor("green"));
        }else if(roundedSpeed > speed+1){
            statusDisplay.setText(getString(R.string.tooFast));
            speedDisplay.setTextColor(Color.parseColor("red"));
            vibrator.vibrate(decreaseSpeedVibrationPattern); //Calls for the vibrator to vibrate according to the decreaseSpeedVibrationPattern
        }else if(roundedSpeed < speed-1){
            statusDisplay.setText(getString(R.string.tooSlow));
            speedDisplay.setTextColor(Color.parseColor("blue"));
            vibrator.vibrate(increaseSpeedVibrationPattern); //Calls for the vibrator to vibrate according to the increaseSpeedVibrationPattern
        }
    }
    public void scroll(){
      
        

    }
}