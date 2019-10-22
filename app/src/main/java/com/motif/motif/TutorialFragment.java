package com.motif.motif;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.motif.motif.views.TutorialView;


public class TutorialFragment extends Fragment implements TutorialView.OnCompleteListener{

    TutorialView mView;
    FragmentInteractionListener mListener = null;

    private static TutorialFragment sInstance = null;

    public TutorialFragment() { }


    public static TutorialFragment getInstance() {
        if(sInstance == null){
            sInstance = new TutorialFragment();
        }
        return sInstance;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = TutorialView.getInstance(getContext());
        mView.setOnCompleteListener(this);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        TutorialView.getInstance(getContext()).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        TutorialView.pauseInstance();
    }

    @Override
    public void onDestroy() {
        TutorialView.quitInstance();
        Log.i("TutorialFragment", "destroyed");
        super.onDestroy();
    }

    @Override
    public void quit() {
        if(mListener != null){
            mListener.quitFragment();
        }
    }

    public interface FragmentInteractionListener{
        void quitFragment();
    }

    public void setFragmentListener(FragmentInteractionListener listener){
        mListener = listener;
    }


}
