package awais.backworddictionary.custom;

public class WordItem {
    private final String word;
    private final int numSyllables;
    private final String[] defs;
    private final String[] tags;

    public WordItem(String word, String[] tags, int numSyllables, String[] defs) {
        this.word = word;
        this.tags = tags;
        this.numSyllables = numSyllables;
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
    public String[] getDefs() {
        return defs;
    }
}