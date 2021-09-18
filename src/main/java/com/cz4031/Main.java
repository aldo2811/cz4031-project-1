package com.cz4031;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Application");

        Storage st = new Storage();

        bplustree bpt = st.initWithTSV("data.tsv");
        for(int i = 0 ; i < 5 ; i++) {
            Block b = Block.fromByteArray(st.readBlock(i), 19);
            System.out.println(b);
        }
        
        System.out.println(st.readRecord(bpt.search(9916690)));
        System.out.println(st.readRecord(bpt.search(9916420)));
        
        bpt.delete(9916420);
        
        System.out.println(st.readRecord(bpt.search(9916420)));
        
        for (int i=1; i<10; i++)
        	bpt.delete(i);
        
        ArrayList<RecordAddress> records = bpt.search(1,20);
        for (int i=0; i<records.size(); i++) {
        	System.out.println(st.readRecord(records.get(i)));
        }
        
    }
}
