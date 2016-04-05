package com.kindabear.radiople.network;

public class ImageUrlBuilder extends UrlBuilder {

    private final static String HOST = "http://dev.image.radiople.com";
    private final static int PORT = 80;

    public ImageUrlBuilder() {
        super(HOST, PORT);
    }

}
