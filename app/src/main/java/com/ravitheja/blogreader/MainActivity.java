package com.ravitheja.blogreader;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ListActivity {

    //protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    private   final String KEY_TITLE ="title";
    private   final String KEY_AUTHOR ="author";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar=(ProgressBar) findViewById(R.id.progressBar1);
/*
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);*/

        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        if(isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostTasks getBlogPostTask = new GetBlogPostTasks();
            getBlogPostTask.execute();
        }
        else {
            Toast.makeText(this, "Network Unavailable!!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            JSONArray jsonPosts= mBlogData.getJSONArray("posts");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String blogURL =  jsonPost.getString("url");

            /* to view the contents on a seperate Browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(blogURL));
            startActivity(intent);*/

            // to view the contents on a web view
            Intent intent = new Intent(this,BlogWebViewActivity.class);
            intent.setData(Uri.parse(blogURL));
            startActivity(intent);

        } catch (JSONException e) {
            logException(e);
        }
    }

    private void logException(Exception e){
        Log.e(TAG,"Exception Caught!",e);
    }

    public boolean isNetworkAvailable(){
        //Checks if connection available
        ConnectivityManager manager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
        //is Network active
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if(networkInfo != null  && networkInfo.isConnected()){
            isAvailable=true;
        }
        return isAvailable;
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    public void handleBlogResponse(){
        mProgressBar.setVisibility(View.INVISIBLE);
        if(mBlogData==null){
            updateDisplayForError();
        }

        else{
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
            //    mBlogPostTitles = new String[jsonPosts.length()];
                ArrayList<HashMap<String,String>> blogPosts=
                        new ArrayList<HashMap<String, String>>();

                for(int i=0;i<jsonPosts.length();i++) {
                    JSONObject jsonPost = jsonPosts.getJSONObject(i);
                    String title = jsonPost.getString(KEY_TITLE);
                    title= Html.fromHtml(title).toString();
                    String author = jsonPost.getString(KEY_AUTHOR);
                    title= Html.fromHtml(title).toString();
                //    mBlogPostTitles[i]=title;

                    HashMap<String,String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE,title);
                    blogPost.put(KEY_AUTHOR,author);

                    blogPosts.add(blogPost);
                }
        /*        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1,
                        mBlogPostTitles);*/

                    String[] keys = {KEY_TITLE, KEY_AUTHOR};
                    int[] ids = {android.R.id.text1,android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this,blogPosts,
                        android.R.layout.simple_list_item_2,
                        keys,ids);
                setListAdapter(adapter);
            } catch (JSONException e) {
                Log.e(TAG,"Exception Caught",e);
            }
        }
    }

    private void updateDisplayForError() {
        //TODO Handle Error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok,null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    private class GetBlogPostTasks extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... arg0) {
            int responseCode = -1;
            JSONObject jsonResponse = null;

            try {
                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream(); //data in bytes format
                    Reader reader = new InputStreamReader(inputStream);    // data in character format
               /*     int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);          */                 // data added to char array
                    //    String responseData = new String(charArray);      //data converted to string
                    BufferedReader br = new BufferedReader(reader);
                    StringBuffer buffer = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        buffer.append(line);
                    }
                    //   Log.v(TAG,responseData);
                    jsonResponse = new JSONObject(buffer.toString());
          /*          String status = jsonResponse.getString("status");
                    Log.v(TAG,status);

                    JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
                    for(int i=0;i<jsonPosts.length();i++){
                        JSONObject jsonPost = jsonPosts.getJSONObject(i);
                        String title = jsonPost.getString("title");
                        Log.v(TAG,"Post" + i +": " + title );
                    }*/
                }else {
                    Log.i(TAG,"Unsuccessful HTTP Response Code:" + responseCode);
                }
                Log.i(TAG, "Code:" + responseCode);
            } catch (MalformedURLException e) {
                logException(e);
            } catch (IOException e) {
                logException(e);
            } catch (Exception e) {
                logException(e);
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mBlogData=result;
            handleBlogResponse();
        }
    }
}

