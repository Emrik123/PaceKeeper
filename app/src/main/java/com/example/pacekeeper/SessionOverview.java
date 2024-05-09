package com.example.pacekeeper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionOverview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionOverview extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2;


    private SessionManager sessionManager;
    private Session currentSession;

    public SessionOverview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SessionOverview.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionOverview newInstance(Session currentSession, SessionManager sessionManager) {
        SessionOverview fragment = new SessionOverview();
        Bundle args = new Bundle();
        fragment.setCurrentSession(currentSession);
        fragment.setSessionManager(sessionManager);
        return fragment;
    }

    public void setSessionManager(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }
    public void setCurrentSession(Session currentSession){
        this.currentSession = currentSession;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_session_overview, container, false);

        ImageButton saveSession = rootView.findViewById(R.id.save_session_button);
        ImageButton resumeSession = rootView.findViewById(R.id.resume_session_button);
        ImageButton deleteSession = rootView.findViewById(R.id.delete_session_button);


        saveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.add(currentSession.getSerializableSession());
                sessionManager.storeSessionToMemory(getContext());
                currentSession.killSession();
               // getParentFragmentManager().popBackStackImmediate();

                getActivity().getSupportFragmentManager().popBackStack(
                    "mainActivity",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                stopService();
            }
        });

        resumeSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack(
                        "runnerView",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        deleteSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSession.killSession();
                getActivity().getSupportFragmentManager().popBackStack(
                        "mainActivity",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                stopService();
            }
        });


        return rootView;
    }

    /**
     * Method used for stopping the foregroundService.
     * @author Johnny
     */
    public void stopService() {
        Intent intent = new Intent(getContext(), SensorUnitHandler.class);
        intent.setAction("STOP");
        requireContext().startForegroundService(intent);
    }
}