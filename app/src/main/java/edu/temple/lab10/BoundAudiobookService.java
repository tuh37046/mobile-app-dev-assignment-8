package edu.temple.lab10;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.temple.audiobookplayer.AudiobookService;

public class BoundAudiobookService extends AudiobookService {

    private final IBinder myBinder = new BABinder();

    public BoundAudiobookService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


    public class BABinder extends Binder {
        BoundAudiobookService getService() {
            return BoundAudiobookService.this;
        }
    }
}