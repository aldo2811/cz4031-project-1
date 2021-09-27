package com.cz4031;

/**
 * Class representing result of deletion in B+ tree, used as a helper to implement recursive deletion on the tree
 */
public class DeleteResult {

    /**
     * Index of deleted child node if any, otherwise null
     */
    private Integer oldChildIndex;

    /**
     * Indicates if an entry has been deleted
     */
    private boolean found;

    /**
     * Construct a DeleteResult object
     * @param oldChildIndex index of deleted child node if any, otherwise null
     * @param found indicates if an entry has been deleted
     */
    public DeleteResult(Integer oldChildIndex, boolean found) {
        this.oldChildIndex = oldChildIndex;
        this.found = found;
    }

    public Integer getOldChildIndex() {
        return oldChildIndex;
    }

    public void setOldChildIndex(Integer oldChildIndex) {
        this.oldChildIndex = oldChildIndex;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
