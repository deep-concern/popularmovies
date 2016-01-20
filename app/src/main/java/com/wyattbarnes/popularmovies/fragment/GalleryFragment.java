package com.wyattbarnes.popularmovies.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.activity.DetailActivity;
import com.wyattbarnes.popularmovies.adapter.GalleryAdapter;
import com.wyattbarnes.popularmovies.model.Movie;
import com.wyattbarnes.popularmovies.task.FetchGalleryTask;

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

import butterknife.Bind;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {
    // Views
    @Bind(R.id.grid_view_gallery) GridView mGalleryView;
    @Bind(R.id.no_connection) TextView mConnectionView;
    // Constant for fetching state
    private static final String STATE_GALLERY = "gallery";
    private static final String STATE_SORTING = "sorting";

    // Adapter for getting movies
    private GalleryAdapter galleryAdapter;

    // Checking for if app has resumed
    private boolean mIsResumed = true;

    // Internet connectivity
    private boolean mIsConnected = false;

    // Checking for internet connection
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (mIsResumed && networkInfo != null && networkInfo.isConnected()) {
                mIsConnected = true;
                mGalleryView.setVisibility(View.INVISIBLE);

            } else {
                mIsConnected = false;
                mConnectionView.setVisibility(View.VISIBLE);
            }
        }
    };

    public GalleryFragment() {
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsResumed = true;
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

        // Set adapter
        mGalleryView.setAdapter(galleryAdapter);

        // Transition to detail activity if a movie is clicked
        mGalleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    public void updateGallery() {
        // Run only if internet is available
        if (mIsConnected) {
            // Create new FetchGalleryTask
            FetchGalleryTask fetchGalleryTask = new FetchGalleryTask(getContext(), galleryAdapter);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = preferences.getString(
                    getString(R.string.pref_key_sort),
                    getString(R.string.pref_sort_default));
            fetchGalleryTask.execute(sortBy);
        }
    }
}
