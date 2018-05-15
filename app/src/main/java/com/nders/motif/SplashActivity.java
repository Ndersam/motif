package com.nders.motif;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.nders.motif.data.DataInitService;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setContentView(R.layout.activity_splash);

        init();

        TimerTask splashTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        };
        Timer opening = new Timer();
        opening.schedule(splashTask,1);


    }

    public void init(){

        // Update SCREEN_HEIGHT & SCREEN_WIDTH global constants
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = pref.edit();
        if(!pref.contains(Constants.KEY_SCREEN_WIDTH) || !pref.contains(Constants.KEY_SCREEN_HEIGHT)){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            edit.putInt(Constants.KEY_SCREEN_WIDTH, displayMetrics.widthPixels);
            edit.putInt(Constants.KEY_SCREEN_HEIGHT, displayMetrics.heightPixels);
            edit.apply();
        }

        // Check Database
        Intent intent = new Intent(this, DataInitService.class);
        startService(intent);


        // TODO : [2]  27/02/2018 Check Other settings
        // if game is starting for the first time
    }

}
