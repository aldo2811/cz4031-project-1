package com.cz4031;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Application");

        Storage st = new Storage();

        st.initWithTSV("data.tsv");
        // TODO: Use storage instead of initializing b+ tree here
        // Experiment 2
        BPlusTree bpt = st.buildIndex();
        System.out.println("\nExperiment 2");
        System.out.println("Parameter n of B+ tree: " + bpt.getN());
        System.out.println("Number of nodes in B+ tree: " + bpt.getTotalNodes());
        System.out.println("Height of B+ tree: " + bpt.getHeight());

        // TODO: Retrieve number and content of data blocks accessed
        // TODO: Move search to storage
        // Experiment 3
        bpt.search(500);
        System.out.println("\nExperiment 3");
        System.out.println("Number of index nodes accessed: " + bpt.getTotalNodeAccess());
        // TODO: Add logging for accessed nodes

        // Experiment 4
        bpt.search(30000, 40000);
        System.out.println("\nExperiment 4");
        System.out.println("Number of index nodes accessed: " + bpt.getTotalNodeAccess());

        // TODO: Move delete to storage
        // Experiment 5
        bpt.delete(1000);
        System.out.println("\nExperiment 5");
        System.out.println("Total number of deleted nodes: " + bpt.getTotalNodesDeleted());
        System.out.println("Number of nodes of updated B+ tree: " + bpt.getTotalNodes());
        System.out.println("Height of updated B+ tree: " + bpt.getHeight());
        System.out.println("Content of root node: " + bpt.getRoot());
        System.out.println("Content of first child of root node: " + ((InternalNode)bpt.getRoot()).getPointers()[0]);

        // TODO: Redo experiment with 500 B block
        // TODO: Add installation guide for running code
    }
}
