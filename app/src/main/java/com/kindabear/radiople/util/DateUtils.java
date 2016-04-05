package com.kindabear.radiople.util;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DateUtils {

    private final static String TAG = "DateUtils";

    public final static String FORMAT_DATETIME = "YYYY-MM-dd HH:mm:ss";
    public final static String FORMAT_DATE = "YYYY-MM-dd";
    public final static String FORMAT_TIME = "HH:mm:ss";

    public final static String FORMAT_CLOCK = "%02d:%02d:%02d";

    public static DateTime now() {
        return DateTime.now();
    }

    public static String toString(String datetime) {
        return DateTime.parse(datetime).toString(FORMAT_DATETIME);
    }

    public static String toString(String datetime, String format) {
        return DateTime.parse(datetime).toString(format);
    }

    public static String toString(long millis) {
        return new DateTime(millis).toString(FORMAT_DATETIME);
    }

    public static String toString(long millis, String format) {
        return new DateTime(millis).toString(format);
    }

    public static DateTime toDateTime(long millis) {
        return new DateTime(millis);
    }

    public static DateTime toDateTime(String dateTime) {
        return new DateTime(dateTime);
    }

    public static String toClock(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = ((milliseconds / (1000 * 60)) % 60);
        int hours = ((milliseconds / (1000 * 60 * 60)) % 24);
        return String.format(FORMAT_CLOCK, hours, minutes, seconds);
    }

    public static String humunize(String dateTime) {
        return humunize(DateTime.parse(dateTime), FORMAT_DATETIME);
    }

    public static String humunize(String dateTime, String defaultFormat) {
        return humunize(DateTime.parse(dateTime), defaultFormat);
    }

    public static String humunize(String dateTime, String defaultFormat, boolean detailToday) {
        return humunize(DateTime.parse(dateTime), defaultFormat, detailToday);
    }

    public static String humunize(DateTime dateTime) {
        return humunize(dateTime, FORMAT_DATETIME, true);
    }

    public static String humunize(DateTime dateTime, String defaultFormat) {
        return humunize(dateTime, defaultFormat, true);
    }

    public static String humunize(DateTime dateTime, String defaultFormat, boolean detailToday) {
        if (!detailToday && isTodate(dateTime)) {
            return "오늘";
        }

        Duration duration = new Duration(dateTime, now()).toDuration();
        long seconds = duration.getStandardSeconds();
        long minutes = duration.getStandardMinutes();
        long hours = duration.getStandardHours();
        long days = duration.getStandardDays();

        if (seconds < 60) {
            return "조금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 4) {
            return days + "일 전";
        } else {
            return dateTime.toString(defaultFormat);
        }
    }

    public static boolean isTodate(DateTime datetime) {
        return now().toString(FORMAT_DATE).equals(datetime.toString(FORMAT_DATE));
    }
}
