package awais.backworddictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import awais.backworddictionary.adapters.SearchAdapter;
import awais.backworddictionary.adapters.WordAdapter;
import awais.backworddictionary.custom.WordDialog;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.WordItemHolder;
import awais.backworddictionary.interfaces.DictionaryWordsItemListener;

class DictionaryWordsAdapter extends RecyclerView.Adapter<WordItemHolder> implements Filterable {
    private final Context context;
    private final View.OnClickListener onClickListener;
    private final SearchAdapter.OnItemClickListener itemClickListener;
    private final LinkedHashSet<WordItemHolder> holdersHashSet = new LinkedHashSet<>();
    private final LinkedHashSet<WordItem> expandedHashSet = new LinkedHashSet<>();
    private final String[] noItemFound;
    private final Filter filter;
    private boolean isShowDialogEnabled;
    private List<?> filterList;

    DictionaryWordsAdapter(@NonNull Context context, List<WordItem> wordList) {
        this.context = context;
        this.filterList = wordList;
        this.noItemFound = new String[] {"", context.getString(R.string.no_definition_found)};

        refreshShowDialogEnabled();

        this.onClickListener = view -> Utils.showPopupMenu(null, context, view,
                view.getTag() instanceof String ? (String) view.getTag() : null);

        this.itemClickListener = (view, pos, text) -> {
            String str = String.valueOf(text);
            if (str.isEmpty() || str.equals(context.getString(R.string.no_definition_found))) return;
            Utils.copyText(context, str.replaceAll("^(.*)\\t", ""));
        };

        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults results = new FilterResults();
                results.values = wordList;
                if (Utils.isEmpty(charSequence)) return results;

                final boolean showWords = Main.sharedPreferences.getBoolean("filterWord", true);
                final boolean showDefs = Main.sharedPreferences.getBoolean("filterDefinition", false);
                final boolean contains = Main.sharedPreferences.getBoolean("filterContain", false);

                if (!showDefs && !showWords) {
                    Toast.makeText(context, context.getString(R.string.select_filter_first), Toast.LENGTH_SHORT).show();
                    return results;
                }

                final List<WordItem> filteredList = new ArrayList<>();
                for (WordItem mWord : wordList) {
                    final String word = mWord.getWord().toLowerCase();
                    final String[][] defs = mWord.getDefs();
                    final String searchVal = String.valueOf(charSequence);

                    final boolean wordBool = contains ? word.contains(searchVal) : word.startsWith(searchVal);

                    if (showWords && showDefs) {
                        if (wordBool) {
                            filteredList.add(mWord);
                            continue;
                        }
                        if (defs != null) {
                            for (String[] rawDef : defs) {
                                if (contains ? rawDef[1].contains(searchVal) : rawDef[1].startsWith(searchVal)) {
                                    filteredList.add(mWord);
                                    break;
                                }
                            }
                        }
                    } else if (showWords) {
                        if (wordBool) filteredList.add(mWord);
                    } else if (defs != null) {
                        for (String[] rawDef : defs) {
                            if (contains ? rawDef[1].contains(searchVal) : rawDef[1].startsWith(searchVal)) {
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

    @NonNull
    @Override
    public WordItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new WordItemHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.word_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WordItemHolder holder, int position) {
        final WordItem wordItem = (WordItem) filterList.get(position);
        holdersHashSet.add(holder);

        final boolean wordItemExpanded = wordItem.isExpanded();
        holder.expandableMenu.setVisibility(wordItemExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpandedSearch.setVisibility(wordItemExpanded ? View.VISIBLE : View.GONE);
        holder.setIsRecyclable(!wordItemExpanded);

        final int wordItemSyllables = wordItem.getNumSyllables();
        final String[] wordItemTags = wordItem.getTags();
        final String[][] wordItemDefs = wordItem.getDefs();
        final String wordItemWord = wordItem.getWord();

        holder.word.setText(wordItemWord);
        holder.overflow.setTag(position);

        final StringBuilder tagsBuilder = new StringBuilder();
        if (wordItemTags != null && wordItemTags.length > 0) {
            tagsBuilder.insert(0, "tags:");
            for (String tag : wordItemTags) {
                if (tag.equals("syn")) tagsBuilder.insert(5, " [synonym]");
                if (tag.equals("prop")) tagsBuilder.insert(5, " [proper]");
                if (tag.equals("n")) tagsBuilder.append(" noun,");
                if (tag.equals("adj")) tagsBuilder.append(" adjective,");
                if (tag.equals("v")) tagsBuilder.append(" verb,");
                if (tag.equals("adv")) tagsBuilder.append(" adverb,");
            }
        }

        // remove last , (comma) from tags
        final int lastCharIndex = tagsBuilder.length() - 1;
        if (lastCharIndex > 1 && tagsBuilder.charAt(lastCharIndex) == ',')
            tagsBuilder.deleteCharAt(lastCharIndex);

        tagsBuilder.append(wordItemTags != null && wordItemTags.length > 0 && wordItemSyllables > 0 ? '\n' : '\0')
                .append("syllables: ").append(wordItemSyllables);

        holder.subtext.setText(String.valueOf(tagsBuilder));

        final ArrayList<String[]> defsList = new ArrayList<>();
        if (wordItemDefs == null)
            defsList.add(noItemFound);
        else
            defsList.addAll(Arrays.asList(wordItemDefs));

        holder.lvExpandedDefs.setAdapter(new WordAdapter(context, true, defsList, itemClickListener));

        holder.ivExpandedSearch.setTag(wordItem.getWord());
        holder.ivExpandedSearch.setOnClickListener(onClickListener);

        final DictionaryWordsItemListener itemListener = new DictionaryWordsItemListener(holder.overflow, wordItem);
        holder.overflow.setOnClickListener(itemListener);
        holder.cardView.setOnLongClickListener(itemListener);

        holder.cardView.setOnClickListener(view -> {
            if (isShowDialogEnabled || defsList.size() > 5)
                new WordDialog(context, wordItemWord, defsList, itemClickListener).show();
            else {
                final boolean itemExpanded = wordItem.isExpanded();

                holder.expandableMenu.setVisibility(itemExpanded ? View.GONE : View.VISIBLE);
                holder.ivExpandedSearch.setVisibility(itemExpanded ? View.GONE : View.VISIBLE);

                if (itemExpanded) expandedHashSet.remove(wordItem);
                else expandedHashSet.add(wordItem);

                wordItem.setExpanded(!itemExpanded);
                holder.setIsRecyclable(!itemExpanded);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    void updateList(List<WordItem> list){
        filterList = list;
        notifyDataSetChanged();
    }

    void refreshShowDialogEnabled() {
        this.isShowDialogEnabled = Main.sharedPreferences.getBoolean("showDialog", false);
    }

    LinkedHashSet[] getHashSets() {
        return new LinkedHashSet[] {expandedHashSet, holdersHashSet};
    }
}