package ise.roletagger.model;

import java.util.List;

/**
 * POJO to keep train and test dataset
 * @author fbm
 *
 */
public class TrainTestData {
	private final List<String> trainSet;
	private final List<String> testSet;

	public TrainTestData(List<String> trainSet, List<String> testSet) {
		this.trainSet = trainSet;
		this.testSet = testSet;
	}

	public List<String> getTrainSet() {
		return trainSet;
	}

	public List<String> getTestSet() {
		return testSet;
	}
	
}
