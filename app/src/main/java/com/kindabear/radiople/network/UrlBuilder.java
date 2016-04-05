package com.kindabear.radiople.network;

import android.content.ContentValues;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

public class UrlBuilder {

    private final static String TAG = "UrlBuilder";

    private final static String URL_ENCODING = "UTF-8";

    private ArrayList<String> mPaths = new ArrayList<String>();
    private ContentValues mParams = new ContentValues();

    public String mUrl = null;
    private final String mHostName;

    public UrlBuilder(String hostName, int port) {
        mHostName = hostName;
        mUrl = mHostName + ":" + port + "/";
    }

    public UrlBuilder addPath(String... paths) {
        mPaths.addAll(Arrays.asList(paths));
        return this;
    }

    public UrlBuilder addParam(String key, String value) {
        if (key == null || key.trim().equals("")) {
            throw new IllegalArgumentException("param's key required");
        }
        if (value == null) {
            throw new IllegalArgumentException("param's key required");
        }

        try {
            value = URLEncoder.encode(value, URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return this;
        }

        mParams.put(key, value);

        return this;
    }

    public String toString() {
        if (mPaths.size() > 0) {
            for (int i = 0; i < mPaths.size(); i++) {
                mUrl += mPaths.get(i) + "/";
            }
            mUrl = mUrl.substring(0, mUrl.length() - 1);
        }

        if (mParams.size() > 0) {
            mUrl += "?";
            for (String key : mParams.keySet()) {
                mUrl += key + "=" + mParams.get(key) + "&";
            }
            mUrl = mUrl.substring(0, mUrl.length() - 1);
        }

        return mUrl;
    }
}
