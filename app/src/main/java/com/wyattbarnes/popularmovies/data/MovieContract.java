package com.wyattbarnes.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wyatt.barnes on 2/25/16.
 */
public class MovieContract {

    // Content authority for this app
    public static final String CONTENT_AUTHORITY = "com.wyattbarnes.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Movie title
        public static final String COLUMN_TITLE = "title";

        // Overview
        public static final String COLUMN_OVERVIEW = "overview";

        // Vote Average
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        // Release date
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // Poster URL
        public static final String COLUMN_POSTER_URL = "poster_url";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
