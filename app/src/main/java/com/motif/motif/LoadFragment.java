package com.motif.motif;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LoadFragment extends Fragment implements Runnable {

    private volatile boolean mRunning = false;
    private Thread mLoadThread = null;
    private boolean mDataChecked = false;


    public LoadFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_load, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mRunning){
            mRunning = true;
            mLoadThread = new Thread(this);
            mLoadThread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mRunning){
            try{
                mRunning = false;
                mLoadThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Log.i("TutorialCompleteFragment", "Running");
        while(mRunning){
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if(prefs.getBoolean(Constants.KEY_DATA_CHECK_COMPLETE, false)){
                mDataChecked = true;
                break;
            }
        }

        if(mDataChecked){
            Log.i("TutorialCompleteFragment", "Running");
            Intent i = new Intent(getContext(), HomeActivity.class);
            startActivity(i);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
