package com.keiferstone.nonet;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

class SnackbarFactory {

    static Snackbar getSnackbar(Context context) {
        return getSnackbar(context, R.string.no_server_connection_message);
    }

    static Snackbar getSnackbar(Context context, String message) {
        View view = extractView(context);
        if (view != null) return Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

        return null;
    }

    static Snackbar getSnackbar(Context context, @StringRes int messageRes) {
        View view = extractView(context);
        if (view != null) return Snackbar.make(view, messageRes, Snackbar.LENGTH_INDEFINITE);
        return null;
    }

    @Nullable
    private static View extractView(Context context) {
        if (context instanceof Activity)
            return ((Activity) context).findViewById(android.R.id.content);
        return null;
    }
}
