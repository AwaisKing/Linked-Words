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

import awais.backworddictionary.adapters.DictionaryWordsAdapter;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.databinding.DictionaryViewBinding;
import awais.backworddictionary.helpers.AppHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.SmoothScroller;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;

public final class DictionaryFragment extends Fragment implements FragmentCallback, SwipeRefreshLayout.OnRefreshListener,
                                                                          View.OnClickListener, TextWatcher {
    private static final RecyclerView.OnScrollListener VIEWPAGER_SCROLL_HACK = new RecyclerView.OnScrollListener() {
        private boolean isVertical = false, isHorizontal = false;

        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                isHorizontal = layoutManager.canScrollHorizontally();
                isVertical = layoutManager.canScrollVertically();
            }
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            if (isHorizontal || isVertical) {
                recyclerView.requestDisallowInterceptTouchEvent(true);
                ViewParent parent = recyclerView.getParent();
                while (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                    parent = parent.getParent();
                }
            }
        }
    };
    private final ArrayList<WordItem> wordList = new ArrayList<>(0);
    private final SmoothScroller smoothScroller = new SmoothScroller();
    private final boolean[] filterCheck = {true, true, true};

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

        // check for tts before initializing it
        if (Main.tts == null) {
            try {
                ActivityCompat.startActivityForResult(this.activity, new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA),
                                                      Main.TTS_DATA_CHECK_CODE, null);
            } catch (final Throwable e) {
                // tts check activity not found
                Toast.makeText(this.activity, R.string.tts_act_not_found, Toast.LENGTH_SHORT).show();
            }
            // Main.tts = new TextToSpeech(this.activity.getApplicationContext(), Main::onTTSInit);
        }
    }

    @Override
    public void onDestroy() {
        try { Main.tts.stop(); } catch (Exception ignored) {}
        try { Main.tts.shutdown(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        if (dictionaryBinding == null)
            dictionaryBinding = DictionaryViewBinding.inflate(inflater, container, false);
        return dictionaryBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View magicRootView, @Nullable final Bundle savedInstanceState) {
        final Activity act = getActivity();
        activity = act == null ? (Activity) getContext() : act;
        final boolean actNotNull = activity != null;

        dictionaryBinding.rvItems.addOnScrollListener(VIEWPAGER_SCROLL_HACK);

        if (resources == null) resources = actNotNull ? activity.getResources() : getResources();
        final Resources.Theme theme = actNotNull ? activity.getTheme() : null;
        cardBackColor = ResourcesCompat.getColor(resources, R.color.cards_back_color, theme);

        wordList.clear();
        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);

        dictionaryBinding.swipeRefreshLayout.setEnabled(false);
        dictionaryBinding.swipeRefreshLayout.setColorSchemeResources(R.color.progress1, R.color.progress2,
                                                                     R.color.progress3, R.color.progress4);
        dictionaryBinding.swipeRefreshLayout.setOnRefreshListener(this);

        startOffset = dictionaryBinding.swipeRefreshLayout.getProgressViewStartOffset();
        expandedEndOffset = (int) (getExpandedOffset() * 1.2f);
        endOffset = (int) (expandedEndOffset - expandedEndOffset * 0.72f);

        if (BuildConfig.DEBUG) {
            // todo add animator in next release build
            // recyclerView.setItemAnimator(new MagicAnimator());
            dictionaryBinding.rvItems.setItemAnimator(null);
        }
        dictionaryBinding.rvItems.setHasFixedSize(true);
        dictionaryBinding.rvItems.setAdapter(wordsAdapter);

        topPadding = dictionaryBinding.rvItems.getPaddingTop();
        topMargin = Math.round(resources.getDimension(R.dimen.filter_top_margin));

        dictionaryBinding.filterSearchButton.setTag("filter");
        dictionaryBinding.filterSearchEditor.setOnFocusChangeListener((view, b) -> toggleKeyboard(b));
        dictionaryBinding.filterSearchEditor.addTextChangedListener(this);

        dictionaryBinding.filterBackButton.setOnClickListener(this);
        dictionaryBinding.filterSearchEditor.setOnClickListener(this);
        dictionaryBinding.filterSearchButton.setOnClickListener(this);
    }

    @Override
    public void onRefresh() {
        dictionaryBinding.swipeRefreshLayout.setRefreshing(true);

        if (activity instanceof Main) {
            final Main mainActivity = (Main) activity;
            if (mainActivity.mainBinding != null && mainActivity.mainBinding.searchView.isSearchOpen())
                mainActivity.mainBinding.searchView.close(true);

            final String title = (String) activity.getTitle();
            if (!title.equals(resources.getString(R.string.app_name))) mainActivity.onSearch(title);
            else dictionaryBinding.swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onClick(final View view) {
        if (view == dictionaryBinding.filterBackButton)
            hideFilter();
        else if (view == dictionaryBinding.filterSearchEditor)
            toggleKeyboard(true);
        else if (view == dictionaryBinding.filterSearchButton) {
            final Object tag = dictionaryBinding.filterSearchButton.getTag();

            if (!Utils.isEmpty((CharSequence) tag) && tag.equals("filter")) {
                filterCheck[0] = SettingsHelper.isFilterWords();
                filterCheck[1] = SettingsHelper.isFilterDefinition();
                filterCheck[2] = SettingsHelper.isFilterContains();

                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.select_filters)
                        .setMultiChoiceItems(new String[]{getString(R.string.words), getString(R.string.defs), getString(R.string.contains)}, filterCheck, (dialogInterface, i, checked) -> {
                            filterCheck[i] = checked;
                            if (i == 0) SettingsHelper.setFilter("filterWord", checked);
                            else if (i == 1) SettingsHelper.setFilter("filterDefinition", checked);
                            else if (i == 2) SettingsHelper.setFilter("filterContain", checked);
                        })
                        .setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                            if (wordList.size() > 2)
                                wordsAdapter.getFilter().filter(dictionaryBinding.filterSearchEditor.getText());
                            dialogInterface.dismiss();
                        }).show();
            } else {
                dictionaryBinding.filterSearchEditor.setText("");
                dictionaryBinding.filterSearchButton.setTag("filter");
            }
        }
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (wordList.size() > 2)
            wordsAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(@NonNull final Editable editable) {
        if (editable.length() > 0) {
            dictionaryBinding.filterSearchButton.setImageResource(R.drawable.ic_clear);
            dictionaryBinding.filterSearchButton.setTag("clear");
        } else {
            dictionaryBinding.filterSearchButton.setImageResource(R.drawable.ic_settings);
            dictionaryBinding.filterSearchButton.setTag("filter");
        }
    }

    void startWords(final String method, final String word) {
        if (dictionaryBinding != null)
            dictionaryBinding.filterSearchEditor.setText("");
        if (!Utils.isEmpty(word))
            new WordsAsync(this, word, method, activity).execute();
    }

    @Override
    public void wordStarted() {
        if (dictionaryBinding != null) {
            dictionaryBinding.swipeRefreshLayout.post(() -> {
                dictionaryBinding.swipeRefreshLayout.setEnabled(true);
                dictionaryBinding.swipeRefreshLayout.setRefreshing(true);
            });
        }
    }

    @Override
    public void done(final ArrayList<WordItem> items, final String word) {
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
        boolean returnVal = false;

        if (dictionaryBinding != null) {
            final boolean isRefrehing = dictionaryBinding.swipeRefreshLayout.isRefreshing();
            final boolean isMethod0 = method == FilterMethod.RECYCLER_PADDING;
            final boolean isMethod2 = method == FilterMethod.RECYCLER_NO_PADDING;

            if (showFilter) {
                if (isMethod0 || isMethod2) {
                    dictionaryBinding.filterView.setVisibility(View.VISIBLE);
                    if (isMethod0)
                        dictionaryBinding.filterSearchEditor.requestFocus();
                }

                if (isMethod0) dictionaryBinding.rvItems.setPadding(0, topMargin, 0, dictionaryBinding.rvItems.getPaddingBottom());

                final LinearLayoutManager manager = (LinearLayoutManager) dictionaryBinding.rvItems.getLayoutManager();
                final boolean managerNotNull = manager != null;

                final boolean canScroll = wordsAdapter.getItemCount() > 5 || managerNotNull && manager.getItemCount() > 5;
                if (canScroll && managerNotNull && manager.findFirstVisibleItemPosition() <= 3)
                    dictionaryBinding.rvItems.smoothScrollToPosition(0);

                returnVal = isMethod2 || isMethod0;
            } else {
                if (isMethod0 || isMethod2) {
                    returnVal = true;
                    dictionaryBinding.filterView.setVisibility(View.GONE);
                }

                dictionaryBinding.rvItems.setPadding(0, topPadding, 0, dictionaryBinding.rvItems.getPaddingBottom());
            }

            if (!returnVal) returnVal = method != FilterMethod.DO_NOTHING;

            if (method != FilterMethod.DO_NOTHING) {
                dictionaryBinding.swipeRefreshLayout.setProgressViewOffset(false, startOffset,
                                                                           showFilter ? expandedEndOffset : endOffset);
                if (isRefrehing) dictionaryBinding.swipeRefreshLayout.setRefreshing(true);
            }
        }

        return returnVal;
    }

    boolean hideFilter() {
        return showFilter(false, FilterMethod.RECYCLER_PADDING);
    }

    boolean isFilterOpen() {
        return dictionaryBinding != null && dictionaryBinding.filterView.getVisibility() == View.VISIBLE;
    }

    private void toggleKeyboard(final boolean show) {
        final InputMethodManager imm = AppHelper.getInstance(getContext()).getInputMethodManager();
        if (show) imm.showSoftInput(dictionaryBinding.filterSearchEditor, 1);
        else imm.hideSoftInputFromWindow(dictionaryBinding.filterSearchEditor.getWindowToken(), 1);
    }

    void scrollRecyclerView(final boolean directionUp) {
        if (dictionaryBinding != null) {
            final int itemCount = wordsAdapter.getItemCount();
            final RecyclerView.LayoutManager layoutManager;
            if (itemCount > 0 && (layoutManager = dictionaryBinding.rvItems.getLayoutManager()) != null) {
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
        }
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

    void closeExpanded() {
        wordsAdapter.refreshShowDialogEnabled();

        for (final WordItemViewHolder holder : wordsAdapter.holdersHashSet)
            holder.cardView.setCardBackgroundColor(cardBackColor);

        for (final WordItem wordItem : wordsAdapter.expandedHashSet)
            wordItem.setExpanded(false);

        wordsAdapter.expandedHashSet.clear();

        wordsAdapter.updateList(wordList);
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

    public enum FilterMethod {
        DO_NOTHING, RECYCLER_PADDING, RECYCLER_NO_PADDING,
    }
}