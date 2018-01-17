package ise.roletagger.entitygenerator;
public enum DataForomat {
	JSON("application/sparql-results+json"),
	XML("application/sparql-results+xml"),
	TSV("text/tab-separated-values"),
	CSV("text/csv");
	
	String text;
	
	private DataForomat(String text) {
		this.text = text;
	}
	
	String getText(){
		return text;
	}
}
