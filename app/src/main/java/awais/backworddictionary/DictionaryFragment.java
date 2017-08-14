package awais.backworddictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.Monitor;
import com.keiferstone.nonet.NoNet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DictionaryFragment extends Fragment implements FragmentCallback {
    private List<Object> wordList;
    private RecyclerView recyclerView;
    private ProgressBar progressWords;
    private FragmentCallback fragmentCallback;
    private TextToSpeech tts;
    private SmoothRefreshLayout refreshLayout;
    private Activity activity;
    private EditText filterSearchEditor;
    private boolean[] filterCheck = {true, true, true};
    public DictionaryAdapter adapter;
    public String title;

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

    void filter(String text){
        List<Object> newList = new ArrayList<>();
        newList.add(wordList.get(0));
        if (text != null && (TextUtils.isEmpty(text) || text.equals(""))) newList = wordList;
        else {
            String filterPattern = text != null ? text.toLowerCase() : "";

            Log.d("AWAISKING_APP", "filter: " + wordList.size() + " -- " + newList.size());

            for (Object mWord : wordList) {
                if (mWord.getClass() == WordItem.class) {
                    WordItem wordItem = (WordItem) mWord;
                    boolean showWords = Main.sharedPreferences.getBoolean("filterWord", false);
                    boolean showDefs = Main.sharedPreferences.getBoolean("filterDefn", false);
                    boolean contains = Main.sharedPreferences.getBoolean("filterContain", true);

                    if (showWords && showDefs) {
                        if (contains ? wordItem.getWord().toLowerCase().contains(filterPattern) :
                                wordItem.getWord().toLowerCase().startsWith(filterPattern)) {
                            newList.add(wordItem);
                            continue;
                        }
                        // TODO check for defintion search bugs  --- seems to be ok
                        if (wordItem.getDefs() != null) {
                            for (String def : wordItem.getDefs()) {
                                if (contains ? def.split("\t")[1].trim().toLowerCase().contains(filterPattern)
                                        : def.split("\t")[1].trim().toLowerCase().startsWith(filterPattern)) {
                                    Log.d("AWAISKING_APP", "" + def);
                                    newList.add(wordItem);
                                    break;
                                }
                            }
                        }
                    } else if (showWords) {
                        if (contains ? wordItem.getWord().toLowerCase().contains(filterPattern) :
                                wordItem.getWord().toLowerCase().startsWith(filterPattern))
                            newList.add(wordItem);

                    } else if (showDefs) {
                        // TODO check for defintion search bugs  --- seems to be ok
                        if (wordItem.getDefs() != null)
                            for (String def : wordItem.getDefs())
                                if (contains ? def.split("\t")[1].trim().contains(filterPattern)
                                        : def.split("\t")[1].trim().toLowerCase().startsWith(filterPattern))
                                    newList.add(wordItem);
                    } else {
                        Toast.makeText(activity, "Select a filter first.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }

        adapter.updateList(newList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View magicRootView = inflater.inflate(R.layout.dictionary_view, container, false);

        fragmentCallback = this;

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        progressWords = magicRootView.findViewById(R.id.progressWords);

        wordList = new ArrayList<>();
        adapter = new DictionaryAdapter(getContext(), wordList, tts);
        adapter.setHasStableIds(true);

        recyclerView.setAdapter(adapter);

        refreshLayout = magicRootView.findViewById(R.id.smoothRefreshLayout);
        refreshLayout.setHeaderView(new SearchHeader(activity));
        refreshLayout.setEnablePinContentView(true);
        refreshLayout.setEnablePinRefreshViewWhileLoading(true);
        refreshLayout.setNestedScrollingEnabled(true);
        refreshLayout.setOverScrollDistanceRatio(1f);
        refreshLayout.setClickable(true);

        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY > 5000)
                    refreshLayout.refreshComplete(true);
                if (adapter.getItemCount() > 150 && velocityY < -5000)
                    refreshLayout.autoRefresh(true, true);
                return false;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.computeVerticalScrollOffset() <= 0 && dy < -110)
                    refreshLayout.refreshComplete(true, 0);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        filterSearchEditor = refreshLayout.findViewById(R.id.swipeSearch);
        filterSearchEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openKeyboard();
            }
        });

        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                adapter.getFilter().filter(charSequence);
                if (wordList.size() > 2)
                    filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ImageView filterSearchButton = refreshLayout.findViewById(R.id.filterSettings);
        filterSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterCheck[0] = Main.sharedPreferences.getBoolean("filterWord", false);
                filterCheck[1] = Main.sharedPreferences.getBoolean("filterDefn", false);
                filterCheck[2] = Main.sharedPreferences.getBoolean("filterContain", true);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Select The Difficulty Level");
                builder.setMultiChoiceItems(new String[]{"Words", "Definitions", "Contains"}, filterCheck,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                filterCheck[i] = b;
                                if (i == 0)
                                    Main.sharedPreferences.edit().putBoolean("filterWord", b).apply();
                                else if (i == 1)
                                    Main.sharedPreferences.edit().putBoolean("filterDefn", b).apply();
                                else if (i == 2)
                                    Main.sharedPreferences.edit().putBoolean("filterContain", b).apply();
                            }
                        });
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (wordList.size() > 2)
                            filter(filterSearchEditor.getText().toString());
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        return magicRootView;
    }


    private void openKeyboard(){
        try {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(filterSearchEditor, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
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
                            NoNet.check(activity).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build())
                                    .callback(new Monitor.Callback() {
                                        @Override
                                        public void onConnectionEvent(int connectionStatus) {
                                            if (connectionStatus != ConnectionStatus.CONNECTED)
                                                Toast.makeText(activity, "Not connected to internet.\nPlease connect to network.", Toast.LENGTH_SHORT).show();
                                        }
                                    }).start();
                        } catch (Exception e1) {
                            Toast.makeText(activity, "Error occurred" +
                                    (e1.getStackTrace() != null ? ": " + e1.getStackTrace()[1].toString()
                                    : ""), Toast.LENGTH_LONG).show();
                        }
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
