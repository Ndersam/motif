package com.nders.motif;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.nders.motif.views.GameMapView;

/**
 * Created by nders on 9/4/2018.
 */

public class LevelSelectActivity extends AppCompatActivity implements GameMapView.GameListener, ObjectiveFragment.OnStartBtnClicked{

    private ViewGroup mContentView;
    private GameMapView mGameMapView;

    int mScrollOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_gamemap);


        mContentView = findViewById(R.id.game_map_layout);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setFullScreen(mContentView);
            }
        });



        mGameMapView = findViewById(R.id.game_map);
        mGameMapView.setGameListener(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mScrollOffset = pref.getInt(Constants.KEY_MAP_RECT_BOTTOM, 0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void displayPopup(int levelId, int scrollOffset) {

        SoundHelper.getInstance(this).playButtonClick();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1));
        if(levelId > pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1)){
            return;
        }

        ObjectiveFragment menu = new ObjectiveFragment();
        menu.init(levelId);
        menu.setOnStartBtnClicked(this);
        menu.show(getSupportFragmentManager(), "objective");

        mScrollOffset = scrollOffset;
    }

    @Override
    public void start(int levelId) {
        Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
        intent.putExtra(Constants.KEY_GAME_ID, levelId);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        SoundHelper.getInstance(this).stopMusic();
        finish();
    }

    @Override
    public void onCancel() {
        SoundHelper.getInstance(this).playButtonClick();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constants.KEY_MAP_RECT_BOTTOM, mScrollOffset);
        editor.apply();
    }
}
