package ise.roletagger.datasetconvertor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ise.roletagger.model.Global;
import ise.roletagger.model.TrainTestData;
import ise.roletagger.util.CharactersUtils;
import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

/**
 * This class convert dataset files to a structure which CRFSuite CRF can read
 * for training
 * 
 * This version works with output of
 * {@link DatasetGeneratorWithCategoryTrees4thVersion}}
 * 
 * @author fbm
 *
 */
public class ConvertDatasetToFeatureSetForCRFSuite {

	private final String WHERE_TO_WRITE_CRF_DATASET = Config.getString("WHERE_TO_WRITE_CRF_DATASET", "");

	private final int CHUNKING_SIZE = 1000;
	private final int NUMBER_OF_THREADS = Config.getInt("NUMBER_OF_THREADS", 0);

	private final String POSITIVE_DATA_ADDRESS = Config.getString("WHERE_TO_WRITE_DATASET", "")
			+ Config.getString("POSITIVE_UNIQUE_DATASET_NAME", "");
	private final String NEGATIVE_DATA_ADDRESS = Config.getString("WHERE_TO_WRITE_DATASET", "")
			+ Config.getString("DIFFICULT_UNIQUE_NEGATIVE_DATASET_NAME", "");
	private final String EASY_NEGATIVE_DATA_ADDRESS = Config.getString("WHERE_TO_WRITE_DATASET", "")
			+ Config.getString("EASY_UNIQUE_NEGATIVE_DATASET_NAME", "");
	/**
	 * How much data should be considered as test data?
	 */
	private double TEST_PERCENTAGE = 0.2f;

	/**
	 * With seed
	 */
	private final Random RAND = new Random(1);

	public ConvertDatasetToFeatureSetForCRFSuite(double d) {
		TEST_PERCENTAGE = d;
	}

