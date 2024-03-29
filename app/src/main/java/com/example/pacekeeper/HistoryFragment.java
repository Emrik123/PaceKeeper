package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
public class HistoryFragment extends Fragment {

    private TextView textView;
    private Button saveButton;
    private Button loadButton;
    private List<Session> sessionList = new ArrayList<>();
    private List<Session> sessionListRead = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_history, container, false);
        textView = view.findViewById(R.id.textView);
        loadButton = view.findViewById(R.id.load);
        saveButton = view.findViewById(R.id.save);

        sessionList.add(new Session("10/1", 2));
        sessionList.add(new Session("11/1", 3,3,4,5));
        sessionList.add(new Session("12/1", 4,3,4,5));
        sessionList.add(new Session("13/1", 5,3,4,5));

        saveButton.setOnClickListener(v -> saveFile("list.dat", sessionList));
        loadButton.setOnClickListener(v -> loadFile("list.dat"));

        return view;
    }

    public void loadFile(String fileName) {
        File path = getActivity().getApplicationContext().getFilesDir();
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path, fileName)))){
            sessionListRead = (ArrayList<Session>)ois.readObject();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String textViewText = new String();
        for(Session session : sessionListRead){
            textViewText += session.toString();
        }
        textView.setText(textViewText);
    }
    public void saveFile(String fileName, List<Session> arrayList){
        File path = getActivity().getApplicationContext().getFilesDir();
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path, fileName)))){
            oos.writeObject(arrayList);
        }
        catch (IOException e){
        }
    }
}