package ise.roletagger.evaluationtokenbased;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import ise.roletagger.datasetconvertor.ConvertDatasetToFeatureSetForCRFSuite;
import ise.roletagger.datasetconvertor.SentenceToFeature;
import ise.roletagger.datasetconvertor.TagParser;
import ise.roletagger.datasetconvertor.TaggerType;
import ise.roletagger.model.Category;
import ise.roletagger.model.Global;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.Config;

/**
 * This class convert ground truth files to a structure which CRFSuite CRF can
 * read for testing
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
public class CRFEvaluation {

	private static final String GROUND_TRUTH_FOLDER = Config.getString("GROUND_TRUTH_FOLDER", "");

	/**
	 * How to convert ground truth to feature set? AND How to calculate accuracy,
	 * precision, ... Consider which part as TP,FP,TN,FN
	 */
	private final TaggerType TP;
	private final CRFModels CRF_MODEL;

	static float truePositive = 0;
	static float falsePositive = 0;
	static float falseNegative = 0;
	static float trueNegative = 0;

	public CRFEvaluation(TaggerType howToTagGroundTruth, CRFModels whichCRFModelUse) {
		TP = howToTagGroundTruth;
		CRF_MODEL = whichCRFModelUse;
	}

	/**
	 * With seed
	 */
	public static void main(String[] args) throws IOException {

		final Thread t = new Thread(new CRFEvaluation(TaggerType.ONLY_HEAD_ROLE, CRFModels.ONLY_HEAD_ROLE).run());
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
				final float precision = truePositive / (truePositive + falsePositive);
				System.err
						.println("----------------CRF-" + CRF_MODEL.name() + "-" + TP.name() + "--------------------");
				System.err.println("Precision= " + precision);
				final float recall = truePositive / (truePositive + falseNegative);
				System.err.println("Recall= " + recall);
				System.err.println("F1= " + (2 * precision * recall) / (precision + recall));
				System.err.println("Accuracy= " + (truePositive + trueNegative)
						/ (falsePositive + falseNegative + trueNegative + truePositive));

				System.err.println("Total= " + (truePositive + falseNegative + falsePositive + trueNegative));
				System.err.println("Total Role TOKENS= " + (truePositive + falseNegative));
				System.err.println("TRUE POSITIVE= " + truePositive);
				System.err.println("FALSE POSITIVE= " + falsePositive);
				System.err.println("FALSE NEGATIVE= " + falseNegative);

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
				evaluate(result, noTaggedLine,taggedLine);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluate(Map<Integer, Map<String, String>> result, String noTaggedLine, String taggedLine) {
		final List<Tuple> tuples = RunCRFSuite.run(CRF_MODEL,result);
		if (tuples.size() != result.size()) {
			throw new IllegalArgumentException("Size of the tuples and size of the result are not similar");
		}

		for (int i = 0; i < tuples.size(); i++) {
			final Tuple tuple = tuples.get(i);
			final Map<String, String> map = result.get(i);
			final String realTag = map.get("TAG");
			final String predictaedTag = tuple.b;

			if (realTag.equalsIgnoreCase(predictaedTag) && realTag.equalsIgnoreCase("O")) {
				trueNegative++;
			} else if (realTag.equalsIgnoreCase(predictaedTag) && !realTag.equalsIgnoreCase("O")) {
				truePositive++;
				System.out.println(map.get("P2") + " " + map.get("P1") + " " + map.get("word") + " " + map.get("N1")
				+ " " + map.get("N2") + "==>" + map.get("word"));
			} else {
				if (realTag.equalsIgnoreCase("O")) {
					falsePositive++;
					// System.err.println(i+"\n"+taggedLine+"\n"+map);
//					System.out.println(map.get("P2") + " " + map.get("P1") + " " + map.get("word") + " " + map.get("N1")
//							+ " " + map.get("N2") + "==>" + map.get("word"));
				} else {
					falseNegative++;
//					 System.err.println(map.get("P2") + " " + map.get("P1") + " " +
//					 map.get("word") + " " + map.get("N1")
//					 + " " + map.get("N2") + "==>" + map.get("word"));
					// System.out.println(i+"\n"+taggedLine+"\n"+map);
				}
			}
		}
	}
}
