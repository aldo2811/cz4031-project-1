package com.cz4031;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class representing the B+ tree data structure
 */
public class BPlusTree {

    /**
     * Maximum number of keys in a node
     */
    private int n;

    /**
     * Root node of B+ tree
     */
    private Node root;

    /**
     * Construct a B+ tree
     * @param n maximum number of keys in a node
     */
    public BPlusTree(int n) {
        this.n = n;

        // Initialize root as an empty leaf node
        this.root = new LeafNode(n);
    }

    /**
     * Search for records with the specified value
     * @param searchKey search key (numVotes)
     * @return a list of record addresses with a key value equal to the search key
     */
    public List<RecordAddress> search(int searchKey) {
        // Since there could be more than one result for a search key, searching for a single key can be done
        // by using range search, with the search key as both the lower and upper bound
        return searchInternal(root, searchKey, searchKey);
    }

    /**
     * Search for records with the value within the lower and upper bounds
     * @param lower lower bound of the search key (numVotes)
     * @param upper upper bound of the search key (numVotes)
     * @return a list of record addresses with a key value between the lower and upper bounds
     */
    public List<RecordAddress> search(int lower, int upper) {
        return searchInternal(root, lower, upper);
    }

    /**
     * Internal implementation of searching in the B+ tree
     * @param node current node
     * @param lower lower bound of search
     * @param upper upper bound of search
     * @return a list of record addresses with a key value between the lower and upper bounds
     */
    public List<RecordAddress> searchInternal(Node node, int lower, int upper) {
        List<RecordAddress> result = new ArrayList<>();
        if (node instanceof LeafNode) {
            LeafNode leafNode = (LeafNode) node;
            boolean finished = false;

            // Iterate through leaf node to find all occurrences of search key
            while (leafNode != null && !finished) {
                KeyValuePair[] kvPairs = leafNode.getKvPairs();

                for (KeyValuePair kv : kvPairs) {
                    // If key-value pair is null, continue to next leaf node
                    if (kv == null) break;

                    // Add to result if current key value is within lower and upper bounds
                    // Finish search if it is higher than the upper bound
                    int curK1 = kv.getKey().getK1();
                    if (lower <= curK1 && curK1 <= upper) {
                        result.add(kv.getRecordAddress());
                    } else if (upper < curK1) {
                        finished = true;
                        break;
                    }
                }

                // Iterate to right sibling of leaf node
                leafNode = leafNode.getRightSibling();
            }
        } else if (node instanceof InternalNode) {
            InternalNode curNode = (InternalNode) node;

            // Traverse to the leftmost subtree possibly containing the lower bound
            int pointerIndex = findIndexOfNode(curNode, lower);
            return searchInternal(curNode.getPointers()[pointerIndex], lower, upper);
        }

        return result;
    }

    /**
     * Insert to B+ tree with numVotes and tconst as key to support duplicate values of numVotes
     * Value of the entry is the logical address of the record (Block ID, Record ID)
     * @param record record to be inserted
     * @param address address of record to be inserted
     */
    public void insert(Record record, RecordAddress address) {
        Key key = new Key(record.getNumVotes(), record.getTconst());
        KeyValuePair entry = new KeyValuePair(key, address);

        // Insert by traversing the tree from the root node
        insertInternal(this.root, entry, null);
    }

