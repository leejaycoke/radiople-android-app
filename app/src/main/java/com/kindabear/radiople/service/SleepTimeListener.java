package com.kindabear.radiople.service;

public interface SleepTimeListener {

    void onStart();

    void onCancel();

    void onUpdate(int left);
}
