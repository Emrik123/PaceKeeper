package com.example.pacekeeper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

public class SessionManager {

    private ArrayList<Session.StoredSession> sessions;
    public SessionManager(){
        sessions = new ArrayList<>();
    }


    public void add(Session.StoredSession session){
        if(sessions==null){
            sessions = new ArrayList<>();
        }
        sessions.add(session);
    }

    public ArrayList<Session.StoredSession> getSavedSessions(){
        return sessions;
    }

    public void storeSessionToMemory(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("testDataFile.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(sessions);
            oos.close();
            Toast.makeText(context, "File stored", Toast.LENGTH_SHORT).show();
            Log.i("Store session", "File successfully created and stored");
        } catch (IOException e) {
            Log.e("Store session", "Error storing file: " + e.getMessage());
        }
    }

    public void readFile(Context context) {
        try {
            FileInputStream fis = context.openFileInput("testDataFile.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            sessions = (ArrayList<Session.StoredSession>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


