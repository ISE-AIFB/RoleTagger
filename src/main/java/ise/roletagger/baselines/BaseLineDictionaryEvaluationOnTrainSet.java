package ise.roletagger.baselines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import ise.roletagger.datasetconvertor.TaggerType;
import ise.roletagger.model.Position;
import ise.roletagger.model.TagPosition;
import ise.roletagger.model.TagPositions;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.Config;
import ise.roletagger.util.DictionaryRegexPatterns;

/**
 * This class convert ground truth files to a structure which CRFSuite CRF can read
 * for testing
 * 
 * Ground truth should be in the format of
 * 
 * <RP Category=""><HR></HR></RP>
 * 
 * @author fbm
 *
 */
public class BaseLineDictionaryEvaluationOnTrainSet {

	private static final String CONVERTED_TRAIN_DATA = Config.getString("WHERE_TO_WRITE_CRF_DATASET", "")+"AllInOne";
	/**
	 * How to convert ground truth to feature set?
	 * AND
	 * How to calculate accuracy, precision, ...
	 * Consider which part as TP,FP,TN,FN 
	 */
	private final TaggerType DictionayType;

	static float truePositive = 0;
	static float falsePositive = 0;
	static float falseNegative = 0;
	static float trueNegative = 0;

	/**
	 * contains all the role from dictionary.Every role goes to a new regex pattern
	 * to be able to select the longest match
	 */
	private List<Pattern> rolePatterns = new ArrayList<>();

	public BaseLineDictionaryEvaluationOnTrainSet(TaggerType whichModelUse) {
		DictionayType = whichModelUse;
	}


	public static void main(String[] args) throws IOException {
		final Thread t = new Thread(new BaseLineDictionaryEvaluationOnTrainSet(TaggerType.ONLY_HEAD_ROLE).run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {

			getRoleRegex();

			try {
				generateFullDataset();
				
				final float precision = truePositive/(truePositive+falsePositive);
				System.err.println("----------------BaseLine--"+DictionayType.name()+"--------------------");
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


	public void getRoleRegex() {

		switch (DictionayType) {
		case ONLY_HEAD_ROLE:
			rolePatterns = DictionaryRegexPatterns.getRolePatterns();
			break;
		default:
			break;
		}
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
	private void generateFullDataset() {
		try {
			final Map<Integer, Map<String, String>> result = new HashMap<>();
			StringBuilder noTaggedLine = new StringBuilder();
			int wordCount=0;
			try (BufferedReader br = new BufferedReader(new FileReader(CONVERTED_TRAIN_DATA))) {
				String line;
				while ((line = br.readLine()) != null) {
					String tabSeperatedLine = new String(line);
					if(tabSeperatedLine.isEmpty()) {
						evaluate(result, noTaggedLine.toString().trim());
						noTaggedLine = new StringBuilder();
						result.clear();
						wordCount=0;
						continue;
					}else {
						final String[] split = tabSeperatedLine.split("\t");
						Map<String,String> map = new HashMap<>();
						map.put("TAG", split[0]);
	 					result.put(wordCount, map);
						noTaggedLine.append(split[1].split("=")[1]).append(" ");
						wordCount++;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluate(Map<Integer, Map<String, String>> result, String noTaggedLine) {
		final List<Tuple> tuples = runBaseLineDictionary(noTaggedLine);
		if(tuples.size()!=result.size()) {
			throw new IllegalArgumentException("Size of the tuples and size of the result are not similar");
		}

		for(int i=0;i<tuples.size();i++) {
			final Tuple tuple = tuples.get(i);
			final Map<String, String> map = result.get(i);
			final String realTag = map.get("TAG");
			final String predictaedTag = tuple.b;

			if(realTag.equalsIgnoreCase(predictaedTag) && realTag.equalsIgnoreCase("O")) {
				trueNegative++;
			}else if(realTag.equalsIgnoreCase(predictaedTag) && !realTag.equalsIgnoreCase("O")) {
				truePositive++;
			}
			else {
				if(realTag.equalsIgnoreCase("O")) {
					falsePositive++;	
					//System.out.println(map.get("P2")+" "+map.get("P1")+" "+map.get("word")+" "+map.get("N1")+" "+map.get("N2")+"==>"+map.get("word"));
				}else {
					falseNegative++;
					//System.err.println(map.get("P2")+" "+map.get("P1")+" "+map.get("word")+" "+map.get("N1")+" "+map.get("N2")+"==>"+map.get("word"));
				}
			}
		}
	}

	public List<Tuple> runBaseLineDictionary(String noTaggedLine) {
		final List<Tuple> result = new ArrayList<>();
		final TagPositions tagPositions = new TagPositions();
		for (Pattern p : rolePatterns) {
			final Matcher matcher = p.matcher(noTaggedLine);
			while (matcher.find()) {
				String foundText = matcher.group();
				int start = matcher.start();
				int end = matcher.end();
				final TagPosition tp = new TagPosition(foundText, start, end);
				if (tagPositions.alreadyExist(tp)) {
					continue;
				}
				tagPositions.add(tp);
			}
		}

		final TokenizerFactory<Word> tf = PTBTokenizer.factory();		
		List<Word> tokens_words = null;
		try {
			tokens_words = tf.getTokenizer(new StringReader(noTaggedLine)).tokenize();
			for(Word w:tokens_words) {
				if(isDetected(tagPositions,w)) {
					result.add(new Tuple(w.value(), "ROLE"));
				}else {
					result.add(new Tuple(w.value(), "O"));
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static boolean isDetected(TagPositions tagPositions, Word w) {
		int start = w.beginPosition();
		int end = w.endPosition();

		for(int i=0;i<tagPositions.getPositions().size();i++) {
			if(tagPositions.getPositions().get(i).contains(new Position(start, end))) {
				return true;
			}
		}
		return false;
	}
}
