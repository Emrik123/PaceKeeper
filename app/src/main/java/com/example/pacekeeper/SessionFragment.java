package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

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
        LinearLayout sessionContainer = rootView.findViewById(R.id.session_layout);

        List<Session> sessionsList = sessionManager.getSavedSessions();

        for (Session session : sessionsList) {
            View sessionView = LayoutInflater.from(getContext()).inflate(R.layout.session_item, null);

            TextView sessionOverview = sessionView.findViewById(R.id.summary_text_view1);
            TextView allKmInSession = sessionView.findViewById(R.id.detail_text_view_km);
            TextView routeText = sessionView.findViewById(R.id.detail_text_view_route);
            ImageView routeAsImage = sessionView.findViewById(R.id.route_image);
            TextView sessionCommentTitle = sessionView.findViewById(R.id.detail_text_view_session_comment_title);
            TextView sessionComment = sessionView.findViewById(R.id.detail_text_view_session_comment_text);
            ImageButton expandButton = sessionView.findViewById(R.id.expand_button);

            sessionOverview.setText(session.getSessionDate() + "|" + session.getTotalSessionTime() + "|" + (int) session.getDistance() + " km");
            StringBuilder allKmTime = new StringBuilder();
            for(int i=0; i<session.getTimePerKm().size(); i++){
               allKmTime.append("km ").append(i + 1).append(" ").append(session.getTimePerKm().get(i)).append("\n ");
            }
            allKmInSession.setText(allKmTime);
            
            
           // routeText.setText(session.getRoute());
           // routeAsImage.setImageResource(session.getRouteImage());
           // sessionComment.setText(session.getCommentText());
            

            expandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allKmInSession.getVisibility() == View.VISIBLE) {
                        allKmInSession.setVisibility(View.GONE);
                        routeText.setVisibility(View.GONE);
                        routeAsImage.setVisibility(View.GONE);
                        sessionCommentTitle.setVisibility(View.GONE);
                        sessionComment.setVisibility(View.GONE);
                    } else {
                        allKmInSession.setVisibility(View.VISIBLE);
                        routeText.setVisibility(View.VISIBLE);
                        routeAsImage.setVisibility(View.VISIBLE);
                        sessionCommentTitle.setVisibility(View.VISIBLE);
                        sessionComment.setVisibility(View.VISIBLE);
                    }
                }
            });

            sessionContainer.addView(sessionView);
        }

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        return rootView;
    }


    public void setSessionManager(SessionManager sessionManager){
        if(sessionManager==null){
            sessionManager = new SessionManager();
        }
        this.sessionManager = sessionManager;
    }
}