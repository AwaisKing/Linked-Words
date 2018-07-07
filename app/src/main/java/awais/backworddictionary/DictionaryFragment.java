package awais.backworddictionary;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
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

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.interfaces.FilterCheck;
import awais.backworddictionary.interfaces.FragmentCallback;

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
                } else if (showDefs) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        final View magicRootView = inflater.inflate(R.layout.dictionary_view, container, false);

        AdView adView = magicRootView.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());

        fragmentCallback = this;

        progressWords = magicRootView.findViewById(R.id.progressWords);

        wordList = new ArrayList<>();
        adapter = new DictionaryAdapter(getContext(), wordList, tts);
        adapter.setHasStableIds(true);

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        FastScroller fastScroller = magicRootView.findViewById(R.id.fastscroller);
        fastScroller.setHandleColor(getResources().getColor(R.color.colorAccent));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
        fastScroller.setRecyclerView(recyclerView);

        filterChecker = this;

        filterView = magicRootView.findViewById(R.id.filterView);
        ImageView filterBackButton = magicRootView.findViewById(R.id.filterBack);
        filterBackButton.setOnClickListener(view -> filterChecker.isOpen(false, fab, 0));
        filterSearchEditor = magicRootView.findViewById(R.id.swipeSearch);
        filterSearchButton = magicRootView.findViewById(R.id.filterSettings);
        filterSearchButton.setTag("filter");
        filterSearchEditor.setOnClickListener(view -> openKeyboard());
        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (wordList.size() > 2) filter(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    filterSearchButton.setImageResource(R.drawable.ic_clear);
                    filterSearchButton.setTag("clear");
                } else {
                    filterSearchButton.setImageResource(R.drawable.ic_settings);
                    filterSearchButton.setTag("filter");
                }
            }
        });
        filterSearchButton.setOnClickListener(view -> {
            if (filterSearchButton.getTag() != null && !TextUtils.isEmpty((CharSequence) filterSearchButton.getTag())
                    && filterSearchButton.getTag().equals("filter")) {
                filterCheck[0] = Main.sharedPreferences.getBoolean("filterWord", false);
                filterCheck[1] = Main.sharedPreferences.getBoolean("filterDefinition", false);
                filterCheck[2] = Main.sharedPreferences.getBoolean("filterContain", true);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Select The Difficulty Level");
                builder.setMultiChoiceItems(new String[]{"Words", "Definitions", "Contains"}, filterCheck,
                        (dialogInterface, i, b) -> {
                            filterCheck[i] = b;
                            String item = "";
                            switch (i) {
                                case 0: item = "filterWord"; break;
                                case 1: item = "filterDefinition"; break;
                                case 2: item = "filterContain"; break;
                            }
                            Main.sharedPreferences.edit().putBoolean(item, b).apply();
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
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.showSoftInput(filterSearchEditor, 1);
        } catch (Exception e) { Log.e("AWAISKING_APP", "", e); }
    }

    public void startWords(int method, String word) {
        new WordsAsync(activity, fragmentCallback, word, method, progressWords, recyclerView)
                .execute();
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
            if (method == 0 && filterView != null) filterView.setVisibility(View.VISIBLE);
            if (method == 30 && filterView != null) filterView.setVisibility(View.GONE);
            hideFAB();
        } else {
            if (method == 0 && filterView != null) filterView.setVisibility(View.GONE);
            if (method == 30 && filterView != null) filterView.setVisibility(View.VISIBLE);
            showFAB();
        }
    }
    private void hideFAB() {
        if (fab == null) return;
        fab.animate().cancel();
        fab.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationEnd(Animator animation) {
                        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                        p.setAnchorId(View.NO_ID);
                        fab.setLayoutParams(p);
                        fab.hide();
                    }
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }
    private void showFAB() {
        if (fab == null) return;
        fab.animate().cancel();
        fab.animate().scaleX(0f).scaleY(0f);
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(R.id.appbarlayout);
        fab.setLayoutParams(p);
        fab.show();
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationEnd(Animator animation) {
                        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                        p.setAnchorId(R.id.appbarlayout);
                        p.anchorGravity = Gravity.END | Gravity.BOTTOM;
                        fab.setLayoutParams(p);
                        fab.show();
                    }
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}
                });
    }
}
