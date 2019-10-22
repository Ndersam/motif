package com.motif.motif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class AboutAndOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_aboutandoptions);

        Intent intent = getIntent();
        int fragmentType = intent.getIntExtra(Constants.KEY_ABOUT_OR_OPTIONS, Constants.VALUE_OPTIONS);

        Fragment fragment = null;
        String tag = null;
        switch (fragmentType){
            case Constants.VALUE_ABOUT:
                fragment = new AboutFragment();
                tag = AboutFragment.class.getSimpleName();
                break;
            case Constants.VALUE_OPTIONS:
                fragment = new SettingsFragment();
                tag = SettingsFragment.class.getSimpleName();
                break;
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.about_or_options, fragment,
                GameFragment.class.getSimpleName())
                .addToBackStack(tag)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
