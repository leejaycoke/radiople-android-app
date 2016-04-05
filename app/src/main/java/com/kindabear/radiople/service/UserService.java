package com.kindabear.radiople.service;

import android.content.Context;
import android.util.Log;

import com.kindabear.radiople.db.OnDataChangedListener;
import com.kindabear.radiople.db.PreferenceService;
import com.kindabear.radiople.response.Session;
import com.kindabear.radiople.response.User;
import com.kindabear.radiople.response.UserSession;

import java.util.ArrayList;

public class UserService extends PreferenceService {

    private final static String NAME = "user";

    public UserService(Context context) {
        super(context, NAME);
    }

    public void set(User user) {
        mEditor.putInt("id", user.id);
        mEditor.putString("nickname", user.nickname);
        mEditor.putString("email", user.email);
        mEditor.putString("profile_image", user.profileImage);
        mEditor.commit();
    }

    public User get() {
        if (!exists()) {
            return null;
        }

        User user = new User();
        user.id = getId();
        user.nickname = getNickname();
        user.email = getEmail();
        user.profileImage = getProfileImage();
        return user;
    }

    public boolean exists() {
        return getId() > 0;
    }

    public int getId() {
        return mPref.getInt("id", 0);
    }

    public String getNickname() {
        return mPref.getString("nickname", null);
    }

    public String getEmail() {
        return mPref.getString("email", null);
    }

    public String getProfileImage() {
        return mPref.getString("profile_image", null);
    }
}