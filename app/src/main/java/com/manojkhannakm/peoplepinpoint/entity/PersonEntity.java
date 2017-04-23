package com.manojkhannakm.peoplepinpoint.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Manoj Khanna
 */

public class PersonEntity implements Parcelable {

    public static final Creator<PersonEntity> CREATOR = new Creator<PersonEntity>() {

        @Override
        public PersonEntity createFromParcel(Parcel source) {
            return new PersonEntity(source);
        }

        @Override
        public PersonEntity[] newArray(int size) {
            return new PersonEntity[size];
        }

    };

    private final long mId;
    private final String mName, mEmail;

    public PersonEntity(long id, String name, String email) {
        mId = id;
        mName = name;
        mEmail = email;
    }

    private PersonEntity(Parcel parcel) {
        mId = parcel.readLong();
        mName = parcel.readString();
        mEmail = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeString(mEmail);
    }

    public long getId() {
        return mId;
    }

    public String getIdString() {
        return String.valueOf(mId);
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

}
