package com.motif.motif.entities;


/**
 *  Location class keeps track of the portion of the GameMap ,
 *  that when clicked opens up a level
 */

public class Location{

    private float mRadius;
    private float mCentreX;
    private float mCentreY;

    public Location(float centerx, float centrey, float radius){
        mCentreX = centerx;
        mCentreY = centrey;
        mRadius = radius;
    }

    public float radius() {return mRadius;}

    public boolean contains(float x, float y){
        return Math.sqrt(Math.pow(x - mCentreX, 2) + Math.pow(y - mCentreY, 2)) < mRadius;
    }
    public float centreX(){return mCentreX;}
    public float centreY(){return mCentreY;}
}
