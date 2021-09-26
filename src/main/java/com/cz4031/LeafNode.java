package com.cz4031;

import java.util.Arrays;
import java.util.Dictionary;

public class LeafNode extends Node{

	private int maxDegree;
	private int minDegree;
	private int degree;
	protected LeafNode leftSibling;
	protected LeafNode rightSibling;
	private DictionaryPair[] dictionary;
	
	public LeafNode(int n, DictionaryPair dp) {
		this.maxDegree = n;
		this.minDegree = (int)(Math.floor((n+1)/2.0));
		this.degree = 0;
		this.dictionary = new DictionaryPair[n+1];
		this.addRecord(dp);
	}
	
	public LeafNode(int n, DictionaryPair[] dp, InternalNode parent) {
		this.maxDegree = n;
		this.minDegree = (int)(Math.floor((n+1)/2.0));
		this.dictionary = dp;
		this.degree = getDegree(dp);
		this.parent = parent;
	}
	
	public DictionaryPair[] getDictionary() {
		return dictionary;
	}
	
	public int getDegree() {
		return degree;
	}
	
	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	//get degree given a sorted array of dictionary pairs
	public int getDegree(DictionaryPair[] dp) {
		for (int i = 0; i <  dp.length; i++) {
			if (dp[i] == null) { return i; }
		}
		return -1;
	}
	
	//insert record into leafnode
	public boolean addRecord(DictionaryPair dp) {
		if (isFull()) {
			return false;
		} else {
			
			this.dictionary[degree] = dp;
			degree++;
			Arrays.sort(this.dictionary, 0, degree);

			return true;
		}
	}
	
	//delete record from leafnode
	public void removeRecord(int index) {

		// Delete dictionary pair from leaf
		this.dictionary[index] = null;

		// Decrement numPairs
		degree--;
	}
	
	//check if degree is below the required minimum
	public boolean isLacking() { 
		return degree < minDegree; 
	}
	
	//check if leafnode is full
	public boolean isFull() { 
		return degree == maxDegree;
	}
	
	//check if leafnode is able to loan a record to its siblings
	public boolean isLendable() { 
		return degree > minDegree; 
	}
	
	//check if leafnode is able to merge with another leafnode
	public boolean isMergeable() {
		return degree == minDegree;
	}
}
