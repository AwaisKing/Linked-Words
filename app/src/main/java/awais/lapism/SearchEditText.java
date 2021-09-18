package awais.lapism;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.google.android.material.textfield.TextInputEditText;

public final class SearchEditText extends TextInputEditText {
    private MaterialSearchView searchView;

    public SearchEditText(final Context context) {
        super(context);
    }

    public SearchEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setSearchView(final MaterialSearchView searchView) {
        this.searchView = searchView;
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP
                && searchView != null && searchView.isSearchOpen()) {
            searchView.close(true);
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}