	public static void main(String[] args) throws IOException {
		final Thread t = new Thread(new ConvertDatasetToFeatureSetForCRFSuite(0.).run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {
			try {
				final List<String> positiveLines = Files.readAllLines(Paths.get(POSITIVE_DATA_ADDRESS),
						StandardCharsets.UTF_8);
				final List<String> negativeLines = Files.readAllLines(Paths.get(NEGATIVE_DATA_ADDRESS),
						StandardCharsets.UTF_8);
				
				final List<String> easyNegativeLines = Files.readAllLines(Paths.get(EASY_NEGATIVE_DATA_ADDRESS),
						StandardCharsets.UTF_8);

				final TrainTestData positiveTTD = sampleData(positiveLines, TEST_PERCENTAGE);
				final TrainTestData difficultNegativeTTD = sampleData(negativeLines, TEST_PERCENTAGE);
				final TrainTestData easyNegativeTTD = sampleData(easyNegativeLines, TEST_PERCENTAGE);

				System.err.println(positiveTTD.getTrainSet().size());
				System.err.println(positiveTTD.getTestSet().size());

				System.err.println(difficultNegativeTTD.getTrainSet().size());
				System.err.println(difficultNegativeTTD.getTestSet().size());
				
				System.err.println(easyNegativeTTD.getTrainSet().size());
				System.err.println(easyNegativeTTD.getTestSet().size());

				generateFullDatasetOnlyForPositive(positiveTTD.getTrainSet(),true);
				generateFullDatasetOnlyForDifficultNegative(difficultNegativeTTD.getTrainSet(),false);
				generateFullDatasetOnlyForEasyNegative(easyNegativeTTD.getTrainSet(),false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	private void generateFullDatasetOnlyForDifficultNegative(List<String> difficultNegative,boolean isPositive) {
		try {
			final List<String> finalResult = Collections.synchronizedList(new ArrayList<>());
			final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
			for (int i = 0; i < difficultNegative.size(); i = Math.min(i + CHUNKING_SIZE, difficultNegative.size())) {
				executor.execute(handle(difficultNegative, i, Math.min(i + CHUNKING_SIZE, difficultNegative.size()), isPositive,finalResult));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			FileUtil.createFolder(WHERE_TO_WRITE_CRF_DATASET);
			FileUtil.writeDataToFile(finalResult, WHERE_TO_WRITE_CRF_DATASET + "difficultNegativeSeparated", false);
			finalResult.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void generateFullDatasetOnlyForPositive(List<String> positive,boolean isPositive) {
		try {
			final List<String> finalResult = Collections.synchronizedList(new ArrayList<>());
			final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
			for (int i = 0; i < positive.size(); i = Math.min(i + CHUNKING_SIZE, positive.size())) {
				executor.execute(handle(positive, i, Math.min(i + CHUNKING_SIZE, positive.size()),isPositive,finalResult));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			FileUtil.createFolder(WHERE_TO_WRITE_CRF_DATASET);
			FileUtil.writeDataToFile(finalResult, WHERE_TO_WRITE_CRF_DATASET + "positiveSeparated", false);
			finalResult.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
	}

	private void generateFullDatasetOnlyForEasyNegative(List<String> easyNegativeData,boolean isPositive) {
		try {
			final List<String> finalResult = Collections.synchronizedList(new ArrayList<>());
			final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
			for (int i = 0; i < easyNegativeData.size(); i = Math.min(i + CHUNKING_SIZE, easyNegativeData.size())) {
				executor.execute(handle(easyNegativeData, i, Math.min(i + CHUNKING_SIZE, easyNegativeData.size()), isPositive,finalResult));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			FileUtil.createFolder(WHERE_TO_WRITE_CRF_DATASET);
			FileUtil.writeDataToFile(finalResult, WHERE_TO_WRITE_CRF_DATASET + "easyNegativeSeparated", false);
			finalResult.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a runnable for processing chunk of data. What this function does is
	 * converting data to features set to be used in CRFSuite
	 * 
	 * @param data
	 * @param start
	 * @param end
	 * @param isPositive
	 * @param finalResult 
	 * @return
	 */
	private Runnable handle(List<String> data, int start, int end, boolean isPositive, List<String> finalResult) {
		final Runnable r = () -> {
			for (int i = start; i < end; i++) {
				String line = data.get(i);
				String taggedLine = line;
				taggedLine = normalizeTaggedSentence(taggedLine);
				final Map<Integer, Map<String, String>> result = SentenceToFeature.convertTaggedSentenceToFeatures(taggedLine,isPositive);
				finalResult.addAll(SentenceToFeature.featureMapToStringList(result));
			}
			System.err.println(
					"Thread " + (isPositive ? "positive " : "negative ") + ((start / CHUNKING_SIZE) + 1) + " is done");
		};
		return r;
	}
	
	public static String normalizeTaggedSentence(String line) {
		final Map<String,String> result = new HashMap<>();
		line = Global.removeRolePhraseTag(line);
		Pattern p = Pattern.compile("</?HR>");
		Matcher matcher = p.matcher(line);
		while(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			line = line.replace(matcher.group(0),saltString);
			result.put(saltString, matcher.group(0));
		}
		
		line = CharactersUtils.normalizeTrainSentence(line);
		
		for(Entry<String, String> e:result.entrySet()) {
			line = line.replace(e.getKey(), e.getValue());
		}
		
		line = line.replaceAll("\\s+"," ");
		return line;
	}

	private TrainTestData sampleData(List<String> lines, double testPer) {
		if(testPer==0.) {
			return new TrainTestData(lines, new ArrayList<>());
		}
		
		int total = lines.size();
		int trainSize = (int) (total * (1 - testPer));
		final Set<Integer> indexes = new HashSet<>();
		while (indexes.size() < trainSize) {
			indexes.add((int) (RAND.nextFloat() * total));
		}
		final List<String> train = new ArrayList<>();
		final List<String> test = new ArrayList<>();

		for (int i : indexes) {
			train.add(lines.get(i));
		}

		for (int i = 0; i < total; i++) {
			if (!indexes.contains(i)) {
				test.add(lines.get(i));
			}
		}

		return new TrainTestData(train, test);
	}

}
