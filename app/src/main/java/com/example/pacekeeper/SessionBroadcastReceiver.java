package com.example.pacekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.location.LocationResult;

public class SessionBroadcastReceiver extends BroadcastReceiver {

    private Session session;

    public SessionBroadcastReceiver(Session session){
        this.session = session;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LocationResult result = bundle.getParcelable("loc");
        float[] a = bundle.getFloatArray("accel");
        session.updateLocation(result, a);
        System.out.println("Received message");
//        if(session == null){
//            session = RunnerView.currentSession;
//        }else{
//            session.updateLocation(result, a);
//        }
//        if(session != null){
//            session.updateLocation(result, a);
//        }
    }

    public void setSession(Session session){
        this.session = session;
    }
}