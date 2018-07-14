package awais.backworddictionary.custom;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;

import awais.backworddictionary.R;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

public class MenuCaller {
    private final AppCompatActivity activity;
    private final MenuDialog bottomSheetDialogFragment;
    private static SpannableStringBuilder examplesBuilder, helpBuilder, licensesBuilder;
    private static CustomTabsIntent.Builder customTabsIntent;

    public MenuCaller(AppCompatActivity act) {
        activity = act;
        bottomSheetDialogFragment = new MenuDialog();
        helpBuilder = new SpannableStringBuilder();
        examplesBuilder = new SpannableStringBuilder();
        licensesBuilder = new SpannableStringBuilder();
        customTabsIntent = new CustomTabsIntent.Builder();
        new Menuer().execute(act);
    }

    public void show(MenuItem item) {
        bottomSheetDialogFragment.setTitle(item.getTitle());
        switch (item.getItemId()) {
            case R.id.mExamples: bottomSheetDialogFragment.setMessage(examplesBuilder); break;
            case R.id.mHelp: bottomSheetDialogFragment.setMessage(helpBuilder); break;
            case R.id.mLicenses: bottomSheetDialogFragment.setMessage(licensesBuilder);
                bottomSheetDialogFragment.setTitle(item.getTitle() + " & Credits");break;
            case R.id.mAbout: bottomSheetDialogFragment.setMessage("aboutHere"); break;
        }
        bottomSheetDialogFragment.show(activity.getSupportFragmentManager(),
                bottomSheetDialogFragment.getTag());
    }

    private static class Menuer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Activity activity = (Activity) params[0];

            // XXX EXAMPLES
            examplesBuilder.append("Finding a word by its definition:\n");
            examplesBuilder.append("person who makes gold\n");
            examplesBuilder.append("one who massages\n");
            examplesBuilder.append("food search\n\n");
            examplesBuilder.append("Finding related words:\n");
            examplesBuilder.append("rainbow colors\n");
            examplesBuilder.append("tropical birds\n");
            examplesBuilder.append("spicy vegetables\n\n");
            examplesBuilder.append("Finding answers:\n");
            examplesBuilder.append("what's popular city of Pakistan?\n");
            examplesBuilder.append("what's largest continent on earth?\n");
            examplesBuilder.append("who was Galileo?\n\n");
            examplesBuilder.append("Wildcards [Spelled Like]:\n");
            examplesBuilder.append("l?nd\t-- here ? matches any single character.\n");
            examplesBuilder.append("fr*k\t-- here * matches any number of characters.\n");
            examplesBuilder.append("ta#t\t-- here # matches any English consonant.\n");
            examplesBuilder.append("**stone**\t-- find phrases with stone word in it.\n");
            examplesBuilder.append("te@s\t-- here @ matches any English vowel.");
            examplesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 34,51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 56, 72, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 73, 84, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new RelativeSizeSpan(1.1f), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 86, 108, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 109, 123, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 124, 138, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 139, 155, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new RelativeSizeSpan(1.1f), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 157, 173, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 174, 206, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 207, 241, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 242, 258, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new RelativeSizeSpan(1.1f), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 260, 285, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 286, 331, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 331, 380, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 380, 426, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 426, 469, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            examplesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 475, 516, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // XXX HELP
            helpBuilder.append("Reverse:\n");
            helpBuilder.append("find related words, phrases, answers by a word or by meaning\n\n");
            helpBuilder.append("Sounds Like:\n");
            helpBuilder.append("find words which sound similar to given word\n\n");
            helpBuilder.append("Spelled Like:\n");
            helpBuilder.append("find words which are spelled like defined word with wildcards\n");
            helpBuilder.append("[Wildcard Help]");
            helpBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new BulletSpan(26, 0xFF212121), 9,69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new StyleSpan(Typeface.BOLD), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new RelativeSizeSpan(1.1f), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 71, 83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new BulletSpan(26, 0xFF212121), 84,128, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new StyleSpan(Typeface.BOLD), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new RelativeSizeSpan(1.1f), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 130, 143, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new BulletSpan(26, 0xFF212121), 144,221, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            helpBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#FFC400"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.onelook.com/?c=faq#patterns"));
                }}, 206, 221, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // XXX LICENSES
            licensesBuilder.append("App Icon:\n");
            licensesBuilder.append("Android Asset Studio - Launcher icon generator\n\n");
            licensesBuilder.append("Dictionary API:\n");
            licensesBuilder.append("Datamuse API\n\n");
            licensesBuilder.append("Libraries:\n");
            licensesBuilder.append("OkHttp3 [Apache License 2.0]\n");
            licensesBuilder.append("GSON [Apache License 2.0]\n");
            licensesBuilder.append("SearchView [Apache License 2.0]\n");
            licensesBuilder.append("Chrome Custom Tabs [Apache License 2.0]\n");
            licensesBuilder.append("NoNet [Apache License 2.0]\n");
            licensesBuilder.append("LollipopContactsRecyclerViewFastScroller [Apache License 2.0]\n\n");
            licensesBuilder.append("License:\n");
            licensesBuilder.append("Apache License 2.0");
            licensesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 10,56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#607d8b"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://romannurik.github.io/AndroidAssetStudio/"));
                }}, 10,56, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new RelativeSizeSpan(1.1f), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 58, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 74,86, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#006fcc"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("http://www.datamuse.com/api/"));
                }},74,86, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new RelativeSizeSpan(1.1f), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 88, 98, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 99,127, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 128,153, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 154,185, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 186,225, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 226,252, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 253,314, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new StyleSpan(Typeface.BOLD), 316, 324, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new RelativeSizeSpan(1.1f), 316, 324, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ForegroundColorSpan(0xFF212121), 316, 324, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new BulletSpan(26, 0xFF212121), 325,343, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            licensesBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    customTabsIntent.setToolbarColor(Color.parseColor("#cb2533"));
                    CustomTabActivityHelper.openCustomTab(activity, customTabsIntent.build(),
                            Uri.parse("https://www.apache.org/licenses/LICENSE-2.0"));
                }},325,343, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return null;
        }
    }
}
