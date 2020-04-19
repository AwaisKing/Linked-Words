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

import awais.backworddictionary.adapters.DefinitionsAdapter;
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.dialogs.WordDialog;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.DictionaryWordsItemListener;
import awais.backworddictionary.interfaces.SearchAdapterClickListener;

class DictionaryWordsAdapter extends RecyclerView.Adapter<WordItemViewHolder> implements Filterable {
    private final Context context;
    private final View.OnClickListener onClickListener, onWordItemClickListener;
    private final SearchAdapterClickListener itemClickListener;
    private final String[] noItemFound;
    private final Filter filter;
    private boolean isShowDialogEnabled;
    private List<?> filterList;
    final LinkedHashSet<WordItemViewHolder> holdersHashSet = new LinkedHashSet<>();
    final LinkedHashSet<WordItem> expandedHashSet = new LinkedHashSet<>();

    DictionaryWordsAdapter(@NonNull Context context, List<WordItem> wordList) {
        this.noItemFound = new String[]{"", context.getString(R.string.no_definition_found)};
        this.context = context;
        this.filterList = wordList;

        refreshShowDialogEnabled();

        this.onClickListener = view -> Utils.showPopupMenu(null, context, view,
                view.getTag() instanceof String ? (String) view.getTag() : null);

        this.itemClickListener = (text) -> {
            final String str = String.valueOf(text);
            if (!Utils.isEmpty(str) && !str.equals(context.getString(R.string.no_definition_found))) {
                Utils.copyText(context, str.replaceAll("^(.*)\\t", ""));
            }
        };

        this.onWordItemClickListener = view -> {
            final Object tag = view.getTag();
            if (tag instanceof TagItemHolder) {
                final TagItemHolder itemHolder = (TagItemHolder) tag;
                final WordItem wordItem = itemHolder.wordItem;

                if (isShowDialogEnabled || itemHolder.defsList.size() > 5)
                    new WordDialog(context, wordItem.getWord(), itemHolder.defsList, itemClickListener).show();
                else {
                    final boolean itemExpanded = wordItem.isExpanded();

                    itemHolder.expandableMenu.setVisibility(itemExpanded ? View.GONE : View.VISIBLE);
                    //holder.ivExpandedSearch.setVisibility(itemExpanded ? View.GONE : View.VISIBLE);

                    if (itemExpanded) expandedHashSet.remove(wordItem);
                    else expandedHashSet.add(wordItem);

                    wordItem.setExpanded(!itemExpanded);

                    //itemHolder.holder.setIsRecyclable(itemExpanded);
                }
            }
        };

        this.filter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence charSequence) {
                final FilterResults results = new FilterResults();
                results.values = wordList;
                if (Utils.isEmpty(charSequence)) return results;

                final boolean showWords = SettingsHelper.isFilterWords();
                final boolean showDefs = SettingsHelper.isFilterDefinition();
                final boolean contains = SettingsHelper.isFilterContains();

                if (!showDefs && !showWords) {
                    Toast.makeText(context, context.getString(R.string.select_filter_first), Toast.LENGTH_SHORT).show();
                    return results;
                }

                final ArrayList<WordItem> filteredList = new ArrayList<>(wordList.size() >> 1);
                for (final WordItem mWord : wordList) {
                    final String word = mWord.getWord().toLowerCase(Utils.defaultLocale);
                    final String[][] defs = mWord.getDefs();
                    final String searchVal = String.valueOf(charSequence);

                    final boolean wordBool = contains ? word.contains(searchVal) : word.startsWith(searchVal);

                    if (showWords && showDefs) {
                        if (wordBool) {
                            filteredList.add(mWord);
                            continue;
                        }
                        if (defs != null) {
                            for (final String[] rawDef : defs) {
                                if (contains ? rawDef[1].contains(searchVal) : rawDef[1].startsWith(searchVal)) {
                                    filteredList.add(mWord);
                                    break;
                                }
                            }
                        }
                    } else if (showWords) {
                        if (wordBool) filteredList.add(mWord);
                    } else if (defs != null) {
                        for (final String[] rawDef : defs) {
                            if (contains ? rawDef[1].contains(searchVal) : rawDef[1].startsWith(searchVal)) {
                                filteredList.add(mWord);
                                break;
                            }
                        }
                    }
                }
                filteredList.trimToSize();
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
    public WordItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        return new WordItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.word_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final WordItemViewHolder holder, final int position) {
        final WordItem wordItem = (WordItem) filterList.get(position);
        holdersHashSet.add(holder);

        final boolean wordItemExpanded = wordItem.isExpanded();
        holder.expandableMenu.setVisibility(wordItemExpanded ? View.VISIBLE : View.GONE);
        //holder.setIsRecyclable(!wordItemExpanded);

        final String[][] wordItemDefs = wordItem.getDefs();
        final String wordItemWord = wordItem.getWord();

        holder.overflow.setTag(position);
        holder.word.setText(wordItemWord);
        holder.subtext.setText(wordItem.getParsedTags());

        final ArrayList<String[]> defsList = new ArrayList<>();
        if (wordItemDefs == null)
            defsList.add(noItemFound);
        else
            defsList.addAll(Arrays.asList(wordItemDefs));

        holder.lvExpandedDefs.setAdapter(new DefinitionsAdapter(context, true, defsList, itemClickListener));

        holder.ivExpandedSearch.setTag(wordItem.getWord());
        holder.ivExpandedSearch.setOnClickListener(onClickListener);

        final DictionaryWordsItemListener itemListener = new DictionaryWordsItemListener(holder.overflow, wordItem);
        holder.overflow.setOnClickListener(itemListener);
        holder.cardView.setOnLongClickListener(itemListener);

        holder.cardView.setTag(new TagItemHolder(holder.expandableMenu, wordItem, defsList));
        holder.cardView.setOnClickListener(onWordItemClickListener);
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

    void updateList(final List<WordItem> list) {
        filterList = list;
        notifyDataSetChanged();
    }

    void refreshShowDialogEnabled() {
        this.isShowDialogEnabled = SettingsHelper.showDialog();
    }

    private static class TagItemHolder {
        private final View expandableMenu;
        private final WordItem wordItem;
        private final ArrayList<String[]> defsList;

        private TagItemHolder(final View expandableMenu, final WordItem wordItem, final ArrayList<String[]> defsList) {
            this.expandableMenu = expandableMenu;
            this.wordItem = wordItem;
            this.defsList = defsList;
        }
    }
}