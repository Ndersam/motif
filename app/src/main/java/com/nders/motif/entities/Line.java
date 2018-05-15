package com.nders.motif.entities;

/**
 * Line
 */

public class Line {
    public float startX, startY, endX, endY;

    public Line(float startX, float startY, float endX, float endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }


    public float length(){
        return (float) Math.sqrt(Math.pow(this.startX - this.endX, 2) + Math.pow(this.startY - this.endY, 2));
    }
}
