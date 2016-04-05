package com.kindabear.radiople.service;

import android.media.MediaPlayer;

import com.kindabear.radiople.response.Episode;

public interface PlayerListener {

    void onBuffering();

    void onPrepared(Episode episode, MediaPlayer mediaPlayer);

    void onStarted();

    void onPaused();

    void onComplete();

    void onStopped();

    void onError(String message);

    void onPositionUpdated(int position);

    void onEpisodePrepared(Episode episode);
}
