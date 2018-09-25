package awais.backworddictionary.custom;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

public class MenuCaller {
    private final AppCompatActivity activity;
    private final MenuDialog menuDialog;
    private static SpanBuilder examplesBuilder, helpBuilder, licensesBuilder;
    private static CustomTabsIntent.Builder customTabsIntent;

    public MenuCaller(AppCompatActivity act) {
        activity = act;
        menuDialog = new MenuDialog();
        helpBuilder = new SpanBuilder();
        examplesBuilder = new SpanBuilder();
        licensesBuilder = new SpanBuilder();
        customTabsIntent = new CustomTabsIntent.Builder();
        new Menuer().execute(act);
    }

    public void show(MenuItem item) {
        menuDialog.setTitle(item.getTitle());
        switch (item.getItemId()) {
            case R.id.mExamples: menuDialog.setMessage(examplesBuilder.build()); break;
            case R.id.mHelp: menuDialog.setMessage(helpBuilder.build()); break;
            case R.id.mLicenses: menuDialog.setMessage(licensesBuilder.build());
                menuDialog.setTitle(item.getTitle() + " & Credits");break;
            case R.id.mAbout: menuDialog.setMessage("aboutHere"); break;
        }
        menuDialog.show(activity.getSupportFragmentManager(),
                menuDialog.getTag());
    }

    private static class Menuer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Activity activity = (Activity) params[0];

            // XXX EXAMPLES
            examplesBuilder.append("Finding a word by its definition:\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("person who makes gold\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("one who massages\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("food search\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("Finding related words:\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("rainbow colors\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("tropical birds\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("spicy vegetables\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("Finding answers:\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("what's popular city of Pakistan?\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("what's largest continent on earth?\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("who was Galileo?\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("Wildcards [Spelled Like]:\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("l?nd\t-- here ? matches any single character.\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("fr*k\t-- here * matches any number of characters.\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("ta#t\t-- here # matches any English consonant.\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("**stone**\t-- find phrases with stone word in it.\n", new BulletSpan(26, 0xFF212121));

            // XXX HELP
            helpBuilder = new SpanBuilder();
            helpBuilder.append("Reverse:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find related words, phrases, answers by a word or by meaning\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Sounds Like:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which sound similar to given word\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Spelled Like:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which are spelled like defined word with wildcards\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("[Wildcard Help]\n\n", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#FFC400"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.onelook.com/?c=faq#patterns"));
                }});
            helpBuilder.append("Synonyms:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find synonyms of given word (similar to Reverse function, but more category specific)\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Antonyms:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find antonyms of give word\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Triggers:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which suit the given word or are associated with the word in paragraph or text\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Is part of:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which are part of or are meronyms of given word\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Comprises of:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which is/are part(s) or component(s) of given word\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Rhymes:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which perfectly rhyme with given word\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append("Homophones:\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append("find words which sound exactly alike\n\n", new BulletSpan(26, 0xFF212121));

            // XXX LICENSES
            licensesBuilder = new SpanBuilder();
            licensesBuilder.append("App Icon:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121));
            licensesBuilder.append("Android Asset Studio - Launcher icon generator\n\n", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#607d8b"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://romannurik.github.io/AndroidAssetStudio/"));
                }});
            licensesBuilder.append("Dictionary API:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121));
            licensesBuilder.append("Datamuse API\n\n", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#006fcc"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.datamuse.com/api/"));
                }});
            licensesBuilder.append("Libraries:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121));
            licensesBuilder.append("OkHttp3 [Apache License 2.0]\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("GSON [Apache License 2.0]\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("SearchView [Apache License 2.0]\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("NoNet [Apache License 2.0]\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("License:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121));
            licensesBuilder.append("Apache License 2.0", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#cb2533"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"));
                }});

            return null;
        }
    }

    private static class SpanBuilder {
        private final ArrayList<SpanSection> spanSections;
        private final StringBuilder stringBuilder;

        private class SpanSection {
            private final String text;
            private final int startIndex;
            private final Object[] styles;

            private SpanSection(String text, int startIndex, Object... styles){
                this.styles = styles;
                this.text = text;
                this.startIndex = startIndex;
            }
            private void apply(SpannableStringBuilder spanStringBuilder){
                if (spanStringBuilder == null) return;
                for (Object style : styles)
                    spanStringBuilder.setSpan(style, startIndex, startIndex + text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        SpanBuilder() {
            stringBuilder = new StringBuilder();
            spanSections = new ArrayList<>();
        }

        void append(String text, Object... styles) {
            if (styles != null && styles.length > 0)
                spanSections.add(new SpanSection(text, stringBuilder.length(), styles));
            stringBuilder.append(text);
        }

        SpannableStringBuilder build() {
            SpannableStringBuilder ssb = new SpannableStringBuilder(stringBuilder);
            for (SpanSection section : spanSections) section.apply(ssb);
            return ssb;
        }

        @Override
        public String toString() {
            return String.valueOf(build());
        }
    }
}
