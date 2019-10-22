package com.motif.motif;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).apply();

        Fragment f = new TutorialCompleteFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.tutorial_container, f)
                .commit();
    }
}
