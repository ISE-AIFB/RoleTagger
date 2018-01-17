package ise.roletagger.model;

import java.util.ArrayList;
import java.util.List;

public class ListOfSubjectObject {
	private static final List<SubjectObject> LIST = new ArrayList<>();
	
	public static List<SubjectObject> getListOfSubjectObjects() {
		return LIST;
	}
}
