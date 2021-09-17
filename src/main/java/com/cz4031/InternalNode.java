package com.cz4031;

public class InternalNode extends Node{

	int maxDegree; //allowed maximum of pointers
	int minDegree; //required minimum of pointers
	int degree; //current number of pointers
	InternalNode leftSibling;
	InternalNode rightSibling;
	Integer[] keys;
	Node[] pointers;
	
	private InternalNode(int n, Integer[] keys) {
		this.maxDegree = n;
		this.minDegree = (int)Math.floor(n/2.0);
		this.degree = 0;
		this.keys = keys;
		this.pointers = new Node[this.maxDegree+1];
	}

	private InternalNode(int m, Integer[] keys, Node[] pointers) {
		this.maxDegree = m;
		this.minDegree = (int)Math.ceil(m/2.0);
		this.degree = getDegree(pointers);
		this.keys = keys;
		this.pointers = pointers;
	}
	
	private int getDegree(Node[] pointers) {
		for (int i = 0; i <  pointers.length; i++) {
			if (pointers[i] == null) { return i; }
		}
		return -1;
	}
	
	//Remove key at particular index
	private void removeKey(int index) { 
		this.keys[index] = null; 
	}
	
	//Remove pointer at particular index
	private void removePointer(int index) {
		this.pointers[index] = null;
		this.degree--;
	}
	
	//Remove specified pointer from node
	private void removePointer(Node pointer) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == pointer) { this.pointers[i] = null; }
		}
		this.degree--;
	}
	
	//Add pointer at the end.
	private void addPointer(Node pointer) {
		this.pointers[degree] = pointer;
		this.degree++;
	}
	
	//Add pointer at particular index.
	private void addPointer(Node pointer, int index) {
		for (int i = degree - 1; i >= index ;i--) {
			pointers[i + 1] = pointers[i];
		}
		this.pointers[index] = pointer;
		this.degree++;
	}
	
	//Add pointer at the beginning of node.
	private void insertPointerAtFront(Node pointer) {
		for (int i = degree - 1; i >= 0 ;i--) {
			pointers[i + 1] = pointers[i];
		}
		this.pointers[0] = pointer;
		this.degree++;
	}

	//Get index of particular pointer.
	private int getPointerIndex(Node pointer) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == pointer) { return i; }
		}
		return -1;
	}
	
	// Check whether current degree of node is below the required minimum.
	private boolean isLacking() {
		return this.degree < this.minDegree;
	}
	
	// Check whether node is able to loan to its siblings.
	private boolean isLendable() { 
		return this.degree > this.minDegree; 
	}
	
	// Check whether node is able to merge with another node.
	private boolean isMergeable() { 
		return this.degree == this.minDegree; 
	}

	// Check whether node is overflowed.
	private boolean isOverflowed() {
		return this.degree == maxDegree + 1;
	}
	


}
