package ise.roletagger.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.XMLOutputter;
import nu.xom.Document;

public class CustomNERTagger {
	private final static Properties props;
	private final static StanfordCoreNLP pipeline;

	static {
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.model","/home/fbm/eclipse-workspace/RoleTaggerWorkFlow/stanfordmodels/farshad-model-goooood.ser.gz");
		
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * Run NER on plain text and returns full XML
	 * 
	 * @param text
	 * @return
	 */
	public static String runTaggerXML(String text) {
		final Annotation document = new Annotation(text);
		pipeline.annotate(document);
		final Document doc = XMLOutputter.annotationToDoc(document, pipeline);
		return doc.toXML();
	}

	/**
	 * Parse XML result of {@code runTaggerXML} function and returns
	 * aggregated and filter statistic 
	 * @param xml
	 * @return
	 */
	public static Map<Integer, NerTag> nerXmlParser(final String xml) {
		try {
			Map<Integer, NerTag> result = new LinkedHashMap<>();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final org.w3c.dom.Document document = docBuilder
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

			final NodeList nodeList = document.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && isATokenTag(node)) {

					String word = null;
					String nerTag = null;
					int startPosition = 0;
					int endPosition = 0;

					if (node.hasChildNodes()) {
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);

							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								if (childNode.getNodeName().equals("word")) {
									word = childNode.getTextContent();
								} else if (childNode.getNodeName().equals("NER")) {
									nerTag = childNode.getTextContent();
								} else if (childNode.getNodeName().equals("CharacterOffsetBegin")) {
									startPosition = Integer.parseInt(childNode.getTextContent());
								} else if (childNode.getNodeName().equals("CharacterOffsetEnd")) {
									endPosition = Integer.parseInt(childNode.getTextContent());
								}
							}
						}
					}
					result.put(startPosition, new NerTag(word, NER_TAG.resolve(nerTag), startPosition, endPosition));
				}
			}
			result = aggregateTagPositions(result);
			result = filterOnlyValidNerTags(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Filter the map and just keep NER tag
	 * @param inputMap
	 * @return
	 */
	private static Map<Integer, NerTag> filterOnlyValidNerTags(Map<Integer, NerTag> inputMap) {
		final Map<Integer, NerTag> result = new LinkedHashMap<>();
		for (Entry<Integer, NerTag> entry : inputMap.entrySet()) {
			if (entry.getValue().getNerTag() != null && NER_TAG.resolve(entry.getValue().getNerTag().text) != null) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Aggregate tag position
	 * e.g. Farshad Moghaddam ==> <PERSON> <PERSON> ==> <PERSON>
	 * @param inputMap
	 * @return
	 */
	private static Map<Integer, NerTag> aggregateTagPositions(final Map<Integer, NerTag> inputMap) {
		final Map<Integer, NerTag> result = new LinkedHashMap<>();
		final List<NerTag> tags = new ArrayList<>(inputMap.values());
		for (int i = 0; i < tags.size(); i++) {
			final NerTag nerTag = tags.get(i);
			StringBuilder words = new StringBuilder(nerTag.getWord());
			int updatedEndposition = nerTag.getEndPosition();
			final NER_TAG tag = nerTag.getNerTag();

			for (int j = i+1; j < tags.size() && tag == tags.get(j).getNerTag(); j++, i++) {
				final NerTag nextNerTag = tags.get(j);
				updatedEndposition = nextNerTag.getEndPosition();
				int endOfFirstTag = tags.get(i).getEndPosition();
				while(endOfFirstTag<nextNerTag.getStartPosition()){
					words.append(" ");
					endOfFirstTag++;
				}
				words.append(nextNerTag.getWord());
			}
			result.put(nerTag.getStartPosition(),
					new NerTag(words.toString(), nerTag.getNerTag(), nerTag.getStartPosition(), updatedEndposition));
		}

		return result;
	}

	/**
	 * Only used for parsing Stanford CoreNlp XML result
	 * @param node
	 * @return
	 */
	private static boolean isATokenTag(Node node) {
		if (node.getNodeName().equals("token")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Run NER tagger on a plain text and return aggregated tagged string
	 * @param plainText
	 * @return
	 */
	public static String runTaggerString(String plainText) {
		StringBuilder result = new StringBuilder(plainText); 
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(runTaggerXML(plainText));
		int offset = 0;
		for(NerTag tag:nerXmlParser.values()){ 
			result.replace(tag.getStartPosition()+offset, tag.getEndPosition()+offset, "<"+tag.getNerTag().text+">");
			int diff = tag.getEndPosition()-tag.getStartPosition();
			int tagLength = 2+tag.getNerTag().text.length();
			if(diff>=tagLength){
				offset -= Math.abs(diff-tagLength);
			}else{
				offset += Math.abs(diff-tagLength);
			}

		}
		return result.toString();
	}

	public static String runTaggerStringWithoutHeadRoleReplacement(String plainText, Set<String> headRoles) {
		StringBuilder result = new StringBuilder(plainText); 
		final Map<Integer, NerTag> nerXmlParser = nerXmlParserWithoutHeadRoleReplacement(runTaggerXML(plainText),headRoles);
		int offset = 0;
		for(NerTag tag:nerXmlParser.values()){
			result.replace(tag.getStartPosition()+offset, tag.getEndPosition()+offset, "<"+tag.getNerTag().text+">");
			int diff = tag.getEndPosition()-tag.getStartPosition();
			int tagLength = 2+tag.getNerTag().text.length();
			if(diff>=tagLength){
				offset -= Math.abs(diff-tagLength);
			}else{
				offset += Math.abs(diff-tagLength);
			}

		}
		return result.toString();
	}

	public static Map<Integer, NerTag> nerXmlParserWithoutHeadRoleReplacement(String xml, Set<String> headRoles) {
		try {
			Map<Integer, NerTag> result = new LinkedHashMap<>();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final org.w3c.dom.Document document = docBuilder
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

			final NodeList nodeList = document.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				boolean doNotConsiderFlag = false;
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && isATokenTag(node)) {

					String word = null;
					String nerTag = null;
					int startPosition = 0;
					int endPosition = 0;

					if (node.hasChildNodes()) {
						for (int j = 0; j < node.getChildNodes().getLength(); j++) {
							final Node childNode = node.getChildNodes().item(j);

							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								if (childNode.getNodeName().equals("word")) {
									word = childNode.getTextContent();
									if(headRoles.contains(word)){
										doNotConsiderFlag = true;
										break;
									}
								} else if (childNode.getNodeName().equals("NER")) {
									nerTag = childNode.getTextContent();
								} else if (childNode.getNodeName().equals("CharacterOffsetBegin")) {
									startPosition = Integer.parseInt(childNode.getTextContent());
								} else if (childNode.getNodeName().equals("CharacterOffsetEnd")) {
									endPosition = Integer.parseInt(childNode.getTextContent());
								}
							}
						}
					}
					if(!doNotConsiderFlag){
						result.put(startPosition, new NerTag(word, NER_TAG.resolve(nerTag), startPosition, endPosition));
					}
				}
			}
			result = aggregateTagPositions(result);
			result = filterOnlyValidNerTags(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
