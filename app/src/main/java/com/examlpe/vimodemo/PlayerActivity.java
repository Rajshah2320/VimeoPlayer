package com.examlpe.vimodemo;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView mPlayerView;
    private String stream;
    private SimpleExoPlayer simpleExoPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mPlayerView = findViewById(R.id.player_view);
        stream=getIntent().getStringExtra("url");

         initPlayer(stream);

    }

    private void initPlayer(String stream) {

        HttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getApplicationContext(), String.valueOf(R.string.app_name)),null,DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true );

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(stream));
        simpleExoPlayer = new SimpleExoPlayer.Builder(PlayerActivity.this).build();
        mPlayerView.setPlayer(simpleExoPlayer);

        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        simpleExoPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        simpleExoPlayer.release();
    }
}
