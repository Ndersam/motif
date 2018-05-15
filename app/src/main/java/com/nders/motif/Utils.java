package com.nders.motif;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nders on 9/4/2018.
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
}
