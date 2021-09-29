package com.cz4031;

/**
 * Class representing result of deletion in B+ tree, used as a helper to implement recursive deletion on the tree
 */
public class DeleteResult {

    /**
     * Index of deleted child node if any, otherwise null
     */
    private Integer oldChildIndex;

    private InternalNode parentNode;

    /**
     * Indicates if an entry has been deleted
     */
    private boolean found;

    /**
     * Construct a DeleteResult object
     * @param oldChildIndex index of deleted child node if any, otherwise null
     * @param found indicates if an entry has been deleted
     */
    public DeleteResult(Integer oldChildIndex, InternalNode parentNode, boolean found) {
        this.oldChildIndex = oldChildIndex;
        this.parentNode = parentNode;
        this.found = found;
    }

    public Integer getOldChildIndex() {
        return oldChildIndex;
    }

    public void setOldChildIndex(Integer oldChildIndex) {
        this.oldChildIndex = oldChildIndex;
    }

    public InternalNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(InternalNode parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
