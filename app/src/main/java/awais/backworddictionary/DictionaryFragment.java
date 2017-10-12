package awais.backworddictionary;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.keiferstone.nonet.ConnectionStatus;
import com.keiferstone.nonet.NoNet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.custom.RecyclerViewFastScroller;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FilterCheck;
import awais.backworddictionary.interfaces.FragmentCallback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DictionaryFragment extends Fragment implements FragmentCallback, FilterCheck {
    private List<WordItem> wordList;
    private RecyclerView recyclerView;
    private ProgressBar progressWords;
    private FragmentCallback fragmentCallback;
    private TextToSpeech tts;
    private Activity activity;

    private EditText filterSearchEditor;
    private ImageView filterSearchButton;
    private FrameLayout filterView;
    private FloatingActionButton fab;

    private final boolean[] filterCheck = {true, true, true};
    public DictionaryAdapter adapter;
    public String title;

    private FilterCheck filterChecker;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        tts = new TextToSpeech(activity, initStatus -> {
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
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {tts.stop();} catch (Exception ignore){}
        try {tts.shutdown();} catch (Exception ignore){}
    }

    private void filter(String text) {
        List<WordItem> newList = new ArrayList<>();

        if (text != null && (TextUtils.isEmpty(text) || text.equals(""))) newList = wordList;
        else {
            String filterPattern = text != null ? text.toLowerCase() : "";

            for (WordItem mWord : wordList) {
                boolean showWords = Main.sharedPreferences.getBoolean("filterWord", false);
                boolean showDefs = Main.sharedPreferences.getBoolean("filterDefinition", false);
                boolean contains = Main.sharedPreferences.getBoolean("filterContain", true);

                if (showWords && showDefs) {
                    if (contains ? mWord.getWord().toLowerCase().contains(filterPattern) :
                            mWord.getWord().toLowerCase().startsWith(filterPattern)) {
                        newList.add(mWord);
                        continue;
                    }
                    // TODO check for definition search bugs  --- seems to be ok
                    if (mWord.getDefs() != null) {
                        for (String def : mWord.getDefs()) {
                            if (contains ? def.split("\t")[1].trim().toLowerCase().contains(filterPattern)
                                    : def.split("\t")[1].trim().toLowerCase().startsWith(filterPattern)) {
                                newList.add(mWord);
                                break;
                            }
                        }
                    }
                }

                else if (showWords) {
                    if (contains ? mWord.getWord().toLowerCase().contains(filterPattern) :
                            mWord.getWord().toLowerCase().startsWith(filterPattern))
                        newList.add(mWord);

                }

                else if (showDefs) {
                    // TODO check for definition search bugs  --- seems to be ok
                    if (mWord.getDefs() != null)
                        for (String def : mWord.getDefs())
                            if (contains ? def.split("\t")[1].trim().contains(filterPattern)
                                    : def.split("\t")[1].trim().toLowerCase().startsWith(filterPattern)) {
                                newList.add(mWord);
                                break;
                            }
                }

                else {
                    Toast.makeText(activity, "Select a filter first.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Log.d("AWAISKING_APP", "filter: " + wordList.size() + " -- " + newList.size());
        adapter.updateList(newList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View magicRootView = inflater.inflate(R.layout.dictionary_view, container, false);

        AdView adView = magicRootView.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());

        fragmentCallback = this;

        progressWords = magicRootView.findViewById(R.id.progressWords);

        wordList = new ArrayList<>();
        adapter = new DictionaryAdapter(getContext(), wordList, tts);
        adapter.setHasStableIds(true);

        RecyclerViewFastScroller fastScroller = magicRootView.findViewById(R.id.fastscroller);
        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1) fastScroller.setVisibility(View.GONE);
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                fastScroller.setVisibility(wordList.size() > itemsShown ? View.VISIBLE : View.GONE);
            }
        });
        fastScroller.setRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        filterChecker = this;

        filterView = magicRootView.findViewById(R.id.filterView);
        ImageView filterBackButton = magicRootView.findViewById(R.id.filterBack);
        filterBackButton.setOnClickListener(view -> filterChecker.isOpen(false, fab, 0));
        filterSearchEditor = magicRootView.findViewById(R.id.swipeSearch);
        filterSearchButton = magicRootView.findViewById(R.id.filterSettings);
        filterSearchEditor.setOnClickListener(view -> openKeyboard());
        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (wordList.size() > 2)
                    filter(charSequence.toString());
            }

            @SuppressLint("PrivateResource")
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    filterSearchButton.setImageResource(R.drawable.abc_ic_clear_material);
                    filterSearchButton.setTag("clear");
                } else {
                    filterSearchButton.setImageResource(R.drawable.ic_settings);
                    filterSearchButton.setTag("filter");
                }
            }
        });
        filterSearchButton.setOnClickListener(view -> {
            if (filterSearchButton.getTag() != null &&
                    !TextUtils.isEmpty((CharSequence) filterSearchButton.getTag())
                    && filterSearchButton.getTag().equals("filter")) {
                filterCheck[0] = Main.sharedPreferences.getBoolean("filterWord", false);
                filterCheck[1] = Main.sharedPreferences.getBoolean("filterDefinition", false);
                filterCheck[2] = Main.sharedPreferences.getBoolean("filterContain", true);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Select The Difficulty Level");
                builder.setMultiChoiceItems(new String[]{"Words", "Definitions", "Contains"}, filterCheck,
                        (dialogInterface, i, b) -> {
                            filterCheck[i] = b;
                            if (i == 0)
                                Main.sharedPreferences.edit().putBoolean("filterWord", b).apply();
                            else if (i == 1)
                                Main.sharedPreferences.edit().putBoolean("filterDefinition", b).apply();
                            else if (i == 2)
                                Main.sharedPreferences.edit().putBoolean("filterContain", b).apply();
                        });
                builder.setNeutralButton("OK", (dialogInterface, i) -> {
                    if (wordList.size() > 2)
                        filter(filterSearchEditor.getText().toString());
                    dialogInterface.dismiss();
                });
                builder.create().show();
            } else {
                filterSearchEditor.setText("");
                filterSearchButton.setTag("filter");
            }
        });

        return magicRootView;
    }


    private void openKeyboard(){
        try {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.showSoftInput(filterSearchEditor, 1);
        } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
    }

    public void startWords(int method, String word) {
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
    public void done(ArrayList<WordItem> items, final String word) {
        wordList = items;
        adapter = new DictionaryAdapter(activity, wordList, tts);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        title = word;
        activity.setTitle(title);
    }

    @Override
    public void isOpen(boolean opened, FloatingActionButton fab, int method) {
        this.fab = fab;
        if (opened) {
            if (method == 0) filterView.setVisibility(View.VISIBLE);
            hideFAB();
        } else {
            if (method == 0) filterView.setVisibility(View.GONE);
            showFAB();
        }
    }
    private void hideFAB() {
        fab.animate().cancel();
        fab.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationEnd(Animator animation) {
                        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                        p.setAnchorId(View.NO_ID);
                        fab.setLayoutParams(p);
                        fab.setVisibility(View.GONE);
                    }
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }
    private void showFAB() {
        fab.animate().cancel();
        fab.animate().scaleX(0f).scaleY(0f);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(R.id.appbarlayout);
        fab.setLayoutParams(p);
        fab.setVisibility(View.VISIBLE);
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {}

                    @Override public void onAnimationEnd(Animator animation) {
                        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                        p.setAnchorId(R.id.appbarlayout);
                        p.anchorGravity = Gravity.END|Gravity.BOTTOM;
                        fab.setLayoutParams(p);
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override public void onAnimationCancel(Animator animation) {}

                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }

    @SuppressLint("StaticFieldLeak")
    public class WordsAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
        ArrayList<WordItem> wordItemsList = new ArrayList<>();
        String word, method;
        final OkHttpClient client = new OkHttpClient();
        final Request.Builder builder = new Request.Builder();
        Response response = null;

        @Override
        protected ArrayList<WordItem> doInBackground(String... params) {
            word = params[0];
            method = params[1];

            if (activity != null)
                activity.runOnUiThread(() -> {
                    progressWords.setVisibility(View.VISIBLE);
                    recyclerView.setClickable(false);
                    recyclerView.setEnabled(false);
                    recyclerView.setFocusable(false);
                    recyclerView.setLayoutFrozen(true);

                });

            if (wordItemsList == null) wordItemsList = new ArrayList<>();
            else wordItemsList.clear();

            String query = word.replaceAll("\\s", "+").replaceAll(" ", "+").replace("#","%23")
                    .replace("@","%40").replace("&", "%26");

            int wordsCount = Main.sharedPreferences.getInt("maxWords", 400);

            builder.url("https://api.datamuse.com/words?md=pds&max=" + wordsCount + "&" + method + "=" + query);

            try {
                if (response != null) response.close();
            } catch (Exception ignored){}
            try {
                response = client.newCall(builder.build()).execute();
                if (response.code() == 200)
                    wordItemsList = new Gson().fromJson(response.body().string(),
                            new TypeToken<List<WordItem>>(){}.getType());
            } catch (final Exception e) {
                if (activity != null)
                    activity.runOnUiThread(() -> {
                        try {
                            NoNet.check(activity).configure(NoNet.configure().endpoint("https://api.datamuse.com/words").build())
                                    .callback(connectionStatus -> {
                                        if (connectionStatus != ConnectionStatus.CONNECTED)
                                            Toast.makeText(activity, "Not connected to internet.\nPlease connect to network.", Toast.LENGTH_SHORT).show();
                                    }).start();
                        } catch (Exception e1) {
                            Toast.makeText(activity, "Error occurred" +
                                    (e1.getStackTrace() != null ? ": " + e1.getStackTrace()[1].toString()
                                            : ""), Toast.LENGTH_LONG).show();
                        }
                    });
                Log.e("AWAISKING_APP", "", e);
            } finally {
                if (response != null) response.close();
            }

            return wordItemsList;
        }

        @Override
        protected void onPostExecute(ArrayList<WordItem> wordItems) {
            if (wordItems != null) if (fragmentCallback!=null) fragmentCallback.done(wordItems, word);
            if (activity != null)
                activity.runOnUiThread(() -> {
                    progressWords.setVisibility(View.GONE);
                    recyclerView.setClickable(true);
                    recyclerView.setEnabled(true);
                    recyclerView.setFocusable(true);
                    recyclerView.setLayoutFrozen(false);
                });
            super.onPostExecute(wordItems);
        }

    }
}
