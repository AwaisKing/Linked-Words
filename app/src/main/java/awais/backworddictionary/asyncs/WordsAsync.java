package awais.backworddictionary.asyncs;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FragmentCallback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WordsAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private ArrayList<WordItem> wordItemsList = new ArrayList<>();
    private final String word;
    private final String method;
    private final OkHttpClient client = new OkHttpClient();
    private final Request.Builder builder = new Request.Builder();
    private Response response = null;

    private final FragmentCallback fragmentCallback;

    public WordsAsync(FragmentCallback fragmentCallback, String word, String method, Context context) {
        this.fragmentCallback = fragmentCallback;
        this.word = word;
        if (context == null) {
            this.method = "ml";
            return;
        }
        final String[] methodsList = new String[] {
                context.getResources().getString(R.string.reverse),
                context.getResources().getString(R.string.sounds_like),
                context.getResources().getString(R.string.spelled_like),
                context.getResources().getString(R.string.synonyms),
                context.getResources().getString(R.string.antonyms),
                context.getResources().getString(R.string.triggers),
                context.getResources().getString(R.string.part_of),
                context.getResources().getString(R.string.comprises),
                context.getResources().getString(R.string.homophones),
                context.getResources().getString(R.string.rhymes)
        };
        if (methodsList[0].equals(method))      this.method = "ml";
        else if (methodsList[1].equals(method)) this.method = "sl";
        else if (methodsList[2].equals(method)) this.method = "sp";
        else if (methodsList[3].equals(method)) this.method = "rel_syn";
        else if (methodsList[4].equals(method)) this.method = "rel_ant";
        else if (methodsList[5].equals(method)) this.method = "rel_trg";
        else if (methodsList[6].equals(method)) this.method = "rel_par";
        else if (methodsList[7].equals(method)) this.method = "rel_com";
        else if (methodsList[8].equals(method)) this.method = "rel_hom";
        else if (methodsList[9].equals(method)) this.method = "rel_rhy";
        else                                    this.method = "ml";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fragmentCallback != null) fragmentCallback.wordStarted();
    }

    @Override
    protected ArrayList<WordItem> doInBackground(String... params) {
        if (wordItemsList == null) wordItemsList = new ArrayList<>();
        else wordItemsList.clear();

        String query;
        try {
            query = URLEncoder.encode(word, "UTF-8");
        } catch (Exception e) {
            query = word.replaceAll("\\s", "+").replaceAll(" ", "+")
                    .replaceAll("#", "%23").replaceAll("@", "%40")
                    .replaceAll("&", "%26");
        }

        final int wordsCount = Main.sharedPreferences.getInt("maxWords", 80);

        builder.url("https://api.datamuse.com/words?md=pds&max=".concat(String.valueOf(wordsCount))
                .concat("&").concat(method).concat("=").concat(query));

        try { if (response != null) response.close(); } catch (Exception ignored) {}

        try {
            response = client.newCall(builder.build()).execute();
            if (response.code() == 200) {
                final ResponseBody body = response.body();
                if (body != null) {
                    final JSONArray jsonArray = new JSONArray(body.string());

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
            }
        } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }

        if (response != null) response.close();

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> wordItems) {
        if (fragmentCallback != null)
            fragmentCallback.done(wordItems, word);
        super.onPostExecute(wordItems);
    }
}