package edu.temple.lab9;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity {

    AudiobookService.MediaControlBinder player;
    boolean bound = false;

    int nowPlaying = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Button searchButton = (Button) findViewById(R.id.searchButton);
        Button playButton = (Button) findViewById(R.id.playButton);
        Button pauseButton = (Button) findViewById(R.id.pauseButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView search = findViewById(R.id.input);
                String id = search.getText().toString();
                player.play(Integer.valueOf(id));
                nowPlaying = Integer.valueOf(id);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                player.play(nowPlaying);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                player.pause();
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AudiobookService.MediaControlBinder binder = (AudiobookService.MediaControlBinder) service;
            player = binder;
            Toast.makeText(getApplicationContext(), "Bind successful", Toast.LENGTH_SHORT).show();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


}
