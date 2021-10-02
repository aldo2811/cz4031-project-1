package com.cz4031;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Class representing a block in disk storage
 */
public class Block {

    /**
     * Array of records in block
     */
    private Record[] records;

    /**
     * Block size in bytes
     */
    private int blockSize;

    /**
     * Construct a block with list of records and block size
     * @param records array of records
     * @param blockSize block size
     */
    public Block(Record[] records, int blockSize) {
        this.records = records;
        this.blockSize = blockSize;
    }

    /**
     * Construct an empty block
     * @param blockSize size of one block
     * @param recordSize size of one record
     * @return empty block object
     */
    public static Block empty(int blockSize, int recordSize) {
        Record[] records = new Record[blockSize/recordSize];
        for (int i = 0; i < records.length; ++i) {
            records[i] = Record.empty();
        }
        return new Block(records, blockSize);
    }

    /**
     * Construct a block from byte array
     * @param byteArr byte array containing serialized data of block
     * @param recordSize size of one record
     * @return deserialized block
     */
    public static Block fromByteArray(byte[] byteArr, int recordSize) {
        ByteBuffer buf = ByteBuffer.wrap(byteArr);
        Block block = Block.empty(byteArr.length, recordSize);

        for (int i = 0; i < block.records.length; ++i) {
            boolean empty = buf.get() == 1;

            char[] tconst = new char[10];
            for (int j = 0; j < tconst.length; ++j) {
                tconst[j] = (char) buf.get();
            }

            float avgRating = buf.getFloat();
            int numVotes = buf.getInt();

            block.updateRecord(i, tconst, avgRating, numVotes, empty);
        }

        return block;
    }

    /**
     * Convert/serialize block to byte array
     * @return byte array containing serialized data of block
     */
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(blockSize);
        for (Record record : records) {
            buf.put(record.isEmpty() ? (byte) 1 : (byte) 0);
            buf.put(new String(record.getTconst()).getBytes(StandardCharsets.US_ASCII));
            buf.putFloat(record.getAvgRating());
            buf.putInt(record.getNumVotes());
        }
        return buf.array();
    }

    /**
     * Retrieve a record given the id relative to the block
     * @param recordId record id
     * @return record
     */
    public Record readRecord(int recordId) {
        return records[recordId];
    }

    /**
     * Update a record given the id and values
     * @param recordId record id
     * @param tconstStr string representation of tconst attribute
     * @param avgRating average rating attribute
     * @param numVotes number of votes attribute
     */
    public void updateRecord(int recordId, String tconstStr, float avgRating, int numVotes) {
        updateRecord(recordId, tconstStr, avgRating, numVotes, false);
    }

    /**
     * Update a record given the id, values and empty flag
     * @param recordId record id
     * @param tconstStr string representation of tconst attribute
     * @param avgRating average rating attribute
     * @param numVotes number of votes attribute
     * @param empty empty flag
     */
    public void updateRecord(int recordId, String tconstStr, float avgRating, int numVotes, boolean empty) {
        char[] tconst = new char[10];
        System.arraycopy(tconstStr.toCharArray(), 0, tconst, 0, tconstStr.length());
        updateRecord(recordId, tconst, avgRating, numVotes, empty);
    }

    /**
     * Update a record given the id and values
     * @param recordId record id
     * @param tconst tconst attribute
     * @param avgRating average rating attribute
     * @param numVotes number of votes attribute
     * @param empty empty flag
     */
    public void updateRecord(int recordId, char[] tconst, float avgRating, int numVotes, boolean empty) {
        records[recordId].setEmpty(empty);
        records[recordId].setTconst(tconst);
        records[recordId].setAvgRating(avgRating);
        records[recordId].setNumVotes(numVotes);
    }

    /**
     * Delete record given the id
     * @param recordId record id
     */
    public void deleteRecord(int recordId) {
        records[recordId].setEmpty(true);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (Record record : records) {
            joiner.add(record.toString());
        }
        return joiner.toString();
    }
}
