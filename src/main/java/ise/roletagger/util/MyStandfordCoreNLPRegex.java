package ise.roletagger.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.util.CoreMap;
import ise.roletagger.datasetconvertor.TagParser;
import ise.roletagger.model.Category;
import ise.roletagger.model.Token;
import nu.xom.Document;

public class MyStandfordCoreNLPRegex {

	private final static Properties props_MONARCH_TAG;
	private final static Properties props_POPE_TAG;
	private final static Properties props_CHAIR_PERSON_TAG;
	private final static Properties props_HEAD_OF_STATE_TAG;
	private final static Properties props_ALL_AS_ROLE;
	private final static StanfordCoreNLP pipeline_MONARCH_TAG;
	private final static StanfordCoreNLP pipeline_POPE_TAG;
	private final static StanfordCoreNLP pipeline_CHAIR_PEROSN_TAG;
	private final static StanfordCoreNLP pipeline_HEAD_OF_STATE_TAG;
	private final static StanfordCoreNLP pipeline_ALL_AS_ROLE;

	private static final String REGEXNER_ADDRESS = Config.getString("WHERE_TO_WRITE_REGEXNER", "");
	static {
		props_MONARCH_TAG = new Properties();
		props_MONARCH_TAG.put("annotators", "tokenize, ssplit, pos, lemma, regexner");
		props_MONARCH_TAG.put("regexner.ignorecase", "true");
		props_MONARCH_TAG.setProperty("untokenizable","noneDelete");
		props_MONARCH_TAG.put("regexner.mapping", REGEXNER_ADDRESS + File.separator + "regexNerMONARCH_TAG.txt");
		pipeline_MONARCH_TAG = new StanfordCoreNLP(props_MONARCH_TAG);

		props_POPE_TAG = new Properties();
		props_POPE_TAG.put("annotators", "tokenize, ssplit, pos, lemma, regexner");
		props_POPE_TAG.put("regexner.ignorecase", "true");
		props_POPE_TAG.setProperty("untokenizable","noneDelete");
		props_POPE_TAG.put("regexner.mapping", REGEXNER_ADDRESS + File.separator + "regexNerPOPE_TAG.txt");
		pipeline_POPE_TAG = new StanfordCoreNLP(props_POPE_TAG);

		props_CHAIR_PERSON_TAG = new Properties();
		props_CHAIR_PERSON_TAG.put("annotators", "tokenize, ssplit, pos, lemma, regexner");
		props_CHAIR_PERSON_TAG.put("regexner.ignorecase", "true");
		props_CHAIR_PERSON_TAG.setProperty("untokenizable","noneDelete");
		props_CHAIR_PERSON_TAG.put("regexner.mapping",
				REGEXNER_ADDRESS + File.separator + "regexNerCHAIR_PERSON_TAG.txt");
		pipeline_CHAIR_PEROSN_TAG = new StanfordCoreNLP(props_CHAIR_PERSON_TAG);

		props_HEAD_OF_STATE_TAG = new Properties();
		props_HEAD_OF_STATE_TAG.put("annotators", "tokenize, ssplit, pos, lemma, regexner");
		props_HEAD_OF_STATE_TAG.put("regexner.ignorecase", "true");
		props_HEAD_OF_STATE_TAG.setProperty("untokenizable","noneDelete");
		props_HEAD_OF_STATE_TAG.put("regexner.mapping",
				REGEXNER_ADDRESS + File.separator + "regexNerHEAD_OF_STATE_TAG.txt");
		pipeline_HEAD_OF_STATE_TAG = new StanfordCoreNLP(props_HEAD_OF_STATE_TAG);

		props_ALL_AS_ROLE = new Properties();
		props_ALL_AS_ROLE.put("annotators", "tokenize, ssplit, pos, lemma, regexner");
		props_ALL_AS_ROLE.put("regexner.ignorecase", "true");
		props_ALL_AS_ROLE.setProperty("untokenizable","noneDelete");		
		props_ALL_AS_ROLE.put("regexner.mapping", REGEXNER_ADDRESS + File.separator + "regexNer.txt");
		pipeline_ALL_AS_ROLE = new StanfordCoreNLP(props_ALL_AS_ROLE);
	}

	public static void main(String[] args) {
	}

