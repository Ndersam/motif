package com.nders.motif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        SoundHelper soundHelper = SoundHelper.getInstance(this);
        soundHelper.prepareMusicPlayer(getApplicationContext());

        Button btnPlay = findViewById(R.id.btn_play);
        Button btnOptions = findViewById(R.id.btn_options);
        Button btnAbout = findViewById(R.id.btn_about);

        btnPlay.setOnClickListener( view ->{
                SoundHelper.getInstance(HomeActivity.this).playButtonClick();
                Intent intent  = new Intent(HomeActivity.this, LevelSelectActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
        });

        btnOptions.setOnClickListener( view -> {
                SoundHelper.getInstance(HomeActivity.this).playButtonClick();
                Intent intent = new Intent(HomeActivity.this, AboutAndOptionsActivity.class);
                intent.putExtra(Constants.KEY_ABOUT_OR_OPTIONS, Constants.VALUE_OPTIONS);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundHelper.getInstance(HomeActivity.this).playButtonClick();
                Intent intent = new Intent(HomeActivity.this, AboutAndOptionsActivity.class);
                intent.putExtra(Constants.KEY_ABOUT_OR_OPTIONS, Constants.VALUE_ABOUT);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundHelper.getInstance(this).playMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundHelper.getInstance(this).pauseMusic();
    }

}
