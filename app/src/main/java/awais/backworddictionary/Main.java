package awais.backworddictionary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.MobileAds;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.Monitor;
import com.keiferstone.nonet.NoNet;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.custom.MenuCaller;
import awais.backworddictionary.custom.SettingsDialog;

public class Main extends AppCompatActivity {
    public ViewPager viewPager;
    public DictionariesAdapter adapter;
    private SearchView mSearchView;
    public static SearchAdapter searchAdapter;
//    private List<SearchItem> suggestionsList;
    private FloatingActionButton fabFilter;
    private MenuCaller menuCaller;
    private ImageView noInternet;

    public static SharedPreferences sharedPreferences;

    @SuppressLint("ClickableViewAccessibility") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        noInternet = findViewById(R.id.noInternet);
        fabFilter = findViewById(R.id.filterButton);

        menuCaller = new MenuCaller(this);
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupNoNet(false);

        MobileAds.initialize(this, "ca-app-pub-6411761147229517~1317441366");

        setupLayout();
        setSearchView();

        TooltipCompat.setTooltipText(fabFilter, "Filter");
        fabFilter.setOnClickListener(view ->
                adapter.getItem(viewPager.getCurrentItem()).isOpen(true, fabFilter, 0));

        findViewById(R.id.shadow).setOnTouchListener((view, motionEvent) -> false);

