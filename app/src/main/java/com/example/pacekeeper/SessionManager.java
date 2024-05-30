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

/**
 * Class responsible for adding and removing sessions from the list of sessions.
 * The list of sessions is then stored in the internal storage when a session is saved or modified.
 * @author Jonathan
 */

public class SessionManager {
    private ArrayList<Session.StoredSession> sessions;
    public SessionManager(){
        sessions = new ArrayList<>();
    }

    /**
     * Method for adding a session in the list of sessions.
     * @param session the session which is added.
     */
    public void add(Session.StoredSession session){
        if(sessions==null){
            sessions = new ArrayList<>();
        }
        sessions.add(session);
    }

    /**
     * Method for removing a session from the list of sessions.
     * @param session the session to remove.
     */
    public void remove(Session.StoredSession session){
        sessions.remove(session);
    }

    public ArrayList<Session.StoredSession> getSavedSessions(){
        return sessions;
    }

    /**
     * This method stores the list of sessions to the internal memory.
     * @param context the context to which the file should be saved.
     */

    public void storeSessionToMemory(Context context) {
        try {
                FileOutputStream fos = context.openFileOutput("testDataFile.dat", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(sessions);
                oos.close();
                Log.i("Store session", "File successfully created and stored");

        } catch (IOException e) {
            Log.e("Store session", "Error storing file: " + e.getMessage());
        }
    }

    /**
     * This method sets the session list with the sessions stored in the internal memory.
     * @param context the context from which the data is fetched.
     */

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


