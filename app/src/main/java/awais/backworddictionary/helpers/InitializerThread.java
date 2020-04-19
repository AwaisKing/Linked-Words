package awais.backworddictionary.helpers;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

import awais.backworddictionary.Main;

import static awais.backworddictionary.Main.tts;

public final class InitializerThread extends Thread {
    private final boolean method;
    private final Main activity;

    public InitializerThread(boolean method, Main activity) {
        this.method = method;
        this.activity = activity;
    }

    @Override
    public void run() {
        if (method) {
            activity.runOnUiThread(() -> activity.loadFragments(true));
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
