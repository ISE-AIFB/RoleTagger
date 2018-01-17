package ise.roletagger.model;

/**
 * This class contains information about each triple Subject Property Object.
 * As property here always means skos:broader, it is omitted.
 * @author fbm
 *
 */
public class SubjectObject {
	
	private final String subject;
	private final String object;

	public SubjectObject(String subject, String object) {
		this.subject = subject;
		this.object = object;
	}
	public String getSubject() {
		return subject;
	}
	public String getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "SubjectObject [subject=" + subject + ", object=" + object + "]";
	}

}
