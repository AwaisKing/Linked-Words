package awais.backworddictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Native;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DictionaryFragment extends Fragment implements FragmentCallback {
    private List<Object> wordList;
    private DictionaryAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressWords;
    private FragmentCallback fragmentCallback;
    private TextToSpeech tts;
    public String title;
    private Activity activity;

    public DictionaryFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        tts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int initStatus) {
            if (initStatus == TextToSpeech.SUCCESS) {
                if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                    tts.setLanguage(Locale.US);
                else if (tts.isLanguageAvailable(Locale.CANADA) == TextToSpeech.LANG_AVAILABLE)
                    tts.setLanguage(Locale.CANADA);
                else if (tts.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE)
                    tts.setLanguage(Locale.UK);
                else if (tts.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
                    tts.setLanguage(Locale.ENGLISH);
            }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {tts.stop();} catch (Exception ignore){}
        try {tts.shutdown();} catch (Exception ignore){}
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View magicRootView = inflater.inflate(R.layout.dictionary_view, container, false);

        fragmentCallback = this;

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        progressWords = magicRootView.findViewById(R.id.progressWords);

        wordList = new ArrayList<>();
        adapter = new DictionaryAdapter(getContext(), wordList, tts);
        adapter.setHasStableIds(true);

        recyclerView.setAdapter(adapter);

        return magicRootView;
    }

    @SuppressWarnings("unused")
    public void startWords(int method, String word) throws ExecutionException, InterruptedException {
        switch (method) {
            case 0:
                new WordsAsync().execute(word, "ml");
                break;
            case 1:
                new WordsAsync().execute(word, "sl");
                break;
            case 2:
                new WordsAsync().execute(word, "sp");
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void done(ArrayList<Object> items, final String word) {
        wordList = items;

        if (wordList.size() > 70) {
            for (int i = 0; i <= wordList.size(); i += 66)
                wordList.add(i, new NativeExpressAdView(activity));

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= wordList.size(); i += 66) {
                        if (wordList.get(i).getClass() == NativeExpressAdView.class) {
                            final NativeExpressAdView adView = (NativeExpressAdView) wordList.get(i);
                            adView.setAdUnitId("ca-app-pub-6411761147229517/6314261957");
                            AdSize adSize = new AdSize(AdSize.FULL_WIDTH, 133);
                            adView.setAdSize(adSize);
                            adView.setAdListener(new AdListener() {
                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                    adView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAdFailedToLoad(int errorCode) {
                                    adView.setVisibility(View.GONE);
                                }
                            });
                            adView.loadAd(new AdRequest.Builder().addTestDevice("0917FA1DDAD4C1B9A1B8A66FA56661A8").build());
                        }
                    }
                }
            });
        }

        adapter = new DictionaryAdapter(activity, wordList, tts);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        title = word;
        activity.setTitle(title);
    }


    @SuppressLint("StaticFieldLeak")
    public class WordsAsync extends AsyncTask<String, Void, ArrayList<Object>> {
        ArrayList<Object> wordItemsList = new ArrayList<>();
        String word, method;
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Response response = null;

        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            word = params[0];
            method = params[1];

            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWords.setVisibility(View.VISIBLE);
                        recyclerView.setClickable(false);
                        recyclerView.setEnabled(false);
                        recyclerView.setFocusable(false);
                        recyclerView.setLayoutFrozen(true);

                    }
                });

            if (wordItemsList == null) wordItemsList = new ArrayList<>();
            else wordItemsList.clear();

            String query = word.replaceAll("\\s", "+").replaceAll(" ", "+").replace("#","%23")
                    .replace("@","%40").replace("&", "%26");

            builder.url("https://api.datamuse.com/words?md=pds&max=400&" + method + "=" + query);

            try {
                if (response != null) response.close();
            } catch (Exception ignored){}
            try {
                response = client.newCall(builder.build()).execute();
                if (response.code() == 200) {
                    Type type = new TypeToken<List<WordItem>>(){}.getType();
                    wordItemsList = new Gson().fromJson(response.body().string(), type);
                }
            } catch (final Exception e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(activity, "Error occurred: " + e.getStackTrace()[1].toString(), Toast.LENGTH_LONG).show();
                        } catch (Exception ignored) {}
                    }
                });
                Log.e("AWAISKING_APP", "", e);
            } finally {
                if (response != null) response.close();
            }

            return wordItemsList;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> wordItems) {
            if (wordItems != null) if (fragmentCallback!=null) fragmentCallback.done(wordItems, word);
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWords.setVisibility(View.GONE);
                        recyclerView.setClickable(true);
                        recyclerView.setEnabled(true);
                        recyclerView.setFocusable(true);
                        recyclerView.setLayoutFrozen(false);
                    }
                });
            super.onPostExecute(wordItems);
        }

    }
}
