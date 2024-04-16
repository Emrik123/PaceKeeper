package com.example.pacekeeper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
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
        sessions.add(session);
    }

    public ArrayList<Session.StoredSession> getSavedSessions(){
        return sessions;
    }

    public void storeSessionToMemory(Context context) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "testDataFile.dat");
            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
            oos.writeObject(sessions);
            oos.flush();
            oos.close();
            Toast.makeText(context, "File stored", Toast.LENGTH_SHORT).show();
            Log.i("Store session", "File successfully created and stored in: " + file.getPath());
        } catch (IOException e) {
            Log.e("Store session", "Error storing file: " + e.getMessage());
        }
    }

    public void readFile() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "testDataFile.dat");
            ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(file.getAbsolutePath())));
            sessions = (ArrayList<Session.StoredSession>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


