package awais.lapism;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;

@SuppressWarnings( "unused" )
public class MaterialSearchView extends FrameLayout implements View.OnClickListener {
    public static final int VERSION_TOOLBAR = 1000;
    public static final int VERSION_MENU_ITEM = 1001;
    public static final int VERSION_MARGINS_TOOLBAR_SMALL = 2000;
    public static final int VERSION_MARGINS_TOOLBAR_BIG = 2001;
    public static final int VERSION_MARGINS_MENU_ITEM = 2002;
    public static final int THEME_LIGHT = 3000;
    public static final int THEME_DARK = 3001;
    public static final int SPEECH_REQUEST_CODE = 4000;
    public static final int LAYOUT_TRANSITION_DURATION = 200;
    public static final int ANIMATION_DURATION = 300;
    private static int mIconColor = Color.BLACK;
    private static int mTextColor = Color.BLACK;
    private static int mTextHighlightColor = Color.BLACK;
    private static int mTextStyle = Typeface.NORMAL;
    private static Typeface mTextFont = Typeface.DEFAULT;
    private final Context mContext;
    protected View mMenuItemView = null;
    protected Activity mActivity = null;
    protected Fragment mFragment = null;
    protected android.support.v4.app.Fragment mSupportFragment = null;
    protected SearchArrowDrawable mSearchArrow = null;
    protected RecyclerView.Adapter mAdapter = null;
    protected List<Boolean> mSearchFiltersStates = null;
    protected OnQueryTextListener mOnQueryChangeListener = null;
    protected OnOpenCloseListener mOnOpenCloseListener = null;
    protected OnMenuClickListener mOnMenuClickListener = null;
    protected OnVoiceClickListener mOnVoiceClickListener = null;
    protected RecyclerView mRecyclerView;
    protected View mShadowView;
    protected View mDividerView;
    protected CardView mCardView;
    protected SearchEditText mSearchEditText;
    protected ProgressBar mProgressBar;
    protected ImageView mBackImageView;
    protected ImageView mVoiceImageView;
    protected ImageView mEmptyImageView;
    protected LinearLayoutCompat mFiltersContainer;
    protected LinearLayoutCompat mLinearLayout;
    protected CharSequence mOldQueryText;
    protected CharSequence mUserQuery = "";
    protected String mVoiceText = "Speak now";
    protected int mVersion = VERSION_TOOLBAR;
    protected int mAnimationDuration = ANIMATION_DURATION;
    protected int mMenuItemCx = -1;
    protected float mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    protected boolean mShadow = true;
    protected boolean mArrow = false;
    protected boolean mVoice = false;
    protected boolean mIsSearchOpen = false;
    protected boolean mShouldClearOnOpen = false;
    protected boolean mShouldClearOnClose = false;
    protected boolean mShouldHideOnKeyboardClose = true;
    private InputMethodManager imm;

    // ---------------------------------------------------------------------------------------------
    public MaterialSearchView(Context context) {
        this(context, null);
    }

