package awais.backworddictionary;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.ads.MobileAds;
import com.rafakob.nsdhelper.NsdHelper;
import com.rafakob.nsdhelper.NsdListener;
import com.rafakob.nsdhelper.NsdService;
import com.rafakob.nsdhelper.NsdType;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.customweb.CustomTabActivityHelper;
import awais.backworddictionary.customweb.WebViewFallback;
import awais.floatysearch.SearchResultItem;
import awais.floatysearch.onSearchListener;
import awais.floatysearch.onSimpleSearchActionsListener;
import awais.floatysearch.widgets.MaterialSearchView;


public class Main extends AppCompatActivity implements onSimpleSearchActionsListener, onSearchListener {
    private boolean mSearchViewAdded = false, searchActive = false;
    private MaterialSearchView mSearchView;
    private WindowManager mWindowManager;
    ViewPager viewPager;
    DictionariesAdapter adapter;
    private NsdHelper nsdHelper;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-6411761147229517~1317441366");

        mWindowManager = getWindowManager();
        mSearchView = new MaterialSearchView(this);
//        mSearchView.setHintText("Search");

        TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= 21)
            findViewById(R.id.shadow).setVisibility(View.GONE);
        else
            findViewById(R.id.shadow).setVisibility(View.VISIBLE);

//        checkDrawOverlayPermission();

        // TODO: SNACKS
        final Snackbar snackbar = Snackbar.make(viewPager, "Not connected to internet.",
                Snackbar.LENGTH_INDEFINITE).setAction("Settings",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));}});
//        snackbar.setCallback(new Snackbar.Callback() {
//            @Override
//            public void onShown(Snackbar sb) {
//                Log.d("AWAISKING_APP", "[os]shown: " + sb.isShown());
//            }
//
//            @Override
//            public void onDismissed(Snackbar sb, @DismissEvent int event) {
//                Log.d("AWAISKING_APP", "[od]shown: " + sb.isShown());
//                if (event == Snackbar.Callback.DISMISS_EVENT_MANUAL) sb.dismiss();
//            }
//        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            merlin = new Merlin.Builder().withEndpoint(Endpoint.from("http://www.datamuse.com/api/"))
//                    .withResponseCodeValidator(new ResponseCodeValidator() {
//                        @Override
//                        public boolean isResponseCodeValid(int responseCode) {return responseCode == 200;
//                        }
//                    })
//                    .withConnectableCallbacks().withDisconnectableCallbacks()
//                    .withBindableCallbacks().build(this);
//        else
//            merlin = new Merlin.Builder().withConnectableCallbacks().withDisconnectableCallbacks()
//                    .withBindableCallbacks().build(this);
        nsdHelper = new NsdHelper(this, new NsdListener() {
            @Override
            public void onNsdRegistered(NsdService nsdService) {}
            @Override
            public void onNsdDiscoveryFinished() {}

            @Override
            public void onNsdServiceFound(NsdService nsdService) {
                snackbar.dismiss();
            }

            @Override
            public void onNsdServiceResolved(NsdService nsdService) {
                snackbar.dismiss();
            }

            @Override
            public void onNsdServiceLost(NsdService nsdService) {
                snackbar.show();
            }

            @Override
            public void onNsdError(String s, int i, String s1) {
                snackbar.show();
            }
        });
        nsdHelper.registerService("WordService", NsdType.HTTP);

//        merlin.registerConnectable(new Connectable() {
//            @Override
//            public void onConnect() {
//                snackbar.dismiss();
//            }
//        });
//        merlin.registerDisconnectable(new Disconnectable() {
//            @Override
//            public void onDisconnect() {
//                snackbar.show();
//            }
//        });
//        merlin.registerBindable(new Bindable() {
//            @Override
//            public void onBind(NetworkStatus networkStatus) {
//                if (networkStatus.isAvailable())
//                    snackbar.dismiss();
//                else snackbar.show();
//            }
//        });
//        merlin.bind();

        adapter = new DictionariesAdapter(getSupportFragmentManager());
        adapter.addFrag(new DictionaryFragment(), "Reverse");
        adapter.addFrag(new DictionaryFragment(), "Sounds Like");
        adapter.addFrag(new DictionaryFragment(), "Spelled Like");

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
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
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            currentItem.startWords(tab.getPosition(), prevItem.title);
                                            currentItem.title = prevItem.title;
                                        } catch (Exception e) {
                                            Log.e("AWAISKING_APP", "" , e);
                                        }
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

        mSearchView.setOnSearchListener(this);
        mSearchView.setSearchResultsListener(this);

        if (mToolbar != null) mToolbar.post(new Runnable() {
            @Override
            public void run() {
                if (!mSearchViewAdded && mWindowManager != null && mSearchView != null)
                    if (!isFinishing()) {
                        mWindowManager.addView(mSearchView, MaterialSearchView.getSearchViewLayoutParams(Main.this));
                        mSearchViewAdded = true;
//                        mToolbar.removeCallbacks(this);
                    }
            }
        });

