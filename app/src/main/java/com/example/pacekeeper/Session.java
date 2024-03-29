package com.example.pacekeeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Session implements Serializable {
    private String date;
    private List<Integer> kmTimes = new ArrayList<>();

    public Session(String date, int...kmTime){
        this.date = date;
        for(int time: kmTime){
            kmTimes.add(time);
        }
    }

    @Override
    public String toString(){
        return date;
    }
}
