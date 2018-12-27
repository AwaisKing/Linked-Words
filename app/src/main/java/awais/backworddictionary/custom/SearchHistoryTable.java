package awais.backworddictionary.custom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.R;
import awais.lapism.SearchItem;

public class SearchHistoryTable {
    private static int mHistorySize = 4;
    private final SearchHistoryDatabase dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(Context mContext) {
        dbHelper = new SearchHistoryDatabase(mContext);
        open();
    }

    public void open() {
        if (db == null && dbHelper != null) db = dbHelper.getWritableDatabase();
        if (db != null && !db.isOpen() && dbHelper != null) db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) dbHelper.close();
    }

    public void setHistorySize(int historySize) {
        mHistorySize = historySize;
    }

    public void addItem(SearchItem item) {
        if (db == null) return;
        if (!db.isOpen()) return;
        ContentValues values = new ContentValues();
        boolean isTextAvailable = checkText(item.get_text().toString());
        if (!isTextAvailable) {
            values.put("_text", item.get_text().toString());
            db.insert("search_history", null, values);
        } else {
            int lastItemId = getLastItemId();
            values.put("_id", lastItemId + 1);
            db.update("search_history", values, "_id = ?", new String[]{Integer.toString(getItemId(item))});
        }
    }

    List<SearchItem> getAllItems(String databaseKey) {
        List<SearchItem> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM search_history";
        if (databaseKey != null) selectQuery += " WHERE _text = " + databaseKey;
        selectQuery += " ORDER BY _id DESC LIMIT " + mHistorySize;

        if (db == null) return list;
        if (!db.isOpen()) return list;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) do
            list.add(new SearchItem(R.drawable.ic_history_black_24dp, cursor.getString(1)));
        while (cursor.moveToNext());
        cursor.close();

        return list;
    }

    public void clearDatabase(String key) {
        if (db == null) return;
        if (!db.isOpen()) return;
        if (key == null) db.delete("search_history", null, null);
        else db.delete("search_history", "_text = ?", new String[]{key});
    }

    @SuppressWarnings("unused")
    public int getItemsCount() {
        if (db == null) return -1;
        if (!db.isOpen()) return -1;
        Cursor cursor = db.rawQuery("SELECT * FROM search_history;", null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private int getItemId(SearchItem item) {
        Cursor res = db.rawQuery("SELECT _id FROM search_history WHERE _text = ?;", new String[]{item.get_text().toString()});
        res.moveToFirst();
        int id = res.getInt(0);
        res.close();
        return id;
    }

    private int getLastItemId() {
        String sql = "SELECT _id FROM search_history";
        Cursor res = db.rawQuery(sql, null);
        int count = 0;
        if (res.moveToLast()) count = res.getInt(0);
        res.close();
        return count;
    }

    private boolean checkText(String text) {
        Cursor cursor = db.rawQuery("SELECT _text FROM search_history WHERE _text = ?;", new String[]{text});
        boolean ret = cursor.moveToFirst();
        cursor.close();
        return ret;
    }

    class SearchHistoryDatabase extends SQLiteOpenHelper {
//        static final String SEARCH_HISTORY_TABLE = "search_history";
//        static final String SEARCH_HISTORY_COLUMN_ID = "_id";
//        static final String SEARCH_HISTORY_COLUMN_TEXT = "_text";

        SearchHistoryDatabase(Context context) {
            super(context, "search_history_database.db", null, 4);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS search_history ( _id INTEGER PRIMARY KEY AUTOINCREMENT, _text TEXT );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS search_history");
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        @Override public void onOpen(SQLiteDatabase db) {}
    }
}