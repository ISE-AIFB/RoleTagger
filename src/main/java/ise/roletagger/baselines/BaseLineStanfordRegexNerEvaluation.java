package ise.roletagger.baselines;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ise.roletagger.datasetconvertor.ConvertDatasetToFeatureSetForCRFSuite;
import ise.roletagger.datasetconvertor.SentenceToFeature;
import ise.roletagger.datasetconvertor.TagParser;
import ise.roletagger.datasetconvertor.TaggerType;
import ise.roletagger.datasetgenerator.RoleListProviderFileBased;
import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Global;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.model.Token;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.Config;

/**
 * @author fbm
 *
 */
public class BaseLineStanfordRegexNerEvaluation {

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

	RoleListProvider myOriginalDictioanry = new RoleListProviderFileBased();

	public BaseLineStanfordRegexNerEvaluation(TaggerType whichModelUse) {
		myOriginalDictioanry.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		myOriginalDictioanry.loadRoles(DataSourceType.WIKIDATA_LABEL);
		DictionayType = whichModelUse;
	}


	public static void main(String[] args) throws IOException {
		final Thread t = new Thread(new BaseLineStanfordRegexNerEvaluation(TaggerType.ONLY_HEAD_ROLE).run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {
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
		final List<Tuple> tuples = runBaseLineStanfordRegexNer(noTaggedLine);
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



	public List<Tuple> runBaseLineStanfordRegexNer(String noTaggedLine) {
		final List<Tuple> result = new ArrayList<>();
		try {
			final Process p = new ProcessBuilder("curl","--data",noTaggedLine,"http://10.10.4.10:9000/?properties={%22annotators%22%3A%22tokenize%2Cssplit%2Cpos%2Cner%2Cregexner%22%2C%22outputFormat%22%3A%22xml%22}").start();
			p.waitFor();
			final BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream())); 
			String s;
			StringBuilder outputXml = new StringBuilder();
			while ((s = stdInput.readLine()) != null) {
				outputXml.append(s);
			}
			
			List<Token> nerXmlParser = nerXmlParser(outputXml.toString());
			for(Token t:nerXmlParser) {
				final String word = t.getContent().get("word");
				final String tag = t.getContent().get("NER");
				if(tag!=null && tag.equalsIgnoreCase("TITLE")) {
					if(!myOriginalDictioanry.getData().containsKey(word.toLowerCase())){
						result.add(new Tuple(word, "O"));
					}else {
						result.add(new Tuple(word, "ROLE"));
					}
				}else {
					result.add(new Tuple(word, "O"));
				}
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;

	}
	
	public static List<Token> aggregateNerTagPositions(final List<Token> tokens) {
		final List<Token> result = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			final Token t = tokens.get(i);
			final String possibleNerTag = t.getContent().get("NER");
			if (possibleNerTag != null && possibleNerTag.equalsIgnoreCase("TITLE")) {
				Token newToken = new Token();
				newToken.getContent().putAll(t.getContent());
				for (int j = i + 1; j < tokens.size(); j++, i++) {
					final Token tt = tokens.get(j);
					final String possibleNerTag2 = tt.getContent().get("NER");
					if (possibleNerTag2 != null && possibleNerTag.equalsIgnoreCase("TITLE")
							&& possibleNerTag2.equals(possibleNerTag)) {
						newToken.getContent().put("word",
								newToken.getContent().get("word") + " " + tt.getContent().get("word"));
						newToken.getContent().put("CharacterOffsetEnd", tt.getContent().get("CharacterOffsetEnd"));
						newToken.getContent().put("POS", null);
						newToken.getContent().put("lemma", null);
					} else {
						break;
					}
				}
				newToken.getContent().put("NER", possibleNerTag);
				result.add(newToken);
			}else {
				result.add(t);
			}
			
		}
		return result;
	}

	public static List<Token> nerXmlParser(final String xml) {
		try {
			final List<Token> result = new ArrayList<>();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final org.w3c.dom.Document document = docBuilder
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

			final NodeList nodeList = document.getElementsByTagName("*");
			int wordCounter = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("token")) {
					if (node.hasChildNodes()) {
						Token t = new Token();
						
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);
							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								t.getContent().put(childNode.getNodeName(), childNode.getTextContent());
							}
						}
						t.getContent().put("ID", String.valueOf(wordCounter++));
						result.add(t);
					}
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
