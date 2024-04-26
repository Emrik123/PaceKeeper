package com.example.pacekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.location.LocationResult;

public class TestBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("received in broadcast receiver");
        Bundle bundle = intent.getExtras();
        LocationResult result = bundle.getParcelable("loc");
        System.out.println(result.toString());
    }
}