	/**
	 * Normalize given sentence
	 * - Convert umlaut
	 * - Remove Special Characters and numbers
	 * @param input
	 * @return
	 */
	public static String normzalize(String input) {
		String result = new String(input);
		
		/**
		 * use for result of folder 1 - dataset - only anchortext - only important page
		 */
//		result = CharactersUtils.convertUmlaut(result);
//		result = CharactersUtils.removeGenitiveS(result);
//		result = CharactersUtils.removeSpeicalCharactersForEvaluationAdnDatasetGeneration(result);		
		
		/**
		 * use for result of folder ......
		 */
		result = CharactersUtils.convertUmlaut(result);
		result = CharactersUtils.removeGenitiveS(result);
		result = CharactersUtils.removeSpeicalCharactersForEvaluationAdnDatasetGeneration(result);
		
		//result = result.replace("'", "");
		//result = result.replaceAll("[^\\w\\s]", " ").replaceAll("\\s+"," ").trim();
		//result = result.toLowerCase();
		
		
		return result;
	}

	/**
	 * get a sentence and run this pipeline on it. Currently it only return ROLEs
	 * 
	 * @param text
	 * @param category
	 * @return
	 */
	public static List<Token> run(String text, Category category) {
		final Annotation annotation = new Annotation(text);
		switch (category) {
		case CHAIR_PERSON_TAG:
			pipeline_CHAIR_PEROSN_TAG.annotate(annotation);
			final nu.xom.Document doc1 = XMLOutputter.annotationToDoc(annotation, pipeline_MONARCH_TAG);
			final List<Token> nerXmlParser1 = nerXmlParser(doc1.toXML());
			return nerXmlParser1;
		case POPE_TAG:
			pipeline_POPE_TAG.annotate(annotation);
			final nu.xom.Document doc2 = XMLOutputter.annotationToDoc(annotation, pipeline_MONARCH_TAG);
			final List<Token> nerXmlParser2 = nerXmlParser(doc2.toXML());
			return nerXmlParser2;
		case HEAD_OF_STATE_TAG:
			pipeline_HEAD_OF_STATE_TAG.annotate(annotation);
			final nu.xom.Document doc3 = XMLOutputter.annotationToDoc(annotation, pipeline_MONARCH_TAG);
			final List<Token> nerXmlParser3 = nerXmlParser(doc3.toXML());
			return nerXmlParser3;
		case MONARCH_TAG:
			pipeline_MONARCH_TAG.annotate(annotation);
			final nu.xom.Document doc4 = XMLOutputter.annotationToDoc(annotation, pipeline_MONARCH_TAG);
			final List<Token> nerXmlParser4 = nerXmlParser(doc4.toXML());
			return nerXmlParser4;
		default:
			return null;
		}
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

	public static List<Token> aggregateNerTagPositions(final List<Token> tokens, Category tag) {
		final List<Token> result = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			final Token t = tokens.get(i);
			final String possibleNerTag = t.getContent().get("NER");
			if (possibleNerTag != null && Category.resolveWithCategoryName(possibleNerTag) != null
					&& Category.resolveWithCategoryName(possibleNerTag).equals(tag)) {
				Token newToken = new Token();
				newToken.getContent().putAll(t.getContent());
				for (int j = i + 1; j < tokens.size(); j++, i++) {
					final Token tt = tokens.get(j);
					final String possibleNerTag2 = tt.getContent().get("NER");
					if (possibleNerTag2 != null && Category.resolveWithCategoryName(possibleNerTag2) != null
							&& Category.resolveWithCategoryName(possibleNerTag2).equals(tag)) {
						newToken.getContent().put("word",
								newToken.getContent().get("word") + " " + tt.getContent().get("word"));
						newToken.getContent().put("CharacterOffsetEnd", tt.getContent().get("CharacterOffsetEnd"));
						newToken.getContent().put("POS", null);
						newToken.getContent().put("lemma", null);
					} else {
						break;
					}
				}
				newToken.getContent().put("NER", tag.text());
				result.add(newToken);
			}
		}
		return result;
	}

