package ise.roletagger.model;

import java.util.ArrayList;
import java.util.List;

public class TagPositions {

	private final List<TagPosition> positions = new ArrayList<>();

	public void add(final TagPosition tp) {
		if (!alreadyExist(tp)) {
			positions.add(tp);
		}
	}

	public List<TagPosition> getPositions() {
		return positions;
	}

	public boolean alreadyExist(TagPosition tp) {
		for (TagPosition tagPosition : positions) {
			if (tagPosition.getStartIndex() <= tp.getStartIndex() && tagPosition.getEndIndex() >= tp.getEndIndex()) {
				return true;
			}else if(tp.getStartIndex()>=tagPosition.getStartIndex() && tp.getStartIndex()<=tagPosition.getEndIndex() ){
				return true;
			}else if(tp.getEndIndex()>=tagPosition.getStartIndex() && tp.getEndIndex()<=tagPosition.getEndIndex() ){
				return true;
			}
		}
		return false;
	}

	public void reset() {
		positions.clear();
	}

	@Override
	public String toString() {
		return "TagPostions [positions=" + positions + "]";
	}
}
