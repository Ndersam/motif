package com.nders.motif;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by nders on 10/4/2018.
 */

public class SoundHelper {
    private MediaPlayer mMusicPlayer;
    private SoundPool mSoundPool;
    private int mKickId;
    private int mClickId;
    private boolean mLoaded;
    private float mVolume;
    private SharedPreferences mPrefs;


    private static SoundHelper sInstance = null;

    public static  SoundHelper getInstance(Activity activity){
        if(sInstance == null){
            sInstance = new SoundHelper(activity);
        }
        return  sInstance;
    }


    private SoundHelper(Activity activity) {

        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(6).build();
        } else {
            //noinspection deprecation
            mSoundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });

        mKickId = mSoundPool.load(activity, R.raw.kick, 2);
        mClickId = mSoundPool.load(activity, R.raw.multimedia_button_click_021, 1);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    }


    public void prepareMusicPlayer(Context context){
        mMusicPlayer = MediaPlayer.create(context.getApplicationContext(),
                R.raw.sneaky_snitch);
        mMusicPlayer.setVolume(.5f, .5f);
        mMusicPlayer.setLooping(true);
    }

    public void playMusic(){
        if(mMusicPlayer != null && mPrefs.getBoolean(Constants.KEY_MUSIC_ENABLED, true)){
            mMusicPlayer.start();
        }else {
            Log.i("MUSIC", "I'm null");
        }
    }

    public void pauseMusic(){
        if(mMusicPlayer != null && mMusicPlayer.isPlaying()){
            mMusicPlayer.pause();
        }
    }

    public void stopMusic(){
        if(mMusicPlayer != null && mMusicPlayer.isPlaying()){
            mMusicPlayer.seekTo(0);
            mMusicPlayer.pause();
        }
    }

    public void playButtonClick(){
        if (mLoaded && mPrefs.getBoolean(Constants.KEY_SOUND_ENABLED, true)) {
            mSoundPool.play(mClickId, mVolume, mVolume, 1, 0, 1f);
        }
    }

    public void playKickSound(){
        if (mLoaded && mPrefs.getBoolean(Constants.KEY_SOUND_ENABLED, true)) {
            mSoundPool.play(mKickId, mVolume, mVolume, 1, 0, 1f);
        }
    }
}
