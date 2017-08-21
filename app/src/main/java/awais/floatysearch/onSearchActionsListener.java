package awais.floatysearch;

public interface onSearchActionsListener {
    void onItemClicked(SearchResultItem item);
    void showProgress(boolean show);
    void listEmpty();
    void onScroll();
//    void error(String localizedMessage);
}
