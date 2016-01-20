package com.wyattbarnes.popularmovies.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.wyattbarnes.popularmovies.adapter.GalleryAdapter;
import com.wyattbarnes.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by wbarnes on 1/16/16.
 */
public class FetchGalleryTask extends AsyncTask<String, Void, Movie[]> {
    // Tag for logging purposes
    private final String LOG_TAG = FetchGalleryTask.class.getSimpleName();

    // Constants for API
    private final String API_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
    private final String API_ID_PARAM = "api_key";
    private final String SORT_PARAM = "sort_by";

    // TODO add API key
    // Api key
    private final String API_KEY = "ADD_API_KEY_HERE";

    private Context mContext;
    private GalleryAdapter mGalleryAdapter;

    public FetchGalleryTask(Context context, GalleryAdapter galleryAdapter) {
        mContext = context;
        mGalleryAdapter = galleryAdapter;
    }

    @Override
    protected Movie[] doInBackground(String... params) {
        // If not enough parameters, return null
        if (params.length < 1) {
            return null;
        }

        // Set up for making HTTP request
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // String to hold JSON data
        String jsonDataStr = null;

        // Build our URI with the values we set above and add params
        Uri uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendQueryParameter(SORT_PARAM, params[0])
                .appendQueryParameter(API_ID_PARAM, API_KEY)
                .build();

        // Attempt request
        try {
            // Create URL from URI and get connection
            URL galleryUrl = new URL(uri.toString());
            urlConnection = (HttpURLConnection) galleryUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {

            }
            jsonDataStr = buffer.toString();

        } catch (java.io.IOException e) {
            // If there was an issue with the URL, log and return null
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // Attempt to convert string into Movie list
        try {
            return getGalleryDataFromJson(jsonDataStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Movie[] result) {
        if (result != null && mGalleryAdapter != null) {
            mGalleryAdapter.clear();
            mGalleryAdapter.addAll(result);
        }

        super.onPostExecute(result);
    }

    private Movie[] getGalleryDataFromJson(String jsonDataStr) throws JSONException {
        // Constants for traversing the JSON object
        final String ID = "id";
        final String RESULTS = "results";
        final String RELEASE_DATE = "release_date";
        final String TITLE = "title";
        final String OVERVIEW = "overview";
        final String VOTE_AVERAGE = "vote_average";
        final String POSTER_PATH = "poster_path";

        // Get the array of moviews
        JSONObject galleryJson = new JSONObject(jsonDataStr);
        JSONArray movieArray = galleryJson.getJSONArray(RESULTS);

        // Movie list to return
        Movie[] galleryList = new Movie[movieArray.length()];

        // Prepare builder
        Movie.Builder builder = new Movie.Builder();

        // Get ready to parse date
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        // Build the movies
        for(int i = 0; i < movieArray.length(); i++) {
            JSONObject movieJson = movieArray.getJSONObject(i);

            builder.setId(movieJson.getInt(ID));
            builder.setOverview(movieJson.getString(OVERVIEW));
            builder.setPosterPath(movieJson.getString(POSTER_PATH));
            String dateData = movieJson.getString(RELEASE_DATE);
            if (!"".equals(dateData)) {
                try {
                    builder.setReleaseDate(format.parse(dateData));
                } catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    Log.e(LOG_TAG, "Movie at position: " + i);
                    e.printStackTrace();
                    builder.setReleaseDate(null);
                }
            } else {
                builder.setReleaseDate(null);
            }

            builder.setTitle(movieJson.getString(TITLE));
            builder.setVoteAverage((float) movieJson.getDouble(VOTE_AVERAGE));
            builder.setContext(mContext);

            // Build it!
            galleryList[i] = builder.build();

        }

        return galleryList;
    }

}
