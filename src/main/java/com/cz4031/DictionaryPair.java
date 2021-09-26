package com.cz4031;

public class DictionaryPair implements Comparable<DictionaryPair> {

	private MultiKey key;
	private RecordAddress value;
	
	public DictionaryPair(MultiKey key, RecordAddress value) {
		this.key = key;
		this.value = value;
	}
	
	public MultiKey getKey() {
		return key;
	}
	
	public RecordAddress getValue() {
		return value;
	}

	@Override
	public int compareTo(DictionaryPair o) {
		if (o == null) return -1;
		return key.compareTo(o.getKey());
	}
}
