package com.nders.motif.levels;


import com.nders.motif.entities.DotColor;

import java.util.EnumMap;

/**
 * Created by nders on 6/5/2018.
 */

public class Level {


    private int mId;
    private int mHighScore;
    private int mScore;
    private boolean mUnlocked;

    private EnumMap<DotColor, Integer> mObjectiveMap;
    private EnumMap<DotColor, Integer> mScoreMap;

    private int mMoves;


    public Level(int id, boolean unlocked, int highScore, int moves, EnumMap<DotColor, Integer> objectiveMap){
        mScore = 0;
        mId = id;
        mUnlocked = unlocked;
        mHighScore = highScore;
        mMoves = moves;

        mObjectiveMap = new EnumMap<>(DotColor.class);
        mScoreMap = new EnumMap<>(DotColor.class);
        for(DotColor dotColor: objectiveMap.keySet()){
            if(objectiveMap.get(dotColor) > 0){
                mObjectiveMap.put(dotColor, objectiveMap.get(dotColor));
                mScoreMap.put(dotColor, 0);
            }
        }
    }



    //////////////////////////////////////////////////////////
    //                                                      //
    // ACCESSORS                                            //
    //                                                      //
    //////////////////////////////////////////////////////////

    public EnumMap<DotColor, Integer> getObjective(){ return mObjectiveMap; }

    public EnumMap<DotColor, Integer> getScore(){ return mScoreMap; }

    public int movesLeft(){  return mMoves;  }

    public boolean isUnlocked(){  return mUnlocked;  }

    public int id(){ return mId; }

    public int highScore(){ return mHighScore;   }

    public int score(){  return mScore;  }

    //////////////////////////////////////////////////////////
    //                                                      //
    // MISC                                                 //
    //                                                      //
    //////////////////////////////////////////////////////////

    /**
     * Updates game score and the count of "dotColor"
     * @param dotColor The DotColor of the dots selected
     * @param added the number of dots with dotColor
     */
    public void updateScore(DotColor dotColor, int added){
        mScore += added;
        mMoves--;

        if(mObjectiveMap.containsKey(dotColor)){
            int score = mScoreMap.get(dotColor) + added;

            if(score > mObjectiveMap.get(dotColor)){
                score = mObjectiveMap.get(dotColor);
            }
            mScoreMap.put(dotColor,  score);
        }
    }

    /**
     * Returns true if level has been completed
     * @return boolean stating whether the level has been completed
     */
    public boolean succeeded(){
        return objectiveSatisfied() && (mMoves >= 0);
    }


    public boolean failed(){
        return !objectiveSatisfied() && (mMoves <= 0);
    }

    private boolean objectiveSatisfied(){
        boolean complete = true;

        for(DotColor key: mObjectiveMap.keySet()){
            if(mObjectiveMap.get(key).intValue() != mScoreMap.get(key).intValue()){
                complete = false;
                break;
            }
        }
        return complete;
    }

    /**
     * Returns true if the number dots selected (with color "dotColor") is greater or ...
     * ... equals the number to be selected (as specified in "mObjectiveMap" object).
     * @param dotColor Color of a dot
     * @return
     */
    public boolean isDotColorComplete(DotColor dotColor){
        return mObjectiveMap.containsKey(dotColor) && mScoreMap.get(dotColor) >= mObjectiveMap.get(dotColor);
    }

    /**
     * Returns true if new high score has been achieved.
     * @return
     */
    public boolean isNewHighScore(){
        return mScore > mHighScore;
    }
}
