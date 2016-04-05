package com.kindabear.radiople.player;

public interface PodcastStateListener {

    void onBuffering();

    void onPrepared();

    void onStart();

    void onPause();

    void onComplete();

    void onStop();

    void onError(String message);
}