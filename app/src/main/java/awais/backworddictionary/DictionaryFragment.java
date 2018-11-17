package awais.backworddictionary;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FilterCheck;
import awais.backworddictionary.interfaces.FragmentCallback;

public class DictionaryFragment extends Fragment implements FragmentCallback, FilterCheck {
    private List<WordItem> wordList;
    private RecyclerView recyclerView;
    private Activity activity;
    private InputMethodManager imm;

    private EditText filterSearchEditor;
    private ImageView filterSearchButton;
    private FrameLayout filterView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final boolean[] filterCheck = {true, true, true};
    public DictionaryWordsAdapter wordsAdapter;
    public String title;

    private static int startOffset=-120, endOffset, topPadding;

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {Main.tts.stop();} catch (Exception ignored){}
        try {Main.tts.shutdown();} catch (Exception ignored){}
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        final View magicRootView = inflater.inflate(R.layout.dictionary_view, container, false);

        if (getActivity() != null) activity = getActivity();
        else activity = (Activity) getContext();

        if (activity != null)
            imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        wordList = new ArrayList<>();
        wordsAdapter = new DictionaryWordsAdapter(getContext(), wordList);
        wordsAdapter.setHasStableIds(true);

        swipeRefreshLayout = magicRootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeResources(R.color.progress1, R.color.progress2,
                R.color.progress3, R.color.progress4);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            if (getActivity() != null) {
                Main activity = (Main) getActivity();
                if (activity.mSearchView != null && activity.mSearchView.isSearchOpen())
                    activity.mSearchView.close(true);
                if (!activity.getTitle().equals(getResources().getString(R.string.app_name)))
                    activity.onSearch(String.valueOf(activity.getTitle()));
                else swipeRefreshLayout.setRefreshing(false);
            }
        });

        startOffset = swipeRefreshLayout.getProgressViewStartOffset();
        endOffset = (int) getEndOffset();

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(wordsAdapter);

        topPadding = recyclerView.getPaddingTop();

        filterView = magicRootView.findViewById(R.id.filterView);

        ImageView filterBackButton = magicRootView.findViewById(R.id.filterBack);
        filterBackButton.setOnClickListener(view -> showFilter(false, fab, 0));
        filterSearchEditor = magicRootView.findViewById(R.id.swipeSearch);
        filterSearchButton = magicRootView.findViewById(R.id.filterSettings);
        filterSearchButton.setTag("filter");
        filterSearchEditor.setOnClickListener(view -> toggleKeyboard(true));
        filterSearchEditor.setOnFocusChangeListener((view, b) -> toggleKeyboard(b));
        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                if (wordList.size() > 2) wordsAdapter.getFilter().filter(cs);
            }
            @Override public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    filterSearchButton.setImageResource(R.drawable.ic_clear);
                    filterSearchButton.setTag("clear");
                } else {
                    filterSearchButton.setImageResource(R.drawable.ic_settings);
                    filterSearchButton.setTag("filter");
                }
            }
        });
        filterSearchButton.setOnClickListener(view -> {
            if (filterSearchButton.getTag() != null && !TextUtils.isEmpty((CharSequence) filterSearchButton.getTag())
                    && filterSearchButton.getTag().equals("filter")) {
                filterCheck[0] = Main.sharedPreferences.getBoolean("filterWord", true);
                filterCheck[1] = Main.sharedPreferences.getBoolean("filterDefinition", false);
                filterCheck[2] = Main.sharedPreferences.getBoolean("filterContain", false);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(getString(R.string.select_filters));
                builder.setMultiChoiceItems(new String[]{getString(R.string.words), getString(R.string.defs), getString(R.string.contains)}, filterCheck,
                        (dialogInterface, i, b) -> {
                            filterCheck[i] = b;
                            String item = "";
                            switch (i) {
                                case 0: item = "filterWord"; break;
                                case 1: item = "filterDefinition"; break;
                                case 2: item = "filterContain"; break;
                            }
                            Main.sharedPreferences.edit().putBoolean(item, b).apply();
                        });
                builder.setNeutralButton(getString(R.string.ok), (dialogInterface, i) -> {
                    if (wordList.size() > 2)
                        wordsAdapter.getFilter().filter(filterSearchEditor.getText());
                    dialogInterface.dismiss();
                });
                builder.create().show();
            } else {
                filterSearchEditor.setText("");
                filterSearchButton.setTag("filter");
            }
        });

        return magicRootView;
    }

    private void toggleKeyboard(boolean show) {
        if (imm != null) {
            if (show) imm.showSoftInput(filterSearchEditor, 1);
            else imm.hideSoftInputFromWindow(filterSearchEditor.getWindowToken(), 1);
        }
    }

    public void startWords(CharSequence method, String word) {
        if (filterView != null && filterSearchEditor != null) filterSearchEditor.setText("");
        if (word == null || word.isEmpty() || TextUtils.isEmpty(word)) return;
        new WordsAsync(this, word, (String) method, this.activity).execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null) activity = getActivity();
        else activity = (Activity) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public void done(ArrayList<WordItem> items, final String word) {
        wordList = items != null ? items : new ArrayList<>();
        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);
        wordsAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(wordsAdapter);

        title = word;
        activity.setTitle(title);

        recyclerView.setClickable(true);
        recyclerView.setEnabled(true);
        recyclerView.setFocusable(true);
        recyclerView.setLayoutFrozen(false);
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public void wordStarted() {
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        recyclerView.setClickable(false);
        recyclerView.setEnabled(false);
        recyclerView.setFocusable(false);
        recyclerView.setLayoutFrozen(true);
    }

    public boolean isFilterOpen() {
        return filterView != null && filterView.getVisibility() == View.VISIBLE;
    }

    public void hideFilter() {
        if (fab != null) showFilter(false, fab, 0);
    }


    /**
     * DO NOT CHANGE ANYTHING HERE, I STILL DON'T HAVE
     * ANY IDEA WHAT IS METHOD ABOUT, PLEASE FORGIVE ME.
     * I ASK FOR FORGIVENESS GOD. I SWEAR.
     * @param showFilter either to show or hide filter
     * @param fab        the floating action button
     * @param method     no idea what this is. i really forgot.
     */
    @Override
    public void showFilter(boolean showFilter, FloatingActionButton fab, int method) {
        this.fab = fab;

        float topMargin = getResources().getDimension(R.dimen.toolbarSize);

        if (showFilter) {
            if (method == 0 && filterView != null) {
                filterView.setVisibility(View.VISIBLE);
                if (filterSearchEditor != null) filterSearchEditor.requestFocus();
            } else if (method == 30 && filterView != null)
                filterView.setVisibility(View.GONE);

            if (recyclerView != null) {
                if (method == 0)
                    recyclerView.setPadding(0, (int) topMargin, 0, recyclerView.getPaddingBottom());
                if (recyclerView.getLayoutManager() != null &&
                        ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() <= 3)
                    recyclerView.smoothScrollToPosition(0);
                swipeRefreshLayout.setProgressViewOffset(false, startOffset, endOffset);
            }
            hideFAB();
        } else {
            if (method == 0 && filterView != null)
                filterView.setVisibility(View.GONE);
            else if (method == 30 && filterView != null)
                filterView.setVisibility(View.VISIBLE);
            if (recyclerView != null) {
                recyclerView.setPadding(0, topPadding, 0, recyclerView.getPaddingBottom());
                swipeRefreshLayout.setProgressViewOffset(false, startOffset, (int) (endOffset-(endOffset*0.7)));
            }
            showFAB();
        }
    }

    private void hideFAB() {
        if (fab == null) return;
        fab.animate().cancel();
        fab.animate().scaleX(1f).scaleY(1f);
        fab.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200)
                .setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationEnd(Animator animation) {
                        fab.hide();
                        ((View)fab.getParent()).setVisibility(View.GONE);
                    }
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }

    private void showFAB() {
        if (fab == null) return;
        fab.animate().cancel();
        fab.animate().scaleX(0f).scaleY(0f);
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationEnd(Animator animation) {
                        fab.show();
                        ((View)fab.getParent()).setVisibility(View.VISIBLE);
                    }
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }

    private float getEndOffset() {
        TypedValue tv = new TypedValue();
        if (getActivity() != null &&
                getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        return swipeRefreshLayout != null ? swipeRefreshLayout.getProgressViewEndOffset() : 250.0f;
    }
}
