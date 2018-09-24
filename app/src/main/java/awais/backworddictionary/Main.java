package awais.backworddictionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SnackbarContentLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TooltipCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.NoNet;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.custom.AdvancedDialog;
import awais.backworddictionary.custom.MenuCaller;
import awais.backworddictionary.custom.SettingsDialog;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import io.fabric.sdk.android.Fabric;

public class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
    public static TextToSpeech tts;
    private TabLayout tabLayout;
    public ViewPager viewPager;
    public DictionariesAdapter adapter;
    public SearchView mSearchView;
    private SearchAdapter searchAdapter;
    private SearchHistoryTable mHistoryDatabase;
    private FloatingActionButton fabFilter;
    private MenuCaller menuCaller;
    private ImageView noInternet;
    private Snackbar snackbar;
    public static String[] boolsArray;
    private static final String[] tabs = {"Reverse", "Sounds Like", "Spelled Like", "Synonyms", "Antonyms",
            "Triggers", "Is Part of", "Comprises of", "Rhymes", "Homophones"};

    public static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-6411761147229517~1317441366");

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        noInternet = findViewById(R.id.noInternet);
        fabFilter = findViewById(R.id.filterButton);

        snackbar = Snackbar.make(viewPager, R.string.no_connection, -2);
        SnackbarContentLayout contentLayout = (SnackbarContentLayout) ((ViewGroup)snackbar.getView()).getChildAt(0);
        Button actionButton = contentLayout.findViewById(R.id.snackbar_action);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText(R.string.settings);
        actionButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));

        menuCaller = new MenuCaller(this);
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        setSupportActionBar(findViewById(R.id.toolbar));

        TooltipCompat.setTooltipText(fabFilter, "Filter");
        fabFilter.setOnClickListener(view ->
                adapter.getItem(viewPager.getCurrentItem()).isOpen(true, fabFilter, 0));

        handleData();

        setupNoNet();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setSearchView();
        myThread loadFragments = new myThread(1);
        myThread setupTTS = new myThread(2);

        loadFragments.start();
        setupTTS.start();
    }

    private class myThread extends Thread {
        private final int method;
        myThread(int method) { this.method = method; }
        @Override public void run() {
            switch (method) {
                case 1: loadFragments(true); break;
                case 2:
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
                    break;
            }
        }
    }

    private void setupNoNet() {
        NoNet.monitor(this).configure(NoNet.configure().endpoint("http://api.datamuse.com/words")
                .connectedPollFrequency(1).disconnectedPollFrequency(1).build()).callback(connectionStatus -> {
            Log.d("AWAISKING_APP", "--> " + adapter.getItem(viewPager.getCurrentItem()));
            if (connectionStatus != ConnectionStatus.CONNECTED) {
                if (adapter != null && adapter.getCount() > 0 && (
                        adapter.getItem(viewPager.getCurrentItem()).adapter != null
                                && adapter.getItem(viewPager.getCurrentItem()).adapter.getItemCount() < 1
                        || adapter.getItem(viewPager.getCurrentItem()).adapter == null
                        )) {
                    if (noInternet != null) noInternet.setVisibility(View.VISIBLE);
                    if (viewPager != null)
                        viewPager.setBackgroundColor(getResources().getColor(R.color.noInternet));
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.noInternetBar)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(getResources().getColor(R.color.noInternetStatusBar));
                    if (findViewById(R.id.tabs) != null)
                        findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.noInternetBar));
                    if (fabFilter != null) {
                        fabFilter.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.noInternetAccent)));
                        if (fabFilter.getParent() != null && fabFilter.getParent() instanceof View)
                            ((View) fabFilter.getParent()).setBackgroundColor(getResources().getColor(R.color.noInternetBar));
                    }
                }
            } else {
                if (noInternet != null) noInternet.setVisibility(View.GONE);
                if (viewPager != null) viewPager.setBackground(null);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                if (findViewById(R.id.tabs) != null)
                    findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (fabFilter != null) {
                    fabFilter.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    if (fabFilter.getParent() != null && fabFilter.getParent() instanceof View)
                        ((View) fabFilter.getParent()).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }).snackbar(snackbar);
    }

    @Override
    public void loadFragments(boolean main) {
        if (!main) {
            finish();
            startActivity(new Intent(this, Main.class));
            return;
        }
        String bools = Main.sharedPreferences.getString("tabs", "[true, true, true, true, false, false, false, false, false, false]");
        if (tabLayout != null) try { tabLayout.clearOnTabSelectedListeners(); } catch (Exception ignored) {}
        adapter = new DictionariesAdapter(getSupportFragmentManager());
        bools = bools.substring(1, bools.length() - 1);
        boolsArray = bools.split(", ");
        for (int i=0; i<boolsArray.length; i++)
            if (boolsArray[i].equalsIgnoreCase("true"))
                adapter.addFrag(new DictionaryFragment(), tabs[i]);
        viewPager.setOffscreenPageLimit(5);
        if (tabLayout == null) tabLayout = findViewById(R.id.tabs);
        if (tabLayout == null) return;
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            int prevTab = 0;
            @Override public void onTabSelected(final TabLayout.Tab tab) {
                setTitle(R.string.app_name);

                if (adapter != null) {
                    final DictionaryFragment currentItem = adapter.getItem(tab.getPosition());
                    final DictionaryFragment prevItem = adapter.getItem(prevTab);

                    if (currentItem != null) {
                        if (currentItem.title != null && (!currentItem.title.isEmpty()|| TextUtils.isEmpty(currentItem.title)))
                            setTitle(currentItem.title);
                        else {
                            if (prevItem.title != null && !prevItem.title.isEmpty()) {
                                try {
                                    currentItem.adapter.updateList(new ArrayList<>());
                                    currentItem.startWords(tab.getText(), prevItem.title);
                                    currentItem.title = prevItem.title;
                                } catch (Exception e) {
                                    Log.e("AWAISKING_APP", "" , e);
                                }
                            }
                        }
                        try {
                            if (currentItem.adapter != null && currentItem.adapter.getItemCount() > 0) {
                                noInternet.setVisibility(View.GONE);
                                viewPager.setBackground(null);
                                if (getSupportActionBar() != null)
                                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                                findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                if (fabFilter != null)
                                    fabFilter.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                            }
                        } catch (Exception ignored) {}
                        if (prevItem != null) prevItem.isOpen(true, fabFilter, 30);
                        currentItem.isOpen(false, fabFilter, 0);
                    }
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { prevTab = tab.getPosition(); }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public int getItemPosition(String item) {
        return adapter.mFragmentTitleList.indexOf(item);
    }

    private void handleData() {
        if (getIntent() == null && (getIntent().getExtras() == null || getIntent().getAction() == null))
            return;

        Handler handler;
        if (getMainLooper() != null) handler = new Handler(getMainLooper());
        else handler = new Handler();

        Bundle bundle = getIntent().getExtras();
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEND)
                && getIntent().getType() != null && getIntent().getType().equals("text/plain")) {
            if (bundle != null && bundle.containsKey(Intent.EXTRA_TEXT)) {
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

        if (bundle != null && bundle.containsKey("query")) {
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
            new SettingsDialog(this).show();
            return true;
        });

        return true;
    }

    public void onSearch(String word) {
        try {
            if (adapter != null && viewPager != null) {
                CharSequence method = adapter.getPageTitle(viewPager.getCurrentItem());
                adapter.getItem(viewPager.getCurrentItem()).startWords(method, word);
            }
            if (mSearchView != null && mSearchView.isSearchOpen()) mSearchView.close(false);
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "" , e);
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
        if (result != null && !result.isEmpty() && mSearchView != null
                && mSearchView != null && searchAdapter != null) {
            ArrayList<SearchItem> suggestionsList = new ArrayList<>();
            for (WordItem item : result) suggestionsList.add(new SearchItem(item.getWord()));
            searchAdapter.setData(suggestionsList);
            searchAdapter.setSuggestionsList(suggestionsList);
            searchAdapter.notifyDataSetChanged();
            mSearchView.showSuggestions();
        }
    }

    public class DictionariesAdapter extends FragmentPagerAdapter {
        private final List<DictionaryFragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        DictionariesAdapter(FragmentManager manager) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuCaller.show(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && viewPager != null && adapter.getCount() > 0 &&
                adapter.getItem(viewPager.getCurrentItem()).isFilterOpen())
            adapter.getItem(viewPager.getCurrentItem()).hideFilter();
        else super.onBackPressed();
    }

    private void setSearchView() {
        mSearchView = findViewById(R.id.searchView);
        if (mSearchView != null) {
            mSearchView.bringToFront();

            mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
            mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
            mSearchView.setHint(R.string.search);
            mSearchView.setVoice(false);
            mSearchView.setTheme(SearchView.THEME_LIGHT);
            mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));

            mHistoryDatabase = new SearchHistoryTable(this);
            mHistoryDatabase.setHistorySize(8);

            searchAdapter = new SearchAdapter(this);
            searchAdapter.setHasStableIds(true);
            searchAdapter.addOnItemClickListener((View view, int position) -> {
                TextView tvItem = view.findViewById(R.id.textView_item_text);

//                if (searchAdapter.getSuggestionsList() != null
//                        && searchAdapter.getSuggestionsList().size() > 0
//                        && searchAdapter.getSuggestionsList().get(position) != null) {
//                    SearchItem item = searchAdapter.getSuggestionsList().get(position);
//                    onSearch(String.valueOf(item.get_text()));
//                    mHistoryDatabase.addItem(item);
//                }

                SearchItem item = new SearchItem(tvItem.getText());
                onSearch(String.valueOf(tvItem.getText()));
                mHistoryDatabase.addItem(item);
            });
            mSearchView.setAdapter(searchAdapter);

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                String text = "";
                long last_text_edit = System.currentTimeMillis();
                final Handler handler = new Handler();
                final Runnable textWatch = () -> {
                    if (System.currentTimeMillis() > (last_text_edit + 200L)) {
                        if (text != null && !text.isEmpty() && !TextUtils.isEmpty(text) && !text.equals(""))
                            new SearchAsync(Main.this).execute(text);
                    }
                };

                @Override
                public boolean onQueryTextSubmit(String query) {
                    try { handler.removeCallbacks(textWatch); } catch (Exception ignored) {}
                    onSearch(query);
                    mHistoryDatabase.addItem(new SearchItem(query));
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    text = newText;
                    if (newText != null && !TextUtils.isEmpty(newText) && !newText.equals("")) {
                        last_text_edit = System.currentTimeMillis();
                        try { handler.removeCallbacks(textWatch); } catch (Exception ignored) {}
                        handler.postDelayed(textWatch, 800);
                    }
                    return false;
                }
            });

            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override public boolean onClose() {
                    if (adapter != null && adapter.getItem(viewPager.getCurrentItem()) != null &&
                            !adapter.getItem(viewPager.getCurrentItem()).isFilterOpen())
                        adapter.getItem(viewPager.getCurrentItem()).isOpen(false, fabFilter, 1);
                    return false;
                }

                @Override public boolean onOpen() {
                    adapter.getItem(viewPager.getCurrentItem()).isOpen(true, fabFilter, 1);
                    return false;
                }
            });
        }
    }
}