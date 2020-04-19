package awais.backworddictionary.interfaces;

public interface SearchAdapterClickListener {
    void onItemClick(final String text);
    default void onItemLongClick(final String text) {}
}