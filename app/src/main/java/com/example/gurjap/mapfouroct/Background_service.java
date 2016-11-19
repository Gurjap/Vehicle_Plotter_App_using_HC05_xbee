package com.example.gurjap.mapfouroct;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


import android.app.IntentService;
import android.content.Intent;

import android.database.Cursor;
import android.media.MediaPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Background_service extends IntentService
{

    public Background_service()
    {
        super("Background_service");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        final YourTask yourTask = new YourTask();
        final Thread thread = new Thread(yourTask);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public class YourTask implements Runnable {
        @Override
        public void run()
        {
         try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DeviceControlActivity.Broadcasr_recievre.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);



            final Intent mServiceIntent = new Intent(Background_service.this,Background_service.class);

            startService(mServiceIntent);
        }
    }
}
