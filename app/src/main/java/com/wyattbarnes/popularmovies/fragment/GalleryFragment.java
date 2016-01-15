package com.wyattbarnes.popularmovies.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.activity.DetailActivity;
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
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {
    // Constant for fetching state
    private static final String STATE_GALLERY = "gallery";
    private static final String STATE_SORTING = "sorting";
    // Adapter for getting movies
    private ArrayAdapter<Movie> galleryAdapter;

    public GalleryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Store the fetched movies
        ArrayList<Movie> movieList = new ArrayList<Movie>();
        for (int i = 0; i < galleryAdapter.getCount(); i++) {
            movieList.add(galleryAdapter.getItem(i));
        }
        savedInstanceState.putParcelableArrayList(STATE_GALLERY, movieList);

        // Store settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        savedInstanceState.putString(STATE_SORTING, preferences.getString(
                getString(R.string.pref_key_sort), getString(R.string.pref_sort_default)));

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Restore movies from saved state if it exists
        if (savedInstanceState != null) {
            ArrayList<Movie>gallery = savedInstanceState.getParcelableArrayList(STATE_GALLERY);
            galleryAdapter = new GalleryAdapter(
                    getActivity(),
                    gallery);

            // If settings have changed, fetch movies again
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            String saved_sorting = savedInstanceState
                    .getString(getString(R.string.pref_sort_default));
            String current_sorting = preferences.getString(
                    getString(R.string.pref_key_sort), getString(R.string.pref_sort_default));
            if (saved_sorting != null && !saved_sorting.equals(current_sorting)) {
                updateGallery();
            }
        } else {
            // No movies, so update the gallery
            galleryAdapter = new GalleryAdapter(
                    getActivity(),
                    new ArrayList<Movie>());

            updateGallery();
        }

        //Inflate the view and set adapter
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view_gallery);
        gridView.setAdapter(galleryAdapter);

        // Transition to detail activity if a movie is clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = galleryAdapter.getItem(position);
                String releaseDateStr = getString(R.string.no_date_set);
                if (movie.releaseDate != null) {
                    releaseDateStr = Long.toString(movie.releaseDate.getTime());
                }
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(getString(R.string.movie_key), movie);
                startActivity(detailIntent);
            }
        });

        return rootView;

    }

    public void updateGallery() {
        // Create new FetchGalleryTask
        FetchGalleryTask fetchGalleryTask = new FetchGalleryTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = preferences.getString(
                getString(R.string.pref_key_sort),
                getString(R.string.pref_sort_default));
        fetchGalleryTask.execute(sortBy);
    }

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
            if (result != null) {
                galleryAdapter.clear();
                galleryAdapter.addAll(result);
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
                builder.setContext(getContext());

                // Build it!
                galleryList[i] = builder.build();

            }

            return galleryList;
        }

    }
}
