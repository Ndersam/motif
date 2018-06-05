package com.nders.motif;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nders.motif.data.LevelDatabaseHelper;
import com.nders.motif.game.State;
import com.nders.motif.views.GameView;


public class GameFragment extends Fragment implements GameView.GameListener {

    private static final String TAG = GameFragment.class.getSimpleName();

    private int mGameID = 1;
    private static GameFragment instance;
    ObjectiveFragment menu = null;


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
    public void onResume() {
        GameView.getInstance(getContext()).resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(GameView.getInstance(getContext()) != null)
            GameView.getInstance(getContext()).pause();
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
    public void gameOver(State state) {
        boolean success = state.succeeded();
        Bundle bundle = new Bundle();

        if(success){

            // Update HighScore
            if(state.isNewHighScore()){
                LevelDatabaseHelper.getInstance(getContext()).updateLevel(state.level());
            }

            // Unlock new level (if any)
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            System.out.println(pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1));
            if(state.levelId() == pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1) && state.levelId() < Constants.MAX_LEVEL){
                System.out.println("I got here");
                pref.edit().putInt(Constants.KEY_HIGHEST_LEVEL, state.levelId() + 1).apply();

            }
            System.out.println(pref.getInt(Constants.KEY_HIGHEST_LEVEL, 1));

        }

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        bundle.putInt(Constants.KEY_GAME_SCORE, state.score());
        bundle.putInt(Constants.KEY_GAME_ID, state.levelId());
        bundle.putBoolean(Constants.KEY_GAME_COMPLETE, success);

        GameOverMenu levelCompleteMenu = new GameOverMenu();
        levelCompleteMenu.setArguments(bundle);

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.game_content_frame, levelCompleteMenu).commit();
    }

    @Override
    public void displayObjective() {
        menu = new ObjectiveFragment();
        menu.init(mGameID);
        menu.setOnStartBtnClicked(new ObjectiveFragment.OnStartBtnClicked() {
            @Override
            public void onCancel() {

            }

            @Override
            public void start(int levelId) {

            }
        });
        menu.show(getActivity().getSupportFragmentManager(), "objective");
    }

    @Override
    public boolean isObjectiveVisible(){
        return menu != null && !menu.isDismissed();
    }

    public void restart(){
        GameView.quitInstance();
        onDestroyView();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.game_content_frame, GameFragment.getInstance()).commit();
        GameView.getInstance(getContext()).resume();
    }
}
