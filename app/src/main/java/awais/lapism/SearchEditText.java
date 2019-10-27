package awais.lapism;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class SearchEditText extends AppCompatEditText {
    private MaterialSearchView mSearchView;

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setSearchView(MaterialSearchView searchView) {
        mSearchView = searchView;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mSearchView.getShouldHideOnKeyboardClose()) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (mSearchView != null && mSearchView.isSearchOpen()) {
                    mSearchView.close(true);
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}