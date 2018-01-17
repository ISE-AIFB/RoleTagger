package ise.roletagger.model;

public class TagPosition extends Position {
	private final String tag;

	public TagPosition(String tag, int startIndex, int endIndex) {
		super(startIndex, endIndex);
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return "TagPostion [tag=" + tag + ", toString()=" + super.toString() + "]";
	}
}

