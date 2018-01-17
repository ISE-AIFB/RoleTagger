package ise.roletagger.evaluationtokenbased;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ise.roletagger.datasetconvertor.TaggerType;
import ise.roletagger.model.Tuple;

/**
 * This class reads already converted ground truth and run CRF on it
 * 
 * @author fbm
 *
 */
public class CRFEvaluationByReadingFile {

	private static final String GROUND_TRUTH_CONVERTED_FILE = "./data/GroundTruthFeaturesMergedSpaceOnlyWord";

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

	public CRFEvaluationByReadingFile(TaggerType howToTagGroundTruth, CRFModels whichCRFModelUse) {
		TP = howToTagGroundTruth;
		CRF_MODEL = whichCRFModelUse;
	}

	/**
	 * With seed
	 */
	public static void main(String[] args) throws IOException {

		final Thread t = new Thread(new CRFEvaluationByReadingFile(TaggerType.ONLY_HEAD_ROLE, CRFModels.ONLY_HEAD_ROLE).run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {
			try {
				final List<String> positiveLines = Files.readAllLines(Paths.get(GROUND_TRUTH_CONVERTED_FILE),
						StandardCharsets.UTF_8);
				generateFullDataset(positiveLines);

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
			final Map<Integer, Map<String, String>> result = new HashMap<>();
			StringBuilder noTaggedLine = new StringBuilder();
			int wordCount=0;
			for (String line : data) {
				String tabSeperatedLine = new String(line);
				if(tabSeperatedLine.isEmpty() || tabSeperatedLine.equals("")) {
					evaluate(result,tabSeperatedLine);
					noTaggedLine = new StringBuilder();
					result.clear();
					wordCount=0;
					continue;
				}else {
					final String[] split = tabSeperatedLine.split("\t");
					Map<String,String> map = new HashMap<>();
					map.put("TAG", split[0]);
					for(int i=1;i<split.length;i++) {
						final String[] split2 = split[i].split("=");
						map.put(split2[0], split2[1]);						
					}
					noTaggedLine.append(split[1].split("=")[1]).append(" ");
					result.put(wordCount, map);
					wordCount++;
				}
			}
	} catch (final Exception e) {
		e.printStackTrace();
	}
}

private void evaluate(Map<Integer, Map<String, String>> result,String taggedLine) {
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
		} else {
			if (realTag.equalsIgnoreCase("O")) {
				falsePositive++;
				System.out.println(map.get("P2") + " " + map.get("P1") + " " + map.get("word") + " " + map.get("N1")
				+ " " + map.get("N2") + "==>" + map.get("word"));
			} else {
				falseNegative++;
				System.err.println(map.get("P2") + " " + map.get("P1") + " " +
						map.get("word") + " " + map.get("N1")
						+ " " + map.get("N2") + "==>" + map.get("word"));
			}
		}
	}
}
}
