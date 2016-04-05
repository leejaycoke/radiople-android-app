package com.kindabear.radiople.response;

import java.io.Serializable;
import java.util.ArrayList;

public class Storage implements Serializable {

    public int id = 0;

    public String filename = null;

    public ArrayList<String> mimes = new ArrayList<String>();

    public String fileType = null;

    public String url = null;

    public Extra extra = null;

    public class Extra implements Serializable {
        public String displayLength = null;
    }

    public static class FileType {
        public final static String AUDIO = "audio";
        public final static String VIDEO = "audio";
        public final static String PDF = "pdf";
        public final static String UNKNOWN = "unknown";
    }
}