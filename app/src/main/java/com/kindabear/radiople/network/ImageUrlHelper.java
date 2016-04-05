package com.kindabear.radiople.network;

import java.net.URL;

public class ImageUrlHelper {

    private final static String TAG = "ImageUrlHelper";

    public static String create(String url, int width, int height) {
        try {
            URL u = new URL(url);
            url = u.getProtocol() + "://" + u.getAuthority() + "/" + width + "x" + height + u.getPath();
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }
}
