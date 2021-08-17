package com.example.newsapp;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();
    private QueryUtils() {}

    public static List<Article> fetchArticleData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem with HTTP request.", e);
        }
        List<Article> articles = extractContentFromJson(jsonResponse);
        return articles;
    }

    private static URL createUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL.", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error with response code: " +
                        urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not retrieve the article JSON response", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Article> extractContentFromJson(String contentJson) {
        if (TextUtils.isEmpty(contentJson)) {
            return null;
        }

        List<Article> articles = new ArrayList<>();

        try {
            JSONObject completeResponse = new JSONObject(contentJson);
            JSONObject response = completeResponse.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonObject = results.getJSONObject(i);
                JSONArray tags = jsonObject.getJSONArray("tags");
                String title = jsonObject.getString("webTitle");
                String section = jsonObject.getString("sectionName");
                String url = jsonObject.getString("webUrl");
                String author = getAuthorFromJson(tags);

                if (jsonObject.has("webPublicationDate") && author != null) {
                    //Converting datetime string received in JSON to datetime object
                    DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime
                            .parse(jsonObject.getString("webPublicationDate"), format);

                    //Converting datetime object to the string format that will be used in the app
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
                    String dateString = "Published on " + dateTime.format(formatter);

                    String authorText = "by " + author;

                    Article article = new Article(title, section, authorText, dateString, url);
                    articles.add(article);

                } else {
                    Article article = new Article(title, section, url);
                    articles.add(article);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing JSON", e);
        }

        return articles;
    }

    private static String getAuthorFromJson(JSONArray tags) throws JSONException {
        if (tags == null || TextUtils.isEmpty(tags.toString())) {
            return null;
        }
        for (int i = 0; i < tags.length(); i++) {
            JSONObject object = tags.getJSONObject(i);
            if (object.has("webTitle") && object.has("type")) {
                if (object.getString("type").equals("contributor")) {
                    return object.getString("webTitle");
                }
            }
        }
        return null;
    }
}