        handleData();
    }

    private void handleData() {
        if (getIntent() == null && getIntent().getExtras() == null) return;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("query")) {
            Handler handler;

            if (getMainLooper() != null) handler = new Handler(getMainLooper());
            else handler = new Handler();

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

        menu.findItem(R.id.mRefresh).setOnMenuItemClickListener(item -> {
            if (mSearchView != null) mSearchView.close(true);
            if (!getTitle().equals(getResources().getString(R.string.app_name)))
                onSearch(getTitle());
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

    private void onSearch(CharSequence text) {
        onSearch(text.toString());
    }
    public void onSearch(String word) {
        try {
            if (adapter != null && viewPager != null)
                adapter.getItem(viewPager.getCurrentItem()).startWords(viewPager.getCurrentItem(), word);
            if (mSearchView != null && mSearchView.isSearchOpen()) mSearchView.close(true);
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "" , e);
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

    private void setupLayout() {
        adapter = new DictionariesAdapter(getSupportFragmentManager());
        adapter.addFrag(new DictionaryFragment(), "Reverse");
        adapter.addFrag(new DictionaryFragment(), "Sounds Like");
        adapter.addFrag(new DictionaryFragment(), "Spelled Like");
        viewPager.setOffscreenPageLimit(5);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            int prevTab = 0;

            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                setTitle(R.string.app_name);

                if (adapter != null) {
                    final DictionaryFragment currentItem = adapter.getItem(tab.getPosition());
                    final DictionaryFragment prevItem = adapter.getItem(prevTab);

                    if (currentItem != null) {
                        if (currentItem.title != null && (!currentItem.title.equals("")||!currentItem.title.isEmpty()))
                            setTitle(currentItem.title);
                        else {
                            if (prevItem.title != null && !prevItem.title.isEmpty()) {
                                try {
                                    currentItem.adapter.updateList(new ArrayList<>());
                                    currentItem.startWords(tab.getPosition(), prevItem.title);
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
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { prevTab = tab.getPosition(); }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && viewPager != null && adapter.getItem(viewPager.getCurrentItem()).isFilterOpen())
            adapter.getItem(viewPager.getCurrentItem()).hideFilter();
        else super.onBackPressed();
    }

    private void setSearchView() {
        mSearchView = findViewById(R.id.searchView);
        if (mSearchView != null) {
            mSearchView.bringToFront();
//            suggestionsList = new ArrayList<>();

            mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
            mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
            mSearchView.setHint(R.string.search);
            mSearchView.setVoice(false);
            mSearchView.setTheme(SearchView.THEME_LIGHT);
            mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));

            searchAdapter = new SearchAdapter(this);
            searchAdapter.setHasStableIds(true);
            searchAdapter.addOnItemClickListener((view, position) ->
                    onSearch(searchAdapter.getSuggestionsList().get(position).get_text()));
            mSearchView.setAdapter(searchAdapter);

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                String text = "";
                long last_text_edit = System.currentTimeMillis();
                final Handler handler = new Handler();
                final Runnable textWatch = () -> {
                    if (System.currentTimeMillis() > (last_text_edit + 200L)) {
                        if (text != null && !text.isEmpty() && !TextUtils.isEmpty(text) && !text.equals(""))
                            new SearchAsync(mSearchView).execute(text);
                    }
                };

                @Override
                public boolean onQueryTextSubmit(String query) {
                    try { handler.removeCallbacks(textWatch); } catch (Exception ignored) {}
                    onSearch(query);
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
                @Override
                public boolean onClose() {
                    if (!adapter.getItem(viewPager.getCurrentItem()).isFilterOpen())
                        adapter.getItem(viewPager.getCurrentItem()).isOpen(false, fabFilter, 1);
                    return false;
                }

                @Override
                public boolean onOpen() {
                    adapter.getItem(viewPager.getCurrentItem()).isOpen(true, fabFilter, 1);
                    return false;
                }
            });
        }
    }

//    @SuppressLint("StaticFieldLeak")
//    private class SearchTask extends AsyncTask<String, Void, ArrayList<WordItem>> {
//        private final OkHttpClient client = new OkHttpClient();
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mSearchView.showProgress();
//        }
//
//        @Override
//        protected ArrayList<WordItem> doInBackground(String... params) {
//            String query;
//            try {
//                query = URLEncoder.encode(params[0], "UTF-8");
//            } catch (Exception e) {
//                query = params[0].replace("&", "%26").replace("@","%40").replace("#","%23");
//            }
//            ArrayList<WordItem> arrayList = new ArrayList<>();
//
//            Response response = null;
//            try {
//                if (isCancelled()) {
//                    client.dispatcher().cancelAll();
//                    client.connectionPool().evictAll();
//                    client.cache().close();
//                }
//
//                Call call = client.newCall(new Request.Builder().url("https://api.datamuse.com/sug?s=" + query).build());
//                response = call.execute();
//
//                if (isCancelled()) {
//                    call.cancel();
//                    response.close();
//                }
//
//                if (response != null && response.code() == 200) {
//                    //noinspection ConstantConditions
//                    arrayList = new Gson().fromJson(response.body().string(),
//                            new TypeToken<List<WordItem>>(){}.getType());
//                }
//            } catch (Exception e) {
//                Log.e("AWAISKING_APP", "", e);
//            } finally {
//                if (response != null) response.close();
//            }
//            return arrayList;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<WordItem> result) {
//            super.onPostExecute(result);
//            if (!isCancelled()) {
//                if (mSearchView.isShowingProgress()) mSearchView.hideProgress();
//                if (!result.isEmpty()) {
//                    if (suggestionsList != null) suggestionsList.clear();
//                    else suggestionsList = new ArrayList<>();
//                    for (WordItem item : result) suggestionsList.add(new SearchItem(item.getWord()));
//                    searchAdapter.setData(suggestionsList);
//                    searchAdapter.notifyDataSetChanged();
//                    mSearchView.showSuggestions();
//                }
//            }
//        }
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupNoNet(true);
    }

    private void setupNoNet(boolean isConfig) {
        Monitor.Builder noNet = NoNet.monitor(this).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build());
        if (!isConfig) noNet = noNet.poll();
        noNet = noNet.callback(connectionStatus -> {
            if (connectionStatus != ConnectionStatus.CONNECTED) {
                if (adapter != null && adapter.getItem(viewPager.getCurrentItem()).adapter != null
                        && adapter.getItem(viewPager.getCurrentItem()).adapter.getItemCount() < 1) {
                    if (noInternet != null) noInternet.setVisibility(View.VISIBLE);
                    if (viewPager != null) viewPager.setBackgroundColor(getResources().getColor(R.color.noInternet));
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.noInternetBar)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(getResources().getColor(R.color.noInternetStatusBar));
                    if(findViewById(R.id.tabs) != null)
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
                if(findViewById(R.id.tabs) != null)
                    findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (fabFilter != null) {
                    fabFilter.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    if (fabFilter.getParent() != null && fabFilter.getParent() instanceof View)
                        ((View) fabFilter.getParent()).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        if (!isConfig)
            noNet.snackbar(Snackbar.make(viewPager, "Not connected to internet.", -2)
                    .setAction("Settings", view -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS))));
    }

}
