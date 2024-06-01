package com.example.RealFilm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.RealFilm.R;
import com.example.RealFilm.service.MovieService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.Locale;



public class WatchMoviesActivity extends AppCompatActivity {
    private SimpleExoPlayerView videoView;
    private SeekBar seekBar_video, seek_volume;
    private TextView textView_duration, textView_current_duration, text_show_volume, movies_name;
    private ImageView btn_play_pause, btn_replay, btn_forward, btn_back, imageView_volume;
    private LinearLayout linearLayoutController;
    private FrameLayout frameLayout;
    private Uri videoUri;
    private MovieService movieService;
    private SimpleExoPlayer exoPlayer;
    private AudioManager audioManager;
    private boolean checkSH = true, checkPlayPause = true;
    private Handler mHandler;
    Integer movieId,userId;
    @Override
    protected void onPause() {
        super.onPause();

        // Lưu trạng thái và thời gian hiện tại vào SharedPreferences cho người dùng hiện tại
        SharedPreferences sharedPreferences = getSharedPreferences("WatchMoviesActivity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("current_position_" + userId, exoPlayer.getCurrentPosition());
        editor.putBoolean("is_playing_" + userId, exoPlayer.getPlayWhenReady());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Khôi phục trạng thái và thời gian hiện tại từ SharedPreferences cho người dùng hiện tại
        SharedPreferences sharedPreferences = getSharedPreferences("WatchMoviesActivity", Context.MODE_PRIVATE);
        long currentPosition = sharedPreferences.getLong("current_position_" + userId, 0);
        boolean isPlaying = sharedPreferences.getBoolean("is_playing_" + userId, true);

        // Thiết lập thời gian hiện tại và trạng thái của trình phát
        exoPlayer.seekTo(currentPosition);
        exoPlayer.setPlayWhenReady(isPlaying);
    }
    // ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_movies);

        initUi();
        showhideControl();
        initializePlayer();
        startPlayer();
        btn_play_pause_OnClick();
        btnForwardOnClick();
        btnReplayOnClick();
        btnBackOnClick();
        updateSeekBar();
        setSeekBar_video();
        seekBarVolume();
        muteVolumOnClick();
        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    long realDurationMillis = exoPlayer.getDuration();
                    seekBar_video.setMax((int)realDurationMillis);
                    textView_duration.setText(convertMillisToTime(realDurationMillis));
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

        });

    }

    public String convertMillisToTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        return String.format(Locale.getDefault().getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initUi(){
        videoView = findViewById(R.id.videoView);
        seekBar_video = findViewById(R.id.seekBar_video);
        textView_duration = findViewById(R.id.textView_duration);
        textView_current_duration = findViewById(R.id.textView_duration_now);
        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_replay = findViewById(R.id.btn_replay);
        btn_forward = findViewById(R.id.btn_forward);
        btn_back = findViewById(R.id.btn_back);
        linearLayoutController = findViewById(R.id.linearLayoutController);
        frameLayout = findViewById(R.id.frameLayout);
        seek_volume = findViewById(R.id.seek_volume);
        imageView_volume = findViewById(R.id.imageView_volume);
        text_show_volume = findViewById(R.id.text_show_volume);
        movies_name = findViewById(R.id.movies_name);
        mHandler = new Handler();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        String str = intent.getStringExtra("link");
        String str_name = intent.getStringExtra("name");
        String str_year = intent.getStringExtra("year");
        Integer str_movieId = intent.getIntExtra("movieId", 0);
        Integer str_id = intent.getIntExtra("userId", 0);
        System.out.println(movieId);
        movies_name.setText(str_name + " (" +str_year + ")");
        videoUri = Uri.parse(str);
        movieId = str_movieId;
        userId = str_id;

    }
    private void seekBarVolume(){
        seek_volume.setProgress(audioManager.getStreamVolume(exoPlayer.getAudioStreamType()));
        seek_volume.setMax(audioManager.getStreamMaxVolume(exoPlayer.getAudioStreamType()));
        text_show_volume.setText(String.valueOf(exoPlayer.getVolume()));
        float MAX_VOLUME = audioManager.getStreamMaxVolume(exoPlayer.getAudioStreamType());
        setVolume(MAX_VOLUME);
        seek_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(exoPlayer.getAudioStreamType(), i, 0);
                setVolume(MAX_VOLUME);
                checkSH = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkSH = true;
            }
        });
    }

    private void muteVolumOnClick(){
        imageView_volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(exoPlayer.getAudioStreamType(), 0, 0);
                seek_volume.setProgress(0);
                float MAX_VOLUME = audioManager.getStreamMaxVolume(exoPlayer.getAudioStreamType());
                setVolume(MAX_VOLUME);
            }
        });
    }

    private void setVolume(float MAX_VOLUME){
        float VOLUME = (audioManager.getStreamVolume(exoPlayer.getAudioStreamType()) / MAX_VOLUME) *100;
        String formattedStringVolume = String.format("%.0f", VOLUME);

        if (VOLUME == 0){
            imageView_volume.setBackgroundResource(R.drawable.ic_round_volume_off_30);
        } else {
            imageView_volume.setBackgroundResource(R.drawable.ic_round_volume_up_24);
        }
        text_show_volume.setText(formattedStringVolume + "%");
    }

    private void btnBackOnClick() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                exoPlayer.stop();
                Intent intent = new Intent(WatchMoviesActivity.this,MoviesInformationActivity.class);
                System.out.println(movieId);
                intent.putExtra("id", movieId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                onBackPressed();
            }
        });
    }

    private void btnReplayOnClick(){
        btn_replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlayer();
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 30000);
                startPlayer();
            }
        });
    }

    private void btnForwardOnClick(){
        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlayer();
                exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 30000);
                startPlayer();
            }
        });
    }

    private void updateSeekBar(){
        WatchMoviesActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(exoPlayer != null){
                    seekBar_video.setProgress((int)exoPlayer.getCurrentPosition());
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void setSeekBar_video(){
        seekBar_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView_current_duration.setText(convertMillisToTime((long) seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pausePlayer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startPlayer();
                exoPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();


        exoPlayer.stop();
    }


    private void  initializePlayer(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(
                videoUri, dataSourceFactory, extractorsFactory, null, null);

        videoView.setPlayer(exoPlayer);
        exoPlayer.prepare(mediaSource);
    }




    private void btn_play_pause_OnClick(){
        btn_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPlayPause){
                    checkPlayPause = false;
                    btn_play_pause.setBackground(getDrawable(R.drawable.ic_round_play_arrow_24));
                    pausePlayer();

                }
                else {
                    checkPlayPause = true;
                    linearLayoutController.setVisibility(View.VISIBLE);
                    btn_play_pause.setBackground(getDrawable(R.drawable.ic_round_pause_24));
                    startPlayer();
                }
            }
        });
    }

    protected void pausePlayer(){
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.getPlaybackState();
    }

    protected void startPlayer(){
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.getPlaybackState();
    }
    public void showhideControl(){
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSH){
                    linearLayoutController.setVisibility(View.INVISIBLE);
                    checkSH = false;
                }
                else {
                    linearLayoutController.setVisibility(View.VISIBLE);
                    checkSH = true;
                }
            }
        });
    }
}