package awais.floatysearch.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import awais.floatysearch.SearchResultItem;
import awais.floatysearch.onSearchActionsListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressWarnings({"WeakerAccess", "unused"})
public class SearchViewResults implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final int TRIGGER_SEARCH = 1;
    private static final long SEARCH_TRIGGER_DELAY_IN_MS = 400;

    private String sequence;
    private int mPage;
    private SearchTask mSearch;
    private final Handler mHandler;
    private boolean isLoadMore;
    private final ArrayAdapter<SearchResultItem> mAdapter;
    private onSearchActionsListener mListener;

    public SearchViewResults(Context context, String searchQuery) {
        sequence = searchQuery;
        ArrayList<SearchResultItem> searchList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1, searchList);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_SEARCH) {
                    clearAdapter();
                    String sequence = (String) msg.obj;
                    mSearch = new SearchTask();
                    mSearch.execute(sequence);
                }
                return false;
            }
        });
    }

    public void setListView(ListView listView) {
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        listView.setAdapter(mAdapter);
        updateSequence();
    }

    public void updateSequence(String s) {
        sequence = s;
        updateSequence();
    }

    private void updateSequence() {
        mPage = 0;
        isLoadMore = true;

        if (mSearch != null) mSearch.cancel(false);
        if (mHandler != null) mHandler.removeMessages(TRIGGER_SEARCH);
        if (!sequence.isEmpty()) {
            Message searchMessage = new Message();
            searchMessage.what = TRIGGER_SEARCH;
            searchMessage.obj = sequence;
            if (mHandler != null)
                mHandler.sendMessageDelayed(searchMessage, SEARCH_TRIGGER_DELAY_IN_MS);
        } else {
            isLoadMore = false;
            clearAdapter();
        }
    }

    private void clearAdapter() {
        mAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onItemClicked(mAdapter.getItem(position));
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING)
            mListener.onScroll();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
        if (loadMore && isLoadMore) {
            mPage++;
            isLoadMore = false;
//            mSearch = new SearchTask();
//            mSearch.execute(sequence);
        }
    }

    private ArrayList<String> findItem(String query, int page) {
        ArrayList<String> result = new ArrayList<>();
        result.add(query);
        return result;
    }

    public void setSearchProvidersListener(onSearchActionsListener listener) {
        this.mListener = listener;
    }

    @SuppressLint("StaticFieldLeak")
    private class SearchTask extends AsyncTask<String, Void, ArrayList<SearchResultItem>> {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Response response = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.showProgress(true);
        }

        @Override
        protected ArrayList<SearchResultItem> doInBackground(String... params) {
            String query = params[0].replace("&", "%26").replace("@","%40").replace("#","%23");
            ArrayList<SearchResultItem> arrayList = new ArrayList<>();

            builder.url("https://api.datamuse.com/sug?s=" + query);

            try {
                response = client.newCall(builder.build()).execute();
                if (response.code() == 200) {
                    Type type = new TypeToken<List<SearchResultItem>>(){}.getType();
                    arrayList = new Gson().fromJson(response.body().string(), type);
                }
            } catch (Exception e) {
                Log.e("AWAISKING_APP", "", e);
            } finally {
                if (response != null) response.close();
            }

            return arrayList;
            //return findItem(query, mPage);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchResultItem> result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                mListener.showProgress(false);
                if (mPage == 0 && result.isEmpty()) {
                    mListener.listEmpty();
                } else {
                    mAdapter.notifyDataSetInvalidated();
                    mAdapter.addAll(result);
                    mAdapter.notifyDataSetChanged();
                }

            }
        }
    }

}


