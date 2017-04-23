package com.manojkhannakm.peoplepinpoint.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manojkhannakm.peoplepinpoint.Constants;
import com.manojkhannakm.peoplepinpoint.R;

/**
 * @author Manoj Khanna
 */

public class MapFragment extends Fragment implements HomeFragment.OnPageSelectedListener {

    private static final float MAP_DEFAULT_ZOOM = 13.0f;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private long mSelectedPersonId = -1;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        MapsInitializer.initialize(getActivity());

        mMapView = (MapView) inflater.inflate(R.layout.fragment_map, container, false);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap map) {
                mGoogleMap = map;

                //noinspection ResourceType
                map.setMyLocationEnabled(true);

                updateMapMarkers();
            }

        });

        return mMapView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mMapView.onLowMemory();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search_item_home);
        searchMenuItem.setVisible(false);
    }

    @Override
    public void onPageSelected() {
        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if (mGoogleMap == null) {
            return;
        }

        LatLng selectedLatLng = null;
        FragmentActivity activity = getActivity();

        SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);
        Cursor cursor = database.query(Constants.TABLE_PEOPLE, new String[]{
                Constants.COLUMN_PEOPLE_ID,
                Constants.COLUMN_PEOPLE_NAME,
                Constants.COLUMN_PEOPLE_LATITUDE,
                Constants.COLUMN_PEOPLE_LONGITUDE,
                Constants.COLUMN_PEOPLE_LOCATION_UPDATE_TIME
        }, Constants.COLUMN_PEOPLE_TYPE + " = ?", new String[]{PeopleFragment.PersonType.PEOPLE.name()}, null, null, null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            long id = cursor.getLong(0),
                    locationUpdateTime = cursor.getLong(4);
            String name = cursor.getString(1);
            double latitude = cursor.getDouble(2),
                    longitude = cursor.getDouble(3);

            if (locationUpdateTime == 0L) {
                continue;
            }

            long lastSeenTime = System.currentTimeMillis() - locationUpdateTime;
            int lastSeenDays = (int) (lastSeenTime / (24 * 60 * 60 * 1000)),
                    lastSeenHours = (int) (lastSeenTime / (60 * 60 * 1000)),
                    lastSeenMinutes = (int) (lastSeenTime / (60 * 1000)),
                    lastSeenSeconds = (int) (lastSeenTime / (1000));
            String lastSeen;
            if (lastSeenDays > 0) {
                lastSeen = getString(R.string.last_seen_days_map, lastSeenDays);
            } else if (lastSeenHours > 0) {
                lastSeen = getString(R.string.last_seen_hours_map, lastSeenHours);
            } else if (lastSeenMinutes > 0) {
                lastSeen = getString(R.string.last_seen_minutes_map, lastSeenMinutes);
            } else {
                lastSeen = getString(R.string.last_seen_seconds_map, lastSeenSeconds);
            }

            LatLng latLng = new LatLng(latitude, longitude);
            if (id == mSelectedPersonId) {
                selectedLatLng = latLng;
            }

            mGoogleMap.addMarker(new MarkerOptions()
                    .title(name)
                    .snippet(lastSeen)
                    .position(latLng));
        }
        cursor.close();
        database.close();

        if (mSelectedPersonId == -1) {
            CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
            if (cameraPosition.target.latitude == 0.0 && cameraPosition.target.longitude == 0.0) {
                //TODO: Get local person's location and animate
            }
        } else {
            if (selectedLatLng != null) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, MAP_DEFAULT_ZOOM));
            } else {
                Toast.makeText(getActivity(), getString(R.string.location_error_map), Toast.LENGTH_SHORT).show();
            }

            mSelectedPersonId = -1;
        }
    }

    public void setSelectedPersonId(long selectedPersonId) {
        mSelectedPersonId = selectedPersonId;
    }

}
