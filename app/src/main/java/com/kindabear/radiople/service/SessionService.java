package com.kindabear.radiople.service;

import android.content.Context;

import com.kindabear.radiople.db.PreferenceService;
import com.kindabear.radiople.response.Session;
import com.kindabear.radiople.util.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

public class SessionService extends PreferenceService {

    private final static String NAME = "session";

    public SessionService(Context context) {
        super(context, NAME);
    }

    public void set(Session session) {
        mEditor.putString("access_token", session.accessToken);
        mEditor.commit();
    }

    public Session get() {
        if (!exists()) {
            return null;
        }

        Session session = new Session();
        session.accessToken = getAccessToken();
        return session;
    }

    public boolean exists() {
        return getAccessToken() != null;
    }

    public String getAccessToken() {
        return mPref.getString("access_token", null);
    }
}