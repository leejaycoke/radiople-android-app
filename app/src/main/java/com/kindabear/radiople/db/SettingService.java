package com.kindabear.radiople.db;

import android.content.Context;

public class SettingService extends PreferenceService {

    private final static String NAME = "settings";

    public SettingService(Context context) {
        super(context, NAME);
    }

    public void setAllowMobileNetwork(boolean allowMobileNetwork) {
        mEditor.putBoolean("allow_mobile_network", allowMobileNetwork);
        mEditor.commit();
    }

    public boolean isAllowMobileNetwork() {
        return mPref.getBoolean("allow_mobile_network", false);
    }

    public void setAllPush(boolean allPush) {
        mEditor.putBoolean("all_push", allPush);
        mEditor.commit();
    }

    public boolean getAllPush() {
        return mPref.getBoolean("all_push", true);
    }

    public void setUserPush(boolean userPush) {
        mEditor.putBoolean("user_push", userPush);
        mEditor.commit();
    }

    public boolean getUserPush() {
        return mPref.getBoolean("user_push", true);
    }

    public void setSubscriptionPush(boolean subscriptionPush) {
        mEditor.putBoolean("subscription_push", subscriptionPush);
        mEditor.commit();
    }

    public boolean getSubscriptionPush() {
        return mPref.getBoolean("subscription_push", true);
    }

    public void setSkipTime(int skipTime) {
        mEditor.putInt("skip_time", skipTime);
        mEditor.commit();
    }

    public int getSkipTime() {
        return Integer.parseInt(mPref.getString("skip_time", "3000"));
    }

    public String getActivityAfterFinish() {
        return mPref.getString("activity_after_finish", ActivityAfterFinish.FINISH);
    }

    public static class ActivityAfterFinish {
        public final static String PLAY_NEXT_EPISOIDE = "play_next_episode";
        public final static String FINISH = "finish";
    }

    public void deleteAll() {
        mEditor.clear();
        mEditor.commit();
    }

    public boolean isEmpty() {
        return mPref.getAll().isEmpty();
    }

}
