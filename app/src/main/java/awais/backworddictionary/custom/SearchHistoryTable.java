package awais.backworddictionary.custom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.lapism.SearchItem;

public final class SearchHistoryTable {
    private final static int HISTORY_SIZE = 8;
    private final SearchHistoryDatabase dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(final Context mContext) {
        dbHelper = new SearchHistoryDatabase(mContext);
        open();
    }

    public void open() {
        if ((db == null || !db.isOpen()) && dbHelper != null) db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) dbHelper.close();
    }

    public void addItem(final SearchItem item) {
        if (db == null || !db.isOpen()) return;

        final ContentValues values = new ContentValues();
        if (checkText(item.getText().toString())) {
            values.put("_id", getLastItemId() + 1);
            db.update("search_history", values, "_id = ?", new String[]{Integer.toString(getItemId(item))});
        } else {
            values.put("_text", item.getText().toString());
            db.insert("search_history", null, values);
        }
    }

    public List<SearchItem> getAllItems(final String databaseKey) {
        final List<SearchItem> list = new ArrayList<>();
        if (db == null || !db.isOpen()) return list;

        try (Cursor cursor = db.rawQuery(databaseKey == null ?
                "SELECT * FROM search_history ORDER BY _id DESC LIMIT " + HISTORY_SIZE :
                "SELECT * FROM search_history WHERE _text = " + databaseKey, null)) {

            if (cursor.moveToFirst())
                do
                    list.add(new SearchItem(R.drawable.ic_history, cursor.getString(1)));
                while (cursor.moveToNext());
        }

        return list;
    }

    public void clearDatabase(final String key) {
        if (db == null || !db.isOpen()) return;

        if (key == null) db.delete("search_history", null, null);
        else db.delete("search_history", "_text = ?", new String[]{key});
    }

    //public int getItemsCount() {
    //    if (db == null || !db.isOpen()) return -1;
    //
    //    final Cursor cursor = db.rawQuery("SELECT * FROM search_history;", null);
    //    final int count = cursor.getCount();
    //    cursor.close();
    //    return count;
    //}

    private int getItemId(@NonNull final SearchItem item) {
        final int id;
        try (Cursor res = db.rawQuery("SELECT _id FROM search_history WHERE _text = ?;", new String[]{item.getText().toString()})) {
            res.moveToFirst();
            id = res.getInt(0);
        }
        return id;
    }

    private int getLastItemId() {
        int count;
        try (Cursor res = db.rawQuery("SELECT _id FROM search_history", null)) {
            count = 0;
            if (res.moveToLast()) count = res.getInt(0);
        }
        return count;
    }

    private boolean checkText(final String text) {
        final boolean ret;
        try (Cursor cursor = db.rawQuery("SELECT _text FROM search_history WHERE _text = ?;", new String[]{text})) {
            ret = cursor.moveToFirst();
        }
        return ret;
    }

    private final static class SearchHistoryDatabase extends SQLiteOpenHelper {
        SearchHistoryDatabase(Context context) {
            super(context, "search_history_database.db", null, 4);
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS search_history ( _id INTEGER PRIMARY KEY AUTOINCREMENT, _text TEXT );");
        }

        @Override
        public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS search_history");
            onCreate(db);
        }

        @Override
        public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {}
    }
}