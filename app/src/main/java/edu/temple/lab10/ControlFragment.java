package edu.temple.lab10;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.service.controls.Control;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.temple.audiobookplayer.AudiobookService;
import edu.temple.lab10.R;

import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Thread.currentThread;


public class ControlFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";


    private BookList.Book mItem;

    JSONObject progressCache;
    String cacheFilename = ".ProgressCache";

    AudiobookService.MediaControlBinder player;
    boolean bound = false;

    int nowPlayingID = -1;
    int oldProgress = 0;

    public SeekBar seeker;

    public void setDownloaded(String id,boolean isDownloaded) {
        importProgressCache();
        JSONObject books;
        JSONObject thisBook;
        try {
            books = (JSONObject) progressCache.get("books");
        } catch (JSONException e) {
            books = new JSONObject();
        }

        try {
            thisBook = (JSONObject) books.get(String.valueOf(id));
        } catch (JSONException e) {
            thisBook = new JSONObject();
        }

        try {
            thisBook.put("isDownloaded",isDownloaded);
        } catch (JSONException e) {
            Toast.makeText(getActivity(),"Error setting book download status", Toast.LENGTH_SHORT).show();
        }
        try {
            books.put(String.valueOf(id),thisBook);
            progressCache.put("books",books);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeProgressCache();
    }

    public boolean isDownloaded(String id) {
        importProgressCache();
        JSONObject books;
        JSONObject thisBook;
        try {
            books = (JSONObject) progressCache.get("books");
            thisBook = (JSONObject) books.get(String.valueOf(id));
        } catch (JSONException e) {
            thisBook = new JSONObject();
        }

        boolean isDownloaded;

        try {
            isDownloaded = thisBook.getBoolean("isDownloaded");
        } catch (JSONException e) {
            //Toast.makeText(getActivity(),"Book is not downloaded", Toast.LENGTH_SHORT).show();
            try {
                thisBook.put("isDownloaded",false);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            return false;
        }
        try {
            progressCache.put(String.valueOf(id),thisBook);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeProgressCache();
        return isDownloaded;
    }

    public void downloadInThread(String id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                downloadFile(id);
            }
        });

        thread.start();
        setDownloaded(id,true);
    }

    public void downloadFile(String id) {
        String url = "https://kamorris.com/lab/audlib/download.php?id="+id;
        BufferedInputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new URL(url).openStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File file = new File(getContext().getFilesDir(), id+".mp3");
        FileOutputStream fileOS;
        try {
            fileOS = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        byte data[] = new byte[1024];
        int byteContent;
        while (true) {
            try {
                if (!((byteContent = inputStream.read(data, 0, 1024)) != -1)) break;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                fileOS.write(data, 0, byteContent);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            fileOS.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Toast.makeText(getActivity(),"Finished downloading book "+id, Toast.LENGTH_SHORT).show();
    }

    public void writeProgressCache() {
        File file = new File(getContext().getFilesDir(), cacheFilename);
        try {
            FileOutputStream outputStream  = new FileOutputStream(file);
            outputStream.write(progressCache.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlaying(int id) {
        importProgressCache();
        try {
            progressCache.put("nowPlaying",id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeProgressCache();
    }

    public void updateProgress(int nowPlayingID) {
        importProgressCache();
        JSONObject books;
        JSONObject thisBook;
        try {
           books = (JSONObject) progressCache.get("books");
           thisBook = (JSONObject) books.get(String.valueOf(nowPlayingID));
        } catch (JSONException e) {
            thisBook = new JSONObject();
        }

        try {
            thisBook.put("progress",oldProgress);
        } catch (JSONException e) {
            Toast.makeText(getActivity(),"Progress update failed", Toast.LENGTH_SHORT).show();
        }
        try {
            progressCache.put(String.valueOf(nowPlayingID),thisBook);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeProgressCache();
    }

    public void tryInitProgressCache()  {
        if(!importProgressCache()) {
            Toast.makeText(getActivity(),"Created cache file", Toast.LENGTH_SHORT).show();
            try {
                String blank = "{\"nowPlaying\":-1,\"books\":{}}";
                progressCache = new JSONObject(blank);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        writeProgressCache();
        Toast.makeText(getActivity(),progressCache.toString(), Toast.LENGTH_LONG).show();
    }

    public boolean importProgressCache() {
        File file = new File(getContext().getFilesDir(), cacheFilename);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            progressCache = new JSONObject(text.toString());
            //Toast.makeText(getActivity(),"Imported progress cache", Toast.LENGTH_SHORT).show();
            return true;
        } catch(JSONException e) {
            //Toast.makeText(getActivity(),"Error importing progress cache", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public ControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tryInitProgressCache();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = BookList.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.title);
            }
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_item_detail_new, container, false);

        Button playButton = (Button) rootView.findViewById(R.id.playButton);
        Button pauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        Button stopButton = (Button) rootView.findViewById(R.id.stopButton);
        TextView nowPlaying = (TextView) rootView.findViewById(R.id.nowPlaying);
        ImageView cover = rootView.findViewById(R.id.cover);

        if(!isDownloaded(mItem.id)) {
            //playButton.setText(R.string.stream);
        }

        seeker = rootView.findViewById(R.id.seekBar);

        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                if(fromUser) {
                    player.seekTo((int)(seeker.getProgress()*(mItem.duration/(float)100)));
                    oldProgress = seeker.getProgress();
                    updateProgress(nowPlayingID);
                }

            }
        });


        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    while (bound == false || player.isPlaying() == false) {
                    }
                } catch(IllegalStateException e) {
                    //
                    return;
                }
               Handler handler = new Handler(Looper.myLooper()) {
                    public void handleMessage(Message msg) {
                        if(player.isPlaying()) {
                            AudiobookService.BookProgress prog = (AudiobookService.BookProgress) msg.obj;
                            seeker.setProgress((int)(100*(prog.getProgress()/(float)mItem.duration)));
                            oldProgress = (int)(100*(prog.getProgress()/(float)mItem.duration));
                            updateProgress(nowPlayingID);
                            try {
                                currentThread().sleep(200);
                            } catch (InterruptedException e) {
                                //
                            }
                        }
                    }
                };
                player.setProgressHandler(handler);
                Looper.loop();
            }
        });

        thread1.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    try {
                        URL url = new URL(mItem.coverURL);
                        try {
                            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            cover.setImageBitmap(bmp);
                        } catch(IOException e) {
                            //
                        }
                    } catch(MalformedURLException e) {
                        //
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String id = mItem.id;
                nowPlayingID = Integer.valueOf(id);
                setPlaying(Integer.valueOf(id));

                if(!isDownloaded(id)) {
                    downloadInThread(id);
                    player.play(Integer.valueOf(id));
                    Toast.makeText(getActivity(),"Streaming book", Toast.LENGTH_SHORT).show();
                } else {
                    File file = new File(getContext().getFilesDir(), id+".mp3");
                    player.play(file);
                    Toast.makeText(getActivity(),"Playing book from file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                player.pause();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                player.stop();
                seeker.setProgress(0);
            }
        });

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.nowPlaying)).setText("Now playing: "+mItem.title + " by "+mItem.author);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getContext(), AudiobookService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            AudiobookService.MediaControlBinder binder = (AudiobookService.MediaControlBinder) service;
            player = binder;
            if(nowPlayingID != -1) {
                player.play(nowPlayingID);
            }
            //Toast.makeText(getContext().getApplicationContext(), "Bind successful", Toast.LENGTH_SHORT).show();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}