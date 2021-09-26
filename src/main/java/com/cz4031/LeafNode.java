package com.cz4031;

/**
 * Class representing a leaf node in a B+ tree
 * Implements Node interface
 */
public class LeafNode implements Node {

    private int maxDegree;
    private int minDegree;
    private int curDegree;
    private KeyValuePair[] kvPairs;
    private LeafNode leftSibling;
    private LeafNode rightSibling;

    /**
     * Construct leaf node specified with maximum number of keys
     * @param n maximum number of keys in a node
     */
    public LeafNode(int n) {
        this(n, 0, new KeyValuePair[n]);
    }

    /**
     * Construct leaf node with maximum number of keys in a node, current degree and key-value pairs
     * @param n maximum number of keys in a node
     * @param curDegree current degree
     * @param kvPairs array representing key-value pairs of the node
     */
    public LeafNode(int n, int curDegree, KeyValuePair[] kvPairs) {
        this.maxDegree = n;
        this.minDegree = (int) Math.floor((n+1) / 2.0);
        this.curDegree = curDegree;
        this.kvPairs = kvPairs;
    }

    /**
     * Insert entry to leaf node while keeping the key-value pairs sorted
     * @param entry key-value pair to be inserted
     */
    public void addSorted(KeyValuePair entry) {
        int index = Util.findIndexToInsert(kvPairs, entry);
        Util.insertAndShift(kvPairs, entry, index);
        ++curDegree;
    }

    /**
     * Checks whether node is full
     * @return true if node is full, otherwise false
     */
    public boolean isFull() {
        return curDegree == maxDegree;
    }

    @Override
    public int getCurDegree() {
        return curDegree;
    }

    @Override
    public void setCurDegree(int curDegree) {
        this.curDegree = curDegree;
    }

    public KeyValuePair[] getKvPairs() {
        return kvPairs;
    }

    public void setKvPairs(KeyValuePair[] kvPairs) {
        this.kvPairs = kvPairs;
    }

    public LeafNode getRightSibling() {
        return rightSibling;
    }

    public void setRightSibling(LeafNode rightSibling) {
        this.rightSibling = rightSibling;
    }

    public LeafNode getLeftSibling() {
        return leftSibling;
    }

    public void setLeftSibling(LeafNode leftSibling) {
        this.leftSibling = leftSibling;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < curDegree; ++i) {
            sb.append(kvPairs[i].getKey().getK1());
            sb.append("  ");
        }
        return sb.toString();
    }
}
