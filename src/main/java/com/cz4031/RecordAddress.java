package com.cz4031;

public class RecordAddress {
    private int blockID;
    private int recordID;

    public RecordAddress(int blockID, int recordID) {
        this.blockID = blockID;
        this.recordID = recordID;
    }

    public int getBlockID() {
        return blockID;
    }

    public int getRecordID() {
        return recordID;
    }
}
