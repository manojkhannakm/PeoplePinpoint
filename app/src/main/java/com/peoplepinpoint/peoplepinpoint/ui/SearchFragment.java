package com.peoplepinpoint.peoplepinpoint.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.people_pinpoint.myApi.model.Person;
import com.appspot.people_pinpoint.myApi.model.PersonCollection;
import com.peoplepinpoint.peoplepinpoint.Constants;
import com.peoplepinpoint.peoplepinpoint.R;
import com.peoplepinpoint.peoplepinpoint.backend.EndpointAsyncTask;
import com.peoplepinpoint.peoplepinpoint.entity.PersonEntity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Manoj Khanna
 */

public class SearchFragment extends Fragment implements HomeFragment.OnPageSelectedListener {

    private static final String ARG_PERSON_LIST = "personList";
    private static final String ARG_QUERY = "query";

    private TextView mSearchTextView, mNoPeopleFoundTextView;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mRecyclerAdapter;
    private ArrayList<PersonEntity> mPersonEntityList;
    private String mQuery;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mSearchTextView = (TextView) view.findViewById(R.id.search_text_view_search);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar_search);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_search);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mNoPeopleFoundTextView = (TextView) view.findViewById(R.id.no_people_found_text_view_search);

        if (savedInstanceState != null) {
            //noinspection unchecked
            mPersonEntityList = savedInstanceState.getParcelableArrayList(ARG_PERSON_LIST);
            mQuery = savedInstanceState.getString(ARG_QUERY);

            if (!mPersonEntityList.isEmpty()) {
                mSearchTextView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else if (!mQuery.isEmpty()) {
                mSearchTextView.setVisibility(View.GONE);
                mNoPeopleFoundTextView.setVisibility(View.VISIBLE);
            }
        } else {
            mPersonEntityList = new ArrayList<>();
            mQuery = "";
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ARG_PERSON_LIST, mPersonEntityList);
        outState.putString(ARG_QUERY, mQuery);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search_item_home);
        searchMenuItem.setVisible(true);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getString(R.string.query_hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query.toLowerCase();
                if (mQuery.isEmpty()) {
                    return false;
                }

                new SearchAsyncTask().execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
    }

    @Override
    public void onPageSelected() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

        private String mSentIdListString, mReceivedIdListString, mPeopleIdListString;

        private RecyclerAdapter() {
            updateIdListStrings();

            setHasStableIds(true);

            registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

                @Override
                public void onChanged() {
                    updateIdListStrings();
                }

            });
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.recycler_item_person, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            PersonEntity personEntity = mPersonEntityList.get(position);
            String idString = personEntity.getIdString();
            holder.bind(personEntity,
                    mSentIdListString.contains(idString),
                    mReceivedIdListString.contains(idString),
                    mPeopleIdListString.contains(idString));
        }

        @Override
        public long getItemId(int position) {
            return mPersonEntityList.get(position).getId();
        }

        @Override
        public int getItemCount() {
            return mPersonEntityList.size();
        }

        private void updateIdListStrings() {
            SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);
            Cursor cursor = database.query(Constants.TABLE_PERSON, new String[]{
                    Constants.COLUMN_PERSON_SENT_IDS,
                    Constants.COLUMN_PERSON_RECEIVED_IDS,
                    Constants.COLUMN_PERSON_PEOPLE_IDS
            }, null, null, null, null, null);
            cursor.moveToFirst();
            mSentIdListString = cursor.getString(0);
            mReceivedIdListString = cursor.getString(1);
            mPeopleIdListString = cursor.getString(2);
            cursor.close();
            database.close();
        }

    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mPictureTextView, mNameTextView, mEmailTextView;
        private ImageButton mAddImageButton, mRemoveImageButton;
        private ProgressBar mProgressBar;

        private RecyclerViewHolder(final View itemView) {
            super(itemView);

            mPictureTextView = (TextView) itemView.findViewById(R.id.picture_text_view_person);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view_person);
            mEmailTextView = (TextView) itemView.findViewById(R.id.email_text_view_person);
            mAddImageButton = (ImageButton) itemView.findViewById(R.id.add_image_button_person);

            mRemoveImageButton = (ImageButton) itemView.findViewById(R.id.remove_image_button_person);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRemoveImageButton.getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.removeRule(RelativeLayout.END_OF);
                layoutParams.removeRule(RelativeLayout.RIGHT_OF);
            } else {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            }

            mRemoveImageButton.setLayoutParams(layoutParams);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar_person);
            layoutParams = (RelativeLayout.LayoutParams) mProgressBar.getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.removeRule(RelativeLayout.END_OF);
                layoutParams.removeRule(RelativeLayout.RIGHT_OF);
            } else {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            }

            mProgressBar.setLayoutParams(layoutParams);
        }

        private void highlightTextView(TextView textView) {
            String text = textView.getText().toString();
            SpannableString spannableText = new SpannableString(text);

            text = text.toLowerCase();
            int i = text.indexOf(mQuery, 0),
                    color = getResources().getColor(R.color.accent);
            while (i != -1) {
                spannableText.setSpan(new ForegroundColorSpan(color), i, i + mQuery.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                i = text.indexOf(mQuery, i + mQuery.length());
            }

            textView.setText(spannableText);
        }

        private void bind(final PersonEntity personEntity, boolean sentId, boolean receivedId, boolean peopleId) {
            String name = personEntity.getName();

            mPictureTextView.setText(String.valueOf(name.charAt(0)).toUpperCase());

            mNameTextView.setText(name);
            highlightTextView(mNameTextView);

            mEmailTextView.setText(personEntity.getEmail());
            highlightTextView(mEmailTextView);

            mAddImageButton.setVisibility(View.GONE);
            mAddImageButton.setOnClickListener(null);

            mRemoveImageButton.setVisibility(View.GONE);
            mRemoveImageButton.setOnClickListener(null);

            mProgressBar.setVisibility(View.GONE);

            final Long id = personEntity.getId();

            if (!receivedId && !peopleId) {
                if (!sentId) {
                    mAddImageButton.setVisibility(View.VISIBLE);
                } else {
                    mRemoveImageButton.setVisibility(View.VISIBLE);
                }

                mAddImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new AddAsyncTask(RecyclerViewHolder.this).execute(id);
                    }

                });

                mRemoveImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new CancelAsyncTask(RecyclerViewHolder.this).execute(id);
                    }

                });
            }
        }

    }

    private class SearchAsyncTask extends EndpointAsyncTask<Void, Void, PersonCollection> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mSearchTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mNoPeopleFoundTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected PersonCollection doInBackground(Void... params) {
            super.doInBackground(params);

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.searchPeople(Constants.ANDROID_CLIENT_ID, mQuery, localPersonId)
                        .execute();
            } catch (IOException e) {
                Log.e(SearchAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(PersonCollection personCollection) {
            super.onPostExecute(personCollection);

            mProgressBar.setVisibility(View.GONE);

            if (personCollection == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_search), Toast.LENGTH_SHORT).show();

                mSearchTextView.setVisibility(View.VISIBLE);
                return;
            }

            ArrayList<Person> personList = (ArrayList<Person>) personCollection.getItems();
            if (personList == null) {
                mNoPeopleFoundTextView.setVisibility(View.VISIBLE);
                return;
            }

            mPersonEntityList = new ArrayList<>();
            for (Person person : personList) {
                PersonEntity personEntity = new PersonEntity(person.getId(), person.getName(), person.getEmail());
                mPersonEntityList.add(personEntity);
            }

            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerAdapter.notifyDataSetChanged();
        }

    }

    private class AddAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private RecyclerViewHolder mViewHolder;

        private AddAsyncTask(RecyclerViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewHolder.mAddImageButton.setVisibility(View.GONE);
            mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Person doInBackground(Long... params) {
            super.doInBackground(params);

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.addPerson(Constants.ANDROID_CLIENT_ID, localPersonId, params[0])
                        .execute();
            } catch (IOException e) {
                Log.e(AddAsyncTask.class.getName(), e.getMessage(), e);
            }

            return null;
        }


        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);

            mViewHolder.mProgressBar.setVisibility(View.GONE);

            if (person == null) {
                Toast.makeText(getActivity(), getString(R.string.connection_error_search), Toast.LENGTH_SHORT).show();

                mViewHolder.mAddImageButton.setVisibility(View.VISIBLE);
                return;
            }

            mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);

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
            database.close();
        }

    }

    private class CancelAsyncTask extends EndpointAsyncTask<Long, Void, Person> {

        private RecyclerViewHolder mViewHolder;

        private CancelAsyncTask(RecyclerViewHolder viewHolder) {
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

            try {
                long localPersonId = ((MainActivity) getActivity()).getLocalPersonEntity().getId();
                return sMyApi.cancelPerson(Constants.ANDROID_CLIENT_ID, localPersonId, params[0])
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
                Toast.makeText(getActivity(), getString(R.string.connection_error_search), Toast.LENGTH_SHORT).show();

                mViewHolder.mRemoveImageButton.setVisibility(View.VISIBLE);
                return;
            }

            mViewHolder.mAddImageButton.setVisibility(View.VISIBLE);

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
            database.close();
        }

    }

}
