package awais.backworddictionary.adapters.holders;

public final class WordItem {
    private final String word;
    private final String[][] defs;
    private final String parsedTags;
    private boolean expanded;

    public WordItem(final String word, final int numSyllables, final String[] tags, final String[][] defs) {
        this.word = word;
        this.defs = defs;

        final StringBuilder tagsBuilder = new StringBuilder();
        if (tags != null && tags.length > 0) {
            tagsBuilder.insert(0, "tags:");
            for (final String tag : tags) {
                if (tag.equals("syn")) tagsBuilder.insert(5, " [synonym]");
                if (tag.equals("prop")) tagsBuilder.insert(5, " [proper]");
                if (tag.equals("n")) tagsBuilder.append(" noun,");
                if (tag.equals("adj")) tagsBuilder.append(" adjective,");
                if (tag.equals("v")) tagsBuilder.append(" verb,");
                if (tag.equals("adv")) tagsBuilder.append(" adverb,");
            }
        }

        final int lastCharIndex = tagsBuilder.length() - 1;
        if (lastCharIndex > 1 && tagsBuilder.charAt(lastCharIndex) == ',')
            tagsBuilder.deleteCharAt(lastCharIndex);

        tagsBuilder.append(tags != null && tags.length > 0 && numSyllables > 0 ? '\n' : '\0')
                .append("syllables: ").append(numSyllables);

        this.parsedTags = tagsBuilder.toString();
    }

    public String getWord() {
        return word;
    }

    public String getParsedTags() {
        return parsedTags;
    }

    public String[][] getDefs() {
        return defs;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }
}