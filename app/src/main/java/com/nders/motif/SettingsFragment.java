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
                if(progress == 0){
                    pref.edit().putBoolean(Constants.KEY_SOUND_ENABLED, false).apply();
                }else{
                    pref.edit().putBoolean(Constants.KEY_SOUND_ENABLED, true).apply();
                }
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
                if(progress == 0){
                    pref.edit().putBoolean(Constants.KEY_MUSIC_ENABLED, false).apply();
                }else{
                    pref.edit().putBoolean(Constants.KEY_MUSIC_ENABLED, true).apply();
                }
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
        vibrationControl.setChecked(pref.getBoolean(Constants.KEY_VIBRATION_ENABLED, true));
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
                    btn.setBackground(getResources().getDrawable(R.drawable.button_unchecked, null));
                }
                pref.edit().putString(Constants.KEY_GAME_DIFFICULTY, (String)v.getTag()).apply();
                v.setBackground(getResources().getDrawable(R.drawable.button_checked, null));
                float difficulty;

                if(Constants.EASY_MODE.compareTo((String)v.getTag()) == 0){
                    difficulty = Constants.VALUE_EASY;
                }else if(Constants.NORMAL_MODE.compareTo((String)v.getTag()) == 0){
                    difficulty = Constants.VALUE_NORMAL;
                }else if(Constants.HARD_MODE.compareTo((String)v.getTag()) == 0){
                    difficulty = Constants.VALUE_HARD;
                }else{
                    // Constants.EXTREME_MODE
                    difficulty = Constants.VALUE_EXTREME;
                }

                pref.edit().putFloat(Constants.KEY_EDGE_THRESHOLD, difficulty).apply();
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


        // Check current difficulty
        String tag = pref.getString(Constants.KEY_GAME_DIFFICULTY, Constants.NORMAL_MODE);
        Button btn = mContentView.findViewWithTag(tag);
        if(btn != null){
            btn.setBackground(getResources().getDrawable(R.drawable.button_checked, null));

            float difficulty;

            if(Constants.EASY_MODE.compareTo((String)btn.getTag()) == 0){
                difficulty = Constants.VALUE_EASY;
            }else if(Constants.NORMAL_MODE.compareTo((String)btn.getTag()) == 0){
                difficulty = Constants.VALUE_NORMAL;
            }else if(Constants.HARD_MODE.compareTo((String)btn.getTag()) == 0){
                difficulty = Constants.VALUE_HARD;
            }else{
                // Constants.EXTREME_MODE
                difficulty = Constants.VALUE_EXTREME;
            }

            pref.edit().putFloat(Constants.KEY_EDGE_THRESHOLD, difficulty).apply();
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
