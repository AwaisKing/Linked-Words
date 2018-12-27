package awais.backworddictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.custom.AdvancedDialog;
import awais.backworddictionary.custom.MenuCaller;
import awais.backworddictionary.custom.SearchAdapter;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.custom.SettingsDialog;
import awais.backworddictionary.custom.Utils;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import awais.lapism.SearchItem;
import awais.lapism.SearchView;

public class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    private static final int[] tabs = {R.string.reverse, R.string.sounds_like, R.string.spelled_like, R.string.synonyms, R.string.antonyms,
            R.string.triggers, R.string.part_of, R.string.comprises, R.string.rhymes, R.string.homophones};
    public static TextToSpeech tts;
    public static String[] boolsArray;
    public static SharedPreferences sharedPreferences;
    public ViewPager viewPager;
    public DictionaryFragmentsAdapter fragmentsAdapter;
    public SearchView mSearchView;
    private TabLayout tabLayout;
    private SearchAdapter searchAdapter;
    private SearchHistoryTable mHistoryDatabase;
    private FloatingActionButton fabFilter;
    private MenuCaller menuCaller;
//    private ImageView noInternet;
//    private Snackbar snackbar;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.initCrashlytics(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        Utils.adsBox(this);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
//        noInternet = findViewById(R.id.noInternet);
        fabFilter = findViewById(R.id.filterButton);
        appBarLayout = findViewById(R.id.appbarLayout);
        // fabOptions = findViewById(R.id.fabOptions);

//        snackbar = Snackbar.make(viewPager, R.string.no_connection, -2);

//        SnackbarContentLayout contentLayout = (SnackbarContentLayout) ((ViewGroup) snackbar.getView()).getChildAt(0);
//        Button actionButton = contentLayout.findViewById(R.id.snackbar_action);
//        actionButton.setVisibility(View.VISIBLE);
//        actionButton.setText(R.string.settings);
//        actionButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
//        try {
//            View snack = (View) contentLayout.getParent();
//            CoordinatorLayout.LayoutParams snackParams = (CoordinatorLayout.LayoutParams) snack.getLayoutParams();
//            snackParams.width = CoordinatorLayout.LayoutParams.MATCH_PARENT;
//            snackParams.gravity = Gravity.FILL_HORIZONTAL | Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
//            snack.setLayoutParams(snackParams);
//        } catch (Exception ignored) {}

        menuCaller = new MenuCaller(this);
        setSupportActionBar(findViewById(R.id.toolbar));

        TooltipCompat.setTooltipText(fabFilter, getString(R.string.filter));
        fabFilter.setOnClickListener(view -> {
                DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
                if (fragment != null && fragment.isAdded())
                    fragment.showFilter(true, fabFilter, 0);
        });

        handleData();

//        setupNoNet();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        myThread loadFragments = new myThread(true);
        myThread setupTTS = new myThread(false);

        loadFragments.start();
        setupTTS.start();
        
        setSearchView();
    }

//    private void setupNoNet() {
//        NoNet.monitor(this).configure(NoNet.configure().endpoint("http://api.datamuse.com/words")
//                .connectedPollFrequency(1).disconnectedPollFrequency(1).build()).callback(connectionStatus -> {
//
//            DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
//            if (connectionStatus != ConnectionStatus.CONNECTED) {
//                if (fragment == null || fragment.wordsAdapter == null || fragment.wordsAdapter.getItemCount() <= 0)
//                    connectionTheme(false);
//            } else {
//                connectionTheme(true);
//                if (fragment != null && !Utils.isEmpty(fragment.title) &&
//                        !fragment.title.equals(getString(R.string.app_name))
//                        && fragment.wordsAdapter.getItemCount() < 1)
//                    onSearch(fragment.title);
//            }
//
//        }).snackbar(snackbar);
//    }

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
                fragmentsAdapter.addFrag(new DictionaryFragment(), getString(tabs[i]));

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

                final DictionaryFragment currentItem = fragmentsAdapter.getItem(tab.getPosition());
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
                                if (BuildConfig.DEBUG)
                                    Log.e("AWAISKING_APP", "", e);
                            }
                        }
                    }
