package com.nders.motif.entities;


import java.util.Comparator;

/**
 * Created by nders on 5/4/2018.
 */

public class Rectangle {

    private float mTop;
    private float mBottom;
    private float mLeft;
    private float mRight;
    private float mCenterX;
    private float mCenterY;

    private int mRow;
    private int mColumn;

    private float mAnimY;
    private float mAnimStartY;

    private DotColor mDotColor;

    private int mID;
    private String mLabel;
    private int mDegree;

    private boolean mSelected;


    public Rectangle(float left, float top, float right, float bottom, DotNode node, int row, int column) {
        mTop = top;
        mBottom = bottom;
        mLeft = left;
        mRight = right;
        mRow = row;
        mColumn = column;

        mCenterX = (mLeft + mRight) / 2;
        mCenterY = (mTop + mBottom) / 2;
        mSelected = false;

        mAnimY = 0;
        mDotColor = DotColor.valueOf(node.degree);

        mID = node.id;
        mDegree = node.degree;
        mLabel = node.label;
    }

    public float left() {
        return mLeft;
    }

    public float right() {
        return mRight;
    }

    public float top() {
        return mTop;
    }

    public float bottom() {
        return mBottom;
    }

    public boolean contains(float x, float y) {
        return x >= mLeft && x <= mRight && y >= mTop && y <= mBottom;
    }

    public float getX() {
        return mCenterX;
    }

    public float getY() {
        return mCenterY;
    }

    public int id() {
        return mID;
    }


    public void setId(int id) {
        mID = id;
    }

    public void setDimensions(float top, float bottom) {
        mTop = top;
        mBottom = bottom;
        mCenterY = (mTop + mBottom) / 2;
    }

    public void setRow(int row) {
        mRow = row;
    }

    public void setColumn(int column) {
        mColumn = column;
    }

    public int row() {
        return mRow;
    }

    public int column() {
        return mColumn;
    }

    public void setAnimStartY(float startY) {
        this.mAnimStartY = startY;
        this.mAnimY = startY;
    }


    public void select() {
        mSelected = true;
    }

    public void deselect() {
        mSelected = false;
    }

    public boolean isSelected() {
        return mSelected;
    }


    public static Comparator<Rectangle> rowComparator() {
        Comparator<Rectangle> comp = new Comparator<Rectangle>() {
            @Override
            public int compare(Rectangle a, Rectangle b) {
                return b.mRow - a.mRow;
            }
        };
        return comp;
    }

    public boolean step() {
        boolean validStep = true;

        mAnimY += 100;
        if (mAnimY >= mCenterY) {
            mAnimY = mAnimStartY;
            validStep = false;
        }

        return validStep;
    }

    public float getAnimY() {
        return mAnimY;
    }

    public DotColor dotColor(){
        return mDotColor;
    }

    /**
     * Sets dotColor
     * @param dotColor
     *
     */
    public void setDotColor(DotColor dotColor){
        mDotColor = dotColor;
    }


    /**
     * Swap all the content between this Rect and another except for location
     * variables.
     * @param other another Rectangle object
     */
    public void swap(Rectangle other){
        int id = mID;
        DotColor color = mDotColor;
        boolean selected = mSelected;

        mID = other.mID;
        mDotColor = other.mDotColor;
        mSelected = other.mSelected;

        other.mID = id;
        other.mDotColor = color;
        other.mSelected = selected;

        // Sanity setting
        other.mSelected = true;
        mSelected = false;
    }

    public DotNode toDotNode(){
        return new DotNode(mID, mLabel, mDegree);
    }

}
