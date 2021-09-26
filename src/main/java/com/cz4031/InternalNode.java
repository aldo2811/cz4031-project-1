package com.cz4031;

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
