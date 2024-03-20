package com.example.pacekeeper;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {
    private EditText speedInput;
    private TextView viewSpeed;
    private TextView viewStatus;
    private Button confirm;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location location;
    private double currentSpeed;
    private double speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSpeed = 0;
        speedInput = findViewById(R.id.speedInput);
        confirm = findViewById(R.id.confirmButton);
        viewSpeed = findViewById(R.id.speedText);
        viewStatus = findViewById(R.id.viewStatus);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
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
                location = locationResult.getLastLocation();
                currentSpeed = location.getSpeed();
                updateUI();
            }
        };
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

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
}