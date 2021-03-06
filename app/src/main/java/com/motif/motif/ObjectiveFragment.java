package com.motif.motif;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.motif.motif.views.GameObjectiveView;

/**
 * Created by motif on 15/4/2018.
 */

public class ObjectiveFragment extends DialogFragment {

    private static final String TAG = ObjectiveFragment.class.getSimpleName();

    private View mContent;

    private ImageView mBackground;
    private ViewGroup mMainContent;
    private TextView mLevelTitle;


    private int mLevelId;
    private boolean mDismissed = true;

    private OnStartBtnClicked listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return init(inflater);
    }

    @Override
    public int getTheme() {
        return android.R.style.Theme_Translucent_NoTitleBar_Fullscreen;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    public View init(LayoutInflater inflater){
        mContent = inflater.inflate(R.layout.fragment_objective, null);

        mBackground = mContent.findViewById(R.id.background);
        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        mMainContent = mContent.findViewById(R.id.main_layout_obj);
        mMainContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mLevelTitle = mContent.findViewById(R.id.level_title);
        mLevelTitle.setText("Level " + mLevelId);

        Button startbtn = mContent.findViewById(R.id.button_start);
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listener != null){
                    dismiss();
                    listener.start(mLevelId);
                }
            }
        });


        GameObjectiveView objectiveView = mContent.findViewById(R.id.objective_image);
        objectiveView.setGameLevel(mLevelId);

        return mContent;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDismissed = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(listener != null){
            listener.onCancel();
        }
        mDismissed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
       Utils.setFullScreen((ViewGroup)mContent);
    }

    public boolean isDismissed(){
        return mDismissed;
    }

    public void init (int levelId){
        mLevelId = levelId;
    }

    public interface OnStartBtnClicked{
        public void onCancel();
        public void start(int levelId);
    }

    public void setOnStartBtnClicked(OnStartBtnClicked listener){
        this.listener = listener;
    }

}