    public MaterialSearchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
        initStyle(attrs, defStyleAttr);
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public MaterialSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
        initStyle(attrs, defStyleAttr);
    }

    // ---------------------------------------------------------------------------------------------
    public static int getIconColor() {
        return mIconColor;
    }

    public void setIconColor(@ColorInt int color) {
        mIconColor = color;
        ColorFilter colorFilter = new PorterDuffColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);

        mBackImageView.setColorFilter(colorFilter);
        mVoiceImageView.setColorFilter(colorFilter);
        mEmptyImageView.setColorFilter(colorFilter);
    }

    public static int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        mSearchEditText.setTextColor(mTextColor);
        for (int i = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
            View child = mFiltersContainer.getChildAt(i);
            if (child instanceof AppCompatCheckBox)
                ((AppCompatCheckBox) child).setTextColor(mTextColor);
        }
    }

    public static int getTextHighlightColor() {
        return mTextHighlightColor;
    }

    public void setTextHighlightColor(@ColorInt int color) {
        mTextHighlightColor = color;
    }

    public static Typeface getTextFont() {
        return mTextFont;
    }

    public void setTextFont(Typeface font) {
        mTextFont = font;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    public static int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(int style) {
        mTextStyle = style;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    // ---------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater.from(mContext).inflate((R.layout.search_view), this, true);

        mLinearLayout = findViewById(R.id.linearLayout);
        mCardView = findViewById(R.id.cardView);

        mRecyclerView = findViewById(R.id.recyclerView_result);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mRecyclerView.setLayoutTransition(null);
                    hideKeyboard();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
            }
        });

        mDividerView = findViewById(R.id.view_divider);
        mDividerView.setVisibility(View.GONE);

        mShadowView = findViewById(R.id.view_shadow);
        mShadowView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_shadow_layout));
        mShadowView.setOnClickListener(this);
        mShadowView.setVisibility(View.GONE);

        mFiltersContainer = findViewById(R.id.filters_container);
        mFiltersContainer.setVisibility(View.GONE);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        mVoiceImageView = findViewById(R.id.imageView_mic);
        mVoiceImageView.setImageResource(R.drawable.ic_mic_black);
        mVoiceImageView.setOnClickListener(this);
        mVoiceImageView.setVisibility(View.GONE);

        mEmptyImageView = findViewById(R.id.imageView_clear);
        mEmptyImageView.setImageResource(R.drawable.ic_clear_black);
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
            if (!TextUtils.isEmpty(mUserQuery)) {
                mEmptyImageView.setVisibility(hasFocus ? View.VISIBLE  : View.GONE);
                if (mVoice) mVoiceImageView.setVisibility(hasFocus ? View.GONE : VISIBLE);
            }

            if (hasFocus) addFocus();
            else removeFocus();
        });

        setVersion(VERSION_MENU_ITEM);

        mSearchArrow = new SearchArrowDrawable(mContext);
        mBackImageView = findViewById(R.id.imageView_arrow_back);
        mBackImageView.setImageDrawable(mSearchArrow);
        mBackImageView.setOnClickListener(this);

        setVersionMargins(VERSION_MARGINS_MENU_ITEM);
        setTheme(THEME_LIGHT);
        setInfo();
        setVoice(true);
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr) {
        final TypedArray attr = mContext.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, defStyleAttr, 0);
        if (attr == null) return;
        if (attr.hasValue(R.styleable.MaterialSearchView_search_height))
            setHeight(attr.getDimension(R.styleable.MaterialSearchView_search_height, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_version))
            setVersion(attr.getInt(R.styleable.MaterialSearchView_search_version, VERSION_MENU_ITEM));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_version_margins))
            setVersionMargins(attr.getInt(R.styleable.MaterialSearchView_search_version_margins, VERSION_MARGINS_MENU_ITEM));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_theme))
            setTheme(attr.getInt(R.styleable.MaterialSearchView_search_theme, THEME_LIGHT));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_navigation_icon))
            setNavigationIcon(attr.getResourceId(R.styleable.MaterialSearchView_search_navigation_icon, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_icon_color))
            setIconColor(attr.getColor(R.styleable.MaterialSearchView_search_icon_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_background_color))
            setBackgroundColor(attr.getColor(R.styleable.MaterialSearchView_search_background_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_text_color))
            setTextColor(attr.getColor(R.styleable.MaterialSearchView_search_text_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_text_highlight_color))
            setTextHighlightColor(attr.getColor(R.styleable.MaterialSearchView_search_text_highlight_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_text_size))
            setTextSize(attr.getDimension(R.styleable.MaterialSearchView_search_text_size, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_text_style))
            setTextStyle(attr.getInt(R.styleable.MaterialSearchView_search_text_style, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_hint))
            setHint(attr.getString(R.styleable.MaterialSearchView_search_hint));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_hint_color))
            setHintColor(attr.getColor(R.styleable.MaterialSearchView_search_hint_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_divider))
            setDivider(attr.getBoolean(R.styleable.MaterialSearchView_search_divider, false));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_voice))
            setVoice(attr.getBoolean(R.styleable.MaterialSearchView_search_voice, false));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_voice_text))
            setVoiceText(attr.getString(R.styleable.MaterialSearchView_search_voice_text));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_animation_duration))
            setAnimationDuration(attr.getInteger(R.styleable.MaterialSearchView_search_animation_duration, mAnimationDuration));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_shadow))
            setShadow(attr.getBoolean(R.styleable.MaterialSearchView_search_shadow, true));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_shadow_color))
            setShadowColor(attr.getColor(R.styleable.MaterialSearchView_search_shadow_color, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_elevation))
            setElevation(attr.getDimensionPixelSize(R.styleable.MaterialSearchView_search_elevation, 0));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_clear_on_open))
            setShouldClearOnOpen(attr.getBoolean(R.styleable.MaterialSearchView_search_clear_on_open, false));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_clear_on_close))
            setShouldClearOnClose(attr.getBoolean(R.styleable.MaterialSearchView_search_clear_on_close, true));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_hide_on_keyboard_close))
            setShouldHideOnKeyboardClose(attr.getBoolean(R.styleable.MaterialSearchView_search_hide_on_keyboard_close, true));
        if (attr.hasValue(R.styleable.MaterialSearchView_search_cursor_drawable))
            setCursorDrawable(attr.getResourceId(R.styleable.MaterialSearchView_search_cursor_drawable, 0));
        attr.recycle();
    }

    public CharSequence getTextOnly() {
        return mSearchEditText.getText();
    }

    public void setQuery(CharSequence query, boolean submit) {
        setQueryWithoutSubmitting(query);

        if (!TextUtils.isEmpty(mUserQuery)) {
            mEmptyImageView.setVisibility(View.GONE);
            if (mVoice) mVoiceImageView.setVisibility(View.VISIBLE);
        }

        if (submit && !TextUtils.isEmpty(query)) onSubmitQuery();
    }

    public CharSequence getQuery() {
        return mSearchEditText.getText();
    }

    @Nullable
    public CharSequence getHint() {
        return mSearchEditText.getHint();
    }

    public void setHint(@Nullable CharSequence hint) {
        mSearchEditText.setHint(hint);
    }

    public int getImeOptions() {
        return mSearchEditText.getImeOptions();
    }

    public void setImeOptions(int imeOptions) {
        mSearchEditText.setImeOptions(imeOptions);
    }

    public int getInputType() {
        return mSearchEditText.getInputType();
    }

    public void setInputType(int inputType) {
        mSearchEditText.setInputType(inputType);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
    }

    public boolean getShouldClearOnClose() {
        return mShouldClearOnClose;
    }

    public void setShouldClearOnClose(boolean shouldClearOnClose) {
        mShouldClearOnClose = shouldClearOnClose;
    }

    public boolean getShouldClearOnOpen() {
        return mShouldClearOnOpen;
    }

    public void setShouldClearOnOpen(boolean shouldClearOnOpen) {
        mShouldClearOnOpen = shouldClearOnOpen;
    }

    public boolean getShouldHideOnKeyboardClose() {
        return mShouldHideOnKeyboardClose;
    }

    public void setShouldHideOnKeyboardClose(boolean shouldHideOnKeyboardClose) {
        mShouldHideOnKeyboardClose = shouldHideOnKeyboardClose;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;

        if (mVersion == VERSION_TOOLBAR) {
            setVisibility(View.VISIBLE);
            mSearchEditText.clearFocus();
        }

        if (mVersion == VERSION_MENU_ITEM) setVisibility(View.GONE);
    }

    public void setFilters(@Nullable List<SearchFilter> filters) {
        mFiltersContainer.removeAllViews();
        LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams) mFiltersContainer.getLayoutParams();
        if (filters == null) {
            mSearchFiltersStates = null;
            params.topMargin = 0;
            params.bottomMargin = 0;
            mFiltersContainer.setLayoutParams(params);
        } else {
            mSearchFiltersStates = new ArrayList<>();
            params.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.filter_margin_top);
            params.bottomMargin = params.topMargin / 2;
            mFiltersContainer.setLayoutParams(params);
            for (SearchFilter filter : filters) {
                AppCompatCheckBox checkBox = new AppCompatCheckBox(mContext);
                checkBox.setText(filter.getTitle());
                checkBox.setTextSize(11);
                checkBox.setTextColor(mTextColor);
                checkBox.setChecked(filter.isChecked());
                mFiltersContainer.addView(checkBox);
                mSearchFiltersStates.add(filter.isChecked());
            }
        }
    }

    public List<Boolean> getFiltersStates() {
        return mSearchFiltersStates;
    }

    // TODO GET
    // ---------------------------------------------------------------------------------------------
    public void setHeight(float dp) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = height;
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLinearLayout.setLayoutParams(params);
    }

    public void setVersionMargins(int version) {
        CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT);

        int top = 0, leftRight = 0, bottom = 0;
        if (version == VERSION_MARGINS_TOOLBAR_SMALL) {
            top = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
            leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_small_left_right);
        } else if (version == VERSION_MARGINS_TOOLBAR_BIG) {
            top = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
            leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_big_left_right);
        } else if (version == VERSION_MARGINS_MENU_ITEM) {
            top = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);
            leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
            bottom = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);
        }

        params.setMargins(leftRight, top, leftRight, bottom);
        mCardView.setLayoutParams(params);
    }

    public void setTheme(int theme) {
        if (theme == THEME_LIGHT) {
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_light_background));
            setIconColor(ContextCompat.getColor(mContext, R.color.search_light_icon));
            setHintColor(ContextCompat.getColor(mContext, R.color.search_light_hint));
            setTextColor(ContextCompat.getColor(mContext, R.color.search_light_text));
            setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_light_text_highlight));
        } else if (theme == THEME_DARK) {
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_dark_background));
            setIconColor(ContextCompat.getColor(mContext, R.color.search_dark_icon));
            setHintColor(ContextCompat.getColor(mContext, R.color.search_dark_hint));
            setTextColor(ContextCompat.getColor(mContext, R.color.search_dark_text));
            setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_dark_text_highlight));
        }
    }

    public void setNavigationIcon(@DrawableRes int resource) {
        mBackImageView.setImageResource(resource);
    }

    public void setNavigationIcon(Drawable drawable) {
        if (drawable == null) mBackImageView.setVisibility(View.GONE);
        else mBackImageView.setImageDrawable(drawable);
    }

    public void setTextSize(float size) {
        mSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public void setDivider(boolean divider) {
        if (divider) mRecyclerView.addItemDecoration(new SearchDivider(mContext));
        else mRecyclerView.removeItemDecoration(new SearchDivider(mContext));
    }

    public void setVoice(boolean voice) {
        if (voice && isVoiceAvailable()) mVoiceImageView.setVisibility(View.VISIBLE);
        else mVoiceImageView.setVisibility(View.GONE);
        mVoice = voice;
    }

    public void setVoice(boolean voice, Activity context) {
        mActivity = context;
        setVoice(voice);
    }

    public void setVoice(boolean voice, Fragment context) {
        mFragment = context;
        setVoice(voice);
    }

    public void setVoice(boolean voice, android.support.v4.app.Fragment context) {
        mSupportFragment = context;
        setVoice(voice);
    }

    public void setVoiceText(String text) {
        mVoiceText = text;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public void setShadow(boolean shadow) {
        if (shadow) mShadowView.setVisibility(View.VISIBLE);
        else mShadowView.setVisibility(View.GONE);
        mShadow = shadow;
    }

    public void setShadowColor(@ColorInt int color) {
        mShadowView.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mCardView.setCardBackgroundColor(color);
    }

    @Override
    public void setElevation(float elevation) {
        mCardView.setMaxCardElevation(elevation);
        mCardView.setCardElevation(elevation);
        invalidate();
    }

    // http://stackoverflow.com/questions/11554078/set-textcursordrawable-programatically
    public void setCursorDrawable(@DrawableRes int drawable) {
        try {
            //noinspection JavaReflectionMemberAccess
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mSearchEditText, drawable);
        } catch (Exception ignored) {}
    }

    // ---------------------------------------------------------------------------------------------
    public void setArrowOnly(boolean animate) {
        if (!animate) mBackImageView.setImageResource(R.drawable.ic_arrow_back_black_24dp);
        else if (mSearchArrow != null) {
            mSearchArrow.setVerticalMirror(false);
            mSearchArrow.animate(SearchArrowDrawable.STATE_ARROW, mAnimationDuration);
        }
        mArrow = true;
    }

    public void open(boolean animate) {
        open(animate, null);
    }

    public void open(boolean animate, MenuItem menuItem) {
        mFiltersContainer.setVisibility(View.VISIBLE);

        if (mVersion == VERSION_MENU_ITEM) {
            setVisibility(View.VISIBLE);

            if (animate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (menuItem != null) getMenuItemPosition(menuItem.getItemId());
                    reveal();
                } else SearchAnimator.fadeOpen(mCardView, mAnimationDuration, mSearchEditText,
                        mShouldClearOnOpen, mOnOpenCloseListener);
            } else {
                mCardView.setVisibility(View.VISIBLE);
                if (mOnOpenCloseListener != null) mOnOpenCloseListener.onOpen();
                if (mShouldClearOnOpen && mSearchEditText.length() > 0) {
                    Editable text = mSearchEditText.getText();
                    if (text != null) text.clear();
                }
                mSearchEditText.requestFocus();
            }
        } else if (mVersion == VERSION_TOOLBAR) {
            if (mShouldClearOnOpen && mSearchEditText.length() > 0) {
                Editable text = mSearchEditText.getText();
                if (text != null) text.clear();
            }
            mSearchEditText.requestFocus();
        }
    }

    public void close(boolean animate) {
        mFiltersContainer.setVisibility(View.GONE);

        if (mVersion == VERSION_MENU_ITEM) {
            if (animate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    SearchAnimator.revealClose(mCardView, mMenuItemCx, mAnimationDuration, mContext,
                            mSearchEditText, mShouldClearOnClose, this, mOnOpenCloseListener);
                else SearchAnimator.fadeClose(mCardView, mAnimationDuration, mSearchEditText, mShouldClearOnClose,
                        this, mOnOpenCloseListener);
            } else {
                if (mShouldClearOnClose && mSearchEditText.length() > 0) {
                    Editable text = mSearchEditText.getText();
                    if (text != null) text.clear();
                }
                mSearchEditText.clearFocus();
                mCardView.setVisibility(View.GONE);
                setVisibility(View.GONE);
                if (mOnOpenCloseListener != null) mOnOpenCloseListener.onClose();
            }
        } else if (mVersion == VERSION_TOOLBAR) {
            if (mShouldClearOnClose && mSearchEditText.length() > 0) {
                Editable text = mSearchEditText.getText();
                if (text != null) text.clear();
            }
            mSearchEditText.clearFocus();
        }
    }

    public void addFocus() {
        mIsSearchOpen = true;

        if (mArrow) mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
        else setArrow();

        if (mShadow) SearchAnimator.fadeIn(mShadowView, mAnimationDuration);

        showSuggestions();
        showKeyboard();

        if (mVersion == VERSION_TOOLBAR && mOnOpenCloseListener != null)
            postDelayed(() -> mOnOpenCloseListener.onOpen(), mAnimationDuration);

        mFiltersContainer.setVisibility(View.VISIBLE);
    }

    public void removeFocus() {
        mIsSearchOpen = false;

        if (mArrow) mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
        else setHamburger();

        if (mShadow) SearchAnimator.fadeOut(mShadowView, mAnimationDuration);

        hideSuggestions();
        hideKeyboard();

        if (mVersion == VERSION_TOOLBAR && mOnOpenCloseListener != null)
            postDelayed(() -> mOnOpenCloseListener.onClose(), mAnimationDuration);
    }

    public void showSuggestions() {
        if (mAdapter == null) return;
        if (mAdapter.getItemCount() > 0) mDividerView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
    }

    public void hideSuggestions() {
        if (mAdapter == null) return;
        mDividerView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
    }

    public boolean isSearchOpen() {
        return mIsSearchOpen; // getVisibility();
    }

    public void showKeyboard() {
        if (isInEditMode()) return;
        if (imm == null) imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.showSoftInput(mSearchEditText, 0);
        imm.showSoftInput(this, 0);
    }

    public void hideKeyboard() {
        if (isInEditMode()) return;
        if (imm == null) imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

    public void setGoogleIcons() {
        mBackImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_logo));
        mVoiceImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_mic));
    }

    /* Simply do not use it. */
    @Deprecated
    public void setNavigationIconArrowHamburger() {
        mSearchArrow = new SearchArrowDrawable(mContext);
        mBackImageView.setImageDrawable(mSearchArrow);
    }

    // ---------------------------------------------------------------------------------------------
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
        mSearchArrow.animate(SearchArrowDrawable.STATE_ARROW, mAnimationDuration);
        mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
    }

    private void setHamburger() {
        if (mSearchArrow == null) return;
        mSearchArrow.setVerticalMirror(true);
        mSearchArrow.animate(SearchArrowDrawable.STATE_HAMBURGER, mAnimationDuration);
        mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    }

    private void getMenuItemPosition(int menuItemId) {
        if (mMenuItemView != null) mMenuItemCx = getCenterX(mMenuItemView);
        ViewParent viewParent = getParent();
        while (viewParent instanceof View) {
            View parent = (View) viewParent;
            View view = parent.findViewById(menuItemId);
            if (view != null) {
                mMenuItemView = view;
                mMenuItemCx = getCenterX(mMenuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private LayoutTransition getRecyclerViewLayoutTransition() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(LAYOUT_TRANSITION_DURATION);
        return layoutTransition;
    }

    private void onVoiceClicked() {
        if (mOnVoiceClickListener != null) mOnVoiceClickListener.onVoiceClick();
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mVoiceText);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            if (mActivity != null)
                mActivity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
            else if (mFragment != null)
                mFragment.startActivityForResult(intent, SPEECH_REQUEST_CODE);
            else if (mSupportFragment != null)
                mSupportFragment.startActivityForResult(intent, SPEECH_REQUEST_CODE);
            else if (mContext instanceof Activity)
                ((Activity) mContext).startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No app or service found to perform voice search.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setInfo() {
        mVoice = isVoiceAvailable();
        if (mVoice && mSearchEditText != null) mSearchEditText.setPrivateImeOptions("nm");
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) return true;
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    private void setImeVisibility(boolean visible) {
        if (visible || imm == null) return;
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void onSubmitQuery() {
        CharSequence query = mSearchEditText.getText();
        if (query == null || TextUtils.getTrimmedLength(query) <= 0) return;
        dispatchFilters();
        if (mOnQueryChangeListener == null || !mOnQueryChangeListener.onQueryTextSubmit(query.toString()))
            mSearchEditText.setText(query);
    }

    private void onSearchTextChanged(CharSequence newText) {
        CharSequence text = mSearchEditText.getText();
        mUserQuery = text;

        if (mAdapter instanceof Filterable) ((Filterable) mAdapter).getFilter().filter(text);

        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            dispatchFilters();
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();

        boolean isTextEmpty = TextUtils.isEmpty(newText);
        mEmptyImageView.setVisibility(isTextEmpty ? View.GONE : View.VISIBLE);
        if (mVoice) mVoiceImageView.setVisibility(isTextEmpty ? View.VISIBLE : View.GONE);
    }

    private int getCenterX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    private void restoreFiltersState(List<Boolean> states) {
        mSearchFiltersStates = states;
        for (int i = 0, j = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
            View view = mFiltersContainer.getChildAt(i);
            if (view instanceof AppCompatCheckBox)
                ((AppCompatCheckBox) view).setChecked(mSearchFiltersStates.get(j++));
        }
    }

    private void dispatchFilters() {
        if (mSearchFiltersStates == null) return;
        for (int i = 0, j = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
            View view = mFiltersContainer.getChildAt(i);
            if (view instanceof AppCompatCheckBox)
                mSearchFiltersStates.set(j++, ((AppCompatCheckBox) view).isChecked());
        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    private void reveal() {
        mCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                SearchAnimator.revealOpen(mCardView, mMenuItemCx, mAnimationDuration, mContext, mSearchEditText,
                        mShouldClearOnOpen, mOnOpenCloseListener);
            }
        });
    }

    /*private int getPreferredWidth() {
        return getContext().getResources().getDimensionPixelSize(android.support.v7.appcompat.R.dimen.abc_search_view_preferred_width);
    }

    private int getPreferredHeight() {
        return getContext().getResources().getDimensionPixelSize(android.support.v7.appcompat.R.dimen.abc_search_view_preferred_height);
    }*/

    // ---------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        if (v == mVoiceImageView) onVoiceClicked();
        else if (v == mShadowView) close(true);
        else if (v == mBackImageView) {
            if (mSearchArrow != null && mIsSearchArrowHamburgerState == SearchArrowDrawable.STATE_ARROW)
                close(true);
            else if (mOnMenuClickListener != null) mOnMenuClickListener.onMenuClick();
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
        dispatchFilters();
        ss.searchFiltersStates = mSearchFiltersStates;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        if (ss.isSearchOpen) {
            open(true);
            setQueryWithoutSubmitting(ss.query);
            mSearchEditText.requestFocus();
        }

        restoreFiltersState(ss.searchFiltersStates);
        super.onRestoreInstanceState(ss.getSuperState());
        requestLayout();
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        mOnMenuClickListener = listener;
    }

    public void setOnVoiceClickListener(OnVoiceClickListener listener) {
        mOnVoiceClickListener = listener;
    }

    @SuppressWarnings( {"SameReturnValue", "UnusedReturnValue"} )
    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(String query);
        boolean onQueryTextChange(String newText);
    }

    @SuppressWarnings( {"SameReturnValue", "UnusedReturnValue"} )
    public interface OnOpenCloseListener {
        boolean onClose();
        boolean onOpen();
    }

    public interface OnMenuClickListener {
        void onMenuClick();
    }

    public interface OnVoiceClickListener {
        void onVoiceClick();
    }

    // ---------------------------------------------------------------------------------------------
    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String query;
        boolean isSearchOpen;
        List<Boolean> searchFiltersStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            source.readList(searchFiltersStates, List.class.getClassLoader());
        }

        @RequiresApi( api = Build.VERSION_CODES.N )
        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            source.readList(searchFiltersStates, List.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
            out.writeList(searchFiltersStates);
        }
    }
}