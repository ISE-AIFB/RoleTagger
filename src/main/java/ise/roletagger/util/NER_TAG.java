package ise.roletagger.util;

public enum NER_TAG {
	PERSON("PERSON"), LOCATION("LOCATION"), ORGANIZATION("ORGANIZATION")
	,ROLE("ROLE"),NO_ROLE("NO_ROLE");
	//, ORDINAL("ORDINAL"),MISC("MISC") , MONEY("MONEY"), NUMBER("NUMBER"), DATE("DATE"), PERCENT("PERCENT"), TIME("TIME");

	public String text;

	NER_TAG(String text) {
		this.text = text;
	}

	public static NER_TAG resolve(String text) {
		for (NER_TAG tag : NER_TAG.values()) {
			if (tag.text.equals(text)) {
				return tag;
			}
		}
		return null;
	}
}
