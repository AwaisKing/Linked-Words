package awais.backworddictionary.custom;

@SuppressWarnings("unused")
public class WordItem {
    private String word;
    private int numSyllables;
    private String[] defs;
    private String[] tags;

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