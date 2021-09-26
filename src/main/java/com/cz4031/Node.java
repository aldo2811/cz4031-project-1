package com.cz4031;

/**
 * Interface representing a node in a B+ tree
 */
public interface Node {

    public int getCurDegree();

    public void setCurDegree(int curDegree);

    public String toString();
}
