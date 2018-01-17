package ise.roletagger.datasetuniquefier;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ise.roletagger.model.Global;
import ise.roletagger.util.Config;

public class DatasetUniqefier {

	private static final String DATASET_BASE_ADDRESS = Config.getString("WHERE_TO_WRITE_DATASET", "");
	
	private static final String POSITIVE_DATA = DATASET_BASE_ADDRESS+Config.getString("POSITIVE_DATASET_NAME", "");
	private static final String NEGATIVE_DATA = DATASET_BASE_ADDRESS+Config.getString("DIFFICULT_NEGATIVE_DATASET_NAME", "");
	private static final String EASY_NEGATIVE_DATA = DATASET_BASE_ADDRESS+Config.getString("EASY_NEGATIVE_DATASET_NAME", "");

	private static final Map<String,Set<String>> positiveMap = new HashMap<>();
	private static final Map<String,Set<String>> negativeMap = new HashMap<>();
	private static final Map<String,Set<String>> easyNegativeMap = new HashMap<>();

	public static void main(String[] args) {
		final Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	public static Runnable run() {
		return () -> {
			try {
				final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA), StandardCharsets.UTF_8);
				final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA), StandardCharsets.UTF_8);
				final List<String> easyNegativeLines = Files.readAllLines(Paths.get(EASY_NEGATIVE_DATA), StandardCharsets.UTF_8);

				for (int i = 0; i < positiveLines.size(); i++) {
					final String posLine = positiveLines.get(i);			
					final String posSentence = Global.removeAnchorTextTag(Global.removeHeadRoleTag(Global.removeRolePhraseTag(posLine)));
					final String punctuationRemoved = posSentence.replaceAll("[^\\w\\s]", "");
					Set<String> set = positiveMap.get(punctuationRemoved.toLowerCase());
					if(set==null || set.isEmpty()) {
						Set<String> newSet = new HashSet<>();
						newSet.add(posLine);
						positiveMap.put(punctuationRemoved.toLowerCase(), newSet);
					}else {
						set.add(posLine);
						positiveMap.put(punctuationRemoved.toLowerCase(), set);
					}
				}

				writeDataToFile(positiveMap,POSITIVE_DATA+"Unique");

				for (int i = 0; i < negativeLines.size(); i++) {
					final String negLine = negativeLines.get(i);
					final String negSentence = Global.removeAnchorTextTag(Global.removeHeadRoleTag(Global.removeRolePhraseTag(negLine)));
					final String punctuationRemoved = negSentence.replaceAll("[^\\w\\s]", "");
					Set<String> set = negativeMap.get(punctuationRemoved.toLowerCase());
					if(set==null || set.isEmpty()) {
						Set<String> newSet = new HashSet<>();
						newSet.add(negLine);
						negativeMap.put(punctuationRemoved.toLowerCase(), newSet);
					}else {
						set.add(negLine);
						negativeMap.put(punctuationRemoved.toLowerCase(), set);
					}
				}

				writeDataToFile(negativeMap,NEGATIVE_DATA+"Unique");
				
				for (int i = 0; i < easyNegativeLines.size(); i++) {
					final String negLine = easyNegativeLines.get(i);
					final String negSentence = Global.removeAnchorTextTag(Global.removeHeadRoleTag(Global.removeRolePhraseTag(negLine)));
					final String punctuationRemoved = negSentence.replaceAll("[^\\w\\s]", "");
					Set<String> set = negativeMap.get(punctuationRemoved.toLowerCase());
					if(set==null || set.isEmpty()) {
						Set<String> newSet = new HashSet<>();
						newSet.add(negLine);
						easyNegativeMap.put(punctuationRemoved.toLowerCase(), newSet);
					}else {
						set.add(negLine);
						easyNegativeMap.put(punctuationRemoved.toLowerCase(), set);
					}
				}

				writeDataToFile(easyNegativeMap,EASY_NEGATIVE_DATA+"Unique");
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
	}

	private static void writeDataToFile(Map<String, Set<String>> data,final String fileName) {
		final Path file = Paths.get(fileName);
		final List<String> lines = new ArrayList<>();
		for(Set<String> set:data.values()) {
			List<String> a = new ArrayList<>(set);
			lines.add(a.get(0));
		}		
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
