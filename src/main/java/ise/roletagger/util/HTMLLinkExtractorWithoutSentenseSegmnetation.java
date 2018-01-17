package ise.roletagger.util;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ise.roletagger.model.HtmlLink;


public class HTMLLinkExtractorWithoutSentenseSegmnetation {

	private Pattern patternTag, patternLink;
	private Matcher matcherTag, matcherLink;

	private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN =
			"\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


	public HTMLLinkExtractorWithoutSentenseSegmnetation() {
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	}

	public static String cleanAnchorTexts(String sentenceString) {
		return sentenceString.replaceAll("<[^>]*>", "");
	}
	/**
	 * Validate html with regular expression
	 *
	 * @param html
	 *            html content for validation
	 * @return Vector links and link text
	 */
	public Vector<HtmlLink> grabHTMLLinks(final String sentenceString) {
		Vector<HtmlLink> result = new Vector<HtmlLink>();
		final String sentenceWithoutHtmlTag = sentenceString.replaceAll("<[^>]*>", "");
		matcherTag = patternTag.matcher(sentenceString);

		while (matcherTag.find()) {

			String href = matcherTag.group(1); // href
			String linkText = matcherTag.group(2); // link text

			matcherLink = patternLink.matcher(href);

			while (matcherLink.find()) {

				String link = matcherLink.group(1); // link
				HtmlLink obj = new HtmlLink();
				obj.setUrl(link);
				obj.setAnchorText(linkText);
				obj.setFullSentence(sentenceWithoutHtmlTag);
				obj.setPostion(matcherTag.start()+href.length()+3,matcherTag.end()-4);
				result.add(obj);
			}
		}
		return result;
	}
	
	public static String removeAHrefTags(String text) {
		return text.replaceAll("<a.*?>", "").replaceAll("</a>", "");
	}
}


