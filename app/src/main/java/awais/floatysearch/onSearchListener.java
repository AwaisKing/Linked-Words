package awais.floatysearch;

public interface onSearchListener {
    void onSearch(String query, boolean isEnter);
//    void searchViewOpened();
//    void searchViewClosed();
    void onCancelSearch();
}
