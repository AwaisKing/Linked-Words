package awais.backworddictionary.helpers;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TTSHelper {
    public static final int TTS_DATA_CHECK_CODE = 775;
    public static final int TTS_SETTINGS_REQUEST_CODE = 5320;

    public static Context linkedAppContext;
    public static AtomicBoolean isTTSAsyncRunning = new AtomicBoolean();
    public static TextToSpeech tts;

    public static synchronized void onTTSInit(final int initStatus) {
        if (tts == null || initStatus != TextToSpeech.SUCCESS) return;

        final SettingsHelper settingsHelper = SettingsHelper.getInstance(linkedAppContext);

        // set speech rate and pitch
        {
            tts.setSpeechRate(settingsHelper.getTTSSpeechRate() / 100f);
            tts.setPitch(settingsHelper.getTTSPitch() / 100f);
        }

        // set engine and voice
        if (isTTSAsyncRunning.getAndSet(true)) return;

        final String defaultEngine = tts.getDefaultEngine();
        final Locale defaultLanguage = tts.getDefaultLanguage();
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

        final String ttsEngine = settingsHelper.getTTSEngine();
        final String ttsVoice = settingsHelper.getTTSVoice();
        final String ttsLanguage = settingsHelper.getTTSLanguage();

        String tempStr;

        final boolean setEngine = !Utils.isEmpty(ttsEngine) && !ttsEngine.equals(defaultEngine);
        final boolean setLanguage = !Utils.isEmpty(ttsLanguage) && !Utils.isEmpty(
                tempStr = language != null ? language.getDisplayLanguage() : defaultLanguage != null ? defaultLanguage.getDisplayLanguage() : null)
                                    && !ttsLanguage.equals(tempStr);
        final boolean setVoice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Utils.isEmpty(ttsVoice)
                                 && !Utils.isEmpty(tempStr = voice != null ? voice.getName() : defaultVoice != null ? defaultVoice.getName() : null)
                                 && !tempStr.equals(ttsVoice);

        if (setEngine) engine = ttsEngine;

        if (setLanguage) {
            final List<Locale> languages = new ArrayList<>();
            final List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
            Collection<Locale> localeCollection;
            try {
                localeCollection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? tts.getAvailableLanguages() : locales;
            } catch (final Exception e) {
                localeCollection = locales;
            }
            languages.addAll(localeCollection);

            for (final Locale intLanguage : languages) {
                if (intLanguage == null || !intLanguage.getDisplayLanguage().equals(ttsLanguage)) continue;
                language = intLanguage;
                break;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && setVoice) {
            for (final Voice intVoice : tts.getVoices()) {
                if (intVoice == null || !TextUtils.equals(intVoice.getName(), ttsVoice)) continue;
                voice = intVoice;
                break;
            }
        }

        // noinspection deprecation
        tts.setEngineByPackageName(engine);
        tts.setLanguage(language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voice != null) tts.setVoice(voice);

        isTTSAsyncRunning.set(false);
    }

    public static void speakText(final CharSequence text) {
        if (tts == null || Utils.isEmpty(text)) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        else
            tts.speak(String.valueOf(text), TextToSpeech.QUEUE_FLUSH, null);
    }
}