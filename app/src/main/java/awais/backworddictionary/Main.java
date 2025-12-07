package awais.backworddictionary;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Locale;

import awais.backworddictionary.adapters.DictionaryFragmentsAdapter;
import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.custom.SearchHistoryTable;
import awais.backworddictionary.databinding.ActivityMainBinding;
import awais.backworddictionary.dialogs.AdvancedDialog;
import awais.backworddictionary.dialogs.SettingsDialog;
import awais.backworddictionary.executors.SearchAsyncTask;
import awais.backworddictionary.helpers.BubbleHelper;
import awais.backworddictionary.helpers.MenuHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.other.MyApps;
import awais.backworddictionary.interfaces.AdapterClickListener;
import awais.backworddictionary.models.FilterMethod;
import awais.backworddictionary.models.Tab;
import awais.backworddictionary.models.WordItem;
import awais.lapism.MaterialSearchView;
import awais.lapism.SearchItem;

public final class Main extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

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

        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        Utils.defaultLocale = Locale.getDefault();

        menuHelper = new MenuHelper(this);
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this), null, false);

        final var bindingRoot = mainBinding.getRoot();
        final var appBarLayout = mainBinding.appbarLayout;
        final var toolbar = mainBinding.toolbar;
        final var fabOptions = mainBinding.fabOptions;
        final var searchView = mainBinding.searchView;

        setContentView(bindingRoot);

        Utils.adsBox(this);

        toolbarParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        setSupportActionBar(toolbar);

        // check for tts before initializing it
        try {
            ActivityCompat.startActivityForResult(this, new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA),
                                                  TTSHelper.TTS_DATA_CHECK_CODE, null);
        } catch (final Throwable e) {
            // tts check activity not found
            Toast.makeText(Main.this, R.string.tts_act_not_found, Toast.LENGTH_SHORT).show();
        }

        final ViewGroup.LayoutParams fabParams = fabOptions.getLayoutParams();
        fabOptions.setLongClickListener(v -> {
            if (fragmentsAdapter == null || mainBinding == null) return true;
            final DictionaryFragment fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());
            if (fragment.isAdded()) {
                if (fragment.isFilterOpen()) fragment.hideFilter();
                else fragment.showFilter(true, FilterMethod.RECYCLER_PADDING);
            }
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
            fabOptions.close();
        }).setMenuButtonClickListener(v -> {
            if (!fabOptions.isOpened()) {
                fabParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                fabParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                fabOptions.setLayoutParams(fabParams);
            }
            fabOptions.toggle();
        }).setMenuToggleListener(opened -> {
            if (opened) return;
            fabParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            fabParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            fabOptions.setLayoutParams(fabParams);
        });
        TooltipCompat.setTooltipText(fabOptions, getString(R.string.options));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 696969);

        loadFragments(true);
        setSearchView();
        handleData();

        ViewCompat.setOnApplyWindowInsetsListener(bindingRoot, (v, insets) -> {
            var sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            var gestureInsets = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures() | WindowInsetsCompat.Type.systemGestures());

            final int insetTop = Math.max(sysInsets.top, gestureInsets.top);
            final int insetLeft = Math.min(sysInsets.left, gestureInsets.left);
            final int insetRight = Math.min(sysInsets.right, gestureInsets.right);

            appBarLayout.setPadding(insetLeft, insetTop, insetRight, 0);

            final ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
                marginLayoutParams.setMargins(insetLeft, insetTop, insetRight, 0);
                searchView.setLayoutParams(layoutParams);
            }

            return insets;
        });

        appBarLayout.removeOnOffsetChangedListener(this);
        appBarLayout.addOnOffsetChangedListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            private final Runnable backPressRunnable = () -> {
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                try {
                    setEnabled(true);
                } catch (Exception e) {
                    finishAffinity();
                }
            };

            @Override
            public void handleOnBackPressed() {
                if (mainBinding != null) {
                    if (mainBinding.searchView.isSearchOpen()) {
                        mainBinding.searchView.close(true);
                        return;
                    }

                    final DictionaryFragment fragment;
                    if (fragmentsAdapter != null && fragmentsAdapter.getItemCount() > 0
                        && (fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem())) != null
                        && fragment.isAdded() && fragment.isFilterOpen() && fragment.hideFilter()) {
                        return;
                    }
                }

                if (isBubbling) {
                    backPressRunnable.run();
                    return;
                }

                MyApps.showAlertDialog(Main.this, (parent, view, position, id) -> {
                    if (id == -1 && position == -1 && parent == null) backPressRunnable.run();
                    else MyApps.openAppStore(Main.this, position);
                });
            }
        });
    }

    public void loadFragments(final boolean main) {
        if (main) {
            final int[] currentItem = {0};
            final ActivityMainBinding mainBinding = this.mainBinding;

            final DictionaryFragment item;
            final String prevTitle = mainBinding != null && fragmentsAdapter != null
                                     && (currentItem[0] = mainBinding.viewPager.getCurrentItem()) >= 0
                                     && fragmentsAdapter.getItemCount() > 0 && currentItem[0] < fragmentsAdapter.getItemCount()
                                     && (item = fragmentsAdapter.getItem(currentItem[0])) != null && item.isAdded()
                                     && !Utils.isEmpty(item.title) ? item.title : null;

            final Tab[] tabs = Tab.values();
            final boolean[] tabBools = SettingsHelper.getInstance(this).getTabs();
            fragmentsAdapter = new DictionaryFragmentsAdapter(this);
            for (int i = 0; i < Math.min(tabBools.length, tabs.length); i++) {
                tabs[i] = tabs[i].setEnabled(tabBools[i]);
                if (tabs[i].isEnabled()) fragmentsAdapter.addFragment(tabs[i]);
            }

            if (fragmentsAdapter.getItemCount() <= 0) fragmentsAdapter.setFragments(tabs[0], tabs[1], tabs[2], tabs[3]);

            if (mainBinding != null) {
                mainBinding.viewPager.setOffscreenPageLimit(5);
                mainBinding.viewPager.setAdapter(fragmentsAdapter);

                mainBinding.tabLayout.clearOnTabSelectedListeners();
                new TabLayoutMediator(mainBinding.tabLayout, mainBinding.viewPager, true, true,
                                      (tab, position) -> tab.setText(getString(fragmentsAdapter.getPageTitle(position)))).attach();
                mainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    private int prevTab = 0;

                    @Override
                    public void onTabSelected(final TabLayout.Tab tab) {
                        setTitle(R.string.app_name);
                        if (fragmentsAdapter == null) return;

                        final DictionaryFragment currentItem = fragmentsAdapter.getItem(tab.getPosition());
                        if (!currentItem.isAdded()) return;

                        final DictionaryFragment prevItem = fragmentsAdapter.getItem(prevTab);

                        if (!Utils.isEmpty(currentItem.title)) setTitle(currentItem.title);
                        else if (!Utils.isEmpty(prevItem.title)) { /// was ( prevItem.title != null && !prevItem.title.isEmpty() )
                            try {
                                currentItem.wordsAdapter.updateList(null);
                                // currentItem.startWords(String.valueOf(tab.getText()), prevItem.title);
                                currentItem.startWords(currentItem.getTab().getTabName(), prevItem.title);
                                currentItem.title = prevItem.title;
                            } catch (final Exception e) {
                                if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Main::loadFragments::onTabSelected", e);
                            }
                        }

                        if (prevItem.isAdded()) prevItem.showFilter(prevItem.isFilterOpen(), FilterMethod.RECYCLER_NO_PADDING);
                        currentItem.showFilter(currentItem.isFilterOpen(), currentItem.isFilterOpen() ? FilterMethod.RECYCLER_NO_PADDING :
                                                                           FilterMethod.RECYCLER_PADDING);
                    }

                    @Override
                    public void onTabUnselected(final TabLayout.Tab tab) {prevTab = tab.getPosition();}

                    @Override
                    public void onTabReselected(final TabLayout.Tab tab) {}
                });
            }

            if (Utils.isEmpty(prevTitle)) return;

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

            return;
        }

        if (isBubbling) loadFragments(true);
        else {
            finishAndRemoveTask();
            startActivity(new Intent(getBaseContext().getApplicationContext(), Main.class).addFlags(
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void handleData() {
        final Intent intent = getIntent();
        if (intent == null || !Intent.ACTION_VIEW.equals(intent.getAction())) return;

        final String data;
        if (Utils.isEmpty(data = intent.hasExtra(Intent.EXTRA_TEXT) ? intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getDataString())) return;

        if (!isBubbling) isBubbling = intent.hasExtra(BubbleHelper.INTENT_EXTRA_BUBBLING)
                                      && intent.getBooleanExtra(BubbleHelper.INTENT_EXTRA_BUBBLING, false);

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

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleData();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TTSHelper.TTS_DATA_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (ttsErrorDialog != null && ttsErrorDialog.isShowing()) {
                    ttsErrorDialog.cancel();
                    ttsErrorDialog = null;
                }
                ttsErrorDialog = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme).create();
                ttsErrorDialog.setTitle(R.string.tts_not_initialized);
                ttsErrorDialog.setMessage(getString(R.string.tts_data_not_found));
                ttsErrorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (DialogInterface.OnClickListener) null);
                ttsErrorDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.dont_show_again), (d, w) -> {
                    SettingsHelper.getInstance(this).setTTSErrorDialogHidden();
                    ttsErrorDialog.cancel();
                    ttsErrorDialog = null;
                });
                ttsErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), (d, w) -> {
                    ttsErrorDialog = null;
                    startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
                });
                ttsErrorDialog.show();
            } else if (TTSHelper.tts == null) {
                TTSHelper.tts = new TextToSpeech(getApplicationContext(), TTSHelper::onTTSInit);
            }
            return;
        }

        if (requestCode == TTSHelper.TTS_SETTINGS_REQUEST_CODE) {
            TTSHelper.onTTSInit(resultCode == Activity.RESULT_OK ? TextToSpeech.SUCCESS : TextToSpeech.ERROR);
            if (settingsDialog != null && settingsDialog.isShowing()) settingsDialog.dismiss();
            return;
        }

        if (requestCode == MaterialSearchView.SPEECH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (mainBinding != null && text != null && !text.isEmpty())
                    mainBinding.searchView.setQuery(text.get(0), true);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
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
            settingsDialog = new SettingsDialog(Main.this, ttsErrorDialog);
            settingsDialog.show();

        } else {
            menuHelper.show(item);
        }
        return true;
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        if (mainBinding == null) return;
        float percentage = (float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange();
        percentage = 1.0f - (Float.isNaN(percentage) ? 0.0f : Math.abs(percentage));
        mainBinding.toolbar.setAlpha(percentage);
    }

    public void afterSearch(final ArrayList<WordItem> result) {
        final boolean bindingNotNull = mainBinding != null;
        if (bindingNotNull && mainBinding.searchView.isShowingProgress())
            mainBinding.searchView.hideProgress();

        if (result == null || result.isEmpty() || searchAdapter == null) return;

        final ArrayList<SearchItem> suggestionsList = new ArrayList<>();
        for (final WordItem item : result)
            suggestionsList.add(new SearchItem(item.getWord()));

        searchAdapter.setSuggestionsList(suggestionsList);
        if (bindingNotNull) mainBinding.searchView.showSuggestions();
    }

    public void onSearch(final String word) {
        final ActivityMainBinding mainBinding = this.mainBinding;
        if (mainBinding == null) return;
        try {
            final DictionaryFragmentsAdapter fragmentsAdapter = this.fragmentsAdapter;
            if (fragmentsAdapter != null) {
                final int pagerCurrentItem = mainBinding.viewPager.getCurrentItem();
                final DictionaryFragment fragment = fragmentsAdapter.getItem(pagerCurrentItem);
                if (fragment != null && fragment.isAdded()) {
                    final int method = fragmentsAdapter.getPageTitle(pagerCurrentItem);
                    fragment.startWords(method, word);
                }
            }

            if (mainBinding.searchView.isSearchOpen()) {
                mainBinding.searchView.close(false);
                toolbarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                                             AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                mainBinding.toolbar.setLayoutParams(toolbarParams);
            }
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "Main::onSearch", e);
        }
    }

    public void closeExpanded() {
        if (fragmentsAdapter == null) return;
        DictionaryFragment fragment;
        for (int i = 0; i < fragmentsAdapter.getItemCount(); ++i) {
            fragment = fragmentsAdapter.getItem(i);
            if (fragment != null && (fragment.isAdded() || fragment.isResumed())) fragment.closeExpanded();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (false) ;// historyDatabase.open();
        final Intent intent = isBubbling ? null : getIntent();
        if (intent != null) isBubbling = intent.hasExtra(BubbleHelper.INTENT_EXTRA_BUBBLING) &&
                                         intent.getBooleanExtra(BubbleHelper.INTENT_EXTRA_BUBBLING, false);
    }

    @Override
    protected void onPause() {
        try {
            historyDatabase.close();
        } catch (Exception e) {
            // ignore
        }
        super.onPause();
    }

    private void historyItemAction(final boolean delete, final String query) {
        if (delete) historyDatabase.clearDatabase(query);
        else historyDatabase.addItem(new SearchItem(query));
        searchAdapter.getFilter().filter("");
    }

    private void setSearchView() {
        final MaterialSearchView searchView = mainBinding == null ? null : mainBinding.searchView;
        if (searchView == null) return;
        searchView.bringToFront();

        historyDatabase = new SearchHistoryTable(this);
        searchView.setAdapter(searchAdapter = new SearchAdapter(historyDatabase, new AdapterClickListener() {
            private AlertDialog alertDialog;

            @Override
            public void onClick(final View view) {
                if (!(view.getTag() instanceof final SearchItem searchItem)) return;
                final String word = searchItem.getText();
                onSearch(word);
                historyItemAction(false, word);
            }

            @Override
            public boolean onLongClick(final View view) {
                if (view == null) return super.onLongClick(null);

                final Object tag = view.getTag();
                if (tag instanceof final SearchItem searchItem && !searchItem.isSearchable()) {
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                if (mainBinding == null || fragmentsAdapter == null) return;
                fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());

                if (fragment.isAdded() && !fragment.isFilterOpen())
                    fragment.showFilter(false, FilterMethod.DO_NOTHING);
                toolbarParams.setScrollFlags(oldScrollFlag);
                mainBinding.toolbar.setLayoutParams(toolbarParams);
            }

            @Override
            public void onOpen() {
                if (mainBinding == null || fragmentsAdapter == null) return;
                fragment = fragmentsAdapter.getItem(mainBinding.viewPager.getCurrentItem());

                if (fragment.isAdded())
                    fragment.showFilter(true, FilterMethod.DO_NOTHING);
                mainBinding.appbarLayout.setExpanded(true, true);
                oldScrollFlag = toolbarParams.getScrollFlags();
                toolbarParams.setScrollFlags(0);
                mainBinding.toolbar.setLayoutParams(toolbarParams);
            }
        });
    }
}