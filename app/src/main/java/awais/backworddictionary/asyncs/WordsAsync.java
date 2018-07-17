package awais.backworddictionary.asyncs;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import awais.backworddictionary.Main;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FragmentCallback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WordsAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private ArrayList<WordItem> wordItemsList = new ArrayList<>();
    private final String word;
    private final String method;
    private final OkHttpClient client = new OkHttpClient();
    private final Request.Builder builder = new Request.Builder();
    private Response response = null;

    private final AtomicReference<SwipeRefreshLayout> refreshLayout = new AtomicReference<>();
    private final AtomicReference<RecyclerView> recyclerView = new AtomicReference<>();
    private final AtomicReference<Activity> activity = new AtomicReference<>();
    private final FragmentCallback fragmentCallback;

    public WordsAsync(Activity activity, FragmentCallback fragmentCallback, String word, int method,
                      SwipeRefreshLayout refreshLayout, RecyclerView recyclerView) {
        this.activity.set(activity);
        this.fragmentCallback = fragmentCallback;
        this.refreshLayout.set(refreshLayout);
        this.recyclerView.set(recyclerView);
        this.word = word;
        switch (method) {
            case 0: this.method = "ml"; break;
            case 1: this.method = "sl"; break;
            case 2: this.method = "sp"; break;
            case 3: this.method = "rel_syn"; break; // synonyms
            case 4: this.method = "rel_ant"; break; // antonyms
            case 5: this.method = "rel_trg"; break; // triggers
            case 6: this.method = "rel_com"; break; // comprise
            case 7: this.method = "rel_par"; break; // part of
            case 8: this.method = "rel_rhy"; break; // rhymes
            case 9: this.method = "rel_hom"; break; // homohphone
            default: this.method = "ml";
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.get().runOnUiThread(() -> {
            refreshLayout.get().setRefreshing(true);
            recyclerView.get().setClickable(false);
            recyclerView.get().setEnabled(false);
            recyclerView.get().setFocusable(false);
            recyclerView.get().setLayoutFrozen(true);
        });
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
                    .replace("#", "%23").replace("@", "%40")
                    .replace("&", "%26");
        }

        int wordsCount = Main.sharedPreferences.getInt("maxWords", 80);

        builder.url("https://api.datamuse.com/words?md=pds&max=".concat(String.valueOf(wordsCount))
                .concat("&").concat(method).concat("=").concat(query));

        try { if (response != null) response.close(); } catch (Exception ignored) {}

        try {
            response = client.newCall(builder.build()).execute();
            if (response.code() == 200)
                //noinspection ConstantConditions
                wordItemsList = new Gson().fromJson(response.body().string(),
                        new TypeToken<List<WordItem>>() {}.getType());
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "", e);
        }

        if (response != null) response.close();

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> wordItems) {
        if (wordItems != null) if (fragmentCallback != null) fragmentCallback.done(wordItems, word);
        activity.get().runOnUiThread(() -> {
            refreshLayout.get().setRefreshing(false);
            recyclerView.get().setClickable(true);
            recyclerView.get().setEnabled(true);
            recyclerView.get().setFocusable(true);
            recyclerView.get().setLayoutFrozen(false);
        });
        super.onPostExecute(wordItems);
    }
}