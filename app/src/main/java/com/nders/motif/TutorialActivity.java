package com.nders.motif;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class TutorialActivity extends AppCompatActivity implements TutorialFragment.FragmentInteractionListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_tutorial);
        TutorialFragment fragment = TutorialFragment.getInstance();
        fragment.setFragmentListener(this);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.tutorial_container, fragment)
                .commit();
    }

    @Override
    public void quitFragment() {
        Log.i("TutorialActivity", "I WAS CALLED");

        Fragment f = new TutorialCompleteFragment();
        TutorialFragment.quitInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.tutorial_container, f)
                .commit();
    }
}
