package ise.roletagger.datasetconvertor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ise.roletagger.model.Category;
import ise.roletagger.model.Global;
import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

/**
 * This class convert ground truth files to a structure which CRFSuite CRF can
 * read for testing
 * 
 * In this approach first all the GT converted to features set and then once
 * we run CRF and let CRF calculate metircs
 * 
 * Ground truth should be in the format of
 * 
 * <RP Category="">
 * <HR>
 * </HR>
 * </RP>
 * 
 * @author fbm
 *
 */
public class ConvertGroundTruthToFeatures {
	
	private static final String GROUND_TRUTH_FOLDER = Config.getString("GROUND_TRUTH_FOLDER", "");

	/**
	 * How to convert ground truth to feature set? AND How to calculate accuracy,
	 * precision, ... Consider which part as TP,FP,TN,FN
	 */
	private final List<String> finalResult = Collections.synchronizedList(new ArrayList<>());

	public ConvertGroundTruthToFeatures() {
	}

	/**
	 * With seed
	 */
	public static void main(String[] args) throws IOException {

		final Thread t = new Thread(new ConvertGroundTruthToFeatures().run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {
			try {
				final File[] listOfFiles = new File(GROUND_TRUTH_FOLDER).listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					final String fileName = listOfFiles[i].getName();
					final List<String> positiveLines = Files.readAllLines(Paths.get(GROUND_TRUTH_FOLDER + fileName),
							StandardCharsets.UTF_8);
					generateFullDataset(positiveLines);
				}
				FileUtil.createFolder(GROUND_TRUTH_FOLDER);
				FileUtil.writeDataToFile(finalResult, GROUND_TRUTH_FOLDER + "features", false);
				finalResult.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	/**
	 * Run in parallel Convert data to the features set which can be used by
	 * CRFSuite First chunk data and pass each chunk to each thread Can be used for
	 * converting train or test data by {@code isTrain}
	 * 
	 * @param postiveData
	 * @param negativeData
	 * @param isTrain
	 */
	private void generateFullDataset(List<String> data) {
		try {
			for (String line : data) {
				String taggedLine = new String(line);
				if (taggedLine.isEmpty()) {
					continue;
				}

				taggedLine = ConvertDatasetToFeatureSetForCRFSuite.normalizeTaggedSentence(taggedLine);
				final Map<String, Map<String, Category>> parseData = TagParser.parse(taggedLine);
				String noTaggedLine = parseData.get("noTag").keySet().iterator().next();
				noTaggedLine = noTaggedLine.trim();
				final Map<Integer, Map<String, String>> result = SentenceToFeature.convertTaggedSentenceToFeatures(taggedLine, line.contains(Global.getHeadRoleStartTag()));
				finalResult.addAll(SentenceToFeature.featureMapToStringList(result));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
