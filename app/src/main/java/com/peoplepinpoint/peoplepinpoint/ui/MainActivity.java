package com.peoplepinpoint.peoplepinpoint.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.peoplepinpoint.peoplepinpoint.Constants;
import com.peoplepinpoint.peoplepinpoint.R;
import com.peoplepinpoint.peoplepinpoint.entity.PersonEntity;
import com.peoplepinpoint.peoplepinpoint.service.LocationService;

/**
 * @author Manoj Khanna
 */

public class MainActivity extends AppCompatActivity {

    private PersonEntity mLocalPersonEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase database = openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

        database.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_PERSON + "("
                + Constants.COLUMN_PERSON_ID + " BIGINT, "
                + Constants.COLUMN_PERSON_NAME + " VARCHAR, "
                + Constants.COLUMN_PERSON_EMAIL + " VARCHAR, "
                + Constants.COLUMN_PERSON_SENT_IDS + " VARCHAR, "
                + Constants.COLUMN_PERSON_RECEIVED_IDS + " VARCHAR, "
                + Constants.COLUMN_PERSON_PEOPLE_IDS + " VARCHAR);");

        database.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_PEOPLE + "("
                + Constants.COLUMN_PEOPLE_ID + " BIGINT, "
                + Constants.COLUMN_PEOPLE_NAME + " VARCHAR, "
                + Constants.COLUMN_PEOPLE_EMAIL + " VARCHAR, "
                + Constants.COLUMN_PEOPLE_TYPE + " VARCHAR, "
                + Constants.COLUMN_PEOPLE_LATITUDE + " DOUBLE, "
                + Constants.COLUMN_PEOPLE_LONGITUDE + " DOUBLE, "
                + Constants.COLUMN_PEOPLE_LOCATION_UPDATE_TIME + " BIGINT);");

        Cursor cursor = database.query(Constants.TABLE_PERSON, null, null, null, null, null, null);
        boolean signedIn = cursor.moveToFirst();
        if (signedIn) {
            mLocalPersonEntity = new PersonEntity(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
        }
        cursor.close();

        database.close();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_main, !signedIn ? SignInFragment.newInstance() : HomeFragment.newInstance())
                    .commit();
        }

        if (signedIn) {
            Intent intent = new Intent(this, LocationService.class);
            intent.putExtra(LocationService.EXTRA_LOCAL_PERSON_ENTITY, mLocalPersonEntity);
            startService(intent);
        }
    }

    public PersonEntity getLocalPersonEntity() {
        return mLocalPersonEntity;
    }

    public void setLocalPersonEntity(PersonEntity personEntity) {
        mLocalPersonEntity = personEntity;
    }

}
