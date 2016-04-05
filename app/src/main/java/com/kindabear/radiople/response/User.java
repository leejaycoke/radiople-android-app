package com.kindabear.radiople.response;

import java.io.Serializable;

public class User implements Serializable {

    public int id;

    public String account;

    public String nickname;

    public String email;

    public String birthdate;

    public String profileImage = null;

    public String coverImage = null;

    public String gender;

    public boolean isVerified;

    public Provider provider;

}

class Provider {

    public final static String FACEBOOK = "facebook";

    public final static String GOOGLE = "google";

    public String name;

}
