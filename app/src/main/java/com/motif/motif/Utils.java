package com.motif.motif;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by motif on 9/4/2018.
 */

public class Utils {

    public static void setFullScreen(ViewGroup viewGroup){
        // Does what it says
        viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public static void vibrate(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if(pref.getBoolean(Constants.KEY_VIBRATION_ENABLED, true)){
            Vibrator vibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(500,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            }else{
                vibrator.vibrate(800);
            }
        }
    }
}
