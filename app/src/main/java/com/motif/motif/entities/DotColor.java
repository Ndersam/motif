package com.motif.motif.entities;

import android.graphics.Color;

/**
 * Created by motif on 9/5/2018.
 */

public enum DotColor {
    RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET;

    public int colorInfo(){
        switch (this){
            case RED:
                return Color.parseColor("#FF505B");
            case ORANGE:
                return Color.parseColor("#FF7B06");
            case YELLOW:
                return Color.parseColor("#FFD935");
            case BLUE:
                return Color.parseColor("#81B3FF");
            case GREEN:
                return Color.parseColor("#78C26A");
            case INDIGO:
                return Color.parseColor("#324056");
            case VIOLET:
                return Color.parseColor("#8E5DAA");
        }
        return Color.BLACK;
    }

    public static DotColor valueOf(int val){
        if (val < 74)
            return DotColor.VIOLET;
        else if (val <  148)
            return  DotColor.INDIGO;
        else if (val < 222)
            return  DotColor.BLUE;
        else if (val < 296)
            return  DotColor.GREEN;
        else if (val < 370)
            return  DotColor.YELLOW;
        else if (val < 444)
            return  DotColor.ORANGE;
        else
            return  DotColor.RED;
    }

    public static int colorInfo(int val){
        return valueOf(val).colorInfo();
    }
}
