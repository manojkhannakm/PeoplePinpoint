package com.manojkhannakm.peoplepinpoint.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.people_pinpoint.myApi.model.Person;
import com.manojkhannakm.peoplepinpoint.Constants;
import com.manojkhannakm.peoplepinpoint.R;
import com.manojkhannakm.peoplepinpoint.backend.EndpointAsyncTask;
import com.manojkhannakm.peoplepinpoint.entity.PersonEntity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Manoj Khanna
 */

public class PeopleFragment extends Fragment {

    private static final String ARG_RECYCLER_ITEM_LIST = "recyclerItemList";

    private TextView mPeopleTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerAdapter mRecyclerAdapter;
    private ArrayList<RecyclerItem> mRecyclerItemList;

    public static PeopleFragment newInstance() {
        return new PeopleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_people, container, false);

        mPeopleTextView = (TextView) view.findViewById(R.id.people_text_view_people);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_people);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new PeopleAsyncTask().execute();
            }

        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_people);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(mRecyclerAdapter);

        if (savedInstanceState != null) {
            mRecyclerItemList = savedInstanceState.getParcelableArrayList(ARG_RECYCLER_ITEM_LIST);

            if (!mRecyclerItemList.isEmpty()) {
                mPeopleTextView.setVisibility(View.GONE);
            }
        } else {
            mRecyclerItemList = new ArrayList<>();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            new PeopleAsyncTask().execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ARG_RECYCLER_ITEM_LIST, mRecyclerItemList);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search_item_home);
        searchMenuItem.setVisible(false);
    }

    private void updateRecyclerItemList() {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

        mRecyclerItemList = new ArrayList<>();

        Cursor cursor = database.query(Constants.TABLE_PEOPLE, null,
                Constants.COLUMN_PEOPLE_TYPE + " = ?", new String[]{PersonType.RECEIVED.name()},
                null, null, Constants.COLUMN_PEOPLE_NAME);
        int count = cursor.getCount();
        if (count > 0) {
            mRecyclerItemList.add(new RecyclerHeaderItem(getString(R.string.received_requests_header_people)));

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                mRecyclerItemList.add(new RecyclerPersonItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), PersonType.RECEIVED));
            }
        }
        cursor.close();

        cursor = database.query(Constants.TABLE_PEOPLE, null,
                Constants.COLUMN_PEOPLE_TYPE + " = ?", new String[]{PersonType.SENT.name()},
                null, null, Constants.COLUMN_PEOPLE_NAME);
        count = cursor.getCount();
        if (count > 0) {
            mRecyclerItemList.add(new RecyclerHeaderItem(getString(R.string.sent_requests_header_people)));

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                mRecyclerItemList.add(new RecyclerPersonItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), PersonType.SENT));
            }
        }
        cursor.close();

        cursor = database.query(Constants.TABLE_PEOPLE, null,
                Constants.COLUMN_PEOPLE_TYPE + " = ?", new String[]{PersonType.PEOPLE.name()},
                null, null, Constants.COLUMN_PEOPLE_NAME);
        count = cursor.getCount();
        if (count > 0) {
            if (!mRecyclerItemList.isEmpty()) {
                mRecyclerItemList.add(new RecyclerHeaderItem(getString(R.string.people_header_people)));
            }

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                mRecyclerItemList.add(new RecyclerPersonItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), PersonType.PEOPLE));
            }
        }
        cursor.close();

        database.close();

        if (mRecyclerItemList.isEmpty()) {
            mPeopleTextView.setVisibility(View.VISIBLE);
        } else {
            mPeopleTextView.setVisibility(View.GONE);
        }

        mRecyclerAdapter.notifyDataSetChanged();
    }

    public enum PersonType {

        RECEIVED, SENT, PEOPLE

    }

    private static class RecyclerItem implements Parcelable {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_PERSON = 1;

        public static final Creator<RecyclerItem> CREATOR = new Creator<RecyclerItem>() {

            @Override
            public RecyclerItem createFromParcel(Parcel source) {
                return new RecyclerItem(source);
            }

            @Override
            public RecyclerItem[] newArray(int size) {
                return new RecyclerItem[size];
            }

        };

        private final int mType;
        private final long mId;

        private RecyclerItem(int type, long id) {
            mType = type;
            mId = id;
        }

        private RecyclerItem(Parcel parcel) {
            mType = parcel.readInt();
            mId = parcel.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mType);
            dest.writeLong(mId);
        }

    }

    private static class RecyclerHeaderItem extends RecyclerItem {

        public static final Creator<RecyclerHeaderItem> CREATOR = new Creator<RecyclerHeaderItem>() {

            @Override
            public RecyclerHeaderItem createFromParcel(Parcel source) {
                return new RecyclerHeaderItem(source);
            }

            @Override
            public RecyclerHeaderItem[] newArray(int size) {
                return new RecyclerHeaderItem[size];
            }

        };

        private final String mHeader;

        private RecyclerHeaderItem(String header) {
            super(TYPE_HEADER, header.hashCode());

            mHeader = header;
        }

        private RecyclerHeaderItem(Parcel parcel) {
            super(parcel);

            mHeader = parcel.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeString(mHeader);
        }

    }

    private static class RecyclerPersonItem extends RecyclerItem {

        public static final Creator<RecyclerPersonItem> CREATOR = new Creator<RecyclerPersonItem>() {

            @Override
            public RecyclerPersonItem createFromParcel(Parcel source) {
                return new RecyclerPersonItem(source);
            }

            @Override
            public RecyclerPersonItem[] newArray(int size) {
                return new RecyclerPersonItem[size];
            }

        };

        private final PersonEntity mPersonEntity;
        private final PersonType mPersonType;

        private RecyclerPersonItem(long id, String name, String email, PersonType personType) {
            super(TYPE_PERSON, id);

            mPersonEntity = new PersonEntity(id, name, email);
            mPersonType = personType;
        }

        private RecyclerPersonItem(Parcel parcel) {
            super(parcel);

            mPersonEntity = parcel.readParcelable(null);
            mPersonType = (PersonType) parcel.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeParcelable(mPersonEntity, 0);
            dest.writeSerializable(mPersonType);
        }

    }

    private class RecyclerAdapter extends RecyclerView.Adapter {

        private RecyclerAdapter() {
            setHasStableIds(true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {

                case RecyclerItem.TYPE_HEADER: {
                    View view = inflater.inflate(R.layout.recycler_item_header, parent, false);
                    return new RecyclerHeaderViewHolder(view);
                }

                case RecyclerItem.TYPE_PERSON: {
                    View view = inflater.inflate(R.layout.recycler_item_person, parent, false);
                    return new RecyclerPersonViewHolder(view);
                }

            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerItem recyclerItem = mRecyclerItemList.get(position);
            switch (recyclerItem.mType) {

                case RecyclerItem.TYPE_HEADER:
                    ((RecyclerHeaderViewHolder) holder).bind((RecyclerHeaderItem) recyclerItem);
                    break;

                case RecyclerItem.TYPE_PERSON:
                    ((RecyclerPersonViewHolder) holder).bind((RecyclerPersonItem) recyclerItem);
                    break;

            }
        }

        @Override
        public int getItemViewType(int position) {
            return mRecyclerItemList.get(position).mType;
        }

        @Override
        public long getItemId(int position) {
            return mRecyclerItemList.get(position).mId;
        }

        @Override
        public int getItemCount() {
            return mRecyclerItemList.size();
        }

    }

    private class RecyclerHeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView mHeaderTextView;

        private RecyclerHeaderViewHolder(View itemView) {
            super(itemView);

            mHeaderTextView = (TextView) itemView.findViewById(R.id.header_text_view_header);
        }

        private void bind(RecyclerHeaderItem recyclerItem) {
            mHeaderTextView.setText(recyclerItem.mHeader);
        }

    }

    private class RecyclerPersonViewHolder extends RecyclerView.ViewHolder {

        private final TextView mPictureTextView, mNameTextView, mEmailTextView;
        private final ImageButton mAddImageButton, mRemoveImageButton;
        private final ProgressBar mProgressBar;

        private RecyclerPersonViewHolder(View itemView) {
            super(itemView);

            mPictureTextView = (TextView) itemView.findViewById(R.id.picture_text_view_person);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view_person);
            mEmailTextView = (TextView) itemView.findViewById(R.id.email_text_view_person);
            mAddImageButton = (ImageButton) itemView.findViewById(R.id.add_image_button_person);
            mRemoveImageButton = (ImageButton) itemView.findViewById(R.id.remove_image_button_person);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar_person);
        }

        private void bind(final RecyclerPersonItem recyclerItem) {
            String name = recyclerItem.mPersonEntity.getName();

            mPictureTextView.setText(String.valueOf(name.charAt(0)).toUpperCase());

            mNameTextView.setText(name);

            mEmailTextView.setText(recyclerItem.mPersonEntity.getEmail());

            mAddImageButton.setVisibility(View.GONE);
            mAddImageButton.setOnClickListener(null);

            mRemoveImageButton.setVisibility(View.GONE);
            mRemoveImageButton.setOnClickListener(null);

            mProgressBar.setVisibility(View.GONE);

            final long id = recyclerItem.mPersonEntity.getId();

            switch (recyclerItem.mPersonType) {

                case RECEIVED:
                    mAddImageButton.setVisibility(View.VISIBLE);
                    mAddImageButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new AcceptAsyncTask(RecyclerPersonViewHolder.this).execute(id);
                        }

                    });

                    mRemoveImageButton.setVisibility(View.VISIBLE);
                    mRemoveImageButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new RejectAsyncTask(RecyclerPersonViewHolder.this).execute(id);
                        }

                    });
                    break;

                case SENT:
                    mRemoveImageButton.setVisibility(View.VISIBLE);
                    mRemoveImageButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new CancelAsyncTask(RecyclerPersonViewHolder.this).execute(id);
                        }

                    });
                    break;

                case PEOPLE:
                    mRemoveImageButton.setVisibility(View.VISIBLE);
                    mRemoveImageButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new RemoveAsyncTask(RecyclerPersonViewHolder.this).execute(id);
                        }

                    });

                    itemView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            HomeFragment homeFragment = (HomeFragment) getParentFragment();
                            int mapFragmentIndex = 2;

                            MapFragment mapFragment = (MapFragment) homeFragment.getViewPagerAdapter().getFragment(mapFragmentIndex);
                            if (mapFragment != null) {
                                mapFragment.setSelectedPersonId(id);
                            }

                            homeFragment.getViewPager().setCurrentItem(mapFragmentIndex);
                        }

                    });
                    break;

            }
        }

    }

    private class PeopleAsyncTask extends EndpointAsyncTask<Void, Void, Integer> {

        private static final int RESULT_SUCCESS = 0;
        private static final int RESULT_CONNECTION_ERROR = 1;

        private Person mPerson;
        private ArrayList<Person> mReceivedPersonList, mSentPersonList, mPeoplePersonList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.post(new Runnable() {

                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }

                });
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            super.doInBackground(params);

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();

                mPerson = sMyApi.person(Constants.ANDROID_CLIENT_ID, localPersonId)
                        .execute();

                mReceivedPersonList = (ArrayList<Person>) sMyApi.receivedPeople(Constants.ANDROID_CLIENT_ID, localPersonId)
                        .execute()
                        .getItems();

                mSentPersonList = (ArrayList<Person>) sMyApi.sentPeople(Constants.ANDROID_CLIENT_ID, localPersonId)
                        .execute()
                        .getItems();

                mPeoplePersonList = (ArrayList<Person>) sMyApi.people(Constants.ANDROID_CLIENT_ID, localPersonId)
                        .execute()
                        .getItems();

                return RESULT_SUCCESS;
            } catch (IOException e) {
                Log.e(PeopleAsyncTask.class.getName(), e.getMessage(), e);
            }

            return RESULT_CONNECTION_ERROR;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == RESULT_CONNECTION_ERROR) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_people), Toast.LENGTH_SHORT).show();

                updateRecyclerItemList();

                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }

            ArrayList<Long> receivedIdList = (ArrayList<Long>) mPerson.getReceivedIdList();
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            ArrayList<Long> sentIdList = (ArrayList<Long>) mPerson.getSentIdList();
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            ArrayList<Long> peopleIdList = (ArrayList<Long>) mPerson.getPeopleIdList();
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            MainActivity activity = (MainActivity) getActivity();

            SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PERSON_RECEIVED_IDS, receivedIdList.toString());
            values.put(Constants.COLUMN_PERSON_SENT_IDS, sentIdList.toString());
            values.put(Constants.COLUMN_PERSON_PEOPLE_IDS, peopleIdList.toString());
            database.update(Constants.TABLE_PERSON, values, Constants.COLUMN_PERSON_ID + " = ?",
                    new String[]{activity.getLocalPersonEntity().getIdString()});

            database.delete(Constants.TABLE_PEOPLE, null, null);

            if (mReceivedPersonList != null) {
                for (Person person : mReceivedPersonList) {
                    values = new ContentValues();
                    values.put(Constants.COLUMN_PEOPLE_ID, person.getId());
                    values.put(Constants.COLUMN_PEOPLE_NAME, person.getName());
                    values.put(Constants.COLUMN_PEOPLE_EMAIL, person.getEmail());
                    values.put(Constants.COLUMN_PEOPLE_TYPE, PersonType.RECEIVED.name());
                    database.insert(Constants.TABLE_PEOPLE, null, values);
                }
            }

            if (mSentPersonList != null) {
                for (Person person : mSentPersonList) {
                    values = new ContentValues();
                    values.put(Constants.COLUMN_PEOPLE_ID, person.getId());
                    values.put(Constants.COLUMN_PEOPLE_NAME, person.getName());
                    values.put(Constants.COLUMN_PEOPLE_EMAIL, person.getEmail());
                    values.put(Constants.COLUMN_PEOPLE_TYPE, PersonType.SENT.name());
                    database.insert(Constants.TABLE_PEOPLE, null, values);
                }
            }

            if (mPeoplePersonList != null) {
                for (Person person : mPeoplePersonList) {
                    values = new ContentValues();
                    values.put(Constants.COLUMN_PEOPLE_ID, person.getId());
                    values.put(Constants.COLUMN_PEOPLE_NAME, person.getName());
                    values.put(Constants.COLUMN_PEOPLE_EMAIL, person.getEmail());
                    values.put(Constants.COLUMN_PEOPLE_TYPE, PersonType.PEOPLE.name());
                    values.put(Constants.COLUMN_PEOPLE_LATITUDE, person.getLatitude());
                    values.put(Constants.COLUMN_PEOPLE_LONGITUDE, person.getLongitude());
                    values.put(Constants.COLUMN_PEOPLE_LOCATION_UPDATE_TIME, person.getLocationUpdateTime());
                    database.insert(Constants.TABLE_PEOPLE, null, values);
                }
            }

            database.close();

            updateRecyclerItemList();

            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private class AcceptAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private final RecyclerPersonViewHolder mViewHolder;

        private long mPersonId;

        private AcceptAsyncTask(RecyclerPersonViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewHolder.mAddImageButton.setVisibility(View.GONE);
            mViewHolder.mRemoveImageButton.setVisibility(View.GONE);
            mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Person doInBackground(Long... params) {
            super.doInBackground(params);

            mPersonId = params[0];

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.acceptPerson(Constants.ANDROID_CLIENT_ID, localPersonId, mPersonId)
                        .execute();
            } catch (IOException e) {
                Log.e(AcceptAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);

            mViewHolder.mProgressBar.setVisibility(View.GONE);

            if (person == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_people), Toast.LENGTH_SHORT).show();

                mViewHolder.mAddImageButton.setVisibility(View.VISIBLE);
                mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);
                return;
            }

            ArrayList<Long> receivedIdList = (ArrayList<Long>) person.getReceivedIdList();
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            ArrayList<Long> peopleIdList = (ArrayList<Long>) person.getPeopleIdList();
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            MainActivity activity = (MainActivity) getActivity();

            SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PERSON_RECEIVED_IDS, receivedIdList.toString());
            values.put(Constants.COLUMN_PERSON_PEOPLE_IDS, peopleIdList.toString());
            database.update(Constants.TABLE_PERSON, values, Constants.COLUMN_PERSON_ID + " = ?",
                    new String[]{activity.getLocalPersonEntity().getIdString()});

            values = new ContentValues();
            values.put(Constants.COLUMN_PEOPLE_TYPE, PersonType.PEOPLE.name());
            database.update(Constants.TABLE_PEOPLE, values, Constants.COLUMN_PEOPLE_ID + " = ?",
                    new String[]{String.valueOf(mPersonId)});

            database.close();

            updateRecyclerItemList();
        }

    }

    private class RejectAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private final RecyclerPersonViewHolder mViewHolder;

        private long mPersonId;

        private RejectAsyncTask(RecyclerPersonViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewHolder.mAddImageButton.setVisibility(View.GONE);
            mViewHolder.mRemoveImageButton.setVisibility(View.GONE);
            mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Person doInBackground(Long... params) {
            super.doInBackground(params);

            mPersonId = params[0];

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.rejectPerson(Constants.ANDROID_CLIENT_ID, localPersonId, mPersonId)
                        .execute();
            } catch (IOException e) {
                Log.e(RejectAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);

            mViewHolder.mProgressBar.setVisibility(View.GONE);

            if (person == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_people), Toast.LENGTH_SHORT).show();

                mViewHolder.mAddImageButton.setVisibility(View.VISIBLE);
                mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);
                return;
            }

            ArrayList<Long> receivedIdList = (ArrayList<Long>) person.getReceivedIdList();
            if (receivedIdList == null) {
                receivedIdList = new ArrayList<>();
            }

            MainActivity activity = (MainActivity) getActivity();

            SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PERSON_RECEIVED_IDS, receivedIdList.toString());
            database.update(Constants.TABLE_PERSON, values, Constants.COLUMN_PERSON_ID + " = ?",
                    new String[]{activity.getLocalPersonEntity().getIdString()});

            database.delete(Constants.TABLE_PEOPLE, Constants.COLUMN_PEOPLE_ID + " = ?", new String[]{String.valueOf(mPersonId)});

            database.close();

            updateRecyclerItemList();
        }

    }

    private class CancelAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private final RecyclerPersonViewHolder mViewHolder;

        private long mPersonId;

        private CancelAsyncTask(RecyclerPersonViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewHolder.mRemoveImageButton.setVisibility(View.GONE);
            mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Person doInBackground(Long... params) {
            super.doInBackground(params);

            mPersonId = params[0];

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.cancelPerson(Constants.ANDROID_CLIENT_ID, localPersonId, mPersonId)
                        .execute();
            } catch (IOException e) {
                Log.e(CancelAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);

            mViewHolder.mProgressBar.setVisibility(View.GONE);

            if (person == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_people), Toast.LENGTH_SHORT).show();

                mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);
                return;
            }

            ArrayList<Long> sentIdList = (ArrayList<Long>) person.getSentIdList();
            if (sentIdList == null) {
                sentIdList = new ArrayList<>();
            }

            MainActivity activity = (MainActivity) getActivity();

            SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PERSON_SENT_IDS, sentIdList.toString());
            database.update(Constants.TABLE_PERSON, values, Constants.COLUMN_PERSON_ID + " = ?",
                    new String[]{activity.getLocalPersonEntity().getIdString()});

            database.delete(Constants.TABLE_PEOPLE, Constants.COLUMN_PEOPLE_ID + " = ?", new String[]{String.valueOf(mPersonId)});

            database.close();

            updateRecyclerItemList();
        }

    }

    private class RemoveAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private final RecyclerPersonViewHolder mViewHolder;

        private long mPersonId;

        private RemoveAsyncTask(RecyclerPersonViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewHolder.mRemoveImageButton.setVisibility(View.GONE);
            mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Person doInBackground(Long... params) {
            super.doInBackground(params);

            mPersonId = params[0];

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.removePerson(Constants.ANDROID_CLIENT_ID, localPersonId, mPersonId)
                        .execute();
            } catch (IOException e) {
                Log.e(RemoveAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);

            mViewHolder.mProgressBar.setVisibility(View.GONE);

            if (person == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_people), Toast.LENGTH_SHORT).show();

                mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);
                return;
            }

            ArrayList<Long> peopleIdList = (ArrayList<Long>) person.getPeopleIdList();
            if (peopleIdList == null) {
                peopleIdList = new ArrayList<>();
            }

            MainActivity activity = (MainActivity) getActivity();

            SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PERSON_PEOPLE_IDS, peopleIdList.toString());
            database.update(Constants.TABLE_PERSON, values, Constants.COLUMN_PERSON_ID + " = ?",
                    new String[]{activity.getLocalPersonEntity().getIdString()});

            database.delete(Constants.TABLE_PEOPLE, Constants.COLUMN_PEOPLE_ID + " = ?", new String[]{String.valueOf(mPersonId)});

            database.close();

            updateRecyclerItemList();
        }

    }

}
