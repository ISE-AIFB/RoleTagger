package ise.roletagger.model;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.LexedTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;

/**
 * This class models wikipedia files.
 * Each file may contain multiple Documents
 * @author fbm
 *
 */
public class Document {
	private final String location;
	private final String title;
	private final List<String> content;
	private final List<String> sentences;

	public Document(String title, List<String> content,String location) {
		this.title = title;
		this.content = content;
		this.sentences = generateSentences();
		this.location = location;
	}

	private List<String> generateSentences() {
		final ArrayList<String> sentenceList = new ArrayList<String>();
		for (String contentLine : content) {
			final LexedTokenFactory<CoreLabel> tokenFactory = new CoreLabelTokenFactory();
			final List<CoreLabel> tokens = new ArrayList<CoreLabel>();
			final PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(contentLine.trim()),
					tokenFactory, "untokenizable=noneDelete");

			while (tokenizer.hasNext()) {
				tokens.add(tokenizer.next());
			}

			final List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>().process(tokens);
			int end =0;
			int start = 0;
			
			for (List<CoreLabel> sentence : sentences) {
				end = sentence.get(sentence.size() - 1).endPosition();
				sentenceList.add(contentLine.substring(start, end).trim());
				start = end;
			}
		}
		return sentenceList;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getContent() {
		return content;
	}

	public List<String> getSentences() {
		return sentences;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "Document [location=" + location + ", title=" + title + ", content=" + content + ", sentences="
				+ sentences + "]";
	}

}
