package com.cz4031;

/**
 * Class representing a pair of key and pointer to node
 */
public class KeyNodePair implements Comparable<KeyNodePair> {

    /**
     * Key
     */
    private Key key;

    /**
     * Pointer to node
     */
    private Node node;

    /**
     * Construct a key-node pair
     * @param key key
     * @param node pointer to node
     */
    public KeyNodePair(Key key, Node node) {
        this.key = key;
        this.node = node;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public int compareTo(KeyNodePair k) {
        return this.key.compareTo(k.key);
    }
}
