package com.nders.motif.game;

import com.nders.motif.entities.DotColor;
import com.nders.motif.entities.Rectangle;

import java.util.EnumMap;
import java.util.Stack;

public class State {
    private Level mLevel;

    private int mScore;
    private int mMovesLeft;

    private static final int RED_DOT_THRESHOLD = 8;
    private static final int INVALID_DOT_THRESHOLD = 3;
    private static final int TOTAL_INVALID_DOT_THRESHOLD = 10;

    private int mRedDotCount = 0;
    private int mValidDotCount = 0;
    private int mInvalidDotCount = 0;

    private EnumMap<DotColor, Integer> mDotColorCounter = new EnumMap<>(DotColor.class);
    private EnumMap<DotColor, int[]> mProgressCounter = new EnumMap<>(DotColor.class);
    public static final int IDX_DOTS_GOAL = 0;
    public static final int IDX_DOTS_COLLECTED = 1;

    public State(Level level){
        mLevel = level;
        for(DotColor key: mLevel.getObjective().keySet()){
            mProgressCounter.put(key, new int[]{mLevel.getObjective().get(key), 0});
        }
        mMovesLeft = mLevel.moves();
        mScore = 0;
    }

    public EnumMap<DotColor, int[]> progress() {
        return mProgressCounter;
    }

    /**
     * Updates game score, the count of "dotColor" and updates the dotColorCounter
     */
    public void update(DotColor selectedColor, Stack<Rectangle> selected, boolean rectFormed){
        int valid = 0;
        int invalid = 0;

        for(Rectangle dot: selected){
            if(mProgressCounter.containsKey(dot.dotColor())){
                int dotCount = mDotColorCounter.get(dot.dotColor());
                mDotColorCounter.put(dot.dotColor(), --dotCount);
                valid++;
            }else {
                invalid++;
            }
        }

        if(invalid > 0){
            mScore += 2;
        }

        mMovesLeft--;
        mScore += valid *(rectFormed? 2: 1);

        // Update Dots Collected
        if(mProgressCounter.containsKey(selectedColor)){
            int[] values = mProgressCounter.get(selectedColor);
            values[IDX_DOTS_COLLECTED] += selected.size();
            mProgressCounter.put(selectedColor,values);
        }

    }

    public boolean isNodeValid(int degree) {
        DotColor key = DotColor.valueOf(degree);
        boolean badDot = true;
        boolean redDot = false;
        boolean isValid = true;
        int currentCount = 0;

        if(key == DotColor.RED){
            redDot = true;
        }

        if(mProgressCounter.containsKey(key)){
            badDot = false;
        }

        if(mDotColorCounter.containsKey(key)){
            currentCount = mDotColorCounter.get(key);
        }

        if(!badDot){
            mInvalidDotCount++;
        }else if(redDot && mRedDotCount < RED_DOT_THRESHOLD){
            mRedDotCount++;
        }else if(mInvalidDotCount  < TOTAL_INVALID_DOT_THRESHOLD){
            mInvalidDotCount++;
        }else{
            isValid = false;
        }

        if(isValid ){
            mDotColorCounter.put(key, ++currentCount);
            return true;
        }

        return false;
    }

    public boolean succeeded(){
        return  allDotsCollected() && (mMovesLeft >= 0);
    }

    public boolean failed(){
        return !allDotsCollected() && (mMovesLeft < 0);
    }


    /**
     * Returns true if the number dots selected (with color "dotColor") is greater or ...
     * ... equals the number to be selected (as specified in "mObjectiveMap" object).
     * @param dotColor Color of a dot
     */
    public boolean isCollected(DotColor dotColor){
        if(mProgressCounter.containsKey(dotColor)){
            int[] values = mProgressCounter.get(dotColor);
            if(values[IDX_DOTS_COLLECTED] < values[IDX_DOTS_GOAL]){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if level has been completed
     * @return boolean stating whether the level has been completed
     */
    public boolean allDotsCollected(){
        for(DotColor key: mProgressCounter.keySet()){
            int[] values = mProgressCounter.get(key);
            if(values[IDX_DOTS_COLLECTED] < values[IDX_DOTS_GOAL]){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if new high score has been achieved.
     */
    public boolean isNewHighScore(){
        return mScore > mLevel.highScore;
    }

    //////////////////////////////////////////////////////////
    //                                                      //
    // GETTERS                                              //
    //                                                      //
    //////////////////////////////////////////////////////////


    public int score(){
        return  mScore;
    }

    public int levelId(){
        return mLevel.id();
    }

    public Level level(){
        mLevel.highScore = mScore;
        return mLevel;
    }

    public int movesLeft(){
        return mMovesLeft;
    }
}

