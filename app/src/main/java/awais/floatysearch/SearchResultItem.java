package awais.floatysearch;

public class SearchResultItem {
    private final String word;

    public SearchResultItem(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return word;
    }
}