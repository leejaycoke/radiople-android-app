package com.kindabear.radiople.db;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceService {

    protected SharedPreferences mPref = null;

    protected SharedPreferences.Editor mEditor = null;

    public PreferenceService(Context context, String name) {
        mPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
    }

    public void deleteAll() {
        mEditor.clear();
    }

}
