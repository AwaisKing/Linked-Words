package awais.backworddictionary.asyncs;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.executor.LocalAsyncTask;
import awais.backworddictionary.helpers.URLEncoder;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.MainCheck;

public final class SearchAsyncTask extends LocalAsyncTask<String, ArrayList<WordItem>> {
    private MainCheck mainCheck;

    public SearchAsyncTask(final MainCheck mainCheck) {
        this.mainCheck = mainCheck;
    }

    @NonNull
    @Override
    protected ArrayList<WordItem> doInBackground(final String param) {
        final ArrayList<WordItem> arrayList = new ArrayList<>(0);

        try {
            final String response = Utils.getResponse("https://api.data".concat("muse.com/sug?s=")
                    .concat(URLEncoder.encode(param)));

            if (response != null) {
                final JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); ++i) {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    arrayList.add(new WordItem(jsonObject.getString("word"), 0, null, null));
                }
            }
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "SearchAsyncTask", e);
        }

        return arrayList;
    }

    @Override
    protected void onCancelled(@NonNull final ArrayList<WordItem> wordItems) {
        if (mainCheck != null) mainCheck.afterSearch(null);
        mainCheck = null;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> result) {
        if (mainCheck != null) mainCheck.afterSearch(result);
    }
}
