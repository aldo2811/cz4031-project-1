package com.cz4031;

import java.util.Arrays;

public class MultiKey implements Comparable<MultiKey> {
    private int k1;
    private char[] k2;

    public MultiKey(int k1, char[] k2) {
        this.k1 = k1;
        this.k2 = k2;
    }

    public int getK1() {
        return k1;
    }

    public char[] getK2() {
        return k2;
    }

    @Override
    public int compareTo(MultiKey m) {
        if (k1 == m.getK1()) {
            return String.valueOf(k2).compareTo(String.valueOf(m.getK2()));
        }
        return Integer.compare(k1, m.getK1());
    }

    @Override
    public String toString() {
        return "MultiKey{" +
                "k1=" + k1 +
                ", k2=" + Arrays.toString(k2) +
                '}';
    }
}
