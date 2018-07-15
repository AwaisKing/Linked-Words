package awais.backworddictionary.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import awais.backworddictionary.custom.WordItem;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static awais.backworddictionary.Main.searchAdapter;

public class SearchAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private final OkHttpClient client = new OkHttpClient();
    private final AtomicReference<SearchView> mSearchView = new AtomicReference<>();
    private ArrayList<SearchItem> suggestionsList;

    public SearchAsync(SearchView mSearchView) {
        this.mSearchView.set(mSearchView);
        this.suggestionsList = new ArrayList<>();
        searchAdapter.setSuggestionsList(suggestionsList);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mSearchView != null && mSearchView.get() != null)
            mSearchView.get().showProgress();
    }

    @Override
    protected ArrayList<WordItem> doInBackground(String... params) {
        String query;
        try {
            query = URLEncoder.encode(params[0], "UTF-8");
        } catch (Exception e) {
            query = params[0].replace("&", "%26").replace("@","%40").replace("#","%23");
        }
        ArrayList<WordItem> arrayList = new ArrayList<>();

        Response response = null;
        try {
            if (isCancelled()) {
                client.dispatcher().cancelAll();
                client.connectionPool().evictAll();
                client.cache().close();
            }

            Call call = client.newCall(new Request.Builder().url("https://api.datamuse.com/sug?s=" + query).build());
            response = call.execute();

            if (isCancelled()) {
                call.cancel();
                response.close();
            }

            if (response != null && response.code() == 200) {
                //noinspection ConstantConditions
                arrayList = new Gson().fromJson(response.body().string(),
                        new TypeToken<List<WordItem>>(){}.getType());
            }
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "", e);
        } finally {
            if (response != null) response.close();
        }
        return arrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<WordItem> result) {
        super.onPostExecute(result);
        if (!isCancelled()) {
            if (mSearchView != null && mSearchView.get() != null)
                if (mSearchView.get().isShowingProgress()) mSearchView.get().hideProgress();
            if (!result.isEmpty() && mSearchView != null && mSearchView.get() != null
                    && searchAdapter != null) {
                if (suggestionsList != null) suggestionsList.clear();
                else suggestionsList = new ArrayList<>();
                for (WordItem item : result) suggestionsList.add(new SearchItem(item.getWord()));
                searchAdapter.setData(suggestionsList);
                searchAdapter.setSuggestionsList(suggestionsList);
                searchAdapter.notifyDataSetChanged();
                mSearchView.get().showSuggestions();
            }
        }
    }
}
