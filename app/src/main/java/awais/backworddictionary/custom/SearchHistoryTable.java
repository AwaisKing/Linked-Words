package awais.backworddictionary.custom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.backworddictionary.helpers.Utils;
import awais.lapism.SearchItem;

public final class SearchHistoryTable extends SQLiteOpenHelper {
    private final static int HISTORY_SIZE = 8;

    public SearchHistoryTable(final Context context) {
        super(context, "search_history_database.db", null, 5);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS search_history ( _id INTEGER PRIMARY KEY AUTOINCREMENT, _text TEXT );");
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {}

    @Override
    public void onDowngrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {}

    public void addItem(final SearchItem item) {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            if (db == null || !db.isOpen()) return;
            final String text = item.getText();
            final ContentValues values = new ContentValues();
            if (checkText(db, text)) {
                values.put("_id", getLastItemId(db) + 1);
                db.update("search_history", values, "_id = ?", new String[]{Integer.toString(getItemId(db, item))});
            } else {
                values.put("_text", text);
                db.insert("search_history", null, values);
            }
        }
    }

    @Nullable
    public List<SearchItem> getAllItems(final String databaseKey) {
        List<SearchItem> list = null;

        try (final SQLiteDatabase db = getReadableDatabase()) {
            if (db != null && db.isOpen()) {
                final boolean isKeyEmpty = Utils.isEmpty(databaseKey);
                final String query = isKeyEmpty ? "SELECT * FROM search_history ORDER BY _id DESC LIMIT ?"
                                                : "SELECT * FROM search_history WHERE _text = ?";
                try (final Cursor cursor = db.rawQuery(query, new String[]{isKeyEmpty ? "" + HISTORY_SIZE : databaseKey})) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do list.add(new SearchItem(R.drawable.ic_history, cursor.getString(1), false));
                        while (cursor.moveToNext());
                    }
                }
            }
        }

        return list;
    }

    public void clearDatabase(final String key) {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            if (key == null) db.delete("search_history", null, null);
            else db.delete("search_history", "_text = ?", new String[]{key});
        }
    }

    public int getItemsCount() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM search_history", null)) {
            return cursor.getCount();
        }
    }

    private static int getItemId(final SQLiteDatabase db, @NonNull final SearchItem item) {
        try (final Cursor res = db.rawQuery("SELECT _id FROM search_history WHERE _text = ?", new String[]{item.getText()})) {
            return res.moveToFirst() ? res.getInt(0) : 0;
        }
    }

    private static int getLastItemId(final SQLiteDatabase db) {
        try (final Cursor cursor = db.rawQuery("SELECT _id FROM search_history", null)) {
            return cursor.getCount() > 0 && cursor.moveToLast() ? cursor.getInt(0) : 0;
        }
    }

    private static boolean checkText(final SQLiteDatabase db, final String text) {
        try (final Cursor cursor = db.rawQuery("SELECT _text FROM search_history WHERE _text = ?", new String[]{text})) {
            return cursor.getCount() > 0 && cursor.moveToFirst();
        }
    }
}