	public static List<Token> aggregateNerTagPositions(final List<Token> tokens) {
		final List<Token> result = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			final Token t = tokens.get(i);
			final String possibleNerTag = t.getContent().get("NER");
			if (possibleNerTag != null && Category.resolveWithCategoryName(possibleNerTag) != null) {
				Token newToken = new Token();
				newToken.getContent().putAll(t.getContent());
				for (int j = i + 1; j < tokens.size(); j++, i++) {
					final Token tt = tokens.get(j);
					final String possibleNerTag2 = tt.getContent().get("NER");
					if (possibleNerTag2 != null && Category.resolveWithCategoryName(possibleNerTag2) != null
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
			}
		}
		return result;
	}

	public static List<Token> containNer(List<Token> tokens, NER_TAG tag) {
		final List<Token> result = new ArrayList<>();
		for (Token t : tokens) {
			final String possibleNerTag = t.getContent().get("NER");
			if (possibleNerTag != null && NER_TAG.resolve(possibleNerTag) != null
					&& NER_TAG.resolve(possibleNerTag).equals(tag)) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * Run pipeline over given text
	 * @param anchorText
	 * @return
	 */
	public static List<Token> run(String anchorText) {
		final Annotation annotation = new Annotation(anchorText);
		pipeline_ALL_AS_ROLE.annotate(annotation);
		final nu.xom.Document doc = XMLOutputter.annotationToDoc(annotation, pipeline_ALL_AS_ROLE);
		final List<Token> nerXmlParser = nerXmlParser(doc.toXML());
		return nerXmlParser;
	}
	
	/**
	 * Run NER on plain text and returns full XML
	 * 
	 * @param text
	 * @return
	 */
	public static String runNerTaggerXML(String text) {
		final Annotation document = new Annotation(text);
		pipeline_ALL_AS_ROLE.annotate(document);
		final Document doc = XMLOutputter.annotationToDoc(document, pipeline_ALL_AS_ROLE);
		return doc.toXML();
	}

	/**
	 * Converts a sentence to lemma version
	 * this function tries to preserve the capital cases
	 * @param input
	 * @return
	 */
	public static String convertToLemma(String input) {
		StringBuilder rootStr = new StringBuilder();
		Annotation document = pipeline_ALL_AS_ROLE.process(input);
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				if (lemma.equalsIgnoreCase(word)) {
					rootStr.append(word + " ");
				} else {
					rootStr.append(lemma + " ");
				}
			}
		}
		return rootStr.toString().trim();
	}
	
	public static String normalizeAndConvertToLemma(String input) {
		final String normzalized = normzalize(input);
		final String convertToLemma = convertToLemma(normzalized);
		return convertToLemma;		
//		return input;
	}
	
	/**
	 * this function get a tagged sentence and normalize it
	 * and return again normalized tagged sentence 
	 * @param taggedLine
	 * @return
	 */
	public static String normalizeTaggedLine(String taggedLine) {
		final Map<String, String> parsedDataWithPlaceHolder = TagParser.replaceAlltheTagsWithRandomString(taggedLine);
		String taggedWithRandomString =  parsedDataWithPlaceHolder.get("noTag");
		taggedWithRandomString = normalizeAndConvertToLemma(taggedWithRandomString);
		
		for(Entry<String, String> e:parsedDataWithPlaceHolder.entrySet()) {
			taggedWithRandomString = taggedWithRandomString.replace(e.getKey().trim(), e.getValue());
		}
		
		taggedWithRandomString = taggedWithRandomString.replace("<HR> ", "<HR>");
		taggedWithRandomString = taggedWithRandomString.replace(" </HR>", "</HR>");
		taggedWithRandomString = taggedWithRandomString.replace(" </RP>", "</RP>");
		
		taggedWithRandomString = taggedWithRandomString.replace("<RP Category='HEAD_OF_STATE_TAG'> ", "<RP Category='HEAD_OF_STATE_TAG'>");
		taggedWithRandomString = taggedWithRandomString.replace("<RP Category='CHAIR_PERSON_TAG'> ", "<RP Category='CHAIR_PERSON_TAG'>");
		taggedWithRandomString = taggedWithRandomString.replace("<RP Category='MONARCH_TAG'> ", "<RP Category='MONARCH_TAG'>");
		taggedWithRandomString = taggedWithRandomString.replace("<RP Category='POPE_TAG'> ", "<RP Category='POPE_TAG'>");
		
		return taggedWithRandomString;
	}

}
