package awais.backworddictionary.custom;

public class WordItem {
    private final String word;
    private final int numSyllables;
    private final String[] tags;
    private final String[][] defs;
    private boolean expanded;

    public WordItem(String word, int numSyllables, String[] tags, String[][] defs) {
        this.word = word;
        this.numSyllables = numSyllables;
        this.tags = tags;
        this.defs = defs;
    }

    public String getWord() {
        return word;
    }

    public String[] getTags() {
        return tags;
    }

    public int getNumSyllables() {
        return numSyllables;
    }

    public String[][] getDefs() {
        return defs;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}