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
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.databinding.SearchViewBinding;
import awais.backworddictionary.helpers.Utils;

public final class MaterialSearchView extends FrameLayout implements View.OnClickListener {
    public static final int SPEECH_REQUEST_CODE = 4000;
    public static int iconColor = Color.BLACK;
    private final Context context;
    private SearchArrowDrawable searchArrow = null;
    private RecyclerView.Adapter<?> adapter = null;
    private OnQueryTextListener onQueryChangeListener = null;
    private OnOpenCloseListener onOpenCloseListener = null;

    private SearchViewBinding searchViewBinding;

    private View menuItemView = null;
    private CharSequence oldQueryText, userQuery = "";
    private int menuItemCx = -1;
    private boolean isSearchOpen = false, isVoice = false, isHandlingIntentData = false;

    public MaterialSearchView(final Context context) {
        this(context, null);
    }

    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (Utils.inputMethodManager == null)
            Utils.inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSearchView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        if (Utils.inputMethodManager == null)
            Utils.inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true);
        searchViewBinding = SearchViewBinding.bind(this);

        searchViewBinding.rvResults.setNestedScrollingEnabled(false);
        searchViewBinding.rvResults.setLayoutManager(new LinearLayoutManager(context));
        searchViewBinding.rvResults.setItemAnimator(null);
        searchViewBinding.rvResults.setVisibility(View.GONE);

        searchViewBinding.viewDivider.setVisibility(View.GONE);
        searchViewBinding.viewShadow.setVisibility(View.GONE);

        searchViewBinding.viewShadow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.shadow_color, null));
        searchViewBinding.viewShadow.setOnClickListener(this);

        searchViewBinding.progressBar.setVisibility(View.GONE);

        searchViewBinding.ivMic.setOnClickListener(this);
        searchViewBinding.ivMic.setVisibility(View.GONE);

        searchViewBinding.btnCancel.setOnClickListener(this);
        searchViewBinding.btnCancel.setVisibility(View.GONE);

        searchViewBinding.etSearchView.setSearchView(this);
        searchViewBinding.etSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable editable) { }

            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) { }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int start, final int before, final int count) {
                if (!isHandlingIntentData) onSearchTextChanged(charSequence);
            }
        });
        searchViewBinding.etSearchView.setOnEditorActionListener((textView, i, keyEvent) -> {
            onSubmitQuery();
            return true;
        });
        searchViewBinding.etSearchView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!Utils.isEmpty(userQuery)) {
                searchViewBinding.btnCancel.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                if (isVoice) searchViewBinding.ivMic.setVisibility(hasFocus ? View.GONE : VISIBLE);
            }

            if (hasFocus) addFocus();
            else removeFocus();
        });

        setVisibility(View.GONE);

        searchArrow = new SearchArrowDrawable(new ContextThemeWrapper(context, context.getTheme()));
        searchViewBinding.ivBack.setImageDrawable(searchArrow);
        searchViewBinding.ivBack.setOnClickListener(this);

        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        final Resources resources = context.getResources();
        final int top = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin);
        final int leftRight = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
        final int bottom = resources.getDimensionPixelSize(R.dimen.search_menu_item_margin);

        params.setMargins(leftRight, top, leftRight, bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd(leftRight);
            params.setMarginStart(leftRight);
        }

        searchViewBinding.cardView.setLayoutParams(params);

        setBackgroundColor(ContextCompat.getColor(context, R.color.search_background));

        // setIconColor
        iconColor = ContextCompat.getColor(context, R.color.search_icon);
        final ColorFilter colorFilter = new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        searchViewBinding.ivBack.setColorFilter(colorFilter);
        searchViewBinding.ivMic.setColorFilter(colorFilter);
        searchViewBinding.btnCancel.setColorFilter(colorFilter);

        isVoice = isVoiceAvailable();

        if (isVoice) searchViewBinding.etSearchView.setPrivateImeOptions("nm");
        searchViewBinding.ivMic.setVisibility(isVoice ? View.VISIBLE : View.GONE);
    }

    public void setHandlingIntentData(final boolean isHandlingIntentData) {
        this.isHandlingIntentData = isHandlingIntentData;
    }

    public void setQuery(final CharSequence query, final boolean submit) {
        setQueryWithoutSubmitting(query);

        if (!Utils.isEmpty(userQuery)) {
            searchViewBinding.btnCancel.setVisibility(View.GONE);
            if (isVoice) searchViewBinding.ivMic.setVisibility(View.VISIBLE);
        }

        if (submit && !Utils.isEmpty(query)) onSubmitQuery();
    }

    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
        searchViewBinding.rvResults.setAdapter(this.adapter);
    }

    @Override
    public void setBackgroundColor(@ColorInt final int color) {
        searchViewBinding.cardView.setCardBackgroundColor(color);
    }

    @Override
    public void setElevation(final float elevation) {
        searchViewBinding.cardView.setMaxCardElevation(elevation);
        searchViewBinding.cardView.setCardElevation(elevation);
        invalidate();
    }

    public void open(final boolean animate, final MenuItem menuItem) {
        setVisibility(View.VISIBLE);

        if (animate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (menuItem != null) getMenuItemPosition(menuItem.getItemId());
                searchViewBinding.cardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        searchViewBinding.cardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        SearchAnimator.revealOpen(searchViewBinding.cardView, menuItemCx, context,
                                searchViewBinding.etSearchView, onOpenCloseListener);
                    }
                });
            } else
                SearchAnimator.fadeOpen(searchViewBinding.cardView, searchViewBinding.etSearchView, onOpenCloseListener);
        } else {
            searchViewBinding.cardView.setVisibility(View.VISIBLE);
            if (onOpenCloseListener != null) onOpenCloseListener.onOpen();
            searchViewBinding.etSearchView.requestFocus();
        }
    }

    public void close(final boolean animate) {
        if (animate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                SearchAnimator.revealClose(searchViewBinding.cardView, menuItemCx, context,
                        searchViewBinding.etSearchView, this, onOpenCloseListener);
            else
                SearchAnimator.fadeClose(searchViewBinding.cardView, searchViewBinding.etSearchView,
                        this, onOpenCloseListener);
        } else {
            searchViewBinding.etSearchView.clearFocus();
            searchViewBinding.cardView.setVisibility(View.GONE);
            setVisibility(View.GONE);
            if (onOpenCloseListener != null) onOpenCloseListener.onClose();
        }
    }

    private void addFocus() {
        isSearchOpen = true;
        setArrow();
        SearchAnimator.fadeIn(searchViewBinding.viewShadow);
        showSuggestions();
        showKeyboard();
    }

    private void removeFocus() {
        isSearchOpen = false;
        setHamburger();
        SearchAnimator.fadeOut(searchViewBinding.viewShadow);
        hideSuggestions();
        hideKeyboard();
    }

    public void showSuggestions() {
        if (adapter != null && adapter.getItemCount() > 0) searchViewBinding.viewDivider.setVisibility(View.VISIBLE);
        searchViewBinding.rvResults.setVisibility(View.VISIBLE);
        SearchAnimator.fadeIn(searchViewBinding.rvResults);
    }

    private void hideSuggestions() {
        searchViewBinding.viewDivider.setVisibility(View.GONE);
        searchViewBinding.rvResults.setVisibility(View.GONE);
        SearchAnimator.fadeOut(searchViewBinding.rvResults);
    }

    public boolean isSearchOpen() {
        return isSearchOpen; // getVisibility();
    }

    private void showKeyboard() {
        if (!isInEditMode() && Utils.inputMethodManager != null) {
            Utils.inputMethodManager.showSoftInput(searchViewBinding.etSearchView, 0);
            Utils.inputMethodManager.showSoftInput(this, 0);
        }
    }

    private void hideKeyboard() {
        if (!isInEditMode() && Utils.inputMethodManager != null)
            Utils.inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void showProgress() {
        searchViewBinding.progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        searchViewBinding.progressBar.setVisibility(View.GONE);
    }

    public boolean isShowingProgress() {
        return searchViewBinding.progressBar.getVisibility() == View.VISIBLE;
    }

    private void setQueryWithoutSubmitting(final CharSequence query) {
        searchViewBinding.etSearchView.setText(query);
        if (query != null) {
            searchViewBinding.etSearchView.setSelection(searchViewBinding.etSearchView.length());
            userQuery = query;
        } else if (searchViewBinding.etSearchView.getText() != null)
            searchViewBinding.etSearchView.getText().clear();
    }

    private void setArrow() {
        if (searchArrow != null) {
            searchArrow.setVerticalMirror(false);
            searchArrow.animate(SearchArrowDrawable.STATE_ARROW);
        }
    }

    private void setHamburger() {
        if (searchArrow != null) {
            searchArrow.setVerticalMirror(true);
            searchArrow.animate(SearchArrowDrawable.STATE_HAMBURGER);
        }
    }

    private void getMenuItemPosition(final int menuItemId) {
        if (menuItemView != null) menuItemCx = getCenterX(menuItemView);
        ViewParent viewParent = getParent();
        while (viewParent instanceof View) {
            final View parent = (View) viewParent;
            final View view = parent.findViewById(menuItemId);
            if (view != null) {
                menuItemView = view;
                menuItemCx = getCenterX(menuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private void onVoiceClicked() {
        if (context instanceof Activity) {
            try {
                final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

                ((Activity) context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
            } catch (final ActivityNotFoundException e) {
                Toast.makeText(context, "No app or service found to perform voice search.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) return true;
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    private void onSubmitQuery() {
        final CharSequence query = searchViewBinding.etSearchView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0 &&
                (onQueryChangeListener == null || !onQueryChangeListener.onQueryTextSubmit(query.toString())))
            searchViewBinding.etSearchView.setText(query);
    }

    private void onSearchTextChanged(final CharSequence newText) {
        userQuery = searchViewBinding.etSearchView.getText();

        if (adapter instanceof Filterable) ((Filterable) adapter).getFilter().filter(userQuery);

        if (onQueryChangeListener != null && !TextUtils.equals(newText, oldQueryText))
            onQueryChangeListener.onQueryTextChange(newText.toString());
        oldQueryText = newText.toString();

        final boolean isTextEmpty = Utils.isEmpty(newText);
        searchViewBinding.btnCancel.setVisibility(isTextEmpty ? View.GONE : View.VISIBLE);
        if (isVoice) searchViewBinding.ivMic.setVisibility(isTextEmpty ? View.VISIBLE : View.GONE);
    }

    private int getCenterX(@NonNull final View view) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + (view.getWidth() >>> 1);
    }

    @Override
    public void onClick(final View v) {
        if (v == searchViewBinding.ivMic)
            onVoiceClicked();
        else if (v == searchViewBinding.viewShadow || v == searchViewBinding.ivBack)
            close(true);
        else if (v == searchViewBinding.btnCancel && searchViewBinding.etSearchView.length() > 0) {
            final Editable text = searchViewBinding.etSearchView.getText();
            if (text != null) text.clear();
        }
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);

        ss.query = userQuery != null ? userQuery.toString() : null;
        ss.isSearchOpen = isSearchOpen;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof SavedState) {
            final SavedState ss = (SavedState) state;
            if (ss.isSearchOpen) {
                open(true, null);
                setQueryWithoutSubmitting(ss.query);
                searchViewBinding.etSearchView.requestFocus();
            }

            super.onRestoreInstanceState(ss.getSuperState());
            requestLayout();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public void setOnQueryTextListener(final OnQueryTextListener listener) {
        onQueryChangeListener = listener;
    }

    public void setOnOpenCloseListener(final OnOpenCloseListener listener) {
        onOpenCloseListener = listener;
    }

    public interface OnOpenCloseListener {
        void onClose();
        void onOpen();
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @NonNull
            @Override
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            @NonNull
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