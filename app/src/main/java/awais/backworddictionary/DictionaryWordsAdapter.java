package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.custom.WordDialog;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.WordContextItemListener;
import awais.backworddictionary.helpers.WordItemHolder;

public class DictionaryWordsAdapter extends RecyclerView.Adapter<WordItemHolder> implements Filterable {
    private final Context mContext;
    private final List<WordItem> wordList;
    private List<?> filterList;

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
                boolean contains = Main.sharedPreferences.getBoolean("filterContain", false);

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
    public WordItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new WordItemHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.word_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WordItemHolder holder, int position) {
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
        holder.cardView.setOnClickListener(view -> {
             new WordDialog((Activity) mContext, wordItem).show();
    }

    private void showPopupMenu(View view) {
        WordItem currentWord = (WordItem) filterList.get((Integer) view.getTag());
        PopupMenu popup = new PopupMenu(mContext, view);
        popup.getMenuInflater().inflate(R.menu.menu_word, popup.getMenu());
        popup.setOnMenuItemClickListener(new WordContextItemListener(mContext, currentWord.getWord()));
        popup.show();
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