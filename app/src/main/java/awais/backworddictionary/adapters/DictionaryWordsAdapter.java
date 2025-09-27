package awais.backworddictionary.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.databinding.WordItemBinding;
import awais.backworddictionary.dialogs.WordDialog;
import awais.backworddictionary.helpers.MenuBuilderHelper;
import awais.backworddictionary.helpers.MenuBuilderHelper.PopupHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.TTSHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.AdapterClickListener;
import awais.backworddictionary.models.WordItem;

public final class DictionaryWordsAdapter extends RecyclerView.Adapter<WordItemViewHolder> implements Filterable {
    private final LayoutInflater layoutInflater;
    private final AdapterClickListener wordItemClickListener;
    private final AdapterClickListener definitionItemClickListener;

    public final HashSet<WordItem> expandedHashSet = new HashSet<>();
    public final HashSet<WordItemViewHolder> holdersHashSet = new HashSet<>();

    private final String[] noItemFound;
    private final Filter filter;
    private boolean isShowDialogEnabled;
    private ArrayList<WordItem> filterList;

    /// recycling variables for definition PopupMenu tag
    private String word, definition, strToCopy;

    public DictionaryWordsAdapter(@NonNull final Context context, final ArrayList<WordItem> wordList) {
        setHasStableIds(true);

        this.layoutInflater = LayoutInflater.from(context);
        this.noItemFound = new String[]{"", context.getString(R.string.no_definition_found)};
        this.filterList = wordList;

        final SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        refreshShowDialogEnabled(settingsHelper);

        this.definitionItemClickListener = new AdapterClickListener() {
            @Override
            public void onClick(final View view) {
                if (view == null) return;

                word = view.getTag(R.id.word) instanceof final CharSequence cs ? cs.toString() : null;
                if (Utils.isEmpty(word)) return;

                definition = view.getTag(R.id.word_key) instanceof final CharSequence cs ? cs.toString() : null;
                if (Utils.isEmpty(definition) || definition.equals(noItemFound[1])) return;

                strToCopy = word.concat(": ").concat(definition.replaceAll("^(.*)\\t", ""));

                if (!settingsHelper.showDefsPopup()) Utils.copyText(context, strToCopy);
                else {
                    final MenuItem.OnMenuItemClickListener menuItemClickListener = menuItem -> {
                        final int itemId = menuItem.getItemId();
                        if (itemId == R.id.action_copy) {
                            Utils.copyText(context, strToCopy);
                            return true;
                        } else if (itemId == R.id.action_speak) {
                            TTSHelper.speakText(definition);
                            return true;
                        }
                        return false;
                    };

                    Object tag = view.getTag(R.id.key_popup_builder);
                    final MenuBuilderHelper menuBuilder;
                    if (tag instanceof MenuBuilderHelper) menuBuilder = (MenuBuilderHelper) tag;
                    else {
                        menuBuilder = new MenuBuilderHelper(context).setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                        menuBuilder.add(0, R.id.action_copy, 0, R.string.copy)
                                   .setOnMenuItemClickListener(menuItemClickListener);
                        menuBuilder.add(0, R.id.action_speak, 0, R.string.speak)
                                   .setOnMenuItemClickListener(menuItemClickListener);

                        view.setTag(R.id.key_popup_builder, menuBuilder);
                    }

                    MenuItem menuItem = menuBuilder.findItem(R.id.action_copy);
                    if (menuItem == null) menuItem = menuBuilder.add(0, R.id.action_copy, 0, R.string.copy);
                    menuItem.setOnMenuItemClickListener(menuItemClickListener);

                    menuItem = menuBuilder.findItem(R.id.action_speak);
                    if (menuItem == null) menuItem = menuBuilder.add(0, R.id.action_speak, 0, R.string.speak);
                    menuItem.setOnMenuItemClickListener(menuItemClickListener);

                    tag = view.getTag(R.id.key_popup);
                    final PopupHelper popupHelper;
                    if (tag instanceof PopupHelper) popupHelper = (PopupHelper) tag;
                    else {
                        popupHelper = new PopupHelper(context, menuBuilder, view);
                        view.setTag(R.id.key_popup, popupHelper);
                    }

                    if (!popupHelper.tryShow()) popupHelper.show();
                }
            }

            @Override
            public boolean onLongClick(final View view) {
                return super.onLongClick(view);
            }
        };
        this.wordItemClickListener = new AdapterClickListener() {
            @Override
            public void onClick(final View view) {
                if (view instanceof CardView) {
                    final WordItem wordItem = view.getTag(R.id.word) instanceof final WordItem wi ? wi : null;
                    final List<?> defsList = view.getTag(R.id.lvDefs) instanceof final List<?> dl ? dl : null;

                    if (wordItem == null || defsList == null) return;

                    if (isShowDialogEnabled || defsList.size() > 5)
                        new WordDialog(context, wordItem.getWord(), defsList, definitionItemClickListener).show();
                    else {
                        final boolean itemExpanded = wordItem.isExpanded();
                        if (itemExpanded) expandedHashSet.remove(wordItem);
                        else expandedHashSet.add(wordItem);
                        wordItem.setExpanded(!itemExpanded);

                        final int position;
                        if ((position = wordItem.getPosition()) > -1) notifyItemChanged(position);
                    }
                } else if (view instanceof ImageView) {
                    if (view.getTag(R.id.overflow) instanceof final WordItem wordItem)
                        Utils.showPopupMenu(view, wordItem);
                    else if (view.getId() == R.id.ivExpandedSearch && view.getTag() instanceof final CharSequence cs)
                        Utils.showPopupMenu(null, context, view, cs.toString());
                }
            }

            @Override
            public boolean onLongClick(final View view) {
                if (view == null) return super.onLongClick(null);

                final Object viewTag = view.getTag(R.id.overflow);
                final Object tag = view.getTag(R.id.word);
                final boolean validTags = viewTag instanceof ImageView && tag instanceof WordItem;
                if (validTags) {
                    final Object popupTag = view.getTag(R.id.key_popup);
                    if (popupTag instanceof PopupMenu)
                        ((PopupMenu) popupTag).show();
                    else
                        Utils.showPopupMenu((View) viewTag, (WordItem) tag);
                }
                return validTags;
            }
        };

        this.filter = new Filter() {
            private final FilterResults results = new FilterResults();

            @Override
            protected FilterResults performFiltering(final CharSequence charSequence) {
                results.values = wordList;
                results.count = wordList != null ? wordList.size() : 0;
                if (wordList == null || Utils.isEmpty(charSequence)) return results;

                final boolean showWords = settingsHelper.isFilterWords();
                final boolean showDefs = settingsHelper.isFilterDefinition();
                final boolean contains = settingsHelper.isFilterContains();

                if (!showDefs && !showWords) {
                    Toast.makeText(context, context.getString(R.string.select_filter_first), Toast.LENGTH_SHORT).show();
                    return results;
                }

                final String searchVal = String.valueOf(charSequence);
                final ArrayList<WordItem> filteredList = new ArrayList<>(wordList.size() >>> 1);
                for (final WordItem mWord : wordList) {
                    final String word = mWord.getWord().toLowerCase(Utils.defaultLocale);
                    final String[][] defs = mWord.getDefs();

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
                results.values = filteredList;
                results.count = filteredList.size();

                return results;
            }

            @Override
            protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
                // noinspection unchecked
                updateList((ArrayList<WordItem>) filterResults.values);
            }
        };
    }

