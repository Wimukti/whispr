package com.example.vlc.utils;

public class Constants {

    public static final int LOWER_BINARY_THRESHOLD = 145;
    public static final int HIGHER_BINARY_THRESHOLD = 255;
    public static final int CONTOUR_AREA = 400000;
    public static final int DASH_FRAME_COUNT = 3;
    public static final int DOT_FRAME_COUNT = 1;
    public static final int DOT_SPEED_INITIAL_VALUE = 50;
    public static final int DASH_DURATION_MULTIPLIER = 3;
    public static final int SPACE_DURATION_MULTIPLIER = 7;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    //Strings
    public static final String STARTING_MORSE_CODE = "-.-.- ";
    public static final String ENDING_MORSE_CODE = " .-.-";
    public static final char DOT = '.';
    public static final char DASH = '-';
    public static final char SPACE = ' ';

    //DB HELPERS

    public static final String DATABASE_NAME = "message_history.db";
    public static final int DATABASE_VERSION = 1;

    // Table name and column names
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IS_SENT = "is_sent";


}
