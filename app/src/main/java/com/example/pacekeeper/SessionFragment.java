package com.example.pacekeeper;

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

    public SessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SessionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionFragment newInstance(String param1, String param2) {
        SessionFragment fragment = new SessionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_session, container, false);

        ImageButton returnButton = rootView.findViewById(R.id.return_button);
        ImageButton expandButton =rootView.findViewById(R.id.expand_button);
        TextView allKmInSession = rootView.findViewById(R.id.detail_text_view_km);
        TextView routeText = rootView.findViewById(R.id.detail_text_view_route);
        ImageView routeAsImage = rootView.findViewById(R.id.route_image);
        TextView sessionCommentTitle = rootView.findViewById(R.id.detail_text_view_session_comment_title);
        TextView sessionComment = rootView.findViewById(R.id.detail_text_view_session_comment_text);
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

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        return rootView;
    }
}