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
    private static SpannableStringBuilder examplesSpan, helpSpan, licensesSpan;
    private static CustomTabsIntent.Builder customTabsIntent;

    public MenuCaller(AppCompatActivity act) {
        activity = act;
        menuDialog = new MenuDialog();
        customTabsIntent = new CustomTabsIntent.Builder();
        new Menuer().execute(act);
    }

    public void show(MenuItem item) {
        menuDialog.setTitle(item.getTitle());
        switch (item.getItemId()) {
            case R.id.mExamples: menuDialog.setMessage(examplesSpan); break;
            case R.id.mHelp: menuDialog.setMessage(helpSpan); break;
            case R.id.mLicenses: menuDialog.setMessage(licensesSpan);
                menuDialog.setTitle(activity.getString(R.string._credits));break;
            case R.id.mAbout: menuDialog.setMessage("aboutHere"); break;
        }
        menuDialog.show(activity.getSupportFragmentManager(),
                menuDialog.getTag());
    }

    private static class Menuer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Activity activity = (Activity) params[0];

            SpanBuilder examplesBuilder = new SpanBuilder();
            // XXX EXAMPLES
            examplesBuilder.append(activity.getString(R.string.finding_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("person who makes gold\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("one who massages\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("food search\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append(activity.getString(R.string.related_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("rainbow colors\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("tropical birds\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("spicy vegetables\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append(activity.getString(R.string.answers_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("what's popular city of Pakistan?\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("what's largest continent on earth?\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("who was Galileo?\n\n", new BulletSpan(26, 0xFF212121));
            examplesBuilder.append(activity.getString(R.string.spelled_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("l?nd\t-- " + activity.getString(R.string.single_char_help, '?') + '\n', new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("fr*g\t-- " + activity.getString(R.string.number_char_help, '*') + '\n', new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("ta#t\t-- " + activity.getString(R.string.consonant_char_help, '#') + '\n', new BulletSpan(26, 0xFF212121));
            examplesBuilder.append("**stone**\t-- " + activity.getString(R.string.phrase_help) + '\n', new BulletSpan(26, 0xFF212121));
            examplesSpan = examplesBuilder.build();

            // XXX HELP
            SpanBuilder helpBuilder = new SpanBuilder();
            helpBuilder.append(activity.getString(R.string.reverse)+ ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.reverse_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.sounds_like) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.sounds_like_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.spelled_help) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.wildcards_help) + '\n', new BulletSpan(26, 0xFF212121));
            helpBuilder.append("[" + activity.getString(R.string.wildcard_link_help) + "]\n\n", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#FFC400"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.onelook.com/?c=faq#patterns"));
                }});
            helpBuilder.append(activity.getString(R.string.synonyms) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.synonym_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.antonyms) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.antonym_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.triggers) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.triggers_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.part_of) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.is_part_of_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.comprises) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.comprises_of_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.rhymes) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.rhymes_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpBuilder.append(activity.getString(R.string.homophones) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(0xFF212121));
            helpBuilder.append(activity.getString(R.string.homophones_help) + "\n\n", new BulletSpan(26, 0xFF212121));
            helpSpan = helpBuilder.build();

            // XXX LICENSES
            SpanBuilder licensesBuilder = new SpanBuilder();
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
            licensesBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n", new BulletSpan(26, 0xFF212121));
            // licensesBuilder.append("Expandable FAB [Apache License 2.0]\n\n", new BulletSpan(26, 0xFF212121));
            licensesBuilder.append("License:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(0xFF212121));
            licensesBuilder.append("Apache License 2.0", new BulletSpan(26, 0xFF212121), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#cb2533"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"));
                }});
            licensesSpan = licensesBuilder.build();

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
