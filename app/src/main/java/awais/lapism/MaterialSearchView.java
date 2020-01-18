package awais.lapism;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.FadeAnimator;
import awais.backworddictionary.helpers.Utils;

public class MaterialSearchView extends FrameLayout implements View.OnClickListener {
    public static final int SPEECH_REQUEST_CODE = 4000;
    public static int mIconColor = Color.BLACK;
    private final int mAnimationDuration = 317;
    private final Context mContext;
    private SearchArrowDrawable mSearchArrow = null;
    private RecyclerView.Adapter<?> mAdapter = null;
    private OnQueryTextListener mOnQueryChangeListener = null;
    private OnOpenCloseListener mOnOpenCloseListener = null;
    private RecyclerView mRecyclerView;
    private CardView mCardView;
    private ProgressBar mProgressBar;
    private SearchEditText mSearchEditText;
    private View mMenuItemView = null, mShadowView, mDividerView;
    private ImageView mBackImageView, mVoiceImageView, mEmptyImageView;
    private CharSequence mOldQueryText, mUserQuery = "";
    private int mMenuItemCx = -1;
    private boolean mIsSearchOpen = false, mVoice = false;
    private float mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    private InputMethodManager imm;

    public MaterialSearchView(final Context context) {
        this(context, null);
    }

    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    private void setIconColor(@ColorInt final int color) {
        mIconColor = color;
        final ColorFilter colorFilter = new PorterDuffColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);
        mBackImageView.setColorFilter(colorFilter);
        mVoiceImageView.setColorFilter(colorFilter);
        mEmptyImageView.setColorFilter(colorFilter);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true);

        mCardView = findViewById(R.id.cardView);

        mRecyclerView = findViewById(R.id.recyclerView_result);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(new FadeAnimator());
        mRecyclerView.setVisibility(View.GONE);

        mDividerView = findViewById(R.id.view_divider);
        mDividerView.setVisibility(View.GONE);

        mShadowView = findViewById(R.id.view_shadow);
        mShadowView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.shadow_color, null));
        mShadowView.setOnClickListener(this);
        mShadowView.setVisibility(View.GONE);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        mVoiceImageView = findViewById(R.id.imageView_mic);
        mVoiceImageView.setOnClickListener(this);
        mVoiceImageView.setVisibility(View.GONE);

        mEmptyImageView = findViewById(R.id.imageView_clear);
        mEmptyImageView.setOnClickListener(this);
        mEmptyImageView.setVisibility(View.GONE);

        mSearchEditText = findViewById(R.id.searchEditText_input);
        mSearchEditText.setSearchView(this);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onSearchTextChanged(charSequence);
            }
        });
        mSearchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            onSubmitQuery();
            return true;
        });
        mSearchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!Utils.isEmpty(mUserQuery)) {
                mEmptyImageView.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                if (mVoice) mVoiceImageView.setVisibility(hasFocus ? View.GONE : VISIBLE);
            }

            if (hasFocus) addFocus();
            else removeFocus();
        });

        setVisibility(View.GONE);

        mSearchArrow = new SearchArrowDrawable(mContext);
        mBackImageView = findViewById(R.id.imageView_arrow_back);
        mBackImageView.setImageDrawable(mSearchArrow);
        mBackImageView.setOnClickListener(this);

        final CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT);

        final Resources resources = mContext.getResources();
        final int top = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin);
        final int leftRight = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
        final int bottom = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin);

        params.setMargins(leftRight, top, leftRight, bottom);
        mCardView.setLayoutParams(params);

        setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_background));
        setIconColor(ContextCompat.getColor(mContext, R.color.search_icon));

        mVoice = isVoiceAvailable();
        if (mVoice && mSearchEditText != null) mSearchEditText.setPrivateImeOptions("nm");

        mVoiceImageView.setVisibility(isVoiceAvailable() ? View.VISIBLE : View.GONE);
        mVoice = true;
    }

    public void setQuery(final CharSequence query, final boolean submit) {
        setQueryWithoutSubmitting(query);

        if (!Utils.isEmpty(mUserQuery)) {
            mEmptyImageView.setVisibility(View.GONE);
            if (mVoice) mVoiceImageView.setVisibility(View.VISIBLE);
        }

        if (submit && !Utils.isEmpty(query)) onSubmitQuery();
    }

    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setBackgroundColor(@ColorInt final int color) {
        mCardView.setCardBackgroundColor(color);
    }

    @Override
    public void setElevation(final float elevation) {
        mCardView.setMaxCardElevation(elevation);
        mCardView.setCardElevation(elevation);
        invalidate();
    }

    public void open(final boolean animate, final MenuItem menuItem) {
        setVisibility(View.VISIBLE);

        if (animate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (menuItem != null) getMenuItemPosition(menuItem.getItemId());
                mCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        SearchAnimator.revealOpen(mCardView, mMenuItemCx, mAnimationDuration, mContext, mSearchEditText,
                                false, mOnOpenCloseListener);
                    }
                });
            } else SearchAnimator.fadeOpen(mCardView, mSearchEditText,
                    mOnOpenCloseListener);
        } else {
            mCardView.setVisibility(View.VISIBLE);
            if (mOnOpenCloseListener != null) mOnOpenCloseListener.onOpen();
            mSearchEditText.requestFocus();
        }
    }

    public void close(final boolean animate) {
        if (animate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                SearchAnimator.revealClose(mCardView, mMenuItemCx, mAnimationDuration, mContext,
                        mSearchEditText, false, this, mOnOpenCloseListener);
            else SearchAnimator.fadeClose(mCardView, mSearchEditText,
                    this, mOnOpenCloseListener);
        } else {
            mSearchEditText.clearFocus();
            mCardView.setVisibility(View.GONE);
            setVisibility(View.GONE);
            if (mOnOpenCloseListener != null) mOnOpenCloseListener.onClose();
        }
    }

    private void addFocus() {
        mIsSearchOpen = true;
        setArrow();
        SearchAnimator.fadeIn(mShadowView);
        showSuggestions();
        showKeyboard();
    }

    private void removeFocus() {
        mIsSearchOpen = false;
        setHamburger();
        SearchAnimator.fadeOut(mShadowView);
        hideSuggestions();
        hideKeyboard();
    }

    public void showSuggestions() {
        if (mAdapter == null) return;
        if (mAdapter.getItemCount() > 0) mDividerView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        SearchAnimator.fadeIn(mRecyclerView);
    }

    private void hideSuggestions() {
        if (mAdapter == null) return;
        mDividerView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        SearchAnimator.fadeOut(mRecyclerView);
    }

    public boolean isSearchOpen() {
        return mIsSearchOpen; // getVisibility();
    }

    private void showKeyboard() {
        if (isInEditMode()) return;
        if (imm == null) imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.showSoftInput(mSearchEditText, 0);
        imm.showSoftInput(this, 0);
    }

    private void hideKeyboard() {
        if (isInEditMode()) return;
        if (imm == null) imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    public boolean isShowingProgress() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    /* Simply do not use it. */
    @Deprecated
    public void setNavigationIconArrowHamburger() {
        mSearchArrow = new SearchArrowDrawable(mContext);
        mBackImageView.setImageDrawable(mSearchArrow);
    }

    private void setQueryWithoutSubmitting(CharSequence query) {
        mSearchEditText.setText(query);
        if (query != null) {
            mSearchEditText.setSelection(mSearchEditText.length());
            mUserQuery = query;
        } else if (mSearchEditText.getText() != null)
            mSearchEditText.getText().clear();
    }

    private void setArrow() {
        if (mSearchArrow == null) return;
        mSearchArrow.setVerticalMirror(false);
        mSearchArrow.animate(SearchArrowDrawable.STATE_ARROW);
        mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
    }

    private void setHamburger() {
        if (mSearchArrow == null) return;
        mSearchArrow.setVerticalMirror(true);
        mSearchArrow.animate(SearchArrowDrawable.STATE_HAMBURGER);
        mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    }

    private void getMenuItemPosition(final int menuItemId) {
        if (mMenuItemView != null) mMenuItemCx = getCenterX(mMenuItemView);
        ViewParent viewParent = getParent();
        while (viewParent instanceof View) {
            final View parent = (View) viewParent;
            final View view = parent.findViewById(menuItemId);
            if (view != null) {
                mMenuItemView = view;
                mMenuItemCx = getCenterX(mMenuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private void onVoiceClicked() {
        if (mContext instanceof Activity) {
            try {
                final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

                ((Activity) mContext).startActivityForResult(intent, SPEECH_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, "No app or service found to perform voice search.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) return true;
        final PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    private void onSubmitQuery() {
        final CharSequence query = mSearchEditText.getText();
        if (query == null || TextUtils.getTrimmedLength(query) <= 0) return;
        if (mOnQueryChangeListener == null || !mOnQueryChangeListener.onQueryTextSubmit(query.toString()))
            mSearchEditText.setText(query);
    }

    private void onSearchTextChanged(final CharSequence newText) {
        mUserQuery = mSearchEditText.getText();

        if (mAdapter instanceof Filterable) ((Filterable) mAdapter).getFilter().filter(mUserQuery);

        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText))
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        mOldQueryText = newText.toString();

        boolean isTextEmpty = Utils.isEmpty(newText);
        mEmptyImageView.setVisibility(isTextEmpty ? View.GONE : View.VISIBLE);
        if (mVoice) mVoiceImageView.setVisibility(isTextEmpty ? View.VISIBLE : View.GONE);
    }

    private int getCenterX(@NonNull final View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    @Override
    public void onClick(final View v) {
        if (v == mVoiceImageView) onVoiceClicked();
        else if (v == mShadowView) close(true);
        else if (v == mBackImageView) {
            if (mSearchArrow != null && mIsSearchArrowHamburgerState == SearchArrowDrawable.STATE_ARROW)
                close(true);
        } else if (v == mEmptyImageView && mSearchEditText.length() > 0) {
            Editable text = mSearchEditText.getText();
            if (text != null) text.clear();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.query = mUserQuery != null ? mUserQuery.toString() : null;
        ss.isSearchOpen = mIsSearchOpen;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        if (ss.isSearchOpen) {
            open(true, null);
            setQueryWithoutSubmitting(ss.query);
            mSearchEditText.requestFocus();
        }

        super.onRestoreInstanceState(ss.getSuperState());
        requestLayout();
    }

    public void setOnQueryTextListener(final OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnOpenCloseListener(final OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(final String query);
        void onQueryTextChange(final String newText);
    }

    public interface OnOpenCloseListener {
        void onClose();
        void onOpen();
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
        private String query;
        private boolean isSearchOpen;

        SavedState(final Parcelable superState) {
            super(superState);
        }

        SavedState(final Parcel source) {
            super(source);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
        }
    }
}