package ise.roletagger.datasetconvertor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ise.roletagger.model.Category;
import ise.roletagger.model.Token;
import ise.roletagger.util.MyStandfordCoreNLPRegex;
import ise.roletagger.util.MyStanfordCoreNLP;

/**
 * This class is responsible for converting normal sentences or tagged sentences
 * to a features set
 * 
 * @author fbm
 *
 */
public class SentenceToFeature {

	/**
	 * How many words should be considered as a context?
	 */
	private final static int WINDOW_SIZE = 2;
	private final static String RESULT_CONTENT_SEPARATOR = "\t";

	public static Map<Integer, Map<String, String>> convertPlainSentenceToFeatures(final String plainSentence) {
		final Map<Integer, Map<String, String>> result = nerXmlParser(MyStanfordCoreNLP.runNerTaggerXML(plainSentence));		
		addContextFeatures(result, WINDOW_SIZE);

		final List<Token> run = MyStandfordCoreNLPRegex.run(plainSentence);
		final List<Integer> wordIds = run.stream().filter(p->"ROLE".equals(p.getContent().get("NER"))).map(p->Integer.parseInt(p.getContent().get("ID"))).collect(Collectors.toList());
		addOtherFeatrues(result,wordIds);

		return result;
	}

	public static List<String> featureMapToStringList(final Map<Integer, Map<String, String>> result) {
		final List<String> localfinalResult = new ArrayList<>();
		for (Entry<Integer, Map<String, String>> entity2 : result.entrySet()) {
			final Map<String, String> value = entity2.getValue();
			final StringBuilder l2 = new StringBuilder();

			final String tag = value.get("TAG");
			if (tag == null) {
				continue;
			}
			l2.append(tag + RESULT_CONTENT_SEPARATOR);

			for (Entry<String, String> e : value.entrySet()) {
				/**
				 * This tag should be the last tag always
				 */
				if (e.getKey().equals("N2Ner")) {
				//if (e.getKey().equals("P1Pos|IsInDic|N1Pos|N2Ner")) {
				//if (e.getKey().equals("P1IsInCap")) {
					l2.append(e.getKey() + "=" + e.getValue());
				} else if (!e.getKey().equals("TAG") && !e.getKey().equals("ID")) {
					l2.append(e.getKey() + "=" + e.getValue()).append(RESULT_CONTENT_SEPARATOR);
				}
			}
			localfinalResult.add(l2.toString());
		}
		localfinalResult.add("\n");
		return localfinalResult;
	}

	/**
	 * Convert tagged sentence to a feature set + adds corresponding label to it it
	 * can be used to convert Ground Truth sentences to feature set
	 * 
	 * @param taggedLine
	 * @param taggerType
	 * @param isPositive
	 * @return
	 */
	public static Map<Integer, Map<String, String>> convertTaggedSentenceToFeatures(final String taggedLine,
			final boolean isPositive) {
		final Map<String, Map<String, Category>> parseData = TagParser.parse(taggedLine);
		final String noTaggedLine = parseData.get("noTag").keySet().iterator().next().replaceAll("\\s+", " ");

		final Map<Integer, Map<String, String>> result = nerXmlParser(MyStanfordCoreNLP.runNerTaggerXML(noTaggedLine));

		final List<Token> tokens = MyStandfordCoreNLPRegex.run(noTaggedLine);
		final List<Integer> wordIds = tokens.stream().filter(p->"ROLE".equals(p.getContent().get("NER"))).map(p->Integer.parseInt(p.getContent().get("ID"))).collect(Collectors.toList());

		addOtherFeatrues(result,wordIds);		
		addContextFeatures(result, WINDOW_SIZE);		

		try {
			if (isPositive) {
				newEasyWayOfAddingLabel(result, taggedLine,tokens);
			} else {
				addNegativeLabels(result);
			}
		} catch (Exception e) {
			 e.printStackTrace();
		}
		
		return result;
	}

	private static void addOtherFeatrues(Map<Integer, Map<String, String>> result, List<Integer> wordIds) {
		for (int i = 0; i < result.size(); i++) {
			if(wordIds.contains(i)) {
				result.get(i).put("IsInDic", "1");
			}else {
				result.get(i).put("IsInDic", "0");
			}
		}
	}

	/**
	 * Add Tag to negative data. Tag means label of the data which for negative case
	 * it is always "O"
	 * 
	 * @param result
	 */
	public static void addNegativeLabels(Map<Integer, Map<String, String>> result) {
		for (int i = 0; i < result.size(); i++) {
			result.get(i).put("TAG", "O");
		}
	}
	
