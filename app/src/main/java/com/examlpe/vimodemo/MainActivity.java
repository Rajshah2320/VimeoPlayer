package com.examlpe.vimodemo;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vimeo.networking.Configuration;
import com.vimeo.networking.VimeoClient;
import com.vimeo.networking.callbacks.ModelCallback;
import com.vimeo.networking.model.Video;
import com.vimeo.networking.model.error.VimeoError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import xdroid.toaster.Toaster;

import static xdroid.toaster.Toaster.toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String url, privateUrl;
    private Button playPublicBtn, playPrivateBtn, downloadBtn;
    private EditText urlTv, privateUrlTv;
    private Intent intent;


    private String clientId = "Client id - optional";
    private String clientSecret = "CLient secret - optional";
    private String proAccessToken = "Your access token here - make sure it has video_files scope";

    String jsonUrl = "https://player.vimeo.com/video/438676098/config";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Configuration.Builder builder = new Configuration.Builder(proAccessToken);
                //.setClientIdAndSecret(clientId,clientSecret)  //optional
                //.setCacheDirectory(this.getCacheDir());          //optional

        VimeoClient.initialize(builder.build());

        privateUrlTv = findViewById(R.id.PrivateUrlTv);
        urlTv = findViewById(R.id.UrlTv);
        playPublicBtn = findViewById(R.id.playBtn);
        playPrivateBtn = findViewById(R.id.PlayPrivateBtn);
        downloadBtn = findViewById(R.id.downloadBtn);

        intent = new Intent(MainActivity.this, PlayerActivity.class);

        playPublicBtn.setOnClickListener(view -> {
            url = urlTv.getText().toString();
            if (url.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a valid url", Toast.LENGTH_SHORT).show();
            } else {
                playPublicVimeoVideo(url);      //Using library to get the playable link

                //new JsonTask().execute(jsonUrl); //Getting the data manually from json, both do the same thing
            }
        });

        playPrivateBtn.setOnClickListener(view -> {
            privateUrl = privateUrlTv.getText().toString();
            if (privateUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a valid url", Toast.LENGTH_SHORT).show();
            } else {
                privateUrl = privateUrl.replace("https://vimeo.com", "/videos");
                getPrivateVimeoVideo(privateUrl, "play");
            }
        });

        downloadBtn.setOnClickListener(view -> {
            privateUrl = privateUrlTv.getText().toString();
            if (privateUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a valid url", Toast.LENGTH_SHORT).show();
            } else {
                privateUrl = privateUrl.replace("https://vimeo.com", "/videos");
                getPrivateVimeoVideo(privateUrl, "download");
            }

        });
    }

    private void getPrivateVimeoVideo(String uri, String action) {
        VimeoClient.getInstance().fetchNetworkContent(uri, new ModelCallback<Video>(Video.class) {
            @Override
            public void success(Video video) {

                //Picture picture = video.pictures.sizes.get(0);  To get the thumbnail

                if (action == "play") {
                    String link = video.files.get(0).getLink();
                    Log.i("Video", "success : stream link " + link);
                    intent.putExtra("url", link);
                    startActivity(intent);
                } else {
                    String downloadLink = video.getDownload().get(0).getLink();
                    downloadFile(MainActivity.this, video.name, ".mp4", "videos", downloadLink);
                    Toaster.toast("Download started");
                    Log.i("Video", "success : download link " + downloadLink);
                }

/*
                Play videoLink = video.getPlay();
                if (videoLink != null) {
                    String playlink = videoLink.mProgressive.get(0).getLink();
                    Log.i("VIDEO", "success: " + playlink);
                }

 */

            }

            @Override
            public void failure(VimeoError error) {
                // voice the error
                Log.i(TAG, "failure: "+error.getMessage());
            }
        });

    }


    private void playPublicVimeoVideo(String url) {

        VimeoExtractor.getInstance().fetchVideoWithURL(url, null, new OnVimeoExtractionListener() {
            @Override
            public void onSuccess(VimeoVideo video) {
                String videoUrl = null;

                videoUrl = video.getStreams().get("1080p");
                if (videoUrl == null)
                    videoUrl = video.getStreams().get("720p");
                if (videoUrl == null)
                    videoUrl = video.getStreams().get("480p");
                if (videoUrl == null)
                    videoUrl = video.getStreams().get("360p");
                if (videoUrl == null)
                    //Toast.makeText(MainActivity.this, "Enter a valid url", Toast.LENGTH_SHORT).show();
                    toast("enter a valid url");
                if (videoUrl != null) {
                    intent.putExtra("url", videoUrl);
                    startActivity(intent);
                }

            }

            @Override
            public void onFailure(Throwable throwable) {
                //Error handling here
            }
        });

    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destDirectory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destDirectory, fileName + fileExtension);
        downloadManager.enqueue(request);
    }
}

class JsonTask extends AsyncTask<String, String, String> {

    protected void onPreExecute() {
        super.onPreExecute();

    }

    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        JSONArray streamArray = null;
        try {
            streamArray = new JSONObject(result)
                    .getJSONObject("request")
                    .getJSONObject("files")
                    .getJSONArray("progressive");


            //Get info for each stream available
            for (int streamIndex = 0; streamIndex < streamArray.length(); streamIndex++) {
                JSONObject stream = streamArray.getJSONObject(streamIndex);

                String url = stream.getString("url");
                String quality = stream.getString("quality");

                Log.i("JSON", "onPostExecute: " + url);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


