package com.cz4031;

/**
 * Class representing the logical address of a record
 */
public class RecordAddress {

    /**
     * Id of block
     */
    private int blockID;

    /**
     * Id of record
     */
    private int recordID;

    /**
     * Construct record address
     * @param blockID block id
     * @param recordID record id
     */
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
