package ise.roletagger.categorytreegeneration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ise.roletagger.model.ListOfSubjectObject;
import ise.roletagger.model.SubjectObject;

public class CategoryFileParser {

	private final String CATEGORY_FILE;
	public CategoryFileParser(String categoryFile) {
		if(categoryFile==null || categoryFile.isEmpty()) {
			throw new IllegalArgumentException("CATEGORY_FILE is null or empty");
		}
		CATEGORY_FILE = categoryFile;
	}

	public void parse() {
		try (final BufferedReader br = new BufferedReader(new FileReader(CATEGORY_FILE))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.startsWith("#")) {
					continue;
				}else {
					final String[] all = sCurrentLine.split(" ");
					final String subject = all[0];
					final String predicate = all[1];
					final String object = all[2];

					if(predicate.contains("skos/core#broader")) {
						final int subjectStart = subject.lastIndexOf(":")+1;
						final int subjectEnd = subject.indexOf(">");
						final int objectStart = object.lastIndexOf(":")+1;
						final int objectEnd = object.indexOf(">");
						ListOfSubjectObject.getListOfSubjectObjects().add(new SubjectObject(subject.substring(subjectStart, subjectEnd), object.substring(objectStart, objectEnd)));
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}