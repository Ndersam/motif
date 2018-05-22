package com.nders.motif;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by nders on 15/4/2018.
 */

public class GameOverMenu extends DialogFragment {


    private static final String TAG = GameOverMenu.class.getSimpleName();

    private int mLevelId = 0;
    private int mLevelScore = 0;
    private boolean mLevelComplete = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mLevelId = bundle.getInt(Constants.KEY_GAME_ID, 0);
            mLevelScore = bundle.getInt(Constants.KEY_GAME_SCORE, 1);
            mLevelComplete = bundle.getBoolean(Constants.KEY_GAME_COMPLETE, true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return init(inflater);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mLevelComplete){
            animateCountUp();
        }

    }

    private View init(LayoutInflater inflater){
        View contentView;
        if(mLevelComplete){
            contentView= inflater.inflate(R.layout.fragment_levelcomplete, null);

            Button btnContinue = contentView.findViewById(R.id.levelcomplete_continue);
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LevelSelectActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    getActivity().finish();
                }
            });
            btnContinue.setEnabled(false);

            TextView txtScore = contentView.findViewById(R.id.gameover_score);
            //txtScore.setText(String.valueOf(mLevelScore));

            TextView txtTitle = contentView.findViewById(R.id.gameover_title);
            txtTitle.setText("Level " + mLevelId);
        }else{
            contentView= inflater.inflate(R.layout.fragment_levelfailed, null);

            Button btnExit = contentView.findViewById(R.id.levelfailed_exit);
            Button btnRetry = contentView.findViewById(R.id.levelfailed_retry);


            btnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LevelSelectActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    getActivity().finish();
                }
            });


            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();

                    GameFragment.quitInstance();

                    GameFragment gameFragment = GameFragment.getInstance();
                    gameFragment.setGameID(mLevelId);

                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.replace(R.id.game_content_frame, gameFragment).commit();
                }
            });
        }



        return contentView;
    }

    private void animateCountUp(){
        final TextView txtScore = getView().findViewById(R.id.gameover_score);
        final Button btnContinue = getView().findViewById(R.id.levelcomplete_continue);

        final long delay = 1800 / (mLevelScore);


        new Thread(new Runnable() {
            @Override
            public void run() {
                // Count up
                int count = 1;
                while(count <= mLevelScore){
                    final int i = count;
                    txtScore.post(new Runnable() {
                        @Override
                        public void run() {
                            txtScore.setText(String.valueOf(i));
                        }
                    });

                    count++;

                    try{
                        Thread.sleep(delay);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

                // Enable Continue Button
                btnContinue.post(new Runnable() {
                    @Override
                    public void run() {
                        btnContinue.setEnabled(true);
                    }
                });
            }
        }).start();
    }

}
