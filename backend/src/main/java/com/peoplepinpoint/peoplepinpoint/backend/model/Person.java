package com.peoplepinpoint.peoplepinpoint.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.peoplepinpoint.peoplepinpoint.backend.Constants;

import java.util.ArrayList;

/**
 * @author Manoj Khanna
 */

public class Person {

    private long id, locationUpdateTime;
    private String name, email;
    private ArrayList<Long> sentIdList, receivedIdList, peopleIdList;
    private double latitude, longitude;

    public Person(Entity entity) {
        id = entity.getKey().getId();
        name = (String) entity.getProperty(Constants.PROPERTY_PERSON_NAME);
        email = (String) entity.getProperty(Constants.PROPERTY_PERSON_EMAIL);
        //noinspection unchecked
        sentIdList = (ArrayList<Long>) entity.getProperty(Constants.PROPERTY_PERSON_SENT_IDS);
        //noinspection unchecked
        receivedIdList = (ArrayList<Long>) entity.getProperty(Constants.PROPERTY_PERSON_RECEIVED_IDS);
        //noinspection unchecked
        peopleIdList = (ArrayList<Long>) entity.getProperty(Constants.PROPERTY_PERSON_PEOPLE_IDS);

        if (entity.hasProperty(Constants.PROPERTY_PERSON_LATITUDE)) {
            latitude = (double) entity.getProperty(Constants.PROPERTY_PERSON_LATITUDE);
        }

        if (entity.hasProperty(Constants.PROPERTY_PERSON_LONGITUDE)) {
            longitude = (double) entity.getProperty(Constants.PROPERTY_PERSON_LONGITUDE);
        }

        if (entity.hasProperty(Constants.PROPERTY_PERSON_LOCATION_UPDATE_TIME)) {
            locationUpdateTime = (long) entity.getProperty(Constants.PROPERTY_PERSON_LOCATION_UPDATE_TIME);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Long> getSentIdList() {
        return sentIdList;
    }

    public void setSentIdList(ArrayList<Long> sentIdList) {
        this.sentIdList = sentIdList;
    }

    public ArrayList<Long> getReceivedIdList() {
        return receivedIdList;
    }

    public void setReceivedIdList(ArrayList<Long> receivedIdList) {
        this.receivedIdList = receivedIdList;
    }

    public ArrayList<Long> getPeopleIdList() {
        return peopleIdList;
    }

    public void setPeopleIdList(ArrayList<Long> peopleIdList) {
        this.peopleIdList = peopleIdList;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getLocationUpdateTime() {
        return locationUpdateTime;
    }

    public void setLocationUpdateTime(long locationUpdateTime) {
        this.locationUpdateTime = locationUpdateTime;
    }

}
