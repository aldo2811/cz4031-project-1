package com.cz4031;

import java.util.Arrays;

/**
 * Class representing internal node (non-leaf node) in a B+ tree
 * Implements Node interface
 */
public class InternalNode implements Node {

    /**
     * Current number of pointers to child node
     */
    private int degree;

    /**
     * Array of keys
     */
    private Key[] keys;

    /**
     * Array of pointers to child node
     */
    private Node[] pointers;

    /**
     * Parent node
     */
    private InternalNode parent;

    /**
     * Construct an empty internal node specified with maximum number of keys
     * @param n maximum number of keys in a node
     */
    public InternalNode(int n) {
        this(0, new Key[n], new Node[n+1], null);
    }

    /**
     * Construct an internal node with current degree, array of keys and pointers
     * @param degree current degree of node
     * @param keys array of keys
     * @param pointers array of pointers
     */
    public InternalNode(int degree, Key[] keys, Node[] pointers) {
        this(degree, keys, pointers, null);
    }

    /**
     * Construct an internal node with current degree, array of keys and pointers, and parent node
     * @param degree current degree of node
     * @param keys array of keys
     * @param pointers array of pointers
     * @param parent parent node
     */
    public InternalNode(int degree, Key[] keys, Node[] pointers, InternalNode parent) {
        this.degree = degree;
        this.keys = keys;
        this.pointers = pointers;
        this.parent = parent;
    }

    /**
     * Insert a key-node pair to the node while keeping the key and pointer arrays sorted
     * @param knPair key and node to be inserted
     */
    public void addSorted(KeyNodePair knPair) {
        int index = Util.findIndexToInsert(keys, knPair.getKey());
        Util.insertAndShift(keys, knPair.getKey(), index);
        Util.insertAndShift(pointers, knPair.getNode(), index+1);
        ++degree;
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
        ++degree;
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
        --degree;
        return pointer;
    }

    /**
     * Delete all keys and pointers in the node
     */
    public void deleteAll() {
        Arrays.fill(keys, null);
        Arrays.fill(pointers, null);
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

    public InternalNode getParent() {
        return parent;
    }

    public void setParent(InternalNode parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Key k : keys) {
            if (k == null) break;
            sb.append("(");
            sb.append(k.getK1());
            sb.append(", ");
            sb.append(String.valueOf(k.getK2()).trim());
            sb.append(")  ");
        }
        return sb.toString();
    }
}
