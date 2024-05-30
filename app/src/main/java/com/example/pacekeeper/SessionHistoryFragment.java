package com.example.pacekeeper;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

/**
 * This class creates the session history fragment and determines
 * the actions of all buttons.
 */
public class SessionHistoryFragment extends Fragment {

    private SessionManager sessionManager;
    LinearLayout sessionContainer;
    private Runnable uiPopulation;
    private ImageButton returnButton;
    private Fragment sessionFragment;
    private View sessionView;
    Handler uiHandler;


    public SessionHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * The new instance method is used to send data between fragment/activities when creating a fragment.
     * @return A new instance of fragment SessionFragment.
     * @author Jonathan
     */
    public static SessionHistoryFragment newInstance(SessionManager sessionManager) {
        SessionHistoryFragment fragment = new SessionHistoryFragment();
        fragment.setSessionManager(sessionManager);
        return fragment;
    }

    /**
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     * @author Jonathan, Emrik
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStackImmediate();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * standard method which is run when the fragment is created which initializes all the
     * elements in the fragment and their corresponding listeners.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionFragment = this;

        uiHandler = new Handler(Looper.getMainLooper());
        View rootView = inflater.inflate(R.layout.fragment_session_history, container, false);
        initializeGraphicalResources(rootView);

        uiPopulation = new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        };
        populateUI();
        initializeEventListeners();
        return rootView;
    }

    /**
     * Method which populates the fragment on a separate thread to ensure that
     * the application does not close unexpectedly when different views is
     * opened rapidly.
     */
    public void populateUI(){
        uiHandler.post(uiPopulation);
    }

    /**
     * Separate method to initialize the elements from the fragment.
     * @param rootView the fragment which the elements is fetched from.
     * @author Jonathan, Samuel
     */
    public void initializeGraphicalResources(View rootView) {
        returnButton = rootView.findViewById(R.id.return_button);
        sessionContainer = rootView.findViewById(R.id.session_layout);
    }

    /**
     * The method which initializes the eventlisteners for the elements in the
     * fragment_session_history.
     */
    public void initializeEventListeners() {
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.storeSessionToMemory(requireContext());
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(returnButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                getParentFragmentManager().popBackStackImmediate();
            }
        });
    }

    /**
     * The method which is run on a separate thread and is responsible for populating
     * the session history fragment by creating several session_items and placing them
     * inside the session history fragment.
     * Here all sessions is retrieved from the session list and for each session a
     * session item is populated and the appropriate action listeners is added to
     * each element.
     * @author Jonathan
     */

    private void updateUI() {
        List<Session.StoredSession> sessionsList = sessionManager.getSavedSessions();

        if(sessionsList!=null) {
            Collections.reverse(sessionsList);
            for (Session.StoredSession session : sessionsList) {
                //Initialize each element in the session_item
                sessionView = LayoutInflater.from(getContext()).inflate(R.layout.session_item, null);
                LinearLayout sessionItem = sessionView.findViewById(R.id.session_layout);
                ImageButton compressButton = sessionView.findViewById(R.id.compress_button);
                ImageButton  expandButton = sessionView.findViewById(R.id.expand_button);
                ImageButton deleteSessionButton = sessionView.findViewById(R.id.delete_session);
                TextView sessionComment = sessionView.findViewById(R.id.detail_text_view_session_comment_text);
                TextView sessionCommentTitle = sessionView.findViewById(R.id.detail_text_view_session_comment_title);
                TextView sessionDistance = sessionView.findViewById(R.id.session_distance);
                ImageView routeImage = sessionView.findViewById(R.id.route_image);

                Button saveCommentButton = sessionView.findViewById(R.id.save_comment_button);
                EditText editCommentText = sessionView.findViewById(R.id.edit_comment);
                ImageButton editCommentIcon = sessionView.findViewById(R.id.edit_comment_icon);

                TextView sessionOverview = sessionView.findViewById(R.id.summary_text_view1);
                TextView allKmInSession = sessionView.findViewById(R.id.detail_text_view_km);

                String formattedDistance ="";
                //Add the data from the session retrieved.
                if(session.getTotalDistance()>100){
                    formattedDistance = String.format(Locale.forLanguageTag("Swedish"), "%.1f", session.getTotalDistance() / 1000) + " km";
                } else{
                    formattedDistance = (int) session.getTotalDistance()+ " m";
                }

                sessionOverview.setText(session.getDate() + " | " + session.getTotalTime());
                sessionDistance.setText(formattedDistance);
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
                //Listener for the expand button which enables the user to
                //expand a session item.
                expandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.GONE) {
                            editCommentIcon.setVisibility(View.VISIBLE);
                            allKmInSession.setVisibility(View.VISIBLE);
                            sessionCommentTitle.setVisibility(View.VISIBLE);
                            routeImage.setVisibility(View.VISIBLE);
                            //Creates the route image and places it in the routeImage imageview.
                            MapGenerator mapGenerator = new MapGenerator();
                            Glide.with(sessionFragment).load(mapGenerator.getUrlFromStoredSession(getString(R.string.mapbox_access_token),session)).into(routeImage);

                            if (session.getSessionComment() != null) {
                                sessionComment.setVisibility(View.VISIBLE);
                            }
                            expandButton.setVisibility(View.GONE);
                            compressButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
                //Enables the user to make the session item smaller again.
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
                            routeImage.setVisibility(View.GONE);
                        }
                    }
                });
                //Listener which reveals the edit text element on click.
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
                //Saves the comment the user has entered in the edit text element.
                //And ensures that the keyboard is minimized on save.
                saveCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        session.setSessionComment(String.valueOf(editCommentText.getText()));
                        sessionComment.setText(editCommentText.getText());
                        sessionComment.setVisibility(View.VISIBLE);
                        editCommentText.setVisibility(View.GONE);
                        saveCommentButton.setVisibility(View.GONE);

                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(saveCommentButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    }

                });
                //Adds a bit of spacing in between each session item for increased readability.
                View spacerView = new View(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20);
                spacerView.setLayoutParams(params);
                //Listener for deleting a session.
                deleteSessionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sessionManager.remove(session);
                        sessionItem.setVisibility(View.GONE);
                        sessionContainer.removeView(spacerView);
                    }
                });
                //Listener which expands the session item if it is not expanded
                //and compresses it if it's already expanded.
                sessionItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allKmInSession.getVisibility() == View.GONE) {
                            editCommentIcon.setVisibility(View.VISIBLE);
                            allKmInSession.setVisibility(View.VISIBLE);
                            sessionCommentTitle.setVisibility(View.VISIBLE);
                            routeImage.setVisibility(View.VISIBLE);
                            MapGenerator mapGenerator = new MapGenerator();
                            Glide.with(sessionFragment).load(mapGenerator.getUrlFromStoredSession(getString(R.string.mapbox_access_token),session)).into(routeImage);

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
                            routeImage.setVisibility(View.GONE);
                        }

                    }
                });
                //Removes the place default text when the user presses the text field.
                editCommentText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus && editCommentText.getText().toString().equals("Add text here.")){
                            editCommentText.setText("");
                        }
                    }
                });
                //Adds the session item (sessionView) to the session history fragment (session container).
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