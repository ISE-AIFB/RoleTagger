package ise.roletagger.evaluationtokenbased;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import ise.roletagger.datasetconvertor.SentenceToFeature;
import ise.roletagger.model.Global;
import ise.roletagger.util.Config;

public class GroundTruthStatistics {

	private static final String GROUND_TRUTH_FOLDER = Config.getString("GROUND_TRUTH_FOLDER", "");
	
	public static void main(String[] args) {
		
		final Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	public static Runnable run() {
		return () -> {
			double totalNumberOfTokens = 0.;
			double totalNumberOfRoles = 0.;
			int numberOfPositiveSentence = 0;
			int numberOfNegativeSentence = 0;
			try {
				final File[] listOfFiles = new File(GROUND_TRUTH_FOLDER).listFiles();				
				for (int i = 0; i < listOfFiles.length; i++) {
					final String fileName = listOfFiles[i].getName();
					final List<String> lines = Files.readAllLines(Paths.get(GROUND_TRUTH_FOLDER+fileName),StandardCharsets.UTF_8);
					
					for(String line:lines) {
						if(line.isEmpty()) {
							continue;
						}
						if(line.contains(Global.getHeadRoleEndTag())) {
							numberOfPositiveSentence++;
						}else {
							numberOfNegativeSentence++;
						}
						
						final Map<Integer, Map<String, String>> convertTaggedSentenceToFeatures = SentenceToFeature.convertTaggedSentenceToFeatures(line, line.contains(Global.getHeadRoleEndTag()));
						totalNumberOfTokens+=convertTaggedSentenceToFeatures.size();
						
						for(Map<String, String> entry:convertTaggedSentenceToFeatures.values()) {
							if(!entry.get("TAG").equalsIgnoreCase("O")) {
								totalNumberOfRoles++;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.err.println("-------------GroundTruth Statistics------------------");
			System.err.println("Total number of positive sentences = "+numberOfPositiveSentence);
			System.err.println("Total number of negative sentences = "+numberOfNegativeSentence);
			System.err.println("Total number of sentences = "+(numberOfNegativeSentence+numberOfPositiveSentence));
			System.err.println("Total number of token = "+totalNumberOfTokens);
			System.err.println("Total number of roles = "+totalNumberOfRoles);
			System.err.println("Percentage of roles = "+(totalNumberOfRoles*100)/totalNumberOfTokens +"%");
		};
	}
}
