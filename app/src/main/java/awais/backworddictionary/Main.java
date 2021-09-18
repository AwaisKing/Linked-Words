package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.DictionaryFragment.FilterMethod;
import awais.backworddictionary.adapters.DictionaryFragmentsAdapter;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.asyncs.SearchAsyncTask;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.databinding.ActivityMainBinding;
import awais.backworddictionary.dialogs.AdvancedDialog;
import awais.backworddictionary.dialogs.SettingsDialog;
import awais.backworddictionary.helpers.BubbleHelper;
import awais.backworddictionary.helpers.MenuHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.MyApps;
import awais.backworddictionary.interfaces.AdapterClickListener;
import awais.backworddictionary.interfaces.FragmentLoader;
import awais.backworddictionary.interfaces.MainCheck;
import awais.backworddictionary.interfaces.TTSRefresher;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public final class Main extends AppCompatActivity implements FragmentLoader, MainCheck {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final int TTS_DATA_CHECK_CODE = 775;
    static final TTSRefresher ttsRefresher = () -> onTTSInit(TextToSpeech.SUCCESS);
    private static final int[] tabs = {R.string.reverse, R.string.sounds_like, R.string.spelled_like, R.string.synonyms, R.string.antonyms,
            R.string.triggers, R.string.part_of, R.string.comprises, R.string.rhymes, R.string.homophones};
    public static boolean[] tabBoolsArray = {true, true, true, true, false, false, false, false, false, false};
    public static boolean isTTSAsyncRunning;
    public static TextToSpeech tts;
    private boolean isBubbling = false;
    private MenuHelper menuHelper;
    private AlertDialog ttsErrorDialog;
    private SearchAdapter searchAdapter;
    private SettingsDialog settingsDialog;
    private SearchHistoryTable historyDatabase;
    private AppBarLayout.LayoutParams toolbarParams;
    public DictionaryFragmentsAdapter fragmentsAdapter;
    public ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window window = getWindow();
        final Resources resources = getResources();

        Utils.statusBarHeight = Utils.getStatusBarHeight(window, resources);
        Utils.navigationBarHeight = Utils.getNavigationBarHeight(window, resources);
        Utils.inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Utils.defaultLocale = Locale.getDefault();
        final int nightMode = SettingsHelper.getNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(nightMode);

        final View contentView = View.inflate(this, R.layout.activity_main, null);
        mainBinding = ActivityMainBinding.bind(contentView);
        setContentView(contentView);

        final View decorView = window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        if (Build.VERSION.SDK_INT == 19 && Utils.statusBarHeight > 0 && decorView instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) decorView;

            boolean isFound = false;
            View child;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child != null && child.getId() == android.R.id.custom) {
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                final Context context = viewGroup.getContext();
                final View view = new View(context, null, 0);
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Utils.statusBarHeight));
                view.setFocusableInTouchMode(false);
                view.setLongClickable(true);
                view.setFocusable(false);
                view.setClickable(true);
                viewGroup.addView(view);
            }
        }

        menuHelper = new MenuHelper(this);
        Utils.adsBox(this);

        toolbarParams = (AppBarLayout.LayoutParams) mainBinding.toolbar.getLayoutParams();
        mainBinding.appbarLayout.setPadding(0, Utils.statusBarHeight, 0, 0);
        contentView.setPadding(0, 0, 0, Utils.navigationBarHeight);

        setSupportActionBar(mainBinding.toolbar);

        // check for tts before initializing it
        startActivityForResult(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA),
                TTS_DATA_CHECK_CODE);

        final ViewGroup.LayoutParams fabParams = mainBinding.fabOptions.getLayoutParams();
        mainBinding.fabOptions.setLongClickListener(v -> {
            if (fragmentsAdapter == null || mainBinding == null) return true;
            final DictionaryFragment fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());
            if (fragment.isAdded())
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            return true;
        }).setMenuItemSelector((fab, pos) -> {
            final DictionaryFragment fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());
            if (pos == 0) { // scroll to top
                fragment.scrollRecyclerView(true);

            } else if (pos == 1) { // scroll to bottom
                fragment.scrollRecyclerView(false);

            } else if (pos == 2) { // filter
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            }
            mainBinding.fabOptions.close();
        }).setMenuButtonClickListener(v -> {
            if (!mainBinding.fabOptions.isOpened()) {
                fabParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                fabParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                mainBinding.fabOptions.setLayoutParams(fabParams);
            }
            mainBinding.fabOptions.toggle();
        }).setMenuToggleListener(opened -> {
            if (!opened) {
                fabParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                fabParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                mainBinding.fabOptions.setLayoutParams(fabParams);
            }
        });
        TooltipCompat.setTooltipText(mainBinding.fabOptions, getString(R.string.options));

        loadFragments(true);
        setSearchView();
        handleData();
    }

    @Override
    public void loadFragments(final boolean main) {
        if (main) {
            final int[] currentItem = {0};
            final DictionaryFragment item;
            final String prevTitle = mainBinding != null && fragmentsAdapter != null && (currentItem[0] = mainBinding.viewPager.getCurrentItem()) >= 0
                    && fragmentsAdapter.getItemCount() > 0 && currentItem[0] < fragmentsAdapter.getItemCount()
                    && (item = fragmentsAdapter.getItem(currentItem[0])) != null && item.isAdded()
                    && !Utils.isEmpty(item.title) ? item.title : null;

            tabBoolsArray = SettingsHelper.getTabs();
            fragmentsAdapter = new DictionaryFragmentsAdapter(this, tabBoolsArray.length);
            for (int i = 0; i < tabBoolsArray.length; i++)
                if (tabBoolsArray[i])
                    fragmentsAdapter.addFragment(getString(tabs[i]));

            if (fragmentsAdapter.isEmpty())
                fragmentsAdapter.setFragments(getString(tabs[0]), getString(tabs[1]), getString(tabs[2]), getString(tabs[3]));

            mainBinding.viewPager.setOffscreenPageLimit(5);
            mainBinding.viewPager.setAdapter(fragmentsAdapter);

            if (mainBinding != null) {
                mainBinding.tabLayout.clearOnTabSelectedListeners();
                new TabLayoutMediator(mainBinding.tabLayout, mainBinding.viewPager, true, true,
                        (tab, position) -> tab.setText(fragmentsAdapter.getPageTitle(position)))
                        .attach();
                mainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
                                        currentItem.wordsAdapter.updateList(null);
                                        currentItem.startWords(String.valueOf(tab.getText()), prevItem.title);
                                        currentItem.title = prevItem.title;
                                    } catch (final Exception e) {
                                        if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Main::loadFragments::onTabSelected", e);
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

            if (!Utils.isEmpty(prevTitle)) {
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentItem[0] = Math.max(0, Math.min(fragmentsAdapter.getItemCount() - 1, currentItem[0]));
                        mainBinding.viewPager.setCurrentItem(currentItem[0], false);
                        final DictionaryFragment fragment = fragmentsAdapter.getItem(currentItem[0]);

                        if (fragment != null && fragment.isAdded())
                            onSearch(prevTitle);
                        handler.removeCallbacks(this);
                    }
                }, 800);
            }

        } else if (!isBubbling) {
            finish();
            startActivity(new Intent(getBaseContext().getApplicationContext(), Main.class));
        } else
            loadFragments(true);
    }

    private void handleData() {
        final Intent intent = getIntent();
        final String data;

        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) &&
                !Utils.isEmpty(data = intent.hasExtra(Intent.EXTRA_TEXT) ?
                        intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getDataString())) {

            if (!isBubbling)
                isBubbling = intent.hasExtra(BubbleHelper.INTENT_EXTRA_BUBBLING) &&
                        intent.getBooleanExtra(BubbleHelper.INTENT_EXTRA_BUBBLING, false);

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.removeCallbacks(this);
                    if (mainBinding != null) {
                        mainBinding.searchView.setHandlingIntentData(true);
                        mainBinding.searchView.setQuery(data, true);
                        mainBinding.searchView.setHandlingIntentData(false);
                    }
                }
            }, 300);
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
        if (requestCode == TTS_DATA_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (ttsErrorDialog != null && ttsErrorDialog.isShowing()) {
                    ttsErrorDialog.cancel();
                    ttsErrorDialog = null;
                }
                ttsErrorDialog = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme).create();
                ttsErrorDialog.setTitle(R.string.tts_not_initialized);
                ttsErrorDialog.setMessage(getString(R.string.tts_data_not_found));
                ttsErrorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                        (DialogInterface.OnClickListener) null);
                ttsErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), (d, w) -> {
                    ttsErrorDialog = null;
                    startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
                });
                ttsErrorDialog.show();
            } else if (tts == null)
                tts = new TextToSpeech(getApplicationContext(), Main::onTTSInit);

        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SettingsDialog.TTS_SETTINGS_REQUEST_CODE && settingsDialog != null && settingsDialog.isShowing()) {
                settingsDialog.dismiss();

            } else if (requestCode == MaterialSearchView.SPEECH_REQUEST_CODE && data != null) {
                final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (mainBinding != null && text != null && text.size() > 0)
                    mainBinding.searchView.setQuery(text.get(0), true);
            }
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

        } else if (itemId == R.id.mSearch && mainBinding != null) {
            mainBinding.searchView.open(true, item);
            mainBinding.searchView.bringToFront();

        } else if (itemId == R.id.mSettings) {
            (settingsDialog = new SettingsDialog(Main.this, ttsErrorDialog)).show();

        } else {
            menuHelper.show(item);
        }

        return true;
    }

    @Override
    public void afterSearch(final ArrayList<WordItem> result) {
        final boolean bindingNotNull = mainBinding != null;
        if (bindingNotNull && mainBinding.searchView.isShowingProgress())
            mainBinding.searchView.hideProgress();

        if (result != null && !result.isEmpty() && searchAdapter != null) {
            final ArrayList<SearchItem> suggestionsList = new ArrayList<>(0);
            for (final WordItem item : result)
                suggestionsList.add(new SearchItem(item.getWord()));

            searchAdapter.setSuggestionsList(suggestionsList);
            if (bindingNotNull) mainBinding.searchView.showSuggestions();
        }
    }

    public void onSearch(final String word) {
        try {
            if (fragmentsAdapter != null && mainBinding != null) {
                final int pagerCurrentItem = mainBinding.viewPager.getCurrentItem();
                final DictionaryFragment fragment = fragmentsAdapter.getItem(pagerCurrentItem);
                if (fragment != null && fragment.isAdded()) {
                    final String method = fragmentsAdapter.getPageTitle(pagerCurrentItem);
                    fragment.startWords(method, word);
                }
            }

            if (mainBinding != null && mainBinding.searchView.isSearchOpen()) {
                mainBinding.searchView.close(false);
                toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                mainBinding.toolbar.setLayoutParams(toolbarParams);
            }
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Main::onSearch", e);
            else Utils.firebaseCrashlytics.recordException(e);
        }
    }

    public void closeExpanded() {
        if (fragmentsAdapter != null) {
            for (int i = 0; i < fragmentsAdapter.getItemCount(); ++i) {
                final DictionaryFragment fragment = fragmentsAdapter.getItem(i);
                if (fragment != null && fragment.isAdded()) fragment.closeExpanded();
            }
        }
    }

    @Override
    public void onBackPressed() {
        final DictionaryFragment fragment;
        if (fragmentsAdapter != null && mainBinding != null && fragmentsAdapter.getItemCount() > 0
                && (fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem())) != null
                && fragment.isAdded() && fragment.isFilterOpen() && fragment.hideFilter())
            return;

        if (isBubbling)
            super.onBackPressed();
        else MyApps.showAlertDialog(this, (parent, view, position, id) -> {
            if (id == -1 && position == -1 && parent == null) super.onBackPressed();
            else MyApps.openAppStore(this, position);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        historyDatabase.open();
        final Intent intent;
        if (!isBubbling && (intent = getIntent()) != null)
            isBubbling = intent.hasExtra(BubbleHelper.INTENT_EXTRA_BUBBLING) &&
                    intent.getBooleanExtra(BubbleHelper.INTENT_EXTRA_BUBBLING, false);
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
        if (mainBinding != null) {
            mainBinding.searchView.bringToFront();

            historyDatabase = new SearchHistoryTable(this);
            mainBinding.searchView.setAdapter(searchAdapter = new SearchAdapter(historyDatabase, new AdapterClickListener() {
                private AlertDialog alertDialog;

                @Override
                public void onClick(final View view) {
                    final Object tag = view.getTag();
                    if (tag instanceof SearchItem) {
                        final String word = ((SearchItem) tag).getText();
                        onSearch(word);
                        historyItemAction(false, word);
                    }
                }

                @Override
                public boolean onLongClick(final View view) {
                    if (view == null) return super.onLongClick(null);

                    final Object tag = view.getTag();
                    if (tag instanceof SearchItem) {
                        final SearchItem searchItem = (SearchItem) tag;
                        if (searchItem.isSearchable()) return true;

                        final String word = searchItem.getText();

                        if (alertDialog == null) {
                            final DialogInterface.OnClickListener btnListener = (dialog, which) -> {
                                if (which == DialogInterface.BUTTON_POSITIVE) historyItemAction(true, word);
                                dialog.dismiss();
                            };

                            alertDialog = new MaterialAlertDialogBuilder(Main.this, R.style.MaterialAlertDialogTheme)
                                    .setTitle(R.string.remove_recent)
                                    .setPositiveButton(R.string.yes, btnListener)
                                    .setNegativeButton(R.string.no, btnListener).create();
                        }

                        alertDialog.setMessage(getString(R.string.confirm_remove, word));
                        alertDialog.show();

                        final TextView tvMessage = alertDialog.findViewById(android.R.id.message);
                        if (tvMessage != null) {
                            if (tvMessage.getTypeface() != LinkedApp.fontRegular)
                                tvMessage.setTypeface(LinkedApp.fontRegular);

                            final CharSequence tvMessageText = tvMessage.getText();
                            if (!Utils.isEmpty(tvMessageText)) {
                                final SpannableStringBuilder messageSpan = new SpannableStringBuilder(tvMessageText);
                                final int spanStart = Utils.indexOfChar(tvMessageText, '"', 0);
                                final int spanEnd = spanStart + word.length() + 1;
                                messageSpan.setSpan(new StyleSpan(Typeface.BOLD), spanStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                messageSpan.delete(spanStart, spanStart + 1);
                                messageSpan.delete(spanEnd - 1, spanEnd);
                                tvMessage.setText(messageSpan);
                            }
                        }
                    }

                    return true;
                }
            }));
            mainBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                private String text = "";
                private SearchAsyncTask prevAsync;
                private final Handler handler = new Handler(Looper.getMainLooper());
                private final Runnable textWatch = () -> {
                    final SearchAsyncTask async = new SearchAsyncTask(Main.this);
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
                    return query != null;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {
                    text = newText;
                    if (!Utils.isEmpty(newText)) {
                        Utils.removeHandlerCallbacksSilent(handler, textWatch);
                        handler.postDelayed(textWatch, 800);
                        mainBinding.searchView.showProgress();
                    } else mainBinding.searchView.hideProgress();
                    return true;
                }
            });

            mainBinding.searchView.setOnOpenCloseListener(new MaterialSearchView.OnOpenCloseListener() {
                private DictionaryFragment fragment;
                private int oldScrollFlag = toolbarParams.getScrollFlags();

                @Override
                public void onClose() {
                    if (mainBinding != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());

                        if (fragment.isAdded() && !fragment.isFilterOpen())
                            fragment.showFilter(false, FilterMethod.DO_NOTHING);
                        toolbarParams.setScrollFlags(oldScrollFlag);
                        mainBinding.toolbar.setLayoutParams(toolbarParams);
                    }
                }

                @Override
                public void onOpen() {
                    if (mainBinding != null && fragmentsAdapter != null) {
                        fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());

                        if (fragment.isAdded())
                            fragment.showFilter(true, FilterMethod.DO_NOTHING);
                        mainBinding.appbarLayout.setExpanded(true, true);
                        oldScrollFlag = toolbarParams.getScrollFlags();
                        toolbarParams.setScrollFlags(0);
                        mainBinding.toolbar.setLayoutParams(toolbarParams);
                    }
                }
            });
        }
    }

    public static synchronized void onTTSInit(final int initStatus) {
        if (tts != null && initStatus == TextToSpeech.SUCCESS) {
            // set speech rate and pitch
            {
                tts.setSpeechRate(SettingsHelper.getTTSSpeechRate() / 100f);
                tts.setPitch(SettingsHelper.getTTSPitch() / 100f);
            }

            // set engine and voice
            if (isTTSAsyncRunning) return;

            isTTSAsyncRunning = true;

            final String defaultEngine = tts.getDefaultEngine();
            final Locale defaultLanguage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? tts.getDefaultLanguage() : null;
            Voice defaultVoice;
            try {
                defaultVoice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? tts.getDefaultVoice() : null;
            } catch (final Exception e) {
                defaultVoice = null;
            }

            String engine = defaultEngine;
            Locale language = tts.getLanguage();
            Voice voice;
            try {
                voice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? tts.getVoice() : null;
            } catch (final Exception e) {
                voice = null;
            }

            final String ttsEngine = SettingsHelper.getTTSEngine();
            final String ttsVoice = SettingsHelper.getTTSVoice();
            final String ttsLanguage = SettingsHelper.getTTSLanguage();

            String tempStr;

            final boolean setEngine = !Utils.isEmpty(ttsEngine) && !ttsEngine.equals(defaultEngine);
            final boolean setLanguage = !Utils.isEmpty(ttsLanguage) && !Utils.isEmpty(tempStr = language != null
                    ? language.getDisplayLanguage() : defaultLanguage != null ? defaultLanguage.getDisplayLanguage() : null)
                    && !ttsLanguage.equals(tempStr);
            final boolean setVoice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    !Utils.isEmpty(ttsVoice) && !Utils.isEmpty(tempStr = voice != null
                    ? voice.getName() : defaultVoice != null ? defaultVoice.getName() : null) && !tempStr.equals(ttsVoice);

            if (setEngine) engine = ttsEngine;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && setLanguage) {
                final List<Locale> languages = new ArrayList<>(0);
                final List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
                Collection<Locale> localeCollection;
                try {
                    localeCollection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? tts.getAvailableLanguages() :
                            locales;
                } catch (final Exception e) {
                    localeCollection = locales;
                }
                languages.addAll(localeCollection);

                for (final Locale intLanguage : languages) {
                    if (intLanguage != null && intLanguage.getDisplayLanguage().equals(ttsLanguage)) {
                        language = intLanguage;
                        break;
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && setVoice) {
                for (final Voice intVoice : tts.getVoices()) {
                    if (intVoice != null && TextUtils.equals(intVoice.getName(), ttsVoice)) {
                        voice = intVoice;
                        break;
                    }
                }
            }

            //noinspection deprecation
            tts.setEngineByPackageName(engine);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) tts.setLanguage(language);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voice != null) tts.setVoice(voice);

            isTTSAsyncRunning = false;
        }
    }
}