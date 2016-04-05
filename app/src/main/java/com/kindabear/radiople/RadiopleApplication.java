package com.kindabear.radiople;

import android.app.Application;

import com.kindabear.radiople.service.SessionService;
import com.kindabear.radiople.service.UserService;

public class RadiopleApplication extends Application {

    private UserService mUserService = null;
    private SessionService mSessionService = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mUserService = new UserService(this);
        mSessionService = new SessionService(this);
    }

    public UserService getUserService() {
        return mUserService;
    }

    public SessionService getSessionService() {
        return mSessionService;
    }

}