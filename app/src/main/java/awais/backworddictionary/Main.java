package awais.backworddictionary;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Locale;

import awais.backworddictionary.DictionaryFragment.FilterMethod;
import awais.backworddictionary.adapters.DictionaryFragmentsAdapter;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.asyncs.SearchAsync;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.dialogs.AdvancedDialog;
import awais.backworddictionary.dialogs.SettingsDialog;
import awais.backworddictionary.helpers.InitializerThread;
import awais.backworddictionary.helpers.MenuHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.MyApps;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import awais.backworddictionary.interfaces.SearchAdapterClickListener;
import awais.clans.FloatingActionMenu;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public final class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
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
    private SearchHistoryTable historyDatabase;
    private AppBarLayout.LayoutParams toolbarParams;
    public DictionaryFragmentsAdapter fragmentsAdapter;
    public ViewPager2 viewPager2;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.defaultLocale = Locale.getDefault();
        Utils.statusBarHeight = Utils.getStatusBarHeight(getWindow(), getResources());
        final int nightMode = SettingsHelper.getNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(nightMode);

        setContentView(R.layout.activity_main);

        Utils.adsBox(this);

        Utils.inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        menuHelper = new MenuHelper(this);

        tabLayout = findViewById(R.id.tabs);
        viewPager2 = findViewById(R.id.viewpager);
        fabOptions = findViewById(R.id.fabOptions);
        appBarLayout = findViewById(R.id.appbarLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbarParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        appBarLayout.setPadding(0, Utils.statusBarHeight, 0, 0);

        setSupportActionBar(toolbar);

        final ViewGroup.LayoutParams fabParams = fabOptions.getLayoutParams();
        fabOptions.setLongClickListener(v -> {
            if (fragmentsAdapter == null || viewPager2 == null) return true;
            final DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager2.getCurrentItem());
            if (fragment.isAdded())
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            return true;
        }).setMenuItemSelector((fab, pos) -> {
            final DictionaryFragment fragment = fragmentsAdapter.getItem(viewPager2.getCurrentItem());
            if (pos == 0) { // scroll to top
                fragment.scrollRecyclerView(true);

            } else if (pos == 1) { // scroll to bottom
                fragment.scrollRecyclerView(false);

            } else if (pos == 2) { // filter
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            }
            fabOptions.close();

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

        TooltipCompat.setTooltipText(fabOptions, getString(R.string.options));
        setSearchView();
        handleData();
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new InitializerThread(true, this).start(); // load fragments
        new InitializerThread(false, this).start(); // setup tts
    }

    @Override
    public void loadFragments(final boolean main) {
        if (main) {
            tabBoolsArray = SettingsHelper.getTabs();
            fragmentsAdapter = new DictionaryFragmentsAdapter(this, tabBoolsArray.length);
            for (int i = 0; i < tabBoolsArray.length; i++)
                if (tabBoolsArray[i])
                    fragmentsAdapter.addFragment(getString(tabs[i]));

            if (fragmentsAdapter.isEmpty())
                fragmentsAdapter.setFragments(getString(tabs[0]), getString(tabs[1]), getString(tabs[2]), getString(tabs[3]));

            viewPager2.setOffscreenPageLimit(5);
            if (tabLayout == null) tabLayout = findViewById(R.id.tabs);
            if (tabLayout != null) {
                tabLayout.clearOnTabSelectedListeners();
                viewPager2.setAdapter(fragmentsAdapter);
                new TabLayoutMediator(tabLayout, viewPager2, true, true,
                        (tab, position) -> tab.setText(fragmentsAdapter.getPageTitle(position)))
                        .attach();
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
                                        else Utils.firebaseCrashlytics.recordException(e);
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
        if (requestCode == MaterialSearchView.SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (searchView != null && text != null && text.size() > 0)
                searchView.setQuery(text.get(0), true);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.mAdvance) {
            new AdvancedDialog(this).show();

        } else if (itemId == R.id.mSearch && searchView != null) {
            searchView.open(true, item);
            searchView.bringToFront();

        } else if (itemId == R.id.mSettings) {
            new SettingsDialog(Main.this).show();

        } else {
            menuHelper.show(item);
        }

        return true;
    }

    @Override
    public void afterSearch(final ArrayList<WordItem> result) {
        final boolean searchViewNotNull = searchView != null;
        if (searchViewNotNull && searchView.isShowingProgress()) searchView.hideProgress();

        if (result != null && !result.isEmpty() && searchAdapter != null) {
            final ArrayList<SearchItem> suggestionsList = new ArrayList<>();
            for (final WordItem item : result)
                suggestionsList.add(new SearchItem(item.getWord()));

            searchAdapter.setSuggestionsList(suggestionsList);
            if (searchViewNotNull) searchView.showSuggestions();
        }
    }

    public void onSearch(final String word) {
        try {
            if (fragmentsAdapter != null && viewPager2 != null) {
                final int pagerCurrentItem = viewPager2.getCurrentItem();
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
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
            else Utils.firebaseCrashlytics.recordException(e);
        }
    }

    public void closeExpanded() {
        if (fragmentsAdapter != null) {
            for (int i = 0; i < fragmentsAdapter.getItemCount(); ++i) {
                final DictionaryFragment fragment = fragmentsAdapter.getItem(i);
                if (fragment.isAdded()) fragment.closeExpanded();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentsAdapter != null && viewPager2 != null && fragmentsAdapter.getItemCount() > 0) {
            final DictionaryFragment dictionaryFragment = fragmentsAdapter.getItem(viewPager2.getCurrentItem());
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
        historyDatabase.open();
    }

    @Override
    protected void onPause() {
        historyDatabase.close();
        super.onPause();
    }

    private void historyItemAction(final boolean delete, final String query) {
        if (delete) historyDatabase.clearDatabase(query);
        else historyDatabase.addItem(new SearchItem(query));
        searchAdapter.getFilter().filter("");
    }

    private void setSearchView() {
        searchView = findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.bringToFront();

            historyDatabase = new SearchHistoryTable(this);
            searchAdapter = new SearchAdapter(this, historyDatabase, new SearchAdapterClickListener() {
                private AlertDialog alertDialog;

                @Override
                public void onItemClick(final String text) {
                    onSearch(text);
                    historyItemAction(false, text);
                }

                @Override
                public void onItemLongClick(final String text) {
                    if (alertDialog == null) {
                        final DialogInterface.OnClickListener btnListener = (dialog, which) -> {
                            if (which == DialogInterface.BUTTON_POSITIVE) historyItemAction(true, text);
                            dialog.dismiss();
                        };

                        alertDialog = new AlertDialog.Builder(Main.this)
                                .setTitle(R.string.remove_recent)
                                .setPositiveButton(R.string.yes, btnListener)
                                .setNegativeButton(R.string.no, btnListener).create();
                    }

                    alertDialog.setMessage(getString(R.string.confirm_remove, text));
                    alertDialog.show();

                    final TextView tvMessage = alertDialog.findViewById(android.R.id.message);
                    if (tvMessage != null) {
                        tvMessage.setTypeface(LinkedApp.fontRegular);

                        final CharSequence tvMessageText = tvMessage.getText();
                        if (!Utils.isEmpty(tvMessageText)) {
                            final SpannableStringBuilder messageSpan = new SpannableStringBuilder(tvMessageText);
                            final int spanStart = Utils.indexOfChar(tvMessageText, '"', 0);
                            final int spanEnd = spanStart + text.length() + 1;
                            messageSpan.setSpan(new StyleSpan(Typeface.BOLD), spanStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            messageSpan.delete(spanStart, spanStart + 1);
                            messageSpan.delete(spanEnd - 1, spanEnd);
                            tvMessage.setText(messageSpan);
                        }
                    }
                }
            });
            searchView.setAdapter(searchAdapter);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                private final Handler handler = new Handler();
                private String text = "";
                private SearchAsync prevAsync;
                private final Runnable textWatch = () -> {
                    final SearchAsync async = new SearchAsync(Main.this);
                    Utils.stopAsyncSilent(prevAsync);
                    if (Utils.isEmpty(text)) Utils.stopAsyncSilent(async);
                    else async.execute(text);
                    prevAsync = async;
                };

                @Override
                public boolean onQueryTextSubmit(final String query) {
                    Utils.removeHandlerCallbacksSilent(handler, textWatch);
                    if (historyDatabase != null && !Utils.isEmpty(query)) historyItemAction(false, query);
                    onSearch(query);
                    return query != null || BuildConfig.DEBUG;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {
                    text = newText;
                    if (!Utils.isEmpty(newText)) {
                        Utils.removeHandlerCallbacksSilent(handler, textWatch);
                        handler.postDelayed(textWatch, 800);
                        searchView.showProgress();
                    } else searchView.hideProgress();
                    return true;
                }
            });

            searchView.setOnOpenCloseListener(new MaterialSearchView.OnOpenCloseListener() {
                private DictionaryFragment fragment;
                private int oldScrollFlag = toolbarParams.getScrollFlags();

                @Override
                public void onClose() {
                    if (viewPager2 != null && toolbar != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(viewPager2.getCurrentItem());

                        if (fragment.isAdded() && !fragment.isFilterOpen())
                            fragment.showFilter(false, FilterMethod.DO_NOTHING);
                        toolbarParams.setScrollFlags(oldScrollFlag);
                        toolbar.setLayoutParams(toolbarParams);
                    }
                }

                @Override
                public void onOpen() {
                    if (viewPager2 != null && toolbar != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(viewPager2.getCurrentItem());

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
}