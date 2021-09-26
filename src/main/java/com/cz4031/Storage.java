package com.cz4031;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

public class Storage {
    private final int BLOCK_SIZE = 100;
    private final int RECORD_SIZE = 19;
    private final int NUM_OF_RECORD = BLOCK_SIZE / RECORD_SIZE;
    private final int MEMORY_SIZE = 200 << 20;

    private byte[] blocks ;
    private int blockTailIdx;
    private LinkedList<RecordAddress> emptyRecord;

    public Storage() {
        blocks = new byte[MEMORY_SIZE];
        blockTailIdx = -1;
        emptyRecord = new LinkedList<>();
    }

    public void initWithTSV(String path) {
        try {
            Reader in = new FileReader(path);
            Iterable<CSVRecord> records = CSVFormat.TDF.builder().setHeader().setSkipHeaderRecord(true).build().parse(in);
            for (CSVRecord record : records) {
                createRecord(
                        record.get("tconst"),
                        Float.parseFloat(record.get("averageRating")),
                        Integer.parseInt(record.get("numVotes"))
                );
            }
        } catch (FileNotFoundException e) {
            System.out.println("Wrong file path");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while reading file");
            e.printStackTrace();
        }
    }

    /**
     * Build B+ tree on database by inserting the records from database sequentially
     * @return B+ tree filled with records
     */
    public BPlusTree buildIndex() {
        BPlusTree bpt = new BPlusTree(3);
        for (int blockID = 0; blockID <= blockTailIdx; ++blockID) {
            Block block = Block.fromByteArray(readBlock(blockID), RECORD_SIZE);
            for (int recordID = 0; recordID < NUM_OF_RECORD; ++recordID) {
                Record record = block.readRecord(recordID);
                if (!record.isEmpty()) {
                    bpt.insert(record, new RecordAddress(blockID, recordID));
                }
            }
        }
        return bpt;
    }

    /**
     * Insert a new record into "disk storage"
     * @param tConst data for the record
     * @param rating data for the record
     * @param numVotes data for the record
     */
    public RecordAddress createRecord(String tConst, float rating, int numVotes) {
        if(emptyRecord.isEmpty()) createBlock();

        RecordAddress address = emptyRecord.element();
        emptyRecord.remove();

        Block block = Block.fromByteArray(readBlock(address.getBlockID()), RECORD_SIZE);
        block.updateRecord(address.getRecordID(), tConst, rating, numVotes);
        updateBlock(address.getBlockID(), block.toByteArray());

        return address;
    }

    /**
     * Read a record given its address
     * @param address address of record to get
     */
    public Record readRecord(RecordAddress address) {
        return Block.fromByteArray(readBlock(address.getBlockID()), RECORD_SIZE).readRecord(address.getRecordID());
    }

    /**
     * Delete a record given its address, reallocate it for reuse
     * @param address address of record to be deleted
     */
    public void deleteRecord(RecordAddress address) {
        Block block = Block.fromByteArray(readBlock(address.getBlockID()), RECORD_SIZE);
        block.deleteRecord(address.getRecordID());
        updateBlock(address.getBlockID(), block.toByteArray());

        emptyRecord.add(address);
    }

    /**
     * Prepare a new block so that it can be used.
     */
    public void createBlock() {
        blockTailIdx++;
        Block block = Block.empty(BLOCK_SIZE, RECORD_SIZE);
        updateBlock(blockTailIdx, block.toByteArray());
        for(int recordID = 0; recordID < NUM_OF_RECORD ; ++recordID) {
            emptyRecord.add(new RecordAddress(blockTailIdx, recordID));
        }
    }

    /**
     * Update block in "disk storage".
     * @param blockID ID of block to be updated
     * @param data data of the updated block
     */
    public void updateBlock(int blockID, byte[] data) {
        System.arraycopy(data, 0, blocks, blockID * BLOCK_SIZE, BLOCK_SIZE);
    }

    /**
     * Get a block from "disk storage"
     * @param blockID ID of the block to get
     * @return a block with given ID
     */
    public byte[] readBlock(int blockID) {
        return Arrays.copyOfRange(blocks, blockID * BLOCK_SIZE, blockID * BLOCK_SIZE + BLOCK_SIZE);
    }
}
