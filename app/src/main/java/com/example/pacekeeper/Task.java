package com.example.pacekeeper;

import java.util.TimerTask;

/**
 * A Task to be scheduled by a timer.
 *
 * @author Samuel
 */
public class Task extends TimerTask {
    private FeedbackHandler feedbackHandler;

    /**
     * Class constructor.
     *
     * @param feedbackHandler a feedback handler which initiates the timer.
     */
    public Task(FeedbackHandler feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
    }

    /**
     * Calls the method triggering the feedback.
     *
     * @author Samuel
     */
    @Override
    public void run() {
        feedbackHandler.giveFeedback();
    }
}
