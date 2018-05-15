package com.nders.motif.entities;

/**
 * Created by nders on 1/4/2018.
 */

public class DotNode {
    public String label;
    public int id;
    public int degree;

    public DotNode(int id, String label, int degree){
        this.label = label;
        this.id = id;
        this.degree = degree;
    }
}
