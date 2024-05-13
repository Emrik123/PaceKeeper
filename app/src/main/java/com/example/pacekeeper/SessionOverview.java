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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionOverview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionOverview extends Fragment {

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
        fragment.setCurrentSession(currentSession);
        fragment.setSessionManager(sessionManager);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayNavigationBar();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_session_overview, container, false);

        ImageButton saveSession = rootView.findViewById(R.id.save_session_button);
        ImageButton resumeSession = rootView.findViewById(R.id.resume_session_button);
        ImageButton deleteSession = rootView.findViewById(R.id.delete_session_button);
        TextView sessionSummary = rootView.findViewById(R.id.summary_text_view1);
        TextView sessionDistance = rootView.findViewById(R.id.session_distance);
        TextView timePerKm = rootView.findViewById(R.id.detail_text_view_km);
        EditText editComment = rootView.findViewById(R.id.edit_comment);

        sessionSummary.setText(currentSession.getSessionDate() + " | " + currentSession.getTotalSessionTime());
        sessionDistance.setText(currentSession.getFormattedDistance());
        StringBuilder allKmTime = new StringBuilder();
        if (currentSession.getTimePerKm() != null) {
            for (int i = 0; i < currentSession.getTimePerKm().size(); i++) {
                allKmTime.append(" km ").append(i + 1).append(" ┃ ").append(currentSession.getTimePerKm().get(i)).append("\n");
            }
            timePerKm.setText(allKmTime);
        }


        saveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!String.valueOf(editComment.getText()).equals("Add text here.")){
                    currentSession.setSessionComment(String.valueOf(editComment.getText()));
                }
                sessionManager.add(currentSession.getSerializableSession());
                sessionManager.storeSessionToMemory(getContext());
                currentSession.killSession();

                getActivity().getSupportFragmentManager().popBackStack(
                    "mainActivity",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);

                stopService();
            }
        });

        editComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && editComment.getText().toString().equals("Add text here.")){
                    editComment.setText("");
                }
            }
        });

        resumeSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack(
                        "runnerView",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                hideNavigationBar();
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

    private void hideNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int hideNavigation = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        int immersive = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(hideNavigation | immersive);
    }
    private void displayNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int visible = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(visible);
    }
}