package com.cz4031;

import java.io.Serializable;

public class Record implements Serializable {

    private boolean empty;
    private char[] tconst;
    private float avgRating;
    private int numVotes;

    public Record() {
        tconst = new char[10];
        empty = true;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public char[] getTconst() {
        return tconst;
    }

    public void setTconst(char[] tconst) {
        this.tconst = tconst;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(float avgRating) {
        this.avgRating = avgRating;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(int numVotes) {
        this.numVotes = numVotes;
    }
}
