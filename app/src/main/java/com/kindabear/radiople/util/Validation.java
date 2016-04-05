package com.kindabear.radiople.util;

import android.util.Patterns;

public class Validation {

    public final static String PATTERN_PASSWORD = "^[0-9a-zA-Z]+$";
    public final static String PATTERN_EMAIL = Patterns.EMAIL_ADDRESS.toString();
    public final static String PATTERN_NICKNAME = "^[가-힣a-zA-Z0-9]+$";

    public final static int MAX_PASSWORD_LENGTH = 30;
    public final static int MIN_PASSWORD_LENGTH = 8;

    public final static int MAX_NICKNAME_LENGTH = 10;
    public final static int MIN_NICKNAME_LENGTH = 3;

}
