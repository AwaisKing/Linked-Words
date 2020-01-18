package awais.lapism;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class SearchEditText extends AppCompatEditText {
    private MaterialSearchView mSearchView;

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
        mSearchView = searchView;
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP &&
                mSearchView != null && mSearchView.isSearchOpen()) {
            mSearchView.close(true);
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}