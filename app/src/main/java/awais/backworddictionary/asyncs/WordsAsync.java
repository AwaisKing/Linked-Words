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
    private String word, method;
    private final OkHttpClient client = new OkHttpClient();
    private final Request.Builder builder = new Request.Builder();
    private Response response = null;

    private final AtomicReference<ProgressBar> progressWords = new AtomicReference<>();
    private final AtomicReference<RecyclerView> recyclerView = new AtomicReference<>();
    private final AtomicReference<Activity> activity = new AtomicReference<>();
    private FragmentCallback fragmentCallback;
    private isError1 = false, isError2 = false;

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
    }

    @Override
    protected ArrayList<WordItem> doInBackground(String... params) {
        isError1 = false;
        isError2 = false;
        activity.get().runOnUiThread(() -> {
            progressWords.get().setVisibility(View.VISIBLE);
            recyclerView.get().setClickable(false);
            recyclerView.get().setEnabled(false);
            recyclerView.get().setFocusable(false);
            recyclerView.get().setLayoutFrozen(true);
        });

        Log.d("AWAISKING_APP", "step: " + 1);
        if (wordItemsList == null) wordItemsList = new ArrayList<>();
        else wordItemsList.clear();

        Log.d("AWAISKING_APP", "step: " + 2);
        String query = word.replaceAll("\\s", "+").replaceAll(" ", "+").replace("#", "%23")
                .replace("@", "%40").replace("&", "%26");

        Log.d("AWAISKING_APP", "step: " + 3);
        int wordsCount = Main.sharedPreferences.getInt("maxWords", 80);

        Log.d("AWAISKING_APP", "step: " + 4);
        builder.url("https://api.datamuse.com/words?md=pds&max=" + wordsCount + "&" + method + "=" + query);

        try { if (response != null) response.close(); } catch (Exception ignored) {}

        try {
            Log.d("AWAISKING_APP", "step: " + 5);
            response = client.newCall(builder.build()).execute();
            Log.d("AWAISKING_APP", "step: " + 6);
            if (response.code() == 200)
                wordItemsList = new Gson().fromJson(response.body().string(),
                new TypeToken<List<WordItem>>() {}.getType());
        } catch (Exception e) {
            isError1 = true;
            Log.e("AWAISKING_APP", "", e);
        }

        if (isError1)
            try {
                activity.get().runOnUiThread(() -> NoNet.check(activity.get()).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build())
                        .callback(connectionStatus -> {
                            if (connectionStatus != ConnectionStatus.CONNECTED)
                                Toast.makeText(activity.get(), "Not connected to internet.\nPlease connect to network.", Toast.LENGTH_SHORT).show();
                        }).start());
            } catch (Exception ignored) { isError2 = true; }

        if (isError2)
            try {
                activity.get().runOnUiThread(() -> Toast.makeText(activity.get(),
                    "Error occurred" + (e1.getStackTrace() != null ? ": " + e1.getStackTrace()[1].toString() : ""), Toast.LENGTH_LONG).show());
            } catch (Exception ignored) {}

        try { if (response != null) response.close(); } catch (Exception ignored) {}

        Log.d("AWAISKING_APP", "step: " + 7);
        return wordItemsList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> wordItems) {
        if (wordItems != null && fragmentCallback != null) fragmentCallback.done(wordItems, word);
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