    @NonNull
    @Override
    public WordItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new WordItemViewHolder(WordItemBinding.inflate(layoutInflater, parent, false),
                                      wordItemClickListener, definitionItemClickListener, noItemFound);
    }

    @Override
    public void onBindViewHolder(@NonNull final WordItemViewHolder holder, final int position) {
        holdersHashSet.add(holder);
        holder.setupItem(filterList.get(position), position);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public long getItemId(final int position) {
        final WordItem wordItem = filterList == null || filterList.isEmpty() ? null : filterList.get(position);
        return wordItem == null ? position : wordItem.hashCode();
    }

    @Override
    public int getItemCount() {
        return filterList == null ? 0 : filterList.size();
    }

    public void updateList(final ArrayList<WordItem> list) {
        final int oldSize = getItemCount();
        filterList = list;
        final int newSize = getItemCount();

        if (oldSize == newSize) {
            notifyItemRangeChanged(0, oldSize);
        } else if (oldSize <= newSize) {
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else if (newSize != 0) {
            notifyItemRangeChanged(0, newSize);
            notifyItemRangeRemoved(newSize - 1, oldSize);
        } else {
            notifyItemRangeRemoved(0, oldSize);
        }
    }

    public void refreshShowDialogEnabled(SettingsHelper settingsHelper) {
        if (settingsHelper == null) settingsHelper = SettingsHelper.getInstance(layoutInflater.getContext());
        this.isShowDialogEnabled = settingsHelper.showDialog();
    }
}