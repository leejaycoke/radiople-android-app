package com.kindabear.radiople.response;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class Broadcast implements Serializable {

    public int id = 0;

    public String title = null;

    public String subtitle = null;

    public ArrayList<String> casting = null;

    public String notice = null;

    public String iconImage = null;

    public String coverImage = null;

    public String latestAirDate = null;

    public String description = null;

    public Category category = null;

    public Scoreboard scoreboard = null;

    public Activity activity = null;

    public User user = null;

    public String getCasting() {
        return TextUtils.join(", ", casting);
    }

    public class Scoreboard implements Serializable {

        public int boradcastId = 0;

        public int commentCount = 0;

        public int likeCount = 0;

        public int subscriptionCount = 0;

        public int ratingCount = 0;

        public int episodeCount = 0;

        public float ratingAverage = 0;

        public float getRatingAverage() {
            return Float.valueOf(String.format("%.1f", ratingAverage));
        }
    }

    public class Activity implements Serializable {

        public boolean isSubscriber = false;

        public float ratingPoint = 0;

        public boolean isRated() {
            return ratingPoint > 0;
        }

        public float getRatingPoint() {
            return Float.valueOf(String.format("%.1f", ratingPoint));
        }
    }

}
