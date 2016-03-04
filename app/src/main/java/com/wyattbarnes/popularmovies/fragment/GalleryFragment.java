package com.wyattbarnes.popularmovies.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.activity.DetailActivity;
import com.wyattbarnes.popularmovies.adapter.GalleryAdapter;
import com.wyattbarnes.popularmovies.model.Movie;
import com.wyattbarnes.popularmovies.task.FetchGalleryTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {
    // Logging
    private static final String LOG_TAG = GalleryFragment.class.getSimpleName();

    // Views
    @Bind(R.id.grid_view_gallery) GridView galleryView;
    @Bind(R.id.text_view_no_connection) TextView noConnectionView;
    @Bind(R.id.text_view_offline_bar) TextView offlineBarView;

    // Constant for fetching state
    private static final String STATE_GALLERY = "gallery";
    private static final String STATE_SORTING = "sorting";

    // Constants for actions
    private static final String BROADCAST_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    // Adapter for getting movies
    private GalleryAdapter mGalleryAdapter;

    // Bool for internet connection
    private boolean mIsConnected;
    private boolean mIsOutdated;

    // Broadcast receiver to check internet connection
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsConnected = checkForConnection();
            if (mIsConnected) {
                if (mIsOutdated) {
                    updateGallery();
                }
                hideOfflineView();
            } else {
                displayOfflineView(false);
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated! [savedInstanceState=" + ((savedInstanceState != null) ? savedInstanceState.toString() : "null") + "]");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get root view
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Bind views
        ButterKnife.bind(this, rootView);

        // Initial check for connection
        mIsConnected = checkForConnection();

        // Shared preferences
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        // Restore movies from saved state if it exists
        if (savedInstanceState != null) {
            ArrayList<Movie>gallery = savedInstanceState.getParcelableArrayList(STATE_GALLERY);
            mGalleryAdapter = new GalleryAdapter(
                    getActivity(),
                    gallery);

            // If settings have changed, fetch movies again
            String saved_sorting = savedInstanceState
                    .getString(getString(R.string.pref_sort_default));
            String current_sorting = preferences.getString(
                    getString(R.string.pref_key_sort), getString(R.string.pref_sort_default));
            if (saved_sorting != null && !saved_sorting.equals(current_sorting)) {
                updateGallery();
            }
        } else {
            // No movies, so update the gallery
            mGalleryAdapter = new GalleryAdapter(
                    getActivity(),
                    new ArrayList<Movie>());

            // Special case on app start
            if (mIsConnected) {
                updateGallery();
            } else {
                displayOfflineView(true);
            }
        }

        // Set adapter
        galleryView.setAdapter(mGalleryAdapter);

        // Transition to detail activity if a movie is clicked
        galleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsConnected) {
                    // Package movie and send to intent
                    Movie movie = mGalleryAdapter.getItem(position);
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(getString(R.string.movie_key), movie);
                    startActivity(detailIntent);
                } else {
                    showNoConnectionToast();
                }
            }
        });

        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Store the fetched movies
        ArrayList<Movie> movieList = new ArrayList<Movie>();
        for (int i = 0; i < mGalleryAdapter.getCount(); i++) {
            movieList.add(mGalleryAdapter.getItem(i));
        }
        savedInstanceState.putParcelableArrayList(STATE_GALLERY, movieList);

        // Store settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        savedInstanceState.putString(STATE_SORTING, preferences.getString(
                getString(R.string.pref_key_sort), getString(R.string.pref_sort_default)));
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void updateGallery() {
        // Run only if internet is available
        if (mIsConnected) {
            // Create new FetchGalleryTask
            FetchGalleryTask fetchGalleryTask = new FetchGalleryTask(getContext(), mGalleryAdapter);
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String sortBy = preferences.getString(
                    getString(R.string.pref_key_sort),
                    getString(R.string.pref_sort_default));
            fetchGalleryTask.execute(sortBy);
            mIsOutdated = false;
        } else {
            showNoConnectionToast();
            mIsOutdated = true;
        }
    }

    private boolean checkForConnection() {
        // Get network info
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // Check if connected
        return networkInfo != null && networkInfo.isConnected();
    }

    private void hideOfflineView() {
        // Remove the bar indicating the app is offline
        offlineBarView.setVisibility(View.GONE);

        // If gallery is not displayed, display it
        if (galleryView.getVisibility() == View.INVISIBLE) {
            noConnectionView.setVisibility(View.INVISIBLE);
            galleryView.setVisibility(View.VISIBLE);
            updateGallery();
        }
    }

    private void displayOfflineView(boolean hideGallery) {
        // Show a bar indicating the app is offline
        offlineBarView.setVisibility(View.VISIBLE);

        // If no movies have been fetched, display offline message in main view
        if (hideGallery) {
            noConnectionView.setVisibility(View.VISIBLE);
            galleryView.setVisibility(View.INVISIBLE);
        }
    }

    private void showNoConnectionToast() {
        Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG_TAG, "Fragment:onStop!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(LOG_TAG, "Fragment:onDestroy!");
    }

}
