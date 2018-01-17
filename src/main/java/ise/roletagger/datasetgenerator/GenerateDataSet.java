package ise.roletagger.datasetgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ise.roletagger.model.Category;
import ise.roletagger.model.CategoryTrees;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Dataset;
import ise.roletagger.model.Document;
import ise.roletagger.model.Entity;
import ise.roletagger.model.Global;
import ise.roletagger.model.HtmlLink;
import ise.roletagger.model.Position;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.util.CharactersUtils;
import ise.roletagger.util.Config;
import ise.roletagger.util.DictionaryRegexPatterns;
import ise.roletagger.util.EntityFileLoader;
import ise.roletagger.util.FileUtil;
import ise.roletagger.util.HTMLLinkExtractorWithoutSentenseSegmnetation;

public class GenerateDataSet {

	private static final AtomicInteger EASY_NAGATIVE_COUNTER = new AtomicInteger(0);
	private static final int MAX_NUMBER_OF_EASY_NEGATIVE_SAMPLES = 1000000;
	/**
	 * Sentences more than this number of href will be ignored. This way I would
	 * like to ignore lists
	 */
	private static final int MAX_NUMBRE_OF_HREF_TO_CONSIDER = 10;
	private static final String WHERE_TO_WRITE_DATASET = Config.getString("WHERE_TO_WRITE_DATASET", "");
	/**
	 * This contains the positive and negative samples
	 */
	private static final Dataset DATASET = new Dataset();
	/**
	 * This folder contains all the wikipedia pages which are already cleaned by a
	 * python code from https://github.com/attardi/wikiextractor and contains the
	 * links and anchor text
	 */
	private static final String WIKI_FILES_FOLDER = Config.getString("WIKI_FILES_FOLDER", "");
	/**
	 * This file is a dump which contains relation between each entity and its
	 * category entity dbp:subject category
	 */
	private static final String ENTITY_CATEGORY_FILE = Config.getString("ENTITY_CATEGORY_FILE", "");
	/**
	 * This folder files related to category trees which are already calculated as a
	 * preprocess by my another project named "CategoryTreeGeneration"
	 * https://github.com/fmoghaddam/CategoryTreeGeneration
	 */
	private static final String CATEGORY_TREE_FOLDER = Config.getString("CATEGORY_TREE_FOLDER_CLEAN", "");
	/**
	 * Number of thread for parallelization
	 */
	private static final int NUMBER_OF_THREADS = Config.getInt("NUMBER_OF_THREADS", 0);
	/**
	 * Contains mapping between urls and entities. It will be loaded based on
	 * "data/entities" folder These are the seed as input list (persons, titles,...)
	 */
	private static Map<String, Entity> entityMap;
	private static ExecutorService executor;
	/**
	 * Contains mapping between entities and their category It uses dbpedia dump. It
	 * uses dct:subject
	 */
	private static EntityToListOfCategories entityToCategoryList;
	/**
	 * Contains category treeS
	 */
	private static final CategoryTrees categoryTrees = new CategoryTrees();

	/**
	 * Contains mapping between a category and all the roles that this category has
	 * from dictionary. Every role goes to a new regex pattern to be able to select
	 * the longest match
	 */
	private static Map<Category, List<Pattern>> categoryToRolePatterns;

	private static RoleListProvider dictionaries;
	private static final String REDIRECT_FILE = Config.getString("REDIRECT_PAGE_ADDRESS", "");

	private static final Map<String, Integer> usedEntityFromDictionary = new ConcurrentHashMap<>();

	private static final Map<String, String> redirectPages = new HashMap<>();

