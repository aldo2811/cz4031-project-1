package com.cz4031;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Block {

    private Record[] records;
    private int blockSize;

    public Block(Record[] records, int blockSize) {
        this.records = records;
        this.blockSize = blockSize;
    }

    public static Block empty(int blockSize, int recordSize) {
        Record[] records = new Record[blockSize/recordSize];
        for (int i = 0; i < records.length; ++i) {
            records[i] = Record.empty();
        }
        return new Block(records, blockSize);
    }

    public static Block fromByteArray(byte[] byteArr) {
        ByteBuffer buf = ByteBuffer.wrap(byteArr);
        // TODO: Move record size
        int RECORD_SIZE = 19;
        Block block = Block.empty(byteArr.length, RECORD_SIZE);

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

    public Record readRecord(int recordId) {
        return records[recordId];
    }

    public void updateRecord(int recordId, String tconstStr, float avgRating, int numVotes) {
        updateRecord(recordId, tconstStr, avgRating, numVotes, false);
    }

    public void updateRecord(int recordId, String tconstStr, float avgRating, int numVotes, boolean empty) {
        char[] tconst = new char[10];
        System.arraycopy(tconstStr.toCharArray(), 0, tconst, 0, tconstStr.length());
        updateRecord(recordId, tconst, avgRating, numVotes, empty);
    }

    public void updateRecord(int recordId, char[] tconst, float avgRating, int numVotes, boolean empty) {
        records[recordId].setEmpty(empty);
        records[recordId].setTconst(tconst);
        records[recordId].setAvgRating(avgRating);
        records[recordId].setNumVotes(numVotes);
    }

    public void deleteRecord(int recordId) {
        records[recordId].setEmpty(true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.length; ++i) {
            sb.append(String.format("Record %d -> ", i));
            sb.append(records[i]);
            sb.append("\n");
        }
        return sb.toString();
    }
}
