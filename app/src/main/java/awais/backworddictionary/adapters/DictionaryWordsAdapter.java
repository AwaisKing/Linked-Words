package awais.backworddictionary.adapters;

import android.content.Context;
import android.content.res.Resources;
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
import awais.backworddictionary.adapters.holders.WordItem;
import awais.backworddictionary.adapters.holders.WordItemViewHolder;
import awais.backworddictionary.databinding.WordItemBinding;
import awais.backworddictionary.dialogs.WordDialog;
import awais.backworddictionary.helpers.MenuBuilderHelper;
import awais.backworddictionary.helpers.MenuBuilderHelper.PopupHelper;
import awais.backworddictionary.helpers.SettingsHelper;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.AdapterClickListener;

public final class DictionaryWordsAdapter extends RecyclerView.Adapter<WordItemViewHolder> implements Filterable {
    private final LayoutInflater layoutInflater;
    private final AdapterClickListener wordItemClickListener;
    private final AdapterClickListener definitionItemClickListener;

    private final String[] noItemFound;
    private final Filter filter;
    private boolean isShowDialogEnabled;
    private boolean showExpandedSearchIcon = true;
    private ArrayList<WordItem> filterList;
    public final HashSet<WordItem> expandedHashSet = new HashSet<>();
    public final HashSet<WordItemViewHolder> holdersHashSet = new HashSet<>();

    // recycling variables for definition PopupMenu tag
    private String def, strToCopy, word;

