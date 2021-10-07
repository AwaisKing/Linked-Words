package awais.backworddictionary.asyncs;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.executor.LocalAsyncTask;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;

public final class WordsAsync extends LocalAsyncTask<Void, ArrayList<WordItem>> {
    private final String word;
    private final String method;
    private final FragmentCallback fragmentCallback;

    public WordsAsync(final FragmentCallback fragmentCallback, final String word, final String method, final Context context) {
        this.fragmentCallback = fragmentCallback;
        this.word = word;
        if (context != null) {
            final Resources resources = context.getResources();
            final String[] methodsList = new String[]{
                    resources.getString(R.string.reverse),
                    resources.getString(R.string.sounds_like),
                    resources.getString(R.string.spelled_like),
                    resources.getString(R.string.synonyms),
                    resources.getString(R.string.antonyms),
                    resources.getString(R.string.triggers),
                    resources.getString(R.string.part_of),
                    resources.getString(R.string.comprises),
                    resources.getString(R.string.homophones),
                    resources.getString(R.string.rhymes)
            };
            if (methodsList[0].equals(method)) this.method = "ml";
            else if (methodsList[1].equals(method)) this.method = "sl";
            else if (methodsList[2].equals(method)) this.method = "sp";
            else if (methodsList[3].equals(method)) this.method = "rel_syn";
            else if (methodsList[4].equals(method)) this.method = "rel_ant";
            else if (methodsList[5].equals(method)) this.method = "rel_trg";
            else if (methodsList[6].equals(method)) this.method = "rel_par";
            else if (methodsList[7].equals(method)) this.method = "rel_com";
            else if (methodsList[8].equals(method)) this.method = "rel_hom";
            else if (methodsList[9].equals(method)) this.method = "rel_rhy";
            else this.method = "ml";
        } else
            this.method = "ml";
    }

    @Override
    protected void onPreExecute() {
        if (fragmentCallback != null) fragmentCallback.wordStarted();
    }

    @Override
    protected ArrayList<WordItem> doInBackground(final Void param) {
        ArrayList<WordItem> wordItemsList = null;

        try {
            final int wordsCount = SettingsHelper.getMaxWords();
            final String body = Utils.getResponse("https://api.data".concat("muse.com/words?md=pds&max=")
                    .concat(String.valueOf(wordsCount)).concat("&").concat(method)
                    .concat("=").concat( URLEncoder.encode(word)));
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
            else Utils.firebaseCrashlytics.recordException(e);
        }

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> wordItems) {
        if (fragmentCallback != null)
            fragmentCallback.done(wordItems, word);
    }
}