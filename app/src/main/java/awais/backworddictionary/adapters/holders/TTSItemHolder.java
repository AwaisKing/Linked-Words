package awais.backworddictionary.adapters.holders;

public class TTSItemHolder<T> {
    public final T object;
    public boolean selected;

    public TTSItemHolder(final T object, final boolean selected) {
        this.object = object;
        this.selected = selected;
    }
}