//        WordItem wordItem = new WordItem("lel", null, -1,
//                new String[]{"awtawt","234432","gddg","jgj","fgjfgj","fdgs","bxcgcb","hgkk","cxcxwt","sdghfgh"});
//        WordDialog wordDialog = new WordDialog(this, wordItem, null);
//        wordDialog.show();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mSearchView != null) onCancelSearch();
        try {mWindowManager.removeViewImmediate(mSearchView);} catch (Exception ignored) {}
        try {mWindowManager.removeView(mSearchView);} catch (Exception ignored) {}

        mSearchView = new MaterialSearchView(this);
        mSearchViewAdded = false;
        mSearchView.setOnSearchListener(this);
        mSearchView.setSearchResultsListener(this);

        if (mToolbar != null) mToolbar.post(new Runnable() {
            @Override
            public void run() {
                if (!mSearchViewAdded && mWindowManager != null && mSearchView != null)
                    if (!isFinishing()) {
                        mWindowManager.addView(mSearchView, MaterialSearchView.getSearchViewLayoutParams(Main.this));
                        mSearchViewAdded = true;
//                        mToolbar.removeCallbacks(this);
                    }
            }
        });
    }

    @Override
    public boolean isFinishing() {
        nsdHelper.unregisterService();
        try {mWindowManager.removeViewImmediate(mSearchView);} catch (Exception ignored) {}
        try {mWindowManager.removeView(mSearchView);} catch (Exception ignored) {}
        mSearchViewAdded = false;
        return super.isFinishing();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.mSearch).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mSearchView.display();
                openKeyboard();
                if (!getTitle().equals(getString(R.string.app_name))) {
                    mSearchView.setSearchQuery(getTitle().toString());
                    mSearchView.selectAll();
                }
                return true;
            }
        });
        if(searchActive) mSearchView.display();

        return true;

    }

    private void openKeyboard(){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mSearchView.getSearchView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                mSearchView.getSearchView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 200);
    }

    @Override
    public void onScroll() {}
    @Override
    public void error(String localizedMessage) {}

    @Override
    public void onSearch(String word, boolean isEnter) {
        DictionaryFragment item = adapter.getItem(viewPager.getCurrentItem());
        if (isEnter) {
            try {
                item.startWords(viewPager.getCurrentItem(), word);
                onCancelSearch();
            } catch (Exception e) {
                Log.e("AWAISKING_APP", "" , e);
            }
        }
    }

    public final static int REQUEST_CODE = 2423;


