package awais.floatysearch;

public interface onSimpleSearchActionsListener {
    void onItemClicked(SearchResultItem item);
    void onScroll();
    void error(String localizedMessage);
}
