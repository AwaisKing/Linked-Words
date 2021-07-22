package awais.backworddictionary.interfaces;

import android.view.View;

import awais.backworddictionary.BuildConfig;

public class AdapterClickListener implements View.OnClickListener, View.OnLongClickListener {
    @Override
    public void onClick(final View view) { }

    @Override
    public boolean onLongClick(final View view) {
        return view != null && BuildConfig.DEBUG;
    }
}