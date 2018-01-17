package ise.roletagger.util;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;
import ise.roletagger.model.Category;
import ise.roletagger.model.NounPhrase;
import ise.roletagger.model.TagPosition;
import ise.roletagger.model.TagPositions;
import nu.xom.Document;

public class MyStanfordCoreNLP {
	private final static Properties props;
	private final static StanfordCoreNLP pipeline;
	private final static TokenizerFactory<Word> TF;
	static {
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse");
		props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
		props.setProperty("untokenizable","noneDelete");
		pipeline = new StanfordCoreNLP(props);
		TF = PTBTokenizer.factory();
	}

	/**
	 * Run NER on plain text and returns full XML
	 * 
	 * @param text
	 * @return
	 */
	public static String runNerTaggerXML(String text) {
		final Annotation document = new Annotation(text);
		pipeline.annotate(document);
		final Document doc = XMLOutputter.annotationToDoc(document, pipeline);
		return doc.toXML();
	}

	/**
	 * Parse XML result of {@code runTaggerXML} function and returns aggregated and
	 * filter statistic
	 * 
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
				if (node.getNodeType() == Node.ELEMENT_NODE && isATokenNerTag(node)) {

					String word = null;
					String nerTag = null;
					int startPosition = 0;
					int endPosition = 0;
					Category normalizedNER=null;
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
								} else if (childNode.getNodeName().equals("NormalizedNER")) {
									normalizedNER = Category.resolveWithCategoryName(childNode.getTextContent());
								}
								
							}
						}
					}
					final NerTag tag = new NerTag(word, NER_TAG.resolve(nerTag), startPosition, endPosition);
					if(normalizedNER!=null) {
						tag.setNomalizedNER(normalizedNER);
					}
					result.put(startPosition, tag);
				}
			}
			result = aggregateNerTagPositions(result);
			result = filterOnlyValidNerTags(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Filter the map and just keep NER tag
	 * 
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
	 * Aggregate tag position e.g. Farshad Moghaddam ==> <PERSON> <PERSON> ==>
	 * <PERSON>
	 * 
	 * @param inputMap
	 * @return
	 */
	private static Map<Integer, NerTag> aggregateNerTagPositions(final Map<Integer, NerTag> inputMap) {
		final Map<Integer, NerTag> result = new LinkedHashMap<>();
		final List<NerTag> tags = new ArrayList<>(inputMap.values());
		for (int i = 0; i < tags.size(); i++) {
			final NerTag nerTag = tags.get(i);
			StringBuilder words = new StringBuilder(nerTag.getWord());
			int updatedEndposition = nerTag.getEndPosition();
			final NER_TAG tag = nerTag.getNerTag();

			for (int j = i + 1; j < tags.size() && tag == tags.get(j).getNerTag(); j++, i++) {
				final NerTag nextNerTag = tags.get(j);
				updatedEndposition = nextNerTag.getEndPosition();
				int endOfFirstTag = tags.get(i).getEndPosition();
				while (endOfFirstTag < nextNerTag.getStartPosition()) {
					words.append(" ");
					endOfFirstTag++;
				}
				words.append(nextNerTag.getWord());
			}
			final NerTag nerTag2 = new NerTag(words.toString(), nerTag.getNerTag(), nerTag.getStartPosition(), updatedEndposition);
			nerTag2.setNomalizedNER(nerTag.getNomalizedNER());
			result.put(nerTag.getStartPosition(),
					nerTag2);
		}

		return result;
	}

