package com.michaelmagdy.mediastylenotification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    static MediaPlayer myMediaPlayer;
    private ImageView pausePlayBtn;

    private NotificationCompat.Builder notificationBuilder;
    private static MediaSessionCompat mediaSessionCompat;
    private PlaybackStateCompat.Builder stateBuilder;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "channelID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        createMediaPlayer();
        handleClicks();
        createMediaSession();
        createNotificationChannel();
    }

    @Override
    protected void onStop() {
        super.onStop();

        showNotification();
    }

    private void initViews() {

        pausePlayBtn = findViewById(R.id.play_pause_btn);
        pausePlayBtn.setBackgroundResource(R.drawable.play);
    }

    private void createMediaPlayer() {

        myMediaPlayer = MediaPlayer.create(this, R.raw.counting);
    }

    private void handleClicks() {

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });
    }

    private void createMediaSession(){

        mediaSessionCompat = new MediaSessionCompat(this, "simple player session");

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                );
        mediaSessionCompat.setMediaButtonReceiver(null);
        mediaSessionCompat.setPlaybackState(stateBuilder.build());
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();

                playPauseSong();
                showNotification();
            }

            @Override
            public void onPause() {
                super.onPause();

                playPauseSong();
                showNotification();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

            }
        });

        mediaSessionCompat.setActive(true);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(){

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        int icon;
        String playPause;
        if (myMediaPlayer.isPlaying()){
            icon = R.drawable.pause;
            playPause = "pause";
        } else {
            icon = R.drawable.play;
            playPause = "play";
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, playPause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)
        );


        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class),0
        );

        notificationBuilder.setContentTitle("Song Title")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(playPauseAction)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken())
                        .setShowActionsInCompactView(0));

        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

    }

    private void playPauseSong() {

        if (myMediaPlayer.isPlaying()) {
            pausePlayBtn.setBackgroundResource(R.drawable.play);
            myMediaPlayer.pause();
        } else {
            pausePlayBtn.setBackgroundResource(R.drawable.pause);
            myMediaPlayer.start();
        }
    }

    public static class MyReceiver extends BroadcastReceiver {

        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        }
    }
}
