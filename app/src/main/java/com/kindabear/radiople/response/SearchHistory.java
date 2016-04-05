package com.kindabear.radiople.response;

import com.kindabear.radiople.util.DateUtils;

import org.joda.time.DateTime;

public class SearchHistory {

    public String keyword = null;

    public String updatedAt = null;

    public DateTime getUpdatedAt() {
        return DateUtils.toDateTime(updatedAt);
    }
}
