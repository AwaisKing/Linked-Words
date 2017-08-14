package awais.floatysearch.widgets;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import awais.backworddictionary.R;
import awais.floatysearch.SearchResultItem;
import awais.floatysearch.onSearchActionsListener;
import awais.floatysearch.onSearchListener;
import awais.floatysearch.onSimpleSearchActionsListener;
import awais.floatysearch.utils.Util;


@SuppressWarnings("unused")
public class MaterialSearchView extends FrameLayout implements View.OnClickListener, onSearchActionsListener, onSimpleSearchActionsListener {
    private final EditText mSearchEditText;
    private final ImageView mClearSearch;
    private final ProgressBar mProgressBar;
    private onSearchListener mOnSearchListener;
    private final View lineDivider;
    private final CardView cardLayout;
    private final RelativeLayout searchLayout;
    private final ListView mFrameLayout;
    private final Context mContext;
    private SearchViewResults searchViewResults;
    private final FrameLayout progressBarLayout;
    private onSimpleSearchActionsListener searchListener;
    private final TextView noResultsFoundText;

//    public void setHintText(String hint) {
//        mSearchEditText.setHint(hint);
//    }

    public CardView getCardLayout() {
        return cardLayout;
    }

    public ListView getListview() {
        return mFrameLayout;
    }

    public View getLineDivider() {
        return lineDivider;
    }

    final Animation fade_in;
    final Animation fade_out;

    @Override
    public void onItemClicked(SearchResultItem item) {
        this.searchListener.onItemClicked(item);
    }

    @Override
    public void showProgress(boolean show) {
        if(show) {
            progressBarLayout.setVisibility(VISIBLE);
            mProgressBar.setVisibility(VISIBLE);
            noResultsFoundText.setVisibility(GONE);
        } else progressBarLayout.setVisibility(GONE);

    }

    @Override
    public void listEmpty() {
        progressBarLayout.setVisibility(VISIBLE);
        noResultsFoundText.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
    }

    @Override
    public void onScroll() {
        this.searchListener.onScroll();
    }

    @Override
    public void error(String localizedMessage) {
        this.searchListener.error(localizedMessage);
    }

    public MaterialSearchView(final Context context) {
        super(context, null, -1);

        final LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.toolbar_searchview, this);

        mContext = context;
        cardLayout = findViewById(R.id.card_search);
        mFrameLayout = findViewById(R.id.material_search_container);
        searchLayout = findViewById(R.id.view_search);
        lineDivider = findViewById(R.id.line_divider);
        noResultsFoundText = findViewById(R.id.textView13);
        mSearchEditText = findViewById(R.id.edit_text_search);
        ImageView backArrowImg = findViewById(R.id.image_search_back);
        mClearSearch = findViewById(R.id.clearSearch);
        progressBarLayout = findViewById(R.id.progressLayout);
        mProgressBar = findViewById(R.id.progressBar);
        fade_in = AnimationUtils.loadAnimation(getContext().getApplicationContext(), android.R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getContext().getApplicationContext(), android.R.anim.fade_out);

        mClearSearch.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        backArrowImg.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        mSearchEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mSearchEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH
                | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchEditText.setTextIsSelectable(true);
        mSearchEditText.setFocusable(true);
        mSearchEditText.setFocusableInTouchMode(true);
        mSearchEditText.setCursorVisible(true);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mOnSearchListener != null) {
                    mOnSearchListener.onSearch(getSearchQuery(), false);
                    onQuery(getSearchQuery());
                }
                toggleClearSearchButton(s);
            }
        });
        mSearchEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH)) {
                    final String query = getSearchQuery();
                    if (!TextUtils.isEmpty(query) && mOnSearchListener != null)
                        mOnSearchListener.onSearch(query, true);
                    return true;
                }
                return false;
            }
        });

