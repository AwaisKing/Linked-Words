package awais.lapism;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import awais.backworddictionary.R;

public class SearchItem implements Parcelable {
    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @NonNull
        public SearchItem createFromParcel(final Parcel source) {
            return new SearchItem(source);
        }

        @NonNull
        public SearchItem[] newArray(final int size) {
            return new SearchItem[size];
        }
    };
    private final int icon;
    private final CharSequence text;

    public SearchItem(final CharSequence text) {
        this(R.drawable.ic_search, text);
    }

    public SearchItem(final int icon, final CharSequence text) {
        this.icon = icon;
        this.text = text;
    }

    private SearchItem(@NonNull final Parcel in) {
        this.icon = in.readInt();
        this.text = in.readParcelable(CharSequence.class.getClassLoader());
    }

    public int getIcon() {
        return this.icon;
    }

    public CharSequence getText() {
        return this.text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeInt(this.icon);
        TextUtils.writeToParcel(this.text, dest, flags);
    }
}