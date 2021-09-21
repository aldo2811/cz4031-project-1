package com.cz4031;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Application");

        Storage st = new Storage();

        st.initWithTSV("data.tsv");
        for(int i = 0 ; i < 5 ; i++) {
            Block b = Block.fromByteArray(st.readBlock(i), 19);
            System.out.println(b);
        }

        BPlusTree bpt = st.buildIndex();
        List<RecordAddress> r = bpt.search(262);
        bpt.delete(262);
        List<RecordAddress> rr = bpt.search(262);
    }
}
