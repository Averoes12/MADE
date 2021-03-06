package com.daff.cataloguemovieapi.view.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.daff.cataloguemovieapi.R;
import com.daff.cataloguemovieapi.db.FavoriteHelper;
import com.daff.cataloguemovieapi.model.movie.ResultsItem;

import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.BACKDROP;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.CONTENT_URI;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.OVERVIEW;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.POSTER;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.REL_DATE;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.TITLE;
import static com.daff.cataloguemovieapi.db.DatabaseContract.FavoriteColumns.VOTE;


public class MovieDetail extends AppCompatActivity {
    ImageView backdrop, poster;
    TextView title, popularity, dateRelease, overview;
    ToggleButton btnFavorite;
    ResultsItem movie;
    boolean state = false;
    final static String URL_BACKDROP = "https://image.tmdb.org/t/p/w500";
    final static String URL_POSTER = "https://image.tmdb.org/t/p/w154";

    FavoriteHelper favoriteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        movie = getIntent().getParcelableExtra("movie");

        backdrop = findViewById(R.id.backdrop_detail);
        poster = findViewById(R.id.poster_detail);
        title = findViewById(R.id.title_detail);
        popularity = findViewById(R.id.genre_detail);
        dateRelease = findViewById(R.id.date_detail);
        overview = findViewById(R.id.desc_detail);
        btnFavorite = findViewById(R.id.button_favorite);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favoriteHelper = new FavoriteHelper(this);
        favoriteHelper = FavoriteHelper.getInstance(this);
        favoriteHelper.open();

        Uri uri = getIntent().getData();
        if (uri != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    movie = new ResultsItem(cursor);
                }
                cursor.close();
            }
        }

        checkFavoriteState();

        btnFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.startAnimation(animationButtonFav());
            checkFavoriteState();
            if (favoriteHelper.cekTitle(movie.getOriginalTitle()).getCount() > 0) {
                state = false;
                btnFavorite.setChecked(true);
                Toast.makeText(this, getString(R.string.failed_favorite), Toast.LENGTH_SHORT).show();
            } else {
                state = true;
                btnFavorite.setChecked(false);
                insertFavorite();
                Toast.makeText(this, getString(R.string.mark_favorite), Toast.LENGTH_SHORT).show();
            }
        });

        movieData();

    }

    void insertFavorite() {
        ContentValues values = new ContentValues();
        values.put(TITLE, movie.getOriginalTitle());
        values.put(REL_DATE, movie.getReleaseDate());
        values.put(OVERVIEW, movie.getOverview());
        values.put(VOTE, movie.getVoteAverage());
        values.put(POSTER, movie.getPosterPath());
        values.put(BACKDROP, movie.getBackdropPath());

        getContentResolver().insert(CONTENT_URI, values);
        Toast.makeText(this, getString(R.string.mark_favorite), Toast.LENGTH_SHORT).show();
    }

    Animation animationButtonFav() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);

        return scaleAnimation;
    }

    void checkFavoriteState() {
        if (favoriteHelper.cekTitle(movie.getOriginalTitle()).getCount() > 0) {
            state = false;
            btnFavorite.setChecked(true);
        } else {
            state = true;
            btnFavorite.setChecked(false);
        }
    }

    void movieData() {

        title.setText(movie.getTitle());
        popularity.setText(String.format(getResources().getString(R.string.vote) + " %s", movie.getVoteAverage()));
        dateRelease.setText(String.format(getResources().getString(R.string.release_date) + " %s", movie.getReleaseDate()));
        overview.setText(String.format(getResources().getString(R.string.overview) + "\n\n %s", movie.getOverview()));
        getSupportActionBar().setTitle(movie.getTitle());

        Glide.with(this).load(url_backdrop + movie.getBackdropPath()).override(800, 600).into(backdrop);
        Glide.with(this).load(url_poster + movie.getPosterPath()).override(512, 512).into(poster);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        favoriteHelper.close();
    }
}
