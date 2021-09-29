package com.cz4031;

/**
 * Interface representing a node in a B+ tree
 */
public interface Node {

    public int getCurDegree();

    public void setCurDegree(int curDegree);

    public InternalNode getParent();

    public void setParent(InternalNode parent);

    public String toString();
}
