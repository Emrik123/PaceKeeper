package com.example.pacekeeper;

import java.util.ArrayList;

public class SessionManager {

    private ArrayList<Session> sessions;
    public SessionManager(){
        sessions = new ArrayList<>();
    }


    public void add(Session session){
        sessions.add(session);
    }

    public ArrayList<Session> getSavedSessions(){
        return sessions;
    }
}
