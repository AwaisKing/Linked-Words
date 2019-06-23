package awais.backworddictionary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TooltipCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Locale;

import awais.backworddictionary.adapters.DictionaryFragmentsAdapter;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.custom.AdvancedDialog;
import awais.backworddictionary.custom.MenuCaller;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.custom.SettingsDialog;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    private static final int[] tabs = {R.string.reverse, R.string.sounds_like, R.string.spelled_like, R.string.synonyms, R.string.antonyms,
            R.string.triggers, R.string.part_of, R.string.comprises, R.string.rhymes, R.string.homophones};
    public static TextToSpeech tts;
    public static String[] boolsArray;
    public static SharedPreferences sharedPreferences;
    public DictionaryFragmentsAdapter fragmentsAdapter;
    private FloatingActionMenu fabOptions;
    private SearchAdapter searchAdapter;
    private SearchHistoryTable mHistoryDatabase;
    private MenuCaller menuCaller;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    public MaterialSearchView mSearchView;
    public ViewPager viewPager;
    private TabLayout tabLayout;
    private AppBarLayout.LayoutParams toolbarParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.initCrashlytics(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        Utils.adsBox(this);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        fabOptions = findViewById(R.id.fabOptions);
        appBarLayout = findViewById(R.id.appbarLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbarParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        fabOptions.getMenuIconView().setImageDrawable(Utils.getDrawable(this, R.drawable.ic_options));
        fabOptions.setClosedOnTouchOutside(true);
        final ViewGroup.LayoutParams fabParams = fabOptions.getLayoutParams();
        fabOptions.setOnMenuButtonLongClickListener(v -> {
            if (fragmentsAdapter == null || viewPager == null) return true;
            DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            if (fragment != null && fragment.isAdded())
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, 0);
            return true;
        });
        fabOptions.setOnMenuButtonClickListener(v -> {
            if (!fabOptions.isOpened()) {
                fabParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                fabParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                fabOptions.setLayoutParams(fabParams);
            }
            fabOptions.toggle(true);
        });
        fabOptions.setOnMenuToggleListener(opened -> {
            if (opened) return;
            fabOptions.postDelayed(() -> {
                fabParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                fabParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                fabOptions.setLayoutParams(fabParams);
            }, 300);
        });
        fabAnimation(fabOptions);
        setFabListeners(fabOptions, v -> {
            if (fragmentsAdapter == null || viewPager == null) return;
            DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            if (fragment != null && fragment.isAdded())
                switch ((int) v.getTag()) {
                    case 0: // filter
                        if (fragment.isFilterOpen()) fragment.hideFilter();
                        else fragment.showFilter(true, 0);
                        break;
                    case 1: // scroll to bottom
                        fragment.scrollRecyclerView(false);
                        break;
                    case 2: // scroll to top
                        fragment.scrollRecyclerView(true);
                        break;
                }
            fabOptions.close(true);
        });

        menuCaller = new MenuCaller(this);
        setSupportActionBar(findViewById(R.id.toolbar));

        TooltipCompat.setTooltipText(fabOptions, getString(R.string.options));

        handleData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        myThread loadFragments = new myThread(true, this);
        myThread setupTTS = new myThread(false, this);

        loadFragments.start();
        setupTTS.start();

        setSearchView();
    }

    @Override
    public void loadFragments(boolean main) {
        if (!main) {
            finish();
            startActivity(new Intent(this, Main.class));
            return;
        }

        String bools = Main.sharedPreferences.getString("tabs", "[true, true, true, true, false, false, false, false, false, false]");
        if (Utils.isEmpty(bools)) bools = "[true, true, true, true, false, false, false, false, false, false]";
        bools = bools.substring(1, bools.length() - 1);
        boolsArray = bools.split(", ");

        fragmentsAdapter = new DictionaryFragmentsAdapter(getSupportFragmentManager());
        for (int i = 0; i < boolsArray.length; i++)
            if (boolsArray[i].equalsIgnoreCase("true"))
                fragmentsAdapter.addFragment(new DictionaryFragment(), getString(tabs[i]));

        if (tabLayout != null) tabLayout.clearOnTabSelectedListeners();

        viewPager.setOffscreenPageLimit(5);
        if (tabLayout == null) tabLayout = findViewById(R.id.tabs);
        if (tabLayout == null) return;
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(fragmentsAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            int prevTab = 0;

            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                setTitle(R.string.app_name);
                if (fragmentsAdapter == null) return;

                final int currTab = tab.getPosition();
                final DictionaryFragment currentItem = fragmentsAdapter.getItem(currTab);
                final DictionaryFragment prevItem = fragmentsAdapter.getItem(prevTab);

                if (currentItem != null && currentItem.isAdded()) {
                    if (!Utils.isEmpty(currentItem.title))
                        setTitle(currentItem.title);
                    else {
                        if (prevItem.title != null && !prevItem.title.isEmpty()) {
                            try {
                                currentItem.wordsAdapter.updateList(new ArrayList<>());
                                currentItem.startWords(tab.getText(), prevItem.title);
                                currentItem.title = prevItem.title;
                            } catch (Exception e) {
                                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
                            }
                        }
                    }

                    if (prevItem != null && prevItem.isAdded())
                        prevItem.showFilter(prevItem.isFilterOpen(), 2);
                    currentItem.showFilter(currentItem.isFilterOpen(), currentItem.isFilterOpen() ? 2 : 0);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { prevTab = tab.getPosition(); }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public int getItemPosition(String item) {
        return fragmentsAdapter.mFragmentTitleList.indexOf(item);
    }

    private void handleData() {
        Intent thisIntent = getIntent();
        if (thisIntent == null) return;

        Bundle bundle = thisIntent.getExtras();
        String action = thisIntent.getAction();
        String type = thisIntent.getType();
        if (bundle == null || action == null || type == null) return;

        Handler handler = new Handler();
        //        if (getMainLooper() != null) handler = new Handler(getMainLooper());
        //        else handler = new Handler();

        if (action.equals(Intent.ACTION_SEND) && type.equals("text/plain")) {
            if (bundle.containsKey(Intent.EXTRA_TEXT)) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onSearch(bundle.getString(Intent.EXTRA_TEXT));
                        handler.removeCallbacks(this);
                    }
                }, 400);
            }
            return;
        }

        if (bundle.containsKey("query")) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onSearch(bundle.getString("query"));
                    handler.removeCallbacks(this);
                }
            }, 400);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != MaterialSearchView.SPEECH_REQUEST_CODE
                || resultCode != Activity.RESULT_OK || data == null) return;
        ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (mSearchView != null && text != null && text.size() > 0)
            mSearchView.setQuery(text.get(0), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.mAdvance).setOnMenuItemClickListener(item -> {
            new AdvancedDialog(this).show();
            return true;
        });

        menu.findItem(R.id.mSearch).setOnMenuItemClickListener(item -> {
            if (mSearchView != null) {
                mSearchView.open(true, item);
                mSearchView.bringToFront();
            }
            return true;
        });

        menu.findItem(R.id.mSettings).setOnMenuItemClickListener(item -> {
            new SettingsDialog(Main.this).show();
            return true;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuCaller.show(item);
        return super.onOptionsItemSelected(item);
    }

    public void onSearch(String word) {
        try {
            if (fragmentsAdapter != null && viewPager != null) {
                final int pagerCurrentItem = viewPager.getCurrentItem();
                final CharSequence method = fragmentsAdapter.getPageTitle(pagerCurrentItem);
                DictionaryFragment fragment = fragmentsAdapter.getItem(pagerCurrentItem);
                if (fragment != null && fragment.isAdded())
                    fragment.startWords(method, word);
            }
            if (mSearchView != null && mSearchView.isSearchOpen()) {
                mSearchView.close(false);
                toolbarParams.setScrollFlags(5);
                toolbar.setLayoutParams(toolbarParams);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }
    }

    @Override
    public void afterSearch(ArrayList<WordItem> result) {
        if (mSearchView != null && mSearchView.isShowingProgress()) mSearchView.hideProgress();

        if (result == null || result.isEmpty() || mSearchView == null || searchAdapter == null)
            return;

        ArrayList<SearchItem> suggestionsList = new ArrayList<>();
        for (WordItem item : result)
            suggestionsList.add(new SearchItem(item.getWord()));

        searchAdapter.setData(suggestionsList);
        searchAdapter.setSuggestionsList(suggestionsList);
        searchAdapter.notifyDataSetChanged();
        mSearchView.showSuggestions();
    }

    @Override
    public void onBackPressed() {
        if (fragmentsAdapter == null || viewPager == null || fragmentsAdapter.getCount() <= 0)
            super.onBackPressed();
        else {
            DictionaryFragment dictionaryFragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            if (dictionaryFragment != null && dictionaryFragment.isAdded()
                    && dictionaryFragment.isFilterOpen())
                dictionaryFragment.hideFilter();
            else super.onBackPressed();
        }
    }

    private void setSearchView() {
        mSearchView = findViewById(R.id.searchView);
        if (mSearchView == null) return;
        mSearchView.bringToFront();

        mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));

        mHistoryDatabase = new SearchHistoryTable(this);
        mHistoryDatabase.setHistorySize(8);

        searchAdapter = new SearchAdapter(this, mHistoryDatabase);
        searchAdapter.setHasStableIds(true);
        searchAdapter.addOnItemClickListener((View view, int position, String text) -> {
            onSearch(text);
            addHistoryItem(text);
        });
        searchAdapter.addOnItemLongClickListener((view, position, text) -> {
            AlertDialog alertDialog = new AlertDialog.Builder(Main.this).create();
            alertDialog.setTitle(getString(R.string.remove_recent));
            alertDialog.setMessage(getString(R.string.confirm_remove, text));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), (dialog, which) -> {
                deleteHistoryItem(text);
                dialog.dismiss();
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            TextView tvMessage = alertDialog.findViewById(android.R.id.message);
            if (tvMessage != null) {
                tvMessage.setTypeface(LinkedApp.fontRegular);
                String tvMessageText = tvMessage.getText().toString();
                SpannableStringBuilder messageSpan = new SpannableStringBuilder(tvMessageText);
                int spanStart = tvMessageText.indexOf('"');
                int spanEnd = tvMessageText.lastIndexOf('"');
                messageSpan.setSpan(new StyleSpan(Typeface.BOLD), spanStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpan.delete(spanStart, spanStart + 1);
                messageSpan.delete(spanEnd - 1, spanEnd);
                tvMessage.setText(messageSpan);
            }
            return true;
        });
        mSearchView.setAdapter(searchAdapter);

        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private String text = "";
            private final Runnable textWatch = () -> {
                final SearchAsync async = new SearchAsync(Main.this);
                if (!Utils.isEmpty(text)) async.execute(text);
                else try { async.cancel(true); } catch (Exception ignore) {}
            };

            @Override
            public boolean onQueryTextSubmit(String query) {
                try { handler.removeCallbacks(textWatch); } catch (Exception ignored) {}
                if (mHistoryDatabase != null && !Utils.isEmpty(query))
                    try {
                        addHistoryItem(query);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                onSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                text = newText;
                if (!Utils.isEmpty(newText)) {
                    try { handler.removeCallbacks(textWatch); } catch (Exception ignored) {}
                    handler.postDelayed(textWatch, 800);
                    mSearchView.showProgress();
                } else mSearchView.hideProgress();
                return false;
            }
        });

        mSearchView.setOnOpenCloseListener(new MaterialSearchView.OnOpenCloseListener() {
            private final DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            private int oldScrollFlag = toolbarParams.getScrollFlags();

            @Override
            public boolean onClose() {
                if (fragment != null && fragment.isAdded() && !fragment.isFilterOpen())
                    fragment.showFilter(false, 1);
                toolbarParams.setScrollFlags(oldScrollFlag);
                toolbar.setLayoutParams(toolbarParams);
                return false;
            }

            @Override
            public boolean onOpen() {
                if (fragment != null && fragment.isAdded())
                    fragment.showFilter(true, 1);
                appBarLayout.setExpanded(true, true);
                oldScrollFlag = toolbarParams.getScrollFlags();
                toolbarParams.setScrollFlags(0);
                toolbar.setLayoutParams(toolbarParams);
                return false;
            }
        });
    }

    private void addHistoryItem(CharSequence query) {
        mHistoryDatabase.addItem(new SearchItem(query));
        searchAdapter.getFilter().filter("");
    }

    private void deleteHistoryItem(@NonNull String query) {
        mHistoryDatabase.clearDatabase(query);
        searchAdapter.getFilter().filter("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHistoryDatabase.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHistoryDatabase.close();
    }

    private static class myThread extends Thread {
        private final boolean method;
        private final Main activity;

        myThread(boolean method, Main activity) {
            this.method = method;
            this.activity = activity;
        }

        @Override
        public void run() {
            if (method) {
                try {
                    activity.loadFragments(true);
                } catch (Exception ignored) {
                    activity.runOnUiThread(() -> activity.loadFragments(true));
                }
            } else {
                tts = new TextToSpeech(activity, initStatus -> {
                    if (initStatus == TextToSpeech.SUCCESS) {
                        if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                            tts.setLanguage(Locale.US);
                        else if (tts.isLanguageAvailable(Locale.CANADA) == TextToSpeech.LANG_AVAILABLE)
                            tts.setLanguage(Locale.CANADA);
                        else if (tts.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE)
                            tts.setLanguage(Locale.UK);
                        else if (tts.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
                            tts.setLanguage(Locale.ENGLISH);
                    }
                });
            }
        }
    }

    private static void fabAnimation(FloatingActionMenu fabOptions) {
        AnimatorSet set = new AnimatorSet();
        ImageView fabIcon = fabOptions.getMenuIconView();
        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(fabIcon, "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(fabIcon, "scaleY", 1.0f, 0.2f);
        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(fabIcon, "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(fabIcon, "scaleY", 0.2f, 1.0f);
        scaleOutX.setDuration(200);
        scaleOutY.setDuration(200);
        scaleInX.setDuration(200);
        scaleInY.setDuration(200);
        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabIcon.setImageResource(fabOptions.isOpened() ? R.drawable.ic_close : R.drawable.ic_options);
            }
        });
        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(3.17f));
        fabOptions.setIconToggleAnimatorSet(set);
    }

    private static void setFabListeners(FloatingActionMenu fabOptions, View.OnClickListener onClickListener) {
        if (fabOptions == null) return;
        int[] resIds = {R.drawable.ic_filter, R.drawable.ic_arrow_down, R.drawable.ic_arrow_up};
        Context context = fabOptions.getContext();

        int j = 0;
        for (int i = fabOptions.getChildCount() - 1; i > -1; --i) {
            View view = fabOptions.getChildAt(i);
            if (view instanceof com.github.clans.fab.FloatingActionButton) {
                final FloatingActionButton fab = (FloatingActionButton) view;
                if (fab.getButtonSize() == 0) continue;
                Drawable drawable = Utils.getDrawable(context, resIds[j]);
                drawable.setColorFilter(0xFF2196F3, PorterDuff.Mode.SRC_ATOP);
                fab.setImageDrawable(drawable);
                fab.setTag(j++);
                fab.setOnClickListener(onClickListener);
            }
        }
    }
}