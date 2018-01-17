package ise.roletagger.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global {

	private static final String ROLE_PHRASE_STASRT_TAG = "<RP Category='XXXX'>";
	private static final String ROLE_PHRASE_END_TAG = "</RP>";
	private static final String HEAD_ROLE_START_TAG = "<HR>";
	private static final String HEAD_ROLE_END_TAG = "</HR>";
	private static final String ANCHOR_START_TAG = "<a>";
	private static final String ANCHOR_END_TAG = "</a>";
	private static final Pattern pattern = Pattern.compile(Global.getRolePhraseStartTag("").substring(0,14)+"(.+?)'>");
	public static final boolean USE_NORMALIZATION = true;
	
	public static String getRolePhraseStartTag(String catName) {
		return new String(ROLE_PHRASE_STASRT_TAG.replace("XXXX",catName));
	}
	public static String getRolePhraseEndTag() {
		return new String(ROLE_PHRASE_END_TAG);
	}
	public static String getHeadRoleStartTag() {
		return new String(HEAD_ROLE_START_TAG);
	}
	public static String getHeadRoleEndTag() {
		return new String(HEAD_ROLE_END_TAG);
	}
	public static String getAnchorStartTag() {
		return new String(ANCHOR_START_TAG);
	}
	public static String getAnchorEndTag() {
		return new String(ANCHOR_END_TAG);
	}
	public static String removeRolePhraseTag(String text) {
		return text.replaceAll(Global.getRolePhraseStartTag("").substring(0,14)+"(.+?)'>", "").replace(Global.getRolePhraseEndTag(), ""); 
	}
	public static Category getcategoryFromRolePhrase(String text) {
		final Matcher matcher = pattern.matcher(text);
		Category category = null; 
		if(matcher.find()) {
			category = Category.resolveWithCategoryName(matcher.group(1));
		}
		return category;
	}
	public static String removeAnchorTextTag(String text) {
		return text.replaceAll(Global.getAnchorEndTag(), "").replaceAll(Global.getAnchorStartTag(), "");
	}
	public static String removeHeadRoleTag(String text) {
		return text.replaceAll(Global.getHeadRoleStartTag(), "").replaceAll(Global.getHeadRoleEndTag(), "");
	}
}
