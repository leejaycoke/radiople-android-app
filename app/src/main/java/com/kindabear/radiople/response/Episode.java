package com.kindabear.radiople.response;

import java.io.Serializable;

public class Episode implements Serializable {

    public int id = 0;

    public String title = null;

    public String subtitle = null;

    public String airDate = null;

    public String content = null;

    public Storage storage = null;

    public Broadcast broadcast = null;

    public Scoreboard scoreboard = null;

    public Activity activity = null;

    public String getSubtitle() {
        return subtitle != null && !subtitle.equals("") ? subtitle : null;
    }

    public String getContent() {
        return content != null && !content.equals("") ? content : null;
    }

    public class Scoreboard implements Serializable {

        public int episodeId = 0;

        public int likeCount = 0;

        public int playCount = 0;

        public int downloadCount = 0;

        public String getLikeCount() {
            return likeCount > 99999 ? "99999+" : String.valueOf(likeCount);
        }
    }

    public class Activity implements Serializable {
        public boolean isLike = false;

        public long position = 0;

    }

}
