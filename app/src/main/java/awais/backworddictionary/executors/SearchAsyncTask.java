package awais.backworddictionary.executors;

import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.Main;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.models.WordItem;

public final class SearchAsyncTask extends LocalAsyncTask<String, ArrayList<WordItem>> {
    private Main main;

    public SearchAsyncTask(final Main main) {
        this.main = main;
    }

    @Nullable
    @Override
    protected ArrayList<WordItem> doInBackground(final String param) {
        try {
            final ArrayList<WordItem> arrayList = new ArrayList<>();
            final String response = Utils.getResponse("https://api.datamuse.com/sug?s=" + URLEncoder.encode(param));

            if (!Utils.isEmpty(response)) {
                final JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); ++i) {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    arrayList.add(new WordItem(jsonObject.getString("word"), 0, null, null));
                }
            }
            return arrayList;
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "SearchAsyncTask", e);
            return null;
        }
    }

    @Override
    protected void onCancelled(final ArrayList<WordItem> wordItems) {
        if (main != null) main.afterSearch(null);
        main = null;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> result) {
        if (main != null) main.afterSearch(result);
    }
}