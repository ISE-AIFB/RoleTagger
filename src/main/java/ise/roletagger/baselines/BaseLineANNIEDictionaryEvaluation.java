package ise.roletagger.baselines;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import ise.roletagger.datasetconvertor.ConvertDatasetToFeatureSetForCRFSuite;
import ise.roletagger.datasetconvertor.SentenceToFeature;
import ise.roletagger.datasetconvertor.TagParser;
import ise.roletagger.datasetconvertor.TaggerType;
import ise.roletagger.datasetgenerator.RoleListProviderFileBased;
import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Global;
import ise.roletagger.model.Position;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.model.TagPosition;
import ise.roletagger.model.TagPositions;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.Config;
import ise.roletagger.util.DictionaryRegexPatterns;

/**
 * @author fbm
 *
 */
public class BaseLineANNIEDictionaryEvaluation {

	private static final String GROUND_TRUTH_FOLDER = Config.getString("GROUND_TRUTH_FOLDER", "");
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

	RoleListProvider myOriginalDictioanry = new RoleListProviderFileBased();
	
	public BaseLineANNIEDictionaryEvaluation(TaggerType whichModelUse) {
		myOriginalDictioanry.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		myOriginalDictioanry.loadRoles(DataSourceType.WIKIDATA_LABEL);
		DictionayType = whichModelUse;
	}


	public static void main(String[] args) throws IOException {
		final Thread t = new Thread(new BaseLineANNIEDictionaryEvaluation(TaggerType.ONLY_HEAD_ROLE).run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {

			getRoleRegex();

			try {
				final File[] listOfFiles = new File(GROUND_TRUTH_FOLDER).listFiles();				
				for (int i = 0; i < listOfFiles.length; i++) {
					final String fileName = listOfFiles[i].getName();
					final List<String> positiveLines = Files.readAllLines(Paths.get(GROUND_TRUTH_FOLDER+fileName),StandardCharsets.UTF_8);
					generateFullDataset(positiveLines);
				}
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
	private void generateFullDataset(List<String> data) {
		try {
			for(String line:data) {
				String taggedLine = new String(line);
				if(taggedLine.isEmpty()) {
					continue;
				}
				taggedLine = ConvertDatasetToFeatureSetForCRFSuite.normalizeTaggedSentence(taggedLine);
				final Map<String, Map<String, Category>> parseData = TagParser.parse(taggedLine);
				String noTaggedLine = parseData.get("noTag").keySet().iterator().next();
				noTaggedLine = noTaggedLine.trim();
				final Map<Integer, Map<String, String>> result = SentenceToFeature.convertTaggedSentenceToFeatures(taggedLine, line.contains(Global.getHeadRoleStartTag()));
				
				evaluate(result, noTaggedLine);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluate(Map<Integer, Map<String, String>> result, String noTaggedLine) {
		final List<Tuple> tuples = runBaseLineOtherDictionary(noTaggedLine);
		if(tuples.size()!=result.size()) {
			throw new IllegalArgumentException("Size of the tuples and size of the result are not similar");
		}

		for(int i=0;i<tuples.size();i++) {
			final Tuple tuple = tuples.get(i);
			final Map<String, String> map = result.get(i);
			final String realTag = map.get("TAG");
			final String predictedTag = tuple.b;

			if(realTag.equalsIgnoreCase(predictedTag) && realTag.equalsIgnoreCase("O")) {
				trueNegative++;
			}else if(realTag.equalsIgnoreCase(predictedTag) && !realTag.equalsIgnoreCase("O")) {
				truePositive++;
			}
			else {
				if(realTag.equalsIgnoreCase("O")) {
					falsePositive++;	
					System.out.println(map.get("P2")+" "+map.get("P1")+" "+map.get("word")+" "+map.get("N1")+" "+map.get("N2")+"==>"+map.get("word"));
				}else {
					falseNegative++;
					System.err.println(map.get("P2")+" "+map.get("P1")+" "+map.get("word")+" "+map.get("N1")+" "+map.get("N2")+"==>"+map.get("word"));
				}
			}
		}
	}
	
	

	public List<Tuple> runBaseLineOtherDictionary(String noTaggedLine) {
		final List<Tuple> result = new ArrayList<>();
		final TagPositions tagPositions = new TagPositions();
		for (Pattern p : rolePatterns) {
			final Matcher matcher = p.matcher(noTaggedLine);
			while (matcher.find()) {
				String foundText = matcher.group();
				int start = matcher.start();
				int end = matcher.end();
				if(!myOriginalDictioanry.getData().containsKey(foundText.toLowerCase())){
					continue;
				}
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