    /**
     * Internal implementation of insertion in B+ tree
     * @param node current node
     * @param entry entry to be inserted
     * @param newChildEntry key-node pair which points to split child, null if child was not split
     * @return a key-node pair if current node is split, otherwise null
     */
    public KeyNodePair insertInternal(Node node, KeyValuePair entry, KeyNodePair newChildEntry) {
        boolean split = false;
        if (node instanceof InternalNode) {
            InternalNode curNode = (InternalNode) node;

            // Find index of pointer to leftmost node that can be inserted with the entry
            int pointerIndex = findIndexOfNode(curNode, entry.getKey());

            // Insert entry to subtree
            newChildEntry = insertInternal(curNode.getPointers()[pointerIndex], entry, newChildEntry);

            if (newChildEntry != null) {
                if (!curNode.isFull()) {
                    // Insert entry to node if it is not full
                    curNode.addSorted(newChildEntry);
                    newChildEntry = null;
                } else {
                    // Split node if it is full
                    newChildEntry = splitNode(curNode, newChildEntry);
                    split = true;
                }
            }

        } else if (node instanceof LeafNode) {
            LeafNode leafNode = (LeafNode) node;
            if (!leafNode.isFull()) {
                // Add entry to leaf node if it is not full
                leafNode.addSorted(entry);
                newChildEntry = null;
            } else {
                // Split leaf if it is full
                newChildEntry = splitLeaf(leafNode, entry);
                split = true;
            }
        }

        if (split && root == node) {
            // If root is split, add a new node to be the root
            InternalNode newNode = new InternalNode(n);
            newNode.addPointer(node);
            newNode.addSorted(newChildEntry);
            root = newNode;
        }

        return newChildEntry;
    }

    /**
     * Delete all entries with having the specified delete key as its primary key
     * @param deleteKey key to delete
     */
    public void delete(int deleteKey) {
        // Keep deleting until the key is not found in the B+ tree
        DeleteResult result = deleteInternal(null, root, deleteKey, null);
        while (result.isFound()) {
            result = deleteInternal(null, root, deleteKey, null);
        }
    }

