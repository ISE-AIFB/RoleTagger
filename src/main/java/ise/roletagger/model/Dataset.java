package ise.roletagger.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

public class Dataset {
	
	private final CopyOnWriteArrayList<String> positiveDataset = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDatasetDifficult = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDatasetEasy = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<String> negativeDatasetDifficultPlusPositive = new CopyOnWriteArrayList<>();
	
	
	private final Map<Category,Set<String>> positiveDatasetStatistic = new HashMap<>();
	private final Map<Category,Set<String>> negativeDatasetStatistic = new HashMap<>();
	
	private static final String WHERE_TO_WRITE_DATASET = Config.getString("WHERE_TO_WRITE_DATASET","");
	private static final String POSITIVE_DATASET_NAME  = Config.getString("POSITIVE_DATASET_NAME","");
	private static final String DIFFICULT_NEGATIVE_DATASET_NAME = Config.getString("DIFFICULT_NEGATIVE_DATASET_NAME","");
	private static final String EASY_NEGATIVE_DATASET_NAME = Config.getString("EASY_NEGATIVE_DATASET_NAME","");
	private static final String NEAGTIVE_POSITIVE_DATASET_NAME = Config.getString("NEAGTIVE_POSITIVE_DATASET_NAME","");
	
	public void addPositiveData(final Dataset localDataset) {
		if(localDataset.getPositiveDataset()==null) {
			throw new IllegalArgumentException("Positive dataset list can not be null");
		}
		this.positiveDataset.addAll(localDataset.getPositiveDataset());
		
		for(Entry<Category, Set<String>> entity:localDataset.positiveDatasetStatistic.entrySet()) {
			final Set<String> set = positiveDatasetStatistic.get(entity.getKey());
			if(set==null) {
				positiveDatasetStatistic.putAll(localDataset.positiveDatasetStatistic);
			}else {
				set.addAll(entity.getValue());
				positiveDatasetStatistic.put(entity.getKey(), set);
			}
		}
	}
	
	public void addNegativeDatasetDifficult(final Dataset localDataset) {
		if(localDataset==null) {
			throw new IllegalArgumentException("Negative difficult dataset list can not be null");
		}
		this.negativeDatasetDifficult.addAll(localDataset.getNegativeDatasetDifficult());
		
		for(Entry<Category, Set<String>> entity:localDataset.negativeDatasetStatistic.entrySet()) {
			final Set<String> set = negativeDatasetStatistic.get(entity.getKey());
			if(set==null) {
				negativeDatasetStatistic.putAll(localDataset.negativeDatasetStatistic);
			}else {
				set.addAll(entity.getValue());
				negativeDatasetStatistic.put(entity.getKey(), set);
			}
		}
	}
	
