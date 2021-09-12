package com.cz4031;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Application");

        Storage st = new Storage();

        st.initWithTSV("data.tsv");
        for(int i = 0 ; i < 5 ; i++) {
            Block b = Block.fromByteArray(st.readBlock(i), 19);
            System.out.println(b);
        }
    }
}
