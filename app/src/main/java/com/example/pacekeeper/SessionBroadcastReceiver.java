package com.example.pacekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.location.LocationResult;

/**
 * Class for receiving broadcasts from the SensorUnitHandler
 */
public class SessionBroadcastReceiver extends BroadcastReceiver {

    private Session session;

    /**
     * Constructor
     * @param session, session reference for updating its data
     * @author Emrik
     */
    public SessionBroadcastReceiver(Session session) {
        this.session = session;
    }

    /**
     * Method called when a broadcast has been received, updates the location
     * in current session.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     * @author Emrik
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LocationResult result = bundle.getParcelable("loc");
        float[] a = bundle.getFloatArray("accel");
        session.updateLocation(result, a);
    }

    public void setSession(Session session) {
        this.session = session;
    }
}