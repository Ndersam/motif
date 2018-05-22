package com.nders.motif.game;


import com.nders.motif.entities.DotColor;

import java.util.EnumMap;

/**
 * Created by nders on 6/5/2018.
 */

public class Level {
    private int mId;
    public int highScore;

    private boolean mUnlocked;

    private EnumMap<DotColor, Integer> mObjectiveMap;
    private int mMoves;

    public Level(int id, boolean unlocked, int highScore, int moves, EnumMap<DotColor, Integer> objectiveMap){
        mId = id;
        mUnlocked = unlocked;
        this.highScore = highScore;
        mMoves = moves;

        mObjectiveMap = new EnumMap<>(DotColor.class);
        for(DotColor dotColor: objectiveMap.keySet()){
            if(objectiveMap.get(dotColor) > 0){
                mObjectiveMap.put(dotColor, objectiveMap.get(dotColor));
            }
        }
    }


    //////////////////////////////////////////////////////////
    //                                                      //
    // ACCESSORS                                            //
    //                                                      //
    //////////////////////////////////////////////////////////

    public EnumMap<DotColor, Integer> getObjective(){ return mObjectiveMap; }

    public int moves(){ return mMoves;  }

    public boolean isUnlocked(){  return mUnlocked;  }

    public int id(){ return mId; }
}
