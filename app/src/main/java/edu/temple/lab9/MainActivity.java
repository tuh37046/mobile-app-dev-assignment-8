package edu.temple.lab9;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity {

    AudiobookService.MediaControlBinder player;
    boolean bound = false;

    int nowPlayingID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_new);

        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Button playButton = (Button) findViewById(R.id.playButton);
        Button pauseButton = (Button) findViewById(R.id.pauseButton);
        Button stopButton = (Button) findViewById(R.id.stopButton);
        TextView nowPlaying = (TextView) findViewById(R.id.nowPlaying);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView search = findViewById(R.id.input);
                String id = search.getText().toString();
                player.play(Integer.valueOf(id));
                nowPlayingID = Integer.valueOf(id);
                //String title = searchAPI();
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
            }
        });



    }

    public String searchAPI(String term) {
        String url = "https://kamorris.com/lab/cis3515/search.php?term="+term;
        final TextView nowPlaying = (TextView) findViewById(R.id.nowPlaying);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest stringRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = (JSONObject)response.get(0);
                        } catch(JSONException e) {
                            Toast.makeText(getApplicationContext(), "API query failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                nowPlaying.setText("Now playing: -");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
        return "hi";
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
