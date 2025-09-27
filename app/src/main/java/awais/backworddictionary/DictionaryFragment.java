package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import awais.backworddictionary.adapters.DictionaryWordsAdapter;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.databinding.DictionaryViewBinding;
import awais.backworddictionary.executors.WordsAsync;
import awais.backworddictionary.helpers.AppHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.SmoothScroller;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;
import awais.backworddictionary.models.FilterMethod;
import awais.backworddictionary.models.Tab;
import awais.backworddictionary.models.WordItem;

public final class DictionaryFragment extends Fragment implements FragmentCallback, SwipeRefreshLayout.OnRefreshListener,
                                                                          View.OnClickListener, TextWatcher {
    private static final AtomicBoolean ASKING_FOR_PERMISSIONS = new AtomicBoolean(false);
    private static final RecyclerView.OnScrollListener VIEWPAGER_SCROLL_HACK = new RecyclerView.OnScrollListener() {
        private boolean isVertical = false, isHorizontal = false;

        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) return;
            isHorizontal = layoutManager.canScrollHorizontally();
            isVertical = layoutManager.canScrollVertically();
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            if (!isHorizontal && !isVertical) return;
            recyclerView.requestDisallowInterceptTouchEvent(true);
            ViewParent parent = recyclerView.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                parent = parent.getParent();
            }
        }
    };
    private final ArrayList<WordItem> wordList = new ArrayList<>();
    private final SmoothScroller smoothScroller = new SmoothScroller();
    private final boolean[] filterCheck = {true, true, true};

    private Tab tab;
    private int cardBackColor;
    private Activity activity;
    private Resources resources;
    private DictionaryViewBinding dictionaryBinding;

    public String title;
    DictionaryWordsAdapter wordsAdapter;

    private static int startOffset = -120, endOffset, expandedEndOffset, topPadding, topMargin;

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        final Activity activity = getActivity();
        this.activity = activity != null ? activity : (Activity) context;

        /// check for tts before initializing it
        if (TTSHelper.tts == null && !ASKING_FOR_PERMISSIONS.getAndSet(true)) {
            try {
                ActivityCompat.startActivityForResult(this.activity, new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA),
                                                      TTSHelper.TTS_DATA_CHECK_CODE, null);
            } catch (final Throwable e) {
                /// tts check activity not found
                Toast.makeText(this.activity, R.string.tts_act_not_found, Toast.LENGTH_SHORT).show();
            }
            // Main.tts = new TextToSpeech(this.activity.getApplicationContext(), Main::onTTSInit);
        }
    }

    @Override
    public void onDestroy() {
        try {TTSHelper.tts.stop();} catch (final Exception ignored) {}
        try {TTSHelper.tts.shutdown();} catch (final Exception ignored) {}
        super.onDestroy();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        if (dictionaryBinding == null) dictionaryBinding = DictionaryViewBinding.inflate(inflater, container, false);
        return dictionaryBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View magicRootView, @Nullable final Bundle savedInstanceState) {
        final DictionaryViewBinding dictionaryBinding = this.dictionaryBinding;
        final Activity act = getActivity();
        activity = act == null ? (Activity) getContext() : act;
        final boolean actNotNull = activity != null;

        dictionaryBinding.rvItems.addOnScrollListener(VIEWPAGER_SCROLL_HACK);

        if (resources == null) resources = actNotNull ? activity.getResources() : getResources();
        cardBackColor = ResourcesCompat.getColor(resources, R.color.cards_back_color, actNotNull ? activity.getTheme() : null);

        wordList.clear();
        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);

        dictionaryBinding.swipeRefreshLayout.setEnabled(false);
        dictionaryBinding.swipeRefreshLayout.setColorSchemeResources(R.color.progress1, R.color.progress2,
                                                                     R.color.progress3, R.color.progress4);
        dictionaryBinding.swipeRefreshLayout.setOnRefreshListener(this);

        startOffset = dictionaryBinding.swipeRefreshLayout.getProgressViewStartOffset();
        expandedEndOffset = (int) (getExpandedOffset() * 1.2f);
        endOffset = (int) (expandedEndOffset - expandedEndOffset * 0.72f);

        /// no animator, because animators make recyclerview act up sometimes
        dictionaryBinding.rvItems.setItemAnimator(null);
        dictionaryBinding.rvItems.setHasFixedSize(true);
        dictionaryBinding.rvItems.setAdapter(wordsAdapter);

        topPadding = dictionaryBinding.rvItems.getPaddingTop();
        topMargin = Math.round(resources.getDimension(R.dimen.filter_top_margin));

        dictionaryBinding.filterSearchButton.setTag("filter");
        dictionaryBinding.filterSearchEditor.setOnFocusChangeListener((view, b) -> toggleKeyboard(b));
        dictionaryBinding.filterSearchEditor.addTextChangedListener(this);

        dictionaryBinding.filterBackButton.setOnClickListener(this);
        dictionaryBinding.filterSearchButton.setOnClickListener(this);
        dictionaryBinding.filterSearchEditor.setOnClickListener(this);
    }

    @Override
    public void onRefresh() {
        dictionaryBinding.swipeRefreshLayout.setRefreshing(true);

        if (!(activity instanceof final Main mainActivity)) return;

        if (mainActivity.mainBinding != null && mainActivity.mainBinding.searchView.isSearchOpen())
            mainActivity.mainBinding.searchView.close(true);

        final String title = (String) activity.getTitle();
        if (!title.equals(resources.getString(R.string.app_name))) mainActivity.onSearch(title);
        else dictionaryBinding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(final View view) {
        final DictionaryViewBinding dictionaryBinding = this.dictionaryBinding;

        if (view == dictionaryBinding.filterBackButton) hideFilter();
        else if (view == dictionaryBinding.filterSearchEditor) toggleKeyboard(true);
        else if (view == dictionaryBinding.filterSearchButton) {
            final Object tag = dictionaryBinding.filterSearchButton.getTag();

            if (Utils.isEmpty((CharSequence) tag) || !tag.equals("filter")) {
                dictionaryBinding.filterSearchEditor.setText("");
                dictionaryBinding.filterSearchButton.setTag("filter");
            } else {
                final SettingsHelper settingsHelper = SettingsHelper.getInstance(activity);

                filterCheck[0] = settingsHelper.isFilterWords();
                filterCheck[1] = settingsHelper.isFilterDefinition();
                filterCheck[2] = settingsHelper.isFilterContains();

                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.select_filters)
                        .setMultiChoiceItems(new String[]{getString(R.string.words), getString(R.string.defs), getString(R.string.contains)}, filterCheck, (dialogInterface, i, checked) -> {
                            filterCheck[i] = checked;
                            if (i == 0) settingsHelper.setFilter("filterWord", checked);
                            else if (i == 1) settingsHelper.setFilter("filterDefinition", checked);
                            else if (i == 2) settingsHelper.setFilter("filterContain", checked);
                        }).setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                            if (wordList.size() > 2)
                                wordsAdapter.getFilter().filter(dictionaryBinding.filterSearchEditor.getText());
                            dialogInterface.dismiss();
                        }).show();
            }
        }
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (wordList.size() > 2) wordsAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(@NonNull final Editable editable) {
        final ImageView filterSearchButton = dictionaryBinding.filterSearchButton;
        if (editable.length() > 0) {
            filterSearchButton.setImageResource(R.drawable.ic_clear);
            filterSearchButton.setTag("clear");
        } else {
            filterSearchButton.setImageResource(R.drawable.ic_settings);
            filterSearchButton.setTag("filter");
        }
    }

    public Tab getTab() {
        return tab;
    }

    public DictionaryFragment setTab(final Tab tab) {
        this.tab = tab;
        return this;
    }

    void startWords(final int method, final String word) {
        if (dictionaryBinding != null) dictionaryBinding.filterSearchEditor.setText("");
        if (!Utils.isEmpty(word)) new WordsAsync(this, word, method, activity).execute();
    }

    @Override
    public void wordStarted() {
        if (dictionaryBinding == null) return;
        final SwipeRefreshLayout swipeRefreshLayout = dictionaryBinding.swipeRefreshLayout;
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
        });
    }

    @Override
    public void done(final ArrayList<WordItem> items, final String word) {
        final DictionaryViewBinding dictionaryBinding = this.dictionaryBinding;

        wordList.clear();
        if (items != null) wordList.addAll(items);

        dictionaryBinding.swipeRefreshLayout.post(() -> dictionaryBinding.swipeRefreshLayout.setRefreshing(false));

        wordsAdapter.updateList(wordList);
        dictionaryBinding.rvItems.setAdapter(wordsAdapter);

        title = word;
        activity.setTitle(title);
    }

    /**
     * DO NOT CHANGE ANYTHING HERE, I STILL DON'T HAVE
     * ANY IDEA WHAT IS METHOD ABOUT, PLEASE FORGIVE ME.
     * I ASK FOR FORGIVENESS GOD. I SWEAR.
     * 14th March 2020, still a little clue, trying to make it work with enums
     *
     * @param showFilter either to show or hide filter
     * @param method     no idea what this is. i really forgot.
     */
    boolean showFilter(final boolean showFilter, final FilterMethod method) {
        final DictionaryViewBinding dictionaryBinding = this.dictionaryBinding;
        if (dictionaryBinding == null) return false;

        boolean returnVal = false;

        final RecyclerView rvItems = dictionaryBinding.rvItems;
        final FrameLayout filterView = dictionaryBinding.filterView;
        final SwipeRefreshLayout swipeRefreshLayout = dictionaryBinding.swipeRefreshLayout;

        final boolean isRefrehing = swipeRefreshLayout.isRefreshing();
        final boolean isMethodAny = method != FilterMethod.DO_NOTHING;
        final boolean isMethodPadding = method == FilterMethod.RECYCLER_PADDING;
        final boolean isMethodNoPadding = method == FilterMethod.RECYCLER_NO_PADDING;

        if (showFilter) {
            if (isMethodPadding || isMethodNoPadding) {
                filterView.setVisibility(View.VISIBLE);
                if (isMethodPadding) dictionaryBinding.filterSearchEditor.requestFocus();
            }

            if (isMethodPadding) rvItems.setPadding(0, topMargin, 0, rvItems.getPaddingBottom());

            final LinearLayoutManager manager = (LinearLayoutManager) rvItems.getLayoutManager();
            final boolean managerNotNull = manager != null;

            final boolean canScroll = wordsAdapter.getItemCount() > 5 || managerNotNull && manager.getItemCount() > 5;
            if (canScroll && managerNotNull && manager.findFirstVisibleItemPosition() <= 3)
                rvItems.smoothScrollToPosition(0);

            returnVal = isMethodNoPadding || isMethodPadding;
        } else {
            if (isMethodPadding || isMethodNoPadding) {
                returnVal = true;
                filterView.setVisibility(View.GONE);
            }

            rvItems.setPadding(0, topPadding, 0, rvItems.getPaddingBottom());
        }

        if (!returnVal) returnVal = isMethodAny;

        if (isMethodAny) {
            swipeRefreshLayout.setProgressViewOffset(false, startOffset, showFilter ? expandedEndOffset : endOffset);
            if (isRefrehing) swipeRefreshLayout.setRefreshing(true);
        }

        return returnVal;
    }

    boolean hideFilter() {
        return showFilter(false, FilterMethod.RECYCLER_PADDING);
    }

    boolean isFilterOpen() {
        return dictionaryBinding != null && dictionaryBinding.filterView.getVisibility() == View.VISIBLE;
    }

    void scrollRecyclerView(final boolean directionUp) {
        if (dictionaryBinding == null) return;

        final int itemCount = wordsAdapter.getItemCount();
        final RecyclerView.LayoutManager layoutManager = itemCount > 0 ? dictionaryBinding.rvItems.getLayoutManager() : null;
        if (layoutManager == null) return;

        final LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
        final int firstCompletelyVisible = manager.findFirstCompletelyVisibleItemPosition();

        final float scrollPerInch; // larger value = slower scroll
        if (itemCount <= 20) scrollPerInch = 50f;
        else if (itemCount <= 80) scrollPerInch = 27f;
        else if (firstCompletelyVisible <= itemCount / 15) scrollPerInch = 20f;
        else if (firstCompletelyVisible <= itemCount / 11) scrollPerInch = 26f;
        else if (firstCompletelyVisible <= itemCount / 9) scrollPerInch = 38f;
        else if (firstCompletelyVisible <= itemCount / 5) scrollPerInch = 46f;
        else if (firstCompletelyVisible <= itemCount / 3) scrollPerInch = 52f;
        else if (firstCompletelyVisible <= itemCount / 2) scrollPerInch = 67f;
        else if (firstCompletelyVisible >= itemCount / 2) scrollPerInch = 20f;
        else if (firstCompletelyVisible >= itemCount / 3) scrollPerInch = 26f;
        else if (firstCompletelyVisible >= itemCount / 5) scrollPerInch = 38f;
        else if (firstCompletelyVisible >= itemCount / 9) scrollPerInch = 46f;
        else if (firstCompletelyVisible >= itemCount / 11) scrollPerInch = 52f;
        else if (firstCompletelyVisible >= itemCount / 15) scrollPerInch = 67f;
        else scrollPerInch = 10f;

        smoothScroller.setScrollPerInch(resources.getDisplayMetrics(), scrollPerInch);
        smoothScroller.setTargetPosition(directionUp ? 0 : itemCount - 1);
        manager.startSmoothScroll(smoothScroller);
    }

    void closeExpanded() {
        final DictionaryWordsAdapter wordsAdapter = this.wordsAdapter;

        wordsAdapter.refreshShowDialogEnabled(SettingsHelper.getInstance(activity));

        for (final WordItemViewHolder holder : wordsAdapter.holdersHashSet) holder.setCardViewBackground(cardBackColor);
        for (final WordItem wordItem : wordsAdapter.expandedHashSet) wordItem.setExpanded(false);

        wordsAdapter.expandedHashSet.clear();
        wordsAdapter.updateList(wordList);
    }

    private void toggleKeyboard(final boolean show) {
        final EditText filterSearchEditor = dictionaryBinding.filterSearchEditor;
        final InputMethodManager imm = AppHelper.getInstance(getContext()).getInputMethodManager();
        if (show) imm.showSoftInput(filterSearchEditor, InputMethodManager.SHOW_IMPLICIT);
        else imm.hideSoftInputFromWindow(filterSearchEditor.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private float getExpandedOffset() {
        final TypedValue tv = new TypedValue();
        if (activity == null) activity = getActivity();
        if (activity != null && activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            if (resources == null) resources = activity.getResources();
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }
        return dictionaryBinding == null ? 250f : dictionaryBinding.swipeRefreshLayout.getProgressViewEndOffset() * 2f;
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}
}