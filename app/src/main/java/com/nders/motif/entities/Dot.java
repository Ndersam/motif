package com.nders.motif.entities;

public class Dot {
    private float mRadius;
    private float mCentreX;
    private float mCentreY;
    private float mLeft;
    private float mRight;
    private float mTop;
    private float mBottom;
    private float mTolerance;
    private int mRow = -1;
    private int mColumn = -1;

    private boolean mSelected;
    private DotNode mDotNode;


    public Dot(float centreX, float centreY, float radius){
        this(centreX, centreY, radius, null);
    }


    public Dot(float centreX, float centreY, float radius, DotNode dotNode){
        mCentreX = centreX;
        mCentreY = centreY;
        mRadius = radius;
        mLeft = mCentreX - mRadius;
        mRight = mCentreX + mRadius;
        mTop = mCentreY - mRadius;
        mBottom = mCentreY + mRadius;
        mDotNode = dotNode;
        mSelected = false;
        mTolerance = 0;
    }

    public float left(){
        return mLeft;
    }

    public float right(){
        return mRight;
    }
    public float top() {
        return mTop;
    }

    public float bottom(){
        return mBottom;
    }

    public int color(){
        return DotColor.colorInfo(mDotNode.degree);
    }

    public float radius() {
        return mRadius;
    }

    public void setTolerance(float tolerance){
        mTolerance = tolerance;
    }

    public boolean contains(float x, float y){
        return Math.sqrt(Math.pow(x - mCentreX, 2) + Math.pow(y - mCentreY, 2)) <= (mRadius + mTolerance);
    }

    public float centreX(){
        return mCentreX;
    }

    public float centreY(){
        return mCentreY;
    }

    public int id(){
        return mDotNode.id;
    }

    public boolean isSelected(){
        return mSelected;
    }

    public void select(){
        mSelected = true;
    }

    public void deSelect(){
        mSelected = false;
    }

    public void swap(Dot another){
        DotNode temp = another.mDotNode;
        another.mDotNode = this.mDotNode;
        this.mDotNode = temp;
    }

    public static void swap(Dot a, Dot b){
        DotNode temp = b.mDotNode;
        b.mDotNode = a.mDotNode;
        a.mDotNode = temp;
    }

    public void setColumn(int column){
        mColumn = column;
    }

    public void setRow(int row){
        mRow = row;
    }

    public int column(){
        return mColumn;
    }

    public int row(){
        return mRow;
    }

    public void setDotNode(DotNode dotNode){
        mDotNode = dotNode;
    }

    public DotColor dotColor(){
        return DotColor.valueOf(mDotNode.degree);
    }
}
