package com.example.pacekeeper;

import android.content.Context;
import android.media.MediaPlayer;
import java.io.Serializable;

/*
* Could hold a collection of sounds, ways to add and remove sounds.
 */
public class AudioPlayer implements Serializable {
    private MediaPlayer decreaseSpeed;
    private MediaPlayer increaseSpeed;

    public AudioPlayer(Context context) {
        decreaseSpeed = MediaPlayer.create(context, R.raw.toofast_notification);
        increaseSpeed = MediaPlayer.create(context, R.raw.tooslow_notification);
    }

    public void decreaseSound() {
        decreaseSpeed.start();
    }

    public void increaseSound() {
        increaseSpeed.start();
    }
}
