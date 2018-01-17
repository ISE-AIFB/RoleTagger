package ise.roletagger.model;

public class NounPhrase {
	private String text;
	private final Position position;

	public NounPhrase(String text, int start, int end) {
		this.text = text;
		this.position = new Position(start, end);
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "NounPhrase [text=" + text + ", position=" + position + "]";
	}

}
