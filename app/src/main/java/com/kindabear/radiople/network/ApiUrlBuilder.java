package com.kindabear.radiople.network;


public class ApiUrlBuilder extends UrlBuilder {

//    private final static String HOST = "http://183.98.223.6";
//    private final static int PORT = 5001;

    private final static String HOST = "http://dev.api.radiople.com";

    private final static int PORT = 80;

    public ApiUrlBuilder() {
        super(HOST, PORT);
    }
}
