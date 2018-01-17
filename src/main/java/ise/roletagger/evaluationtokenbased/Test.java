package ise.roletagger.evaluationtokenbased;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import ise.roletagger.model.Position;
import ise.roletagger.model.TagPosition;
import ise.roletagger.model.TagPositions;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.DictionaryRegexPatterns;

public class Test {

	public static void main(String[] args) {
		
//		String text = "he is <hr>pope</hr>'s mother";
//		System.err.println(RunCRFSuite.run(text, CRFModels.ONLY_HEAD_ROLE));
		
		List<Pattern> rolePatterns = DictionaryRegexPatterns.getRolePatterns();
		
		
		
		System.err.println(rolePatterns.size());
		
//		String t = "Commander in Chief. Therefore, the big question is this: How could Bill Clinton as President justify sending young men to war, for any reason, when he did everything possible not to go himself?";
//		final List<Tuple> runBaseLineDictionary = runBaseLineDictionary(t);
//		runBaseLineDictionary.forEach(p-> System.err.println(p));
	}

	public static List<Tuple> runBaseLineDictionary(String noTaggedLine) {
		final List<Tuple> result = new ArrayList<>();
		final TagPositions tagPositions = new TagPositions();
		List<Pattern> rolePatterns = DictionaryRegexPatterns.getRolePatterns();
		for (Pattern p : rolePatterns) {
			final Matcher matcher = p.matcher(noTaggedLine);
			while (matcher.find()) {
				String foundText = matcher.group();
				int start = matcher.start();
				int end = matcher.end();
				final TagPosition tp = new TagPosition(foundText, start, end);
				if (tagPositions.alreadyExist(tp)) {
					continue;
				}
				tagPositions.add(tp);
			}
		}

		final TokenizerFactory<Word> tf = PTBTokenizer.factory();		
		List<Word> tokens_words = null;
		try {
			tokens_words = tf.getTokenizer(new StringReader(noTaggedLine)).tokenize();
			for(Word w:tokens_words) {
				if(isDetected(tagPositions,w)) {
					result.add(new Tuple(w.value(), "ROLE"));
				}else {
					result.add(new Tuple(w.value(), "O"));
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static boolean isDetected(TagPositions tagPositions, Word w) {
		int start = w.beginPosition();
		int end = w.endPosition();
		
		for(int i=0;i<tagPositions.getPositions().size();i++) {
			if(tagPositions.getPositions().get(i).contains(new Position(start, end))) {
				return true;
			}
		}
		return false;
	}
}
