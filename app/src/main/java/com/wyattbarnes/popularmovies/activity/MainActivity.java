package com.wyattbarnes.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.wyattbarnes.popularmovies.R;
import com.wyattbarnes.popularmovies.model.Movie;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Fragment mGalleryFragment;
    private Bundle fragmentState;

    @Override
    public void onStart() {
        super.onStart();
        Log.w(LOG_TAG, "Activity:onStart!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.w(LOG_TAG, "Activity:onCreate!");

    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mGalleryFragment = getSupportFragmentManager()
                .getFragment(inState, getString(R.string.gallery_fragment_key));
        Log.w(LOG_TAG, "Activity:onRestoreInstanceState!");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.w(LOG_TAG, "Activity:onCreateOptionsMenu!");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        Log.w(LOG_TAG, "Activity:onOptionsItemSelected!");

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w(LOG_TAG, "Activity:onPause!");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGalleryFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        getSupportFragmentManager().putFragment(
                outState,
                getString(R.string.gallery_fragment_key),
                mGalleryFragment);
        Log.w(LOG_TAG, "Activity:onSaveInstanceState!");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(LOG_TAG, "Activity:onStop!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(LOG_TAG, "Activity:onDestroy!");
    }
}
