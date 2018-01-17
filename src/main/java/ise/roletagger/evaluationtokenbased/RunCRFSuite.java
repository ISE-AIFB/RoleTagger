package ise.roletagger.evaluationtokenbased;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ise.roletagger.datasetconvertor.SentenceToFeature;
import ise.roletagger.model.Tuple;
import ise.roletagger.util.FileUtil;

/**
 * Runs CRFSUite C Library inside java
 * @author fbm
 *
 */
public class RunCRFSuite {
	
	/**
	 * line is a normal sentence
	 * @param line
	 * @param crfModel
	 * @return
	 */
	public static List<Tuple> run(CRFModels crfModel,Map<Integer, Map<String, String>> result) {
		List<Tuple> runCRFTest = new ArrayList<>();
		try {
//			final String noTaggedLine = new String(line);
//			final Map<Integer, Map<String, String>> result = SentenceToFeature.convertPlainSentenceToFeatures(noTaggedLine);
//			
//			SentenceToFeature.addNegativeLabels(result);
			
			final List<String> localfinalResult = SentenceToFeature.featureMapToStringList(result);
			final File temp = File.createTempFile("abc", ".tmp");
			temp.deleteOnExit();
			FileUtil.writeDataToFile(localfinalResult, temp);
			runCRFTest = runCRF(temp,result,crfModel);
			temp.delete();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return runCRFTest;
	}
	
	private static List<Tuple> runCRF(File temp, Map<Integer, Map<String, String>> result, CRFModels crfModel) {
		final List<Tuple> tuples = new LinkedList<>();
		try {
			
			String command = "./crfsuite-0.12/bin/crfsuite tag -m crfsuite-0.12/bin/"+crfModel.text()+" ";
			final Process p = Runtime.getRuntime().exec(command+temp.getPath());
			final BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));
			int index = 0;
			String s;
			while ((s = stdInput.readLine()) != null) {
				final Map<String, String> map = result.get(index);
				if(map!=null) {
					final String word = map.get("word");
					Tuple t = new Tuple(word, s);
					tuples.add(t);
					index++;
				}
			}
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
			temp.delete();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return tuples;
	}
}
