package com.example.pacekeeper;

import java.util.TimerTask;

/*
 * This whole class could probably be refactored into a lambda-expression in the FeedbackHandler-class.
 * Did it this way just so I could explicity cancel the timertask when I tested the feature.
 */
public class Task extends TimerTask {
    private FeedbackHandler feedbackHandler;

    public Task(FeedbackHandler feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
    }

    @Override
    public void run() {
        feedbackHandler.giveFeedback();
    }
}
