package com.cz4031;

import java.util.LinkedList;
import java.util.List;

public class AccessLogger {
    private List<Node> nodeList;
    private List<RecordAddress> blockList;

    private int numNodeAccess;
    private int numBlockAccess;

    private Storage st;

    public AccessLogger(Storage st) {
        this.st = st;
        nodeList = new LinkedList<>();
        blockList = new LinkedList<>();
        numNodeAccess = 0;
        numBlockAccess = 0;
    }

    public void reset() {
        nodeList.clear();
        blockList.clear();
        numNodeAccess = 0;
        numBlockAccess = 0;
    }

    public void addNode(Node node) {
        ++numNodeAccess;
        if (nodeList.size() < 5) nodeList.add(node);
    }

    public void addBlock(RecordAddress ra) {
        ++numBlockAccess;
        if (blockList.size() < 5) blockList.add(ra);
    }

    public String getNodeAccess() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nodeList.size();++i) {
            sb.append(String.format("%d. ", i+1));
            sb.append(nodeList.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getBlockAccess() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<blockList.size(); ++i) {
            sb.append(String.format("%d. ", i+1));
            sb.append(Block.fromByteArray(st.readBlock(blockList.get(i).getBlockID()),st.getRecordSize()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getNumNodeAccess() {
        return numNodeAccess;
    }

    public int getNumBlockAccess() {
        return numBlockAccess;
    }
}
