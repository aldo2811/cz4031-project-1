package com.cz4031;

import java.util.Arrays;

/**
 * Class representing a leaf node in a B+ tree
 * Implements Node interface
 */
public class LeafNode implements Node {

    /**
     * Maximum number of entries in a leaf node
     */
    private int maxDegree;

    /**
     * Minimum number of entries in a leaf node
     */
    private int minDegree;

    /**
     * Current number of entries in the node
     */
    private int curDegree;

    /**
     * Array of key-value pairs representing an entry
     */
    private KeyValuePair[] kvPairs;

    private InternalNode parent;

    /**
     * Left sibling of the leaf node
     */
    private LeafNode leftSibling;

    /**
     * Right sibling of the leaf node
     */
    private LeafNode rightSibling;

    /**
     * Construct leaf node specified with maximum number of keys
     * @param n maximum number of keys in a node
     */
    public LeafNode(int n) {
        this(n, 0, new KeyValuePair[n], null);
    }

    public LeafNode(int n, int curDegree, KeyValuePair[] kvPairs) {
        this(n, curDegree, kvPairs, null);
    }

    /**
     * Construct leaf node with maximum number of keys in a node, current degree and key-value pairs
     * @param n maximum number of keys in a node
     * @param curDegree current degree
     * @param kvPairs array representing key-value pairs of the node
     */
    public LeafNode(int n, int curDegree, KeyValuePair[] kvPairs, InternalNode parent) {
        this.maxDegree = n;
        this.minDegree = (int) Math.floor((n+1) / 2.0);
        this.curDegree = curDegree;
        this.kvPairs = kvPairs;
        this.parent = parent;
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
     * Delete an entry that matches the key value
     * @param deleteKey key to delete
     * @return true if found and deleted, otherwise false
     */
    public boolean delete(int deleteKey) {
        for (int i = 0; i < curDegree; ++i) {
            if (kvPairs[i].getKey().getK1() == deleteKey) {
                Util.deleteAndShift(kvPairs, i);
                --curDegree;
                return true;
            }
        }
        return false;
    }

    /**
     * Delete an entry by its index in the node
     * @param index index of entry to be deleted
     * @return deleted entry
     */
    public KeyValuePair deleteByIndex(int index) {
        KeyValuePair toDelete = kvPairs[index];
        Util.deleteAndShift(kvPairs, index);
        --curDegree;
        return toDelete;
    }

    /**
     * Delete all entries in the node
     */
    public void deleteAll() {
        Arrays.fill(kvPairs, null);
        curDegree = 0;
    }

    /**
     * Checks whether node is full
     * @return true if node is full, otherwise false
     */
    public boolean isFull() {
        return curDegree == maxDegree;
    }

    /**
     * Checks whether an entry can be removed from the node while keeping the tree balanced
     * @return true if entry can be removed, otherwise false
     */
    public boolean canDelete() {
        return curDegree > minDegree;
    }

    /**
     * Checks whether the node is underflow, having less than the minimum required number of entries
     * @return true if node is underflow, otherwise false
     */
    public boolean isUnderflow() {
        return curDegree < minDegree;
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

    public InternalNode getParent() {
        return parent;
    }

    public void setParent(InternalNode parent) {
        this.parent = parent;
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
