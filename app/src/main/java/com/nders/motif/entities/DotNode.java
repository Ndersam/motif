package com.nders.motif.entities;

import java.util.Comparator;

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

    public static Comparator<DotNode> idComparator() {
        return Comparator.comparingInt(d -> d.id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
