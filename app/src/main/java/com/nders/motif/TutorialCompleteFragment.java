package com.nders.motif;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;

public class TutorialCompleteFragment extends Fragment {

    AnimatorSet mButtonSetAnimator = new AnimatorSet();
    boolean stopAnimation = false;

    public TutorialCompleteFragment() {  }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tutorial_complete, container, false);

        Button button = view.findViewById(R.id.tutorial_continue);
        button.setOnClickListener( (View v) ->   exit());
        ObjectAnimator animY = ObjectAnimator.ofFloat(button, "translationY", -100f, 0f);
        animY.setDuration(1000);
        animY.setInterpolator(new BounceInterpolator());
        animY.start();

        AnimatorSet set1 = new AnimatorSet();
        AnimatorSet set2 = new AnimatorSet();


        float a = 1.0f;
        float b = 1.1f;
        int duration = 1000;

        set1.playTogether(
                ObjectAnimator.ofFloat(button, "scaleX", a, b)
                        .setDuration(duration),
                ObjectAnimator.ofFloat(button, "scaleY", a, b)
                        .setDuration(duration)
        );
        set2.playTogether(
                ObjectAnimator.ofFloat(button, "scaleX", b, a)
                        .setDuration(duration),
                ObjectAnimator.ofFloat(button, "scaleY", b, a)
                        .setDuration(duration)
        );

        mButtonSetAnimator.playSequentially(set1, set2);


        mButtonSetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                try{
                    if(!stopAnimation){
                        mButtonSetAnimator.start();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

//        animY.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                mButtonSetAnimator.start();
//            }
//        });

        return view;
    }

    public void exit(){
        stopAnimation = true;
        mButtonSetAnimator.end();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(!pref.getBoolean(Constants.KEY_DATA_CHECK_COMPLETE, false)){
            Log.i("TutorialCompleteFragment", "Data check not complete");
            Fragment f = new LoadFragment();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.tutorial_container, f).setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .commit();

        }else{
            Intent i = new Intent(getContext(), HomeActivity.class);
            startActivity(i);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

    }


}
