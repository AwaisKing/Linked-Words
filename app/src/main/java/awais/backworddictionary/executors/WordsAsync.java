package awais.backworddictionary.executors;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;
import awais.backworddictionary.models.WordItem;

public final class WordsAsync extends LocalAsyncTask<Void, ArrayList<WordItem>> {
    private final String word;
    private final String method;
    private final SettingsHelper settingsHelper;
    private final FragmentCallback fragmentCallback;

    public WordsAsync(final FragmentCallback fragmentCallback, final String word, final int method, final Context context) {
        this.word = word;
        this.fragmentCallback = fragmentCallback;
        this.settingsHelper = SettingsHelper.getInstance(context);
        if (method == R.string.reverse) this.method = "ml";
        else if (method == R.string.sounds_like) this.method = "sl";
        else if (method == R.string.spelled_like) this.method = "sp";
        else if (method == R.string.synonyms) this.method = "rel_syn";
        else if (method == R.string.antonyms) this.method = "rel_ant";
        else if (method == R.string.triggers) this.method = "rel_trg";
        else if (method == R.string.part_of) this.method = "rel_par";
        else if (method == R.string.comprises) this.method = "rel_com";
        else if (method == R.string.homophones) this.method = "rel_hom";
        else if (method == R.string.rhymes) this.method = "rel_rhy";
        else this.method = "ml";
    }

    @Override
    protected void onPreExecute() {
        if (fragmentCallback != null) fragmentCallback.wordStarted();
    }

    @Override
    protected ArrayList<WordItem> doInBackground(final Void param) {
        ArrayList<WordItem> wordItemsList = null;

        try {
            final int wordsCount = settingsHelper.getMaxWords();
            final String body = Utils.getResponse("https://api.data".concat("muse.com/words?md=pds&max=")
                                                                    .concat(String.valueOf(wordsCount)).concat("&").concat(method)
                                                                    .concat("=").concat(URLEncoder.encode(word)));
            if (body != null) {
                final JSONArray jsonArray = new JSONArray(body);
                wordItemsList = new ArrayList<>(jsonArray.length());

                for (int i = 0; i < jsonArray.length(); ++i) {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);

                    final String word = jsonObject.getString("word");
                    final int numSyllables = jsonObject.getInt("numSyllables");
                    String[] tagsString = null;
                    String[][] defsString = null;

                    if (jsonObject.has("tags")) {
                        final JSONArray tags = jsonObject.getJSONArray("tags");
                        tagsString = new String[tags.length()];
                        for (int j = 0; j < tags.length(); ++j)
                            tagsString[j] = tags.getString(j);
                    }
                    if (jsonObject.has("defs")) {
                        final JSONArray defs = jsonObject.getJSONArray("defs");
                        defsString = new String[defs.length()][2];
                        for (int j = 0; j < defs.length(); ++j)
                            defsString[j] = defs.getString(j).split("\t");
                    }
                    wordItemsList.add(new WordItem(word, numSyllables, tagsString, defsString));
                }
            }
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "WordsAsync :: " + e, e);
        }

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> wordItems) {
        if (fragmentCallback != null) fragmentCallback.done(wordItems, word);
    }
}