	public void addPositiveData(final Category category,final String positiveDataFull,final String positiveDataSentence){
		if(positiveDataFull==null || positiveDataFull.isEmpty()){
			throw new IllegalArgumentException("Postive data can not be null or empty");
		}
		positiveDataset.add(positiveDataFull);
		final Set<String> set = positiveDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(positiveDataSentence);
			positiveDatasetStatistic.put(category, newSet);
		}else {
			set.add(positiveDataSentence);
			positiveDatasetStatistic.put(category, set);
		}
	}
	
	public void addNegativeDifficultData(final Category category,final String negativeDataFull,final String negativeDataSentence){
		if(negativeDataFull==null || negativeDataFull.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDatasetDifficult.add(negativeDataFull);
		final Set<String> set = negativeDatasetStatistic.get(category);
		if(set==null) {
			final Set<String> newSet= new HashSet<>();
			newSet.add(negativeDataSentence);
			negativeDatasetStatistic.put(category, newSet);
		}else {
			set.add(negativeDataSentence);
			negativeDatasetStatistic.put(category, set);
		}
	}

	public void addNegativeEasyData(final String negativeDataFull){
		if(negativeDataFull==null || negativeDataFull.isEmpty()){
			throw new IllegalArgumentException("Negative data can not be null or empty");
		}
		negativeDatasetEasy.add(negativeDataFull);
	}
	
	public CopyOnWriteArrayList<String> getPositiveDataset() {
		return positiveDataset;
	}
	
	public CopyOnWriteArrayList<String> getNegativeEasyDataset() {
		return negativeDatasetEasy;
	}

	public CopyOnWriteArrayList<String> getNegativeDatasetDifficult() {
		return negativeDatasetDifficult;
	}
	
	public CopyOnWriteArrayList<String> getNegativeDatasetEasy() {
		return negativeDatasetEasy;
	}
	
	public void printPositiveDataset() {
		FileUtil.createFolder(WHERE_TO_WRITE_DATASET);
		FileUtil.writeDataToFile(positiveDataset, WHERE_TO_WRITE_DATASET+POSITIVE_DATASET_NAME,false);
	}
	
	public void printNegativeDatasetDifficult() {
		FileUtil.createFolder(WHERE_TO_WRITE_DATASET);
		FileUtil.writeDataToFile(negativeDatasetDifficult, WHERE_TO_WRITE_DATASET+DIFFICULT_NEGATIVE_DATASET_NAME,false);
	}
	
	public void printPoitiveNegativeDifficultDataset() {
		FileUtil.createFolder(WHERE_TO_WRITE_DATASET);
		FileUtil.writeDataToFile(negativeDatasetDifficultPlusPositive,WHERE_TO_WRITE_DATASET+NEAGTIVE_POSITIVE_DATASET_NAME,false);
	}
	
	public void printNegativeDatasetEasy() {		
		FileUtil.createFolder(WHERE_TO_WRITE_DATASET);
		FileUtil.writeDataToFile(negativeDatasetEasy, WHERE_TO_WRITE_DATASET+EASY_NEGATIVE_DATASET_NAME,false);
	}
	
//	public void printPositiveDatasetStatistic() {
//		for(Entry<Category, Set<String>> s:positiveDatasetStatistic.entrySet()){
//			positiveLog.info(s.getKey()+" == "+s.getValue().size());
//		}
//	}
//	
//	public void printNegativeDatasetStatistic() {
//		for(Entry<Category, Set<String>> s:negativeDatasetStatistic.entrySet()){
//			negativeDifficultLog.info(s.getKey()+" == "+s.getValue().size());
//		}
//	}
//	
//	public void printNegativeDatasetDifficultUnique() {		
//		for(Entry<Category, Set<String>> s:negativeDatasetStatistic.entrySet()){
//			for(String sentence:s.getValue()) {
//				negativeDifficultLog.info(s.getKey()+"\t"+sentence);
//			}
//		}
//	}
//	
//	public void printPositiveDatasetUnique() {		
//		for(Entry<Category, Set<String>> s:positiveDatasetStatistic.entrySet()){
//			for(String sentence:s.getValue()) {
//				positiveLog.info(s.getKey()+"\t"+sentence);
//			}
//		}
//	}
	
	public void addOnlyPositiveSentence(String positive) {
		if(positive == null || positive.equals("")) {
			throw new IllegalArgumentException("Positive sentence can not be null or empty");
		}
		positiveDataset.add(positive);
	}
	
	public void addOnlyDifficultNegativeSentence(String negative) {
		if(negative == null || negative.equals("")) {
			throw new IllegalArgumentException("Difficult negative sentence can not be null or empty");
		}
		negativeDatasetDifficult.add(negative);
	}
	
	public void addPositiveNegativeSentence(String positiveNegative) {
		if(positiveNegative == null || positiveNegative.equals("")) {
			throw new IllegalArgumentException("PositiveNegative sentence can not be null or empty");
		}
		negativeDatasetDifficultPlusPositive.add(positiveNegative);
	}
}
