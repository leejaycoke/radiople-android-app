package com.kindabear.radiople.player;

public interface AdStateListener {

    void onPrepared();

    void onStart();

    void onComplete();

    void onAllComplete();
}