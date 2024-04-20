package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    ImageButton compressButton;
    ImageButton expandButton;
    TextView sessionComment;
    TextView sessionCommentTitle;
    ImageView routeAsImage;

    Button saveCommentButton;
    EditText editCommentText;
    ImageButton editCommentIcon;

    TextView sessionOverview;
    TextView allKmInSession;
    TextView routeText;


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

        View rootView = inflater.inflate(R.layout.fragment_session, container, false);
        ImageButton returnButton = rootView.findViewById(R.id.return_button);
        sessionContainer = rootView.findViewById(R.id.session_layout);

        View sessionView = LayoutInflater.from(getContext()).inflate(R.layout.session_item, null);

         saveCommentButton = sessionView.findViewById(R.id.save_comment_button);
        editCommentText = sessionView.findViewById(R.id.edit_comment);
         editCommentIcon = sessionView.findViewById(R.id.edit_comment_icon);

         sessionOverview = sessionView.findViewById(R.id.summary_text_view1);
         allKmInSession = sessionView.findViewById(R.id.detail_text_view_km);
        routeText = sessionView.findViewById(R.id.detail_text_view_route);
        routeAsImage = sessionView.findViewById(R.id.route_image);
        sessionCommentTitle = sessionView.findViewById(R.id.detail_text_view_session_comment_title);
        sessionComment = sessionView.findViewById(R.id.detail_text_view_session_comment_text);
        expandButton = sessionView.findViewById(R.id.expand_button);
        compressButton = sessionView.findViewById(R.id.compress_button);

        uiPopulation = new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        };

        Thread thread = new Thread(uiPopulation);
        thread.start();

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.storeSessionToMemory(requireContext());
                requireActivity().onBackPressed();
            }
        });

        return rootView;
    }

    private void updateUI() {
        List<Session.StoredSession> sessionsList = sessionManager.getSavedSessions();

        if(sessionsList!=null) {
            for (Session.StoredSession session : sessionsList) {

                String formattedDistance = String.format(Locale.forLanguageTag("Swedish"), "%.1f", session.getTotalDistance() / 1000);

                sessionOverview.setText(session.getDate() + "|" + session.getTotalTime() + "|" + formattedDistance + " km");
                StringBuilder allKmTime = new StringBuilder();
                if (session.getTimePerKm() != null) {
                    for (int i = 0; i < session.getTimePerKm().size(); i++) {
                        allKmTime.append("km ").append(i + 1).append(" ").append(session.getTimePerKm().get(i)).append("\n ");
                    }
                    allKmInSession.setText(allKmTime);
                }
                if (session.getSessionComment() != null) {
                    sessionComment.setText(session.getSessionComment());
                }

                // routeText.setText(session.getRoute());
                // routeAsImage.setImageResource(session.getRouteImage());


                expandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.GONE) {
                            editCommentIcon.setVisibility(View.VISIBLE);
                            allKmInSession.setVisibility(View.VISIBLE);
                            routeText.setVisibility(View.VISIBLE);
                            routeAsImage.setVisibility(View.VISIBLE);
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
                            routeText.setVisibility(View.GONE);
                            routeAsImage.setVisibility(View.GONE);
                            sessionCommentTitle.setVisibility(View.GONE);
                            sessionComment.setVisibility(View.GONE);
                            expandButton.setVisibility(View.VISIBLE);
                            compressButton.setVisibility(View.GONE);
                        }
                    }
                });

                editCommentIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editCommentText.setVisibility(View.VISIBLE);
                        saveCommentButton.setVisibility(View.VISIBLE);
                    }
                });

                saveCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        session.setSessionComment(String.valueOf(editCommentText.getText()));
                        editCommentText.setVisibility(View.GONE);
                        saveCommentButton.setVisibility(View.GONE);

                    }
                });


                sessionContainer.addView(sessionView);
            }
        }
    }


    public void setSessionManager(SessionManager sessionManager){
        if(this.sessionManager == null && sessionManager != null){
            this.sessionManager = sessionManager;
        }
    }



}