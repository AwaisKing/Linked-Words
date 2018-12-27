package com.keiferstone.nonet;

import android.os.AsyncTask;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.keiferstone.nonet.ConnectionStatus.CONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.DISCONNECTED;

@SuppressWarnings( "UnusedReturnValue" )
class PollTask extends AsyncTask<Void, Void, Integer> {
    private static OkHttpClient client;
    private final Configuration configuration;
    private final OnPollCompletedListener listener;

    private PollTask(Configuration configuration, OnPollCompletedListener listener) {
        this.configuration = configuration;
        this.listener = listener;

        if (client == null) client = new OkHttpClient.Builder()
                .connectTimeout(configuration.getTimeout(), TimeUnit.SECONDS).build();
    }

    static PollTask run(Configuration configuration, OnPollCompletedListener listener) {
        PollTask pollTask = new PollTask(configuration, listener);
        return (PollTask) pollTask.execute();
    }

    @ConnectionStatus
    @Override
    protected Integer doInBackground(Void... params) {
        Request request = new Request.Builder().url(configuration.getEndpoint()).build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            return DISCONNECTED;
        }

        if (response.isSuccessful()) return CONNECTED;

        return DISCONNECTED;
    }

    @Override
    protected void onPostExecute(@ConnectionStatus Integer integer) {
        if (listener != null) listener.onPollCompleted(integer);
    }

    interface OnPollCompletedListener {
        void onPollCompleted(@ConnectionStatus int connectionStatus);
    }
}
