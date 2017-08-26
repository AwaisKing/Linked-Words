package awais.backworddictionary;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.NativeExpressAdView;

import java.net.URL;
import java.util.List;

import awais.backworddictionary.customweb.CustomTabActivityHelper;
import awais.backworddictionary.customweb.WebViewFallback;

public class DictionaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private List<Object> wordList;
    private WordItem currentWord;
    private final TextToSpeech tts;

    private static final int wordType = 0;
    private static final int adType = 1;

    class DictHolder extends RecyclerView.ViewHolder {
        final TextView word;
        final TextView subtext;
        final CardView cardView;
        final ImageView overflow;

        DictHolder(View view) {
            super(view);
            word = view.findViewById(R.id.word);
            subtext = view.findViewById(R.id.subText);
            overflow = view.findViewById(R.id.overflow);
            cardView = view.findViewById(R.id.card_view);
        }
    }
    static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        NativeAdViewHolder(View view) {
            super(view);
        }
    }

    DictionaryAdapter(Context mContext, List<Object> wordList, TextToSpeech tts) {
        this.mContext = mContext;
        this.wordList = wordList;
        this.tts = tts;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 66 == 0) ? adType : wordType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case adType:
                View nativeAdView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.native_ad_item, viewGroup, false);
                return new NativeAdViewHolder(nativeAdView);
            case wordType:
            default:
                View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.word_item, viewGroup, false);
                return new DictHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case wordType:
                final DictHolder menuItemHolder = (DictHolder) holder;
                final WordItem wordItem = (WordItem) wordList.get(position);

                menuItemHolder.word.setText(wordItem.getWord());
                menuItemHolder.overflow.setTag(position);

                String[] tags = wordItem.getTags();
                StringBuilder tagsBuilder = new StringBuilder();
                if (tags != null && tags.length > 0) {
                    tagsBuilder.insert(0, "tags:");
                    for (String tag : tags) {
                        if (tag.equals("syn")) tagsBuilder.insert(5, " [synonym]");
                        if (tag.equals("prop")) tagsBuilder.insert(5, " [proper]");
                        if (tag.equals("n")) tagsBuilder.append(" noun,");
                        if (tag.equals("adj")) tagsBuilder.append(" adjective,");
                        if (tag.equals("v")) tagsBuilder.append(" verb,");
                        if (tag.equals("adv")) tagsBuilder.append(" adverb,");
                    }
                }
                if ((tags != null && tags.length > 0) && wordItem.getNumSyllables() > 0)
                    tagsBuilder.append("\nsyllables: ").append(wordItem.getNumSyllables());
                else
                    tagsBuilder.append("syllables: ").append(wordItem.getNumSyllables());
                menuItemHolder.subtext.setText(tagsBuilder.toString().replaceAll(",\nsyllables", "\nsyllables"));
                menuItemHolder.overflow.setOnClickListener(view -> showPopupMenu(menuItemHolder.overflow));
                menuItemHolder.cardView.setOnLongClickListener(view -> {
                    showPopupMenu(menuItemHolder.overflow);
                    return true;
                });
                menuItemHolder.cardView.setOnClickListener(view -> {
                    WordDialog cdd = new WordDialog((Activity) mContext, wordItem, tts);
                    cdd.show();
                    if (cdd.getWindow()!= null) cdd.getWindow().setLayout(-1, -2);
                });
                break;
            case adType:
                if (wordList.get(position).getClass() != WordItem.class) {
                    NativeAdViewHolder nativeAdViewHolder = (NativeAdViewHolder) holder;
                    NativeExpressAdView adView = (NativeExpressAdView) wordList.get(position);

                    ViewGroup adCardView = (ViewGroup) nativeAdViewHolder.itemView;
                    if (adCardView.getChildCount() > 0) adCardView.removeAllViews();
                    if (adView.getParent() != null)
                        ((ViewGroup)adView.getParent()).removeView(adView);
                    adCardView.addView(adView);
                }
        }
    }

    private void showPopupMenu(View view) {
        if (wordList.get((Integer) view.getTag()).getClass() == WordItem.class) {
            currentWord = (WordItem) wordList.get((Integer) view.getTag());
            PopupMenu popup = new PopupMenu(mContext, view);
            popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
            popup.setOnMenuItemClickListener(new WordContextItemListener());
            popup.show();
        }
    }

    class WordContextItemListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

            switch (menuItem.getItemId()) {
                case R.id.action_copy:
                    try {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null)
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("word", currentWord.getWord()));
                    } catch (Exception e) {
                        try {
                            //noinspection deprecation
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard != null) clipboard.setText(currentWord.getWord());
                        } catch (Exception ignored){}
                    }
                    return true;
                case R.id.action_speak:
                    tts.speak(currentWord.getWord(), TextToSpeech.QUEUE_FLUSH, null);
                    return true;
                case R.id.action_google:
                    String wordRawGoogle = currentWord.getWord().replace(" ", "+").replace("\\s", "+");
                    try {
                        Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent1.putExtra(SearchManager.QUERY, currentWord.getWord());
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        mContext.startActivity(intent1);
                    } catch (Exception e) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#4888f2"));
                        CustomTabActivityHelper.openCustomTab(
                                (Activity) mContext, customTabsIntent.build(), Uri.parse("https://google.com/search?q=" + wordRawGoogle),
                                new WebViewFallback());
                    }

                    return true;
                case R.id.action_wiki:
                    String wordRawWiki = currentWord.getWord().replace(" ", "_").replace("\\s", "_");
                    try {wordRawWiki = new URL(wordRawWiki).toString();} catch (Exception ignored) {}

                    Intent intent1 = new Intent();
                    intent1.setAction(Intent.ACTION_VIEW);
                    intent1.setPackage("org.wikipedia");
                    intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki));
                    List<ResolveInfo> resInfo1 = mContext.getPackageManager().queryIntentActivities(intent1, 0);
                    if (resInfo1 != null && resInfo1.size() > 0) mContext.startActivity(intent1);
                    else {
                        customTabsIntent.setToolbarColor(Color.parseColor("#333333"));
                        CustomTabActivityHelper.openCustomTab(
                                (Activity) mContext, customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/" + wordRawWiki),
                                new WebViewFallback());
                    }
                    return true;
                case R.id.action_urban:
                    customTabsIntent.setToolbarColor(Color.parseColor("#3b496b"));
                    CustomTabActivityHelper.openCustomTab(
                            (Activity) mContext, customTabsIntent.build(), Uri.parse("http://www.urbandictionary.com/define.php?term=" + currentWord.getWord()),
                            new WebViewFallback());

                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    void updateList(List<Object> list){
        wordList = list;
        notifyDataSetChanged();
    }
}