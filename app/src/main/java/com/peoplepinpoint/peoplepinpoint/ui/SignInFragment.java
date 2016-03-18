package com.peoplepinpoint.peoplepinpoint.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.people_pinpoint.myApi.model.Person;
import com.peoplepinpoint.peoplepinpoint.Constants;
import com.peoplepinpoint.peoplepinpoint.R;
import com.peoplepinpoint.peoplepinpoint.backend.EndpointAsyncTask;
import com.peoplepinpoint.peoplepinpoint.entity.PersonEntity;
import com.peoplepinpoint.peoplepinpoint.service.LocationService;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Manoj Khanna
 */

public class SignInFragment extends Fragment {

    private static final String PREFERENCES_EMAIL = "email";

    private TextView mEmailTextView, mPasswordTextView;

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TextView startHereTextView = (TextView) view.findViewById(R.id.start_here_text_view_sign_in);
            startHereTextView.setVisibility(View.INVISIBLE);
            startHereTextView.setTranslationY(-150.0f);
        }

        mEmailTextView = (TextView) view.findViewById(R.id.email_edit_text_sign_in);
        mPasswordTextView = (TextView) view.findViewById(R.id.password_edit_text_sign_in);

        if (savedInstanceState == null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(SignInFragment.class.getName(), Context.MODE_PRIVATE);
            String email = preferences.getString(PREFERENCES_EMAIL, null);
            if (email != null) {
                mEmailTextView.setText(email);
                mPasswordTextView.requestFocus();
            }
        }

        Button signInButton = (Button) view.findViewById(R.id.sign_in_button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean valid = true;

                String email = mEmailTextView.getText().toString();
                if (email.isEmpty()) {
                    mEmailTextView.setError(getString(R.string.email_error_sign_in));
                    valid = false;
                }

                String password = mPasswordTextView.getText().toString();
                if (password.isEmpty()) {
                    mPasswordTextView.setError(getString(R.string.password_error_sign_in));
                    valid = false;
                }

                if (valid) {
                    new SignInAsyncTask().execute(email, password);
                }
            }

        });

        Button signUpButton = (Button) view.findViewById(R.id.sign_up_button_sign_in);
        signUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left,
                                R.anim.fragment_enter_right, R.anim.fragment_exit_right)
                        .replace(R.id.fragment_container_main, SignUpFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }

        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final TextView startHereTextView = (TextView) view.findViewById(R.id.start_here_text_view_sign_in);

            ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(startHereTextView, "alpha", 0.0f, 1.0f);
            fadeInAnimator.setDuration(500);
            fadeInAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    startHereTextView.setVisibility(View.VISIBLE);
                }

            });

            ObjectAnimator bounceAnimator = ObjectAnimator.ofFloat(startHereTextView, "translationY", -150.0f, 0.0f);
            bounceAnimator.setDuration(1000);
            bounceAnimator.setInterpolator(new BounceInterpolator());

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(fadeInAnimator, bounceAnimator);
            animatorSet.setStartDelay(500);
            animatorSet.start();
        }
    }

    private class SignInAsyncTask extends EndpointAsyncTask<String, Void, Integer> {

        private static final int RESULT_SUCCESS = 0;
        private static final int RESULT_CONNECTION_ERROR = 1;
        private static final int RESULT_CREDENTIALS_ERROR = 2;

        private Person mPerson;

        private SignInAsyncTask() {
            super(getActivity(), getString(R.string.signing_in_progress_sign_in));
        }

        @Override
        protected Integer doInBackground(String... params) {
            super.doInBackground(params);

            try {
                mPerson = sMyApi.signInPerson(Constants.ANDROID_CLIENT_ID, params[0], params[1])
                        .execute();
                if (mPerson == null) {
                    return RESULT_CREDENTIALS_ERROR;
                }

                return RESULT_SUCCESS;
            } catch (IOException e) {
                Log.e(SignInAsyncTask.class.getName(), e.getMessage(), e);
            }

            return RESULT_CONNECTION_ERROR;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            switch (result) {

                case RESULT_SUCCESS:
                    long id = mPerson.getId();
                    String name = mPerson.getName(), email = mPerson.getEmail();
                    ArrayList<Long> sentIdList = (ArrayList<Long>) mPerson.getSentIdList(),
                            receivedIdList = (ArrayList<Long>) mPerson.getReceivedIdList(),
                            peopleIdList = (ArrayList<Long>) mPerson.getPeopleIdList();

                    if (sentIdList == null) {
                        sentIdList = new ArrayList<>();
                    }

                    if (receivedIdList == null) {
                        receivedIdList = new ArrayList<>();
                    }

                    if (peopleIdList == null) {
                        peopleIdList = new ArrayList<>();
                    }

                    MainActivity activity = (MainActivity) getActivity();

                    SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_PERSON_ID, id);
                    values.put(Constants.COLUMN_PERSON_NAME, name);
                    values.put(Constants.COLUMN_PERSON_EMAIL, email);
                    values.put(Constants.COLUMN_PERSON_SENT_IDS, sentIdList.toString());
                    values.put(Constants.COLUMN_PERSON_RECEIVED_IDS, receivedIdList.toString());
                    values.put(Constants.COLUMN_PERSON_PEOPLE_IDS, peopleIdList.toString());
                    database.insert(Constants.TABLE_PERSON, null, values);
                    database.close();

                    PersonEntity localPersonEntity = new PersonEntity(id, name, email);
                    activity.setLocalPersonEntity(localPersonEntity);

                    Intent intent = new Intent(activity, LocationService.class);
                    intent.putExtra(LocationService.EXTRA_LOCAL_PERSON_ENTITY, localPersonEntity);
                    activity.startService(intent);

                    SharedPreferences preferences = activity.getSharedPreferences(SignInFragment.class.getName(), Context.MODE_PRIVATE);
                    preferences.edit()
                            .putString(PREFERENCES_EMAIL, email)
                            .apply();

                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left,
                                    R.anim.fragment_enter_right, R.anim.fragment_exit_right)
                            .replace(R.id.fragment_container_main, HomeFragment.newInstance())
                            .commit();
                    break;

                case RESULT_CONNECTION_ERROR:
                    Toast.makeText(getActivity(), getString(R.string.connection_error_sign_in), Toast.LENGTH_SHORT).show();
                    break;

                case RESULT_CREDENTIALS_ERROR:
                    mEmailTextView.setError(getString(R.string.credentials_error_sign_in));
                    mPasswordTextView.setError(getString(R.string.credentials_error_sign_in));
                    break;

            }
        }

    }

}
