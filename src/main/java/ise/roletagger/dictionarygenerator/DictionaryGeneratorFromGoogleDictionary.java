package ise.roletagger.dictionarygenerator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Entity;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.util.DictionaryRegexPatterns;
import ise.roletagger.util.EntityFileLoader;
import ise.roletagger.util.FileUtil;
import ise.roletagger.util.URLUTF8Encoder;

/**
 * This class is responsible for extracting important entries from google dictionary
 * We will add extracted entries to our dictionary
 * @author fbm
 *
 */
public class DictionaryGeneratorFromGoogleDictionary {
	private static Map<String, Entity> entityMap;
	private static Map<Category, List<Pattern>> headRolePatterns = new HashMap<>();
	/**
	 * Reads already calculated dictionary of roles from folder "dictionary/manually
	 * cleaned"
	 */
	private static RoleListProvider dictionaries;

	private static final Map<String, Set<Category>> newDictionary= new LinkedHashMap<>();
	
	private static final List<String> rawNewDictionaryWithUrls = new ArrayList<>();
	
	public static void main(String[] args) {
		dictionaries = DictionaryRegexPatterns.getDictionaries();
		final List<Pattern> headCeoPattrensList = new ArrayList<>();
		final List<Pattern> headMonarchPattrensList = new ArrayList<>();
		final List<Pattern> headPresidentPattrensList = new ArrayList<>();
		final List<Pattern> headPopePattrensList = new ArrayList<>();

		for (Entry<Category, LinkedHashSet<String>> roleEntity : dictionaries.getHeadRoleMap().entrySet()) {
			switch (roleEntity.getKey()) {
			case CHAIR_PERSON_TAG:
				for (String role : roleEntity.getValue()) {
					headCeoPattrensList.add(Pattern.compile("(?im)" + "\\b" + role + "\\b"));
				}
				break;
			case MONARCH_TAG:
				for (String role : roleEntity.getValue()) {
					headMonarchPattrensList.add(Pattern.compile("(?im)" + "\\b" + role + "\\b"));
				}
				break;
			case POPE_TAG:
				for (String role : roleEntity.getValue()) {
					headPopePattrensList.add(Pattern.compile("(?im)" + "\\b" + role + "\\b"));
				}
				break;
			case HEAD_OF_STATE_TAG:
				for (String role : roleEntity.getValue()) {
					headPresidentPattrensList.add(Pattern.compile("(?im)" + "\\b" + role + "\\b"));
				}
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		headRolePatterns.put(Category.CHAIR_PERSON_TAG, headCeoPattrensList);
		headRolePatterns.put(Category.HEAD_OF_STATE_TAG, headPresidentPattrensList);
		headRolePatterns.put(Category.MONARCH_TAG, headMonarchPattrensList);
		headRolePatterns.put(Category.POPE_TAG, headPopePattrensList);
		System.out.println("Loading seeds(list of persons, wikidata)....");
		entityMap = EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_TILTES, null);
		entityMap.putAll(EntityFileLoader.loadData(DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL, null));
		//int oldSize =0;
		try {
			FileInputStream fin = new FileInputStream("/home/fbm/eclipse-workspace/General Data/dictionary.bz2");
			BufferedInputStream bis = new BufferedInputStream(fin);
			CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
			String sCurrentLine;
			while ((sCurrentLine = br2.readLine()) != null)
			{
				final String[] split = sCurrentLine.split("\t");
				final String anchorText = split[0];
				float probability = Float.parseFloat(split[1].split(" ")[0]);
				String url = split[1].split(" ")[1];
				url = URLUTF8Encoder.encodeJavaNative(url);
				final Entity entity = entityMap.get(url);
				if(entity!=null) {
					if(containsHeadRole(entity,anchorText)) {
						if(probability>0.2) {
							rawNewDictionaryWithUrls.add(anchorText+"\t"+entity.getEntityName()+"\t"+probability);
							final Set<Category> set = newDictionary.get(anchorText);
							if(set!=null) {
								set.add(entity.getCategoryFolder());
								newDictionary.put(anchorText, set);
							}else {
								Set<Category> newSet = new HashSet<>();
								newSet.add(entity.getCategoryFolder());
								newDictionary.put(anchorText, newSet);
							}
						}
					}
				}
//				if(newDictionary.size()!=oldSize) {
//					System.out.println("Size of new Dictioanry: "+newDictionary.size());
//					oldSize =newDictionary.size();
//				}
			}
			//FileUtil.writeDataToFile(new ArrayList<>(newDictionary.keySet()), "newDictionary.txt",false);
			FileUtil.writeDataToFile(rawNewDictionaryWithUrls, "newDictionaryRawWithUrls.txt",false);
			System.err.println("Size of new Dictioanry: "+newDictionary.size());
			System.err.println("Size of raw new Dictioanry: "+rawNewDictionaryWithUrls.size());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean containsHeadRole(Entity entity, String anchorText) {
		Matcher matcher1 = null;
		List<Pattern> localHeadrolePatterns = null;
		switch (entity.getCategoryFolder()) {
		case CHAIR_PERSON_TAG:
			localHeadrolePatterns = headRolePatterns.get(Category.CHAIR_PERSON_TAG);
			break;
		case HEAD_OF_STATE_TAG:
			localHeadrolePatterns = headRolePatterns.get(Category.HEAD_OF_STATE_TAG);
			break;
		case MONARCH_TAG:
			localHeadrolePatterns = headRolePatterns.get(Category.MONARCH_TAG);
			break;
		case POPE_TAG:
			localHeadrolePatterns = headRolePatterns.get(Category.POPE_TAG);
			break;
		default:
			throw new IllegalArgumentException("Entity category is no matching with our exising categories");
		}
		for (Pattern p : localHeadrolePatterns) {
			matcher1 = p.matcher(anchorText);
			if (matcher1.find()) {
				return true;
			}
		}
		return false;
	}

}