    /**
     * Internal implementation of deletion in B+ tree
     * @param parentNode parent of current node
     * @param curNode current node
     * @param deleteKey key to delete
     * @param oldChildIndex index of deleted child node if any, otherwise null
     * @return result object consisting of index of deleted child node and boolean indicating if an entry is deleted
     */
    public DeleteResult deleteInternal(InternalNode parentNode, Node curNode, int deleteKey, Integer oldChildIndex) {
        DeleteResult result;
        boolean found = false;

        if (curNode instanceof InternalNode) {
            InternalNode node = (InternalNode) curNode;

            // Find pointer to node possibly containing key to delete
            int pointerIndex = findIndexOfNode(node, deleteKey);

            // Recursively delete
            result = deleteInternal(node, node.getPointers()[pointerIndex], deleteKey, oldChildIndex);

            // Retrieve index of deleted child node, null if no deletion
            oldChildIndex = result.getOldChildIndex();
            found = result.isFound();

            if (found && oldChildIndex != null) {
                // Delete key and pointer to deleted child node
                node.deleteKey(oldChildIndex-1);
                node.deletePointer(oldChildIndex);

                if (!node.isUnderflow() || node == root) {
                    oldChildIndex = null;
                } else {
                    // Find index of pointer to current node in parent
                    int curNodeIndex = findIndexOfPointer(parentNode, node);

                    // Get left and right siblings of current node
                    InternalNode rightSibling = null, leftSibling = null;
                    if (curNodeIndex > 0) {
                        leftSibling = (InternalNode) parentNode.getPointers()[curNodeIndex-1];
                    }
                    if (curNodeIndex < parentNode.getMaxDegree()-1) {
                        rightSibling = (InternalNode) parentNode.getPointers()[curNodeIndex+1];
                    }

                    if (rightSibling != null && rightSibling.canDelete()) {
                        // If right sibling has extra entries, borrow from right sibling
                        Key temp = rightSibling.getKeys()[0];
                        rightSibling.getKeys()[0] = parentNode.getKeys()[curNodeIndex];
                        parentNode.getKeys()[curNodeIndex] = temp;

                        moveEntryToLeftInternalNode(node, rightSibling);
                        oldChildIndex = null;
                    } else if (leftSibling != null && leftSibling.canDelete()) {
                        // If left sibling has extra entries, borrow from left sibling
                        Key temp = leftSibling.getKeys()[leftSibling.getCurDegree()-2];
                        leftSibling.getKeys()[leftSibling.getCurDegree()-2] = parentNode.getKeys()[curNodeIndex-1];
                        parentNode.getKeys()[curNodeIndex-1] = temp;

                        moveEntryToRightInternalNode(leftSibling, node);
                        oldChildIndex = null;
                    } else if (rightSibling != null) {
                        oldChildIndex = curNodeIndex+1;
                        node.addKey(parentNode.getKeys()[curNodeIndex], node.getCurDegree()-1);
                        merge(rightSibling, node);
                        parentNode.getKeys()[curNodeIndex] = node.getKeys()[0];
                    } else if (leftSibling != null) {
                        oldChildIndex = curNodeIndex;
                        leftSibling.addKey(parentNode.getKeys()[curNodeIndex-1], leftSibling.getCurDegree()-1);
                        merge(node, leftSibling);
                        parentNode.getKeys()[curNodeIndex-1] = leftSibling.getKeys()[0];
                    }
                }
            }
        } else if (curNode instanceof LeafNode) {
            LeafNode node = (LeafNode) curNode;
            found = node.delete(deleteKey);
            if (found && node.isUnderflow() && node != root) {
                // Get siblings and their index on parent
                LeafNode rightSibling = node.getRightSibling();
                LeafNode leftSibling = node.getLeftSibling();
                int leftSiblingIndex = findIndexOfPointer(parentNode, leftSibling);
                int rightSiblingIndex = findIndexOfPointer(parentNode, rightSibling);
                int curNodeIndex = findIndexOfPointer(parentNode, node);

                // Check whether merging or borrowing from left and right siblings are possible
                boolean canUseRight = rightSibling != null && rightSiblingIndex < parentNode.getPointers().length;
                boolean canUseLeft = leftSibling != null && leftSiblingIndex < parentNode.getPointers().length;

                if (canUseRight && rightSibling.canDelete()) {
                    // Borrow from right sibling
                    moveEntryToLeftLeafNode(node, rightSibling);
                    parentNode.getKeys()[rightSiblingIndex] = rightSibling.getKvPairs()[0].getKey();
                } else if (canUseLeft && leftSibling.canDelete()) {
                    // Borrow from left sibling
                    moveEntryToRightLeafNode(leftSibling, node);
                    parentNode.getKeys()[curNodeIndex] = node.getKvPairs()[0].getKey();
                } else if (canUseRight) {
                    // Merge with right sibling
                    oldChildIndex = rightSiblingIndex;
                    merge(rightSibling, node);

                    // Modify siblings of nodes
                    node.setRightSibling(rightSibling.getRightSibling());
                    if (node.getRightSibling() != null) node.getRightSibling().setLeftSibling(node);
                } else if (canUseLeft) {
                    // Merge with left sibling
                    oldChildIndex = curNodeIndex;
                    merge(node, leftSibling);

                    // Modify siblings of nodes
                    leftSibling.setRightSibling(node.getRightSibling());
                    if (leftSibling.getRightSibling() != null) leftSibling.getRightSibling().setLeftSibling(leftSibling);
                }
            }
        }

        // If current node is root and it only has 1 child, make child the new root
        if (root == curNode && root instanceof InternalNode && root.getCurDegree() == 1) {
            InternalNode temp = (InternalNode) root;
            root = ((InternalNode) root).getPointers()[0];
            temp.deleteAll();
        }

        return new DeleteResult(oldChildIndex, found);
    }

    /**
     * Merge 2 internal nodes by appending keys and pointers of src to dst
     * @param src source internal node
     * @param dst destination internal node, used to store the merged node
     */
    public void merge(InternalNode src, InternalNode dst) {
        System.arraycopy(src.getKeys(), 0, dst.getKeys(), dst.getCurDegree(), src.getCurDegree() - 1);
        System.arraycopy(src.getPointers(), 0, dst.getPointers(), dst.getCurDegree(), src.getCurDegree());
        dst.setCurDegree(src.getCurDegree() + dst.getCurDegree());
        src.deleteAll();
    }

