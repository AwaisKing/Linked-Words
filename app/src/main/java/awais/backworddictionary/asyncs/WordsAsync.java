package awais.backworddictionary.asyncs;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.NoNet;

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
    private boolean isError1, isError2;

    private final AtomicReference<ProgressBar> progressWords = new AtomicReference<>();
    private final AtomicReference<RecyclerView> recyclerView = new AtomicReference<>();
    private final AtomicReference<Activity> activity = new AtomicReference<>();
    private final FragmentCallback fragmentCallback;

    public WordsAsync(Activity activity, FragmentCallback fragmentCallback, String word, int method,
                      ProgressBar progressWords, RecyclerView recyclerView) {
        this.activity.set(activity);
        this.fragmentCallback = fragmentCallback;
        this.progressWords.set(progressWords);
        this.recyclerView.set(recyclerView);
        this.word = word;
        switch (method) {
            case 0:
                this.method = "ml";
                break;
            case 1:
                this.method = "sl";
                break;
            case 2:
                this.method = "sp";
                break;
            default:
                this.method = "ml";
        }
        isError1 = false;
        isError2 = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.get().runOnUiThread(() -> {
            progressWords.get().setVisibility(View.VISIBLE);
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

        builder.url("https://api.datamuse.com/words?md=pds&max=" + wordsCount + "&" + method + "=" + query);

        try { if (response != null) response.close(); } catch (Exception ignored) {}

        try {
            response = client.newCall(builder.build()).execute();
            if (response.code() == 200)
                //noinspection ConstantConditions
                wordItemsList = new Gson().fromJson(response.body().string(),
                        new TypeToken<List<WordItem>>() {}.getType());
        } catch (Exception e) {
            isError1 = true;
            Log.e("AWAISKING_APP", "", e);
        }

        Exception ex = null;
        if (isError1) {
            try {
                activity.get().runOnUiThread(() -> NoNet.check(activity.get()).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build())
                        .callback(connectionStatus -> {
                            if (connectionStatus != ConnectionStatus.CONNECTED)
                                Toast.makeText(activity.get(), "Not connected to internet.\nPlease connect to network.", Toast.LENGTH_SHORT).show();
                        }).start());
            } catch (Exception e) { isError2 = true; ex = e; }
        }
        if (isError2 && ex != null) {
            Exception finalEx = ex;
            activity.get().runOnUiThread(() -> Toast.makeText(activity.get(),
                    "Error occurred" + (finalEx.getStackTrace() != null ? ": " + finalEx.getStackTrace()[1].toString() : ""), Toast.LENGTH_LONG).show());
        }

        if (response != null) response.close();

        return wordItemsList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> wordItems) {
        if (wordItems != null) if (fragmentCallback != null) fragmentCallback.done(wordItems, word);
        activity.get().runOnUiThread(() -> {
            progressWords.get().setVisibility(View.GONE);
            recyclerView.get().setClickable(true);
            recyclerView.get().setEnabled(true);
            recyclerView.get().setFocusable(true);
            recyclerView.get().setLayoutFrozen(false);
        });
        super.onPostExecute(wordItems);
    }
}