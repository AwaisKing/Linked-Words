package com.keiferstone.nonet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.keiferstone.nonet.ConnectionStatus.CONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.DISCONNECTED;
import static com.keiferstone.nonet.ConnectionStatus.UNKNOWN;

public abstract class ConnectivityReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectivityReceiver.class.getSimpleName();

    public static IntentFilter getIntentFilter() {
        return new IntentFilter(CONNECTIVITY_ACTION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "Received " + CONNECTIVITY_ACTION);

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    onConnectivityChanged(activeNetwork.isConnectedOrConnecting() ? CONNECTED : DISCONNECTED);
                    return;
                }
            }

            onConnectivityChanged(UNKNOWN);
        }
    }

    protected abstract void onConnectivityChanged(@ConnectionStatus int connectionStatus);
}
