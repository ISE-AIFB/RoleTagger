package ise.roletagger.model;

public class Position {
	private int startIndex;
	private int endIndex;
	
	public Position(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int end) {
		this.endIndex =end;
	}
	
	public void setStartIndex(int start) {
		this.startIndex =start;
	}
	
	public boolean hasOverlap(Position otherPosition) {
		if (this.getStartIndex() <= otherPosition.getStartIndex()
				&& this.getEndIndex() >= otherPosition.getEndIndex()) {
			return true;
		} else if (otherPosition.getStartIndex() >= this.getStartIndex()
				&& otherPosition.getStartIndex() <= this.getEndIndex()) {
			return true;
		} else if (otherPosition.getEndIndex() >= this.getStartIndex()
				&& otherPosition.getEndIndex() <= this.getEndIndex()) {
			return true;
		} else if (this.getStartIndex() >= otherPosition.getStartIndex()
				&& this.getStartIndex() <= otherPosition.getEndIndex()) {
			return true;
		} else if (this.getEndIndex() >= otherPosition.getStartIndex()
				&& this.getEndIndex() <= otherPosition.getEndIndex()) {
			return true;
		}
		return false;
	}
	
	public boolean contains(Position othrePosition) {
		if(this.getStartIndex()<=othrePosition.getStartIndex() && this.getEndIndex()>=othrePosition.getEndIndex()){
			return true;
		}else{
			return false;
		}
	}

	public int getLength() {
		return getEndIndex()-getStartIndex();
	}
	
	@Override
	public String toString() {
		return "Position [startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
	}
	
}
