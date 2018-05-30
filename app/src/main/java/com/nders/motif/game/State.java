package com.nders.motif.game;

import com.nders.motif.entities.Dot;
import com.nders.motif.entities.DotColor;


import java.util.EnumMap;
import java.util.Stack;

public class State {
    private Level mLevel;

    private int mScore;
    private int mMovesLeft;

    private static int RED_DOT_LIMIT = 8;
    private static final int BAD_DOT_LIMIT = 16;
    private static final int GOOD_NON_ROY_LIMIT = 20;
    private static final int GOOD_ROY_LIMIT = 36 - BAD_DOT_LIMIT;

    // 'Weird' names

    // A dot is good if it is one of the dots that are to be collected.
    // That is, if the dot color exist in the level objective map.
    private int mGoodROYDots;

    // This is a dot that is 'good' and not one of { RED, ORANGE, YELLOW } dots
    private int mGoodNonROYDots;

    // A RED dot is one with dotColor == DotColor.RED
    private int mRedDotCount = 0;

    // A dot is 'bad' if it is not of the dots that are to be collected.
    private int mBadDotCount = 0;


    // Maps the dotColors to be collected to a tuple of two numbers - the first (the number of dots
    // ... with a certain dotColor to be collected) and the second (the number of dots with
    // ... a such dotColor that have been collected)
    private EnumMap<DotColor, int[]> mProgressCounter = new EnumMap<>(DotColor.class);
    public static final int IDX_DOTS_GOAL = 0;
    public static final int IDX_DOTS_COLLECTED = 1;

    public State(Level level){
        mLevel = level;
        for(DotColor key: mLevel.getObjective().keySet()){
            mProgressCounter.put(key, new int[]{mLevel.getObjective().get(key), 0});
            if(isROYDot(key)){
                mGoodROYDots++;
            }else{
                mGoodNonROYDots++;
            }
        }
        mMovesLeft = mLevel.moves();
        mScore = 0;

        if(mGoodNonROYDots >= mGoodROYDots *.75f){
            RED_DOT_LIMIT = 19;
        }
        mGoodROYDots = mGoodNonROYDots = 0;
    }


    /**
     * Updates game score, the count of "dotColor" and updates the dotColorCounter
     */
    public void update(DotColor selectedColor, Stack<Dot> selected, boolean rectFormed){

        mMovesLeft--;

        // If dots with the color, "selectedColor" have all been collected prior to this ...
        // ... call, only increase score by two
        if(isCollected(selectedColor)) {
            mScore += 2;
        }else{
            mScore += selected.size() *(rectFormed? 2: 1);
        }

        for(Dot dot: selected){
            if(mProgressCounter.containsKey(dot.dotColor())){
                if(!isROYDot(dot.dotColor())){
                    mGoodNonROYDots--;
                }else{
                    mGoodROYDots--;
                }
            }else {
                if(dot.dotColor() == DotColor.RED){
                    mRedDotCount--;
                }else{
                    mBadDotCount--;
                }
            }
        }

        // Update Dots Collected
        if(mProgressCounter.containsKey(selectedColor)){
            int[] values = mProgressCounter.get(selectedColor);
            values[IDX_DOTS_COLLECTED] += selected.size();
            mProgressCounter.put(selectedColor,values);
        }
    }

    /**
     * Returns true if one of the following conditions are met
     *  1. Node's color is contained in the objective and if color is NON-ROY (Red, Orange or Yellow)
     *      ... and the NON_ROY_DOT_LIMIT  has not been reached.
     *  2. Node's color is not contained in the objective and is RED and the ...
     *      ... RED_DOT_LIMIT has not been reached.
     *  3. Node's color is not contained in the objective and is not RED and the ...
     *      ... BAD_DOT_LIMIT has not been reached.
     *
     * @param degree The degree of a DotNode.
     */
    public boolean isNodeValid(int degree) {
        DotColor key = DotColor.valueOf(degree);

        boolean isValid = true;

        // Inspect for validity
        if(mProgressCounter.containsKey(key)){
            if(!isROYDot(key)){
                if(mGoodNonROYDots < GOOD_NON_ROY_LIMIT){
                    mGoodNonROYDots++;
                }else {
                    isValid = false;
                }
            }else{
                if(mGoodROYDots < GOOD_ROY_LIMIT){
                    mGoodROYDots++;
                }else{
                    isValid = false;
                }
            }
        }
        else if(key == DotColor.RED && mRedDotCount < RED_DOT_LIMIT){
            mRedDotCount++;
        }else if(mBadDotCount < BAD_DOT_LIMIT){
            mBadDotCount++;
        }else{
            isValid = false;
        }
        return isValid;
    }

    /**
     * Returns true if game objectives have been satisfied
     */
    public boolean succeeded(){
        return  allDotsCollected() && (mMovesLeft >= 0);
    }

    /**
     * Returns true if game objective has not been satisfied and
     * no moves are left
     */
    public boolean failed(){
        return !allDotsCollected() && (mMovesLeft <= 0);
    }

    /**
     * isROYDot --> is Red or Orange or Yellow dot
     * Returns true if dotColor is one of {DotColor.RED, DotColor.ORANGE, DotColor.YELLOW}
     */
    private boolean isROYDot(DotColor dotColor){
       return dotColor == DotColor.RED || dotColor == DotColor.ORANGE || dotColor == DotColor.YELLOW;
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
    private boolean allDotsCollected(){
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

    public EnumMap<DotColor, int[]> progress() {
        return mProgressCounter;
    }

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