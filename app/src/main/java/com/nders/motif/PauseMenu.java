package com.nders.motif;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

/**
 * Created by nders on 15/4/2018.
 */

public class PauseMenu extends DialogFragment {
    private static final String TAG = PauseMenu.class.getSimpleName();

    private PauseMenuListener mPauseListener = null;


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


    public View init(LayoutInflater inflater){
        View contentView = inflater.inflate(R.layout.fragment_pause, null);
        Button btnRestart = contentView.findViewById(R.id.btnRestart);
        Button btnResume = contentView.findViewById(R.id.btnResume);
        Button btnHome = contentView.findViewById(R.id.btnHome);
        Button btnSettings = contentView.findViewById(R.id.btnSettings);

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.game_content_frame);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();


                if(f instanceof PauseMenu){
                    transaction.replace(R.id.game_content_frame, new SettingsFragment()).commit();
                }
            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPauseListener != null)
                    mPauseListener.onResumeButtonClicked();
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.game_content_frame);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                if(f instanceof PauseMenu){
                    transaction.replace(R.id.game_content_frame, GameFragment.getInstance()).commit();
                }
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPauseListener != null)
                    mPauseListener.onHomeButtonClicked();

                dismiss();
                GameFragment.quitInstance();
                Intent intent = new Intent(getActivity(), LevelSelectActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                getActivity().finish();

            }
        });

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPauseListener != null)
                    mPauseListener.onRestartButtonClicked();
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.game_content_frame);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                if(f instanceof PauseMenu){
                    int gameId = GameFragment.getInstance().getGameID();
                    GameFragment.quitInstance();

                    GameFragment gameFragment = GameFragment.getInstance();
                    gameFragment.setGameID(gameId);
                    transaction.replace(R.id.game_content_frame,gameFragment).commit();
                }
            }
        });


        return contentView;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mPauseListener != null)
            mPauseListener.onFinished();
    }


    public interface PauseMenuListener{
        void onResumeButtonClicked();
        void onHomeButtonClicked();
        void onRestartButtonClicked();
        void onFinished();
    }

    public void setPauseMenuListener(PauseMenuListener listener){
        mPauseListener = listener;
    }
}
