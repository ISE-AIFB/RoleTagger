package ise.roletagger.model;

public enum DataSourceType {
	WIKIPEDIA_LIST_OF_PERSON_MANUAL("wikipedialistofperson"),
	WIKIDATA_LABEL("wikidatalabels"),
	WIKIDATA_LIST_OF_PRESON("wikidatalistofperson"),
	WIKIPEDIA_LIST_OF_TILTES("wikipediatitles"),
	ALL("all");
	
	String text;
	
	private DataSourceType(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
