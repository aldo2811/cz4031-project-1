package com.cz4031;

public class InternalNode extends Node{

	private int maxDegree; //allowed maximum of pointers
	private int minDegree; //required minimum of pointers
	private int degree; //current number of pointers
	protected InternalNode leftSibling;
	protected InternalNode rightSibling;
	private MultiKey[] keys;
	private Node[] pointers;
	
	public InternalNode(int n, MultiKey[] keys) {
		this.maxDegree = n+1;
		this.minDegree = (int)Math.floor(n/2.0);
		this.degree = 0;
		this.keys = keys;
		this.pointers = new Node[this.maxDegree+1];
	}

	public InternalNode(int n, MultiKey[] keys, Node[] pointers) {
		this.maxDegree = n+1;
		this.minDegree = (int)Math.floor(n/2.0);
		this.degree = getDegree(pointers);
		this.keys = keys;
		this.pointers = pointers;
	}
	
	public MultiKey[] getKeys() {
		return keys;
	}
	
	public Node[] getPointers() {
		return pointers;
	}
	
	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public int getDegree() {
		return degree;
	}
	
	//get degree given an array of pointers.
	public int getDegree(Node[] pointers) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == null) { return i; }
		}
		return -1;
	}
	
	//Remove key at particular index
	public void removeKey(int index) { 
		this.keys[index] = null; 
	}
	
	//Remove pointer at particular index
	public void removePointer(int index) {
		this.pointers[index] = null;
		this.degree--;
	}
	
	//Remove specified pointer from node
	public void removePointer(Node pointer) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == pointer) { this.pointers[i] = null; }
		}
		this.degree--;
	}
	
	//Add pointer at the end.
	public void addPointer(Node pointer) {
		this.pointers[degree] = pointer;
		this.degree++;
	}
	
	//Add pointer at particular index.
	public void addPointer(Node pointer, int index) {
		for (int i = degree - 1; i >= index ;i--) {
			pointers[i + 1] = pointers[i];
		}
		this.pointers[index] = pointer;
		this.degree++;
	}
	
	//Add pointer at the beginning of node.
	public void insertPointerAtFront(Node pointer) {
		for (int i = degree - 1; i >= 0 ;i--) {
			pointers[i + 1] = pointers[i];
		}
		this.pointers[0] = pointer;
		this.degree++;
	}

	//Get index of particular pointer.
	public int getPointerIndex(Node pointer) {
		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] == pointer) { return i; }
		}
		return -1;
	}
	
	// Check whether current degree of node is below the required minimum.
	public boolean isLacking() {
		return this.degree < this.minDegree;
	}
	
	// Check whether node is able to loan to its siblings.
	public boolean isLendable() { 
		return this.degree > this.minDegree; 
	}
	
	// Check whether node is able to merge with another node.
	public boolean isMergeable() { 
		return this.degree == this.minDegree; 
	}

	// Check whether node is overflowed.
	public boolean isOverflowed() {
		return this.degree == maxDegree + 1;
	}


}
