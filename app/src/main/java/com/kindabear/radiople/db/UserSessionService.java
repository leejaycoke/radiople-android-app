package com.kindabear.radiople.db;

import android.content.Context;
import android.util.Log;

import com.kindabear.radiople.response.Session;
import com.kindabear.radiople.response.User;
import com.kindabear.radiople.response.UserSession;

import java.util.ArrayList;

public class UserSessionService extends PreferenceService {

    private final static String NAME = "user";

    private final static ArrayList<OnDataChangedListener> mListeners = new ArrayList<OnDataChangedListener>();

    public UserSessionService(Context context) {
        super(context, NAME);
    }

    public void setByUserSession(UserSession userSession) {
        Log.i("!", ">>>>>>>>>>>>>>>>>>" + userSession.user.id);
        mEditor.putInt("id", userSession.user.id);
        mEditor.putString("nickname", userSession.user.nickname);
        mEditor.putString("email", userSession.user.email);
        mEditor.putBoolean("is_verified", userSession.user.isVerified);
        mEditor.putString("profile_image", userSession.user.profileImage);
        mEditor.putString("access_token", userSession.session.accessToken);
        mEditor.commit();
        notifyDataChanged();
    }

    public void setByUser(User user) {
        mEditor.putInt("id", user.id);
        mEditor.putString("nickname", user.nickname);
        mEditor.putString("email", user.email);
        mEditor.putString("profile_image", user.profileImage);
        mEditor.putBoolean("is_verified", user.isVerified);
        mEditor.commit();
        notifyDataChanged();
    }

    public void setBySession(Session session) {
        mEditor.putString("access_token", session.accessToken);
        mEditor.commit();
        notifyDataChanged();
    }

    public void setId(int id) {
        mEditor.putInt("id", id);
        mEditor.commit();
        notifyDataChanged();
    }

    public int getId() {
        return mPref.getInt("id", 0);
    }

    public void setNickname(String nickname) {
        mEditor.putString("nickname", nickname);
        mEditor.commit();
        notifyDataChanged();
    }

    public String getNickname() {
        return mPref.getString("nickname", null);
    }

    public void setProfileImage(String profileImage) {
        mEditor.putString("profile_image", profileImage);
        mEditor.commit();
        notifyDataChanged();
    }

    public void setEmail(String email) {
        mEditor.putString("email", email);
        mEditor.commit();
        notifyDataChanged();
    }

    public String getEmail() {
        return mPref.getString("email", null);
    }

    public String getProfileImage() {
        return mPref.getString("profile_image", null);
    }

    public void setAccessToken(String accessToken) {
        mEditor.putString("access_token", accessToken);
        mEditor.commit();
        notifyDataChanged();
    }

    public boolean isVerified() {
        return mPref.getBoolean("is_verified", false);
    }

    public String getAccessToken() {
        return mPref.getString("access_token", null);
    }

    public void deleteAll() {
        mEditor.clear();
        mEditor.commit();
        notifyDataChanged();
    }

    public boolean exists() {
        return mPref.contains("id");
    }

    public void addOnDataChangedListener(OnDataChangedListener listener) {
        mListeners.add(listener);
        Log.i("UserSessionService", ">>>>>>>>>>>>>>>>>>>>>>>>>> 0000 ??????? " + mListeners.size());
    }

    public void notifyDataChanged() {
        for (OnDataChangedListener listener : mListeners) {
            if (listener != null) {
                Log.i("UserSessionService", ">>>>>>>>>>>>>>>>>>> notify state changed not null");
                listener.onDataChanged(mPref.getAll());
            }
        }
    }

}
