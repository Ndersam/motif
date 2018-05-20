package com.nders.motif.entities;

/**
 * Line
 */

public class Line {
    public float startX, startY, endX, endY;
    public int startId, endId;

    enum TYPE {HORIZONTAL, VERTICAL, POINT, DIAGONAL}

    public Line(float startX, float startY, float endX, float endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public Line(Line another){
        this.startX = another.startX;
        this.startY = another.startY;
        this.endX = another.endX;
        this.endY = another.endY;
        this.startId = another.startId;
        this.endId = another.endId;
    }


    public float length(){
        return (float) Math.sqrt(Math.pow(this.startX - this.endX, 2) + Math.pow(this.startY - this.endY, 2));
    }

    public TYPE type(){
        if(Math.abs(this.startY - this.endY) > 0 && Math.abs(this.startX - this.endX) > 0){
            return TYPE.DIAGONAL;
        }

        if (Math.abs(this.startY - this.endY) > 0){
            return TYPE.VERTICAL;
        }else if(Math.abs(this.startX - this.endX) > 0){
            return TYPE.HORIZONTAL;
        }
        return TYPE.POINT;
    }

    public boolean merge(Line another){
        if(this.type() == another.type()){
            if(this.startX == another.endX ){
                this.startX = another.startX;
            }
            else if(this.endX == another.startX){
                this.endX = another.endX;
            }
            else if(this.startY == another.endY ){
                this.startY = another.startY;
            }
            else if(this.endY == another.startY){
                this.endY = another.endY;
            }

            if(this.startId == another.endId){
                this.startId = another.startId;
            }
            else if(this.endId == another.startId){
                this.endId = another.endId;
            }

            return true;
        }

        return false;
    }

}