	private static void newEasyWayOfAddingLabel(Map<Integer, Map<String, String>> result, String taggedLine, List<Token> tokens) {
		for(int wordCount=0;wordCount<tokens.size();wordCount++) {
			final Token token = tokens.get(wordCount);
			final String NER = token.getContent().get("NER");
			if(NER!=null && NER.equals("ROLE")){
				result.get(wordCount).put("TAG", "ROLE");
			}else {
				result.get(wordCount).put("TAG", "O");
			}
		}
	}

	/**
	 * consider window around the word and add features of the window
	 * 
	 * @param result
	 * @param windowSize
	 *            how many words consider before and after main word
	 */
	private static void addContextFeatures(Map<Integer, Map<String, String>> result, int windowSize) {
		for (final Entry<Integer, Map<String, String>> entity : result.entrySet()) {
			final Integer wordPosition = entity.getKey();

			for (int i = 1; i <= windowSize; i++) {
				Map<String, String> previousWord = getFeaturesOfNeighborWord(wordPosition, wordPosition - i, result, i);
				result.put(wordPosition, previousWord);
			}

			for (int i = 1; i <= windowSize; i++) {
				Map<String, String> nextWord = getFeaturesOfNeighborWord(wordPosition, wordPosition + i, result, i);
				result.put(wordPosition, nextWord);
			}
		}
	}

	/**
	 * Currently only uses - Word - POS - NER
	 * 
	 * @param wordPosition
	 * @param neighborPosition
	 * @param result
	 * @param i
	 * @return
	 */
	private static Map<String, String> getFeaturesOfNeighborWord(int wordPosition, int neighborPosition,
			Map<Integer, Map<String, String>> result, int i) {
		String letter = "P";
		if (neighborPosition > wordPosition) {
			letter = "N";
		}
		if (!result.containsKey(neighborPosition)) {
			final Map<String, String> wordFeature = result.get(wordPosition);
			wordFeature.put(letter + i, "NIL");
			wordFeature.put(letter + i + "Pos", "NIL");
			wordFeature.put(letter + i + "Ner", "NIL");
			return wordFeature;
		} else {
			final Map<String, String> list = result.get(neighborPosition);
			final Map<String, String> wordFeature = result.get(wordPosition);
			wordFeature.put(letter + i, list.get("word"));
			wordFeature.put(letter + i + "Pos", list.get("POS"));
			wordFeature.put(letter + i + "Ner", list.get("NER"));
			return wordFeature;
		}

	}

	/**
	 * Parse result of the Stanford CoreNLP and extract features
	 * 
	 * @param xml
	 * @return
	 */
	public static Map<Integer, Map<String, String>> nerXmlParser(final String xml) {
		try {
			boolean isBeginning = true;
			Map<Integer, Map<String, String>> result = new LinkedHashMap<>();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final org.w3c.dom.Document document = docBuilder
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			final Map<String, String> features = new LinkedHashMap<>();
			final NodeList nodeList = document.getElementsByTagName("*");
			int wordPosition = 0;
			int wordCounter = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("token")) {
					if (node.hasChildNodes()) {
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);

							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								if (childNode.getNodeName().equals("word")) {
									features.put("ID", String.valueOf(wordCounter++));
									features.put("word", childNode.getTextContent());
									if(isBeginning) {
										features.put("START_OF_SENTENCE", "true");
										isBeginning=false;
									}else {
										features.put("START_OF_SENTENCE", "false");
									}
									if (childNode.getTextContent().charAt(0) >= 65
											&& childNode.getTextContent().charAt(0) <= 90) {
										features.put("STARTCAP", "true");
									} else {
										features.put("STARTCAP", "false");
									}
									if (StringUtils.isAllUpperCase(childNode.getTextContent())) {
										features.put("ALLCAP", "true");
									} else {
										features.put("ALLCAP", "false");
									}
								} else if (childNode.getNodeName().equals("lemma")) {
									features.put("lemma", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("POS")) {
									features.put("POS", childNode.getTextContent());
								} else if (childNode.getNodeName().equals("NER")) {
									features.put("NER", childNode.getTextContent());
								}
							}
						}
					}
					final Map<String, String> map = new LinkedHashMap<>();
					map.putAll(features);
					result.put(wordPosition++, map);
					features.clear();
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
