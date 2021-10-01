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
     * Maximum degree (number of child) of an internal node
     */
    private int maxDegreeInternal;

    /**
     * Minimum degree (number of child) of an internal node
     */
    private int minDegreeInternal;

    /**
     * Maximum number of keys in a leaf node
     */
    private int maxKeysLeaf;

    /**
     * Minimum number of keys in a leaf node
     */
    private int minKeysLeaf;

    /**
     * Height of tree
     */
    private int height;

    /**
     * Total number of nodes in the tree
     */
    private int totalNodes;

    /**
     * Total number of nodes deleted in a delete operation
     */
    private int totalNodesDeleted;

    /**
     * Storage, for logging purposes
     */
    private Storage st;

    /**
     * Construct a B+ tree
     * @param n maximum number of keys in a node
     * @param st storage, for logging purposes
     */
    public BPlusTree(int n, Storage st) {
        this.n = n;

        // Initialize root as an empty leaf node
        this.root = null;
        this.maxDegreeInternal = n+1;
        this.minDegreeInternal = (int) Math.floor(n/2.0) + 1;
        this.maxKeysLeaf = n;
        this.minKeysLeaf = (int) Math.floor((n+1) / 2.0);
        this.height = 0;
        this.totalNodes = 0;
        this.totalNodesDeleted = 0;
        this.st = st;
    }

    /**
     * Search for records with the specified value
     * @param searchKey search key (numVotes)
     * @return a list of record addresses with a key value equal to the search key
     */
    public List<RecordAddress> search(int searchKey) {
        // Reset logs for experiment
        st.resetLog();

        // Since there could be more than one result for a search key, searching for a single key can be done
        // by using range search, with the search key as both the lower and upper bound
        return searchInternal(root, searchKey, searchKey);
    }

    /**
     * Search for records with the value within the lower and upper bounds
     * @param lower lower bound of the search key, inclusive (numVotes)
     * @param upper upper bound of the search key, inclusive (numVotes)
     * @return a list of record addresses with a key value between the lower and upper bounds
     */
    public List<RecordAddress> search(int lower, int upper) {
        // Reset logs for experiment
        st.resetLog();

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
                // Record node access here, since leaf nodes can be traversed through siblings
                st.logNodeAccess(leafNode);

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

            // Record node access
            st.logNodeAccess(curNode);

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
        if (root == null) {
            root = new LeafNode(n);
            node = root;
        }

        boolean split = false;
        if (node instanceof InternalNode) {
            InternalNode curNode = (InternalNode) node;

            // Find index of pointer to leftmost node that can be inserted with the entry
            int pointerIndex = findIndexOfNode(curNode, entry.getKey());

            // Insert entry to subtree
            newChildEntry = insertInternal(curNode.getPointers()[pointerIndex], entry, newChildEntry);

            if (newChildEntry != null) {
                newChildEntry.getNode().setParent(curNode);
                if (curNode.getDegree() < maxDegreeInternal) {
                    // Insert entry to node if it is not full
                    curNode.addSorted(newChildEntry);
                    newChildEntry = null;
                } else {
                    // Split node if it is full
                    newChildEntry = splitNode(curNode, newChildEntry);
                    split = true;

                    // Increase number of nodes
                    ++totalNodes;
                }
            }

        } else if (node instanceof LeafNode) {
            LeafNode leafNode = (LeafNode) node;
            if (leafNode.getDegree() < maxKeysLeaf) {
                // Add entry to leaf node if it is not full
                leafNode.addSorted(entry);
                newChildEntry = null;
            } else {
                // Split leaf if it is full
                newChildEntry = splitLeaf(leafNode, entry);
                split = true;

                // Increase number of nodes
                ++totalNodes;
            }
        }

        if (split && root == node) {
            // If root is split, add a new node to be the root
            InternalNode newNode = new InternalNode(n);
            newNode.addPointer(node, newNode.getDegree());
            newNode.addSorted(newChildEntry);
            node.setParent(newNode);
            newChildEntry.getNode().setParent(newNode);
            root = newNode;

            // Increase height of tree and number of nodes
            ++height;
            ++totalNodes;
        }

        return newChildEntry;
    }

    /**
     * Delete all entries having the specified key as its primary key
     * @param deleteKey key to delete
     */
    public void delete(int deleteKey) {
        // Initialize total number of nodes deleted for experiment
        totalNodesDeleted = 0;

        // Keep deleting until the key is not found in the B+ tree
        DeleteResult result;
        do {
            result = deleteInternal(root, deleteKey, null);
        } while (result != null && result.isFound());
    }

    /**
     * Internal implementation of deletion in B+ tree
     * @param curNode current node
     * @param deleteKey key to delete
     * @param oldChildIndex index of deleted child node if any, otherwise null
     * @return result object consisting of index of deleted child node, parent of traversed node,
     * and boolean indicating if an entry is deleted
     */
    public DeleteResult deleteInternal(Node curNode, int deleteKey, Integer oldChildIndex) {
        if (root == null) {
            return null;
        }

        DeleteResult result;
        InternalNode parentNode = curNode.getParent();
        boolean found = false;

        if (curNode instanceof InternalNode) {
            InternalNode node = (InternalNode) curNode;

            // Find pointer to node possibly containing key to delete
            int pointerIndex = findIndexOfNode(node, deleteKey);

            // Recursively delete
            result = deleteInternal(node.getPointers()[pointerIndex], deleteKey, oldChildIndex);

            // Retrieve index of deleted child node, null if no deletion
            oldChildIndex = result.getOldChildIndex();

            // Set current node since parent of traversed nodes in the lower level may be different after recursion
            node = result.getParentNode();
            found = result.isFound();

            if (found && oldChildIndex != null) {
                // Delete key and pointer to deleted child node
                node.deleteKey(oldChildIndex-1);
                node.deletePointer(oldChildIndex);
                parentNode = node.getParent();

                if (node.getDegree() >= minDegreeInternal || node == root) {
                    oldChildIndex = null;
                } else {
                    // Find index of pointer to current node in parent
                    int curNodeIndex = findIndexOfPointer(parentNode, node);

                    // Get left and right siblings of current node
                    InternalNode rightSibling = null, leftSibling = null;
                    if (curNodeIndex > 0) {
                        leftSibling = (InternalNode) parentNode.getPointers()[curNodeIndex - 1];
                    }
                    if (curNodeIndex < maxDegreeInternal - 1) {
                        rightSibling = (InternalNode) parentNode.getPointers()[curNodeIndex + 1];
                    }

                    if (rightSibling != null && rightSibling.getDegree() > minDegreeInternal) {
                        // If right sibling has extra entries, borrow from right sibling
                        // Move first key and pointer of right sibling to current node
                        moveEntryToLeftInternalNode(node, rightSibling);

                        // Swap moved key value with parent key value
                        Key temp = node.getKeys()[node.getDegree()-2];
                        node.getKeys()[node.getDegree()-2] = parentNode.getKeys()[curNodeIndex];
                        parentNode.getKeys()[curNodeIndex] = temp;
                        oldChildIndex = null;
                    } else if (leftSibling != null && leftSibling.getDegree() > minDegreeInternal) {
                        // If left sibling has extra entries, borrow from left sibling
                        // Move last key and pointer of left sibling to current node
                        moveEntryToRightInternalNode(leftSibling, node);

                        // Swap moved key value with parent key value
                        Key temp = node.getKeys()[0];
                        node.getKeys()[0] = parentNode.getKeys()[curNodeIndex - 1];
                        parentNode.getKeys()[curNodeIndex - 1] = temp;
                        oldChildIndex = null;
                    } else if (rightSibling != null) {
                        // If right sibling does not have extra entries, merge with right sibling
                        // Set right sibling node to be removed
                        oldChildIndex = curNodeIndex + 1;

                        // Pull down key from parent to current node
                        node.addKey(parentNode.getKeys()[curNodeIndex], node.getDegree() - 1);

                        // Merge right sibling node to current node
                        merge(rightSibling, node);
                    } else if (leftSibling != null) {
                        // If left sibling does not have extra entries, merge with left sibling
                        // Set current node to be removed
                        oldChildIndex = curNodeIndex;

                        // Pull down key from parent to left node
                        leftSibling.addKey(parentNode.getKeys()[curNodeIndex - 1], leftSibling.getDegree() - 1);

                        // Merge current node to left sibling node
                        merge(node, leftSibling);
                    }
                }
            }

            // If current node is root, and it only has 1 child, make child node the new root
            if (root == node && root.getDegree() == 1) {
                InternalNode temp = (InternalNode) root;
                root = ((InternalNode) root).getPointers()[0];
                root.setParent(null);
                temp.deleteAll();

                // Decrease height of tree and number of nodes, increase total number of deleted nodes
                --height;
                --totalNodes;
                ++totalNodesDeleted;
            }

        } else if (curNode instanceof LeafNode) {
            LeafNode node = (LeafNode) curNode;

            // Traverse leaf nodes to search for key to delete, since it is possible that the current node
            // does not contain the key
            KeyValuePair deletedEntry = node.delete(deleteKey);
            while (deletedEntry == null && node != null && node.getDegree() > 0
                    && deleteKey >= node.getKvPairs()[node.getDegree()-1].getKey().getK1()) {
                node = node.getRightSibling();
                if (node != null) deletedEntry = node.delete(deleteKey);
            }

            // Delete entry in storage
            if (deletedEntry != null) {
                found = true;
                st.deleteRecord(deletedEntry.getRecordAddress());
            }

            if (found && node.getDegree() < minKeysLeaf && node != root) {
                // Set parent node since current node may have a different parent after traversing leaf nodes
                parentNode = node.getParent();

                // Find index of pointer to current node in its parent
                int curNodeIndex = findIndexOfPointer(parentNode, node);

                // Get left and right siblings of current node, if any
                LeafNode rightSibling = null, leftSibling = null;
                if (curNodeIndex < parentNode.getDegree() - 1) {
                    rightSibling = node.getRightSibling();
                }
                if (curNodeIndex > 0) {
                    leftSibling = node.getLeftSibling();
                }

                if (rightSibling != null && rightSibling.getDegree() > minKeysLeaf) {
                    // Borrow from right sibling
                    KeyValuePair entry = rightSibling.deleteByIndex(0);
                    node.addSorted(entry);

                    // Set leftmost key of right sibling to be key value of parent
                    parentNode.getKeys()[curNodeIndex] = rightSibling.getKvPairs()[0].getKey();
                } else if (leftSibling != null && leftSibling.getDegree() > minKeysLeaf) {
                    // Borrow from left sibling
                    KeyValuePair entry = leftSibling.deleteByIndex(leftSibling.getDegree() - 1);
                    node.addSorted(entry);

                    // Set rightmost key of current node to be key value of parent
                    parentNode.getKeys()[curNodeIndex-1] = node.getKvPairs()[0].getKey();
                } else if (rightSibling != null) {
                    // Merge with right sibling
                    oldChildIndex = curNodeIndex+1;
                    merge(rightSibling, node);

                    // Modify siblings of nodes
                    node.setRightSibling(rightSibling.getRightSibling());
                    if (node.getRightSibling() != null) node.getRightSibling().setLeftSibling(node);
                } else if (leftSibling != null) {
                    // Merge with left sibling
                    oldChildIndex = curNodeIndex;
                    merge(node, leftSibling);

                    // Modify siblings of nodes
                    leftSibling.setRightSibling(node.getRightSibling());
                    if (leftSibling.getRightSibling() != null) leftSibling.getRightSibling().setLeftSibling(leftSibling);
                }
            }

            if (node == root && root.getDegree() == 0) {
                root = null;
            }
        }

        // Return object consisting of index of deleted child (if any), parent of current node,
        // and a boolean indicating whether an entry is deleted
        return new DeleteResult(oldChildIndex, parentNode, found);
    }

    /**
     * Merge 2 internal nodes by appending keys and pointers of source node to destination node
     * @param src source internal node
     * @param dst destination internal node, used to store the merged node
     */
    public void merge(InternalNode src, InternalNode dst) {
        // Change parent of source node's child nodes to destination node
        for (int i = 0; i < src.getDegree(); ++i) {
            src.getPointers()[i].setParent(dst);
        }

        // Copy all keys and pointers of the source node to the back of the destination node
        System.arraycopy(src.getKeys(), 0, dst.getKeys(), dst.getDegree(), src.getDegree() - 1);
        System.arraycopy(src.getPointers(), 0, dst.getPointers(), dst.getDegree(), src.getDegree());
        dst.setDegree(src.getDegree() + dst.getDegree());

        // Delete source node
        src.setParent(null);
        src.deleteAll();

        // Increase number of nodes deleted
        ++totalNodesDeleted;
        --totalNodes;
    }

    /**
     * Merge 2 leaf nodes by appending key-value pairs of source node to destination node
     * @param src source leaf node
     * @param dst destination leaf node, used to store the merged node
     */
    public void merge(LeafNode src, LeafNode dst) {
        // Copy all key-value pairs of the source node to the back of the destination node
        System.arraycopy(src.getKvPairs(), 0, dst.getKvPairs(), dst.getDegree(), src.getDegree());
        dst.setDegree(dst.getDegree() + src.getDegree());

        // Delete source node
        src.deleteAll();

        // Increase number of nodes deleted
        ++totalNodesDeleted;
        --totalNodes;
    }

    /**
     * Move the leftmost key and pointer of right internal node to left internal node
     * @param left left internal node (destination)
     * @param right right internal node (source)
     */
    public void moveEntryToLeftInternalNode(InternalNode left, InternalNode right) {
        // Delete the first key and pointer of the node on the right
        Key key = right.deleteKey(0);
        Node pointer = right.deletePointer(0);

        // Add the key and pointer to the back of the node on the left
        left.addKey(key, left.getDegree() - 1);
        left.addPointer(pointer, left.getDegree());
        pointer.setParent(left);
    }

    /**
     * Move the rightmost key and pointer of left internal node to right internal node
     * @param left left internal node (source)
     * @param right right internal node (destination)
     */
    public void moveEntryToRightInternalNode(InternalNode left, InternalNode right) {
        // Delete the last key and pointer of the node on the left
        Key key = left.deleteKey(left.getDegree() - 2);
        Node pointer = left.deletePointer(left.getDegree() - 1);

        // Add the key and pointer to the start of the node on the right
        right.addKey(key, 0);
        right.addPointer(pointer, 0);
        pointer.setParent(right);
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
        node.setDegree(firstHalfPointers.length);

        // Create a new node to store the split keys and pointers
        InternalNode newNode = new InternalNode(secondHalfPointers.length, Arrays.copyOf(secondHalfKeys, keys.length),
                Arrays.copyOf(secondHalfPointers, pointers.length));

        // Set the new node as parent of moved nodes
        for (int i = 0; i < newNode.getDegree(); ++i) {
            newNode.getPointers()[i].setParent(newNode);
        }

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

        // Create a temporary array to store the existing and to be added entry
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
        node.setDegree(firstHalf.length);

        // Create a new node to store the split key-value pairs
        LeafNode newLeaf = new LeafNode(secondHalf.length, Arrays.copyOf(secondHalf, kvPairs.length));

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

    public int getN() {
        return n;
    }

    public Node getRoot() {
        return root;
    }

    public int getHeight() {
        return height;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getTotalNodesDeleted() {
        return totalNodesDeleted;
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
                for (int i = 0; i < cur.getDegree(); ++i) {
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