    /**
     * Merge 2 leaf nodes by appending key-value pairs of src to dst
     * @param src source leaf node
     * @param dst destination leaf node, used to store the merged node
     */
    public void merge(LeafNode src, LeafNode dst) {
        System.arraycopy(src.getKvPairs(), 0, dst.getKvPairs(), dst.getCurDegree(), src.getCurDegree());
        dst.setCurDegree(dst.getCurDegree() + src.getCurDegree());
        src.deleteAll();
    }

    /**
     * Move the leftmost key and pointer of right internal node to left internal node
     * @param left left internal node (destination)
     * @param right right internal node (source)
     */
    public void moveEntryToLeftInternalNode(InternalNode left, InternalNode right) {
        Key key = right.deleteKey(0);
        Node pointer = right.deletePointer(0);
        left.addKey(key, left.getCurDegree() - 1);
        left.addPointer(pointer, left.getCurDegree());
    }

    /**
     * Move the right most key and pointer of left internal node to right internal node
     * @param left left internal node (source)
     * @param right right internal node (destination)
     */
    public void moveEntryToRightInternalNode(InternalNode left, InternalNode right) {
        Key key = left.deleteKey(left.getCurDegree() - 2);
        Node pointer = left.deletePointer(left.getCurDegree() - 1);
        right.addKey(key, 0);
        right.addPointer(pointer, 0);
    }

    /**
     * Move the leftmost entry of right leaf node to left leaf node
     * @param left left leaf node (destination)
     * @param right right leaf node (source)
     */
    public void moveEntryToLeftLeafNode(LeafNode left, LeafNode right) {
        KeyValuePair entry = right.deleteByIndex(0);
        left.addSorted(entry);
    }

    /**
     * Move the rightmost entry of left leaf node to right leaf node
     * @param left left leaf node (source)
     * @param right right leaf node (destination)
     */
    public void moveEntryToRightLeafNode(LeafNode left, LeafNode right) {
        KeyValuePair entry = left.deleteByIndex(left.getCurDegree() - 1);
        right.addSorted(entry);
    }

    /**
     * Split full internal node into two parts
     * @param node internal node to be split
     * @param knPair key and pointer to be added
     * @return pair of the smallest key in second node and pointer to second node
     */
    public KeyNodePair splitNode(InternalNode node, KeyNodePair knPair) {
        Key[] keys = node.getKeys();
        Node[] pointers = node.getPointers();

        // Create temporary array to store existing and to be added keys and pointers
        Key[] tempKeys = Arrays.copyOf(keys, keys.length+1);
        Node[] tempPointers = Arrays.copyOf(pointers, pointers.length+1);

        // Find midpoint to split node
        int mid = (int) Math.floor((n+1)/2.0);

        // Find on which index the key and pointer can be inserted in order to keep it sorted
        int indexToInsertKey = Util.findIndexToInsert(tempKeys, knPair.getKey());

        // Insert key and pointer to temporary array
        Util.insertAndShift(tempKeys, knPair.getKey(), indexToInsertKey);
        Util.insertAndShift(tempPointers, knPair.getNode(), indexToInsertKey+1);

        // Split key and pointer arrays in half
        Key[] firstHalfKeys = Arrays.copyOfRange(tempKeys, 0, mid);
        Node[] firstHalfPointers = Arrays.copyOfRange(tempPointers, 0, mid+1);
        Key[] secondHalfKeys = Arrays.copyOfRange(tempKeys, mid+1, tempKeys.length);
        Node[] secondHalfPointers = Arrays.copyOfRange(tempPointers, mid+1, tempPointers.length);

        // Set keys and pointers to nodes
        node.setKeys(Arrays.copyOf(firstHalfKeys, keys.length));
        node.setPointers(Arrays.copyOf(firstHalfPointers, pointers.length));
        node.setCurDegree(firstHalfPointers.length);
        InternalNode newNode = new InternalNode(n, secondHalfPointers.length,
                Arrays.copyOf(secondHalfKeys, keys.length), Arrays.copyOf(secondHalfPointers, pointers.length));

        // Return pair of the smallest key in second node and pointer to second node
        return new KeyNodePair(tempKeys[mid], newNode);
    }

