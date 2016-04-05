package com.kindabear.radiople.response;


import java.util.ArrayList;

public class Tracks {

    public ArrayList<Track> tracks = new ArrayList<Track>();

    public class Track {

        public int id = 0;

        public String type = null;

        public String url = null;

    }
}