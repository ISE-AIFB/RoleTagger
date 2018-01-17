package ise.roletagger.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Token {
	private final Map<String,String> content = Collections.synchronizedMap(new HashMap<String, String>());

	public Map<String, String> getContent() {
		return content;
	}
	
	public void add(String key, String value) {
		content.put(key, value);
	}

	@Override
	public String toString() {
		return "Token [content=" + content + "]";
	}
}