	/**
	 * Only used for parsing Stanford CoreNlp XML result
	 * 
	 * @param node
	 * @return
	 */
	private static boolean isATokenNerTag(Node node) {
		if (node.getNodeName().equals("token")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Run NER tagger on a plain text and return aggregated tagged string
	 * 
	 * @param plainText
	 * @return
	 */
	public static String runNerTaggerString(String plainText) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(runNerTaggerXML(plainText));
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
					"<" + tag.getNerTag().text + ">");
			int diff = tag.getEndPosition() - tag.getStartPosition();
			int tagLength = 2 + tag.getNerTag().text.length();
			if (diff >= tagLength) {
				offset -= Math.abs(diff - tagLength);
			} else {
				offset += Math.abs(diff - tagLength);
			}

		}
		return result.toString();
	}


	/**
	 * 
	 * @param plainText
	 * @return
	 */
	public static String runRoleTaggerString(String plainText,String xml) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(xml);
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			if (tag.getNerTag() == NER_TAG.ROLE) {
				result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
						"<" + tag.getNerTag().text + ">");
				int diff = tag.getEndPosition() - tag.getStartPosition();
				int tagLength = 2 + tag.getNerTag().text.length();
				if (diff >= tagLength) {
					offset -= Math.abs(diff - tagLength);
				} else {
					offset += Math.abs(diff - tagLength);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Run NER tagger on a plain text for a specific pattern and return aggregated
	 * tagged string
	 * 
	 * @param plainText
	 * @param tag
	 *            specific tag for example PERSON or LOCATION
	 * @return
	 */
	public static String runNerTaggerString(String plainText, NER_TAG nerTag) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(runNerTaggerXML(plainText));
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			if (tag.getNerTag() == nerTag) {
				result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
						"<" + tag.getNerTag().text + ">");
				int diff = tag.getEndPosition() - tag.getStartPosition();
				int tagLength = 2 + tag.getNerTag().text.length();
				if (diff >= tagLength) {
					offset -= Math.abs(diff - tagLength);
				} else {
					offset += Math.abs(diff - tagLength);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Run NER tagger on a plain text for a specific pattern and return aggregated
	 * tagged string
	 * 
	 * @param plainText
	 * @param tag
	 *            specific tag for example PERSON or LOCATION
	 * @return
	 */
	public static String runNerTaggerString(String plainText, List<NER_TAG> nerTags) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(runNerTaggerXML(plainText));
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			if (nerTags.contains(tag.getNerTag())) {
				result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
						"<" + tag.getNerTag().text + ">");
				int diff = tag.getEndPosition() - tag.getStartPosition();
				int tagLength = 2 + tag.getNerTag().text.length();
				if (diff >= tagLength) {
					offset -= Math.abs(diff - tagLength);
				} else {
					offset += Math.abs(diff - tagLength);
				}
			}
		}
		return result.toString();
	}

	public static String runNerTaggerStringWithoutHeadRoleReplacement(String plainText, Set<String> headRoles) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParserWithoutHeadRoleReplacement(runNerTaggerXML(plainText),
				headRoles);
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
					"<" + tag.getNerTag().text + ">");
			int diff = tag.getEndPosition() - tag.getStartPosition();
			int tagLength = 2 + tag.getNerTag().text.length();
			if (diff >= tagLength) {
				offset -= Math.abs(diff - tagLength);
			} else {
				offset += Math.abs(diff - tagLength);
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
				if (node.getNodeType() == Node.ELEMENT_NODE && isATokenNerTag(node)) {

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
									if (headRoles.contains(word)) {
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
					if (!doNotConsiderFlag) {
						result.put(startPosition,
								new NerTag(word, NER_TAG.resolve(nerTag), startPosition, endPosition));
					}
				}
			}
			result = aggregateNerTagPositions(result);
			result = filterOnlyValidNerTags(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Tokenize the sentence to Words
	 * @param sentence
	 * @return
	 */
	public static List<Word> tokenize(final String sentence){
		return TF.getTokenizer(new StringReader(sentence)).tokenize();
	}

	/**
	 * Returns all the HighLevel NounPhrases. 
	 * High level means the longest NounPhrase
	 * @param text
	 * @return
	 */
	public static List<NounPhrase> getAllHighLevelNounPhrases(String text) {
		final List<NounPhrase> result = new ArrayList<>();
		final Annotation document = new Annotation(text);
		pipeline.annotate(document);

		final CoreMap firstSentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		final Tree parseTree = firstSentence.get(TreeCoreAnnotations.TreeAnnotation.class);

		final TagPositions positions = new TagPositions();
		final TregexPattern NPpattern = TregexPattern.compile("NP");
		for (Tree tree : parseTree.subTreeList()) {
			final TregexMatcher matcher = NPpattern.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				final Tree match = matcher.getMatch();
				final ArrayList<Label> yield = match.yield();
				final String[] startSplit = yield.get(0).toString().split("-");
				final int startIndex = Integer.parseInt(startSplit[startSplit.length - 1])-1;
				final String[] endSplit = yield.get(yield.size() - 1).toString().split("-");
				final int endIndex = Integer.parseInt(endSplit[endSplit.length - 1])-1;
				final TagPosition position = new TagPosition(null, startIndex, endIndex);
				try {
					if (!positions.alreadyExist(position)) {
						positions.add(position);
						final StringBuilder sent = new StringBuilder();
						for (Tree localTree : match.getLeaves()) {
							if (localTree.value().contains("<a href=")) {
								sent.append(localTree);
							} else if (localTree.value().contains("</a>")) {
								if(!sent.toString().isEmpty()) {
									final String oldValue = sent.toString().trim();
									sent.delete(0, sent.length() - 1);
									sent.append(oldValue).append(localTree).append(" ");
								}else {
									sent.append(localTree).append(" ");
								}
							} else {
								if(localTree.toString().charAt(0)=='\'') {
									final String oldValue = sent.toString().trim();
									if(sent.length()>=1) {
										sent.delete(0, sent.length() - 1);
									}
									sent.append(oldValue).append(localTree).append(" ");
								}else {
									sent.append(localTree).append(" ");
								}
							}
						}
						result.add(new NounPhrase(sent.toString().trim(),position.getStartIndex(),position.getEndIndex()));
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * Finds a head of a NounPhrase
	 * @param text
	 */
	public static void headFinder(String text) {
		final Annotation document = new Annotation(text);
		pipeline.annotate(document);
		final CoreMap firstSentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		final Tree parseTree = firstSentence.get(TreeCoreAnnotations.TreeAnnotation.class);
		CollinsHeadFinder headFinder = new CollinsHeadFinder();
		dfs(parseTree, parseTree, headFinder);
	}

	public static void dfs(Tree node, Tree parent, HeadFinder headFinder) {
		if (node == null || node.isLeaf()) {
			return;
		}
		for(Tree child : node.children()) {
			if(child.value().equals("NP") ) {
				System.out.println("Noun Phrase is ");
				final List<Tree> leaves = node.getLeaves();
				for(final Tree leaf : leaves) {
					System.out.print(leaf.toString()+" ");
				}
				System.out.println();
				System.out.println("Head string is ");
				System.out.println(node.headTerminal(headFinder, parent));
				System.out.println("----------------");
			}
		}
	}

	/**
	 * Get a given XML and try to parse and aggregate it to a result string
	 * @param plainText
	 * @param XML
	 * @return
	 */
	public static String parseAndAggregateNerRegex(String plainText,String XML) {
		StringBuilder result = new StringBuilder(plainText);
		final Map<Integer, NerTag> nerXmlParser = nerXmlParser(XML);
		int offset = 0;
		for (NerTag tag : nerXmlParser.values()) {
			result.replace(tag.getStartPosition() + offset, tag.getEndPosition() + offset,
					"<" + tag.getNerTag().text + ">");
			int diff = tag.getEndPosition() - tag.getStartPosition();
			int tagLength = 2 + tag.getNerTag().text.length();
			if (diff >= tagLength) {
				offset -= Math.abs(diff - tagLength);
			} else {
				offset += Math.abs(diff - tagLength);
			}

		}
		return result.toString();
	}
}
