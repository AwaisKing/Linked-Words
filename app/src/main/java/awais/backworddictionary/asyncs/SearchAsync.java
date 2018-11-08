package awais.backworddictionary.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.MainCheck;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private WeakReference<OkHttpClient> client = new WeakReference<>(new OkHttpClient());
    private WeakReference<Call> call;
    private final MainCheck mainCheck;

    public SearchAsync(MainCheck mainCheck) {
        this.mainCheck = mainCheck;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mainCheck.beforeSearch();
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
        ArrayList<WordItem> arrayList = new ArrayList<>();

        Response response = null;
        try {
            if (client.get() != null) {
                try {
                    client.get().dispatcher().cancelAll();
                    client.get().connectionPool().evictAll();
                    client.get().cache().close();
                } catch (Exception ignored) {}
            }

            if (call.get() != null) try { call.get().cancel(); } catch (Exception ignored) {}

            if (client.get() == null) client = new WeakReference<>(new OkHttpClient());
            call = new WeakReference<>(client.get().newCall(new Request.Builder()
                    .url("http://api.datamuse.com/sug?s=".concat(query)).build()));
            response = call.get().execute();

            if (response != null && response.code() == 200) {
                //noinspection ConstantConditions
                arrayList = new Gson().fromJson(response.body().string(),
                        new TypeToken<List<WordItem>>(){}.getType());
                try { call.get().cancel(); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "", e);
        }
        if (response != null) try { response.close(); } catch (Exception ignored) {}
        return arrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> result) {
        super.onPostExecute(result);
        mainCheck.afterSearch(result);
    }
}
