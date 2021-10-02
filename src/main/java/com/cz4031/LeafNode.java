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
    private int degree;

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
     * @param degree current degree of node
     * @param kvPairs array of key-value pairs
     */
    public LeafNode(int degree, KeyValuePair[] kvPairs) {
        this(degree, kvPairs, null, null, null);
    }

    /**
     * Construct a leaf node with all attributes
     * @param degree current degree
     * @param kvPairs array representing key-value pairs of the node
     * @param parent parent node
     * @param leftSibling left sibling node
     * @param rightSibling right sibling node
     */
    public LeafNode(int degree, KeyValuePair[] kvPairs, InternalNode parent, LeafNode leftSibling, LeafNode rightSibling) {
        this.degree = degree;
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
        ++degree;
    }

    /**
     * Delete an entry that matches the key value
     * @param deleteKey key to delete
     * @return the deleted entry if found, otherwise null
     */
    public KeyValuePair delete(int deleteKey) {
        for (int i = 0; i < degree; ++i) {
            if (kvPairs[i].getKey().getK1() == deleteKey) {
                KeyValuePair kvToDelete = kvPairs[i];
                Util.deleteAndShift(kvPairs, i);
                --degree;
                return kvToDelete;
            }
        }
        return null;
    }

    /**
     * Delete an entry by its index in the node
     * @param index index of entry to be deleted
     * @return deleted entry
     */
    public KeyValuePair deleteByIndex(int index) {
        KeyValuePair toDelete = kvPairs[index];
        Util.deleteAndShift(kvPairs, index);
        --degree;
        return toDelete;
    }

    /**
     * Delete all entries in the node
     */
    public void deleteAll() {
        Arrays.fill(kvPairs, null);
        degree = 0;
    }

    @Override
    public int getDegree() {
        return degree;
    }

    @Override
    public void setDegree(int degree) {
        this.degree = degree;
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
        for (int i = 0; i < degree; ++i) {
            sb.append("(");
            sb.append(kvPairs[i].getKey().getK1());
            sb.append(", ");
            sb.append(String.valueOf(kvPairs[i].getKey().getK2()).trim());
            sb.append(")  ");
        }
        return sb.toString();
    }
}
