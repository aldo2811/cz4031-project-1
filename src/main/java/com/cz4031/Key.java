package com.cz4031;

import java.util.Arrays;

/**
 * Class representing key of a node in a B+ tree
 */
public class Key implements Comparable<Key> {

    /**
     * Primary indexed attribute (numVotes)
     */
    private int k1;

    /**
     * Secondary indexed attribute (tconst)
     */
    private char[] k2;

    /**
     * Construct a key
     * @param k1 primary indexed attribute
     * @param k2 secondary indexed attribute
     */
    public Key(int k1, char[] k2) {
        this.k1 = k1;
        this.k2 = k2;
    }

    public int getK1() {
        return k1;
    }

    public void setK1(int k1) {
        this.k1 = k1;
    }

    public char[] getK2() {
        return k2;
    }

    public void setK2(char[] k2) {
        this.k2 = k2;
    }

    @Override
    public int compareTo(Key k) {
        if (k == null) return -1;
        if (this.k1 == k.k1) {
            return Arrays.toString(this.k2).compareTo(Arrays.toString(k.k2));
        }
        return Integer.compare(this.k1, k.k1);
    }
}
