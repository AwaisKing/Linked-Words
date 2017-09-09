package awais.backworddictionary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.NoNet;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.custom.MenuCaller;
import awais.backworddictionary.custom.SettingsDialog;
import awais.backworddictionary.custom.WordItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends AppCompatActivity {
    public ViewPager viewPager;
    public DictionariesAdapter adapter;
    private SearchView mSearchView;
    private SearchAdapter searchAdapter;
    private List<SearchItem> suggestionsList;
    private FloatingActionButton fabFilter;
    private final MenuCaller menuCaller = new MenuCaller(this);

    public static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        final ImageView noInternet = findViewById(R.id.noInternet);
        try {
            NoNet.monitor(this).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build())
                    .poll()
                    .callback(connectionStatus -> {
                        if (connectionStatus != ConnectionStatus.CONNECTED) {
                            if (adapter != null && adapter.getItem(viewPager.getCurrentItem()).adapter != null
                                    && adapter.getItem(viewPager.getCurrentItem()).adapter.getItemCount() < 1) {
                                noInternet.setVisibility(View.VISIBLE);
                                viewPager.setBackgroundColor(getResources().getColor(R.color.noInternet));
                            }
                        } else {
                            noInternet.setVisibility(View.GONE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                viewPager.setBackground(null);
                            else viewPager.setBackgroundDrawable(null);
                        }
                    })
                    .snackbar(Snackbar.make(viewPager, "Not connected to internet.", -2)
                            .setAction("Settings", view -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS))));
        } catch (Exception ignored) {}
        MobileAds.initialize(this, "ca-app-pub-6411761147229517~1317441366");

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        setSupportActionBar(findViewById(R.id.toolbar));

        setupLayout();
        setSearchView();

        fabFilter = findViewById(R.id.filterButton);
        fabFilter.setOnClickListener(view ->
                adapter.getItem(viewPager.getCurrentItem()).isOpen(true, fabFilter, 0));

        findViewById(R.id.shadow).bringToFront();
        fabFilter.bringToFront();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.mRefresh).setOnMenuItemClickListener(item -> {
            mSearchView.close(true);
            if (!getTitle().equals(getResources().getString(R.string.app_name)))
                onSearch(getTitle().toString());
            return true;
        });

        menu.findItem(R.id.mSearch).setOnMenuItemClickListener(item -> {
            mSearchView.open(true, item);
            mSearchView.bringToFront();
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
            adapter.getItem(viewPager.getCurrentItem()).startWords(viewPager.getCurrentItem(), word);
            if (mSearchView.isSearchOpen()) mSearchView.close(true);
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
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
                                new Handler().post(() -> {
                                    try {
                                        currentItem.startWords(tab.getPosition(), prevItem.title);
                                        currentItem.title = prevItem.title;
                                    } catch (Exception e) {
                                        Log.e("AWAISKING_APP", "" , e);
                                    }
                                });
                            }
                        }
                    }
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                prevTab = tab.getPosition();
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setSearchView() {
        mSearchView = findViewById(R.id.searchView);
        mSearchView.bringToFront();
        if (mSearchView != null) {
            suggestionsList = new ArrayList<>();

            mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
            mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
            mSearchView.setHint(R.string.search);
            mSearchView.setVoice(false);
            mSearchView.setTheme(SearchView.THEME_LIGHT);
            mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));

            searchAdapter = new SearchAdapter(this, suggestionsList);
            searchAdapter.setHasStableIds(true);
            searchAdapter.addOnItemClickListener((view, position) ->
                    onSearch(suggestionsList.get(position).get_text().toString()));
            mSearchView.setAdapter(searchAdapter);

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null && !TextUtils.isEmpty(newText))
                        new SearchTask().execute(newText);
                    return false;
                }
            });

            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public boolean onClose() {
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

    @SuppressLint("StaticFieldLeak")
    private class SearchTask extends AsyncTask<String, Void, ArrayList<WordItem>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSearchView.showProgress();
        }

        @Override
        protected ArrayList<WordItem> doInBackground(String... params) {
            String query = params[0].replace("&", "%26").replace("@","%40").replace("#","%23");
            ArrayList<WordItem> arrayList = new ArrayList<>();

            Response response = null;
            try {
                response = new OkHttpClient().newCall(new Request.Builder()
                        .url("https://api.datamuse.com/sug?s=" + query).build()).execute();
                if (response.code() == 200)
                    arrayList = new Gson().fromJson(response.body().string(),
                            new TypeToken<List<WordItem>>(){}.getType());
            } catch (Exception e) {
                Log.e("AWAISKING_APP", "", e);
            } finally {
                if (response != null) response.close();
            }
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<WordItem> result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                if (mSearchView.isShowingProgress()) mSearchView.hideProgress();
                if (!result.isEmpty()) {
                    if (suggestionsList != null) suggestionsList.clear();
                    else suggestionsList = new ArrayList<>();
                    for (WordItem item : result) suggestionsList.add(new SearchItem(item.getWord()));
                    searchAdapter.setData(suggestionsList);
                    searchAdapter.notifyDataSetChanged();
                    Log.i("AWAISKING_APP", "getItemCount: " + searchAdapter.getItemCount());
                    mSearchView.showSuggestions();
                }

            }
        }
    }
}
