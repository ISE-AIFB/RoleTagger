package ise.roletagger.dictionarygenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ise.roletagger.model.Category;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.util.DictionaryRegexPatterns;
import ise.roletagger.util.FileUtil;
import ise.roletagger.util.MyStanfordCoreNLP;
import ise.roletagger.util.NER_TAG;

/**
 * This class tries to combine entities in dictionary to make longer entity
 * for example
 * - Former President
 * - President of US
 * ==> Former president of US
 * 
 * It connects last word of one entity to beginning of another entity
 * However, it produces a lot of wrong entities:
 * - US President
 * - President of US
 * ==> Us President of US
 *  
 * @author fbm
 *
 */
public class DictionaryAugmentation {

	/**
	 * Reads already calculated dictionary of roles from folder "dictionary/manually
	 * cleaned"
	 */
	private static RoleListProvider dictionaries;

	public static void main(String[] args) {
		Thread t = new Thread (new DictionaryAugmentation().run());
		t.setDaemon(false);
		t.start();
	}

	public Runnable run() {
		return () -> {
			dictionaries = DictionaryRegexPatterns.getDictionaries();
			final Set<String> allAugmentedRoles = new HashSet<>(); 
			for(Entry<Category, LinkedHashSet<String>> entry:dictionaries.getInverseData().entrySet()) {
				for(String role:entry.getValue()){
					List<String> augmentedRoles = augment(role,entry.getValue());
					if(!augmentedRoles.isEmpty()) {
						allAugmentedRoles.addAll(augmentedRoles);
						//augmentedRoles.forEach(System.out::println);
					}
				}
			}
			
			System.err.println("total augmented size: "+allAugmentedRoles.size());
			FileUtil.writeDataToFile(new ArrayList<String>(allAugmentedRoles), "AugmentedRolesBoth.txt", false);
		};
	}

	private List<String> augment(String role, LinkedHashSet<String> value) {
		final List<String> result = new ArrayList<>();
		final String[] split = role.split(" ");
		if(split.length<=1) {
			return result;
		}
		final String lastWord = split[split.length-1];
		for(String otherRole:value) {
			final String[] otherWordSplit = otherRole.split(" ");
			if(otherWordSplit.length<=1) {
				continue;
			}
			final String otherRoleFirstWord = otherWordSplit[0];
			if(lastWord.equals(otherRoleFirstWord)) {
				final StringBuilder s = new StringBuilder(role);
				for(int k=1;k<otherWordSplit.length;k++)
				{
					s.append(" ").append(otherWordSplit[k]);
				}
				String candicate = s.toString().trim();
				if(isValid(candicate)) {
					result.add(candicate);
				}
			}else if(lastWord.equalsIgnoreCase(otherRoleFirstWord)) {
				final StringBuilder s = new StringBuilder(role);
				for(int k=1;k<otherWordSplit.length;k++)
				{
					s.append(" ").append(otherWordSplit[k]);
				}
				String candicate = s.toString().trim();
				if(isValid(candicate)) {
					result.add(candicate);
				}
				
				final StringBuilder s2 = new StringBuilder();
				for(int k=0;k<split.length-1;k++)
				{
					s2.append(split[k]).append(" ");
				}
				s2.append(otherRole.toString());
				candicate = s2.toString().trim();
				if(isValid(candicate)) {
					result.add(candicate);
				}
			}
		}
		return result;
	}

	/**
	 * This function checks to see if a new candidate is a valid one or not
	 * For example if a candidate contains multiple <LOC> after NER, I remove it
	 * Or if whole candidate after NER is selected I remove it 
	 * @param candicate
	 * @return
	 */
	private boolean isValid(String candicate) {
		final String nerResult = MyStanfordCoreNLP.runNerTaggerString(candicate, NER_TAG.LOCATION);
		if(StringUtils.countMatches(nerResult,NER_TAG.LOCATION.text)<=1) {
			if(nerResult.charAt(0)=='<' && nerResult.charAt(nerResult.length()-1)=='>') {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
}
