package awais.backworddictionary.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.MainCheck;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private WeakReference<OkHttpClient> client = new WeakReference<>(new OkHttpClient());
    private WeakReference<Call> call;
    private MainCheck mainCheck;

    public SearchAsync(MainCheck mainCheck) {
        this.mainCheck = mainCheck;
    }

    @Override
    protected ArrayList<WordItem> doInBackground(String... params) {
        String query;
        try {
            query = URLEncoder.encode(params[0], "UTF-8");
        } catch (Exception e) {
            query = params[0].replaceAll("\\s", "+").replaceAll(" ", "+")
                    .replaceAll("#", "%23").replaceAll("@", "%40")
                    .replaceAll("&", "%26");
        }

        final ArrayList<WordItem> arrayList = new ArrayList<>();

        Response response = null;
        try {
            if (client == null)
                client = new WeakReference<>(new OkHttpClient());

            OkHttpClient okHttpClient = client.get();

            if (okHttpClient != null) {
                try {
                    okHttpClient.dispatcher().cancelAll();
                    okHttpClient.connectionPool().evictAll();
                    Cache cache = okHttpClient.cache();
                    if (cache != null) cache.close();
                } catch (Exception ignored) {}
            }

            if (call != null && call.get() != null)
                try { call.get().cancel(); } catch (Exception ignored) {}

            if (okHttpClient == null) okHttpClient = new OkHttpClient();

            call = new WeakReference<>(okHttpClient.newCall(new Request.Builder()
                    .url("http://api.datamuse.com/sug?s=".concat(query)).build()));
            try { response = call.get().execute(); } catch (Exception ignored) {}

            if (response != null && response.code() == 200) {
                final ResponseBody body = response.body();
                if (body != null) {
                    final JSONArray jsonArray = new JSONArray(body.string());
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        final JSONObject jsonObject = jsonArray.getJSONObject(i);
                        arrayList.add(new WordItem(jsonObject.getString("word"), 0, null, null));
                    }
                }
                try { call.get().cancel(); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }
        if (response != null) try { response.close(); } catch (Exception ignored) {}
        return arrayList;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mainCheck.afterSearch(null);
    }

    @Override
    protected void onCancelled(ArrayList<WordItem> wordItems) {
        super.onCancelled(wordItems);
        mainCheck = null;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> result) {
        super.onPostExecute(result);
        if (mainCheck != null) mainCheck.afterSearch(result);
    }
}
