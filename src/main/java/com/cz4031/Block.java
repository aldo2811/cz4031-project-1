package com.cz4031;

import java.io.IOException;
import java.io.Serializable;

public class Block implements Serializable {

    private Record[] records;

    public Block(byte[] byteArr, int recordSize) {
        records = new Record[byteArr.length/recordSize];
        for (int i = 0; i < records.length; ++i) {
            records[i] = new Record();
        }
    }

    public Record readRecord(int recordId) {
        return records[recordId];
    }

    public void updateRecord(int recordId, char[] tconst, float avgRating, int numVotes) {
        records[recordId].setTconst(tconst);
        records[recordId].setAvgRating(avgRating);
        records[recordId].setNumVotes(numVotes);
        records[recordId].setEmpty(false);
    }

    public void deleteRecord(int recordId) {
        records[recordId].setEmpty(true);
    }

    public byte[] convertBlockToByteArray() throws IOException {
        return Serializer.serialize(this);
    }

    public static Block convertByteArrayToBlock(byte[] byteArr) throws IOException, ClassNotFoundException {
        return (Block) Serializer.deserialize(byteArr);
    }
}
