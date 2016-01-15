package com.wyattbarnes.popularmovies.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.model.Movie;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A placeholder fragment displaying detailed information about a movie.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get root view
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Get intent
        Intent intent = getActivity().getIntent();

        // Get views to fill in from root view
        TextView overviewView = (TextView) rootView.findViewById(R.id.detail_view_overview);
        TextView voteAverageView = (TextView) rootView.findViewById(R.id.detail_view_vote_average);
        TextView titleView = (TextView) rootView.findViewById(R.id.detail_view_title);
        TextView releaseDateView = (TextView) rootView.findViewById(R.id.detail_view_release_date);
        ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_view_poster);

        Movie movie = (Movie) intent.getParcelableExtra(getString(R.string.movie_key));

        // Overview
        if (movie.overview != null && "".equals(movie.overview)) {
            overviewView.setText(getString(R.string.no_overview_set));
        } else {
            overviewView.setText(movie.overview);
        }

        // Vote Average
        voteAverageView.setText(getString(R.string.vote_average) + ": "
                + movie.voteAverage + "/" + Integer.toString(Movie.MAX_VOTE_AVERAGE));

        // Title
        if (movie.title != null && "".equals(movie.title)) {
            titleView.setText(getString(R.string.no_title_set));
        } else {
            titleView.setText(movie.title);
        }

        // Release date
        if (movie.releaseDate == null) {
            releaseDateView.setText(getString(R.string.no_date_set));
        } else {
            DateFormat format = new SimpleDateFormat(getString(R.string.release_date_format));
            releaseDateView.setText(format.format(movie.releaseDate));
        }

        // Poster
        Picasso
                .with(getContext())
                .load(movie.getDetailPosterUrl())
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.no_image_available)
                .fit()
                .centerCrop()
                .into((ImageView) rootView.findViewById(R.id.detail_view_poster));

        return rootView;
    }
}
