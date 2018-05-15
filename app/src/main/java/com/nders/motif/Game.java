package com.nders.motif;

/**
 * Created by nders on 11/4/2018.
 */

public class Game {
    private String mName;
    private boolean mUnlocked;
    private double mHighScore;
    private long mID;
    private int mImageResourceId;

    public Game(String name, long id){
        this(name, id, 0);
    }

    Game(String name, long id, int imageResource){
        mName = name;
        mUnlocked = false;
        mHighScore = 0;
        mID  = id;
        mImageResourceId = imageResource;
    }

    public int getImageResourceId(){return mImageResourceId;}
    public void setImageResourceId(int resourceId){mImageResourceId = resourceId;}

    public String getName(){return mName;}
    public void setName(String name){mName = name;}

    public boolean isUnlocked(){return mUnlocked;}
    public void unlock(){mUnlocked= true;}

    public double getHighScore(){return mHighScore;}
    public void setHighScore(double score){mHighScore = score;}

    public long getID() {
        return mID;
    }
}
