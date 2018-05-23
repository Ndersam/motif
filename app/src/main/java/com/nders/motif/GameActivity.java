package com.nders.motif;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    /*
   *   Media
   */


    private GameFragment mGameFragment;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);

        int gameID = getIntent().getIntExtra(Constants.KEY_GAME_ID, -1);

        // Sounds

        //mSoundHelper.prepareMusicPlayer(getApplicationContext());


        mGameFragment = GameFragment.getInstance();
        mGameFragment.setGameID(gameID);
        mGameFragment.setRetainInstance(true);


        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.game_content_frame, mGameFragment,
                GameFragment.class.getSimpleName())
                .addToBackStack(GameFragment.class.getSimpleName())
                .commit();
    }




    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment f =  manager.findFragmentById(R.id.game_content_frame);


        if(f instanceof PauseMenu){
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.game_content_frame, GameFragment.getInstance()).commit();

            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        else if ( f instanceof GameFragment){
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.game_content_frame, new PauseMenu()).commit();
        }
        else if (f instanceof SettingsFragment){
            transaction.replace(R.id.game_content_frame, new PauseMenu()).commit();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

    }
}
