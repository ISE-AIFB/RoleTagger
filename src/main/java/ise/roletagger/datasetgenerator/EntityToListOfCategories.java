package ise.roletagger.datasetgenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class EntityToListOfCategories {
	private final String folderPath; 
	private final Map<String,Set<String>> entity2categories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public EntityToListOfCategories(String entitycategoryfile) {
		folderPath = entitycategoryfile;
	}

	public void parse() {
		try (final BufferedReader br = new BufferedReader(new FileReader(folderPath))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.startsWith("#")) {
					continue;
				}else {
					final String[] all = sCurrentLine.split(" ");
					String subject = all[0];
					String predicate = all[1];
					String object = all[2];

					if(predicate.contains("terms/subject")) {
						final int subjectStart = subject.lastIndexOf("/")+1;
						final int subjectEnd = subject.indexOf(">");
						final int objectStart = object.lastIndexOf(":")+1;
						final int objectEnd = object.indexOf(">");
						subject = subject.substring(subjectStart, subjectEnd);
						object=object.substring(objectStart, objectEnd);
						
						Set<String> set = entity2categories.get(subject);
						if(set==null) {
							set = new HashSet<>();
							set.add(object);
							entity2categories.put(subject, set);
						}else {
							set.add(object);
							entity2categories.put(subject, set);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Set<String>> getEntity2categories() {
		return entity2categories;
	}
}
