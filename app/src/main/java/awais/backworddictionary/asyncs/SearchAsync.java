package awais.backworddictionary.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    private static OkHttpClient client = new OkHttpClient();
    private static Call call;
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
            if (client != null) {
                try {
                    client.dispatcher().cancelAll();
                    client.connectionPool().evictAll();
                    client.cache().close();
                } catch (Exception ignored) {}
            }

            if (call != null) try { call.cancel(); } catch (Exception ignored) {}

            if (client == null) client = new OkHttpClient();
            call = client.newCall(new Request.Builder()
                    .url("http://api.datamuse.com/sug?s=".concat(query)).build());
            response = call.execute();

//            if (call != null) try { call.cancel(); } catch (Exception ignored) {}

            if (response != null && response.code() == 200) {
                //noinspection ConstantConditions
                arrayList = new Gson().fromJson(response.body().string(),
                        new TypeToken<List<WordItem>>(){}.getType());
                try { call.cancel(); } catch (Exception ignored) {}
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
