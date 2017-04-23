package com.manojkhannakm.peoplepinpoint.backend;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.appspot.people_pinpoint.myApi.MyApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.manojkhannakm.peoplepinpoint.Constants;

/**
 * @author Manoj Khanna
 */

public class EndpointAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected static MyApi sMyApi;

    private final ProgressDialog mProgressDialog;

    public EndpointAsyncTask() {
        mProgressDialog = null;
    }

    public EndpointAsyncTask(Context context, String progressMessage) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(progressMessage);
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Result doInBackground(Params... params) {
        if (sMyApi == null) {
            sMyApi = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(Constants.API_URL)
                    .build();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
