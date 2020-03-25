package awais.backworddictionary.asyncs;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import awais.backworddictionary.R;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.FragmentCallback;

public class WordsAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private ArrayList<WordItem> wordItemsList = new ArrayList<>();
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
    protected ArrayList<WordItem> doInBackground(final String... params) {
        if (wordItemsList == null) wordItemsList = new ArrayList<>(0);
        else wordItemsList.clear();

        String query;
        try {
            query = URLEncoder.encode(word, "UTF-8");
        } catch (Exception e) {
            query = word.replaceAll("\\s", "+").replaceAll(" ", "+")
                    .replaceAll("#", "%23").replaceAll("@", "%40")
                    .replaceAll("&", "%26");
        }

        try {
            final int wordsCount = SettingsHelper.getMaxWords();
            final String body = Utils.getResponse("https://api.datamuse.com/words?md=pds&max=" + wordsCount + "&" + method + "=" + query);
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
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "", e);
        }

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> wordItems) {
        if (fragmentCallback != null)
            fragmentCallback.done(wordItems, word);
    }
}