	public static void main(String[] args) {
		Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	private static void loadRedirectPages() {
		try {
			final BufferedReader br = new BufferedReader(
					new FileReader(REDIRECT_FILE));
			String line;

			while ((line = br.readLine()) != null) {
				if (line == null || line.isEmpty()) {
					continue;
				}
				final String[] data = line.split("\t");
				redirectPages.put(data[0], data[1]);
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static Runnable run() {
		return () -> {
			System.out.println("Loading skos category trees....");
			categoryTrees.load(CATEGORY_TREE_FOLDER);

			executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

			System.out.println("Loading seeds(list of persons, wikidata)....");
			entityMap = EntityFileLoader.loadData(DataSourceType.WIKIDATA_LIST_OF_PRESON, null);
			entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_TILTES, null));

			dictionaries = DictionaryRegexPatterns.getDictionaries();
			categoryToRolePatterns = DictionaryRegexPatterns.getCategoryToRolePatterns();

			System.out.println("Extracting mapping between entites and categories....");
			entityToCategoryList = new EntityToListOfCategories(ENTITY_CATEGORY_FILE);
			entityToCategoryList.parse();

			System.out.println("Loading redirect pages ... ");
			loadRedirectPages();
			
			System.out.println("Start....");
			checkWikiPages();			
		};
	}

	private static void checkWikiPages() {
		try {
			final File[] listOfFolders = new File(WIKI_FILES_FOLDER).listFiles();
			Arrays.sort(listOfFolders);
			for (int i = 0; i < listOfFolders.length; i++) {
				final String subFolder = listOfFolders[i].getName();
				final File[] listOfFiles = new File(WIKI_FILES_FOLDER + File.separator + subFolder + File.separator)
						.listFiles();
				Arrays.sort(listOfFiles);
				for (int j = 0; j < listOfFiles.length; j++) {
					final String file = listOfFiles[j].getName();
					executor.execute(handle(
							WIKI_FILES_FOLDER + File.separator + subFolder + File.separator + File.separator + file));
				}
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			FileUtil.deleteFolder(WHERE_TO_WRITE_DATASET);
			DATASET.printPositiveDataset();
			DATASET.printNegativeDatasetDifficult();
			DATASET.printPoitiveNegativeDifficultDataset();
			DATASET.printNegativeDatasetEasy();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	private static void printDictionaryUsageStatisitcs() {
		System.err.println("TOTAL DIC SIZE CASE SENSITIVE= " + dictionaries.getData().size());
		System.err.println("TOTAL DIC SIZE CASE INSENSITIVE= " + dictionaries.getData().keySet().size());
		System.err.println("USED ENTITIES FROM DIC = " + usedEntityFromDictionary.size());
		System.err.println("PERCENTAGE Case Sensitive= "
				+ (usedEntityFromDictionary.size() * 100.) / (dictionaries.getData().size()) + "%");
		System.err.println("PERCENTAGE Case INSensitive= "
				+ (usedEntityFromDictionary.size() * 100.) / (dictionaries.getData().keySet().size()) + "%");
	}

	private static Runnable handle(String pathToSubFolder) {
		final Runnable r = () -> {
			try {
				final List<Document> documents = getDocuments(pathToSubFolder);
				for (final Document document : documents) {
					final Entity importantPageEntity = isImportantPage(document.getTitle());
					if (importantPageEntity != null) {
						checkAnchorTextAndNormalSentence(document, importantPageEntity);
					} else {
						final Entity relatedPageEntity = isRelatedPage(document.getTitle());
						if (relatedPageEntity != null) {
							checkAnchorTextAndNormalSentence(document, relatedPageEntity);
						} else {
							checkOnlyAnchorText(document);
						}
					}
				}
				System.out.println("File " + pathToSubFolder + " has been processed.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		return r;
	}

	private static void checkOnlyAnchorText(Document document) {
		for (final String line : document.getSentences()) {
			if (StringUtils.countMatches(line, "<a href") > MAX_NUMBRE_OF_HREF_TO_CONSIDER) {
				continue;
			} else if(line.contains("<a href")) {
				handleOnlyAnchorText(line);
			} else {
				if(EASY_NAGATIVE_COUNTER.get()<MAX_NUMBER_OF_EASY_NEGATIVE_SAMPLES) {
					if(line.length()>100) {
						String l = normalizeTaggedSentence(line);
						final String result = giveMeTaggedAnchorTextIfItContainsRole(l,null);
						if(!result.contains(Global.getHeadRoleEndTag())) {
							DATASET.addNegativeEasyData(l);
							EASY_NAGATIVE_COUNTER.incrementAndGet();
						}
					}
				}
			}
		}

	}

	private static void handleOnlyAnchorText(String line) {
		line = normalizeTaggedSentence(line);
		
		final HTMLLinkExtractorWithoutSentenseSegmnetation htmlLinkExtractor = new HTMLLinkExtractorWithoutSentenseSegmnetation();
		final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);

		final Set<Boolean> decisionCase = new HashSet<>();
		StringBuilder originalSentence = new StringBuilder(line);
		int offset =0;
		for (final Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
			final HtmlLink htmlLink = (HtmlLink) iterator.next();
			String url = htmlLink.getDecodedUrl();
			String anchorText = htmlLink.getAnchorText();
			Position position = new Position(htmlLink.getStart(),htmlLink.getEnd());

			String redirectUrl = redirectPages.get(url)==null?url:redirectPages.get(url);
			
			final Entity entity = isImportantPage(redirectUrl);

			if (entity != null) {
				final String taggedAnchorText = giveMeTaggedAnchorTextIfItContainsRole(anchorText,entity);
				if(taggedAnchorText.contains(Global.getHeadRoleStartTag())) {
					originalSentence.replace(position.getStartIndex()+offset,position.getEndIndex()+offset, taggedAnchorText);
					offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
					decisionCase.add(true);
				}
			} else {
				final Entity isRelatedEntity = isRelatedPage(redirectUrl);
				if (isRelatedEntity == null) {
					final String taggedAnchorText = giveMeTaggedAnchorTextIfItContainsRole(anchorText,entity);
					if(taggedAnchorText.contains(Global.getHeadRoleStartTag())) {
						originalSentence.replace(position.getStartIndex()+offset,position.getEndIndex()+offset, taggedAnchorText);
						offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
						decisionCase.add(false);
					}
				}
			}
		}

		originalSentence = new StringBuilder(HTMLLinkExtractorWithoutSentenseSegmnetation.removeAHrefTags(originalSentence.toString()));

		if (decisionCase.size() == 1) {
			if (decisionCase.contains(true)) {
				DATASET.addOnlyPositiveSentence(originalSentence.toString());
			} else {
				DATASET.addOnlyDifficultNegativeSentence(originalSentence.toString());
			}
		} else if (decisionCase.size() == 2) {
			// Means sentence contain positive and negative roles
			// Ignore
			DATASET.addPositiveNegativeSentence(originalSentence.toString());
		} else {
			// Means sentence contain positive and negative roles
			// Ignore
		}
	}

	private static void checkAnchorTextAndNormalSentence(Document document, Entity importantPageEntity) {
		for (String line : document.getSentences()) {
			if (StringUtils.countMatches(line, "<a href") > MAX_NUMBRE_OF_HREF_TO_CONSIDER) {
				continue;
			} else {
				if(line.contains("<a hre")){
					handleAnchorTextAndNormalSentence(line, importantPageEntity);
				}else {
					handleOnlyNormalSentence(line, importantPageEntity);
				}
			}
		}
	}

	private static void handleOnlyNormalSentence(String line, Entity importantPageEntity) {
		line = normalizeTaggedSentence(line);
		final String result = giveMeTaggedAnchorTextIfItContainsRole(line,importantPageEntity);
		if(result.contains(Global.getHeadRoleEndTag())) {
			DATASET.addOnlyPositiveSentence(result);
		}
	}

	private static void handleAnchorTextAndNormalSentence(String line, Entity importantPageEntity) {
		line = normalizeTaggedSentence(line); 
		
		final HTMLLinkExtractorWithoutSentenseSegmnetation htmlLinkExtractor = new HTMLLinkExtractorWithoutSentenseSegmnetation();
		final Vector<HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(line);

		final Set<Boolean> decisionCase = new HashSet<>();
		StringBuilder originalSentence = new StringBuilder(line);
		int offset =0;
		for (final Iterator<?> iterator = links.iterator(); iterator.hasNext();) {
			final HtmlLink htmlLink = (HtmlLink) iterator.next();
			String url = htmlLink.getDecodedUrl();
			String anchorText = htmlLink.getAnchorText();
			Position position = new Position(htmlLink.getStart(),htmlLink.getEnd());

			String redirectUrl = redirectPages.get(url)==null?url:redirectPages.get(url);
			final Entity entity = isImportantPage(redirectUrl);

			if (entity != null) {
				final String taggedAnchorText = giveMeTaggedAnchorTextIfItContainsRole(anchorText,entity);
				if(taggedAnchorText.contains(Global.getHeadRoleStartTag())) {
					originalSentence.replace(position.getStartIndex()+offset,position.getEndIndex()+offset, taggedAnchorText);
					offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
					decisionCase.add(true);
				}
			} else {
				final Entity isRelatedEntity = isRelatedPage(redirectUrl);
				if (isRelatedEntity == null) {
					final String taggedAnchorText = giveMeTaggedAnchorTextIfItContainsRole(anchorText,entity);
					if(taggedAnchorText.contains(Global.getHeadRoleStartTag())) {
						originalSentence.replace(position.getStartIndex()+offset,position.getEndIndex()+offset, taggedAnchorText);
						offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
						decisionCase.add(false);
					}
				}
			}
		}

		originalSentence = new StringBuilder(HTMLLinkExtractorWithoutSentenseSegmnetation.removeAHrefTags(originalSentence.toString()));

		if (decisionCase.size() == 1) {
			if (decisionCase.contains(true)) {
				originalSentence  = new StringBuilder(giveMeTaggedAnchorTextIfItContainsRole(originalSentence.toString(),importantPageEntity));
				originalSentence = new StringBuilder(originalSentence.toString().replaceAll("("+Global.getHeadRoleStartTag()+")+", Global.getHeadRoleStartTag()).replaceAll("("+Global.getHeadRoleEndTag()+")+", Global.getHeadRoleEndTag()));
				DATASET.addOnlyPositiveSentence(originalSentence.toString());
			} else {
				DATASET.addOnlyDifficultNegativeSentence(originalSentence.toString());
			}
		} else if (decisionCase.size() == 2) {
			// Means sentence contain positive and negative roles
			// Ignore
			DATASET.addPositiveNegativeSentence(originalSentence.toString());
		} else {
			// Means sentence contain positive and negative roles
			// Ignore
		}
	}

	public static String normalizeTaggedSentence(String line) {
		StringBuilder resultLine = new StringBuilder(line);
		final Map<String,String> result = new HashMap<>();
		Pattern p = Pattern.compile("</?a.*?>");
		Matcher matcher = p.matcher(line);
		int offset = 0;
		while(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			resultLine.replace(matcher.start()+offset, matcher.end()+offset, saltString);
			offset+= saltString.length()-(matcher.end()-matcher.start());
			result.put(saltString, matcher.group(0));
		}
		
		resultLine = new StringBuilder(CharactersUtils.normalizeTrainSentence(resultLine.toString()));
		
		for(Entry<String, String> e:result.entrySet()) {
			resultLine = new StringBuilder(resultLine.toString().replace(e.getKey(), e.getValue()));
		}
		
		resultLine = new StringBuilder(resultLine.toString().replaceAll("\\s+"," ").trim());
		return resultLine.toString();
	}

	public static String giveMeTaggedAnchorTextIfItContainsRole(String anchorText, Entity entity) {
		if(entity!=null) {
			List<Pattern> rolePatterns = null;
			StringBuilder result = new  StringBuilder(anchorText);
			switch (entity.getCategoryFolder()) {
			case CHAIR_PERSON_TAG:
				rolePatterns = categoryToRolePatterns.get(Category.CHAIR_PERSON_TAG);
				break;
			case HEAD_OF_STATE_TAG:
				rolePatterns = categoryToRolePatterns.get(Category.HEAD_OF_STATE_TAG);
				break;
			case MONARCH_TAG:
				rolePatterns = categoryToRolePatterns.get(Category.MONARCH_TAG);
				break;
			case POPE_TAG:
				rolePatterns = categoryToRolePatterns.get(Category.POPE_TAG);
				break;
			default:
				throw new IllegalArgumentException("Entity category is no matching with our exising categories");
			}
			Matcher matcher = null;
			for (Pattern pattern : rolePatterns) {
				matcher = pattern.matcher(anchorText);
				boolean found = false;
				int offset= 0;
				while (matcher.find()) {
					String foundText = matcher.group();
					int end = matcher.end();
					int start = matcher.start();
					result= result.replace(start+offset, end+offset,
							Global.getHeadRoleStartTag() + foundText + Global.getHeadRoleEndTag());
					found = true;
					offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
				}
				if (found) {
					return result.toString();
				}
			}
			return result.toString();	
		}else {
			StringBuilder result = new  StringBuilder(anchorText);
			for(Entry<Category, List<Pattern>> patternEntity:categoryToRolePatterns.entrySet()) {
				boolean localFound = false;
				for (Pattern pattern : categoryToRolePatterns.get(patternEntity.getKey())) {
					Matcher matcher = pattern.matcher(anchorText);
					int offset= 0;
					while (matcher.find()) {
						String foundText = matcher.group();
						int end = matcher.end();
						int start = matcher.start();
						result = result.replace(start+offset, end+offset,
								Global.getHeadRoleStartTag() + foundText + Global.getHeadRoleEndTag());
						localFound = true;
						offset += Global.getHeadRoleStartTag().length()+Global.getHeadRoleEndTag().length();
					}
					if (localFound) {
						return result.toString();
					}
				}
			}
			return result.toString();
		}
	}

	private static Entity isRelatedPage(String url) {
		final Set<String> categoriesOfEntity = entityToCategoryList.getEntity2categories().get(url);
		if (categoriesOfEntity == null) {
			return null;
		} else {
			boolean isImportantPage = false;
			Category existInAnyTree = null;
			for (String cat1 : categoriesOfEntity) {
				existInAnyTree = categoryTrees.existInAnyTree(cat1);
				if (existInAnyTree != null) {
					isImportantPage = true;
					break;
				}
			}
			if (isImportantPage) {
				return new Entity(null, null, null, existInAnyTree);
			} else {
				return null;
			}
		}
	}

	private static Entity isImportantPage(String url) {
		final Entity entity = entityMap.get(url);
		if (entity == null) {
			return null;
		} else {
			return entity;
		}
	}

	public static List<Document> getDocuments(String pathTofile) {
		final List<Document> result = new ArrayList<>();
		final Pattern titlePattern = Pattern.compile("<doc.* url=\".*\" title=\".*\">");
		try {
			final List<String> lines = Files.readAllLines(Paths.get(pathTofile), StandardCharsets.UTF_8);
			String title = "";
			final List<String> content = new ArrayList<>();
			for (int i = 0; i < lines.size(); i++) {
				final String line = lines.get(i);
				if (line.isEmpty()) {
					continue;
				}
				final Matcher titleMatcher = titlePattern.matcher(line);
				if (titleMatcher.find()) {
					content.clear();
					title = lines.get(++i);
					continue;
				} else if (line.equals("</doc>")) {
					final Document d = new Document(title.replace(" ", "_"), content, pathTofile);
					result.add(d);
				} else {
					content.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
