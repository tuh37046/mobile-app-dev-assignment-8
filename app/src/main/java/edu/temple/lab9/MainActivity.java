package edu.temple.lab9;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity {

    AudiobookService.MediaControlBinder player;
    boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = new Intent(this, BoundAudiobookService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        int i=0;
        while(i<100000) {
            i++;
        }

        if(bound) {
            player.play(300);
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Bind failed";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();

        }
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
            bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

}
