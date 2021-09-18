package com.cz4031;

public class DictionaryPair implements Comparable<DictionaryPair> {

	private int key;
	private RecordAddress value;
	
	public DictionaryPair(int key, RecordAddress value) {
		this.key = key;
		this.value = value;
	}
	
	public int getKey() {
		return key;
	}
	
	public RecordAddress getValue() {
		return value;
	}
	
	public int compareTo(DictionaryPair o) {
		if (key == o.key) {
			return 0; 
		}
		else if (key > o.key) {
			return 1;
		}
		else { 
			return -1; 
		}
	}
}
