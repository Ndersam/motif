package com.nders.motif;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.levels.Level;
import com.nders.motif.views.GameView;


public class GameFragment extends Fragment implements GameView.GameOverListener{

    /*
    *   Views
    */
    //private GameView mGameView;

    /*
    *   Game Logic
    */
    private int mGameID = 1;


    private static final String TAG = GameFragment.class.getSimpleName();
    private static GameFragment instance;


    public GameFragment() {
        // Required empty public constructor
    }

    public static GameFragment getInstance(){
        if(instance == null){
            instance = new GameFragment();
        }
        return instance;
    }

    public static void quitInstance(){
        if(instance != null){
            instance.onDestroy();
            instance = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GameView gameView = GameView.getInstance(getContext());
        gameView.setGameLevel(mGameID);
        gameView.setGameOverListener(this);
        return gameView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        GameView.getInstance(getContext()).resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mSoundHelper.pauseMusic();
        if(GameView.getInstance(getContext()) != null) GameView.getInstance(getContext()).pause();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        if(getContext() != null && GameView.getInstance(getContext()) != null) {
            GameView.quitInstance();
            super.onDestroy();
        }

        Log.i(TAG, "I GOT DESTROYED");
    }

    public void setGameID(int id){
        mGameID = id;
    }

    public int getGameID(){return  mGameID;}

    @Override
    public void gameOver(Level level) {

        boolean success = level.succeeded();
        Bundle bundle = new Bundle();

        if(success){

            // Update HighScore
            if(level.isNewHighScore()){
                LevelDatabaseHelper.getInstance(getContext()).updateLevel(level);
            }

            // Unlock new level (if any)
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            System.out.println(pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1));
            if(level.id() == pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1) && level.id() < Constants.MAX_LEVEL){
                System.out.println("I got here");
                pref.edit().putInt(Constants.KEY_HIGHEST_LEVEL, level.id() + 1).apply();

            }
            System.out.println(pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1));

        }

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        bundle.putInt(Constants.KEY_GAME_SCORE, level.score());
        bundle.putInt(Constants.KEY_GAME_ID, level.id());
        bundle.putBoolean(Constants.KEY_GAME_COMPLETE, success);

        GameOverMenu levelCompleteMenu = new GameOverMenu();
        levelCompleteMenu.setArguments(bundle);

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.game_content_frame, levelCompleteMenu).commit();

    }
}