    public DictionaryWordsAdapter(@NonNull final Context context, final ArrayList<WordItem> wordList) {
        setHasStableIds(true);
        final String strNoItemFound = context.getString(R.string.no_definition_found);

        this.layoutInflater = LayoutInflater.from(context);
        this.noItemFound = new String[]{"", strNoItemFound};
        this.filterList = wordList;

        refreshShowDialogEnabled();

        final Resources resources = context.getResources();
        this.definitionItemClickListener = new AdapterClickListener() {
            @Override
            public void onClick(final View view) {
                if (view != null) {
                    Object tag = view.getTag(R.id.word);
                    word = tag instanceof CharSequence ? tag.toString() : null;

                    if (!Utils.isEmpty(word) && (tag = view.getTag(R.id.word_key)) instanceof CharSequence
                            && !Utils.isEmpty(def = tag.toString()) && !def.equals(strNoItemFound)) {
                        strToCopy = word.concat(": ").concat(def.replaceAll("^(.*)\\t", ""));

                        if (SettingsHelper.showDefsPopup()) {
                            final MenuItem.OnMenuItemClickListener menuItemClickListener = menuItem -> {
                                final int itemId = menuItem.getItemId();
                                final boolean isActionCopy = itemId == R.id.action_copy;
                                if (isActionCopy || itemId == R.id.action_speak) {
                                    if (isActionCopy) Utils.copyText(context, strToCopy);
                                    else Utils.speakText(def);
                                    return true;
                                }
                                return false;
                            };

                            tag = view.getTag(R.id.key_popup_builder);
                            final MenuBuilderHelper menuBuilder;
                            if (tag instanceof MenuBuilderHelper) menuBuilder = (MenuBuilderHelper) tag;
                            else {
                                menuBuilder = new MenuBuilderHelper(context)
                                        .setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                                menuBuilder.add(0, R.id.action_copy, 0, resources.getString(R.string.copy))
                                        .setOnMenuItemClickListener(menuItemClickListener);
                                menuBuilder.add(0, R.id.action_speak, 0, resources.getString(R.string.speak))
                                        .setOnMenuItemClickListener(menuItemClickListener);

                                view.setTag(R.id.key_popup_builder, menuBuilder);
                            }

                            MenuItem menuItem = menuBuilder.findItem(R.id.action_copy);
                            if (menuItem == null) menuItem = menuBuilder.add(0, R.id.action_copy, 0,
                                    resources.getString(R.string.copy));
                            menuItem.setOnMenuItemClickListener(menuItemClickListener);

                            menuItem = menuBuilder.findItem(R.id.action_speak);
                            if (menuItem == null) menuItem = menuBuilder.add(0, R.id.action_speak, 0,
                                    resources.getString(R.string.speak));
                            menuItem.setOnMenuItemClickListener(menuItemClickListener);

                            tag = view.getTag(R.id.key_popup);
                            final PopupHelper popupHelper;
                            if (tag instanceof PopupHelper) popupHelper = (PopupHelper) tag;
                            else {
                                popupHelper = new PopupHelper(context, menuBuilder, view);
                                view.setTag(R.id.key_popup, popupHelper);
                            }
                            if (!popupHelper.tryShow())
                                popupHelper.show();

                        } else {
                            Utils.copyText(context, strToCopy);
                        }
                    }
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
                Object tag;

                if (view instanceof CardView) {
                    final WordItem wordItem = (tag = view.getTag(R.id.word)) instanceof WordItem ? (WordItem) tag : null;
                    final List<?> defsList = (tag = view.getTag(R.id.lvDefs)) instanceof List ? (List<?>) tag : null;

                    if (wordItem == null || defsList == null) return;

                    if (!isShowDialogEnabled && defsList.size() <= 5) {
                        final boolean itemExpanded = wordItem.isExpanded();
                        if (itemExpanded) expandedHashSet.remove(wordItem);
                        else expandedHashSet.add(wordItem);
                        wordItem.setExpanded(!itemExpanded);

                        final int position;
                        if ((position = wordItem.getPosition()) > -1)
                            notifyItemChanged(position);

                    } else new WordDialog(context, wordItem.getWord(), defsList,
                            definitionItemClickListener).show();

                } else if (view instanceof ImageView) {
                    tag = view.getTag(R.id.overflow);

                    if (tag instanceof WordItem)
                        Utils.showPopupMenu(view, (WordItem) tag);
                    else if (view.getId() == R.id.ivExpandedSearch && (tag = view.getTag()) instanceof CharSequence)
                        Utils.showPopupMenu(null, context, view, tag.toString());
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
                if (wordList == null) return results;
                if (Utils.isEmpty(charSequence)) return results;

                final boolean showWords = SettingsHelper.isFilterWords();
                final boolean showDefs = SettingsHelper.isFilterDefinition();
                final boolean contains = SettingsHelper.isFilterContains();

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
            protected void publishResults(final CharSequence charSequence, FilterResults filterResults) {
                //noinspection unchecked
                updateList((ArrayList<WordItem>) filterResults.values);
            }
        };
    }

    @NonNull
    @Override
    public WordItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new WordItemViewHolder(WordItemBinding.inflate(layoutInflater, parent, false),
                wordItemClickListener, definitionItemClickListener, noItemFound,
                showExpandedSearchIcon);
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
        return position;
    }

    @Override
    public int getItemCount() {
        return filterList == null ? 0 : filterList.size();
    }

    public void updateList(final ArrayList<WordItem> list) {
        final int previousSize = getItemCount();
        filterList = list;
        final int nextSize = getItemCount();

        if (previousSize == nextSize)
            notifyItemRangeChanged(0, previousSize);
        else if (previousSize <= nextSize) {
            notifyItemRangeChanged(0, previousSize);
            notifyItemRangeInserted(previousSize, nextSize - previousSize);
        } else if (nextSize != 0) {
            notifyItemRangeChanged(0, nextSize);
            notifyItemRangeRemoved(nextSize - 1, previousSize);
        } else
            notifyItemRangeRemoved(0, previousSize);
    }

    public void refreshShowDialogEnabled() {
        this.isShowDialogEnabled = SettingsHelper.showDialog();
    }

    public void setShowExpandedSearchIcon(final boolean showExpandedSearchIcon) {
        this.showExpandedSearchIcon = showExpandedSearchIcon;
    }
}