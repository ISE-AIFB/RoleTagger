package ise.roletagger.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;

import ise.roletagger.datasetgenerator.RoleListProviderFileBased;
import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.RoleListProvider;

public class DictionaryRegexPatterns {
	/**
	 * Contains mapping between a category and all the roles that this category has
	 * from dictionary. Every role goes to a new regex pattern to be able to select
	 * the longest match
	 */
	private static final Map<Category, List<Pattern>> categoryToRolePatterns = new HashedMap<>();
	/**
	 * Contains mapping between a category and all the roles in that category
	 */
	private static final Map<Category, Set<String>> categoryToRoles = new HashedMap<>();

	/**
	 * contains all the role from dictionary.Every role goes to a new regex pattern
	 * to be able to select the longest match
	 */
	private static final List<Pattern> rolePatterns = new ArrayList<>();
	private static final Map<Category, List<Pattern>> headRolePatterns = new HashMap<>();
	
	/**
	 * Reads already calculated dictionary of roles from folder "dictionary/manually
	 * cleaned"
	 */
	private static final RoleListProvider dictionaries = new RoleListProviderFileBased();
	
	static {
		
		dictionaries.loadRoles(DataSourceType.WIKIPEDIA_LIST_OF_TILTES);
		dictionaries.loadRoles(DataSourceType.WIKIDATA_LABEL);
		
		final List<Pattern> ceoPattrensList = new ArrayList<>();
		final List<Pattern> monarchPattrensList = new ArrayList<>();
		final List<Pattern> presidentPattrensList = new ArrayList<>();
		final List<Pattern> popePattrensList = new ArrayList<>();

		final Set<String> ceoRolesList = new HashSet<>();
		final Set<String> monarchRolesList = new HashSet<>();
		final Set<String> presidentRolesList = new HashSet<>();
		final Set<String> popeRolesList = new HashSet<>();
		
		for(Entry<String, Set<Category>> roleEntity:dictionaries.getData().entrySet()) {
			rolePatterns.add(Pattern.compile("(?im)" + "(?<!-|\\.)\\b" + Pattern.quote(roleEntity.getKey()) + "\\b(?!-|\\.)"));
		}
		
		for (Entry<Category, LinkedHashSet<String>> roleEntity : dictionaries.getInverseData().entrySet()) {
			switch (roleEntity.getKey()) {
			case CHAIR_PERSON_TAG:
				for (String role : roleEntity.getValue()) {
					ceoPattrensList.add(Pattern.compile("(?im)(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
					ceoRolesList.add(role);
				}
				break;
			case MONARCH_TAG:
				for (String role : roleEntity.getValue()) {
					monarchPattrensList.add(Pattern.compile("(?im)(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
					monarchRolesList.add(role);
				}
				break;
			case POPE_TAG:
				for (String role : roleEntity.getValue()) {
					popePattrensList.add(Pattern.compile("(?im)(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
					popeRolesList.add(role);
				}
				break;
			case HEAD_OF_STATE_TAG:
				for (String role : roleEntity.getValue()) {
					presidentPattrensList.add(Pattern.compile("(?im)(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
					presidentRolesList.add(role);
				}
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		
		categoryToRolePatterns.put(Category.CHAIR_PERSON_TAG, ceoPattrensList);
		categoryToRolePatterns.put(Category.HEAD_OF_STATE_TAG, presidentPattrensList);
		categoryToRolePatterns.put(Category.MONARCH_TAG, monarchPattrensList);
		categoryToRolePatterns.put(Category.POPE_TAG, popePattrensList);
		
		categoryToRoles.put(Category.CHAIR_PERSON_TAG, ceoRolesList);
		categoryToRoles.put(Category.HEAD_OF_STATE_TAG, presidentRolesList);
		categoryToRoles.put(Category.MONARCH_TAG, monarchRolesList);
		categoryToRoles.put(Category.POPE_TAG, popeRolesList);

		final List<Pattern> headCeoPattrensList = new ArrayList<>();
		final List<Pattern> headMonarchPattrensList = new ArrayList<>();
		final List<Pattern> headPresidentPattrensList = new ArrayList<>();
		final List<Pattern> headPopePattrensList = new ArrayList<>();

		for (Entry<Category, LinkedHashSet<String>> roleEntity : dictionaries.getHeadRoleMap().entrySet()) {
			switch (roleEntity.getKey()) {
			case CHAIR_PERSON_TAG:
				for (String role : roleEntity.getValue()) {
					headCeoPattrensList.add(Pattern.compile("(?im)" + "(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
				}
				break;
			case MONARCH_TAG:
				for (String role : roleEntity.getValue()) {
					headMonarchPattrensList.add(Pattern.compile("(?im)" + "(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
				}
				break;
			case POPE_TAG:
				for (String role : roleEntity.getValue()) {
					headPopePattrensList.add(Pattern.compile("(?im)" + "(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
				}
				break;
			case HEAD_OF_STATE_TAG:
				for (String role : roleEntity.getValue()) {
					headPresidentPattrensList.add(Pattern.compile("(?im)" + "(?<!-|\\.)\\b" + Pattern.quote(role) + "\\b(?!-|\\.)"));
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
	}

	public static Map<Category, List<Pattern>> getCategoryToRolePatterns() {
		return categoryToRolePatterns;
	}

	public static List<Pattern> getRolePatterns() {
		return rolePatterns;
	}

	public static Map<Category, List<Pattern>> getHeadRolePatterns() {
		return headRolePatterns;
	}

	public static RoleListProvider getDictionaries() {
		return dictionaries;
	}

	public static Map<Category, Set<String>> getCategoryToRoles() {
		return categoryToRoles;
	}
	
}
