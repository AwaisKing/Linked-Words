package awais.backworddictionary;

import static android.speech.tts.TextToSpeech.EngineInfo;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Preconditions;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.adapters.TTSItemsAdapter;
import awais.backworddictionary.executor.LocalAsyncTask;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.NumberPickerProgressListener;
import awais.sephiroth.numberpicker.HorizontalNumberPicker;

public final class TTSActivity extends AppCompatActivity {
    private static final Comparator<TTSItemsAdapter.TTSItemHolder<Voice>> VOICES_NAME_COMPARATOR = (voice1, voice2) -> {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voice1 != null && voice2 != null && !voice1.equals(voice2)
                && voice1.object != null && voice2.object != null && !voice1.object.equals(voice2.object)) {
            int compare = voice1.object.getLocale().getDisplayName()
                    .compareTo(voice2.object.getLocale().getDisplayName());
            if (compare == 0) compare = Integer.compare(voice2.object.getLatency(),
                    voice1.object.getLatency());
            if (compare == 0) compare = Integer.compare(voice2.object.getQuality(),
                    voice1.object.getQuality());
            return compare;
        }
        return 0;
    };
    private static final Comparator<TTSItemsAdapter.TTSItemHolder<Voice>> VOICES_INTERNET_COMPARATOR = (voice1, voice2) -> {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voice1 != null && voice2 != null &&
                !voice1.equals(voice2) && voice1.object != null && voice2.object != null && !voice1.object.equals(voice2.object)) {
            final boolean v1Network = voice1.object.isNetworkConnectionRequired();
            final boolean v2Network = voice2.object.isNetworkConnectionRequired();
            int compare = v1Network ? v2Network ? 0 : 1 : !v2Network ? 0 : -1;

            int nameCompare = 0;
            if (compare != 0) {
                final String displayName1 = voice1.object.getLocale().getDisplayName();
                final String displayName2 = voice2.object.getLocale().getDisplayName();
                nameCompare = displayName2.compareTo(displayName1);
            }

            /*
            helps with debugging comparison:
                        Log.d("AWAISKING_APP", "comparing: " + voice1.object.getLocale().getDisplayName()
                                + " - " + voice2.object.getLocale().getDisplayName()
                                + " ---- " + compare
                                + " -- " + nameCompare
                                + " -- " + Integer.compare(compare, nameCompare)
                        );
            */

            compare = Integer.compare(compare, nameCompare);

            return compare;
        }
        return 0;
    };
    private static final String DEMO_VOICE_MESSAGE = "Example voice for ";
    private static final int FALLBACK_TTS_DEFAULT_PITCH = 100;
    private static final int FALLBACK_TTS_DEFAULT_RATE = 100;
    private static TextToSpeech testingTTS;

    private RecyclerView rvEngines, rvLanguages, rvVoices;
    private View btnPlayTest, btnSaveSettings, rvEnginesParent, rvLanguagesParent, rvVoicesParent;
    private HorizontalNumberPicker pickerPitch, pickerSpeechRate;

    private int defaultPitch = FALLBACK_TTS_DEFAULT_PITCH, defaultRate = FALLBACK_TTS_DEFAULT_RATE;
    private String defaultEngine;
    private EngineInfo selectedEngine;
    private Locale defaultLanguage, selectedLanguage;
    private Voice defaultVoice, selectedVoice;

    private TTSItemsAdapter<Voice> voicesAdapter;
    private TTSItemsAdapter<Locale> languagesAdapter;
    private TTSItemsAdapter<EngineInfo> enginesAdapter;

    private boolean isEngineSelected, isLanguageSelected, isVoiceSelected;

    private final ArrayList<TTSItemsAdapter.TTSItemHolder<EngineInfo>> enginesList = new ArrayList<>(0);
    private final ArrayList<TTSItemsAdapter.TTSItemHolder<Locale>> languagesList = new ArrayList<>(0);
    private final ArrayList<TTSItemsAdapter.TTSItemHolder<Voice>> voicesList = new ArrayList<>(0);

    @SuppressLint("NotifyDataSetChanged")
    private final View.OnClickListener onClickListener = v -> {
        final Object tag = v.getTag();

        if (v == btnSaveSettings) {

            final int pitch = pickerPitch.getProgress();
            final int speechRate = pickerSpeechRate.getProgress();

            final boolean saveEngine = (rvEngines == null || rvEngines.getVisibility() == View.VISIBLE)
                    && (rvEngines == null || rvEngines.getVisibility() == View.VISIBLE);
            final boolean saveLang = (rvLanguagesParent == null || rvLanguagesParent.getVisibility() == View.VISIBLE)
                    && (rvLanguages == null || rvLanguages.getVisibility() == View.VISIBLE);
            final boolean saveVoice = (rvVoicesParent == null || rvVoicesParent.getVisibility() == View.VISIBLE)
                    && (rvVoices == null || rvVoices.getVisibility() == View.VISIBLE);

            SettingsHelper.setTTSPitch(pitch == defaultPitch ? null : pitch);
            SettingsHelper.setTTSSpeechRate(speechRate == defaultRate ? null : speechRate);

            if (saveEngine) {
                String engineName = null;
                for (final TTSItemsAdapter.TTSItemHolder<EngineInfo> itemHolder : enginesList) {
                    if (itemHolder.selected) {
                        engineName = itemHolder.object.name;
                        break;
                    }
                }

                if (!Utils.isEmpty(defaultEngine) && Utils.isEmpty(engineName))
                    engineName = defaultEngine;
                SettingsHelper.setTTSEngine(engineName);
            }

            if (saveLang) {
                String displayLanguage = null;
                for (final TTSItemsAdapter.TTSItemHolder<Locale> itemHolder : languagesList) {
                    if (itemHolder.selected) {
                        displayLanguage = itemHolder.object.getDisplayLanguage();
                        break;
                    }
                }

                if (defaultLanguage != null && Utils.isEmpty(displayLanguage))
                    displayLanguage = defaultLanguage.getDisplayLanguage();

                SettingsHelper.setTTSLanguage(displayLanguage);
            }

            if (saveVoice && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String voiceName = null;
                for (final TTSItemsAdapter.TTSItemHolder<Voice> itemHolder : voicesList) {
                    if (itemHolder.selected) {
                        voiceName = itemHolder.object.getName();
                        break;
                    }
                }

                if (selectedVoice != defaultVoice && !ObjectsCompat.equals(defaultVoice, selectedVoice)
                        && !selectedVoice.getName().equalsIgnoreCase(defaultVoice.getName()))
                    SettingsHelper.setTTSVoice(voiceName);
            }

            Main.ttsRefresher.doRefresh();

            finishResult(RESULT_OK);

        } else if (v == btnPlayTest) {
            Locale language = null;

            Locale locale;
            try {
                locale = VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? testingTTS.getVoice().getLocale() : null;
            } catch (final Exception e) {
                locale = null;
            }

            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && locale != null) language = locale;
            else {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) language = testingTTS.getDefaultLanguage();
                if (selectedLanguage != null && !(selectedLanguage.equals(language) && selectedLanguage.getDisplayLanguage()
                        .equals(language.getDisplayLanguage()))) language = selectedLanguage;
                else {
                    final Locale ttsLanguage = testingTTS.getLanguage();
                    if (language == null) language = ttsLanguage;
                    else if (ttsLanguage != language && !language.equals(ttsLanguage) ||
                            !language.getDisplayLanguage().equals(ttsLanguage.getDisplayLanguage())) language = ttsLanguage;
                }
            }

            final String demoVoiceMessage = DEMO_VOICE_MESSAGE.concat(language.getDisplayName());

            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                testingTTS.speak(demoVoiceMessage, QUEUE_FLUSH, null, null);
            else testingTTS.speak(demoVoiceMessage, QUEUE_FLUSH, null);

        } else if (tag instanceof TTSItemsAdapter.TTSItemHolder) {
            if (testingTTS.isSpeaking()) testingTTS.stop();

            final TTSItemsAdapter.TTSItemHolder<?> itemHolder = (TTSItemsAdapter.TTSItemHolder<?>) tag;

            final boolean isEngineInfo = itemHolder.object instanceof EngineInfo;
            final boolean isLocale = itemHolder.object instanceof Locale;
            final boolean isVoice = VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && itemHolder.object instanceof Voice;

            Boolean changed = null;

            final List<?> listToChange = isEngineInfo ? enginesList : isLocale ? languagesList : isVoice ? voicesList : null;
            if (listToChange != null) {
                if (listToChange.size() > 0) for (final Object ttsItemHolder : listToChange)
                    if (ttsItemHolder instanceof TTSItemsAdapter.TTSItemHolder && ((TTSItemsAdapter.TTSItemHolder<?>) ttsItemHolder).selected) {
                        final TTSItemsAdapter.TTSItemHolder<?> changeableTTSItemHolder = (TTSItemsAdapter.TTSItemHolder<?>) ttsItemHolder;

                        // get index before modifying
                        final int indexOf = listToChange.indexOf(changeableTTSItemHolder);

                        // modify
                        changeableTTSItemHolder.selected = false;

                        // notify
                        if (indexOf >= 0) {
                            if (listToChange == enginesList && enginesAdapter != null)
                                enginesAdapter.notifyItemChanged(indexOf, changeableTTSItemHolder);
                            else if (listToChange == languagesList && languagesAdapter != null)
                                languagesAdapter.notifyItemChanged(indexOf, changeableTTSItemHolder);
                            else if (listToChange == voicesList && voicesAdapter != null)
                                voicesAdapter.notifyItemChanged(indexOf, changeableTTSItemHolder);
                        }
                        break;
                    }

                changed = Boolean.FALSE;

                final int indexOf = listToChange.indexOf(itemHolder);

                if (itemHolder.object instanceof EngineInfo) selectedEngine = (EngineInfo) itemHolder.object;
                else if (itemHolder.object instanceof Locale) selectedLanguage = (Locale) itemHolder.object;
                else selectedVoice = (Voice) itemHolder.object;

                itemHolder.selected = true;
                if (indexOf >= 0) {
                    changed = Boolean.TRUE;
                    // notify
                    if (listToChange == enginesList && enginesAdapter != null) {
                        enginesAdapter.notifyItemChanged(indexOf, itemHolder);
                    } else if (listToChange == languagesList && languagesAdapter != null) {
                        languagesAdapter.notifyItemChanged(indexOf, itemHolder);
                    } else if (listToChange == voicesList && voicesAdapter != null) {
                        voicesAdapter.notifyItemChanged(indexOf, itemHolder);
                    }
                }
            }

            if (changed != null && (changed == Boolean.FALSE || !changed)) {
                if (isEngineInfo && enginesAdapter != null) enginesAdapter.notifyDataSetChanged();
                if (isLocale && languagesAdapter != null) languagesAdapter.notifyDataSetChanged();
                if (isVoice && voicesAdapter != null) voicesAdapter.notifyDataSetChanged();
            }

            if (isEngineInfo) {
                //noinspection deprecation
                testingTTS.setEngineByPackageName(((EngineInfo) itemHolder.object).name);
            } else if (isLocale) {
                final Locale locale = (Locale) itemHolder.object;
                testingTTS.setLanguage(locale);
                try {
                    if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Voice voice = testingTTS.getVoice();
                        testingTTS.setVoice(new Voice(voice.getName(), locale,
                                voice.getQuality(), voice.getLatency(),
                                voice.isNetworkConnectionRequired(), voice.getFeatures()));
                    }
                } catch (final Exception e) {
                    // ignore
                }
            } else if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isVoice)
                testingTTS.setVoice((Voice) itemHolder.object);
        }
    };

    private final NumberPickerProgressListener progressListener = (numberPicker, progressValue, isChangeCompleted) -> {
        final float value = Math.max(0.1f, Math.min(2.0f, progressValue / 100f));
        if (isChangeCompleted && testingTTS != null) {
            if (numberPicker == pickerPitch) testingTTS.setPitch(value);
            else if (numberPicker == pickerSpeechRate) testingTTS.setSpeechRate(value);
        }
    };

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.tts_settings);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.tts_settings);
        toolbar.setTitle(R.string.tts_settings);

        pickerSpeechRate = findViewById(R.id.pickerSpeechRate);
        pickerPitch = findViewById(R.id.pickerPitch);
        rvVoices = findViewById(R.id.rvVoices);
        rvEngines = findViewById(R.id.rvEngines);
        rvLanguages = findViewById(R.id.rvLanguages);
        btnSaveSettings = findViewById(android.R.id.button1);
        btnPlayTest = findViewById(android.R.id.button2);

        rvVoicesParent = (View) rvVoices.getParent();
        rvEnginesParent = (View) rvEngines.getParent();
        rvLanguagesParent = (View) rvLanguages.getParent();

        final boolean doInit = testingTTS != null;
        if (!doInit) testingTTS = new TextToSpeech(getApplicationContext(), this::onTTSInit);

        try {
            Preconditions.checkNotNull(testingTTS);
        } catch (final Throwable e) {
            // ignore
        }

        if (!SettingsHelper.isTTSHelpDialogSeen()) {
            final AlertDialog alertDialog = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme).setTitle(R.string.tts_settings)
                    .setMessage(R.string.tts_first_time_message).setPositiveButton(R.string.ok, null)
                    .setCancelable(false).setOnCancelListener(DialogInterface::dismiss)
                    .setOnDismissListener(dialog -> SettingsHelper.setTTSHelpDialogSeen()).show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);

            final View btnOK = alertDialog.findViewById(android.R.id.button1);
            if (btnOK != null) {
                btnOK.setEnabled(false);
                btnOK.setOnClickListener(v -> alertDialog.dismiss());
            }

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.removeCallbacks(this);
                    if (btnOK != null) btnOK.setEnabled(true);
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                }
            }, 2000);
        }

        pickerPitch.setMinValue(1);
        pickerPitch.setMaxValue(200);
        pickerSpeechRate.setMinValue(1);
        pickerSpeechRate.setMaxValue(200);

        final ContentResolver contentResolver = getContentResolver();
        defaultPitch = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_PITCH,
                FALLBACK_TTS_DEFAULT_PITCH);
        defaultRate = Settings.Secure.getInt(contentResolver, Settings.Secure.TTS_DEFAULT_RATE,
                FALLBACK_TTS_DEFAULT_RATE);

        pickerPitch.setProgress(defaultPitch);
        pickerSpeechRate.setProgress(defaultRate);
        pickerPitch.setProgressListener(progressListener);
        pickerSpeechRate.setProgressListener(progressListener);

        rvVoicesParent.setVisibility(View.GONE);
        rvLanguagesParent.setVisibility(View.GONE);

        btnPlayTest.setEnabled(false);
        btnSaveSettings.setEnabled(false);

        if (testingTTS != null) {
            synchronized (DEMO_VOICE_MESSAGE) {
                if (doInit) onTTSInit(TextToSpeech.SUCCESS);
                if (defaultEngine == null) defaultEngine = testingTTS.getDefaultEngine();
                if (defaultLanguage == null) defaultLanguage = VERSION.SDK_INT >= 18 ? testingTTS.getDefaultLanguage() : null;
            }
            return;
        }

        Toast.makeText(getApplicationContext(), R.string.tts_not_initialized, Toast.LENGTH_SHORT).show();
        finishResult(RESULT_CANCELED);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();

        final boolean isResetTTS = itemId == R.id.mResetTTS;
        if (itemId == android.R.id.home || isResetTTS) {
            if (isResetTTS) SettingsHelper.resetTTS();
            finishResult(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        clearTTS();
        super.onDestroy();
    }

    @Override
    public boolean isFinishing() {
        final boolean finishing = super.isFinishing();
        if (finishing) clearTTS();
        return finishing;
    }

    private synchronized void finishResult(final int result) {
        setResult(result == RESULT_CANCELED || result == RESULT_FIRST_USER || result == RESULT_OK ? result : RESULT_CANCELED);
        //if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
        //else
        finish();
    }

    private synchronized void clearTTS() {
        synchronized (DEMO_VOICE_MESSAGE) {
            if (testingTTS != null) {
                testingTTS.stop();
                testingTTS.shutdown();
            }
            testingTTS = null;
        }
    }

    private synchronized void onTTSInit(final int initStatus) {
        if (testingTTS == null || initStatus != TextToSpeech.SUCCESS) return;

        defaultEngine = testingTTS.getDefaultEngine();
        defaultLanguage = VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? testingTTS.getDefaultLanguage() : null;
        try {
            defaultVoice = VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? testingTTS.getDefaultVoice() : null;
        } catch (final Exception e) {
            defaultVoice = null;
        }

        final Handler handler = new Handler(getMainLooper());
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, VERTICAL);

        final String ttsEngine = SettingsHelper.getTTSEngine();
        final String ttsLanguage = SettingsHelper.getTTSLanguage();
        final String ttsVoice = SettingsHelper.getTTSVoice();

        if (languagesList != null && languagesList.size() > 0) languagesList.clear();
        if (enginesList != null && enginesList.size() > 0) enginesList.clear();
        if (voicesList != null && voicesList.size() > 0) voicesList.clear();

        // set speech rate and pitch
        {
            testingTTS.setSpeechRate(SettingsHelper.getTTSSpeechRate() / 100f);
            testingTTS.setPitch(SettingsHelper.getTTSPitch() / 100f);
        }

        final Locale[] language = {testingTTS.getLanguage()};
        final Voice[] voice = new Voice[1];
        try {
            voice[0] = VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? testingTTS.getVoice() : null;
        } catch (final Exception e) {
            voice[0] = null;
        }

        new LocalAsyncTask<Void, Void>() {
            @Override
            protected Void doInBackground(final Void param) {
                // set engine, language and voice
                {
                    String tempStr;

                    final boolean setEngine = !Utils.isEmpty(ttsEngine) && !ttsEngine.equals(defaultEngine);
                    final boolean setLanguage = !Utils.isEmpty(ttsLanguage) && !Utils.isEmpty(tempStr = language[0] != null
                            ? language[0].getDisplayLanguage() : defaultLanguage != null ? defaultLanguage.getDisplayLanguage() : null)
                            && !ttsLanguage.equals(tempStr);
                    final boolean setVoice = VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Utils.isEmpty(ttsVoice)
                            && !Utils.isEmpty(tempStr = voice[0] != null ? voice[0].getName() : defaultVoice != null
                            ? defaultVoice.getName() : null) && !tempStr.equals(ttsVoice);

                    if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && setLanguage) {
                        final List<Locale> languages = new ArrayList<>(0);
                        final List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
                        Collection<Locale> localeCollection;
                        try {
                            localeCollection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? testingTTS.getAvailableLanguages() :
                                    locales;
                        } catch (final Exception e) {
                            localeCollection = locales;
                        }
                        languages.addAll(localeCollection);

                        for (final Locale intLanguage : languages) {
                            if (intLanguage != null && intLanguage.getDisplayLanguage().equals(ttsLanguage)) {
                                language[0] = intLanguage;
                                break;
                            }
                        }
                    }

                    if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && setVoice) {
                        for (final Voice intVoice : testingTTS.getVoices()) {
                            if (intVoice != null && TextUtils.equals(intVoice.getName(), ttsVoice)) {
                                voice[0] = intVoice;
                                break;
                            }
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //noinspection deprecation
                            testingTTS.setEngineByPackageName(setEngine ? ttsEngine : defaultEngine);

                            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && setVoice) {
                                language[0] = voice[0].getLocale();
                                testingTTS.setVoice(selectedVoice = voice[0]);
                            }

                            if (setLanguage && VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                testingTTS.setLanguage(selectedLanguage = language[0]);

                            handler.removeCallbacks(this);
                        }
                    });
                }

                ////////////////////////////////////////////////////////////////////////////////////////////

                // get engines
                {
                    for (final EngineInfo engine : testingTTS.getEngines()) {
                        if (engine == null) continue;

                        final boolean selected = !isEngineSelected && ((ttsEngine != null && ttsEngine.equals(engine.name)) ||
                                (!Utils.isEmpty(defaultEngine) && defaultEngine.equals(engine.name)));
                        if (selected && !isEngineSelected) isEngineSelected = true;
                        if (selectedEngine == null && selected) selectedEngine = engine;

                        enginesList.add(new TTSItemsAdapter.TTSItemHolder<>(engine, selected));
                    }
                    enginesList.trimToSize();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(this);

                            for (int i = rvEngines.getItemDecorationCount() - 1; i >= 0; i--)
                                rvEngines.removeItemDecorationAt(i);

                            if (enginesList.size() <= 0) {
                                rvEngines.setAdapter(enginesAdapter = null);
                                rvEnginesParent.setVisibility(View.GONE);
                            } else {
                                rvEngines.addItemDecoration(dividerItemDecoration);
                                rvEngines.setLayoutManager(new LinearLayoutManager(TTSActivity.this));
                                rvEngines.setAdapter(enginesAdapter = new TTSItemsAdapter<>(TTSActivity.this, enginesList, onClickListener));
                                rvEnginesParent.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                // get voices
                if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (defaultVoice == null) {
                        try {
                            defaultVoice = testingTTS.getDefaultVoice();
                        } catch (final Exception e) {
                            defaultVoice = null;
                        }
                    }

                    try {
                        for (final Voice voice : testingTTS.getVoices()) {
                            if (voice == null) continue;

                            final boolean selected = !isVoiceSelected && ttsVoice != null && voice.getName().equals(ttsVoice);
                            if (selected) {
                                if (!isVoiceSelected) isVoiceSelected = true;
                                if (selectedVoice == null) selectedVoice = voice;
                            }

                            voicesList.add(new TTSItemsAdapter.TTSItemHolder<>(voice, selected));
                        }
                    } catch (final Exception e) {
                        // ignore
                    }
                    voicesList.trimToSize();

                    final boolean voiceListEmpty = voicesList.size() <= 0;
                    if (!voiceListEmpty) {
                        Collections.sort(voicesList, VOICES_NAME_COMPARATOR);
                        try {
                            Collections.sort(voicesList, VOICES_INTERNET_COMPARATOR);
                        } catch (final Throwable e) {
                            // idk how this worked, but it MIGHT crash
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(this);

                            for (int i = rvVoices.getItemDecorationCount() - 1; i >= 0; i--)
                                rvVoices.removeItemDecorationAt(i);

                            if (voiceListEmpty) {
                                rvVoices.setAdapter(voicesAdapter = null);
                                rvVoicesParent.setVisibility(View.GONE);
                            } else {
                                rvVoices.addItemDecoration(dividerItemDecoration);
                                rvVoices.setNestedScrollingEnabled(true);
                                rvVoices.setLayoutManager(new LinearLayoutManager(TTSActivity.this));
                                rvVoices.setAdapter(voicesAdapter = new TTSItemsAdapter<>(TTSActivity.this, voicesList, onClickListener));
                                rvVoicesParent.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                // get languages
                if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                        (voice[0] == null || VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                ) {
                    final Locale[] availableLocales = Locale.getAvailableLocales();
                    for (final Locale locale : availableLocales) {
                        if (locale == null) continue;
                        final int languageAvailable = testingTTS.isLanguageAvailable(locale);
                        if (languageAvailable != TextToSpeech.LANG_AVAILABLE) continue;

                        final boolean selected = !isLanguageSelected && (ttsLanguage != null
                                && locale.getDisplayLanguage().equals(ttsLanguage) || defaultLanguage != null
                                && (locale == defaultLanguage || locale.equals(defaultLanguage)));
                        if (selected && !isLanguageSelected) isLanguageSelected = true;
                        if (selectedLanguage == null && selected) selectedLanguage = locale;

                        languagesList.add(new TTSItemsAdapter.TTSItemHolder<>(locale, selected));
                    }
                    languagesList.trimToSize();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(this);

                            for (int i = rvLanguages.getItemDecorationCount() - 1; i >= 0; i--)
                                rvLanguages.removeItemDecorationAt(i);

                            if (languagesList.size() <= 0) {
                                rvLanguages.setAdapter(languagesAdapter = null);
                                rvLanguagesParent.setVisibility(View.GONE);
                            } else {
                                rvLanguages.addItemDecoration(dividerItemDecoration);
                                rvLanguages.setNestedScrollingEnabled(true);
                                rvLanguages.setLayoutManager(new LinearLayoutManager(TTSActivity.this));
                                rvLanguages.setAdapter(languagesAdapter = new TTSItemsAdapter<>(TTSActivity.this, languagesList, onClickListener));
                                rvLanguagesParent.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }


                // if defaults not selected, select first item
                if (enginesList.size() > 0 && !isEngineSelected) {
                    final TTSItemsAdapter.TTSItemHolder<EngineInfo> itemHolder = enginesList.get(0);
                    selectedEngine = itemHolder.object;
                    itemHolder.selected = true;
                }
                if (languagesList.size() > 0 && !isLanguageSelected) {
                    for (final TTSItemsAdapter.TTSItemHolder<Locale> itemHolder : languagesList) {
                        if (itemHolder.object.getDisplayLanguage().equals(defaultLanguage.getDisplayLanguage())) {
                            selectedLanguage = itemHolder.object;
                            itemHolder.selected = true;
                            break;
                        }
                    }
                }
                if (voicesList.size() > 0 && !isVoiceSelected && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (final TTSItemsAdapter.TTSItemHolder<Voice> itemHolder : voicesList) {
                        if (TextUtils.equals(itemHolder.object.getName(),
                                defaultVoice != null ? defaultVoice.getName() : null)) {
                            selectedVoice = itemHolder.object;
                            itemHolder.selected = true;
                            break;
                        }
                    }
                }

                // enable buttons and setup click listeners
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacks(this);

                        btnPlayTest.setEnabled(true);
                        btnSaveSettings.setEnabled(true);

                        btnPlayTest.setOnClickListener(onClickListener);
                        btnSaveSettings.setOnClickListener(onClickListener);
                    }
                });

                return null;
            }
        }.execute();
    }
}