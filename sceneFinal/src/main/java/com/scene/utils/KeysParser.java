package com.scene.utils;

class KeysParser {
	private KeyCreater creater; 
	private String key;
	public KeysParser(String key) {
		this.creater = new KeyCreater();
		this.key = key;
	}
	char key(int index) {
		return (char) (this.creater.getChar(index) + 1);
	}
	
	int dealIndex(int index) {
		index *= 5;
		return index;
	}
}
