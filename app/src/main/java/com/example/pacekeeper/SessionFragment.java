package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SessionManager sessionManager;
    LinearLayout sessionContainer;
    private Runnable uiPopulation;


    View sessionView;
    Handler uiHandler;


    public SessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SessionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionFragment newInstance(SessionManager sessionManager) {
        SessionFragment fragment = new SessionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setSessionManager(sessionManager);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        uiHandler = new Handler(Looper.getMainLooper());
        View rootView = inflater.inflate(R.layout.fragment_session, container, false);
        ImageButton returnButton = rootView.findViewById(R.id.return_button);
        sessionContainer = rootView.findViewById(R.id.session_layout);


        uiPopulation = new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        };
            populateUI();


        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.storeSessionToMemory(requireContext());
                requireActivity().onBackPressed();
            }
        });

        return rootView;
    }

    public void populateUI(){
        uiHandler.post(uiPopulation);

    }




    private void updateUI() {
        List<Session.StoredSession> sessionsList = sessionManager.getSavedSessions();

        if(sessionsList!=null) {
            Collections.reverse(sessionsList);
            for (Session.StoredSession session : sessionsList) {

                sessionView = LayoutInflater.from(getContext()).inflate(R.layout.session_item, null);
                LinearLayout sessionItem = sessionView.findViewById(R.id.session_layout);
                ImageButton compressButton = sessionView.findViewById(R.id.compress_button);
                ImageButton  expandButton = sessionView.findViewById(R.id.expand_button);
                ImageButton deleteSessionButton = sessionView.findViewById(R.id.delete_session);
                TextView sessionComment = sessionView.findViewById(R.id.detail_text_view_session_comment_text);
                TextView sessionCommentTitle = sessionView.findViewById(R.id.detail_text_view_session_comment_title);
                TextView sessionDistance = sessionView.findViewById(R.id.session_distance);

                Button saveCommentButton = sessionView.findViewById(R.id.save_comment_button);
                EditText editCommentText = sessionView.findViewById(R.id.edit_comment);
                ImageButton editCommentIcon = sessionView.findViewById(R.id.edit_comment_icon);

                TextView sessionOverview = sessionView.findViewById(R.id.summary_text_view1);
                TextView allKmInSession = sessionView.findViewById(R.id.detail_text_view_km);

                String formattedDistance = String.format(Locale.forLanguageTag("Swedish"), "%.1f", session.getTotalDistance() / 1000);

                sessionOverview.setText(session.getDate() + " | " + session.getTotalTime());
                sessionDistance.setText(formattedDistance + " km");
                StringBuilder allKmTime = new StringBuilder();
                if (session.getTimePerKm() != null) {
                    for (int i = 0; i < session.getTimePerKm().size(); i++) {
                        allKmTime.append(" km ").append(i + 1).append(" ┃ ").append(session.getTimePerKm().get(i)).append("\n");
                    }
                    allKmInSession.setText(allKmTime);
                }
                if (session.getSessionComment() != null) {
                    sessionComment.setText(session.getSessionComment());
                }




                expandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.GONE) {
                            editCommentIcon.setVisibility(View.VISIBLE);
                            allKmInSession.setVisibility(View.VISIBLE);
                            sessionCommentTitle.setVisibility(View.VISIBLE);
                            if (session.getSessionComment() != null) {
                                sessionComment.setVisibility(View.VISIBLE);
                            }
                            expandButton.setVisibility(View.GONE);
                            compressButton.setVisibility(View.VISIBLE);
                        }
                    }
                });

                compressButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.VISIBLE) {
                            allKmInSession.setVisibility(View.GONE);
                            sessionCommentTitle.setVisibility(View.GONE);
                            sessionComment.setVisibility(View.GONE);
                            expandButton.setVisibility(View.VISIBLE);
                            compressButton.setVisibility(View.GONE);
                            editCommentText.setVisibility(View.GONE);
                            saveCommentButton.setVisibility(View.GONE);
                            editCommentIcon.setVisibility(View.GONE);
                        }
                    }
                });

                editCommentIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editCommentText.setVisibility(View.VISIBLE);
                        if(session.getSessionComment()!=null){
                            editCommentText.setText(session.getSessionComment());
                            sessionComment.setVisibility(View.GONE);
                        }
                        saveCommentButton.setVisibility(View.VISIBLE);
                        editCommentIcon.setVisibility(View.GONE);
                    }
                });

                saveCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        session.setSessionComment(String.valueOf(editCommentText.getText()));
                        sessionComment.setText(editCommentText.getText());
                        sessionComment.setVisibility(View.VISIBLE);
                        editCommentText.setVisibility(View.GONE);
                        saveCommentButton.setVisibility(View.GONE);

                    }
                });
                View spacerView = new View(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20);
                spacerView.setLayoutParams(params);

                deleteSessionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sessionManager.remove(session);
                        sessionItem.setVisibility(View.GONE);
                        sessionContainer.removeView(spacerView);
                    }
                });

                sessionItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.GONE) {
                            editCommentIcon.setVisibility(View.VISIBLE);
                            allKmInSession.setVisibility(View.VISIBLE);
                            sessionCommentTitle.setVisibility(View.VISIBLE);
                            if (session.getSessionComment() != null) {
                                sessionComment.setVisibility(View.VISIBLE);
                            }
                            expandButton.setVisibility(View.GONE);
                            compressButton.setVisibility(View.VISIBLE);
                        } else{
                            allKmInSession.setVisibility(View.GONE);
                            sessionCommentTitle.setVisibility(View.GONE);
                            sessionComment.setVisibility(View.GONE);
                            expandButton.setVisibility(View.VISIBLE);
                            compressButton.setVisibility(View.GONE);
                            editCommentText.setVisibility(View.GONE);
                            saveCommentButton.setVisibility(View.GONE);
                            editCommentIcon.setVisibility(View.GONE);
                        }
                    }
                });

                editCommentText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus && editCommentText.getText().toString().equals("Add text here.")){
                            editCommentText.setText("");
                        }
                    }
                });




                sessionContainer.addView(sessionView);
                sessionContainer.addView(spacerView);


            }

        }

    }




    public void setSessionManager(SessionManager sessionManager){
        if(this.sessionManager == null && sessionManager != null){
            this.sessionManager = sessionManager;
        }
    }



}