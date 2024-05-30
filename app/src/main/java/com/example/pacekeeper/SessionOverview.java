package com.example.pacekeeper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionOverview#newInstance} factory method to
 * create an instance of this fragment.
 */

/**
 * This class creates the session overview fragment and determines
 * the actions of all the buttons.
 * @author Jonathan, Johnny, Samuel
 */
public class SessionOverview extends Fragment {
    private SessionManager sessionManager;
    private Session currentSession;
    private ImageButton saveSession;
    private ImageButton resumeSession;
    private ImageButton deleteSession;
    private TextView sessionSummary;
    private TextView sessionDistance;
    private TextView timePerKm;
    private EditText editComment;
    private ImageView routeImage;
    private MapGenerator mapGenerator;


    public SessionOverview() {
        // Required empty public constructor
    }

    /**
     *The new instance method is used to send data between fragments when creating a fragment.
     * @return A new instance of fragment SessionOverview.
     * @author Jonathan
     */

    public static SessionOverview newInstance(Session currentSession, SessionManager sessionManager) {
        SessionOverview fragment = new SessionOverview();
        fragment.setCurrentSession(currentSession);
        fragment.setSessionManager(sessionManager);
        fragment.createMapGenerator();

        return fragment;
    }

    public void createMapGenerator(){
        mapGenerator = new MapGenerator();
    }

    /**
     * Method which determines the actions of the application when it's reopened.
     * @author Johnny
     */
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

    /**
     * The on create view populates all elements in the fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the rootview, i.e. the fragment created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_session_overview, container, false);

        initializeGraphicalResources(rootView);

        sessionSummary.setText(currentSession.getSessionDate() + " | " + currentSession.getTotalSessionTime());
        sessionDistance.setText(currentSession.getFormattedDistance());
        StringBuilder allKmTime = new StringBuilder();
        if (currentSession.getTimePerKm() != null) {
            for (int i = 0; i < currentSession.getTimePerKm().size(); i++) {
                allKmTime.append(" km ").append(i + 1).append(" ┃ ").append(currentSession.getTimePerKm().get(i)).append("\n");
            }
            timePerKm.setText(allKmTime);
        }

        Glide.with(this).load(mapGenerator.getUrl(getString(R.string.mapbox_access_token),currentSession)).into(routeImage);


        initializeEventListeners();

        return rootView;
    }

    /**
     * Separate method to initialize the elements from the fragment.
     * @param rootView the fragment which the elements is fetched from.
     * @author Jonathan, Samuel
     */
    public void initializeGraphicalResources(View rootView) {
        saveSession = rootView.findViewById(R.id.save_session_button);
        resumeSession = rootView.findViewById(R.id.resume_session_button);
        deleteSession = rootView.findViewById(R.id.delete_session_button);
        sessionSummary = rootView.findViewById(R.id.summary_text_view1);
        sessionDistance = rootView.findViewById(R.id.session_distance);
        timePerKm = rootView.findViewById(R.id.detail_text_view_km);
        editComment = rootView.findViewById(R.id.edit_comment);
        routeImage = rootView.findViewById(R.id.route_image);
    }

    /**
     * The method which initializes the eventlisteners for the elements in the
     * fragment_session_overview.
     * @author Jonathan, Samuel, Johnny
     */
    public void initializeEventListeners() {
        saveSession.setOnClickListener(v -> {
            if(!String.valueOf(editComment.getText()).equals("Add text here.")){
                currentSession.setSessionComment(String.valueOf(editComment.getText()));
            }
            sessionManager.add(currentSession.getSerializableSession());
            sessionManager.storeSessionToMemory(getContext());
            currentSession.killSession();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(saveSession.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            getActivity().getSupportFragmentManager().popBackStack(
                    "mainActivity",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            stopService();
        });

        editComment.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus && editComment.getText().toString().equals("Add text here.")){
                editComment.setText("");
            }
        });

        resumeSession.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(resumeSession.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            getActivity().getSupportFragmentManager().popBackStack(
                    "runnerView",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            hideNavigationBar();
        });

        deleteSession.setOnClickListener(v -> {
            currentSession.killSession();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(deleteSession.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            getActivity().getSupportFragmentManager().popBackStack(
                    "mainActivity",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            stopService();
        });
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

    /**
     * Method for hiding the navigation bar
     * @author Johnny
     */
    private void hideNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int hideNavigation = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        int immersive = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(hideNavigation | immersive);
    }

    /**
     * Method for displaying the navigation bar
     * @author Johnny
     */
    private void displayNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int visible = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(visible);
    }
}