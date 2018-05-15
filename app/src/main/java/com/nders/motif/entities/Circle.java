package com.nders.motif.entities;

/**
 * Created by nders on 18/4/2018.
 */

public class Circle {
    private float mRadius;
    private float mCentreX;
    private float mCentreY;
    private float mLeft;
    private float mRight;
    private float mTop;
    private float mBottom;
    private int mColor;
    private int mID;


    public Circle(float centreX, float centreY, float radius, int id){
        this(centreX, centreY, radius,  id, 0);
    }



    public Circle(float centreX, float centreY, float radius, int id, int color){
        mCentreX = centreX;
        mCentreY = centreY;
        mRadius = radius;
        mID = id;
        mLeft = mCentreX - mRadius;
        mRight = mCentreX + mRadius;
        mTop = mCentreY - mRadius;
        mBottom = mCentreY + mRadius;
        mColor = color;
    }
    public void setColor(int color){
        mColor = color;
    }

    public float left(){ return mLeft;}
    public float right(){ return mRight;}
    public float top() {return mTop;}
    public float bottom(){return mBottom;}
    public int color(){return mColor;}
    public float radius() {return mRadius;}
    public boolean contains(float x, float y){
        return Math.sqrt(Math.pow(x - mCentreX, 2) + Math.pow(y - mCentreY, 2)) < mRadius;
    }

    public float centreX(){return mCentreX;}
    public float centreY(){return mCentreY;}
    public int id(){return mID;}

}
