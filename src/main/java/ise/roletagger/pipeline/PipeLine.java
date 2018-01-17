package ise.roletagger.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import ise.roletagger.categorytreegeneration.CategoryTreeMainGenerator;
import ise.roletagger.categorytreegeneration.CategoryTreePostProccessing;
import ise.roletagger.datasetconvertor.ConvertDatasetToFeatureSetForCRFSuite;
import ise.roletagger.datasetgenerator.GenerateDataSet;
import ise.roletagger.datasetuniquefier.DatasetUniqefier;
import ise.roletagger.dictionarygenerator.DictionaryGenerator;
import ise.roletagger.entitygenerator.RunRequest;
import ise.roletagger.entitygenerator.RunRequestOnlyPersons;
import ise.roletagger.regexner.ConvertDictionaryToRegexNer;

public class PipeLine {

	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	
	public static void main(String[] args) {
		
		try {

			/**Entity generation & Wikidata dictionary generation**/
			queue.put(RunRequest.run());
			
			/**Entity generation**/
			queue.put(RunRequestOnlyPersons.run());
			
			/**Dictionary Generation**/
			queue.put(DictionaryGenerator.run());
			
			/**Category Tree Generation**/
			queue.put(CategoryTreeMainGenerator.run());
			queue.put(new CategoryTreePostProccessing().run());
			
			/**Dictionary to regexNER**/
			queue.put(ConvertDictionaryToRegexNer.run());
			
			/**Dataset Generation**/
			queue.put(GenerateDataSet.run());
			
			/**Dataset uniquefier*/
			queue.put(DatasetUniqefier.run());
			
			/**Dataset to feature set for CRF**/
			queue.put(new ConvertDatasetToFeatureSetForCRFSuite(0).run());
			

			while(!queue.isEmpty()) {
				executor.submit(queue.take());
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}


