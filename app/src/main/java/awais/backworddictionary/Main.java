package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.viewpager.widget.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Locale;

import awais.backworddictionary.DictionaryFragment.FilterMethod;
import awais.backworddictionary.adapters.DictionaryFragmentsAdapter;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.dialogs.AdvancedDialog;
import awais.backworddictionary.helpers.MenuHelper;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.dialogs.SettingsDialog;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.helpers.other.MyApps;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import awais.clans.FloatingActionButton;
import awais.clans.FloatingActionMenu;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private static final int[] tabs = {R.string.reverse, R.string.sounds_like, R.string.spelled_like, R.string.synonyms, R.string.antonyms,
            R.string.triggers, R.string.part_of, R.string.comprises, R.string.rhymes, R.string.homophones};
    public static boolean[] tabBoolsArray = {true, true, true, true, false, false, false, false, false, false};
    public static TextToSpeech tts;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private MenuHelper menuHelper;
    private AppBarLayout appBarLayout;
    private SearchAdapter searchAdapter;
    private FloatingActionMenu fabOptions;
    private SearchHistoryTable mHistoryDatabase;
    private AppBarLayout.LayoutParams toolbarParams;
    public DictionaryFragmentsAdapter fragmentsAdapter;
    public ViewPager viewPager;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.statusBarHeight = Utils.getStatusBarHeight(getWindow(), getResources());
        Utils.defaultLocale = Locale.getDefault();
        Utils.initCrashlytics(this);
        final int nightMode = SettingsHelper.getNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) AppCompatDelegate.setDefaultNightMode(nightMode);

        setContentView(R.layout.activity_main);

        Utils.adsBox(this);

        Utils.inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        menuHelper = new MenuHelper(this);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        fabOptions = findViewById(R.id.fabOptions);
        appBarLayout = findViewById(R.id.appbarLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbarParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        appBarLayout.setPadding(0, Utils.statusBarHeight, 0, 0);

        setSupportActionBar(toolbar);

        final ViewGroup.LayoutParams fabParams = fabOptions.getLayoutParams();
        fabOptions.setLongClickListener(v -> {
            if (fragmentsAdapter == null || viewPager == null) return true;
            DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            if (fragment.isAdded())
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            return true;
        }).setMenuButtonClickListener(v -> {
            if (!fabOptions.isOpened()) {
                fabParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                fabParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                fabOptions.setLayoutParams(fabParams);
            }
            fabOptions.toggle();
        }).setMenuToggleListener(opened -> {
            if (!opened) {
                fabParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                fabParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                fabOptions.setLayoutParams(fabParams);
            }
        });

        setFabListeners(fabOptions, v -> {
            if (fragmentsAdapter != null && viewPager != null) {
                final DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
                if (fragment.isAdded())
                    switch ((int) v.getTag()) {
                        case 0: // filter
                            if (fragment.isFilterOpen()) fragment.hideFilter();
                            else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
                            break;
                        case 1: // scroll to bottom
                            fragment.scrollRecyclerView(false);
                            break;
                        case 2: // scroll to top
                            fragment.scrollRecyclerView(true);
                            break;
                    }
                fabOptions.close();
            }
        });

        TooltipCompat.setTooltipText(fabOptions, getString(R.string.options));
        setSearchView();
        handleData();
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new myThread(true, this).start(); // load fragments
        new myThread(false, this).start(); // setup tts
    }

    @Override
    public void loadFragments(final boolean main) {
        if (main) {
            tabBoolsArray = SettingsHelper.getTabs();
            fragmentsAdapter = new DictionaryFragmentsAdapter(getSupportFragmentManager(), tabBoolsArray.length);
            for (int i = 0; i < tabBoolsArray.length; i++)
                if (tabBoolsArray[i])
                    fragmentsAdapter.addFragment(getString(tabs[i]));

            if (fragmentsAdapter.isEmpty())
                fragmentsAdapter.setFragments(getString(tabs[0]), getString(tabs[1]), getString(tabs[2]), getString(tabs[3]));

            viewPager.setOffscreenPageLimit(5);
            if (tabLayout == null) tabLayout = findViewById(R.id.tabs);
            if (tabLayout != null) {
                tabLayout.clearOnTabSelectedListeners();
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setAdapter(fragmentsAdapter);
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    private int prevTab = 0;

                    @Override
                    public void onTabSelected(final TabLayout.Tab tab) {
                        setTitle(R.string.app_name);
                        if (fragmentsAdapter != null) {
                            final int currTab = tab.getPosition();
                            final DictionaryFragment currentItem = fragmentsAdapter.getItem(currTab);
                            final DictionaryFragment prevItem = fragmentsAdapter.getItem(prevTab);

                            if (currentItem.isAdded()) {
                                if (!Utils.isEmpty(currentItem.title)) setTitle(currentItem.title);
                                else if (prevItem.title != null && !prevItem.title.isEmpty()) {
                                    try {
                                        currentItem.wordsAdapter.updateList(new ArrayList<>(0));
                                        currentItem.startWords(String.valueOf(tab.getText()), prevItem.title);
                                        currentItem.title = prevItem.title;
                                    } catch (Exception e) {
                                        if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
                                    }
                                }

                                if (prevItem.isAdded())
                                    prevItem.showFilter(prevItem.isFilterOpen(), FilterMethod.RECYCLER_NO_PADDING);
                                currentItem.showFilter(currentItem.isFilterOpen(),
                                        currentItem.isFilterOpen() ? FilterMethod.RECYCLER_NO_PADDING : FilterMethod.RECYCLER_PADDING);
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(final TabLayout.Tab tab) { prevTab = tab.getPosition(); }

                    @Override
                    public void onTabReselected(final TabLayout.Tab tab) {}
                });
            }
        } else {
            finish();
            startActivity(new Intent(this, Main.class));
        }
    }

    private void handleData() {
        final Intent intent = getIntent();
        if (intent != null) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                final String str;

                if (bundle.containsKey(Intent.EXTRA_TEXT)
                        && "text/plain".equals(intent.getType())
                        && Intent.ACTION_SEND.equals(intent.getAction()))
                    str = bundle.getString(Intent.EXTRA_TEXT);
                else if (bundle.containsKey("query"))
                    str = bundle.getString("query");
                else str = null;

                if (str != null) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onSearch(str);
                            handler.removeCallbacks(this);
                        }
                    }, 400);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleData();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MaterialSearchView.SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (searchView != null && text != null && text.size() > 0)
                searchView.setQuery(text.get(0), true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem.OnMenuItemClickListener clickListener = item -> {
            switch (item.getItemId()) {
                case R.id.mAdvance:
                    new AdvancedDialog(this).show();
                    break;

                case R.id.mSearch:
                    if (searchView != null) {
                        searchView.open(true, item);
                        searchView.bringToFront();
                    }
                    break;

                case R.id.mSettings:
                    new SettingsDialog(Main.this).show();
                    break;
            }
            return true;
        };

        menu.findItem(R.id.mAdvance).setOnMenuItemClickListener(clickListener);
        menu.findItem(R.id.mSearch).setOnMenuItemClickListener(clickListener);
        menu.findItem(R.id.mSettings).setOnMenuItemClickListener(clickListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        menuHelper.show(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void afterSearch(final ArrayList<WordItem> result) {
        if (searchView != null && searchView.isShowingProgress()) searchView.hideProgress();

        if (result != null && !result.isEmpty() && searchView != null && searchAdapter != null) {
            final ArrayList<SearchItem> suggestionsList = new ArrayList<>();
            for (final WordItem item : result)
                suggestionsList.add(new SearchItem(item.getWord()));

            searchAdapter.setData(suggestionsList);
            searchAdapter.setSuggestionsList(suggestionsList);
            searchAdapter.notifyDataSetChanged();
            searchView.showSuggestions();
        }
    }

    public void onSearch(final String word) {
        try {
            if (fragmentsAdapter != null && viewPager != null) {
                final int pagerCurrentItem = viewPager.getCurrentItem();
                final CharSequence method = fragmentsAdapter.getPageTitle(pagerCurrentItem);
                final DictionaryFragment fragment = fragmentsAdapter.getItem(pagerCurrentItem);
                if (fragment.isAdded())
                    fragment.startWords("" + method, word);
            }

            if (searchView != null && searchView.isSearchOpen()) {
                searchView.close(false);
                toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                toolbar.setLayoutParams(toolbarParams);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }
    }

    public void closeExpanded() {
        if (fragmentsAdapter != null) {
            for (int i = 0; i < fragmentsAdapter.getCount(); ++i) {
                final DictionaryFragment fragment = fragmentsAdapter.getItem(i);
                if (fragment.isAdded()) fragment.closeExpanded();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentsAdapter != null && viewPager != null && fragmentsAdapter.getCount() > 0) {
            final DictionaryFragment dictionaryFragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());
            if (dictionaryFragment.isAdded() && dictionaryFragment.isFilterOpen()) {
                dictionaryFragment.hideFilter();
                return;
            }
        }
        MyApps.showAlertDialog(this, (parent, view, position, id) -> {
            if (id == -1 && position == -1 && parent == null) super.onBackPressed();
            else MyApps.openAppStore(this, position);
        });
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

    private void addHistoryItem(final CharSequence query) {
        mHistoryDatabase.addItem(new SearchItem(query));
        searchAdapter.getFilter().filter("");
    }

    private void deleteHistoryItem(@NonNull final String query) {
        mHistoryDatabase.clearDatabase(query);
        searchAdapter.getFilter().filter("");
    }

    private void setSearchView() {
        searchView = findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.bringToFront();

            mHistoryDatabase = new SearchHistoryTable(this);

            searchAdapter = new SearchAdapter(this, mHistoryDatabase, (view, position, text) -> {
                onSearch(text);
                addHistoryItem(text);
            }, (view, position, text) -> {
                final DialogInterface.OnClickListener btnListener = (dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) deleteHistoryItem(text);
                    dialog.dismiss();
                };

                final AlertDialog alertDialog = new AlertDialog.Builder(Main.this)
                        .setTitle(R.string.remove_recent)
                        .setMessage(getString(R.string.confirm_remove, text))
                        .setPositiveButton(R.string.yes, btnListener)
                        .setNegativeButton(R.string.no, btnListener).show();

                final TextView tvMessage = alertDialog.findViewById(android.R.id.message);
                if (tvMessage == null) return true;

                tvMessage.setTypeface(LinkedApp.fontRegular);

                final String tvMessageText = "" + tvMessage.getText();
                if (Utils.isEmpty(tvMessageText)) return true;
                final SpannableStringBuilder messageSpan = new SpannableStringBuilder(tvMessageText);
                final int spanStart = tvMessageText.indexOf('"');
                final int spanEnd = tvMessageText.lastIndexOf('"');
                messageSpan.setSpan(new StyleSpan(Typeface.BOLD), spanStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpan.delete(spanStart, spanStart + 1);
                messageSpan.delete(spanEnd - 1, spanEnd);
                tvMessage.setText(messageSpan);
                return true;
            });
            searchView.setAdapter(searchAdapter);

            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                private final Handler handler = new Handler();
                private String text = "";
                private SearchAsync prevAsync;
                private final Runnable textWatch = () -> {
                    final SearchAsync async = new SearchAsync(Main.this);
                    if (!Utils.isEmpty(text)) {
                        if (prevAsync != null)
                            try {
                                prevAsync.cancel(true);
                            } catch (Exception ignore) {
                            }
                        async.execute(text);
                        prevAsync = async;
                    } else {
                        try {
                            prevAsync.cancel(true);
                        } catch (Exception ignore) {
                        }
                        try {
                            async.cancel(true);
                        } catch (Exception ignore) {
                        }
                    }
                };

                @Override
                public boolean onQueryTextSubmit(final String query) {
                    try {
                        handler.removeCallbacks(textWatch);
                    } catch (Exception ignored) {
                    }
                    if (mHistoryDatabase != null && !Utils.isEmpty(query))
                        try {
                            addHistoryItem(query);
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                        }
                    onSearch(query);
                    return query != null || BuildConfig.DEBUG;
                }

                @Override
                public void onQueryTextChange(final String newText) {
                    text = newText;
                    if (!Utils.isEmpty(newText)) {
                        try {
                            handler.removeCallbacks(textWatch);
                        } catch (Exception ignored) {
                        }
                        handler.postDelayed(textWatch, 800);
                        searchView.showProgress();
                    } else searchView.hideProgress();
                }
            });

            searchView.setOnOpenCloseListener(new MaterialSearchView.OnOpenCloseListener() {
                private DictionaryFragment fragment;
                private int oldScrollFlag = toolbarParams.getScrollFlags();

                @Override
                public void onClose() {
                    if (viewPager != null && toolbar != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());

                        if (fragment.isAdded() && !fragment.isFilterOpen())
                            fragment.showFilter(false, FilterMethod.DO_NOTHING);
                        toolbarParams.setScrollFlags(oldScrollFlag);
                        toolbar.setLayoutParams(toolbarParams);
                    }
                }

                @Override
                public void onOpen() {
                    if (viewPager != null && toolbar != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(viewPager.getCurrentItem());

                        if (fragment.isAdded())
                            fragment.showFilter(true, FilterMethod.DO_NOTHING);
                        appBarLayout.setExpanded(true, true);
                        oldScrollFlag = toolbarParams.getScrollFlags();
                        toolbarParams.setScrollFlags(0);
                        toolbar.setLayoutParams(toolbarParams);
                    }
                }
            });
        }
    }

    private static void setFabListeners(final FloatingActionMenu fabOptions, final View.OnClickListener onClickListener) {
        if (fabOptions != null) {
            final int[] resIds = {R.drawable.ic_filter, R.drawable.ic_arrow_down, R.drawable.ic_arrow_up};
            final Context context = fabOptions.getContext();

            int j = 0;
            for (int i = fabOptions.getChildCount() - 1; i > -1; --i) {
                final View view = fabOptions.getChildAt(i);
                if (view instanceof FloatingActionButton) {
                    final FloatingActionButton fab = (FloatingActionButton) view;
                    if (fab.getButtonSize() == FloatingActionButton.SIZE_MINI) {
                        final Drawable drawable = Utils.getDrawable(context, resIds[j]);
                        drawable.setColorFilter(new PorterDuffColorFilter(0xFF2196F3, PorterDuff.Mode.SRC_ATOP));

                        fab.setImageDrawable(drawable);
                        fab.setTag(j++);
                        fab.setOnClickListener(onClickListener);
                    }
                }
            }
        }
    }

    private final static class myThread extends Thread {
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
}