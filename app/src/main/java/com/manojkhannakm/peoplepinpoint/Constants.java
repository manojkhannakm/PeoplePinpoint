package com.manojkhannakm.peoplepinpoint;

/**
 * @author Manoj Khanna
 */

public class Constants {

    //Priority: High
    //TODO: Destroy AsyncTasks properly
    //TODO: Add confirmation dialogs wherever necessary

    //Priority: Medium
    //TODO: Create new property searchTag for Person for easy searching
    //TODO: G+ sync
    //TODO: Add email verification

    //Priority: Low
    //TODO: Change icon
    //TODO: Add shadow under SlidingTabLayout on pre Lollipop

    public static final String API_URL = "https://people-pinpoint.appspot.com/_ah/api/";
    public static final String ANDROID_CLIENT_ID = "743785073287-l3dsd4ivnrst5esv8mhtg8uq5s2vokdq.apps.googleusercontent.com";

    public static final String DATABASE = "database";

    public static final String TABLE_PERSON = "person";
    public static final String COLUMN_PERSON_ID = "id";
    public static final String COLUMN_PERSON_NAME = "name";
    public static final String COLUMN_PERSON_EMAIL = "email";
    public static final String COLUMN_PERSON_SENT_IDS = "sentIds";
    public static final String COLUMN_PERSON_RECEIVED_IDS = "receivedIds";
    public static final String COLUMN_PERSON_PEOPLE_IDS = "peopleIds";

    public static final String TABLE_PEOPLE = "people";
    public static final String COLUMN_PEOPLE_ID = "id";
    public static final String COLUMN_PEOPLE_NAME = "name";
    public static final String COLUMN_PEOPLE_EMAIL = "email";
    public static final String COLUMN_PEOPLE_TYPE = "type";
    public static final String COLUMN_PEOPLE_LATITUDE = "latitude";
    public static final String COLUMN_PEOPLE_LONGITUDE = "longitude";
    public static final String COLUMN_PEOPLE_LOCATION_UPDATE_TIME = "locationUpdateTime";

}
