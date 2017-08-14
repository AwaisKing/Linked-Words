package awais.backworddictionary;

class WordItem {
    private final String word;
    private final int numSyllables;
    private final String[] defs;
    private final String[] tags;

    WordItem(String word, String[] tags, int numSyllables, String[] defs) {
        this.word = word;
        this.tags = tags;
        this.numSyllables = numSyllables;
        this.defs = defs;
    }
    String getWord() {
        return word;
    }
    String[] getTags() {
        return tags;
    }
    int getNumSyllables() {
        return numSyllables;
    }
    String[] getDefs() {
        return defs;
    }
}
