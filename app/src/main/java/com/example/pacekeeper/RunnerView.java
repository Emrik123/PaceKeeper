package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunnerView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunnerView extends Fragment {
    private MainActivity mainActivity;
    private SessionManager sessionManager;
    private TextView speedDisplay;
    private TextView timeDisplay;
    private TextView distanceDisplay;
    private ImageButton pauseButton;
    private ImageButton resumeButton;
    private ImageButton stopButton;
    private ImageButton settingsButton;
    private double selectedPace;
    private final double LOWEST_SPEED_THRESHOLD = 0.5;
    private final String START_SESSION ="START";
    private Session currentSession;
    private FeedbackHandler feedbackHandler;
    private UnitOfVelocity unitOfVelocity;
    private boolean autosaveSession;
    private FragmentManager fragmentManager;
    private Handler interfaceUpdateHandler;
    private Runnable uiUpdates;
    private TextView selectedPaceDisplay;
    private ImageView speedCircle;
    private Drawable slowCircle;
    private Drawable fastCircle;
    private Drawable goodSpeedCircle;
    private Intent serviceIntent;
    private Context context;
    private TextView unitOfVelocityDisplay;


    public static RunnerView newInstance(MainActivity mainActivity, double selectedPace, boolean autoSaveSession) {
        RunnerView fragment = new RunnerView();
        Bundle args = new Bundle();
        args.putDouble("speed", selectedPace);
        fragment.setArguments(args);
        fragment.setMainActivity(mainActivity);
        fragment.setAutosaveSession(autoSaveSession);
        return fragment;
    }

    /**
     * OnCreate method, called when creating the fragment,
     * initializes graphical elements and starts a session.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     *
     * @author Jonathan, Samuel
     */
    @SuppressLint({"VisibleForTests", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        context = container.getContext();
        initializeGraphicalResources(rootView);
        fragmentManager = mainActivity.getSupportFragmentManager();
        interfaceUpdateHandler = new Handler(Looper.getMainLooper());

        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            feedbackHandler = (FeedbackHandler) intent.getSerializableExtra("feedbackHandler");
            unitOfVelocity = (UnitOfVelocity) intent.getSerializableExtra("unitOfVelocity");
        }

        Bundle args = getArguments();
        if (args != null) {
            selectedPace = args.getDouble("speed", 0);
        }

        if (mainActivity != null) {
            sessionManager = mainActivity.getSessionManager();
        }

        initializeEventListeners();
        initializeUiUpdates();
        start();
        setInitialState();
        return rootView;
    }

    /**
     * Instantiates resources tied to the inflated XML.
     * @param rootView The root View of the inflated hierarchy.
     *
     * @author Samuel
     */
    public void initializeGraphicalResources(View rootView) {
        timeDisplay = rootView.findViewById(R.id.time);
        speedDisplay = rootView.findViewById(R.id.speedDisplay);
        distanceDisplay = rootView.findViewById(R.id.distanceDisplay);
        pauseButton = rootView.findViewById(R.id.pauseButtonLogo);
        resumeButton = rootView.findViewById(R.id.playButtonLogo);
        stopButton = rootView.findViewById(R.id.stopButtonLogo);
        settingsButton = rootView.findViewById(R.id.settingsButton);
        speedCircle = rootView.findViewById(R.id.speed_circle);
        selectedPaceDisplay = rootView.findViewById(R.id.desired_speed_text);
        slowCircle = ContextCompat.getDrawable(requireContext(), R.drawable.circle);
        fastCircle = ContextCompat.getDrawable(requireContext(), R.drawable.redcircle);
        goodSpeedCircle = ContextCompat.getDrawable(requireContext(), R.drawable.greencircle);
        unitOfVelocityDisplay = rootView.findViewById(R.id.unit_of_velocity);
    }

    /**
     * Initializes event listeners for interactable elements such as buttons.
     *
     * @author Samuel
     */
    public void initializeEventListeners() {
        pauseButton.setOnClickListener(v -> {
            pauseButton.setVisibility(View.INVISIBLE);
            resumeButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            currentSession.pauseSession();
            feedbackHandler.stopFeedback();
        });

        resumeButton.setOnClickListener(v -> {
            pauseButton.setVisibility(View.VISIBLE);
            resumeButton.setVisibility(View.INVISIBLE);
            stopButton.setVisibility(View.INVISIBLE);
            settingsButton.setVisibility(View.INVISIBLE);
            currentSession.continueSession();
            feedbackHandler.startFeedback(currentSession.getSelectedSpeed());
            hideNavigationBar();
        });

        settingsButton.setOnClickListener(v -> displaySettingsView());

        stopButton.setOnClickListener(v -> {
            feedbackHandler.stopFeedback();
            if (autosaveSession) {
                sessionManager.add(currentSession.getSerializableSession());
                sessionManager.storeSessionToMemory(mainActivity);
                currentSession.killSession();
                getParentFragmentManager().popBackStackImmediate();
                serviceIntent.setAction("STOP");
                context.startForegroundService(serviceIntent);
            } else {
                displaySessionOverview();
                displayNavigationBar();
            }
        });
    }

    /**
     * Sets the graphical elements to an initial state.
     *
     * @author Samuel
     */
    @SuppressLint("SetTextI18n")
    public void setInitialState() {
        stopButton.setVisibility(View.INVISIBLE);
        resumeButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);
        selectedPaceDisplay.setText(getString(R.string.desired_pace_text)
                + currentSession.getFormattedSelectedSpeed());
        unitOfVelocityDisplay.setText(unitOfVelocity.toString());
        hideNavigationBar();
        setBackButtonBehavior();
    }

    /**
     * Creates a runnable which is passed to the queue of the Handler managing the main looper.
     *
     * @author Samuel
     */
    public void initializeUiUpdates() {
        uiUpdates = new Runnable() {
            @Override
            public void run() {
                updateUI();
                interfaceUpdateHandler.postDelayed(this, 250);
            }
        };
    }

    /**
     * onResume method, called when the runnerView is focused
     * calls for the UI to be updated and sets application behaviour
     * @author Johnny
     */
    @Override
    public void onResume() {
        super.onResume();
        runUiUpdates();
        hideNavigationBar();
        setBackButtonBehavior();
    }

    /**
     * Called when the Fragment is no longer in focus (resumed).
     * Removes any pending UI-updates.
     *
     * @author Samuel
     */
    @Override
    public void onPause() {
        super.onPause();
        interfaceUpdateHandler.removeCallbacks(uiUpdates);
    }

    /**
     * onDestroy method, stops the text to speech upon fragment destruction
     * @author Samuel
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        feedbackHandler.removeTextToSpeech();
    }

    /**
     * Method for setting the return button on the navigation bar to minimize the application
     * when the runnerView fragment is active.
     * @author Johnny
     */
    private void setBackButtonBehavior() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainActivity.moveTaskToBack(true);
            }
        });
    }

    /**
     * Method for hiding the navigation bar to avoid accidental clicks,
     * navigation bar is displayed when swiping up from bottom of screen or down from top.
     * @author Johnny
     */
    private void hideNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int hideNavigation = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        int immersive = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(hideNavigation | immersive);
    }

    /**
     * Method for displaying the navigation bar.
     * @author Johnny
     */
    private void displayNavigationBar() {
        View decorView = requireActivity().getWindow().getDecorView();
        int visible = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(visible);
    }

    /**
     * Method for updating the elements on the GUI to display the current values
     * from a running session
     * @author Samuel, Jonathan, Emrik
     */
    @SuppressLint("SetTextI18n")
    public void updateUI() {
        if (currentSession.getRunning()) {
            distanceDisplay.setText(currentSession.getFormattedDistance());
            if (currentSession.getCurrentSpeed() > LOWEST_SPEED_THRESHOLD) {
                speedDisplay.setText(currentSession.getFormattedCurrentSpeed());
            } else {
                speedDisplay.setText(getResources().getString(R.string.null_speed));
            }
            double velocity = currentSession.getCurrentSpeed();
            final double delta = feedbackHandler.getVelocityDelta();
            double selectedVelocity = currentSession.getSelectedSpeed();
            if (velocity < selectedVelocity + delta && velocity > selectedVelocity - delta) {
                speedCircle.setBackground(goodSpeedCircle);
            } else if (velocity > selectedVelocity + delta) {
                speedCircle.setBackground(fastCircle);
            } else if (velocity < selectedVelocity - delta) {
                speedCircle.setBackground(slowCircle);
            }
            timeDisplay.setText(currentSession.updateTime());
        }
    }

    public void runUiUpdates() {
        interfaceUpdateHandler.post(uiUpdates);
    }

    /**
     * Method called when starting a new session, calls for a new session to
     * be created, a foregroundservice to be started and the feedbackhandler to give
     * feedback.
     * @author Samuel, Johnny
     */
    private void start() {
        currentSession = new Session(selectedPace, context, feedbackHandler);
        serviceIntent = new Intent(context, SensorUnitHandler.class);
        serviceIntent.setAction(START_SESSION);
        context.startForegroundService(serviceIntent);
        feedbackHandler.setRunning(currentSession.getRunning());
        feedbackHandler.setCurrentSpeed(currentSession.getCurrentSpeed());
        currentSession.setUnitOfVelocity(unitOfVelocity);
        feedbackHandler.startFeedback(currentSession.getSelectedSpeed());
        runUiUpdates();
    }

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }

    public void setUnitOfVelocityDisplay() {
        unitOfVelocityDisplay.setText(unitOfVelocity.toString());
    }

    /**
     * Method for displaying the settings fragment
     * @author Johnny
     */
    private void displaySettingsView() {
        fragmentManager.beginTransaction().add(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Method for updating the GUI to display the desired pace
     * @author Emrik
     */
    @SuppressLint("SetTextI18n")
    public void updateSelectedPaceUnit(){
        if(currentSession != null && currentSession.getRunning()){
            selectedPaceDisplay.setText(getString(R.string.desired_pace_text)
                    + currentSession.getFormattedSelectedSpeed());
        }
    }

    public void setAutosaveSession(boolean autosaveSession) {
        this.autosaveSession = autosaveSession;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Method for displaying the sessionOverview fragment
     * @author Jonathan
     */
    private void displaySessionOverview() {
        SessionOverview sessionOverview = SessionOverview.newInstance(currentSession, sessionManager);
        fragmentManager.beginTransaction().add(R.id.fragment_container, sessionOverview, null)
                .addToBackStack("runnerView")
                .commit();
    }
}