//                    try {
//                        if (currentItem.wordsAdapter != null && currentItem.wordsAdapter.getItemCount() > 0)
//                            connectionTheme(true);
//                    } catch (Exception ignored) {}

                    if (prevItem != null && prevItem.isAdded())
                        prevItem.showFilter(true, fabFilter, 30);
                    currentItem.showFilter(false, fabFilter, 0);
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

    public void onSearch(String word) {
        try {
            if (fragmentsAdapter != null && viewPager != null) {
                final int pagerCurrentItem = viewPager.getCurrentItem();
                final CharSequence method = fragmentsAdapter.getPageTitle(pagerCurrentItem);
                DictionaryFragment fragment = fragmentsAdapter.getItem(pagerCurrentItem);
                if (fragment != null && fragment.isAdded())
                    fragment.startWords(method, word);
            }
            if (mSearchView != null && mSearchView.isSearchOpen())
                mSearchView.close(false);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }
    }

    @Override
    public void beforeSearch() {
        if (searchAdapter != null) searchAdapter.setSuggestionsList(new ArrayList<>());
        if (mSearchView != null) mSearchView.showProgress();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        menuCaller.show(item);
        return super.onOptionsItemSelected(item);
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

        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
        mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
        mSearchView.setHint(R.string.search);
        mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));

        mHistoryDatabase = new SearchHistoryTable(this);
        mHistoryDatabase.setHistorySize(8);

        searchAdapter = new SearchAdapter(mHistoryDatabase);
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

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private String text = "";
            private SearchAsync async = new SearchAsync(Main.this);
            private final Runnable textWatch = () -> {
                if (async.getStatus() == AsyncTask.Status.RUNNING)
                    async.cancel(true);
                if (async.getStatus() == AsyncTask.Status.FINISHED)
                    async = new SearchAsync(Main.this);
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
                }
                return false;
            }
        });

        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            private final Toolbar mToolbar = findViewById(R.id.toolbar);
            private final AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();

            @Override
            public boolean onClose() {
                if (fragmentsAdapter != null) {
                    DictionaryFragment currentFragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
                    if (currentFragment != null && currentFragment.isAdded()
                            && !currentFragment.isFilterOpen())
                        currentFragment.showFilter(false, fabFilter, 1);
                }
                p.setScrollFlags(5);
                mToolbar.setLayoutParams(p);
                return false;
            }

            @Override
            public boolean onOpen() {
                DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
                if (fragment != null && fragment.isAdded())
                    fragment.showFilter(true, fabFilter, 1);
                appBarLayout.setExpanded(true, true);
                p.setScrollFlags(0);
                mToolbar.setLayoutParams(p);
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

//    private void connectionTheme(boolean isConnected) {
//        if (isConnected) {
//            if (noInternet != null) noInternet.setVisibility(View.GONE);
//            if (viewPager != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//                    viewPager.setBackground(null);
//                viewPager.setBackgroundDrawable(null);
//                viewPager.setBackgroundResource(0);
//            }
//
//            if (appBarLayout != null)
//                appBarLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
//
//            int color = getResources().getColor(R.color.colorAccent);
//            if (fabFilter != null) {
//                fabFilter.setBackgroundColor(color);
//                fabFilter.setBackgroundTintList(ColorStateList.valueOf(color));
//            }
//        } else {
//            if (noInternet != null)
//                noInternet.setVisibility(View.VISIBLE);
//
//            int color = getResources().getColor(R.color.noInternet);
//            if (viewPager != null)
//                viewPager.setBackgroundColor(color);
//            if (appBarLayout != null)
//                appBarLayout.setBackgroundColor(color);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                getWindow().setStatusBarColor(getResources().getColor(R.color.noInternetStatusBar));
//
//            color = getResources().getColor(R.color.noInternetAccent);
//            if (fabFilter != null) {
//                fabFilter.setBackgroundColor(color);
//                fabFilter.setBackgroundTintList(ColorStateList.valueOf(color));
//            }
//        }
//    }

    private class myThread extends Thread {
        private final boolean method;

        myThread(boolean method) { this.method = method; }

        @Override
        public void run() {
            if (method) {
                try {
                    loadFragments(true);
                } catch (Exception ignored) {
                    runOnUiThread(() -> loadFragments(true));
                }
            } else {
                tts = new TextToSpeech(Main.this, initStatus -> {
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

    public class DictionaryFragmentsAdapter extends FragmentPagerAdapter {
        private final List<DictionaryFragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        DictionaryFragmentsAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public DictionaryFragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(DictionaryFragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}