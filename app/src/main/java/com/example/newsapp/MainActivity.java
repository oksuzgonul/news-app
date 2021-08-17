package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Article>> {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?";

    private static final int ARTICLE_LOADER_ID = 1;

    private ArticleAdapter mArticleAdapter;

    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView articleListView = findViewById(R.id.list);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        articleListView.setEmptyView(mEmptyStateTextView);

        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());
        articleListView.setAdapter(mArticleAdapter);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.not_connected_to_internet);
        }
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences orderPreference = PreferenceManager
                .getDefaultSharedPreferences(this);
        String orderBy = orderPreference.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        SharedPreferences sectionPreference = PreferenceManager
                .getDefaultSharedPreferences(this);
        String section = sectionPreference.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default));

        SharedPreferences queryPreference = PreferenceManager
                .getDefaultSharedPreferences(this);
        String queryRaw = queryPreference.getString(
                getString(R.string.settings_query_key),
                getString(R.string.settings_query_default));

        //Trim the query and split it into a string array by spaces
        String[] splitQuery = queryRaw.trim().split("\\s+");
        //removing non-alphanumerical characters to avoid broken URL's. and concatenate with AND
        String query = String.join(" AND ", splitQuery)
                .replaceAll("[^a-zA-Z0-9]","");

        Uri readUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = readUri.buildUpon();

        uriBuilder.appendQueryParameter("q", query);
        uriBuilder.appendQueryParameter("section", section);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("from-date", "2019-01-01");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("api-key", "GUARDIAN_API_KEY");

        //Logs the request URL here, easy for reviewer to check
        Log.e(LOG_TAG, uriBuilder.toString());

        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        mEmptyStateTextView.setText(R.string.no_articles);
        mArticleAdapter.clear();

        if (articles != null && !articles.isEmpty()) {
            mArticleAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {

        mArticleAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
