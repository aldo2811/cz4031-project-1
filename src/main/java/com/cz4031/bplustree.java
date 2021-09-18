package com.cz4031;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class bplustree {

	private int n;
	private InternalNode root;
	private LeafNode firstLeaf;
	
	public bplustree(int n) {
		this.n = n;
		this.root = null;
	}
	
	public int getMidpoint() {
		return (int)Math.floor(this.n / 2);
	}
	
	public void delete(int key) {
		if (isEmpty()) {
			
			System.err.println("Invalid Delete: The B+ tree is currently empty.");

		} else {

			// Get leaf node and attempt to find index of key to delete
			LeafNode ln = (this.root == null) ? this.firstLeaf : getLeafNode(key);
			int targetIndex = getTargetIndex(ln.getDictionary(), ln.getDegree(), key);


			if (targetIndex < 0) {

				System.err.println("Invalid Delete: Key unable to be found.");

			} else {

				// Successfully delete the dictionary pair
				ln.removeRecord(targetIndex);

				// Check for deficiencies
				if (ln.isLacking()) {

					LeafNode sibling;
					InternalNode parent = ln.parent;

					// Borrow: First, check the left sibling, then the right sibling
					if (ln.leftSibling != null &&
						ln.leftSibling.parent == ln.parent &&
						ln.leftSibling.isLendable()) {

						sibling = ln.leftSibling;
						DictionaryPair borrowedDP = sibling.getDictionary()[sibling.getDegree() - 1];

						/* Insert borrowed dictionary pair, sort dictionary,
						   and delete dictionary pair from sibling */
						ln.addRecord(borrowedDP);
						sortDictionary(ln.getDictionary());
						sibling.removeRecord(sibling.getDegree() - 1);

						// Update key in parent if necessary
						int pointerIndex = findIndexOfPointer(parent.getPointers(), ln);
						if (!(borrowedDP.getKey() >= parent.getKeys()[pointerIndex - 1])) {
							parent.getKeys()[pointerIndex - 1] = ln.getDictionary()[0].getKey();
						}

					} 
					else if (ln.rightSibling != null &&
							   ln.rightSibling.parent == ln.parent &&
							   ln.rightSibling.isLendable()) {

						sibling = ln.rightSibling;
						DictionaryPair borrowedDP = sibling.getDictionary()[0];

						/* Insert borrowed dictionary pair, sort dictionary,
					       and delete dictionary pair from sibling */
						ln.addRecord(borrowedDP);
						sibling.removeRecord(0);
						sortDictionary(sibling.getDictionary());

						// Update key in parent if necessary
						int pointerIndex = findIndexOfPointer(parent.getPointers(), ln);
						if (!(borrowedDP.getKey() < parent.getKeys()[pointerIndex])) {
							parent.getKeys()[pointerIndex] = sibling.getDictionary()[0].getKey();
						}

					}

					// Merge: First, check the left sibling, then the right sibling
					else if (ln.leftSibling != null &&
							 ln.leftSibling.parent == ln.parent &&
							 ln.leftSibling.isMergeable()) {

						sibling = ln.leftSibling;
						int pointerIndex = findIndexOfPointer(parent.getPointers(), ln);

						// Remove key and child pointer from parent
						parent.removeKey(pointerIndex - 1);
						parent.removePointer(ln);

						// Update sibling pointer
						sibling.rightSibling = ln.rightSibling;

						// Check for deficiencies in parent
						if (parent.isLacking()) {
							manageLacking(parent);
						}

					} 
					else if (ln.rightSibling != null &&
							   ln.rightSibling.parent == ln.parent &&
							   ln.rightSibling.isMergeable()) {

						sibling = ln.rightSibling;
						int pointerIndex = findIndexOfPointer(parent.getPointers(), ln);

						// Remove key and child pointer from parent
						parent.removeKey(pointerIndex);
						parent.removePointer(pointerIndex);

						// Update sibling pointer
						sibling.leftSibling = ln.leftSibling;
						if (sibling.leftSibling == null) {
							firstLeaf = sibling;
						}

						if (parent.isLacking()) {
							manageLacking(parent);
						}
					}

				} else if (this.root == null && this.firstLeaf.getDegree() == 0) {

					/* Flow of execution goes here when the deleted dictionary
					   pair was the only pair within the tree */

					// Set first leaf as null to indicate B+ tree is empty
					this.firstLeaf = null;

				} else {

					/* The dictionary of the LeafNode object may need to be
					   sorted after a successful delete */
					sortDictionary(ln.getDictionary());

				}
			}
		}
	}
	
	public void insert(int key, RecordAddress value){
		if (isEmpty()) {

			// Create leaf node 
			LeafNode ln = new LeafNode(this.n, new DictionaryPair(key, value));

			// Set as first leaf node
			this.firstLeaf = ln;

		} else {

			// Find leaf node to insert into
			LeafNode ln = (this.root == null) ? this.firstLeaf : getLeafNode(key);

			// Insert into leaf node fails if node becomes overflowed
			if (!ln.addRecord(new DictionaryPair(key, value))) {

				// Sort all the dictionary pairs with the included pair to be inserted
				ln.getDictionary()[ln.getDegree()] = new DictionaryPair(key, value);
				ln.setDegree(ln.getDegree() + 1);
				sortDictionary(ln.getDictionary());

				// Split the sorted pairs into two halves
				int midpoint = getMidpoint();
				DictionaryPair[] halfDict = splitDictionary(ln, midpoint);
				
				//check if 1 node in tree
				if (ln.parent == null) {

					// Create internal node to serve as parent, use dictionary midpoint key
					Integer[] parent_keys = new Integer[this.n];
					parent_keys[0] = halfDict[0].getKey();
					InternalNode parent = new InternalNode(this.n, parent_keys);
					ln.parent = parent;
					parent.addPointer(ln);

				} 
				else {

					// Add new key to parent for proper indexing
					int newParentKey = halfDict[0].getKey();
					ln.parent.getKeys()[ln.parent.getDegree() - 1] = newParentKey;
					Arrays.sort(ln.parent.getKeys(), 0, ln.parent.getDegree());
				}

				// Create new LeafNode that holds the other half
				LeafNode newLeafNode = new LeafNode(this.n, halfDict, ln.parent);

				// Update child pointers of parent node
				int pointerIndex = ln.parent.getPointerIndex(ln) + 1;
				ln.parent.addPointer(newLeafNode, pointerIndex);

				// Make leaf nodes siblings of one another
				newLeafNode.rightSibling = ln.rightSibling;
				if (newLeafNode.rightSibling != null) {
					newLeafNode.rightSibling.leftSibling = newLeafNode;
				}
				ln.rightSibling = newLeafNode;
				newLeafNode.leftSibling = ln;

				if (this.root == null) {

					// Set the root of B+ tree to be the parent
					this.root = ln.parent;

				} else {

					/* If parent is overflowed, repeat the process up the tree,
			   		   until no deficiencies are found */
					InternalNode in = ln.parent;
					while (in != null) {
						if (in.isOverflowed()) {
							splitInternalNode(in);
						} else {
							break;
						}
						in = in.parent;
					}
				}
			}
		}
	}
	
	public RecordAddress search(int key) {

		// If B+ tree is completely empty, simply return null
		if (isEmpty()) { return null; }

		// Find leaf node that holds the dictionary key
		LeafNode ln = (this.root == null) ? this.firstLeaf : getLeafNode(key);

		// Perform binary search to find index of key within dictionary
		DictionaryPair[] dp = ln.getDictionary();
		int index = getTargetIndex(dp, ln.getDegree(), key);

		// If index negative, the key doesn't exist in B+ tree
		if (index < 0) {
			return null;
		} else {
			return dp[index].getValue();
		}
	}
	
	public ArrayList<RecordAddress> search(int lowerBound, int upperBound) {

		// Instantiate Double array to hold values
		ArrayList<RecordAddress> values = new ArrayList<RecordAddress>();

		// Iterate through the doubly linked list of leaves
		LeafNode currNode = this.firstLeaf;
		while (currNode != null) {

			// Iterate through the dictionary of each node
			DictionaryPair dps[] = currNode.getDictionary();
			for (DictionaryPair dp : dps) {

				/* Stop searching the dictionary once a null value is encountered
				   as this the indicates the end of non-null values */
				if (dp == null) { break; }

				// Include value if its key fits within the provided range
				if (lowerBound <= dp.getKey() && dp.getKey() <= upperBound) {
					values.add(dp.getValue());
				}
			}

			/* Update the current node to be the right sibling,
			   leaf traversal is from left to right */
			currNode = currNode.rightSibling;

		}

		return values;
	}
	
	//check if b+tree is empty
	private boolean isEmpty() {
		return firstLeaf == null;
	}
	
	public void shiftDown(Node[] pointers, int count) {
		Node[] newPointers = new Node[this.n + 1];
		for (int i = count; i < pointers.length; i++) {
			newPointers[i - count] = pointers[i];
		}
		pointers = newPointers;
	}
	
	private void sortDictionary(DictionaryPair[] dictionary) {
		Arrays.sort(dictionary, new Comparator<DictionaryPair>() {
			@Override
			public int compare(DictionaryPair dp1, DictionaryPair dp2) {
				if (dp1 == null && dp2 == null) { return 0; }
				if (dp1 == null) { return 1; }
				if (dp2 == null) { return -1; }
				return dp1.compareTo(dp2);
			}
		});
	}
	
	//split keys at specified index
	private Integer[] splitKeys(Integer[] keys, int index) {

		Integer[] halfKeys = new Integer[this.n];

		keys[index] = null;

		for (int i = index + 1; i < keys.length; i++) {
			halfKeys[i - index - 1] = keys[i];
			keys[i] = null;
		}

		return halfKeys;
	}
	
	//split pointers at specified index
	private Node[] splitPointers(InternalNode in, int index) {

		Node[] pointers = in.getPointers();
		Node[] halfPointers = new Node[this.n + 1];
		
		for (int i = index + 1; i < pointers.length; i++) {
			halfPointers[i - index - 1] = pointers[i];
			in.removePointer(i);
		}

		return halfPointers;
	}
	
	//split dictionary at specified index
	private DictionaryPair[] splitDictionary(LeafNode ln, int index) {

		DictionaryPair[] dictionary = ln.getDictionary();

		DictionaryPair[] halfDict = new DictionaryPair[this.n];

		for (int i = index; i < dictionary.length; i++) {
			halfDict[i - index] = dictionary[i];
			ln.removeRecord(i);
		}

		return halfDict;
	}
	
	//function called when an overflowed internal node has to be split
	private void splitInternalNode(InternalNode in) {

		InternalNode parent = in.parent;

		// Split keys and pointers in half
		int midpoint = getMidpoint();
		int newParentKey = in.getKeys()[midpoint];
		Integer[] halfKeys = splitKeys(in.getKeys(), midpoint);
		Node[] halfPointers = splitPointers(in, midpoint);

		// Change degree of original InternalNode in
		in.setDegree(in.getDegree(in.getPointers()));

		// Create new sibling internal node and add half of keys and pointers
		InternalNode sibling = new InternalNode(this.n, halfKeys, halfPointers);
		for (Node pointer : halfPointers) {
			if (pointer != null) { pointer.parent = sibling; }
		}

		// Make internal nodes siblings of one another
		sibling.rightSibling = in.rightSibling;
		if (sibling.rightSibling != null) {
			sibling.rightSibling.leftSibling = sibling;
		}
		in.rightSibling = sibling;
		sibling.leftSibling = in;

		if (parent == null) {

			// Create new parent node and add midpoint key and pointers
			Integer[] keys = new Integer[this.n];
			keys[0] = newParentKey;
			InternalNode newRoot = new InternalNode(this.n, keys);
			newRoot.addPointer(in);
			newRoot.addPointer(sibling);
			this.root = newRoot;

			// Add pointers from children to parent
			in.parent = newRoot;
			sibling.parent = newRoot;

		} else {

			// Add key to parent
			parent.getKeys()[parent.getDegree() - 1] = newParentKey;
			Arrays.sort(parent.getKeys(), 0, parent.getDegree());

			// Set up pointer to new sibling
			int pointerIndex = parent.getPointerIndex(in) + 1;
			parent.addPointer(sibling, pointerIndex);
			sibling.parent = parent;
		}
	}
	
	//locate index of given target key using binary search
	public int getTargetIndex(DictionaryPair[] dp, int degree, int key) {
		Comparator<DictionaryPair> c = new Comparator<DictionaryPair>() {
			@Override
			public int compare(DictionaryPair dp1, DictionaryPair dp2) {
				Integer a = Integer.valueOf(dp1.getKey());
				Integer b = Integer.valueOf(dp2.getKey());
				return a.compareTo(b);
			}
		};
		
		return Arrays.binarySearch(dp, 0, degree, new DictionaryPair(key, new RecordAddress(1,1)), c);
	}
	
	//locate leafnode using a given key; start from root node
	public LeafNode getLeafNode(int key) {

		Integer[] keys = this.root.getKeys();
		int i;

		for (i = 0; i < this.root.getDegree() - 1; i++) {
			if (key < keys[i])
				break;
		}

		//return node if it is a LeafNode object; else continue 1 level down
		Node child = this.root.getPointers()[i];
		if (child instanceof LeafNode) {
			return (LeafNode)child;
		} 
		else {
			return getLeafNode((InternalNode)child, key);
		}
	}
	
	//locate leafnode using a given key; continue from internal nodes
	public LeafNode getLeafNode(InternalNode node, int key) {

		Integer[] keys = node.getKeys();
		int i;

		for (i = 0; i < node.getDegree() - 1; i++) {
			if (key < keys[i])
				break;
		}

		//return node if it is a LeafNode object; else continue 1 level down
		Node child = node.getPointers()[i];
		if (child instanceof LeafNode) {
			return (LeafNode)child;
		} 
		else {
			return getLeafNode((InternalNode)child, key);
		}
	}
	
	//get index of pointer which points to specified leafnode
	public int getLeafNodeIndex(Node[] pointers, LeafNode node) {
		int i;
		for (i = 0; i < pointers.length; i++) {
			if (pointers[i] == node) { break; }
		}
		return i;
	}
	
	//function called if internal node is lacking keys
	public void manageLacking(InternalNode in) {
	
		InternalNode sibling;
		InternalNode parent = in.parent;
	
		if (this.root == in && this.root.getDegree() < 1) {
			for (int i = 0; i < in.getPointers().length; i++) {
				if (in.getPointers()[i] != null) {
					if (in.getPointers()[i] instanceof InternalNode) {
						this.root = (InternalNode)in.getPointers()[i];
						this.root.parent = null;
					} 
					else if (in.getPointers()[i] instanceof LeafNode) {
						this.root = null;
					}
				}
			}
		}
		//borrow from left sibling
		else if (in.leftSibling != null && in.leftSibling.isLendable()) {
			sibling = in.leftSibling;
		}
		//borrow from right sibling
		else if (in.rightSibling != null && in.rightSibling.isLendable()) {
			sibling = in.rightSibling;
		
			int borrowedKey = sibling.getKeys()[0];
			Node pointer = sibling.getPointers()[0];
			
			//update new key and pointer into internal node
			in.getKeys()[in.getDegree() - 1] = parent.getKeys()[0];
			in.getPointers()[in.getDegree()] = pointer;
			
			//update parent key
			parent.getKeys()[0] = borrowedKey;
			
			//delete key and pointer from sibling
			sibling.removeKey(0);
			Arrays.sort(sibling.getKeys());
			sibling.removePointer(0);
			shiftDown(in.getPointers(), 1);
		}
		//merge with left sibling
		else if (in.leftSibling != null && in.leftSibling.isMergeable()) {
			
		} 
		//merge with right sibling
		else if (in.rightSibling != null && in.rightSibling.isMergeable()) {
			sibling = in.rightSibling;

			sibling.getKeys()[sibling.getDegree() - 1] = parent.getKeys()[parent.getDegree() - 2];
			Arrays.sort(sibling.getKeys(), 0, sibling.getDegree());
			parent.getKeys()[parent.getDegree() - 2] = null;

			// Copy in's child pointer over to sibling's list of child pointers
			for (int i = 0; i < in.getPointers().length; i++) {
				if (in.getPointers()[i] != null) {
					sibling.insertPointerAtFront(in.getPointers()[i]);
					in.getPointers()[i].parent = sibling;
					in.removePointer(i);
				}
			}

			// Delete pointer to in
			parent.removePointer(in);

			// Update left sibling
			sibling.leftSibling = in.leftSibling;
		}
		
		// Handle deficiency a level up if it exists
		if (parent != null && parent.isLacking()) {
			manageLacking(parent);
		}
	}
	
	private int findIndexOfPointer(Node[] pointers, LeafNode node) {
		int i;
		for (i = 0; i < pointers.length; i++) {
			if (pointers[i] == node) { break; }
		}
		return i;
	}
	
}
