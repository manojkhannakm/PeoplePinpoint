package com.manojkhannakm.peoplepinpoint.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appspot.people_pinpoint.myApi.model.Person;
import com.manojkhannakm.peoplepinpoint.Constants;
import com.manojkhannakm.peoplepinpoint.R;
import com.manojkhannakm.peoplepinpoint.backend.EndpointAsyncTask;

import java.io.IOException;

/**
 * @author Manoj Khanna
 */

public class SignUpFragment extends Fragment {

    private EditText mNameEditText, mEmailEditText, mPasswordEditText, mConfirmPasswordEditText;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mNameEditText = (EditText) view.findViewById(R.id.name_edit_text_sign_up);
        mEmailEditText = (EditText) view.findViewById(R.id.email_edit_text_sign_up);
        mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text_sign_up);
        mConfirmPasswordEditText = (EditText) view.findViewById(R.id.confirm_password_edit_text_sign_up);

        Button signUpButton = (Button) view.findViewById(R.id.sign_up_button_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean valid = true;

                String name = mNameEditText.getText().toString();
                if (name.matches("[^a-zA-Z ]")) {
                    mNameEditText.setError(getString(R.string.name_error_1_sign_up));
                    valid = false;
                } else if (name.startsWith(" ") || name.endsWith(" ")) {
                    mNameEditText.setError(getString(R.string.name_error_2_sign_up));
                    valid = false;
                } else if (name.contains("  ")) {
                    mNameEditText.setError(getString(R.string.name_error_3_sign_up));
                    valid = false;
                } else if (name.length() < 5) {
                    mNameEditText.setError(getString(R.string.name_error_4_sign_up));
                    valid = false;
                }

                String email = mEmailEditText.getText().toString();
                if (!email.matches("^.+@.+\\..+$")) {
                    mEmailEditText.setError(getString(R.string.email_error_1_sign_up));
                    valid = false;
                }

                String password = mPasswordEditText.getText().toString();
                if (!password.matches("^.{6,}$")) {
                    mPasswordEditText.setError(getString(R.string.password_error_sign_up));
                    valid = false;
                }

                String confirmPassword = mConfirmPasswordEditText.getText().toString();
                if (!confirmPassword.equals(password)) {
                    mConfirmPasswordEditText.setError(getString(R.string.confirm_password_error_sign_up));
                    valid = false;
                }

                if (valid) {
                    new SignUpAsyncTask().execute(name, email, password);
                }
            }

        });

        return view;
    }

    private class SignUpAsyncTask extends EndpointAsyncTask<String, Void, Integer> {

        private static final int RESULT_SUCCESS = 0;
        private static final int RESULT_CONNECTION_ERROR = 1;
        private static final int RESULT_EMAIL_ERROR = 2;

        private SignUpAsyncTask() {
            super(getActivity(), getString(R.string.signing_up_progress_sign_up));
        }

        @Override
        protected Integer doInBackground(String... params) {
            super.doInBackground(params);

            try {
                Person person = sMyApi.signUpPerson(Constants.ANDROID_CLIENT_ID, params[0], params[1], params[2])
                        .execute();
                if (person == null) {
                    return RESULT_EMAIL_ERROR;
                }

                return RESULT_SUCCESS;
            } catch (IOException e) {
                Log.e(SignUpAsyncTask.class.getName(), e.getMessage(), e);
            }

            return RESULT_CONNECTION_ERROR;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            switch (result) {

                case RESULT_SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.sign_up_successful_sign_up), Toast.LENGTH_SHORT).show();

                    getFragmentManager().popBackStack();
                    break;

                case RESULT_CONNECTION_ERROR:
                    Toast.makeText(getActivity(), getString(R.string.connection_error_sign_up), Toast.LENGTH_SHORT).show();
                    break;

                case RESULT_EMAIL_ERROR:
                    mEmailEditText.setError(getString(R.string.email_error_2_sign_up));
                    break;

            }
        }

    }

}
