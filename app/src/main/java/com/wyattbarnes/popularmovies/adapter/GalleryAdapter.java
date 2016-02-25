package com.wyattbarnes.popularmovies.adapter;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.model.Movie;

import java.util.List;

/**
 * Created by wyatt.barnes on 1/13/16.
 *
 * Adapter to handle movie information.
 *
 * Based off of this custom Udacity adapter:
 * https://github.com/udacity/android-custom-arrayadapter/blob/master/app/src/main/java/demo/example/com/customarrayadapter/AndroidFlavorAdapter.java
 */
public class GalleryAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = GalleryAdapter.class.getSimpleName();

    // To be used for resizing images
    private int mPosterWidth = -1;
    private int mPosterHeight = -1;

    /**
     * Custom constructor
     *
     * @param context   Current context.
     * @param movies    List of Movie objects to display
     */
    public GalleryAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    /**
     * View for an AdapterView.
     *
     * @param position      AdapterView position that is requesting the view
     * @param convertView   Recycled view to populate
     * @param parent        Parent ViewGroup that is used for inflation
     * @return              The View for the position in the AdapterView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the Movie object at position
        Movie movie = getItem(position);

        // View recyling
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_gallery, parent, false);
        }

        // Return if no movie
        if (movie == null) {
            return convertView;
        }

        // If poster width isn't set, set the size of each poster and number of columns to display
        if (mPosterWidth == -1) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

            int postersPerScreen = (int) Math.ceil(((double) metrics.widthPixels)/((double) movie.galleryImageSize));

            if (parent == null) {
                return convertView;
            }

            // Set GridView's number of columns
            GridView gridView = (GridView) parent;
            gridView.setNumColumns(postersPerScreen);

            // Set poster size
            mPosterWidth = metrics.widthPixels/postersPerScreen;
            mPosterHeight = (int) (mPosterWidth * Movie.ASPECT_RATIO);

        }

        // Get the ImageView setup with proper sizing and source image
        ImageView poster = (ImageView) convertView.findViewById(R.id.grid_item_poster);
        poster.setMaxWidth(mPosterWidth);
        poster.setMaxHeight(mPosterHeight);
        Picasso
                .with(getContext())
                .load(movie.getGalleryPosterUrl())
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.no_image_available)
                .fit()
                .centerCrop()
                .into(poster);

        return convertView;
    }
}
