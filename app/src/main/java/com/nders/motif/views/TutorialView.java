package com.nders.motif.views;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TutorialView extends SurfaceView implements SurfaceHolder.Callback{

    SurfaceHolder mSurfaceHolder;

    /*
    *   DRAWING PARAMETERS
     */


    TutorialView(Context context){
        super(context);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
