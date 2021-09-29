package com.cz4031;

/**
 * Interface representing a node in a B+ tree
 */
public interface Node {

    int getDegree();

    void setDegree(int degree);

    InternalNode getParent();

    void setParent(InternalNode parent);

    String toString();
}
