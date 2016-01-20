package com.wyattbarnes.popularmovies.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;

import com.squareup.picasso.Picasso;
import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.model.Movie;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A placeholder fragment displaying detailed information about a movie.
 */
public class DetailActivityFragment extends Fragment {
    @Bind(R.id.detail_view_overview) TextView mOverviewView;
    @Bind(R.id.detail_view_vote_average) TextView mVoteAverageView;
    @Bind(R.id.detail_view_title) TextView mTitleView;
    @Bind(R.id.detail_view_release_date) TextView mReleaseDateView;
    @Bind(R.id.detail_view_poster) ImageView mPosterView;


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get movie from intent
        Movie movie = (Movie) getActivity()
                .getIntent()
                .getParcelableExtra(getString(R.string.movie_key));

        // Overview
        if (movie.overview != null && "".equals(movie.overview)) {
            mOverviewView.setText(getString(R.string.no_overview_set));
        } else {
            mOverviewView.setText(movie.overview);
        }

        // Vote Average
        mVoteAverageView.setText(getString(R.string.vote_average) + ": "
                + movie.voteAverage + "/" + Integer.toString(Movie.MAX_VOTE_AVERAGE));

        // Title
        if (movie.title != null && "".equals(movie.title)) {
            mTitleView.setText(getString(R.string.no_title_set));
        } else {
            mTitleView.setText(movie.title);
        }

        // Release date
        if (movie.releaseDate == null) {
            mReleaseDateView.setText(getString(R.string.no_date_set));
        } else {
            DateFormat format = new SimpleDateFormat(getString(R.string.release_date_format));
            mReleaseDateView.setText(format.format(movie.releaseDate));
        }

        // Poster
        Picasso
                .with(getContext())
                .load(movie.getDetailPosterUrl())
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.no_image_available)
                .fit()
                .centerCrop()
                .into(mPosterView);

        return inflater.inflate(R.layout.fragment_detail, container, false);
    }
}
