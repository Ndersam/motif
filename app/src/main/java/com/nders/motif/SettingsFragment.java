package com.nders.motif;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;


public class SettingsFragment extends Fragment {

    private View mContentView;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_settings,null);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        SeekBar soundControl = mContentView.findViewById(R.id.sound_control);
        SeekBar musicControl = mContentView.findViewById(R.id.music_control);
        final CheckBox vibrationControl = mContentView.findViewById(R.id.vibration_control);

        soundControl.setProgress(pref.getInt(Constants.KEY_SOUND_LEVEL, 100));
        soundControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pref.edit().putInt(Constants.KEY_SOUND_LEVEL, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {   }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        musicControl.setProgress(pref.getInt(Constants.KEY_MUSIC_LEVEL, 40));
        musicControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pref.edit().putInt(Constants.KEY_MUSIC_LEVEL, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        vibrationControl.setChecked(pref.getBoolean(Constants.KEY_VIBRATION_ENABLED, false));
        vibrationControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref.edit().putBoolean(Constants.KEY_VIBRATION_ENABLED, vibrationControl.isChecked())
                    .apply();
            }
        });


        return mContentView;
    }

}
