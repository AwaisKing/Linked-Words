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

public final class SearchHistoryTable {
    private final static int HISTORY_SIZE = 8;
    private final SearchHistoryDatabase dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(final Context context) {
        dbHelper = new SearchHistoryDatabase(context, this);
        open();
    }

    public void open() {
        if ((db == null || !db.isOpen()) && dbHelper != null) db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) dbHelper.close();
    }

    public void addItem(final SearchItem item) {
        if (db != null && db.isOpen()) {
            final String text = item.getText();
            final ContentValues values = new ContentValues();
            if (checkText(text)) {
                values.put("_id", getLastItemId() + 1);
                db.update("search_history", values, "_id = ?", new String[]{Integer.toString(getItemId(item))});
            } else {
                values.put("_text", text);
                db.insert("search_history", null, values);
            }
        }
    }

    @Nullable
    public List<SearchItem> getAllItems(final String databaseKey) {
        List<SearchItem> list = null;

        if (db == null || !db.isOpen()) db = dbHelper.getWritableDatabase();

        if (db != null && db.isOpen()) {
            final boolean isKeyEmpty = Utils.isEmpty(databaseKey);
            try (final Cursor cursor = db.rawQuery(isKeyEmpty ? "SELECT * FROM search_history ORDER BY _id DESC LIMIT ?"
                            : "SELECT * FROM search_history WHERE _text = ?",
                    new String[]{isKeyEmpty ? Integer.toString(HISTORY_SIZE) : databaseKey})) {
                if (cursor != null && cursor.moveToFirst()) {
                    list = new ArrayList<>(0);
                    do list.add(new SearchItem(R.drawable.ic_history, cursor.getString(1), false));
                    while (cursor.moveToNext());
                }
            }
        }

        return list;
    }

    public void clearDatabase(final String key) {
        if (db != null && db.isOpen()) {
            if (key == null) db.delete("search_history", null, null);
            else db.delete("search_history", "_text = ?", new String[]{key});
        }
    }

    /*
    enable?
    public int getItemsCount() {
        if (db == null || !db.isOpen()) return -1;

        final Cursor cursor = db.rawQuery("SELECT * FROM search_history", null);
        final int count = cursor.getCount();
        cursor.close();
        return count;
    }
    */

    private int getItemId(@NonNull final SearchItem item) {
        final int id;
        try (final Cursor res = db.rawQuery("SELECT _id FROM search_history WHERE _text = ?", new String[]{item.getText()})) {
            res.moveToFirst();
            id = res.getInt(0);
        }
        return id;
    }

    private int getLastItemId() {
        try (final Cursor cursor = db.rawQuery("SELECT _id FROM search_history", null)) {
            return cursor != null && cursor.moveToLast() ? cursor.getInt(0) : 0;
        }
    }

    private boolean checkText(final String text) {
        try (final Cursor cursor = db.rawQuery("SELECT _text FROM search_history WHERE _text = ?", new String[]{text})) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    private final static class SearchHistoryDatabase extends SQLiteOpenHelper {
        private final SearchHistoryTable searchHistoryTable;

        SearchHistoryDatabase(final Context context, final SearchHistoryTable searchHistoryTable) {
            super(context, "search_history_database.db", null, 4);
            this.searchHistoryTable = searchHistoryTable;
        }

        @Override
        public void onCreate(@NonNull final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS search_history ( _id INTEGER PRIMARY KEY AUTOINCREMENT, _text TEXT );");
        }

        @Override
        public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            List<SearchItem> searchItems = null;
            if (searchHistoryTable != null) searchItems = searchHistoryTable.getAllItems(null);

            db.execSQL("DROP TABLE IF EXISTS search_history;");
            onCreate(db);

            if (db.isOpen() && searchHistoryTable != null && searchItems != null && searchItems.size() > 0)
                for (final SearchItem searchItem : searchItems)
                    searchHistoryTable.addItem(searchItem);
        }

        @Override
        public void onDowngrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}