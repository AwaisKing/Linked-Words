package awais.backworddictionary;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.DialogTitle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import awais.backworddictionary.customweb.CustomTabActivityHelper;
import awais.backworddictionary.customweb.WebViewFallback;

class WordDialog extends Dialog implements android.view.View.OnClickListener {
    private final WordItem wordItem;
    private final TextToSpeech tts;
    private final Activity activity;

    WordDialog(Activity act, WordItem wordItem, TextToSpeech tts) {
        super(act);
        this.activity = act;
        this.wordItem = wordItem;
        this.tts = tts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.word_dialog);

        DialogTitle alertTitle = findViewById(R.id.alertTitle);
        alertTitle.setText(wordItem.getWord());

        ListView lvDefs = findViewById(R.id.lvDefs);

        ArrayList<SpannableStringBuilder> defsStyled = new ArrayList<>();
        if (wordItem.getDefs() != null) {
            for (String item : wordItem.getDefs()) {
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                stringBuilder.append(item);
                stringBuilder.insert(0, "[");
                stringBuilder.insert(item.indexOf("\t")+1, "]");
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, item.indexOf("\t")+2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                defsStyled.add(stringBuilder);
            }
        } else defsStyled.add(SpannableStringBuilder.valueOf("No definition found..."));


        lvDefs.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                android.R.id.text1, defsStyled));

        lvDefs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SpannableStringBuilder wordItem = (SpannableStringBuilder) adapterView.getItemAtPosition(i);
                if (wordItem != null && !wordItem.toString().isEmpty() && wordItem.toString().contains("\t")) {
                    String item = wordItem.toString().replaceAll("^(.*)\\t", "");
                    try {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", item));
                        Toast.makeText(activity, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        try {
                            //noinspection deprecation
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(item);
                            Toast.makeText(activity, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                        } catch (Exception ignored){
                            Toast.makeText(activity, "Error copying to clipboard!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        Button copy = findViewById(R.id.btnCopy);
        Button speak = findViewById(R.id.btnSpeak);
        Button google = findViewById(R.id.btnGoogle);
        Button wiki = findViewById(R.id.btnWiki);
        Button urban = findViewById(R.id.btnUrban);
        Button reverse = findViewById(R.id.btnReverse);
        Button soundslike = findViewById(R.id.btnSoundsLike);

        copy.setOnClickListener(this);
        speak.setOnClickListener(this);
        google.setOnClickListener(this);
        wiki.setOnClickListener(this);
        urban.setOnClickListener(this);
        reverse.setOnClickListener(this);
        soundslike.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

        switch (v.getId()) {
            case R.id.btnCopy:
                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", wordItem.getWord()));
                    Toast.makeText(activity, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    try {
                        //noinspection deprecation
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(wordItem.getWord());
                        Toast.makeText(activity, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored){
                        Toast.makeText(activity, "Error copying to clipboard!", Toast.LENGTH_SHORT).show();
                    }
                }
                return;

            case R.id.btnSpeak:
                if (tts != null) tts.speak(wordItem.getWord(), TextToSpeech.QUEUE_FLUSH, null);
                return;
                
            case R.id.btnGoogle:
                String wordRawGoogle = wordItem.getWord().replace(" ", "+").replace("\\s", "+");
                try {
                    Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent1.putExtra(SearchManager.QUERY, wordItem.getWord());
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    activity.startActivity(intent1);
                } catch (Exception e) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#4888f2"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(), Uri.parse("https://google.com/search?q=" + wordRawGoogle),
                            new WebViewFallback());
                }
                break;

            case R.id.btnWiki:
                String wordRawWiki = wordItem.getWord().replace(" ", "_").replace("\\s", "_");
                try {wordRawWiki = new URL(wordRawWiki).toString();} catch (Exception ignored) {}

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setPackage("org.wikipedia");
                intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki));
                List<ResolveInfo> resInfo1 = activity.getPackageManager().queryIntentActivities(intent1, 0);
                if (resInfo1 != null && resInfo1.size() > 0) activity.startActivity(intent1);
                else {
                    customTabsIntent.setToolbarColor(Color.parseColor("#333333"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki),
                            new WebViewFallback());
                }
                break;

            case R.id.btnUrban:
                customTabsIntent.setToolbarColor(Color.parseColor("#3b496b"));
                CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                        Uri.parse("http://www.urbandictionary.com/define.php?term=" + wordItem.getWord()),
                        new WebViewFallback());
                break;

            case R.id.btnReverse:
                if (activity.getClass() == Main.class) {
                    ((Main)activity).adapter.getItem(0).title = wordItem.getWord();
                    ((Main)activity).viewPager.setCurrentItem(0, true);
                    ((Main)activity).onSearch(wordItem.getWord(), true);
                }
                break;

            case R.id.btnSoundsLike:
                if (activity.getClass() == Main.class) {
                    try {
                        ((Main)activity).adapter.getItem(1).title = wordItem.getWord();
                        ((Main)activity).viewPager.setCurrentItem(1, true);
                        ((Main)activity).onSearch(wordItem.getWord(), true);
                    } catch (Exception e) {
                        Log.e("AWAISKING_APP", "", e);
                    }
                }
                break;
            default: break;
        }
        dismiss();
    }
}