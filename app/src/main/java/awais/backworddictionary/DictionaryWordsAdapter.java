package awais.backworddictionary;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.custom.Utils;
import awais.backworddictionary.custom.WordDialog;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.customweb.CustomTabActivityHelper;

import static awais.backworddictionary.Main.tts;

public class DictionaryWordsAdapter extends RecyclerView.Adapter<DictionaryWordsAdapter.DictHolder> implements Filterable {
    private final Context mContext;
    private final List<WordItem> wordList;
    private List<?> filterList;
    private WordItem currentWord;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                results.values = wordList;
                if (Utils.isEmpty(charSequence))
                    return results;

                boolean showWords = Main.sharedPreferences.getBoolean("filterWord", true);
                boolean showDefs = Main.sharedPreferences.getBoolean("filterDefinition", false);
                boolean contains = Main.sharedPreferences.getBoolean("filterContain", true);

                if (!showDefs && !showWords && mContext != null) {
                    Toast.makeText(mContext, mContext.getString(R.string.select_filter_first), Toast.LENGTH_SHORT).show();
                    return results;
                }

                List<WordItem> filteredList = new ArrayList<>();
                for (WordItem mWord : wordList) {
                    String word = mWord.getWord().toLowerCase();
                    if (showWords && showDefs) {
                        if (contains ? word.contains(charSequence) : word.startsWith(String.valueOf(charSequence))) {
                            filteredList.add(mWord);
                            continue;
                        }
                        if (mWord.getDefs() != null) {
                            for (String rawDef : mWord.getDefs()) {
                                String def = rawDef.split("\t")[1].trim().toLowerCase();
                                if (contains ? rawDef.contains(charSequence) : def.startsWith(String.valueOf(charSequence))) {
                                    filteredList.add(mWord);
                                    break;
                                }
                            }
                        }
                    }
                    else if (showWords) {
                        if (contains ? word.contains(charSequence) : word.startsWith(String.valueOf(charSequence)))
                            filteredList.add(mWord);
                    } else {
                        if (mWord.getDefs() != null)
                            for (String rawDef : mWord.getDefs()) {
                                String def = rawDef.split("\t")[1].trim().toLowerCase();
                                if (contains ? def.contains(charSequence) : def.startsWith(String.valueOf(charSequence))){
                                    filteredList.add(mWord);
                                    break;
                                }
                            }
                    }
                }
                filterList = filteredList;
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values instanceof List)
                    filterList = (List<?>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

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

    DictionaryWordsAdapter(Context mContext, List<WordItem> wordList) {
        this.mContext = mContext;
        this.wordList = wordList;
        this.filterList = wordList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull @Override
    public DictHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new DictHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.word_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DictHolder holder, int position) {
        final WordItem wordItem = (WordItem) filterList.get(position);

        holder.word.setText(wordItem.getWord());
        holder.overflow.setTag(position);

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
        holder.subtext.setText(String.valueOf(tagsBuilder).replaceAll(",\nsyllables", "\nsyllables"));
        holder.overflow.setOnClickListener(view -> showPopupMenu(holder.overflow));
        holder.cardView.setOnLongClickListener(view -> {
            showPopupMenu(holder.overflow);
            return true;
        });
        holder.cardView.setOnClickListener(view ->
                new WordDialog((Activity) mContext, wordItem).show());
    }

    private void showPopupMenu(View view) {
        currentWord = (WordItem) filterList.get((Integer) view.getTag());
        PopupMenu popup = new PopupMenu(mContext, view);
        popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
        popup.setOnMenuItemClickListener(new WordContextItemListener());
        popup.show();
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
                    String wordRawGoogle = currentWord.getWord().replaceAll(" ", "+").replaceAll("\\s", "+");
                    try {
                        Intent intent1 = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent1.putExtra(SearchManager.QUERY, currentWord.getWord());
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        mContext.startActivity(intent1);
                    } catch (Exception e) {
                        customTabsIntent.setToolbarColor(Color.parseColor("#4888f2"));
                        CustomTabActivityHelper.openCustomTab(
                                (Activity) mContext, customTabsIntent.build(), Uri.parse("https://google.com/search?q=".concat(wordRawGoogle)));
                    }

                    return true;
                case R.id.action_wiki:
                    String wordRawWiki = currentWord.getWord().replaceAll(" ", "_").replaceAll("\\s", "_");
                    try {wordRawWiki = String.valueOf(new URL(wordRawWiki));} catch (Exception ignored) {}

                    Intent intent1 = new Intent();
                    intent1.setAction(Intent.ACTION_VIEW);
                    intent1.setPackage("org.wikipedia");
                    intent1.setData(Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                    List<ResolveInfo> resInfo1 = mContext.getPackageManager().queryIntentActivities(intent1, 0);
                    if (resInfo1 != null && resInfo1.size() > 0) mContext.startActivity(intent1);
                    else {
                        customTabsIntent.setToolbarColor(Color.parseColor("#333333"));
                        CustomTabActivityHelper.openCustomTab(
                                (Activity) mContext, customTabsIntent.build(), Uri.parse("https://en.wikipedia.org/wiki/".concat(wordRawWiki)));
                    }
                    return true;
                case R.id.action_urban:
                    customTabsIntent.setToolbarColor(Color.parseColor("#3b496b"));
                    CustomTabActivityHelper.openCustomTab(
                            (Activity) mContext, customTabsIntent.build(), Uri.parse("http://www.urbandictionary.com/define.php?term=".concat(currentWord.getWord())));

                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    void updateList(List<WordItem> list){
        filterList = list;
        notifyDataSetChanged();
    }
}