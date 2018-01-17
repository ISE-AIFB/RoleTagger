package ise.roletagger.model;

import ise.roletagger.util.URLUTF8Encoder;

public class HtmlLink{

	private String url;
	private String anchorText;
	private String fullSentence;
	private int start;
	private int end;
	
	public HtmlLink(){};

	public void setFullSentence(String sentenceWithoutHtmlTag) {
		fullSentence = sentenceWithoutHtmlTag;
	}

	@Override
	public String toString() {
		return "HtmlLink [url=" + url + ", anchorText=" + anchorText + ", fullSentence=" + fullSentence
				+ ", start=" + start + ", end=" + end + "]";
	}

	public String getDecodedUrl() {
		return URLUTF8Encoder.decodeJavaNative(url);
	}

	public String getUrl() {
		return url;
	}
	
	public String getFullSentence(){
		return fullSentence;
	}

	public void setUrl(String link) {
		this.url = replaceInvalidChar(link);
	}

	public String getAnchorText() {
		return new String(anchorText);
	}

	public void setAnchorText(String linkText) {
		this.anchorText = linkText;
	}

	private String replaceInvalidChar(String link){
		link = link.replaceAll("'", "");
		link = link.replaceAll("\"", "");
		return link;
	}

	public void setPostion(int start2, int end2) {
		this.start = start2;
		this.end = end2;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}