package com.example.ryo.flickrimagescrollview;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {

    final String API_KEY = "3D00ac5f70d662304b87e7da585bbdef9d";
    GridView gridView;
    RetrieveFeedTask test;
    CustomGridViewAdapter adapter;
    File[] listFile;
    ArrayList<String> photoPaths = new ArrayList<String>();// list of file paths
    ArrayList<String> photoNames = new ArrayList<String>();// list of photo names
    int lenght = 0;
    Thread thread;



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = new RetrieveFeedTask(this);
        gridView = (GridView) findViewById(R.id.customgrid);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) { // using this, every time someone scrolls to the back of the gridView, this method will initiate photo download.
                new RetrieveFeedTask(getApplicationContext()).execute();
                return false;
            }
        });

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.e("AAAAA","Permission is granted");
            test.execute();
            adapter = new CustomGridViewAdapter(this,photoPaths,photoNames);
            gridView.setAdapter(adapter);
            stuff();
        }else{
            Log.e("AAAAA","Permission NOT granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    @Override
    protected void onDestroy() {// used this method to delete all photos from directory, to reduce used space.
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/flickr");
        try {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("AAAAA","Permission: "+permissions[0]+ "was "+grantResults[0]);
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void getFromSdcard(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/flickr");
        if (file.isDirectory()) {
            try {
            listFile = file.listFiles();
            if(lenght == listFile.length){ // used "lenght" as an indicator to check if method got photos
                getFromSdcard();
            }else{
                    lenght = listFile.length;

                for (int i = 0; i < listFile.length; i++){
                    if(!photoNames.contains(listFile[i].getName())) { // made this to eliminate duplicates.
                        photoPaths.add(listFile[i].getAbsolutePath());
                        if(listFile[i].getName().isEmpty()){
                            photoNames.add("No Title");
                        }else{
                            photoNames.add(listFile[i].getName());
                        }

                    }
                }
                adapter.notifyDataSetChanged();
                gridView.invalidate();
                thread.interrupt();
                thread = null;
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void stuff(){
        thread = new Thread(new Runnable(){ // created a thread to reduce scrolling lag while photos is being taken from device.
            @Override
            public void run(){
                getFromSdcard();
            }
        });
        thread.start();
    }
    class RetrieveFeedTask extends AsyncTask<Void, Void, Void> {

        JSONObject jsonobject;
        Context context;
        public RetrieveFeedTask(Context context){
            this.context = context;
        }


        @Override
        protected Void doInBackground(Void... params) { // used this method mainly for getting json file from flickr api then generate link to photos and download them.
            jsonobject = getJSONfromURL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20flickr.photos.recent%20where%20api_key%"+API_KEY+"&format=json&diagnostics=true&callback=");
            try {
                JSONObject userDetails = jsonobject.getJSONObject("query");
                JSONObject two = userDetails.getJSONObject("results");
                JSONArray three = two.getJSONArray("photo");

                int nam2e = three.length();
                Log.e("AAAAA",nam2e + "");
                for (int i = 0; i < three.length(); i++){
                    JSONObject  jObj = three.getJSONObject(i);
                    String farm = jObj.getString("farm");
                    String server = jObj.getString("server");
                    String id = jObj.getString("id");
                    String secret = jObj.getString("secret");
                    String title = jObj.getString("title");
                    Log.e("AAAAA","https://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+".jpg");
                    file_download("https://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+".jpg",title);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JSONObject getJSONfromURL(String url){
            InputStream Input_Stream = null;
            String result = "";
            JSONObject jArray = null;

            try{

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                Input_Stream = entity.getContent();

            }catch (Exception e) {
                Log.e("log_tag","Error in Http Connection" + e.toString());
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(Input_Stream, "iso-8859-1"), 8);
                StringBuilder String_builder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    String_builder.append(line+ "\n");
                }

                Input_Stream.close();

                result = String_builder.toString();

            } catch (Exception e) {
                Log.e("Log_tag","Error Converting result" + e.toString());
            }

            try {
                jArray = new JSONObject(result);

            } catch (JSONException e) {
                Log.e("Log_tag", "Error Parsing Data" + e.toString());
            }
            return jArray;
        }

        public void file_download(String uRl,String name) {
            File direct = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/flickr");

            if (!direct.exists()) {
                direct.mkdirs();
            }

            DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(uRl);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Photo")
                    .setDescription("Downloading photos.")
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES+"/flickr", name+".jpg");

            mgr.enqueue(request);
        }

        @Override
        protected void onPostExecute(Void aVoid) { // here i mainly tried to refresh my adapters to show new downloaded photos.
            stuff();
            test = null;
            super.onPostExecute(aVoid);
        }

    }
}

