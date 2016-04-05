package com.kindabear.radiople.network;


public class AudioUrlBuilder extends UrlBuilder {

    private final static String HOST = "http://dev.audio.radiople.com";
    private final static int PORT = 80;

    public AudioUrlBuilder() {
        super(HOST, PORT);
    }
}
