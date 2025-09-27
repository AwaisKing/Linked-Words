package awais.backworddictionary.models;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Objects;

public final class WordItem {
    private int position = RecyclerView.NO_POSITION;
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
                if (tag == null) continue;
                switch (tag) {
                    case "syn" -> tagsBuilder.insert(5, " [synonym]");
                    case "prop" -> tagsBuilder.insert(5, " [proper]");
                    case "n" -> tagsBuilder.append(" noun,");
                    case "adj" -> tagsBuilder.append(" adjective,");
                    case "v" -> tagsBuilder.append(" verb,");
                    case "adv" -> tagsBuilder.append(" adverb,");
                }
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

    public void setPosition(final int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof final WordItem that && this.position == that.position && this.expanded == that.expanded
               && Objects.equals(this.word, that.word) && Objects.deepEquals(this.defs, that.defs);
    }

    @Override
    public int hashCode() {
        // return Objects.hash(position, word, Arrays.deepHashCode(defs), expanded);
        int result = 1;
        result = (31 * result) + Objects.hashCode(word);
        result = (31 * result) + Objects.hashCode(position);
        result = (31 * result) + Objects.hashCode(expanded);
        result = (31 * result) + Arrays.deepHashCode(defs);
        return result;
    }
}