    /**
     * Split full leaf node into two parts
     * @param node leaf node to be split
     * @param entry entry to be added
     * @return pair of the smallest key in second node and pointer to second node
     */
    public KeyNodePair splitLeaf(LeafNode node, KeyValuePair entry) {
        KeyValuePair[] kvPairs = node.getKvPairs();
        KeyValuePair[] temp = Arrays.copyOf(kvPairs, kvPairs.length+1);

        // Find midpoint to split node
        int mid = (int) Math.ceil((n+1)/2.0);

        // Find on which index the entry can be inserted to kvPairs in order to keep it sorted
        int indexToInsert = Util.findIndexToInsert(temp, entry);

        // Insert key-value pair
        Util.insertAndShift(temp, entry, indexToInsert);

        // Split key-value pair array into half
        KeyValuePair[] firstHalf = Arrays.copyOfRange(temp, 0, mid);
        KeyValuePair[] secondHalf = Arrays.copyOfRange(temp, mid, temp.length);

        // Set key-value pairs to nodes
        node.setKvPairs(Arrays.copyOf(firstHalf, kvPairs.length));
        node.setCurDegree(firstHalf.length);
        LeafNode newLeaf = new LeafNode(n, secondHalf.length, Arrays.copyOf(secondHalf, kvPairs.length));

        // Modify sibling relations on leaf nodes
        LeafNode rightSibling = node.getRightSibling();
        node.setRightSibling(newLeaf);
        newLeaf.setRightSibling(rightSibling);
        newLeaf.setLeftSibling(node);
        if (rightSibling != null) rightSibling.setLeftSibling(newLeaf);

        // Return pair of the smallest key in second node and pointer to second node
        return new KeyNodePair(newLeaf.getKvPairs()[0].getKey(), newLeaf);
    }

    /**
     * Find index of leftmost child node that can be inserted with key value
     * @param node parent node
     * @param keyValue integer value of key
     * @return index of insertion
     */
    public int findIndexOfNode(InternalNode node, int keyValue) {
        return findIndexOfNode(node, new Key(keyValue, new char[10]));
    }

    /**
     * Find index of leftmost child node that can be inserted with key
     * @param node parent node
     * @param key key
     * @return index of insertion
     */
    public int findIndexOfNode(InternalNode node, Key key) {
        Key[] keys = node.getKeys();
        int i;
        for (i = 0; i < keys.length; ++i) {
            if (key.compareTo(keys[i]) < 0) break;
        }
        return i;
    }

    /**
     * Find index of pointer to node in its parent node
     * @param parentNode parent of node
     * @param node node
     * @return index of node
     */
    public int findIndexOfPointer(InternalNode parentNode, Node node) {
        Node[] pointers = parentNode.getPointers();
        int i;
        for (i = 0; i < pointers.length; ++i) {
            if (pointers[i] == node) break;
        }
        return i;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Queue<Node> temp = new LinkedList<>();
            while (!queue.isEmpty()) {
                Node cur = queue.remove();
                sb.append(cur.toString()).append("    ");
                for (int i = 0; i < cur.getCurDegree(); ++i) {
                    if (cur instanceof InternalNode) {
                        temp.add(((InternalNode) cur).getPointers()[i]);
                    }
                }
            }
            sb.append("\n");
            queue = temp;
        }
        return sb.toString();
    }
}