//        mSearchEditText.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .toggleSoftInput(InputMethodManager.SHOW_FORCED,
//                            InputMethodManager.HIDE_IMPLICIT_ONLY);
//                } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
//            }
//        });

        findViewById(R.id.image_search_back).setOnClickListener(this);
        mClearSearch.setOnClickListener(this);
        setVisibility(View.GONE);
        clearAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            if (!isEnabled()) return;
            super.setEnabled(false);
            super.setEnabled(true);
            if (!getSearchView().isEnabled()) return;
            getSearchView().setEnabled(false);
            getSearchView().setEnabled(true);
        } catch (Exception e) {
            Log.d("AWAISKING_APP", "", e);
        }
    }

    public void onQuery(String query) {
        String trim = query.trim();
        if(TextUtils.isEmpty(trim)) progressBarLayout.setVisibility(GONE);
        if (searchViewResults != null) {
            searchViewResults.updateSequence(trim);
        } else {
            try {
                searchViewResults = new SearchViewResults(mContext, trim);
                searchViewResults.setListView(mFrameLayout);
                searchViewResults.setSearchProvidersListener(this);
            } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
        }
    }

    public void selectAll() {
        if (!getSearchQuery().equals("")) mSearchEditText.selectAll();
    }

    public void setOnSearchListener(final onSearchListener l) {
        mOnSearchListener = l;
    }

    public void setSearchResultsListener(onSimpleSearchActionsListener listener) {
        this.searchListener = listener;
    }

    public void setSearchQuery(final String query) {
        mSearchEditText.setText(query);
        toggleClearSearchButton(query);
    }

    public String getSearchQuery() {
        return mSearchEditText.getText() != null ? mSearchEditText.getText().toString() : "";
    }

    public boolean isSearchViewVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void display() {
        if (isSearchViewVisible()) return;
        setVisibility(View.VISIBLE);
//        mOnSearchListener.searchViewOpened();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Animator animator = ViewAnimationUtils.createCircularReveal(cardLayout,
                    cardLayout.getWidth() - Util.dpToPx(getContext(), 56*2),
                    Util.dpToPx(getContext(), 23), 0,
                    (float) Math.hypot(cardLayout.getWidth(), cardLayout.getHeight()));
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLayout.startAnimation(fade_in);
                    try {
                        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);
                    } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
                }
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            cardLayout.setVisibility(View.VISIBLE);
            if (cardLayout.getVisibility() == View.VISIBLE) {
                animator.setDuration(300);
                animator.start();
                cardLayout.setEnabled(true);
            }
            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mFrameLayout.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } else {
            cardLayout.setVisibility(View.VISIBLE);
            cardLayout.setEnabled(true);

            mFrameLayout.setVisibility(View.VISIBLE);
            try {
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
        }
    }

    public void hide() {
        if (!isSearchViewVisible()) return;
//        try{
//            mOnSearchListener.searchViewClosed();
//        } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Animator animatorHide = ViewAnimationUtils.createCircularReveal(cardLayout,
                    cardLayout.getWidth() - Util.dpToPx(getContext(), 56),
                    Util.dpToPx(getContext(), 23),
                    (float) Math.hypot(cardLayout.getWidth(), cardLayout.getHeight()), 0);
            animatorHide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    searchLayout.startAnimation(fade_out);
                    searchLayout.setVisibility(View.INVISIBLE);
                    cardLayout.setVisibility(View.GONE);
                    try {
                        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(searchLayout.getWindowToken(), 0);
                    } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
                    mFrameLayout.setVisibility(View.GONE);
                    clearSearch();
                    setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            animatorHide.setDuration(300);
            animatorHide.start();
        } else {
            try {
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchLayout.getWindowToken(), 0);
            } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
            searchLayout.startAnimation(fade_out);
            searchLayout.setVisibility(View.INVISIBLE);
            cardLayout.setVisibility(View.GONE);
            clearSearch();
            setVisibility(View.GONE);
        }
    }

    public EditText getSearchView(){
        return mSearchEditText;
    }

    public static WindowManager.LayoutParams getSearchViewLayoutParams(final Activity activity) {
        final Rect rect = new Rect();
        final Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        final int statusBarHeight = rect.top;

//        final TypedArray actionBarSize = activity.getTheme().obtainStyledAttributes(new int[]{R.attr.actionBarSize});
//        actionBarSize.recycle();
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                rect.right, /* This ensures we don't go under the navigation bar in landscape */
                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // needs draw on top permission
                WindowManager.LayoutParams.LAST_APPLICATION_WINDOW,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0; // statusBarHeight;
        return params;
    }

    private void toggleClearSearchButton(final CharSequence query) {
        mClearSearch.setVisibility(!TextUtils.isEmpty(query) ? View.VISIBLE : View.INVISIBLE);
    }

    private void clearSearch() {
        mSearchEditText.setText("");
        mClearSearch.setVisibility(View.INVISIBLE);
    }

    private void onCancelSearch() {
        if (mOnSearchListener != null) {
            mOnSearchListener.onCancelSearch();
        } else {
            hide();
        }
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_UP && event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            onCancelSearch();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.image_search_back) onCancelSearch();
        else if (id == R.id.clearSearch) clearSearch();
    }
}