package ise.roletagger.categorytreegeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ise.roletagger.model.SubjectObject;


public class FastLookUpSubjectObject {
	/**
	 * mapping between super category to all its direct sub categories
	 */
	private static final Map<String,Set<String>> data = new HashMap<>();
	
	public FastLookUpSubjectObject(final List<SubjectObject> sos) {
		for(SubjectObject so:sos) {
			Set<String>  set = data.get(so.getObject());
			if(set == null) {
				set = new HashSet<String>();
				set.add(so.getSubject());
				data.put(so.getObject(), set);
			}else {
				set.add(so.getSubject());
				data.put(so.getObject(), set);
			}
		}
	}
	
	public static Map<String,Set<String>> getFastlookUpSubjectObjects() {
		return data;
	}
}
