package com.nders.motif;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        CheckBox vibrationControl = mContentView.findViewById(R.id.vibration_control);


        /////////////////////////////
        //
        // SOUND
        //
        ////////////////////////////
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

        /////////////////////////////
        //
        // MUSIC
        //
        ////////////////////////////
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


        /////////////////////////////
        //
        // VIBRATION
        //
        ////////////////////////////
        vibrationControl.setChecked(pref.getBoolean(Constants.KEY_VIBRATION_ENABLED, false));
        vibrationControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref.edit().putBoolean(Constants.KEY_VIBRATION_ENABLED, isChecked)
                    .apply();
            }
        });

        /////////////////////////////
        //
        // DIFFICULTY SETTINGS
        //
        ////////////////////////////

        Button btnEasyMode = mContentView.findViewById(R.id.options_easy_mode);
        Button btnMediumMode = mContentView.findViewById(R.id.options_medium_mode);
        Button btnHardMode = mContentView.findViewById(R.id.options_hard_mode);
        Button btnExtremeMode = mContentView.findViewById(R.id.options_extreme_mode);

        View.OnClickListener difficultyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = pref.getString(Constants.KEY_GAME_DIFFICULTY, Constants.NORMAL_MODE);
                Button btn = mContentView.findViewWithTag(tag);
                if(btn != null){
                    btn.setBackground(getResources().getDrawable(R.drawable.unchecked_button, null));
                }
                pref.edit().putString(Constants.KEY_GAME_DIFFICULTY, (String)v.getTag()).apply();
                v.setBackground(getResources().getDrawable(R.drawable.checked_button, null));
            }
        };

        btnEasyMode.setTag(Constants.EASY_MODE);
        btnEasyMode.setOnClickListener(difficultyListener);

        btnMediumMode.setTag(Constants.NORMAL_MODE);
        btnMediumMode.setOnClickListener(difficultyListener);

        btnHardMode.setTag(Constants.HARD_MODE);
        btnHardMode.setOnClickListener(difficultyListener);

        btnExtremeMode.setTag(Constants.EXTREME_MODE);
        btnExtremeMode.setOnClickListener(difficultyListener);


        String tag = pref.getString(Constants.KEY_GAME_DIFFICULTY, Constants.NORMAL_MODE);
        Button btn = mContentView.findViewWithTag(tag);
        if(btn != null){
            btn.setBackground(getResources().getDrawable(R.drawable.checked_button, null));
        }


       // Back Button
        ViewGroup backBtn = mContentView.findViewById(R.id.options_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return mContentView;
    }

}
