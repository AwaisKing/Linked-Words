package awais.backworddictionary.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

public class MenuCaller {
    private final AppCompatActivity activity;
    private final MenuDialog menuDialog;
    private static SpannableStringBuilder examplesSpan, helpSpan, licensesSpan;
    private static CustomTabsIntent.Builder customTabsIntent;

    public MenuCaller(final AppCompatActivity act) {
        activity = act;
        menuDialog = new MenuDialog();
        customTabsIntent = new CustomTabsIntent.Builder();
        new Menuer().execute(act);
    }

    public void show(@NonNull final MenuItem item) {
        menuDialog.setTitle(item.getTitle());
        switch (item.getItemId()) {
            case R.id.mExamples:
                menuDialog.setMessage(examplesSpan);
                break;
            case R.id.mHelp:
                menuDialog.setMessage(helpSpan);
                break;
            case R.id.mLicenses:
                menuDialog.setTitle(activity.getString(R.string._credits));
                menuDialog.setMessage(licensesSpan);
                break;
            case R.id.mAbout:
                menuDialog.setMessage("aboutHere");
                break;
        }
        menuDialog.show(activity.getSupportFragmentManager(), menuDialog.getTag());
    }

    private static class Menuer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(@NonNull final Object... params) {
            final Context context = (Context) params[0];

            final int helpColor = ResourcesCompat.getColor(context.getResources(), R.color.helper_color, null);

            final SpanBuilder examplesBuilder = new SpanBuilder();
            // XXX EXAMPLES
            examplesBuilder.append(context.getString(R.string.finding_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("person who makes gold\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("one who massages\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("food search\n\n", new BulletSpan(26, helpColor));
            examplesBuilder.append(context.getString(R.string.related_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("rainbow colors\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("tropical birds\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("spicy vegetables\n\n", new BulletSpan(26, helpColor));
            examplesBuilder.append(context.getString(R.string.answers_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("what's popular city of Pakistan?\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("what's largest continent on earth?\n", new BulletSpan(26, helpColor));
            examplesBuilder.append("who was Galileo?\n\n", new BulletSpan(26, helpColor));
            examplesBuilder.append(context.getString(R.string.spelled_help) + ":\n", new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
            examplesBuilder.append("l?nd\t-- " + context.getString(R.string.single_char_help, '?') + '\n', new BulletSpan(26, helpColor));
            examplesBuilder.append("fr*g\t-- " + context.getString(R.string.number_char_help, '*') + '\n', new BulletSpan(26, helpColor));
            examplesBuilder.append("ta#t\t-- " + context.getString(R.string.consonant_char_help, '#') + '\n', new BulletSpan(26, helpColor));
            examplesBuilder.append("**stone**\t-- " + context.getString(R.string.phrase_help) + '\n', new BulletSpan(26, helpColor));
            examplesSpan = examplesBuilder.build();

            // XXX HELP
            final SpanBuilder helpBuilder = new SpanBuilder();
            helpBuilder.append(context.getString(R.string.reverse)+ ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.reverse_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.sounds_like) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.sounds_like_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.spelled_help) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.wildcards_help) + '\n', new BulletSpan(26, helpColor));
            helpBuilder.append("[" + context.getString(R.string.wildcard_link_help) + "]\n\n", new BulletSpan(26, helpColor), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#FFC400"));
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://www.onelook.com/?c=faq#patterns"));
                }});
            helpBuilder.append(context.getString(R.string.synonyms) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.synonym_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.antonyms) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.antonym_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.triggers) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.triggers_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.part_of) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.is_part_of_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.comprises) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.comprises_of_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.rhymes) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.rhymes_help) + "\n\n", new BulletSpan(26, helpColor));
            helpBuilder.append(context.getString(R.string.homophones) + ":\n", new RelativeSizeSpan(1.1f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
            helpBuilder.append(context.getString(R.string.homophones_help) + "\n\n", new BulletSpan(26, helpColor));
            helpSpan = helpBuilder.build();

            // XXX LICENSES
            final SpanBuilder licensesBuilder = new SpanBuilder();
            licensesBuilder.append("App Icon:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor));
            licensesBuilder.append("Android Asset Studio - Launcher icon generator\n\n", new BulletSpan(26, helpColor), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#607d8b"));
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://romannurik.github.io/AndroidAssetStudio/"));
                }});
            licensesBuilder.append("Dictionary API:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor));
            licensesBuilder.append("Datamuse API\n\n", new BulletSpan(26, helpColor), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#006fcc"));
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://www.datamuse.com/api/"));
                }});
            licensesBuilder.append("Libraries:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor));
            licensesBuilder.append("OkHttp3 [Apache License 2.0]\n", new BulletSpan(26, helpColor));
            licensesBuilder.append("SearchView [Apache License 2.0]\n", new BulletSpan(26, helpColor));
            licensesBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n", new BulletSpan(26, helpColor));
            licensesBuilder.append("Expandable FAB [Apache License 2.0]\n\n", new BulletSpan(26, helpColor));

            licensesBuilder.append("License:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.1f), new ForegroundColorSpan(helpColor));
            licensesBuilder.append("Apache License 2.0", new BulletSpan(26, helpColor), new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#cb2533"));
                    CustomTabActivityHelper.openCustomTab(context, customTabsIntent.build(),
                            Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"));
                }});
            licensesSpan = licensesBuilder.build();

            return null;
        }
    }

    private final static class SpanBuilder {
        private final ArrayList<SpanSection> spanSections;
        private final StringBuilder stringBuilder;

        private final static class SpanSection {
            private final String text;
            private final int startIndex;
            private final Object[] styles;

            private SpanSection(final String text, final int startIndex, final Object... styles) {
                this.styles = styles;
                this.text = text;
                this.startIndex = startIndex;
            }

            private void apply(final SpannableStringBuilder spanStringBuilder) {
                if (spanStringBuilder == null) return;
                for (final Object style : styles)
                    spanStringBuilder.setSpan(style, startIndex, startIndex + text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        SpanBuilder() {
            this.stringBuilder = new StringBuilder();
            this.spanSections = new ArrayList<>();
        }

        void append(final String text, final Object... styles) {
            if (styles != null && styles.length > 0)
                spanSections.add(new SpanSection(text, stringBuilder.length(), styles));
            stringBuilder.append(text);
        }

        @NonNull
        SpannableStringBuilder build() {
            final SpannableStringBuilder ssb = new SpannableStringBuilder(stringBuilder);
            for (final SpanSection section : spanSections) section.apply(ssb);
            return ssb;
        }

        @NonNull
        @Override
        public String toString() {
            return String.valueOf(build());
        }
    }
}
