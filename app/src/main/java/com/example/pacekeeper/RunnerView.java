package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunnerView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunnerView extends Fragment {
    private MainActivity mainActivity;
    private SessionManager sessionManager;
    private NumberPicker speedInput;
    private TextView speedDisplay;
    private TextView timeDisplay;
    private TextView distanceDisplay;
    private ImageButton pauseButton;
    private ImageButton resumeButton;
    private ImageButton stopButton;
    private ImageButton settingsButton;
    private double speed;
    private final double LOWEST_SPEED_THRESHOLD = 0.5;
    private final float X_OFFSET = 0.0455f;
    private final float Y_OFFSET = 0.2534f;
    //private final long UPDATE_INTERVAL_TIMER_MS = 1000; Ignore if not used
    private Bundle savedInstance;
    private MediaPlayer tooSlowAlert;
    private MediaPlayer tooFastAlert;
    private Session currentSession;
    private ArrayList<Session> sessionHistory; // For storing session when you stop a current one, also for loading up existing sessions from file.
    private FeedbackHandler feedback;
    private UnitOfVelocity unitOfVelocity;
    private boolean autosaveSession;
    private int kmDistance;
    private String kmTime;
    private FragmentManager fragmentManager;
    private Handler interfaceUpdateHandler;
    private Runnable uiUpdates;
    private TextView desiredSpeedText;
    private ImageView speedCircle;
    private Drawable slowCircle;
    private Drawable fastCircle;
    private Drawable goodSpeedCircle;
    private Intent serviceIntent;
    private Context context;

    public RunnerView() {
        sessionHistory = new ArrayList<>();
        kmDistance = 1000;
    }


    public static RunnerView newInstance(MainActivity mainActivity, double speed, boolean autoSaveSession) {
        RunnerView fragment = new RunnerView();
        Bundle args = new Bundle();
        // You can pass arguments if needed
        args.putDouble("speed", speed);
        fragment.setArguments(args);
        fragment.setMainActivity(mainActivity);
        fragment.setAutosaveSession(autoSaveSession);
        return fragment;
    }

    @SuppressLint("VisibleForTests")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        context = container.getContext();
        this.savedInstance = savedInstanceState;
        timeDisplay = rootView.findViewById(R.id.time);
        speedDisplay = rootView.findViewById(R.id.speedDisplay);
        distanceDisplay = rootView.findViewById(R.id.distanceDisplay);
        pauseButton = rootView.findViewById(R.id.pauseButtonLogo);
        resumeButton = rootView.findViewById(R.id.playButtonLogo);
        stopButton = rootView.findViewById(R.id.stopButtonLogo);
        settingsButton = rootView.findViewById(R.id.settingsButton);
        stopButton.setVisibility(View.INVISIBLE);
        resumeButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);
        fragmentManager = mainActivity.getSupportFragmentManager();
        Intent intent = requireActivity().getIntent();
        interfaceUpdateHandler = new Handler(Looper.getMainLooper());


        speedCircle = rootView.findViewById(R.id.speed_circle);
        desiredSpeedText = rootView.findViewById(R.id.desired_speed_text);
        slowCircle = ContextCompat.getDrawable(requireContext(), R.drawable.circle);
        fastCircle = ContextCompat.getDrawable(requireContext(), R.drawable.redcircle);
        goodSpeedCircle = ContextCompat.getDrawable(requireContext(), R.drawable.greencircle);

        TextView unitOfVelocityDisplay = rootView.findViewById(R.id.unit_of_velocity);

        if (intent != null) {
            feedback = (FeedbackHandler) intent.getSerializableExtra("feedbackHandler");
            //speedDisplayMode = intent.getStringExtra("speedDisplayMode");
            unitOfVelocity = (UnitOfVelocity) intent.getSerializableExtra("unitOfVelocity");
        }
        Bundle args = getArguments();

        if (args != null) {
            speed = args.getDouble("speed", 0);
        }

        if (mainActivity != null) {
            sessionManager = mainActivity.getSessionManager();
        }

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseButton.setVisibility(View.INVISIBLE);
                resumeButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                settingsButton.setVisibility(View.VISIBLE);
                currentSession.pauseSession();
                feedback.stopFeedback();
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.INVISIBLE);
                settingsButton.setVisibility(View.INVISIBLE);
                currentSession.continueSession();
                feedback.runFeedback(currentSession.getSelectedSpeed());
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySettingsView();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedback.stopFeedback();
//                stopLocationUpdates();
                if (autosaveSession) {
                    sessionManager.add(currentSession.getSerializableSession());
                    sessionManager.storeSessionToMemory(mainActivity);
                    currentSession.killSession();
                    getParentFragmentManager().popBackStackImmediate();
                } else {
                    displaySessionOverview();
                }
                serviceIntent.setAction("STOP");
                context.startForegroundService(serviceIntent);
            }
        });

        uiUpdates = new Runnable() {
            @Override
            public void run() {
                updateUI();
                interfaceUpdateHandler.postDelayed(this, 250);
            }
        };

        start();
        desiredSpeedText.setText(desiredSpeedText.getText() + currentSession.getFormattedSelectedSpeed());
        unitOfVelocityDisplay.setText(unitOfVelocity.toString());
        hideNavigationBar();
        setBackButtonBehavior();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        runUiUpdates();
        hideNavigationBar();
        setBackButtonBehavior();
    }

    @Override
    public void onPause() {
        super.onPause();
        displayNavigationBar();
        interfaceUpdateHandler.removeCallbacks(uiUpdates);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        feedback.removeTextToSpeech();
    }

    private void setBackButtonBehavior() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainActivity.moveTaskToBack(true);
            }
        });
    }

    private void hideNavigationBar() {
        View decorView = getActivity().getWindow().getDecorView();
        int hideNavigation = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        int immersive = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(hideNavigation | immersive);
    }

    private void displayNavigationBar() {
        View decorView = getActivity().getWindow().getDecorView();
        int visible = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(visible);
    }


    @SuppressLint("SetTextI18n")
    public void updateUI() {
        if (currentSession.getRunning()) {
            distanceDisplay.setText(currentSession.getFormattedDistance());
            if (currentSession.getCurrentSpeed() > LOWEST_SPEED_THRESHOLD) {
                speedDisplay.setText(currentSession.getFormattedSpeed());
            } else {
                speedDisplay.setText(getResources().getString(R.string.null_speed));
            }
            double velocity = currentSession.getCurrentSpeed();
            final double delta = feedback.getVelocityDelta();
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

    private void start() {
        currentSession = new Session(speed, getContext(), feedback);
        serviceIntent = new Intent(getActivity().getApplicationContext(), SensorUnitHandler.class);
        serviceIntent.setAction("START");
        context.startForegroundService(serviceIntent);
        feedback.setRunning(currentSession.getRunning());
        feedback.setCurrentSpeed(currentSession.getCurrentSpeed());
        currentSession.setUnitOfVelocity(unitOfVelocity);
        feedback.runFeedback(currentSession.getSelectedSpeed());
        runUiUpdates();
    }

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }


    private void displaySettingsView() {
        fragmentManager.beginTransaction().add(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    public void setAutosaveSession(boolean autosaveSession) {
        this.autosaveSession = autosaveSession;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public TextView getSpeedDisplay() {
        return speedDisplay;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private void displaySessionOverview() {
        SessionOverview sessionOverview = SessionOverview.newInstance(currentSession, sessionManager);
        fragmentManager.beginTransaction().add(R.id.fragment_container, sessionOverview, null)
                .addToBackStack("runnerView")
                .commit();
    }
}