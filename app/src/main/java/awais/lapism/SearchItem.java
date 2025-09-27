package awais.lapism;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

import awais.backworddictionary.R;

public final class SearchItem implements Parcelable {
    public static final Creator<SearchItem> CREATOR = new Creator<>() {
        @Override
        @NonNull
        public SearchItem createFromParcel(final Parcel source) {
            return new SearchItem(source);
        }

        @Override
        @NonNull
        public SearchItem[] newArray(final int size) {
            return new SearchItem[size];
        }
    };
    private final int icon;
    private final boolean searchable;
    private final String text;

    public SearchItem(final String text) {
        this(R.drawable.ic_search, text, true);
    }

    public SearchItem(final int icon, final String text, final boolean searchable) {
        this.icon = icon;
        this.text = text;
        this.searchable = searchable;
    }

    private SearchItem(@NonNull final Parcel in) {
        this.icon = in.readInt();
        this.text = in.readString();
        this.searchable = in.readInt() == 1;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getText() {
        return this.text;
    }

    public boolean isSearchable() {
        return searchable;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeInt(this.icon);
        dest.writeString(this.text);
        dest.writeInt(this.searchable ? 1 : 0);
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || o instanceof final SearchItem item && icon == item.icon && searchable == item.searchable && text.equals(item.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, searchable, text);
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}