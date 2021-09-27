package com.cz4031;

import java.util.Arrays;

/**
 * Class representing internal node (non-leaf node) in a B+ tree
 * Implements Node interface
 */
public class InternalNode implements Node {

    /**
     * Maximum number of pointers to child node
     */
    private int maxDegree;

    /**
     * Minimum number of pointers to child node
     */
    private int minDegree;

    /**
     * Current number of pointers to child node
     */
    private int curDegree;

    /**
     * Array of keys
     */
    private Key[] keys;

    /**
     * Array of pointers to child node
     */
    private Node[] pointers;

    /**
     * Construct internal node specified with maximum number of keys
     * @param n maximum number of keys in a node
     */
    public InternalNode(int n) {
        this(n, 0, new Key[n], new Node[n+1]);
    }

    /**
     * Construct internal node specified with maximum number of keys in a node, current degree, array of keys, array of
     * pointers
     * @param n maximum number of keys in a node
     * @param curDegree current degree
     * @param keys array of keys
     * @param pointers array of pointers
     */
    public InternalNode(int n, int curDegree, Key[] keys, Node[] pointers) {
        this.maxDegree = n+1;
        this.minDegree = (int) Math.floor(n/2.0) + 1;
        this.curDegree = curDegree;
        this.keys = keys;
        this.pointers = pointers;
    }

    /**
     * Insert a key-node pair to the node while keeping the key and pointer arrays sorted
     * @param knPair key and node to be inserted
     */
    public void addSorted(KeyNodePair knPair) {
        int index = Util.findIndexToInsert(keys, knPair.getKey());
        Util.insertAndShift(keys, knPair.getKey(), index);
        Util.insertAndShift(pointers, knPair.getNode(), index+1);
        ++curDegree;
    }

    /**
     * Add a pointer to the node
     * @param node pointer to child node
     */
    public void addPointer(Node node) {
        pointers[curDegree] = node;
        ++curDegree;
    }

    /**
     * Add a key with a specified position in the node
     * @param key key to be added
     * @param pos position to add the key
     */
    public void addKey(Key key, int pos) {
        Util.insertAndShift(keys, key, pos);
    }

    /**
     * Add a pointer with a specified position in the node
     * @param pointer pointer to be added
     * @param pos position to add the pointer
     */
    public void addPointer(Node pointer, int pos) {
        Util.insertAndShift(pointers, pointer, pos);
        ++curDegree;
    }

    /**
     * Delete a key at the specified position
     * @param pos position of key to be deleted
     * @return deleted key
     */
    public Key deleteKey(int pos) {
        Key key = keys[pos];
        Util.deleteAndShift(keys, pos);
        return key;
    }

    /**
     * Delete a pointer at the specified position
     * @param pos position of pointer to be deleted
     * @return deleted pointer
     */
    public Node deletePointer(int pos) {
        Node pointer = pointers[pos];
        Util.deleteAndShift(pointers, pos);
        --curDegree;
        return pointer;
    }

    /**
     * Delete all keys and pointers in the node
     */
    public void deleteAll() {
        Arrays.fill(keys, null);
        Arrays.fill(pointers, null);
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
     * Checks whether a pointer can be removed from the node while keeping the tree balanced
     * @return true if entry can be removed, otherwise false
     */
    public boolean canDelete() {
        return curDegree > minDegree;
    }

    /**
     * Checks whether the node is underflow, having less than the minimum required number of pointers
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

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public void setMinDegree(int minDegree) {
        this.minDegree = minDegree;
    }

    public Key[] getKeys() {
        return keys;
    }

    public void setKeys(Key[] keys) {
        this.keys = keys;
    }

    public Node[] getPointers() {
        return pointers;
    }

    public void setPointers(Node[] pointers) {
        this.pointers = pointers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Key k : keys) {
            if (k == null) break;
            sb.append(k.getK1());
            sb.append("  ");
        }
        return sb.toString();
    }
}
