package com.cz4031;

public class Record {

    private boolean empty;
    private char[] tconst;
    private float avgRating;
    private int numVotes;

    public Record() {
        tconst = new char[10];
        empty = true;
    }

    public static Record empty() {
        return new Record();
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

    @Override
    public String toString() {
        return String.format("Empty: %s, tconst: %s, avgRating: %f, numVotes: %d", empty, String.valueOf(tconst).trim(),
                avgRating, numVotes);
    }
}
