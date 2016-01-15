package com.wyattbarnes.popularmovies.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import java.util.Date;

/**
 * Created by wyatt.barnes on 1/12/16.
 */
public class Movie implements Parcelable {
    // Constants
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
    private static final String IMAGE_SIZE_PREFIX = "/w";
    private static final int IMAGE_SIZE_W92 = 92;
    private static final int IMAGE_SIZE_W154 = 154;
    private static final int IMAGE_SIZE_W185 = 185;
    private static final int IMAGE_SIZE_W342 = 342;
    private static final int IMAGE_SIZE_W500 = 500;
    private static final String LOG_TAG = Movie.class.getSimpleName();

    // Movie poster aspect ratio
    public static final float ASPECT_RATIO = 278F/185F;

    // Maximum vote average for movies
    public static final int MAX_VOTE_AVERAGE = 10;

    // Attributes
    public int galleryImageSize;
    public int detailImageSize;
    public int id;
    public float voteAverage;
    public Date releaseDate;
    public String overview;
    public String posterPath;
    public String title;

    public Movie() { }

    public Movie (
            int id,
            float voteAverage,
            Context context,
            Date releaseDate,
            String overview,
            String posterPath,
            String title
    ) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.posterPath = posterPath;
        this.title = title;

        // Determine image size based on dpi
        if (context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            if (metrics.densityDpi >= DisplayMetrics.DENSITY_XXHIGH) { // XXHIGH
                galleryImageSize = IMAGE_SIZE_W342;
                detailImageSize = IMAGE_SIZE_W500;
            } else if (metrics.densityDpi >= DisplayMetrics.DENSITY_XHIGH
                    && metrics.densityDpi < DisplayMetrics.DENSITY_XXHIGH) { // XHIGH
                galleryImageSize = IMAGE_SIZE_W185;
                detailImageSize = IMAGE_SIZE_W342;
            } else if (metrics.densityDpi >= DisplayMetrics.DENSITY_HIGH
                    && metrics.densityDpi < DisplayMetrics.DENSITY_XHIGH) { // HIGH
                galleryImageSize = IMAGE_SIZE_W154;
                detailImageSize = IMAGE_SIZE_W185;
            } else { // Everything else
                galleryImageSize = IMAGE_SIZE_W92;
                detailImageSize = IMAGE_SIZE_W154;
            }
        } else {
            galleryImageSize = IMAGE_SIZE_W92;
            detailImageSize = IMAGE_SIZE_W154;
        }
    }

    private Movie(Parcel in) {
        this.galleryImageSize = in.readInt();
        this.detailImageSize = in.readInt();
        this.id = in.readInt();
        this.voteAverage = in.readFloat();
        Date date = new Date();
        date.setTime(in.readLong());
        this.releaseDate = date;
        this.overview = in.readString();
        this.posterPath = in.readString();
        this.title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.galleryImageSize);
        out.writeInt(this.detailImageSize);
        out.writeInt(this.id);
        out.writeFloat(this.voteAverage);
        if (this.releaseDate != null) {
            out.writeLong(this.releaseDate.getTime());
        } else {
            out.writeLong(0L);
        }
        out.writeString(this.overview);
        out.writeString(this.posterPath);
        out.writeString(this.title);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getGalleryPosterUrl() {
        return buildImageUrl(galleryImageSize);
    }

    public String getDetailPosterUrl() {
        return buildImageUrl(detailImageSize);
    }

    public String getOriginalPosterUrl() {
        return IMAGE_BASE_URL
                + "/original"
                + this.posterPath;
    }

    private String buildImageUrl(int size) {
        return IMAGE_BASE_URL
                + IMAGE_SIZE_PREFIX
                + Integer.toString(size)
                + this.posterPath;
    }

    public static class Builder {
        // Attributes
        private int id;
        private float voteAverage;
        private Date releaseDate;
        private String title;
        private String posterPath;
        private String overview;
        private Context context;

        public Builder() { }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setReleaseDate(Date releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder setVoteAverage(float voteAverage) {
            this.voteAverage = voteAverage;
            return this;
        }

        public Builder setOverview(String overview) {
            this.overview = overview;
            return this;
        }

        public Builder setPosterPath(String posterPath) {
            this.posterPath = posterPath;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Movie build() {
            return new Movie(
                    this.id,
                    this.voteAverage,
                    this.context,
                    this.releaseDate,
                    this.overview,
                    this.posterPath,
                    this.title
            );
        }

        @Override
        public String toString() {
            return "Movie.Builder["
                    + "id=" + id
                    + ", voteAverage=" + voteAverage
                    + ", context=" + context.toString()
                    + ", releaseDate=" + releaseDate.toString()
                    + ", overview=" + overview
                    + ", posterPath=" + posterPath
                    + ", title=" + title + "]";
        }
    }


    @Override
    public String toString() {
        return "Movie["
                + "detailImageSize=" + detailImageSize
                + ", galleryImageSize=" + galleryImageSize
                + ", id=" + id
                + ", voteAverage=" + voteAverage
                + ", releaseDate=" + releaseDate
                + ", overview=" + overview
                + ", posterPath=" + posterPath
                + ", title=" + title + "]";
    }
}
