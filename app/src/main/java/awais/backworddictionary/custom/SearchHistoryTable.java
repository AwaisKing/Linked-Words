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

public class SearchHistoryTable {
    private final static int HISTORY_SIZE = 8;
    private final SearchHistoryDatabase dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(Context mContext) {
        dbHelper = new SearchHistoryDatabase(mContext);
        open();
    }

    public void open() {
        if ((db == null || !db.isOpen()) && dbHelper != null) db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) dbHelper.close();
    }

    public void addItem(SearchItem item) {
        if (db == null || !db.isOpen()) return;

        final ContentValues values = new ContentValues();
        final boolean isTextAvailable = checkText(item.get_text().toString());

        if (!isTextAvailable) {
            values.put("_text", item.get_text().toString());
            db.insert("search_history", null, values);
        } else {
            final int lastItemId = getLastItemId();
            values.put("_id", lastItemId + 1);
            db.update("search_history", values, "_id = ?", new String[] {Integer.toString(getItemId(item))});
        }
    }

    public List<SearchItem> getAllItems(String databaseKey) {
        final List<SearchItem> list = new ArrayList<>();
        if (db == null || !db.isOpen()) return list;

        final Cursor cursor = db.rawQuery(databaseKey == null ?
                "SELECT * FROM search_history ORDER BY _id DESC LIMIT " + HISTORY_SIZE :
                "SELECT * FROM search_history WHERE _text = " + databaseKey, null);

        if (cursor.moveToFirst())
            do
                list.add(new SearchItem(R.drawable.ic_history_black_24dp, cursor.getString(1)));
            while (cursor.moveToNext());
        cursor.close();

        return list;
    }

    public void clearDatabase(String key) {
        if (db == null || !db.isOpen()) return;

        if (key == null) db.delete("search_history", null, null);
        else db.delete("search_history", "_text = ?", new String[] {key});
    }

    public int getItemsCount() {
        if (db == null || !db.isOpen()) return -1;

        final Cursor cursor = db.rawQuery("SELECT * FROM search_history;", null);
        final int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private int getItemId(@NonNull SearchItem item) {
        final Cursor res = db.rawQuery("SELECT _id FROM search_history WHERE _text = ?;", new String[] {item.get_text().toString()});
        res.moveToFirst();
        final int id = res.getInt(0);
        res.close();
        return id;
    }

    private int getLastItemId() {
        final Cursor res = db.rawQuery("SELECT _id FROM search_history", null);
        int count = 0;
        if (res.moveToLast()) count = res.getInt(0);
        res.close();
        return count;
    }

    private boolean checkText(String text) {
        final Cursor cursor = db.rawQuery("SELECT _text FROM search_history WHERE _text = ?;", new String[] {text});
        final boolean ret = cursor.moveToFirst();
        cursor.close();
        return ret;
    }

    private static class SearchHistoryDatabase extends SQLiteOpenHelper {
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