package ise.roletagger.datasetconvertor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ise.roletagger.model.Category;
import ise.roletagger.model.Global;
import ise.roletagger.util.CharactersUtils;

public class TagParser {

	/**
	 * Used to put full sentence without any tag in the map
	 * This value will be used in {@link ConvertDatasetToFeatureSetForCRFSuite}
	 */
	private static final String NO_TAG = "noTag";
	private static final Pattern headRolepattern = Pattern.compile(Global.getHeadRoleStartTag()+"(.+?)"+Global.getHeadRoleEndTag());
	private static final Pattern rolePhrasePattern = Pattern.compile(Global.getRolePhraseStartTag("").substring(0,14)+"(.+?)'>");
	private static final Pattern anchorTextPattern = Pattern.compile(Global.getAnchorStartTag()+"(.+?)"+Global.getAnchorEndTag());
	
	
	private static final Pattern headRolepatternStart = Pattern.compile(Global.getHeadRoleStartTag());
	private static final Pattern headRolepatternEnd= Pattern.compile(Global.getHeadRoleEndTag());
	private static final Pattern rolePhrasePatternStart = Pattern.compile(Global.getRolePhraseStartTag("").substring(0,14)+"(.+?)'>");
	private static final Pattern rolePhrasePatternEnd= Pattern.compile(Global.getRolePhraseEndTag());
	
	/**
	 * This function is buggy
	 * I need to change all the If s with while here
	 * But as I don't know what is the reason of this function I don't change it now
	 * @param text
	 * @return
	 */
	public static Map<String,Map<String,Category>> parse(String text) {
		String sentence = new String(text);

		final Map<String,Map<String,Category>> tagData = new HashMap<>();
		Matcher matcher = headRolepattern.matcher(sentence);
		if(matcher.find()) {
			final Map<String,Category> map = new HashMap<>();
			map.put(matcher.group(1).trim(), null);
			tagData.put(Global.getHeadRoleEndTag(), map);
		}
		sentence=sentence.replace(Global.getHeadRoleStartTag(), "").replace(Global.getHeadRoleEndTag(), "");
		matcher = rolePhrasePattern.matcher(sentence);
		Category category = null; 
		if(matcher.find()) {
			category = Category.resolveWithCategoryName(matcher.group(1));
		}
		Pattern pattern = null;
		if(category!=null) {
			pattern = Pattern.compile(Global.getRolePhraseStartTag("").substring(0,14)+category.name()+"'>(.+?)"+Global.getRolePhraseEndTag());
		}else {
			pattern = Pattern.compile(Global.getRolePhraseStartTag("").substring(0,14)+"null'>(.+?)"+Global.getRolePhraseEndTag());
		}
		matcher = pattern.matcher(sentence);
		if(matcher.find()) {
			final Map<String,Category> map = new HashMap<>();
			map.put(matcher.group(1), category);
			tagData.put(Global.getRolePhraseEndTag(), map);
		}
		sentence=Global.removeRolePhraseTag(sentence);
		
		matcher = anchorTextPattern.matcher(sentence);
		if(matcher.find()) {
			final Map<String,Category> map = new HashMap<>();
			map.put(matcher.group(1), null);
			tagData.put(Global.getAnchorEndTag(), map);
		}
		
		sentence=Global.removeAnchorTextTag(sentence);

		final Map<String,Category> map = new HashMap<>();
		map.put(sentence, null);
		tagData.put(NO_TAG, map);

		return tagData;
	}
	
	/**
	 * I use this function to be able to normalize a sentence
	 * and keep the tags again in the place
	 * @param sentence
	 * @return
	 */
	public static Map<String,String> replaceAlltheTagsWithRandomString(String sentence) {
		final Map<String,String> result = new HashMap<>();
		Matcher matcher = headRolepatternStart.matcher(sentence);
		if(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			result.put(saltString, matcher.group(0));
			sentence = sentence.replace(Global.getHeadRoleStartTag(),saltString);
		}
		
		matcher = headRolepatternEnd.matcher(sentence);
		if(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			result.put(saltString, matcher.group(0));
			sentence = sentence.replace(Global.getHeadRoleEndTag(),saltString);
		}
		
		matcher = rolePhrasePatternEnd.matcher(sentence);
		if(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			result.put(saltString, matcher.group(0));
			sentence = sentence.replace(Global.getRolePhraseEndTag(),saltString);
		}
		
		matcher = rolePhrasePatternStart.matcher(sentence);
		while(matcher.find()) {
			final String saltString = " "+CharactersUtils.getSaltString()+" ";
			result.put(saltString, matcher.group(0));
			sentence = sentence.replace(matcher.group(0),saltString);
		}
		
		result.put("noTag", sentence);
		return result;
	}
	
}
