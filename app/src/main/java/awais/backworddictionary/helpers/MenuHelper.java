package awais.backworddictionary.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import com.applovin.sdk.AppLovinSdk;

import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.dialogs.MenuDialog;
import awais.backworddictionary.executors.LocalAsyncTask;
import awais.backworddictionary.helpers.other.CustomTabActivityHelper;

public final class MenuHelper {
    private static SpannableStringBuilder examplesSpan, helpSpan, licensesSpan;
    private static CustomTabsHelper customTabsHelper;
    private final Context context;
    private final MenuDialog menuDialog;
    private final String creditsString;
    private final FragmentManager fragmentManager;

    public MenuHelper(@NonNull final AppCompatActivity activity) {
        final Resources resources = activity.getResources();
        creditsString = resources.getString(R.string._credits);
        fragmentManager = activity.getSupportFragmentManager();
        menuDialog = new MenuDialog();
        customTabsHelper = new CustomTabsHelper();
        context = activity;

        final boolean helpEmpty = helpSpan == null || helpSpan.length() < 1;
        final boolean examplesEmpty = examplesSpan == null || examplesSpan.length() < 1;
        final boolean licensesEmpty = licensesSpan == null || licensesSpan.length() < 1;

        if (helpEmpty || examplesEmpty || licensesEmpty) new LocalAsyncTask<Void, Void>() {
            @Nullable
            @Override
            protected Void doInBackground(final Void param) {
                final int helpColor = ResourcesCompat.getColor(resources, R.color.helper_color, null);

                if (examplesEmpty) {
                    // XXX EXAMPLES
                    final SpanBuilder examplesBuilder = new SpanBuilder();
                    examplesBuilder.append(activity.getString(R.string.finding_help).concat(":\n"), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
                    examplesBuilder.append("person who makes gold\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("one who massages\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("food search\n\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append(activity.getString(R.string.related_help).concat(":\n"), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
                    examplesBuilder.append("rainbow colors\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("tropical birds\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("spicy vegetables\n\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append(activity.getString(R.string.answers_help).concat(":\n"), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
                    examplesBuilder.append("what's popular city of Pakistan?\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("what's largest continent on earth?\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append("who was Galileo?\n\n", new BulletSpan(26, helpColor));
                    examplesBuilder.append(activity.getString(R.string.spelled_help).concat(":\n"), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor), new StyleSpan(Typeface.BOLD));
                    examplesBuilder.append("l?nd\t-- " + activity.getString(R.string.single_char_help, '?') + '\n', new BulletSpan(26, helpColor));
                    examplesBuilder.append("fr*g\t-- " + activity.getString(R.string.number_char_help, '*') + '\n', new BulletSpan(26, helpColor));
                    examplesBuilder.append("ta#t\t-- " + activity.getString(R.string.consonant_char_help, '#') + '\n', new BulletSpan(26, helpColor));
                    examplesBuilder.append("**stone**\t-- " + activity.getString(R.string.phrase_help) + '\n', new BulletSpan(26, helpColor));
                    examplesSpan = examplesBuilder.build();
                }

                if (helpEmpty) {
                    // XXX HELP
                    final SpanBuilder helpBuilder = new SpanBuilder();
                    helpBuilder.append(activity.getString(R.string.reverse).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.reverse_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.sounds_like).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.sounds_like_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.spelled_help).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.wildcards_help) + '\n', new BulletSpan(26, helpColor));
                    helpBuilder.append("[" + activity.getString(R.string.wildcard_link_help).concat("]\n\n"), new BulletSpan(26, helpColor), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull final View view) {
                            CustomTabActivityHelper.openCustomTab(activity, customTabsHelper.setToolbarColor(0xFFFFC400),
                                                                  Uri.parse("https://www.one".concat("look.com/?c=faq#patterns")));
                        }
                    });
                    helpBuilder.append(activity.getString(R.string.synonyms).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.synonym_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.antonyms).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.antonym_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.triggers).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.triggers_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.part_of).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.is_part_of_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.comprises).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.comprises_of_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.rhymes).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.rhymes_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpBuilder.append(activity.getString(R.string.homophones).concat(":\n"), new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD), new ForegroundColorSpan(helpColor));
                    helpBuilder.append(activity.getString(R.string.homophones_help).concat("\n\n"), new BulletSpan(26, helpColor));
                    helpSpan = helpBuilder.build();
                }

                if (licensesEmpty) {
                    // XXX LICENSES
                    final SpanBuilder licensesBuilder = new SpanBuilder();
                    licensesBuilder.append("App Icon:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor));
                    licensesBuilder.append("Android Asset Studio - Launcher icon generator\n\n", new BulletSpan(26, helpColor), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull final View view) {
                            CustomTabActivityHelper.openCustomTab(activity, customTabsHelper.setToolbarColor(0xFF607D8B),
                                                                  Uri.parse("https://romannurik.github.io/AndroidAssetStudio/"));
                        }
                    });

                    licensesBuilder.append("Dictionary API:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor));
                    licensesBuilder.append("Datamuse API\n\n", new BulletSpan(26, helpColor), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull final View view) {
                            CustomTabActivityHelper.openCustomTab(activity, customTabsHelper.setToolbarColor(0xFF006FCC),
                                                                  Uri.parse("https://www.datamuse.com/api/"));
                        }
                    });

                    licensesBuilder.append("Libraries:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor));
                    licensesBuilder.append("SearchView [Apache License 2.0]\n", new BulletSpan(26, helpColor));
                    licensesBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n", new BulletSpan(26, helpColor));
                    licensesBuilder.append("Expandable FAB [Apache License 2.0]\n\n", new BulletSpan(26, helpColor));

                    licensesBuilder.append("License:\n", new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.2f), new ForegroundColorSpan(helpColor));
                    licensesBuilder.append("Apache License 2.0", new BulletSpan(26, helpColor), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull final View view) {
                            CustomTabActivityHelper.openCustomTab(activity, customTabsHelper.setToolbarColor(0xFFCB2533),
                                                                  Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"));
                        }
                    });

                    licensesSpan = licensesBuilder.build();
                }

                return null;
            }
        }.execute();
    }

    public void show(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();
        menuDialog.setTitle(itemId == R.id.mLicenses ? creditsString : item.getTitle());
        if (itemId == R.id.mExamples) menuDialog.setMessage(examplesSpan);
        else if (itemId == R.id.mHelp) menuDialog.setMessage(helpSpan);
        else if (itemId == R.id.mLicenses) menuDialog.setMessage(licensesSpan);
        else if (itemId == R.id.mAbout) {
            if (BuildConfig.DEBUG && context != null) AppLovinSdk.getInstance(context).showMediationDebugger();
            else menuDialog.setMessage("aboutHere");
        }
        menuDialog.show(fragmentManager, null);
    }

    private final static class SpanBuilder {
        private final ArrayList<SpanSection> spanSections = new ArrayList<>();
        private final StringBuilder stringBuilder = new StringBuilder();

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

        private record SpanSection(String text, int startIndex, Object... styles) {
            private void apply(final SpannableStringBuilder spanStringBuilder) {
                if (spanStringBuilder == null) return;
                for (final Object style : styles)
                    spanStringBuilder.setSpan(style, startIndex, startIndex + text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
    }
}