package com.cz4031;

import java.util.Arrays;

/**
 * Class representing a leaf node in a B+ tree
 * Implements Node interface
 */
public class LeafNode implements Node {

    /**
     * Current number of entries in the node
     */
    private int curDegree;

    /**
     * Array of key-value pairs representing an entry
     */
    private KeyValuePair[] kvPairs;

    /**
     * Parent node
     */
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
     * Construct an empty leaf node specified with maximum number of keys
     * @param n maximum number of keys in a node
     */
    public LeafNode(int n) {
        this(0, new KeyValuePair[n], null, null, null);
    }

    /**
     * Construct a leaf node with current degree and array of key-value pairs
     * @param curDegree current degree of node
     * @param kvPairs array of key-value pairs
     */
    public LeafNode(int curDegree, KeyValuePair[] kvPairs) {
        this(curDegree, kvPairs, null, null, null);
    }

    /**
     * Construct a leaf node with all attributes
     * @param curDegree current degree
     * @param kvPairs array representing key-value pairs of the node
     * @param parent parent node
     * @param leftSibling left sibling node
     * @param rightSibling right sibling node
     */
    public LeafNode(int curDegree, KeyValuePair[] kvPairs, InternalNode parent, LeafNode leftSibling, LeafNode rightSibling) {
        this.curDegree = curDegree;
        this.kvPairs = kvPairs;
        this.parent = parent;
        this.leftSibling = leftSibling;
        this.rightSibling = rightSibling;
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

    @Override
    public int getDegree() {
        return curDegree;
    }

    @Override
    public void setDegree(int degree) {
        this.curDegree = degree;
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
