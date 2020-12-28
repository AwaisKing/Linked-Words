package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.executor.TaskExecutor;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.SmoothScroller;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;

public final class DictionaryFragment extends Fragment implements FragmentCallback {
    private Activity activity;
    private FrameLayout filterView;
    private List<WordItem> wordList;
    private RecyclerView recyclerView;
    private EditText filterSearchEditor;
    private ImageView filterSearchButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final SmoothScroller smoothScroller = new SmoothScroller();
    private final boolean[] filterCheck = {true, true, true};
    public String title;
    DictionaryWordsAdapter wordsAdapter;

    private static int startOffset = -120, endOffset, expandedEndOffset, topPadding, topMargin;

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        final Activity activity = getActivity();
        this.activity = activity != null ? activity : (Activity) context;

        if (Main.tts == null) Main.tts = new TextToSpeech(this.activity, initStatus -> {
            if (initStatus == TextToSpeech.SUCCESS) {
                if (Main.tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.US);
                else if (Main.tts.isLanguageAvailable(Locale.CANADA) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.CANADA);
                else if (Main.tts.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.UK);
                else if (Main.tts.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.ENGLISH);
            }
        });
    }

    @Override
    public void onDestroy() {
        try { Main.tts.stop(); } catch (Exception ignored) {}
        try { Main.tts.shutdown(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dictionary_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View magicRootView, @Nullable final Bundle savedInstanceState) {
        final Activity act = getActivity();
        activity = act == null ? (Activity) getContext() : act;

        if (Utils.inputMethodManager == null && activity != null)
            Utils.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        wordList = new ArrayList<>(0);
        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);
        wordsAdapter.setHasStableIds(true);

        swipeRefreshLayout = magicRootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.progress1, R.color.progress2,
                R.color.progress3, R.color.progress4);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);

            if (activity instanceof Main) {
                final Main mainActivity = (Main) activity;
                if (mainActivity.searchView != null && mainActivity.searchView.isSearchOpen())
                    mainActivity.searchView.close(true);

                final String title = (String) activity.getTitle();
                if (!title.equals(getResources().getString(R.string.app_name))) mainActivity.onSearch(title);
                else swipeRefreshLayout.setRefreshing(false);
            }
        });

        startOffset = swipeRefreshLayout.getProgressViewStartOffset();
        expandedEndOffset = (int) (getExpandedOffset() * 1.2f);
        endOffset = (int) (expandedEndOffset - expandedEndOffset * 0.72f);

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(wordsAdapter);

        topPadding = recyclerView.getPaddingTop();
        topMargin = Math.round(getResources().getDimension(R.dimen.filter_top_margin));

        filterView = magicRootView.findViewById(R.id.filterView);

        final ImageView filterBackButton = magicRootView.findViewById(R.id.filterBack);
        filterSearchButton = magicRootView.findViewById(R.id.filterSettings);
        filterSearchButton.setTag("filter");

        filterSearchEditor = magicRootView.findViewById(R.id.swipeSearch);
        filterSearchEditor.setOnFocusChangeListener((view, b) -> toggleKeyboard(b));
        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(final CharSequence cs, final int i, final int i1, final int i2) {
                if (wordList.size() > 2)
                    wordsAdapter.getFilter().filter(cs);
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (editable.length() > 0) {
                    filterSearchButton.setImageResource(R.drawable.ic_clear);
                    filterSearchButton.setTag("clear");
                } else {
                    filterSearchButton.setImageResource(R.drawable.ic_settings);
                    filterSearchButton.setTag("filter");
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence cs, final int i, final int i1, final int i2) {}
        });

        final View.OnClickListener onClickListener = view -> {
            if (view == filterBackButton)
                hideFilter();
            else if (view == filterSearchEditor)
                toggleKeyboard(true);
            else if (view == filterSearchButton) {
                final Object tag = filterSearchButton.getTag();

                if (!Utils.isEmpty((CharSequence) tag) && tag.equals("filter")) {
                    filterCheck[0] = SettingsHelper.isFilterWords();
                    filterCheck[1] = SettingsHelper.isFilterDefinition();
                    filterCheck[2] = SettingsHelper.isFilterContains();

                    new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.select_filters).setMultiChoiceItems(new String[]{getString(R.string.words), getString(R.string.defs), getString(R.string.contains)},
                            filterCheck, (dialogInterface, i, checked) -> {
                                filterCheck[i] = checked;
                                if (i == 0) SettingsHelper.setFilter("filterWord", checked);
                                else if (i == 1) SettingsHelper.setFilter("filterDefinition", checked);
                                else if (i == 2) SettingsHelper.setFilter("filterContain", checked);
                            }).setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                                if (wordList.size() > 2)
                                    wordsAdapter.getFilter().filter(filterSearchEditor.getText());
                                dialogInterface.dismiss();
                            }).show();
                } else {
                    filterSearchEditor.setText("");
                    filterSearchButton.setTag("filter");
                }
            }
        };

        filterBackButton.setOnClickListener(onClickListener);
        filterSearchEditor.setOnClickListener(onClickListener);
        filterSearchButton.setOnClickListener(onClickListener);
    }

    void startWords(final String method, final String word) {
        if (filterView != null && filterSearchEditor != null)
            filterSearchEditor.setText("");
        if (!Utils.isEmpty(word))
            TaskExecutor.executeAsync(new WordsAsync(this, word, method, activity));
    }

    @Override
    public void done(final ArrayList<WordItem> items, final String word) {
        wordList = items == null ? new ArrayList<>() : items;

        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));

        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);
        wordsAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(wordsAdapter);

        title = word;
        activity.setTitle(title);
    }

    @Override
    public void wordStarted() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> {
                swipeRefreshLayout.setEnabled(true);
                swipeRefreshLayout.setRefreshing(true);
            });
        }
    }

    /**
     * DO NOT CHANGE ANYTHING HERE, I STILL DON'T HAVE
     * ANY IDEA WHAT IS METHOD ABOUT, PLEASE FORGIVE ME.
     * I ASK FOR FORGIVENESS GOD. I SWEAR.
     * 14th March 2020, still a little clue, trying to make it work with enums
     * @param showFilter either to show or hide filter
     * @param method     no idea what this is. i really forgot.
     */
    void showFilter(final boolean showFilter, final FilterMethod method) {
        if (filterView != null) {
            final boolean isRefrehing = swipeRefreshLayout.isRefreshing();
            final boolean isMethod0 = method == FilterMethod.RECYCLER_PADDING;
            final boolean isMethod2 = method == FilterMethod.RECYCLER_NO_PADDING;

            if (showFilter) {
                if (isMethod0 || isMethod2) {
                    filterView.setVisibility(View.VISIBLE);
                    if (isMethod0 && filterSearchEditor != null)
                        filterSearchEditor.requestFocus();
                }

                if (recyclerView != null) {
                    if (isMethod0) recyclerView.setPadding(0, topMargin, 0, recyclerView.getPaddingBottom());

                    final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    final boolean managerNotNull = manager != null;

                    final boolean canScroll = wordsAdapter != null && wordsAdapter.getItemCount() > 5 || managerNotNull && manager.getItemCount() > 5;
                    if (canScroll && managerNotNull && manager.findFirstVisibleItemPosition() <= 3)
                        recyclerView.smoothScrollToPosition(0);
                }
            } else {
                if (isMethod0 || isMethod2)
                    filterView.setVisibility(View.GONE);

                if (recyclerView != null)
                    recyclerView.setPadding(0, topPadding, 0, recyclerView.getPaddingBottom());
            }

            if (method != FilterMethod.DO_NOTHING) {
                swipeRefreshLayout.setProgressViewOffset(false, startOffset, showFilter ? expandedEndOffset : endOffset);
                if (isRefrehing) swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    void hideFilter() {
        showFilter(false, FilterMethod.RECYCLER_PADDING);
    }

    boolean isFilterOpen() {
        return filterView != null && filterView.getVisibility() == View.VISIBLE;
    }

    private void toggleKeyboard(final boolean show) {
        if (Utils.inputMethodManager != null) {
            if (show) Utils.inputMethodManager.showSoftInput(filterSearchEditor, 1);
            else Utils.inputMethodManager.hideSoftInputFromWindow(filterSearchEditor.getWindowToken(), 1);
        }
    }

    void scrollRecyclerView(final boolean directionUp) {
        if (recyclerView != null) {
            final int itemCount = wordsAdapter == null ? 0 : wordsAdapter.getItemCount();
            if (itemCount > 0 && recyclerView.getLayoutManager() != null) {
                final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
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

                smoothScroller.setScrollPerInch(scrollPerInch);
                smoothScroller.setTargetPosition(directionUp ? 0 : itemCount - 1);
                manager.startSmoothScroll(smoothScroller);
            }
        }
    }

    private float getExpandedOffset() {
        final TypedValue tv = new TypedValue();
        if (activity == null) activity = getActivity();
        if (activity != null && activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data, Utils.displayMetrics);
        return swipeRefreshLayout == null ? 250f : swipeRefreshLayout.getProgressViewEndOffset() * 2f;
    }

    void closeExpanded() {
        if (wordsAdapter != null) {
            wordsAdapter.refreshShowDialogEnabled();

            for (final WordItemViewHolder holder : wordsAdapter.holdersHashSet)
                holder.cardView.setCardBackgroundColor(-1);

            for (final WordItem wordItem : wordsAdapter.expandedHashSet)
                wordItem.setExpanded(false);
            wordsAdapter.notifyDataSetChanged();
        }
    }

    public enum FilterMethod {
        DO_NOTHING, RECYCLER_PADDING, RECYCLER_NO_PADDING,
    }
}