//    @TargetApi(Build.VERSION_CODES.M)
//    public void checkDrawOverlayPermission() {
//        if (!Settings.canDrawOverlays(this))
//            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName())), REQUEST_CODE);
//    }
//
//    @TargetApi(Build.VERSION_CODES.M)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
//        if (requestCode == REQUEST_CODE) if (!Settings.canDrawOverlays(this)); //finish();
//    }

    @Override
    public void onItemClicked(SearchResultItem word) {
        try {
            adapter.getItem(viewPager.getCurrentItem()).startWords(viewPager.getCurrentItem(), word.getWord());
            onCancelSearch();
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "" , e);
        }
    }
    @Override
    public void onCancelSearch() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchActive = false;
                mSearchView.hide();
                handler.removeCallbacks(this);
            }
        }, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nsdHelper.startDiscovery(NsdType.HTTP);
    }

    @Override
    protected void onPause() {
        nsdHelper.stopDiscovery();
        super.onPause();
    }

    class DictionariesAdapter extends FragmentPagerAdapter {
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

///////////////////////////////////////// TODO: END
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuDialog bottomSheetDialogFragment = new MenuDialog();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        final CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

        switch (item.getItemId()) {
            case R.id.mExamples:
                stringBuilder.append("Finding a word by its definition:\n");
                stringBuilder.append("person who makes gold\n");
                stringBuilder.append("one who massages\n");
                stringBuilder.append("food search\n\n");
                stringBuilder.append("Finding related words:\n");
                stringBuilder.append("rainbow colors\n");
                stringBuilder.append("tropical birds\n");
                stringBuilder.append("spicy vegetables\n\n");
                stringBuilder.append("Finding answers:\n");
                stringBuilder.append("what's popular city of Pakistan?\n");
                stringBuilder.append("what's largest continent on earth?\n");
                stringBuilder.append("who was Galileo?\n\n");
                stringBuilder.append("Wildcards [Spelled Like]:\n");
                stringBuilder.append("l?nd\t-- here ? matches any single character.\n");
                stringBuilder.append("fr*k\t-- here * matches any number of characters.\n");
                stringBuilder.append("ta#t\t-- here # matches any English consonant.\n");
                stringBuilder.append("**stone**\t-- find phrases with stone word in it.\n");
                stringBuilder.append("te@s\t-- here @ matches any English vowel.");

                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 34,51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 56, 72, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 73, 84, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 109, 123, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 124, 138, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 139, 155, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 174, 206, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 207, 241, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 242, 258, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 286, 331, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 331, 380, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 380, 426, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 426, 469, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 475, 516, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                bottomSheetDialogFragment.setTitle(item.getTitle());
                bottomSheetDialogFragment.setMessage(stringBuilder);
                break;

            case R.id.mHelp:
                stringBuilder.append("Reverse:\n");
                stringBuilder.append("find related words, phrases, answers by a word or by meaning\n\n");
                stringBuilder.append("Sounds Like:\n");
                stringBuilder.append("find words which sound similar to given word\n\n");
                stringBuilder.append("Spelled Like:\n");
                stringBuilder.append("find words which are spelled like defined word with wildcards\n");
                stringBuilder.append("[Wildcard Help]");

                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 9,69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 84,128, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 144,221, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#FFC400"));
                        CustomTabActivityHelper.openCustomTab(Main.this, customTabsIntent.build(),
                                Uri.parse("https://www.onelook.com/?c=faq#patterns"), new WebViewFallback());
                    }}, 206, 221, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                bottomSheetDialogFragment.setTitle(item.getTitle());
                bottomSheetDialogFragment.setMessage(stringBuilder);
                break;

            case R.id.mLicenses:
                stringBuilder.append("App Icon:\n");
                stringBuilder.append("Android Asset Studio - Launcher icon generator\n\n");
                stringBuilder.append("Dictionary API:\n");
                stringBuilder.append("Datamuse API\n\n");
                stringBuilder.append("Libraries:\n");
                stringBuilder.append("OkHttp3 [Apache License 2.0]\n");
                stringBuilder.append("GSON [Apache License 2.0]\n");
                stringBuilder.append("material-searchview [Apache License 2.0]\n");
                stringBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n\n");
                stringBuilder.append("License:\n");
                stringBuilder.append("Apache License 2.0");

                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 10,56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#607d8b"));
                        CustomTabActivityHelper.openCustomTab(Main.this, customTabsIntent.build(),
                                Uri.parse("https://romannurik.github.io/AndroidAssetStudio/"), new WebViewFallback());
                    }}, 10,56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 74,86, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#006fcc"));
                        CustomTabActivityHelper.openCustomTab(Main.this, customTabsIntent.build(),
                                Uri.parse("http://www.datamuse.com/api/"), new WebViewFallback());
                    }},74,86, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 99,127, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 128,153, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 154,194, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 195,234, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 235, 243, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new RelativeSizeSpan(1.1f), 235, 243, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 235, 243, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new BulletSpan(26, 0xFF212121), 245,263, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#cb2533"));
                        CustomTabActivityHelper.openCustomTab(Main.this, customTabsIntent.build(),
                                Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"), new WebViewFallback());
                    }},245,263, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                bottomSheetDialogFragment.setTitle(item.getTitle() + " & Credits");
                bottomSheetDialogFragment.setMessage(stringBuilder);
                break;

            case R.id.mAbout:
                bottomSheetDialogFragment.setTitle(item.getTitle());
                bottomSheetDialogFragment.setMessage("aboutHere");
                break;

            default: break;
        }
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

        return super.onOptionsItemSelected(item);
    }
}
